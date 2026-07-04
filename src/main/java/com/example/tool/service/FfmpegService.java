package com.example.tool.service;

import com.example.tool.model.VideoEditOptions;
import com.example.tool.model.VideoTextLayer;
import com.example.tool.config.MediaToolProperties;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class FfmpegService {

	private static final Pattern PTS_TIME_PATTERN = Pattern.compile("pts_time:([0-9]+(?:\\.[0-9]+)?)");
	private static final Pattern MEAN_VOLUME_PATTERN = Pattern.compile("mean_volume:\\s*(-?[0-9]+(?:\\.[0-9]+)?)\\s*dB");

	private final MediaToolProperties properties;

	public FfmpegService(MediaToolProperties properties) {
		this.properties = properties;
	}

	public boolean ffmpegAvailable() {
		return commandAvailable(properties.getFfmpegPath());
	}

	public boolean ffprobeAvailable() {
		return commandAvailable(properties.getFfprobePath());
	}

	public double probeDuration(Path input) {
		List<String> command = List.of(
				properties.getFfprobePath(),
				"-v", "error",
				"-show_entries", "format=duration",
				"-of", "default=noprint_wrappers=1:nokey=1",
				input.toAbsolutePath().toString());
		ProcessResult result = run(command, 30);
		if (result.exitCode() != 0) {
			throw new IllegalStateException("ffprobe failed for " + input + ": " + result.output());
		}
		try {
			return Double.parseDouble(result.output().trim());
		}
		catch (NumberFormatException ex) {
			throw new IllegalStateException("Could not parse duration for " + input + ": " + result.output(), ex);
		}
	}

	public boolean hasAudio(Path input) {
		List<String> command = List.of(
				properties.getFfprobePath(),
				"-v", "error",
				"-select_streams", "a:0",
				"-show_entries", "stream=index",
				"-of", "csv=p=0",
				input.toAbsolutePath().toString());
		ProcessResult result = run(command, 30);
		return result.exitCode() == 0 && !result.output().trim().isBlank();
	}

	public void renderEditedVideo(Path input, Path output, VideoEditOptions options, Path musicFile, Path textOverlay) {
		double start = Math.max(0, options.getStartSeconds() == null ? 0 : options.getStartSeconds());
		Double end = options.getEndSeconds();
		double duration = end == null || end <= start ? 0 : end - start;
		boolean hasTextOverlay = textOverlay != null && Files.isRegularFile(textOverlay);
		String videoFilter = editVideoFilter(options, !hasTextOverlay);
		String audioMode = normalizedAudioMode(options.getAudioMode());
		boolean muteOriginalAudio = Boolean.TRUE.equals(options.getMuteOriginalAudio());
		boolean hasMusic = musicFile != null;
		boolean hasInputAudio = !muteOriginalAudio && hasAudio(input);
		boolean mixAudio = hasMusic && "mix".equals(audioMode) && hasInputAudio;
		boolean musicOnly = hasMusic && ("replace".equals(audioMode) || ("mix".equals(audioMode) && !mixAudio));
		int nextInputIndex = 1;
		int musicInputIndex = -1;
		int textOverlayInputIndex = -1;

		List<String> command = new ArrayList<>();
		command.add(properties.getFfmpegPath());
		command.add("-y");
		command.add("-ss");
		command.add(formatSeconds(start));
		if (duration > 0) {
			command.add("-t");
			command.add(formatSeconds(duration));
		}
		command.add("-i");
		command.add(input.toAbsolutePath().toString());
		if (hasMusic) {
			musicInputIndex = nextInputIndex++;
			command.add("-i");
			command.add(musicFile.toAbsolutePath().toString());
		}
		if (hasTextOverlay) {
			textOverlayInputIndex = nextInputIndex++;
			command.add("-loop");
			command.add("1");
			command.add("-i");
			command.add(textOverlay.toAbsolutePath().toString());
		}

		command.add("-filter_complex");
		String videoComplex = "[0:v]" + videoFilter + "[v0]";
		if (hasTextOverlay) {
			videoComplex += ";[v0][" + textOverlayInputIndex + ":v]" + overlayFilter(options) + "[v]";
		}
		else {
			videoComplex += ";[v0]null[v]";
		}
		if (mixAudio) {
			command.add(videoComplex + ";[0:a][" + musicInputIndex + ":a]amix=inputs=2:duration=shortest:dropout_transition=0[a]");
			command.add("-map");
			command.add("[v]");
			command.add("-map");
			command.add("[a]");
			command.add("-shortest");
		}
		else {
			command.add(videoComplex);
			command.add("-map");
			command.add("[v]");
			if (musicOnly) {
				command.add("-map");
				command.add(musicInputIndex + ":a:0");
				command.add("-shortest");
			}
			else if (muteOriginalAudio) {
				command.add("-an");
			}
			else {
				command.add("-map");
				command.add("0:a?");
			}
		}
		command.add("-c:v");
		command.add("libx264");
		command.add("-preset");
		command.add("veryfast");
		command.add("-crf");
		command.add("22");
		command.add("-pix_fmt");
		command.add("yuv420p");
		command.add("-c:a");
		command.add("aac");
		command.add("-b:a");
		command.add("160k");
		command.add("-movflags");
		command.add("+faststart");
		command.add(output.toAbsolutePath().toString());
		runOrThrow(command, 900, "ffmpeg edit render failed");
	}

	public void renderTextLayerOverlays(Path input, Path output, List<VideoTextLayer> layers, List<Path> overlays) {
		if (layers == null || overlays == null || layers.isEmpty() || overlays.isEmpty()) {
			throw new IllegalArgumentException("Không có text layer để render.");
		}
		int count = Math.min(layers.size(), overlays.size());
		List<String> command = new ArrayList<>();
		command.add(properties.getFfmpegPath());
		command.add("-y");
		command.add("-i");
		command.add(input.toAbsolutePath().toString());
		for (int i = 0; i < count; i++) {
			command.add("-loop");
			command.add("1");
			command.add("-i");
			command.add(overlays.get(i).toAbsolutePath().toString());
		}

		StringBuilder filter = new StringBuilder("[0:v]null[v0]");
		for (int i = 0; i < count; i++) {
			VideoTextLayer layer = layers.get(i);
			double xPercent = clampPercent(layer.getTextXPercent() == null ? 50.0 : layer.getTextXPercent());
			double yPercent = clampPercent(layer.getTextYPercent() == null ? 82.0 : layer.getTextYPercent());
			double start = Math.max(0, layer.getStartSeconds());
			double end = Math.max(start + 0.1, layer.getEndSeconds());
			filter.append(";[v").append(i).append("][").append(i + 1).append(":v]")
					.append(String.format(Locale.ROOT,
							"overlay=x=(main_w-overlay_w)*%.4f:y=(main_h-overlay_h)*%.4f:enable='between(t,%.3f,%.3f)':format=auto:shortest=1",
							xPercent / 100.0, yPercent / 100.0, start, end))
					.append("[v").append(i + 1).append("]");
		}

		command.add("-filter_complex");
		command.add(filter.toString());
		command.add("-map");
		command.add("[v" + count + "]");
		command.add("-map");
		command.add("0:a?");
		command.add("-c:v");
		command.add("libx264");
		command.add("-preset");
		command.add("veryfast");
		command.add("-crf");
		command.add("22");
		command.add("-pix_fmt");
		command.add("yuv420p");
		command.add("-c:a");
		command.add("copy");
		command.add("-movflags");
		command.add("+faststart");
		command.add(output.toAbsolutePath().toString());
		runOrThrow(command, 900, "ffmpeg text layer render failed");
	}

	public void applyEditAudio(Path input, Path output, String audioMode, Path musicFile, boolean muteOriginalAudio) {
		if (musicFile == null || "keep".equals(normalizedAudioMode(audioMode))) {
			List<String> command = new ArrayList<>();
			command.add(properties.getFfmpegPath());
			command.add("-y");
			command.add("-i");
			command.add(input.toAbsolutePath().toString());
			command.add("-map");
			command.add("0:v:0");
			if (muteOriginalAudio) {
				command.add("-an");
			}
			else {
				command.add("-map");
				command.add("0:a?");
				command.add("-c:a");
				command.add("copy");
			}
			command.add("-c:v");
			command.add("copy");
			command.add("-movflags");
			command.add("+faststart");
			command.add(output.toAbsolutePath().toString());
			runOrThrow(command, 600, "ffmpeg copy edited video failed");
			return;
		}
		boolean mixAudio = !muteOriginalAudio && "mix".equals(normalizedAudioMode(audioMode)) && hasAudio(input);
		List<String> command = new ArrayList<>();
		command.add(properties.getFfmpegPath());
		command.add("-y");
		command.add("-i");
		command.add(input.toAbsolutePath().toString());
		command.add("-i");
		command.add(musicFile.toAbsolutePath().toString());
		if (mixAudio) {
			command.add("-filter_complex");
			command.add("[0:a][1:a]amix=inputs=2:duration=shortest:dropout_transition=0[a]");
			command.add("-map");
			command.add("0:v:0");
			command.add("-map");
			command.add("[a]");
			command.add("-c:v");
			command.add("copy");
		}
		else {
			command.add("-map");
			command.add("0:v:0");
			command.add("-map");
			command.add("1:a:0");
			command.add("-c:v");
			command.add("copy");
			command.add("-shortest");
		}
		command.add("-c:a");
		command.add("aac");
		command.add("-b:a");
		command.add("160k");
		command.add("-movflags");
		command.add("+faststart");
		command.add(output.toAbsolutePath().toString());
		runOrThrow(command, 900, "ffmpeg edit audio render failed");
	}

	public void createSegment(Path input, Path output, double start, double duration, int width, int height, boolean mirror) {
		String filter = String.format(Locale.ROOT,
				"scale=%d:%d:force_original_aspect_ratio=increase,crop=%d:%d,setsar=1,fps=30%s",
				width, height, width, height, mirror ? ",hflip" : "");
		List<String> command = new ArrayList<>();
		command.add(properties.getFfmpegPath());
		command.add("-y");
		command.add("-ss");
		command.add(formatSeconds(start));
		command.add("-t");
		command.add(formatSeconds(duration));
		command.add("-i");
		command.add(input.toAbsolutePath().toString());
		command.add("-vf");
		command.add(filter);
		command.add("-an");
		command.add("-c:v");
		command.add("libx264");
		command.add("-preset");
		command.add("veryfast");
		command.add("-crf");
		command.add("23");
		command.add("-pix_fmt");
		command.add("yuv420p");
		command.add(output.toAbsolutePath().toString());
		runOrThrow(command, 600, "ffmpeg segment render failed");
	}

	public void createSegmentWithAudio(Path input, Path output, double start, double duration, int width, int height) {
		String filter = String.format(Locale.ROOT,
				"scale=%d:%d:force_original_aspect_ratio=increase,crop=%d:%d,setsar=1,fps=30",
				width, height, width, height);
		List<String> command = new ArrayList<>();
		command.add(properties.getFfmpegPath());
		command.add("-y");
		command.add("-ss");
		command.add(formatSeconds(start));
		command.add("-t");
		command.add(formatSeconds(duration));
		command.add("-i");
		command.add(input.toAbsolutePath().toString());
		command.add("-map");
		command.add("0:v:0");
		command.add("-map");
		command.add("0:a?");
		command.add("-vf");
		command.add(filter);
		command.add("-c:v");
		command.add("libx264");
		command.add("-preset");
		command.add("veryfast");
		command.add("-crf");
		command.add("23");
		command.add("-pix_fmt");
		command.add("yuv420p");
		command.add("-c:a");
		command.add("aac");
		command.add("-b:a");
		command.add("128k");
		command.add(output.toAbsolutePath().toString());
		runOrThrow(command, 600, "ffmpeg highlight segment render failed");
	}

	public List<Double> detectSceneTimes(Path input, double threshold) {
		String filter = String.format(Locale.ROOT, "select=gt(scene\\,%.2f),showinfo", threshold);
		List<String> command = List.of(
				properties.getFfmpegPath(),
				"-hide_banner",
				"-i", input.toAbsolutePath().toString(),
				"-vf", filter,
				"-an",
				"-f", "null",
				"-");
		ProcessResult result = run(command, 900);
		if (result.exitCode() != 0) {
			throw new IllegalStateException("ffmpeg scene detection failed: " + result.output());
		}
		List<Double> times = new ArrayList<>();
		Matcher matcher = PTS_TIME_PATTERN.matcher(result.output());
		while (matcher.find()) {
			times.add(Double.parseDouble(matcher.group(1)));
		}
		return times;
	}

	public double measureMeanVolume(Path input, double start, double duration) {
		List<String> command = List.of(
				properties.getFfmpegPath(),
				"-hide_banner",
				"-ss", formatSeconds(start),
				"-t", formatSeconds(duration),
				"-i", input.toAbsolutePath().toString(),
				"-vn",
				"-af", "volumedetect",
				"-f", "null",
				"-");
		ProcessResult result = run(command, 120);
		if (result.exitCode() != 0) {
			return -60.0;
		}
		Matcher matcher = MEAN_VOLUME_PATTERN.matcher(result.output());
		if (!matcher.find()) {
			return -60.0;
		}
		return Double.parseDouble(matcher.group(1));
	}

	public void concatSegments(Path concatList, Path output) {
		List<String> command = List.of(
				properties.getFfmpegPath(),
				"-y",
				"-f", "concat",
				"-safe", "0",
				"-i", concatList.toAbsolutePath().toString(),
				"-c", "copy",
				output.toAbsolutePath().toString());
		runOrThrow(command, 600, "ffmpeg concat failed");
	}

	public void concatSegmentsReencoded(Path concatList, Path output) {
		List<String> command = List.of(
				properties.getFfmpegPath(),
				"-y",
				"-f", "concat",
				"-safe", "0",
				"-i", concatList.toAbsolutePath().toString(),
				"-c:v", "libx264",
				"-preset", "veryfast",
				"-crf", "23",
				"-pix_fmt", "yuv420p",
				"-c:a", "aac",
				"-b:a", "128k",
				"-movflags", "+faststart",
				output.toAbsolutePath().toString());
		runOrThrow(command, 600, "ffmpeg concat failed");
	}

	public void addAudioOrSilence(Path inputVideo, Path outputVideo, Path backgroundAudio) {
		List<String> command = new ArrayList<>();
		command.add(properties.getFfmpegPath());
		command.add("-y");
		if (backgroundAudio != null) {
			command.add("-stream_loop");
			command.add("-1");
			command.add("-i");
			command.add(backgroundAudio.toAbsolutePath().toString());
			command.add("-i");
			command.add(inputVideo.toAbsolutePath().toString());
			command.add("-shortest");
			command.add("-map");
			command.add("1:v:0");
			command.add("-map");
			command.add("0:a:0");
			command.add("-c:v");
			command.add("copy");
			command.add("-c:a");
			command.add("aac");
			command.add("-b:a");
			command.add("128k");
		}
		else {
			command.add("-i");
			command.add(inputVideo.toAbsolutePath().toString());
			command.add("-f");
			command.add("lavfi");
			command.add("-i");
			command.add("anullsrc=channel_layout=stereo:sample_rate=44100");
			command.add("-shortest");
			command.add("-map");
			command.add("0:v:0");
			command.add("-map");
			command.add("1:a:0");
			command.add("-c:v");
			command.add("copy");
			command.add("-c:a");
			command.add("aac");
			command.add("-b:a");
			command.add("128k");
		}
		command.add(outputVideo.toAbsolutePath().toString());
		runOrThrow(command, 600, "ffmpeg final audio mux failed");
	}

	private boolean commandAvailable(String executable) {
		try {
			Process process = new ProcessBuilder(executable, "-version")
					.redirectErrorStream(true)
					.start();
			return process.waitFor(10, TimeUnit.SECONDS) && process.exitValue() == 0;
		}
		catch (IOException | InterruptedException ex) {
			if (ex instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			return false;
		}
	}

	private void runOrThrow(List<String> command, long timeoutSeconds, String message) {
		ProcessResult result = run(command, timeoutSeconds);
		if (result.exitCode() != 0) {
			throw new IllegalStateException(message + ": " + result.output());
		}
	}

	private ProcessResult run(List<String> command, long timeoutSeconds) {
		try {
			Process process = new ProcessBuilder(command)
					.redirectErrorStream(true)
					.start();
			ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
			Thread outputReader = new Thread(() -> {
				try {
					process.getInputStream().transferTo(outputBuffer);
				}
				catch (IOException ignored) {
					// The process may be destroyed on timeout while the stream is being read.
				}
			}, "ffmpeg-output-reader");
			outputReader.setDaemon(true);
			outputReader.start();
			boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				outputReader.join(TimeUnit.SECONDS.toMillis(2));
				return new ProcessResult(124, "Command timed out: " + String.join(" ", command));
			}
			outputReader.join(TimeUnit.SECONDS.toMillis(2));
			String output = outputBuffer.toString(StandardCharsets.UTF_8);
			return new ProcessResult(process.exitValue(), output);
		}
		catch (IOException ex) {
			return new ProcessResult(127, ex.getMessage());
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return new ProcessResult(130, ex.getMessage());
		}
	}

	private String formatSeconds(double seconds) {
		return String.format(Locale.ROOT, "%.3f", Math.max(0, seconds));
	}

	private String editVideoFilter(VideoEditOptions options, boolean includeDrawText) {
		List<String> filters = new ArrayList<>();
		double zoom = options.getVideoZoom() == null ? 1 : clamp(options.getVideoZoom(), 0.5, 2.5);
		if (Math.abs(zoom - 1.0) > 0.001) {
			String zoomValue = String.format(Locale.ROOT, "%.4f", zoom);
			if (zoom > 1.0) {
				filters.add("scale=trunc(iw*" + zoomValue + "/2)*2:trunc(ih*" + zoomValue + "/2)*2");
				filters.add("crop=trunc(iw/" + zoomValue + "/2)*2:trunc(ih/" + zoomValue + "/2)*2");
			}
			else {
				filters.add("scale=trunc(iw*" + zoomValue + "/2)*2:trunc(ih*" + zoomValue + "/2)*2");
				filters.add("pad=trunc(iw/" + zoomValue + "/2)*2:trunc(ih/" + zoomValue + "/2)*2:(ow-iw)/2:(oh-ih)/2:black");
			}
		}
		double rotation = options.getRotationDegrees() == null ? 0 : options.getRotationDegrees();
		if (Math.abs(rotation) > 0.001) {
			double radians = rotation * Math.PI / 180.0;
			String angle = String.format(Locale.ROOT, "%.8f", radians);
			filters.add("rotate=" + angle + ":ow=iw:oh=ih:fillcolor=black");
		}
		int outputWidth = sanitizedOutputDimension(options.getOutputWidth());
		int outputHeight = sanitizedOutputDimension(options.getOutputHeight());
		if (outputWidth > 0 && outputHeight > 0) {
			filters.add(String.format(Locale.ROOT,
					"scale=%d:%d:force_original_aspect_ratio=increase,crop=%d:%d",
					outputWidth, outputHeight, outputWidth, outputHeight));
		}
		else {
			filters.add("scale=trunc(iw/2)*2:trunc(ih/2)*2");
		}
		String text = includeDrawText && options.getOverlayText() != null ? options.getOverlayText().trim() : "";
		if (!text.isBlank()) {
			int size = Math.max(18, Math.min(160, options.getTextSize() == null ? 42 : options.getTextSize()));
			String color = safeTextColor(options.getTextColor());
			String background = safeTextBackground(options.getTextBackground());
			double xPercent = clampPercent(options.getTextXPercent() == null ? 50.0 : options.getTextXPercent());
			double yPercent = clampPercent(options.getTextYPercent() == null ? 82.0 : options.getTextYPercent());
			int boxBorder = textBoxBorder(background);
			String x = String.format(Locale.ROOT, "(w-text_w-%d)*%.4f+%d", boxBorder * 2, xPercent / 100.0, boxBorder);
			String y = String.format(Locale.ROOT, "(h-text_h-%d)*%.4f+%d", boxBorder * 2, yPercent / 100.0, boxBorder);
			StringBuilder drawText = new StringBuilder("drawtext=")
					.append(fontOption(options.getTextFont()))
					.append(":text='").append(escapeDrawText(text)).append("'")
					.append(":fontcolor=").append(color)
					.append(":fontsize=").append(size)
					.append(":x=").append(x)
					.append(":y=").append(y);
			if ("outline".equals(background)) {
				drawText.append(":borderw=3:bordercolor=black@0.90");
			}
			if (boxBorder > 0) {
				drawText.append(":box=1:boxcolor=").append(textBoxColor(background))
						.append(":boxborderw=").append(boxBorder);
			}
			filters.add(drawText.toString());
		}
		filters.add("setsar=1");
		return String.join(",", filters);
	}

	private String overlayFilter(VideoEditOptions options) {
		double xPercent = clampPercent(options.getTextXPercent() == null ? 50.0 : options.getTextXPercent());
		double yPercent = clampPercent(options.getTextYPercent() == null ? 82.0 : options.getTextYPercent());
		return String.format(Locale.ROOT,
				"overlay=x=(main_w-overlay_w)*%.4f:y=(main_h-overlay_h)*%.4f:format=auto:shortest=1",
				xPercent / 100.0,
				yPercent / 100.0);
	}

	private int sanitizedOutputDimension(Integer value) {
		if (value == null || value < 2) {
			return 0;
		}
		int safe = Math.max(2, Math.min(7680, value));
		return safe % 2 == 0 ? safe : safe - 1;
	}

	private String fontOption(String font) {
		String fontFile = fontFilePath(font);
		if (fontFile != null) {
			return "fontfile='" + escapeDrawText(fontFile) + "'";
		}
		return "font='" + escapeDrawText(fontName(font)) + "'";
	}

	private String fontFilePath(String font) {
		String fileName;
		switch ((font == null ? "" : font).toLowerCase(Locale.ROOT)) {
			case "segoe":
				fileName = "segoeui.ttf";
				break;
			case "tahoma":
				fileName = "tahoma.ttf";
				break;
			case "verdana":
				fileName = "verdana.ttf";
				break;
			case "impact":
				fileName = "impact.ttf";
				break;
			case "georgia":
				fileName = "georgia.ttf";
				break;
			case "times":
				fileName = "times.ttf";
				break;
			case "comic":
				fileName = "comic.ttf";
				break;
			case "arial":
			default:
				fileName = "arial.ttf";
				break;
		}
		String windowsDirectory = System.getenv("WINDIR");
		if (windowsDirectory == null || windowsDirectory.isBlank()) {
			windowsDirectory = "C:/Windows";
		}
		Path fontPath = Path.of(windowsDirectory, "Fonts", fileName);
		if (Files.isRegularFile(fontPath)) {
			return fontPath.toAbsolutePath().toString().replace("\\", "/");
		}
		return null;
	}

	private String fontName(String font) {
		switch ((font == null ? "" : font).toLowerCase(Locale.ROOT)) {
			case "segoe":
				return "Segoe UI";
			case "tahoma":
				return "Tahoma";
			case "verdana":
				return "Verdana";
			case "impact":
				return "Impact";
			case "georgia":
				return "Georgia";
			case "times":
				return "Times New Roman";
			case "comic":
				return "Comic Sans MS";
			case "arial":
			default:
				return "Arial";
		}
	}

	private String safeTextBackground(String background) {
		String value = background == null ? "" : background.trim().toLowerCase(Locale.ROOT);
		if ("none".equals(value) || "outline".equals(value) || "light".equals(value) || "highlight".equals(value)) {
			return value;
		}
		return "dark";
	}

	private int textBoxBorder(String background) {
		return "dark".equals(background) || "light".equals(background) || "highlight".equals(background) ? 14 : 0;
	}

	private String textBoxColor(String background) {
		if ("light".equals(background)) {
			return "white@0.76";
		}
		if ("highlight".equals(background)) {
			return "yellow@0.82";
		}
		return "black@0.45";
	}

	private double clampPercent(double value) {
		return Math.max(0, Math.min(100, value));
	}

	private double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}

	private String normalizedAudioMode(String audioMode) {
		String value = audioMode == null ? "" : audioMode.trim().toLowerCase(Locale.ROOT);
		if ("replace".equals(value) || "mix".equals(value)) {
			return value;
		}
		return "keep";
	}

	private String safeTextColor(String color) {
		String value = color == null ? "" : color.trim();
		if (value.matches("#[0-9a-fA-F]{6}")) {
			return "0x" + value.substring(1);
		}
		if (value.matches("[a-zA-Z]+")) {
			return value.toLowerCase(Locale.ROOT);
		}
		return "white";
	}

	private String escapeDrawText(String text) {
		return text
				.replace("\\", "\\\\")
				.replace(":", "\\:")
				.replace("'", "\\'")
				.replace("%", "\\%");
	}

	private static class ProcessResult {
		private final int exitCode;
		private final String output;

		ProcessResult(int exitCode, String output) {
			this.exitCode = exitCode;
			this.output = output;
		}

		int exitCode() {
			return exitCode;
		}

		String output() {
			return output;
		}
	}
}
