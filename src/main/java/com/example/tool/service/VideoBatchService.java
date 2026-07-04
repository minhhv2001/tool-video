package com.example.tool.service;

import com.example.tool.model.BatchRequest;
import com.example.tool.model.BatchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class VideoBatchService {

	private static final List<String> VIDEO_EXTENSIONS = List.of(".mp4", ".mov", ".m4v", ".webm", ".mkv");

	private final MediaWorkspace workspace;
	private final FfmpegService ffmpegService;
	private final CreativeBriefService creativeBriefService;
	private final ObjectMapper objectMapper;

	public VideoBatchService(
			MediaWorkspace workspace,
			FfmpegService ffmpegService,
			CreativeBriefService creativeBriefService,
			ObjectMapper objectMapper) {
		this.workspace = workspace;
		this.ffmpegService = ffmpegService;
		this.creativeBriefService = creativeBriefService;
		this.objectMapper = objectMapper;
	}

	public BatchResult createBatch(BatchRequest rawRequest) {
		BatchRequest request = rawRequest.withDefaults();
		if (!ffmpegService.ffmpegAvailable() || !ffmpegService.ffprobeAvailable()) {
			throw new IllegalStateException("ffmpeg/ffprobe is not available. Install ffmpeg or set app.media.ffmpeg-path and app.media.ffprobe-path.");
		}
		workspace.ensureBaseDirectories();
		Path sourceDirectory = workspace.resolveUserPath(request.sourceDirectory(), workspace.sourcesDirectory());
		Path outputDirectory = workspace.resolveUserPath(request.outputDirectory(), workspace.outputsDirectory());
		Path backgroundAudio = request.backgroundAudioPath() == null
				? null
				: workspace.resolveUserPath(request.backgroundAudioPath(), workspace.workspace());
		validateDirectories(sourceDirectory, outputDirectory, backgroundAudio);

		List<Path> sourceVideos = listVideos(sourceDirectory);
		if (sourceVideos.isEmpty()) {
			throw new IllegalArgumentException("No source videos found in " + sourceDirectory);
		}

		String jobId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + "-" + UUID.randomUUID().toString().substring(0, 8);
		Path jobDirectory = outputDirectory.resolve("job-" + jobId);
		Path tempDirectory = jobDirectory.resolve("_work");
		try {
			Files.createDirectories(tempDirectory);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not create output directory " + jobDirectory, ex);
		}

		Random random = new Random(request.seed() == null ? System.currentTimeMillis() : request.seed());
		List<String> outputs = new ArrayList<>();
		String brief = creativeBriefService.buildBrief(request.topic(), request.videosToGenerate());
		List<String> warnings = new ArrayList<>();
		warnings.add("Outputs are generated from the provided/licensed sources only; verify rights before publishing.");

		for (int videoIndex = 1; videoIndex <= request.videosToGenerate(); videoIndex++) {
			Path variantDirectory = tempDirectory.resolve("variant-" + videoIndex);
			try {
				Files.createDirectories(variantDirectory);
			}
			catch (IOException ex) {
				throw new IllegalStateException("Could not create variant directory " + variantDirectory, ex);
			}
			List<Path> selectedSources = selectSources(sourceVideos, request.clipsPerVideo(), request.shuffleClips(), random);
			List<Path> segments = renderSegments(request, selectedSources, variantDirectory, videoIndex, random);
			Path concatList = variantDirectory.resolve("concat.txt");
			writeConcatList(concatList, segments);
			Path merged = variantDirectory.resolve("merged.mp4");
			ffmpegService.concatSegments(concatList, merged);
			Path finalOutput = jobDirectory.resolve(slug(request.topic()) + "-" + String.format(Locale.ROOT, "%03d", videoIndex) + ".mp4");
			ffmpegService.addAudioOrSilence(merged, finalOutput, backgroundAudio);
			outputs.add(finalOutput.toAbsolutePath().toString());
		}

		writeJobFiles(jobDirectory, request, brief, outputs);
		return new BatchResult(jobId, "completed", brief, outputs, warnings);
	}

	private List<Path> renderSegments(BatchRequest request, List<Path> selectedSources, Path variantDirectory, int videoIndex, Random random) {
		List<Path> segments = new ArrayList<>();
		for (int clipIndex = 0; clipIndex < selectedSources.size(); clipIndex++) {
			Path source = selectedSources.get(clipIndex);
			double sourceDuration = Math.max(0.5, ffmpegService.probeDuration(source));
			double clipDuration = randomDouble(request.minClipSeconds(), request.maxClipSeconds(), random);
			clipDuration = Math.min(clipDuration, Math.max(0.5, sourceDuration - 0.25));
			double maxStart = Math.max(0, sourceDuration - clipDuration - 0.25);
			double start = maxStart <= 0 ? 0 : random.nextDouble() * maxStart;
			Path segment = variantDirectory.resolve("clip-" + String.format(Locale.ROOT, "%02d", clipIndex + 1) + ".mp4");
			boolean mirror = request.mirrorEveryOtherVideo() && videoIndex % 2 == 0;
			ffmpegService.createSegment(source, segment, start, clipDuration, request.targetWidth(), request.targetHeight(), mirror);
			segments.add(segment);
		}
		return segments;
	}

	private List<Path> selectSources(List<Path> sourceVideos, int clipsPerVideo, boolean shuffle, Random random) {
		List<Path> pool = new ArrayList<>(sourceVideos);
		if (shuffle) {
			Collections.shuffle(pool, random);
		}
		List<Path> selected = new ArrayList<>();
		for (int i = 0; i < clipsPerVideo; i++) {
			if (i >= pool.size()) {
				selected.add(pool.get(random.nextInt(pool.size())));
			}
			else {
				selected.add(pool.get(i));
			}
		}
		return selected;
	}

	private void validateDirectories(Path sourceDirectory, Path outputDirectory, Path backgroundAudio) {
		if (!Files.isDirectory(sourceDirectory)) {
			throw new IllegalArgumentException("Source directory does not exist: " + sourceDirectory);
		}
		try {
			Files.createDirectories(outputDirectory);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not create output directory: " + outputDirectory, ex);
		}
		if (backgroundAudio != null && !Files.isRegularFile(backgroundAudio)) {
			throw new IllegalArgumentException("Background audio file does not exist: " + backgroundAudio);
		}
	}

	private List<Path> listVideos(Path sourceDirectory) {
		try (var stream = Files.list(sourceDirectory)) {
			return stream.filter(Files::isRegularFile)
					.filter(this::isVideo)
					.sorted(Comparator.comparing(path -> path.getFileName().toString()))
					.collect(Collectors.toList());
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not read source directory: " + sourceDirectory, ex);
		}
	}

	private boolean isVideo(Path path) {
		String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
		return VIDEO_EXTENSIONS.stream().anyMatch(name::endsWith);
	}

	private double randomDouble(double min, double max, Random random) {
		double safeMin = Math.max(0.25, Math.min(min, max));
		double safeMax = Math.max(safeMin, Math.max(min, max));
		if (safeMax == safeMin) {
			return safeMin;
		}
		return safeMin + (random.nextDouble() * (safeMax - safeMin));
	}

	private void writeConcatList(Path concatList, List<Path> segments) {
		StringBuilder builder = new StringBuilder();
		for (Path segment : segments) {
			builder.append("file '")
					.append(segment.toAbsolutePath().normalize().toString().replace("\\", "/").replace("'", "'\\''"))
					.append("'\n");
		}
		try {
			Files.writeString(concatList, builder.toString(), StandardCharsets.UTF_8);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not write concat list: " + concatList, ex);
		}
	}

	private void writeJobFiles(Path jobDirectory, BatchRequest request, String brief, List<String> outputs) {
		try {
			Files.writeString(jobDirectory.resolve("brief.txt"), brief, StandardCharsets.UTF_8);
			Files.writeString(jobDirectory.resolve("manifest.json"),
					objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(new JobManifest(request, outputs)),
					StandardCharsets.UTF_8);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not write job metadata.", ex);
		}
	}

	private String slug(String value) {
		String slug = value == null ? "video" : value.toLowerCase(Locale.ROOT)
				.replaceAll("[^a-z0-9]+", "-")
				.replaceAll("(^-|-$)", "");
		return slug.isBlank() ? "video" : slug;
	}

	private static class JobManifest {
		private final BatchRequest request;
		private final List<String> outputs;

		JobManifest(BatchRequest request, List<String> outputs) {
			this.request = request;
			this.outputs = outputs;
		}

		public BatchRequest getRequest() {
			return request;
		}

		public List<String> getOutputs() {
			return outputs;
		}
	}
}
