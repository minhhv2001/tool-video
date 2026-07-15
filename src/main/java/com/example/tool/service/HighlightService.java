package com.example.tool.service;

import com.example.tool.model.HighlightHistoryItem;
import com.example.tool.model.HighlightHistoryPage;
import com.example.tool.model.HighlightJobStatus;
import com.example.tool.model.HighlightDeleteResult;
import com.example.tool.model.SplitClipDeleteRequest;
import com.example.tool.model.SplitClipDeleteResult;
import com.example.tool.model.SplitClipHistoryItem;
import com.example.tool.model.SplitClipHistoryPage;
import com.example.tool.model.VideoEditOptions;
import com.example.tool.model.VideoEditResult;
import com.example.tool.model.VideoEditSegment;
import com.example.tool.model.VideoTextLayer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class HighlightService {

	private static final Logger LOGGER = LogManager.getLogger(HighlightService.class);
	private static final int DEFAULT_WIDTH = 1080;
	private static final int DEFAULT_HEIGHT = 1920;
	private static final int DEFAULT_CLIP_COUNT = 6;
	private static final double DEFAULT_CLIP_SECONDS = 5.0;
	private static final double MAX_CLIP_SECONDS = 60.0;
	private static final double SCENE_THRESHOLD = 0.12;
	private static final int MAX_CANDIDATES_TO_SCORE = 90;
	private static final int MAX_AUDIO_PROBES_PER_SOURCE = 48;
	private static final int HISTORY_DELETE_THRESHOLD = 20;
	private static final int HISTORY_DELETE_OLDEST_COUNT = 10;
	private static final String CATEGORY_HIGHLIGHT = "highlight";
	private static final String CATEGORY_MANUAL_EDIT = "manual-edit";
	private static final String CATEGORY_MANUAL_EDIT_DRAFT = "manual-edit-draft";
	private static final String CATEGORY_FACEBOOK_BATCH = "facebook-batch";
	private static final List<String> VIDEO_EXTENSIONS = List.of(".mp4", ".mkv", ".webm", ".mov", ".m4v");

	private final MediaWorkspace workspace;
	private final FfmpegService ffmpegService;
	private final NetworkVideoDownloadService networkVideoDownloadService;
	private final ObjectMapper objectMapper;
	private final ExecutorService executorService = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors() / 2));
	private final Map<String, HighlightJobStatus> statuses = new ConcurrentHashMap<>();

	public HighlightService(MediaWorkspace workspace, FfmpegService ffmpegService, NetworkVideoDownloadService networkVideoDownloadService, ObjectMapper objectMapper) {
		this.workspace = workspace;
		this.ffmpegService = ffmpegService;
		this.networkVideoDownloadService = networkVideoDownloadService;
		this.objectMapper = objectMapper;
	}

	public HighlightJobStatus createHighlight(List<MultipartFile> files, Integer requestedClipCount, Double requestedClipSeconds, String cutNote, String aspectRatio, String owner) {
		List<MultipartFile> videos = files == null ? List.of() : files.stream()
				.filter(file -> file != null && !file.isEmpty())
				.collect(Collectors.toList());
		if (videos.isEmpty()) {
			throw new IllegalArgumentException("Hãy kéo thả hoặc chọn ít nhất 1 file video.");
		}
		if (!ffmpegService.ffmpegAvailable() || !ffmpegService.ffprobeAvailable()) {
			throw new IllegalStateException("Máy chưa có ffmpeg/ffprobe. Cài ffmpeg hoặc cấu hình đường dẫn ffmpeg trước khi render.");
		}

		workspace.ensureBaseDirectories();
		String jobId = newJobId();
		Path jobDirectory = workspace.jobsDirectory().resolve(jobId).normalize();
		Path uploadsDirectory = jobDirectory.resolve("upload");
		Path workDirectory = jobDirectory.resolve("work");
		Path outputDirectory = jobDirectory.resolve("output");
		List<String> inputNames = videos.stream().map(MultipartFile::getOriginalFilename).collect(Collectors.toList());
		String safeCutNote = sanitizeNote(cutNote);
		HighlightJobStatus status = new HighlightJobStatus(jobId, inputNames.get(0));
		status.setOwner(safeOwner(owner));
		status.setInputFileNames(inputNames);
		status.setCutNote(safeCutNote);
		statuses.put(jobId, status);
		LOGGER.info("[{}] Bắt đầu nhận {} video upload: {}. Ghi chú cắt: {}", jobId, videos.size(), inputNames, safeCutNote);
		try {
			Files.createDirectories(uploadsDirectory);
			Files.createDirectories(workDirectory);
			Files.createDirectories(outputDirectory);
			List<Path> uploadedVideos = new ArrayList<>();
			for (int i = 0; i < videos.size(); i++) {
				MultipartFile file = videos.get(i);
				Path uploadedVideo = uploadsDirectory.resolve(String.format(Locale.ROOT, "%02d-%s", i + 1, safeFileName(file.getOriginalFilename()))).normalize();
				file.transferTo(uploadedVideo);
				uploadedVideos.add(uploadedVideo);
			}

			int clipCount = clamp(requestedClipCount == null ? DEFAULT_CLIP_COUNT : requestedClipCount, 1, 30);
			double clipSeconds = clamp(requestedClipSeconds == null ? DEFAULT_CLIP_SECONDS : requestedClipSeconds, 1.0, MAX_CLIP_SECONDS);
			RenderProfile renderProfile = renderProfile(aspectRatio);
			progress(status, 8, "Đã tải " + uploadedVideos.size() + " video lên server. Đang đưa vào hàng xử lý."
					+ (safeCutNote.isBlank() ? "" : " Đã nhận ghi chú cắt."));
			saveManifest(jobDirectory, toHistory(status, "processing", 0, 0, null, null));

			LOGGER.info("[{}] Upload xong. Files={}, clipCount={}, clipSeconds={}, aspectRatio={}", jobId, uploadedVideos, clipCount, clipSeconds, renderProfile.label());
			CompletableFuture.runAsync(() -> processHighlight(status, uploadedVideos, workDirectory, outputDirectory, clipCount, clipSeconds, safeCutNote, renderProfile), executorService);
			return status;
		}
		catch (IOException | RuntimeException ex) {
			LOGGER.error("[{}] Không thể nhận video upload.", jobId, ex);
			status.failed(ex.getMessage());
			writeError(jobDirectory, ex);
			saveManifest(jobDirectory, toHistory(status, "error", 0, 0, null, ex.getMessage()));
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			throw new IllegalStateException("Không thể xử lý video upload.", ex);
		}
	}

	public HighlightJobStatus createSplitClips(List<MultipartFile> files, Integer requestedClipCount, Double requestedClipSeconds, String cutNote, String aspectRatio, String owner) {
		List<MultipartFile> videos = files == null ? List.of() : files.stream()
				.filter(file -> file != null && !file.isEmpty())
				.collect(Collectors.toList());
		if (videos.isEmpty()) {
			throw new IllegalArgumentException("Hãy kéo thả hoặc chọn ít nhất 1 file video.");
		}
		if (!ffmpegService.ffmpegAvailable() || !ffmpegService.ffprobeAvailable()) {
			throw new IllegalStateException("Máy chưa có ffmpeg/ffprobe. Cài ffmpeg hoặc cấu hình đường dẫn ffmpeg trước khi render.");
		}

		workspace.ensureBaseDirectories();
		String jobId = newJobId();
		Path jobDirectory = workspace.jobsDirectory().resolve(jobId).normalize();
		Path uploadsDirectory = jobDirectory.resolve("upload");
		Path outputDirectory = jobDirectory.resolve("output");
		List<String> inputNames = videos.stream().map(MultipartFile::getOriginalFilename).collect(Collectors.toList());
		String safeCutNote = sanitizeNote(cutNote);
		HighlightJobStatus status = new HighlightJobStatus(jobId, inputNames.get(0));
		status.setOwner(safeOwner(owner));
		status.setInputFileNames(inputNames);
		status.setCutNote(safeCutNote);
		statuses.put(jobId, status);
		LOGGER.info("[{}] Bắt đầu nhận {} video upload để tách clip: {}. Ghi chú cắt: {}", jobId, videos.size(), inputNames, safeCutNote);
		try {
			Files.createDirectories(uploadsDirectory);
			Files.createDirectories(outputDirectory);
			List<Path> uploadedVideos = new ArrayList<>();
			for (int i = 0; i < videos.size(); i++) {
				MultipartFile file = videos.get(i);
				Path uploadedVideo = uploadsDirectory.resolve(String.format(Locale.ROOT, "%02d-%s", i + 1, safeFileName(file.getOriginalFilename()))).normalize();
				file.transferTo(uploadedVideo);
				uploadedVideos.add(uploadedVideo);
			}

			int clipCount = clamp(requestedClipCount == null ? DEFAULT_CLIP_COUNT : requestedClipCount, 1, 30);
			double clipSeconds = clamp(requestedClipSeconds == null ? DEFAULT_CLIP_SECONDS : requestedClipSeconds, 1.0, MAX_CLIP_SECONDS);
			RenderProfile renderProfile = renderProfile(aspectRatio);
			progress(status, 8, "Đã tải " + uploadedVideos.size() + " video lên server. Đang đưa vào hàng tách clip."
					+ (safeCutNote.isBlank() ? "" : " Đã nhận ghi chú cắt."));
			saveSplitManifest(jobDirectory, List.of());

			LOGGER.info("[{}] Upload xong cho chế độ tách clip. Files={}, clipCount={}, clipSeconds={}, aspectRatio={}", jobId, uploadedVideos, clipCount, clipSeconds, renderProfile.label());
			CompletableFuture.runAsync(() -> processSplitClips(status, uploadedVideos, outputDirectory, clipCount, clipSeconds, safeCutNote, renderProfile), executorService);
			return status;
		}
		catch (IOException | RuntimeException ex) {
			LOGGER.error("[{}] Không thể nhận video upload để tách clip.", jobId, ex);
			status.failed(ex.getMessage());
			writeError(jobDirectory, ex);
			saveSplitManifest(jobDirectory, List.of(toSplitHistory(status, 1, "error", originalNameFor(status, 0), null, 0, 0, null, ex.getMessage())));
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			throw new IllegalStateException("Không thể xử lý video upload.", ex);
		}
	}

	public HighlightJobStatus createHighlightFromUrls(List<String> urls, Integer requestedClipCount, Double requestedClipSeconds, String cutNote, String cookiesFilePath, String aspectRatio, String owner) {
		List<String> videoUrls = normalizeUrls(urls);
		if (videoUrls.isEmpty()) {
			throw new IllegalArgumentException("Hãy nhập ít nhất 1 link video.");
		}
		if (!ffmpegService.ffmpegAvailable() || !ffmpegService.ffprobeAvailable()) {
			throw new IllegalStateException("Máy chưa có ffmpeg/ffprobe. Cài ffmpeg hoặc cấu hình đường dẫn ffmpeg trước khi render.");
		}

		workspace.ensureBaseDirectories();
		String jobId = newJobId();
		Path jobDirectory = workspace.jobsDirectory().resolve(jobId).normalize();
		Path uploadsDirectory = jobDirectory.resolve("upload");
		Path workDirectory = jobDirectory.resolve("work");
		Path outputDirectory = jobDirectory.resolve("output");
		String safeCutNote = sanitizeNote(cutNote);
		HighlightJobStatus status = new HighlightJobStatus(jobId, videoUrls.get(0));
		status.setOwner(safeOwner(owner));
		status.setInputFileNames(videoUrls);
		status.setCutNote(safeCutNote);
		statuses.put(jobId, status);
		int clipCount = clamp(requestedClipCount == null ? DEFAULT_CLIP_COUNT : requestedClipCount, 1, 30);
		double clipSeconds = clamp(requestedClipSeconds == null ? DEFAULT_CLIP_SECONDS : requestedClipSeconds, 1.0, MAX_CLIP_SECONDS);
		RenderProfile renderProfile = renderProfile(aspectRatio);
		progress(status, 3, "Đã nhận link video. Đang chuẩn bị tải bản nét nhất tối đa 2K.");
		saveManifest(jobDirectory, toHistory(status, "processing", 0, 0, null, null));
		CompletableFuture.runAsync(() -> processNetworkHighlight(status, videoUrls, uploadsDirectory, workDirectory, outputDirectory, clipCount, clipSeconds, safeCutNote, sanitizeNote(cookiesFilePath), renderProfile), executorService);
		return status;
	}

	public HighlightJobStatus createSplitClipsFromUrls(List<String> urls, Integer requestedClipCount, Double requestedClipSeconds, String cutNote, String cookiesFilePath, String aspectRatio, String owner) {
		List<String> videoUrls = normalizeUrls(urls);
		if (videoUrls.isEmpty()) {
			throw new IllegalArgumentException("Hãy nhập ít nhất 1 link video.");
		}
		if (!ffmpegService.ffmpegAvailable() || !ffmpegService.ffprobeAvailable()) {
			throw new IllegalStateException("Máy chưa có ffmpeg/ffprobe. Cài ffmpeg hoặc cấu hình đường dẫn ffmpeg trước khi render.");
		}

		workspace.ensureBaseDirectories();
		String jobId = newJobId();
		Path jobDirectory = workspace.jobsDirectory().resolve(jobId).normalize();
		Path uploadsDirectory = jobDirectory.resolve("upload");
		Path outputDirectory = jobDirectory.resolve("output");
		String safeCutNote = sanitizeNote(cutNote);
		HighlightJobStatus status = new HighlightJobStatus(jobId, videoUrls.get(0));
		status.setOwner(safeOwner(owner));
		status.setInputFileNames(videoUrls);
		status.setCutNote(safeCutNote);
		statuses.put(jobId, status);
		int clipCount = clamp(requestedClipCount == null ? DEFAULT_CLIP_COUNT : requestedClipCount, 1, 30);
		double clipSeconds = clamp(requestedClipSeconds == null ? DEFAULT_CLIP_SECONDS : requestedClipSeconds, 1.0, MAX_CLIP_SECONDS);
		RenderProfile renderProfile = renderProfile(aspectRatio);
		progress(status, 3, "Đã nhận link video. Đang chuẩn bị tải bản nét nhất tối đa 2K.");
		saveSplitManifest(jobDirectory, List.of());
		CompletableFuture.runAsync(() -> processNetworkSplitClips(status, videoUrls, uploadsDirectory, outputDirectory, clipCount, clipSeconds, safeCutNote, sanitizeNote(cookiesFilePath), renderProfile), executorService);
		return status;
	}

	public HighlightJobStatus status(String jobId, String owner) {
		assertJobOwner(jobId, owner);
		return status(jobId);
	}

	public HighlightJobStatus status(String jobId) {
		HighlightJobStatus status = statuses.get(jobId);
		if (status == null) {
			throw new IllegalArgumentException("Không tìm thấy job: " + jobId + ". Nếu server vừa khởi động lại, hãy xem trong danh sách lịch sử.");
		}
		return status;
	}

	public HighlightHistoryPage history(int page, int size, String owner) {
		workspace.ensureBaseDirectories();
		int safePage = Math.max(1, page);
		int safeSize = Math.max(1, Math.min(50, size));
		List<HighlightHistoryItem> items = readHistoryItems().stream()
				.filter(this::isHighlightHistory)
				.filter(item -> ownerMatches(item.getOwner(), owner))
				.collect(Collectors.toList());
		items.sort(Comparator.comparing(HighlightHistoryItem::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed());
		int totalPages = (int) Math.ceil(items.size() / (double) safeSize);
		int from = Math.min(items.size(), (safePage - 1) * safeSize);
		int to = Math.min(items.size(), from + safeSize);
		return new HighlightHistoryPage(items.subList(from, to), safePage, safeSize, items.size(), totalPages);
	}

	public HighlightHistoryPage manualEditHistory(int page, int size, String owner) {
		workspace.ensureBaseDirectories();
		int safePage = Math.max(1, page);
		int safeSize = Math.max(1, Math.min(50, size));
		List<HighlightHistoryItem> items = readHistoryItems().stream()
				.filter(this::isManualEditHistory)
				.filter(item -> ownerMatches(item.getOwner(), owner))
				.collect(Collectors.toList());
		items.sort(Comparator.comparing(HighlightHistoryItem::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed());
		int totalPages = (int) Math.ceil(items.size() / (double) safeSize);
		int from = Math.min(items.size(), (safePage - 1) * safeSize);
		int to = Math.min(items.size(), from + safeSize);
		return new HighlightHistoryPage(items.subList(from, to), safePage, safeSize, items.size(), totalPages);
	}

	public HighlightHistoryPage facebookBatchHistory(int page, int size, String owner) {
		workspace.ensureBaseDirectories();
		int safePage = Math.max(1, page);
		int safeSize = Math.max(1, Math.min(50, size));
		List<HighlightHistoryItem> items = readHistoryItems().stream()
				.filter(this::isFacebookBatchHistory)
				.filter(item -> ownerMatches(item.getOwner(), owner))
				.collect(Collectors.toList());
		items.sort(Comparator.comparing(HighlightHistoryItem::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed());
		int totalPages = (int) Math.ceil(items.size() / (double) safeSize);
		int from = Math.min(items.size(), (safePage - 1) * safeSize);
		int to = Math.min(items.size(), from + safeSize);
		return new HighlightHistoryPage(items.subList(from, to), safePage, safeSize, items.size(), totalPages);
	}

	public HighlightJobStatus createFacebookBatchDownload(String reelsUrl, Integer requestedStartIndex, Integer requestedEndIndex, String cookiesFilePath, String owner) {
		List<String> urls = normalizeBatchUrls(reelsUrl);
		if (urls.isEmpty()) {
			throw new IllegalArgumentException("Hãy nhập link danh sách video reels Facebook.");
		}
		workspace.ensureBaseDirectories();
		String jobId = newJobId();
		Path jobDirectory = jobDirectory(jobId);
		Path uploadDirectory = jobDirectory.resolve("upload");
		int startIndex = Math.max(1, requestedStartIndex == null ? 1 : requestedStartIndex);
		int endIndex = Math.max(startIndex, requestedEndIndex == null ? startIndex + 9 : requestedEndIndex);
		if (endIndex - startIndex + 1 > 50) {
			throw new IllegalArgumentException("Mỗi lần chỉ nên tải tối đa 50 video để tránh treo máy. Hãy chia nhỏ khoảng tải.");
		}
		String sourceUrl = urls.get(0);
		String note = "Facebook reels: " + sourceUrl + (urls.size() > 1 ? " và " + (urls.size() - 1) + " link khác" : "") + " | khoảng " + startIndex + "-" + endIndex;
		HighlightJobStatus status = new HighlightJobStatus(jobId, sourceUrl);
		status.setOwner(safeOwner(owner));
		status.setInputFileNames(urls);
		status.setCutNote(note);
		statuses.put(jobId, status);
		progress(status, 2, "Đã nhận link danh sách reels. Đang chuẩn bị tải hàng loạt.");
		HighlightHistoryItem item = toHistory(status, "processing", 0, 0, null, null);
		item.setCategory(CATEGORY_FACEBOOK_BATCH);
		saveManifest(jobDirectory, item);
		CompletableFuture.runAsync(() -> processFacebookBatchDownload(status, urls, uploadDirectory, startIndex, endIndex, sanitizeNote(cookiesFilePath)), executorService);
		return status;
	}

	public SplitClipHistoryPage splitHistory(int page, int size, String owner) {
		workspace.ensureBaseDirectories();
		int safePage = Math.max(1, page);
		int safeSize = Math.max(1, Math.min(50, size));
		List<SplitClipHistoryItem> items = readSplitHistoryItems().stream()
				.filter(item -> ownerMatches(item.getOwner(), owner))
				.collect(Collectors.toList());
		items.sort(Comparator.comparing((SplitClipHistoryItem item) -> item.getCreatedAt() == null ? "" : item.getCreatedAt()).reversed()
				.thenComparing(SplitClipHistoryItem::getJobId, Comparator.nullsLast(String::compareTo))
				.thenComparing(SplitClipHistoryItem::getClipIndex));
		int totalPages = (int) Math.ceil(items.size() / (double) safeSize);
		int from = Math.min(items.size(), (safePage - 1) * safeSize);
		int to = Math.min(items.size(), from + safeSize);
		return new SplitClipHistoryPage(items.subList(from, to), safePage, safeSize, items.size(), totalPages);
	}

	public VideoEditResult createEditableVideo(MultipartFile file, String owner) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("Hãy chọn hoặc kéo một file video để chỉnh sửa.");
		}
		if (!ffmpegService.ffmpegAvailable() || !ffmpegService.ffprobeAvailable()) {
			throw new IllegalStateException("Máy chưa có ffmpeg/ffprobe. Cài ffmpeg hoặc cấu hình đường dẫn ffmpeg trước khi render.");
		}
		workspace.ensureBaseDirectories();
		String jobId = newJobId();
		Path jobDirectory = jobDirectory(jobId);
		Path uploadDirectory = jobDirectory.resolve("upload");
		String originalName = file.getOriginalFilename() == null || file.getOriginalFilename().isBlank() ? "video-upload.mp4" : file.getOriginalFilename();
		Path uploadedVideo = uploadDirectory.resolve("01-" + safeFileName(originalName)).normalize();
		try {
			Files.createDirectories(uploadDirectory);
			file.transferTo(uploadedVideo);
			return createEditableVideoRecord(jobId, uploadedVideo, originalName, "Video upload để chỉnh sửa thủ công.", owner);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Không thể lưu video để chỉnh sửa.", ex);
		}
		catch (RuntimeException ex) {
			writeError(jobDirectory, ex);
			HighlightHistoryItem item = toHistory(new HighlightJobStatus(jobId, originalName), "error", 0, 0, null, ex.getMessage());
			item.setCategory(CATEGORY_MANUAL_EDIT_DRAFT);
			item.setOwner(safeOwner(owner));
			saveManifest(jobDirectory, item);
			throw ex;
		}
	}

	public VideoEditResult createEditableVideoFromUrls(List<String> urls, String cookiesFilePath, String owner) {
		List<String> videoUrls = normalizeUrls(urls);
		if (videoUrls.isEmpty()) {
			throw new IllegalArgumentException("Hãy nhập ít nhất 1 link video.");
		}
		if (!ffmpegService.ffmpegAvailable() || !ffmpegService.ffprobeAvailable()) {
			throw new IllegalStateException("Máy chưa có ffmpeg/ffprobe. Cài ffmpeg hoặc cấu hình đường dẫn ffmpeg trước khi render.");
		}
		workspace.ensureBaseDirectories();
		String jobId = newJobId();
		Path jobDirectory = jobDirectory(jobId);
		Path uploadDirectory = jobDirectory.resolve("upload");
		try {
			Files.createDirectories(uploadDirectory);
			Path downloaded = networkVideoDownloadService.downloadBestUpTo2k(videoUrls.get(0), uploadDirectory, 1, sanitizeNote(cookiesFilePath));
			return createEditableVideoRecord(jobId, downloaded, videoUrls.get(0), "Video tải từ link để chỉnh sửa thủ công.", owner);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Không thể chuẩn bị thư mục tải video từ link.", ex);
		}
		catch (RuntimeException ex) {
			writeError(jobDirectory, ex);
			HighlightJobStatus status = new HighlightJobStatus(jobId, videoUrls.get(0));
			status.setOwner(safeOwner(owner));
			HighlightHistoryItem item = toHistory(status, "error", 0, 0, null, ex.getMessage());
			item.setCategory(CATEGORY_MANUAL_EDIT_DRAFT);
			item.setOwner(safeOwner(owner));
			saveManifest(jobDirectory, item);
			throw ex;
		}
	}

	private VideoEditResult createEditableVideoRecord(String jobId, Path source, String title, String note, String owner) {
		Path jobDirectory = jobDirectory(jobId);
		Path workDirectory = jobDirectory.resolve("work");
		Path outputDirectory = jobDirectory.resolve("output");
		Path output = outputDirectory.resolve("highlight.mp4");
		try {
			Files.createDirectories(workDirectory);
			Files.createDirectories(outputDirectory);
			ffmpegService.remuxToMp4(source, output);
			double duration = ffmpegService.probeDuration(output);
			String now = Instant.now().toString();
			HighlightHistoryItem item = new HighlightHistoryItem(
					jobId,
					"ready",
					now,
					now,
					List.of(title),
					note,
					duration,
					1,
					"/api/highlights/" + jobId + "/download",
					null);
			item.setCategory(CATEGORY_MANUAL_EDIT_DRAFT);
			item.setOwner(safeOwner(owner));
			saveManifest(jobDirectory, item);
			enforceHistoryLimit(jobId);
			LOGGER.info("[{}] Đã tạo job chỉnh sửa thủ công từ {}", jobId, source);
			return new VideoEditResult(jobId, item.getDownloadUrl(), "/api/highlights/" + jobId + "/preview", "Đã tải video lên. Có thể chỉnh sửa ngay.");
		}
		catch (IOException ex) {
			throw new IllegalStateException("Không thể lưu video để chỉnh sửa.", ex);
		}
	}

	public void assertJobOwner(String jobId, String owner) {
		if (!isJobOwnedBy(jobId, owner)) {
			throw new IllegalArgumentException("Khong tim thay video cua tai khoan hien tai.");
		}
	}
	public Path downloadPath(String jobId) {
		Path output = jobDirectory(jobId).resolve("output").resolve("highlight.mp4").normalize();
		if (!Files.isRegularFile(output)) {
			throw new IllegalArgumentException("Không tìm thấy file highlight cho job: " + jobId);
		}
		LOGGER.info("[{}] Bắt đầu tải xuống file highlight: {}", jobId, output);
		return output;
	}

	public Path sourceDirectory(String jobId) {
		Path source = jobDirectory(jobId).resolve("upload").normalize();
		if (!Files.isDirectory(source)) {
			throw new IllegalArgumentException("Không tìm thấy thư mục video gốc cho job: " + jobId);
		}
		return source;
	}

	public Path highlightOutputPath(String jobId) {
		Path output = jobDirectory(jobId).resolve("output").resolve("highlight.mp4").normalize();
		if (!Files.isRegularFile(output)) {
			throw new IllegalArgumentException("Không tìm thấy file highlight cho job: " + jobId);
		}
		return output;
	}

	public Path splitClipOutputPath(String jobId, int clipIndex) {
		if (clipIndex < 1 || clipIndex > 999) {
			throw new IllegalArgumentException("Số thứ tự clip không hợp lệ.");
		}
		Path output = jobDirectory(jobId).resolve("output").resolve(String.format(Locale.ROOT, "clip-%02d.mp4", clipIndex)).normalize();
		if (!Files.isRegularFile(output)) {
			throw new IllegalArgumentException("Không tìm thấy clip " + clipIndex + " cho job: " + jobId);
		}
		return output;
	}

	public Path splitClipDownloadPath(String jobId, int clipIndex) {
		if (clipIndex < 1 || clipIndex > 999) {
			throw new IllegalArgumentException("Số thứ tự clip không hợp lệ.");
		}
		Path output = jobDirectory(jobId).resolve("output").resolve(String.format(Locale.ROOT, "clip-%02d.mp4", clipIndex)).normalize();
		if (!Files.isRegularFile(output)) {
			throw new IllegalArgumentException("Không tìm thấy clip " + clipIndex + " cho job: " + jobId);
		}
		LOGGER.info("[{}] Bắt đầu tải xuống clip {}: {}", jobId, clipIndex, output);
		return output;
	}

	public void cleanupJob(String jobId) {
		LOGGER.info("[{}] Tải xuống hoàn tất. Giữ lại file trên server theo cấu hình lịch sử.", jobId);
	}

	public HighlightDeleteResult deleteHighlights(List<String> jobIds, String owner) {
		workspace.ensureBaseDirectories();
		List<String> requestedIds = jobIds == null ? List.of() : jobIds.stream()
				.filter(id -> id != null && !id.isBlank())
				.distinct()
				.collect(Collectors.toList());
		List<String> deleted = new ArrayList<>();
		List<String> skipped = new ArrayList<>();
		for (String jobId : requestedIds) {
			if (deleteJobDirectory(jobId, "người dùng xóa lịch sử")) {
				deleted.add(jobId);
			}
			else {
				skipped.add(jobId);
			}
		}
		return new HighlightDeleteResult(deleted.size(), deleted, skipped);
	}

	public HighlightDeleteResult deleteManualEditVideos(List<String> jobIds, String owner) {
		workspace.ensureBaseDirectories();
		List<String> requestedIds = jobIds == null ? List.of() : jobIds.stream()
				.filter(id -> id != null && !id.isBlank())
				.distinct()
				.collect(Collectors.toList());
		List<String> deleted = new ArrayList<>();
		List<String> skipped = new ArrayList<>();
		for (String jobId : requestedIds) {
			Path manifest = jobDirectory(jobId).resolve("manifest.json");
			HighlightHistoryItem item = Files.isRegularFile(manifest) ? readManifest(manifest) : null;
			if (ownerMatches(item == null ? null : item.getOwner(), owner) && isManualEditHistory(item) && deleteJobDirectory(jobId, "người dùng xóa lịch sử edit video")) {
				deleted.add(jobId);
			}
			else {
				skipped.add(jobId);
			}
		}
		return new HighlightDeleteResult(deleted.size(), deleted, skipped);
	}

	public HighlightDeleteResult deleteFacebookBatchVideos(List<String> jobIds, String owner) {
		workspace.ensureBaseDirectories();
		List<String> requestedIds = jobIds == null ? List.of() : jobIds.stream()
				.filter(id -> id != null && !id.isBlank())
				.distinct()
				.collect(Collectors.toList());
		List<String> deleted = new ArrayList<>();
		List<String> skipped = new ArrayList<>();
		for (String jobId : requestedIds) {
			Path manifest = jobDirectory(jobId).resolve("manifest.json");
			HighlightHistoryItem item = Files.isRegularFile(manifest) ? readManifest(manifest) : null;
			if (ownerMatches(item == null ? null : item.getOwner(), owner) && isFacebookBatchHistory(item) && deleteJobDirectory(jobId, "người dùng xóa batch Facebook")) {
				deleted.add(jobId);
			}
			else {
				skipped.add(jobId);
			}
		}
		return new HighlightDeleteResult(deleted.size(), deleted, skipped);
	}

	public SplitClipDeleteResult deleteSplitClips(List<SplitClipDeleteRequest.ClipRef> clips, String owner) {
		workspace.ensureBaseDirectories();
		List<SplitClipDeleteRequest.ClipRef> requestedClips = clips == null ? List.of() : clips.stream()
				.filter(ref -> ref != null && ref.getJobId() != null && !ref.getJobId().isBlank() && ref.getClipIndex() > 0)
				.collect(Collectors.collectingAndThen(
						Collectors.toMap(
								ref -> splitClipKey(ref.getJobId(), ref.getClipIndex()),
								ref -> ref,
								(first, ignored) -> first,
								java.util.LinkedHashMap::new),
						map -> new ArrayList<>(map.values())));
		List<String> deleted = new ArrayList<>();
		List<String> skipped = new ArrayList<>();
		for (SplitClipDeleteRequest.ClipRef clip : requestedClips) {
			String key = splitClipKey(clip.getJobId(), clip.getClipIndex());
			if (isJobOwnedBy(clip.getJobId(), owner) && deleteSplitClip(clip.getJobId(), clip.getClipIndex())) {
				deleted.add(key);
			}
			else {
				skipped.add(key);
			}
		}
		return new SplitClipDeleteResult(deleted.size(), deleted, skipped);
	}

	public VideoEditResult editHighlight(String jobId, VideoEditOptions options, MultipartFile musicFile, MultipartFile textOverlayFile, List<MultipartFile> textLayerOverlayFiles) {
		workspace.ensureBaseDirectories();
		VideoEditOptions safeOptions = options == null ? new VideoEditOptions() : options;
		Path source = editSourceForHighlight(jobId, safeOptions);
		String sourceTitle = historyTitle(jobId, source.getFileName().toString());
		if (overwriteEdit(safeOptions)) {
			Path jobDirectory = jobDirectory(jobId);
			Path workDirectory = jobDirectory.resolve("work");
			Path output = jobDirectory.resolve("output").resolve("highlight.mp4");
			renderEditedToOutput(source, output, workDirectory, safeOptions, musicFile, textOverlayFile, textLayerOverlayFiles);
			updateHighlightManifest(jobId, editTitle(safeOptions, sourceTitle), output, safeOptions);
			LOGGER.info("[{}] Đã ghi đè video sau chỉnh sửa: {}", jobId, output);
			return new VideoEditResult(jobId, "/api/highlights/" + jobId + "/download", "/api/highlights/" + jobId + "/preview", "Đã lưu đè video đã chỉnh sửa.");
		}
		return createEditedHighlightRecord(source, safeOptions, musicFile, textOverlayFile, textLayerOverlayFiles, sourceTitle);
	}

	public VideoEditResult editSplitClip(String jobId, int clipIndex, VideoEditOptions options, MultipartFile musicFile, MultipartFile textOverlayFile, List<MultipartFile> textLayerOverlayFiles) {
		workspace.ensureBaseDirectories();
		VideoEditOptions safeOptions = options == null ? new VideoEditOptions() : options;
		Path source = editSourceForSplitClip(jobId, clipIndex, safeOptions);
		String sourceTitle = splitClipTitle(jobId, clipIndex, source.getFileName().toString());
		if (overwriteEdit(safeOptions)) {
			Path jobDirectory = jobDirectory(jobId);
			Path workDirectory = jobDirectory.resolve("work");
			Path output = splitClipOutputPath(jobId, clipIndex);
			renderEditedToOutput(source, output, workDirectory, safeOptions, musicFile, textOverlayFile, textLayerOverlayFiles);
			updateSplitClipManifest(jobId, clipIndex, editTitle(safeOptions, sourceTitle), output, safeOptions);
			LOGGER.info("[{}] Đã ghi đè clip {} sau chỉnh sửa: {}", jobId, clipIndex, output);
			return new VideoEditResult(jobId,
					"/api/split-highlights/" + jobId + "/clips/" + clipIndex + "/download",
					"/api/split-highlights/" + jobId + "/clips/" + clipIndex + "/preview",
					"Đã lưu đè clip đã chỉnh sửa.");
		}
		return createEditedSplitClipRecord(source, safeOptions, musicFile, textOverlayFile, textLayerOverlayFiles, sourceTitle);
	}

	private void processFacebookBatchDownload(HighlightJobStatus status, List<String> reelsUrls, Path uploadDirectory, int startIndex, int endIndex, String cookiesFilePath) {
		Path jobDirectory = jobDirectory(status.getJobId());
		try {
			Files.createDirectories(uploadDirectory);
			progress(status, 8, "Đang tải Facebook reels " + startIndex + "-" + endIndex + " với chất lượng tốt nhất tối đa 2K.");
			List<Path> downloadedVideos = downloadFacebookBatchSources(reelsUrls, uploadDirectory, startIndex, endIndex, cookiesFilePath);
			List<String> names = downloadedVideos.stream()
					.map(path -> path.getFileName().toString())
					.collect(Collectors.toList());
			double totalDuration = 0;
			for (Path video : downloadedVideos) {
				try {
					totalDuration += Math.max(0, ffmpegService.probeDuration(video));
				}
				catch (RuntimeException ex) {
					LOGGER.warn("[{}] Không đọc được thời lượng video đã tải, vẫn giữ file: {}", status.getJobId(), video);
				}
			}
			status.setInputFileNames(names);
			status.ready(totalDuration, totalDuration, downloadedVideos.size(), null, List.of(), List.of());
			HighlightHistoryItem item = toHistory(status, "ready", totalDuration, downloadedVideos.size(), null, null);
			item.setCategory(CATEGORY_FACEBOOK_BATCH);
			item.setCutNote("Nguồn: " + reelsUrls.get(0) + (reelsUrls.size() > 1 ? " và " + (reelsUrls.size() - 1) + " link khác" : "") + " | khoảng " + startIndex + "-" + endIndex);
			saveManifest(jobDirectory, item);
			progress(status, 100, "Đã tải xong " + downloadedVideos.size() + " video Facebook.");
			enforceHistoryLimit(status.getJobId());
		}
		catch (IOException | RuntimeException ex) {
			LOGGER.error("[{}] Không thể tải hàng loạt Facebook.", status.getJobId(), ex);
			status.failed(ex.getMessage() == null ? "Không thể tải hàng loạt Facebook." : ex.getMessage());
			writeError(jobDirectory, ex instanceof Exception ? (Exception) ex : new RuntimeException(ex));
			HighlightHistoryItem item = toHistory(status, "error", 0, 0, null, status.getError());
			item.setCategory(CATEGORY_FACEBOOK_BATCH);
			saveManifest(jobDirectory, item);
			enforceHistoryLimit(status.getJobId());
		}
	}

	private List<Path> downloadFacebookBatchSources(List<String> reelsUrls, Path uploadDirectory, int startIndex, int endIndex, String cookiesFilePath) {
		if (reelsUrls.size() == 1) {
			return networkVideoDownloadService.downloadPlaylistBestUpTo2k(reelsUrls.get(0), uploadDirectory, startIndex, endIndex, cookiesFilePath);
		}
		int from = Math.max(0, startIndex - 1);
		int to = Math.min(reelsUrls.size(), endIndex);
		if (from >= reelsUrls.size() || from >= to) {
			throw new IllegalStateException("Danh sách bạn dán chỉ có " + reelsUrls.size() + " link, không đủ tới khoảng " + startIndex + "-" + endIndex + ".");
		}
		List<String> selectedUrls = reelsUrls.subList(from, to);
		List<Path> downloaded = new ArrayList<>();
		for (int i = 0; i < selectedUrls.size(); i++) {
			downloaded.add(networkVideoDownloadService.downloadBestUpTo2k(selectedUrls.get(i), uploadDirectory, i + 1, cookiesFilePath));
		}
		return downloaded;
	}

	private void processNetworkHighlight(HighlightJobStatus status, List<String> videoUrls, Path uploadsDirectory, Path workDirectory,
			Path outputDirectory, int clipCount, double clipSeconds, String cutNote, String cookiesFilePath, RenderProfile renderProfile) {
		Path jobDirectory = jobDirectory(status.getJobId());
		try {
			Files.createDirectories(uploadsDirectory);
			Files.createDirectories(workDirectory);
			Files.createDirectories(outputDirectory);
			List<Path> downloadedVideos = downloadNetworkVideos(status, videoUrls, uploadsDirectory, cookiesFilePath);
			progress(status, 12, "Tải video từ mạng xong. Bắt đầu phân tích và cắt ghép.");
			processHighlight(status, downloadedVideos, workDirectory, outputDirectory, clipCount, clipSeconds, cutNote, renderProfile);
		}
		catch (IOException | RuntimeException ex) {
			LOGGER.error("[{}] Không thể tải/xử lý video từ mạng.", status.getJobId(), ex);
			status.failed(ex.getMessage() == null ? "Không thể tải video từ mạng." : ex.getMessage());
			writeError(jobDirectory, ex instanceof Exception ? (Exception) ex : new RuntimeException(ex));
			saveManifest(jobDirectory, toHistory(status, "error", 0, 0, null, status.getError()));
			enforceHistoryLimit(status.getJobId());
		}
		finally {
			cleanupWorkDirectory(status.getJobId(), workDirectory);
		}
	}

	private void processNetworkSplitClips(HighlightJobStatus status, List<String> videoUrls, Path uploadsDirectory,
			Path outputDirectory, int clipCount, double clipSeconds, String cutNote, String cookiesFilePath, RenderProfile renderProfile) {
		Path jobDirectory = jobDirectory(status.getJobId());
		try {
			Files.createDirectories(uploadsDirectory);
			Files.createDirectories(outputDirectory);
			List<Path> downloadedVideos = downloadNetworkVideos(status, videoUrls, uploadsDirectory, cookiesFilePath);
			progress(status, 12, "Tải video từ mạng xong. Bắt đầu phân tích và tách clip.");
			processSplitClips(status, downloadedVideos, outputDirectory, clipCount, clipSeconds, cutNote, renderProfile);
		}
		catch (IOException | RuntimeException ex) {
			LOGGER.error("[{}] Không thể tải/tách video từ mạng.", status.getJobId(), ex);
			status.failed(ex.getMessage() == null ? "Không thể tải video từ mạng." : ex.getMessage());
			writeError(jobDirectory, ex instanceof Exception ? (Exception) ex : new RuntimeException(ex));
			saveSplitManifest(jobDirectory, List.of(toSplitHistory(status, 1, "error", originalNameFor(status, 0), null, 0, 0, null, status.getError())));
			enforceHistoryLimit(status.getJobId());
		}
	}

	private List<Path> downloadNetworkVideos(HighlightJobStatus status, List<String> videoUrls, Path uploadsDirectory, String cookiesFilePath) {
		List<Path> downloaded = new ArrayList<>();
		for (int i = 0; i < videoUrls.size(); i++) {
			int progressValue = 4 + (int) Math.round((i / (double) Math.max(1, videoUrls.size())) * 8);
			progress(status, progressValue, "Đang tải video từ mạng " + (i + 1) + "/" + videoUrls.size() + " với chất lượng tốt nhất tối đa 2K.");
			Path video = networkVideoDownloadService.downloadBestUpTo2k(videoUrls.get(i), uploadsDirectory, i + 1, cookiesFilePath);
			downloaded.add(video);
		}
		if (downloaded.isEmpty()) {
			throw new IllegalStateException("Không tải được video nào từ link đã nhập.");
		}
		return downloaded;
	}

	private void processHighlight(HighlightJobStatus status, List<Path> uploadedVideos, Path workDirectory, Path outputDirectory, int clipCount, double clipSeconds, String cutNote, RenderProfile renderProfile) {
		Path jobDirectory = jobDirectory(status.getJobId());
		try {
			CutPreference preference = CutPreference.from(cutNote);
			progress(status, 12, "Đang đọc thông tin video gốc.");
			List<VideoSource> sources = new ArrayList<>();
			for (int i = 0; i < uploadedVideos.size(); i++) {
				Path video = uploadedVideos.get(i);
				double duration = ffmpegService.probeDuration(video);
				sources.add(new VideoSource(i, video, duration));
				LOGGER.info("[{}] Video {} dài {} giây.", status.getJobId(), video.getFileName(), String.format(Locale.ROOT, "%.2f", duration));
			}
			double totalDuration = sources.stream().mapToDouble(VideoSource::duration).sum();

			progress(status, 20, "Đang phân tích video theo ghi chú: " + preference.summary() + " Tỉ lệ xuất: " + renderProfile.label() + ".");
			List<SelectedSegment> selectedSegments = selectHighlightSegments(sources, clipCount, clipSeconds, preference);
			progress(status, 35, "Đã chọn " + selectedSegments.size() + " đoạn cách xa nhau. Bắt đầu cắt video.");
			LOGGER.info("[{}] Các đoạn được chọn: {}", status.getJobId(), selectedSegments);

			List<Path> segments = renderSegments(workDirectory, selectedSegments, status, renderProfile);
			Path concatList = workDirectory.resolve("concat.txt");
			writeConcatList(concatList, segments);
			progress(status, 92, "Đang ghép các đoạn hay thành video hoàn chỉnh.");

			Path output = outputDirectory.resolve("highlight.mp4");
			ffmpegService.concatSegmentsFast(concatList, output);

			List<Double> selectedStarts = selectedSegments.stream().map(SelectedSegment::start).collect(Collectors.toList());
			List<String> warnings = new ArrayList<>();
			warnings.add("File gốc và highlight được giữ lại trên server để hiển thị trong lịch sử. File tạm đã được dọn sau khi render.");
			if (!cutNote.isBlank()) {
				warnings.add("Đã áp dụng ghi chú khi chọn đoạn: " + cutNote);
			}
			if (selectedSegments.size() < clipCount) {
				warnings.add("Video ngắn hoặc không đủ khoảng cách an toàn, nên số đoạn được chọn ít hơn yêu cầu để tránh cắt sát nhau.");
			}
			status.ready(sources.isEmpty() ? 0 : sources.get(0).duration(), totalDuration, selectedSegments.size(), "/api/highlights/" + status.getJobId() + "/download", selectedStarts, warnings);
			HighlightHistoryItem historyItem = toHistory(status, "ready", totalDuration, selectedSegments.size(), status.getDownloadUrl(), null);
			saveManifest(jobDirectory, historyItem);
			enforceHistoryLimit(status.getJobId());
			LOGGER.info("[{}] Render hoàn tất. File xuất: {}", status.getJobId(), output);
		}
		catch (RuntimeException ex) {
			LOGGER.error("[{}] Xử lý highlight thất bại.", status.getJobId(), ex);
			status.failed(ex.getMessage() == null ? "Không thể xử lý video." : ex.getMessage());
			writeError(jobDirectory, ex);
			saveManifest(jobDirectory, toHistory(status, "error", 0, 0, null, status.getError()));
			enforceHistoryLimit(status.getJobId());
		}
		finally {
			cleanupWorkDirectory(status.getJobId(), workDirectory);
		}
	}

	private void processSplitClips(HighlightJobStatus status, List<Path> uploadedVideos, Path outputDirectory, int clipCount, double clipSeconds, String cutNote, RenderProfile renderProfile) {
		Path jobDirectory = jobDirectory(status.getJobId());
		try {
			CutPreference preference = CutPreference.from(cutNote);
			progress(status, 12, "Đang đọc thông tin video gốc.");
			List<VideoSource> sources = new ArrayList<>();
			for (int i = 0; i < uploadedVideos.size(); i++) {
				Path video = uploadedVideos.get(i);
				double duration = ffmpegService.probeDuration(video);
				sources.add(new VideoSource(i, video, duration));
				LOGGER.info("[{}] Video {} dài {} giây.", status.getJobId(), video.getFileName(), String.format(Locale.ROOT, "%.2f", duration));
			}
			double totalDuration = sources.stream().mapToDouble(VideoSource::duration).sum();

			progress(status, 20, "Đang phân tích để tách clip rời theo ghi chú: " + preference.summary() + " Tỉ lệ xuất: " + renderProfile.label() + ".");
			List<SelectedSegment> selectedSegments = selectHighlightSegments(sources, clipCount, clipSeconds, preference);
			progress(status, 35, "Đã chọn " + selectedSegments.size() + " đoạn. Bắt đầu xuất từng clip riêng.");
			LOGGER.info("[{}] Các đoạn tách riêng được chọn: {}", status.getJobId(), selectedSegments);

			List<SplitClipHistoryItem> historyItems = renderSplitSegments(outputDirectory, selectedSegments, status, renderProfile);
			List<Double> selectedStarts = selectedSegments.stream().map(SelectedSegment::start).collect(Collectors.toList());
			List<String> warnings = new ArrayList<>();
			warnings.add("File gốc và các clip đã tách được giữ lại trên server để quản lý lịch sử.");
			if (!cutNote.isBlank()) {
				warnings.add("Đã áp dụng ghi chú khi chọn đoạn: " + cutNote);
			}
			if (selectedSegments.size() < clipCount) {
				warnings.add("Video ngắn hoặc không đủ khoảng cách an toàn, nên số đoạn được chọn ít hơn yêu cầu.");
			}
			status.ready(sources.isEmpty() ? 0 : sources.get(0).duration(), totalDuration, selectedSegments.size(), null, selectedStarts, warnings);
			status.progress(100, "Hoàn tất. Các clip tách riêng đã sẵn sàng tải xuống.");
			saveSplitManifest(jobDirectory, historyItems);
			enforceHistoryLimit(status.getJobId());
			LOGGER.info("[{}] Tách clip hoàn tất. Số clip xuất: {}", status.getJobId(), historyItems.size());
		}
		catch (RuntimeException ex) {
			LOGGER.error("[{}] Tách clip thất bại.", status.getJobId(), ex);
			status.failed(ex.getMessage() == null ? "Không thể tách clip từ video." : ex.getMessage());
			writeError(jobDirectory, ex);
			saveSplitManifest(jobDirectory, List.of(toSplitHistory(status, 1, "error", originalNameFor(status, 0), null, 0, 0, null, status.getError())));
			enforceHistoryLimit(status.getJobId());
		}
	}

	private List<SplitClipHistoryItem> renderSplitSegments(Path outputDirectory, List<SelectedSegment> selectedSegments, HighlightJobStatus status, RenderProfile renderProfile) {
		List<SplitClipHistoryItem> items = new ArrayList<>();
		String jobCreatedAt = Instant.now().toString();
		for (int i = 0; i < selectedSegments.size(); i++) {
			SelectedSegment selected = selectedSegments.get(i);
			int clipIndex = i + 1;
			int progress = 35 + (int) Math.round((clipIndex / (double) Math.max(1, selectedSegments.size())) * 58);
			progress(status, progress, "Đang xuất clip " + clipIndex + "/" + selectedSegments.size() + ".");
			String outputFileName = String.format(Locale.ROOT, "clip-%02d.mp4", clipIndex);
			Path output = outputDirectory.resolve(outputFileName);
			LOGGER.info("[{}] Xuất clip {}/{} từ {}: start={}s, duration={}s, score={}",
					status.getJobId(), clipIndex, selectedSegments.size(), selected.source().path().getFileName(), selected.start(), selected.duration(),
					String.format(Locale.ROOT, "%.1f", selected.score()));
			ffmpegService.createSegmentWithAudio(selected.source().path(), output, selected.start(), selected.duration(), renderProfile.width(), renderProfile.height());
			items.add(toSplitHistory(
					status,
					clipIndex,
					"ready",
					originalNameFor(status, selected.source().index()),
					outputFileName,
					selected.start(),
					selected.duration(),
					"/api/split-highlights/" + status.getJobId() + "/clips/" + clipIndex + "/download",
					null));
			items.get(items.size() - 1).setCreatedAt(jobCreatedAt);
			items.get(items.size() - 1).setUpdatedAt(jobCreatedAt);
		}
		return items;
	}

	private List<Path> renderSegments(Path workDirectory, List<SelectedSegment> selectedSegments, HighlightJobStatus status, RenderProfile renderProfile) {
		List<Path> segments = new ArrayList<>();
		for (int i = 0; i < selectedSegments.size(); i++) {
			SelectedSegment selected = selectedSegments.get(i);
			int progress = 35 + (int) Math.round(((i + 1) / (double) Math.max(1, selectedSegments.size())) * 50);
			progress(status, progress, "Đang cắt đoạn " + (i + 1) + "/" + selectedSegments.size() + ".");
			Path segment = workDirectory.resolve(String.format(Locale.ROOT, "segment-%02d.mp4", i + 1));
			LOGGER.info("[{}] Cắt đoạn {}/{} từ {}: start={}s, duration={}s, score={}",
					status.getJobId(), i + 1, selectedSegments.size(), selected.source().path().getFileName(), selected.start(), selected.duration(),
					String.format(Locale.ROOT, "%.1f", selected.score()));
			ffmpegService.createSegmentWithAudio(selected.source().path(), segment, selected.start(), selected.duration(), renderProfile.width(), renderProfile.height());
			segments.add(segment);
		}
		return segments;
	}

	private List<SelectedSegment> selectHighlightSegments(List<VideoSource> sources, int clipCount, double clipSeconds, CutPreference preference) {
		List<List<SelectedSegment>> candidatesBySource = new ArrayList<>();
		List<SelectedSegment> candidates = new ArrayList<>();
		List<SelectedSegment> selected = requestedSegments(sources, preference, clipSeconds);
		int targetCount = Math.max(clipCount, selected.size());
		if (!selected.isEmpty()) {
			LOGGER.info("Đã nhận {} khoảng thời gian cố định từ ghi chú.", selected.size());
		}
		if (selected.size() >= targetCount) {
			selected.sort(Comparator
					.comparingInt((SelectedSegment segment) -> segment.source().index())
					.thenComparingDouble(SelectedSegment::start));
			return selected;
		}

		for (VideoSource source : sources) {
			List<Double> sceneTimes = ffmpegService.detectSceneTimes(source.path(), SCENE_THRESHOLD);
			LOGGER.info("Video {} phát hiện {} mốc chuyển cảnh/chuyển động tiềm năng.", source.path().getFileName(), sceneTimes.size());
			List<SelectedSegment> sourceCandidates = scoreCandidates(source, clipSeconds, sceneTimes, preference);
			sourceCandidates.sort(Comparator.comparingDouble(SelectedSegment::score).reversed());
			candidatesBySource.add(sourceCandidates);
			candidates.addAll(sourceCandidates);
		}
		candidates.sort(Comparator.comparingDouble(SelectedSegment::score).reversed());
		for (int i = 0; i < Math.min(10, candidates.size()); i++) {
			SelectedSegment candidate = candidates.get(i);
			LOGGER.info("  #{} file={} start={}s score={} audio={} sceneCount={} motionScore={}",
					i + 1,
					candidate.source().path().getFileName(),
					candidate.start(),
					String.format(Locale.ROOT, "%.1f", candidate.score()),
					String.format(Locale.ROOT, "%.1fdB", candidate.meanVolume()),
					candidate.sceneCount(),
					String.format(Locale.ROOT, "%.1f", candidate.motionScore()));
		}

		List<Integer> sourceQuotas = calculateSourceQuotas(sources, targetCount - selected.size());
		for (int i = 0; i < candidatesBySource.size(); i++) {
			if (selected.size() >= targetCount) {
				break;
			}
			List<SelectedSegment> picked = pickSpacedSegments(
					candidatesBySource.get(i),
					sourceQuotas.get(i),
					clipSeconds,
					preference,
					selected);
			selected.addAll(picked);
		}

		for (SelectedSegment candidate : candidates) {
			if (selected.contains(candidate)) {
				continue;
			}
			double minimumGap = gapForSource(candidate.source(), Math.max(clipCount, 1), clipSeconds, preference);
			if (farEnoughFromExisting(selected, candidate, minimumGap)) {
				selected.add(candidate);
			}
			if (selected.size() >= targetCount) {
				break;
			}
		}
		if (selected.size() < targetCount) {
			LOGGER.info("Chưa đủ {} đoạn sau lượt chọn an toàn. Thử chọn thêm với khoảng cách mềm hơn.", targetCount);
			for (SelectedSegment candidate : candidates) {
				if (selected.contains(candidate)) {
					continue;
				}
				double relaxedGap = relaxedGapForSource(candidate.source(), Math.max(clipCount, 1), clipSeconds);
				if (farEnoughFromExisting(selected, candidate, relaxedGap)) {
					selected.add(candidate);
				}
				if (selected.size() >= targetCount) {
					break;
				}
			}
		}
		selected.sort(Comparator
				.comparingInt((SelectedSegment segment) -> segment.source().index())
				.thenComparingDouble(SelectedSegment::start));
		return selected;
	}

	private List<SelectedSegment> requestedSegments(List<VideoSource> sources, CutPreference preference, double fallbackClipSeconds) {
		List<SelectedSegment> segments = new ArrayList<>();
		for (RequestedRange range : preference.requestedRanges()) {
			if (sources.isEmpty()) {
				break;
			}
			int sourceIndex = range.sourceIndex() == null ? 0 : range.sourceIndex();
			if (sourceIndex < 0 || sourceIndex >= sources.size()) {
				LOGGER.warn("Bỏ qua khoảng thời gian {} vì video số {} không tồn tại.", range, sourceIndex + 1);
				continue;
			}
			VideoSource source = sources.get(sourceIndex);
			double start = clamp(range.start(), 0, Math.max(0, source.duration() - 0.4));
			double end = range.end() == null ? start + fallbackClipSeconds : range.end();
			end = clamp(end, start + 0.4, source.duration());
			if (end <= start) {
				LOGGER.warn("Bỏ qua khoảng thời gian không hợp lệ: {}", range);
				continue;
			}
			segments.add(new SelectedSegment(source, round(start), round(end - start), 9_999.0, 0, 0, 0));
		}
		segments.sort(Comparator
				.comparingInt((SelectedSegment segment) -> segment.source().index())
				.thenComparingDouble(SelectedSegment::start));
		return segments;
	}

	private List<SelectedSegment> scoreCandidates(VideoSource source, double clipSeconds, List<Double> sceneTimes, CutPreference preference) {
		Set<Double> starts = new LinkedHashSet<>();
		double maxStart = Math.max(0, source.duration() - clipSeconds);
		double gridStep = Math.max(1.5, Math.min(6.0, clipSeconds / 3.0));
		double analysisStart = preference.preferStart ? 0 : Math.min(maxStart, Math.max(0, source.duration() * 0.04));
		double analysisEnd = preference.preferEnd ? maxStart : Math.max(analysisStart, maxStart - source.duration() * 0.04);
		for (double start = analysisStart; start <= analysisEnd; start += gridStep) {
			starts.add(round(start));
		}
		if (preference.preferStart) {
			starts.add(0.0);
		}
		if (preference.preferEnd) {
			starts.add(round(maxStart));
		}
		for (Double sceneTime : sceneTimes) {
			starts.add(round(clamp(sceneTime - clipSeconds * 0.50, 0, maxStart)));
			starts.add(round(clamp(sceneTime - clipSeconds * 0.25, 0, maxStart)));
			starts.add(round(clamp(sceneTime - 1.0, 0, maxStart)));
		}

		List<Double> dedupedStarts = new ArrayList<>();
		for (Double start : starts) {
			if (farEnoughFromExisting(dedupedStarts, start, Math.max(0.75, Math.min(4.0, clipSeconds * 0.20)))) {
				dedupedStarts.add(start);
			}
		}
		if (dedupedStarts.size() > MAX_CANDIDATES_TO_SCORE) {
			dedupedStarts = evenlySample(dedupedStarts, MAX_CANDIDATES_TO_SCORE);
		}

		List<CandidateSeed> seeds = new ArrayList<>();
		for (Double start : dedupedStarts) {
			int sceneCount = countScenesInRange(sceneTimes, start, start + clipSeconds);
			double motionScore = motionScore(sceneCount);
			double positionScore = positionScore(start, source.duration());
			double estimatedAudioScore = preference.preferTalking ? 28.0 : 18.0;
			double preliminaryScore = preference.score(start, source.duration(), sceneCount, motionScore, estimatedAudioScore, positionScore);
			seeds.add(new CandidateSeed(start, sceneCount, motionScore, positionScore, preliminaryScore));
		}
		seeds.sort(Comparator.comparingDouble(CandidateSeed::preliminaryScore).reversed());

		List<SelectedSegment> candidates = new ArrayList<>();
		for (CandidateSeed seed : candidatesToProbe(seeds)) {
			double meanVolume = ffmpegService.measureMeanVolume(source.path(), seed.start(), clipSeconds);
			double audioScore = audioScore(meanVolume);
			double total = preference.score(seed.start(), source.duration(), seed.sceneCount(), seed.motionScore(), audioScore, seed.positionScore());
			candidates.add(new SelectedSegment(source, seed.start(), clipSeconds, total, meanVolume, seed.sceneCount(), seed.motionScore()));
		}
		return candidates;
	}

	private List<CandidateSeed> candidatesToProbe(List<CandidateSeed> seeds) {
		if (seeds.size() <= MAX_AUDIO_PROBES_PER_SOURCE) {
			return seeds;
		}
		List<CandidateSeed> selected = new ArrayList<>();
		Set<Double> selectedStarts = new LinkedHashSet<>();
		int topCount = Math.min(MAX_AUDIO_PROBES_PER_SOURCE * 3 / 4, seeds.size());
		for (int i = 0; i < topCount; i++) {
			addSeedIfAbsent(selected, selectedStarts, seeds.get(i));
		}
		List<CandidateSeed> seedsByTime = new ArrayList<>(seeds);
		seedsByTime.sort(Comparator.comparingDouble(CandidateSeed::start));
		List<CandidateSeed> timelineSample = evenlySample(seedsByTime, MAX_AUDIO_PROBES_PER_SOURCE - selected.size());
		for (CandidateSeed seed : timelineSample) {
			addSeedIfAbsent(selected, selectedStarts, seed);
			if (selected.size() >= MAX_AUDIO_PROBES_PER_SOURCE) {
				break;
			}
		}
		selected.sort(Comparator.comparingDouble(CandidateSeed::preliminaryScore).reversed());
		return selected;
	}

	private void addSeedIfAbsent(List<CandidateSeed> selected, Set<Double> selectedStarts, CandidateSeed seed) {
		if (selectedStarts.add(seed.start())) {
			selected.add(seed);
		}
	}

	private List<Integer> calculateSourceQuotas(List<VideoSource> sources, int clipCount) {
		List<Integer> quotas = new ArrayList<>();
		for (int i = 0; i < sources.size(); i++) {
			quotas.add(0);
		}
		if (sources.isEmpty() || clipCount <= 0) {
			return quotas;
		}
		if (sources.size() == 1) {
			quotas.set(0, clipCount);
			return quotas;
		}
		if (clipCount <= sources.size()) {
			List<VideoSource> sortedSources = new ArrayList<>(sources);
			sortedSources.sort(Comparator.comparingDouble(VideoSource::duration).reversed());
			for (int i = 0; i < clipCount; i++) {
				quotas.set(sortedSources.get(i).index(), 1);
			}
			return quotas;
		}

		for (int i = 0; i < sources.size(); i++) {
			quotas.set(i, 1);
		}
		int remaining = clipCount - sources.size();
		while (remaining > 0) {
			int bestIndex = 0;
			double bestNeed = -1;
			for (int i = 0; i < sources.size(); i++) {
				double need = sources.get(i).duration() / (quotas.get(i) + 1.0);
				if (need > bestNeed) {
					bestNeed = need;
					bestIndex = i;
				}
			}
			quotas.set(bestIndex, quotas.get(bestIndex) + 1);
			remaining--;
		}
		return quotas;
	}

	private List<SelectedSegment> pickSpacedSegments(List<SelectedSegment> sourceCandidates, int desiredCount,
			double clipSeconds, CutPreference preference, List<SelectedSegment> alreadySelected) {
		List<SelectedSegment> picked = new ArrayList<>();
		if (sourceCandidates.isEmpty() || desiredCount <= 0) {
			return picked;
		}
		VideoSource source = sourceCandidates.get(0).source();
		double maxStart = Math.max(0, source.duration() - clipSeconds);
		double windowStart = preference.windowStart(source.duration(), clipSeconds);
		double windowEnd = preference.windowEnd(source.duration(), clipSeconds);
		if (desiredCount * clipSeconds >= source.duration() * 0.45) {
			windowStart = 0;
			windowEnd = maxStart;
		}
		if (windowEnd < windowStart) {
			windowStart = 0;
			windowEnd = maxStart;
		}
		double span = Math.max(0.001, windowEnd - windowStart);
		double minimumGap = gapForSource(source, desiredCount, clipSeconds, preference);

		for (int slot = 0; slot < desiredCount; slot++) {
			double slotStart = windowStart + (span * slot / desiredCount);
			double slotEnd = slot == desiredCount - 1 ? windowEnd + 0.001 : windowStart + (span * (slot + 1) / desiredCount);
			SelectedSegment best = bestCandidateInWindow(sourceCandidates, slotStart, slotEnd, alreadySelected, picked, minimumGap);
			if (best != null) {
				picked.add(best);
			}
		}

		for (SelectedSegment candidate : sourceCandidates) {
			if (picked.size() >= desiredCount) {
				break;
			}
			if (alreadySelected.contains(candidate) || picked.contains(candidate)) {
				continue;
			}
			if (farEnoughFromExisting(alreadySelected, candidate, minimumGap)
					&& farEnoughFromExisting(picked, candidate, minimumGap)) {
				picked.add(candidate);
			}
		}
		return picked;
	}

	private SelectedSegment bestCandidateInWindow(List<SelectedSegment> sourceCandidates, double windowStart, double windowEnd,
			List<SelectedSegment> alreadySelected, List<SelectedSegment> picked, double minimumGap) {
		for (SelectedSegment candidate : sourceCandidates) {
			if (candidate.start() < windowStart || candidate.start() > windowEnd) {
				continue;
			}
			if (alreadySelected.contains(candidate) || picked.contains(candidate)) {
				continue;
			}
			if (farEnoughFromExisting(alreadySelected, candidate, minimumGap)
					&& farEnoughFromExisting(picked, candidate, minimumGap)) {
				return candidate;
			}
		}
		return null;
	}

	private double gapForSource(VideoSource source, int desiredCount, double clipSeconds, CutPreference preference) {
		if (desiredCount <= 1) {
			return clipSeconds * 0.25;
		}
		double freeGap = (source.duration() - (desiredCount * clipSeconds)) / Math.max(1, desiredCount - 1);
		if (freeGap <= 0) {
			return 0;
		}
		double preferredGap = clipSeconds * (preference.spreadWide ? 1.15 : 0.65);
		return Math.min(preferredGap, freeGap * 0.85);
	}

	private double relaxedGapForSource(VideoSource source, int desiredCount, double clipSeconds) {
		if (desiredCount <= 1) {
			return 0;
		}
		double freeGap = (source.duration() - (desiredCount * clipSeconds)) / Math.max(1, desiredCount - 1);
		if (freeGap <= 0) {
			return 0;
		}
		return Math.min(clipSeconds * 0.12, freeGap * 0.35);
	}

	private <T> List<T> evenlySample(List<T> values, int limit) {
		List<T> sampled = new ArrayList<>();
		if (limit <= 0) {
			return sampled;
		}
		if (values.size() <= limit) {
			return values;
		}
		for (int i = 0; i < limit; i++) {
			int index = (int) Math.round(i * (values.size() - 1) / (double) (limit - 1));
			sampled.add(values.get(index));
		}
		return sampled;
	}

	private int countScenesInRange(List<Double> sceneTimes, double start, double end) {
		int count = 0;
		for (Double sceneTime : sceneTimes) {
			if (sceneTime >= start && sceneTime <= end) {
				count++;
			}
		}
		return count;
	}

	private double motionScore(int sceneCount) {
		if (sceneCount == 0) {
			return 8.0;
		}
		double goodMotion = Math.min(sceneCount, 3) * 14.0;
		double tooChaoticPenalty = Math.max(0, sceneCount - 4) * 10.0;
		return Math.max(6.0, goodMotion - tooChaoticPenalty);
	}

	private double audioScore(double meanVolume) {
		double normalized = (meanVolume + 55.0) / 38.0;
		return clamp(normalized, 0, 1) * 38.0;
	}

	private double positionScore(double start, double duration) {
		double ratio = duration <= 0 ? 0 : start / duration;
		if (ratio < 0.06 || ratio > 0.94) {
			return 0;
		}
		if (ratio > 0.18 && ratio < 0.82) {
			return 10;
		}
		return 6;
	}

	private boolean farEnoughFromExisting(List<Double> starts, double candidate, double gap) {
		for (Double start : starts) {
			if (Math.abs(start - candidate) < gap) {
				return false;
			}
		}
		return true;
	}

	private boolean farEnoughFromExisting(List<SelectedSegment> selected, SelectedSegment candidate, double gap) {
		for (SelectedSegment segment : selected) {
			if (segment.source().path().equals(candidate.source().path())
					&& candidate.start() < segment.end() + gap
					&& segment.start() < candidate.end() + gap) {
				return false;
			}
		}
		return true;
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
			throw new IllegalStateException("Không thể tạo danh sách ghép video.", ex);
		}
	}

	private void enforceHistoryLimit(String protectedJobId) {
		List<HistoryJobRef> items = readHistoryJobRefs();
		if (items.size() < HISTORY_DELETE_THRESHOLD) {
			return;
		}
		items.sort(Comparator.comparing(HistoryJobRef::createdAt, Comparator.nullsLast(String::compareTo)));
		int deletedCount = 0;
		for (HistoryJobRef item : items) {
			if (deletedCount >= HISTORY_DELETE_OLDEST_COUNT) {
				break;
			}
			String jobId = item.jobId();
			if (jobId == null || jobId.equals(protectedJobId)) {
				continue;
			}
			if (deleteJobDirectory(jobId, "tự động dọn lịch sử khi đạt " + HISTORY_DELETE_THRESHOLD + " job")) {
				deletedCount++;
			}
		}
		if (deletedCount > 0) {
			LOGGER.info("Đã tự động xóa {} job cũ nhất khỏi ổ cứng.", deletedCount);
		}
	}

	private boolean deleteJobDirectory(String jobId, String reason) {
		Path jobsRoot = workspace.jobsDirectory().toAbsolutePath().normalize();
		Path directory = jobDirectory(jobId).toAbsolutePath().normalize();
		if (!directory.startsWith(jobsRoot)) {
			throw new IllegalArgumentException("Job id không hợp lệ.");
		}
		statuses.remove(jobId);
		if (!Files.exists(directory)) {
			LOGGER.info("[{}] Bỏ qua xóa vì thư mục job không còn tồn tại: {}", jobId, directory);
			return false;
		}
		try {
			boolean deleted = FileSystemUtils.deleteRecursively(directory.toFile());
			if (deleted) {
				LOGGER.info("[{}] Đã xóa sạch job khỏi ổ cứng ({}) tại {}", jobId, reason, directory);
			}
			else {
				LOGGER.warn("[{}] Không xóa được toàn bộ thư mục job tại {}", jobId, directory);
			}
			return deleted;
		}
		catch (RuntimeException ex) {
			LOGGER.warn("[{}] Lỗi khi xóa thư mục job tại {}", jobId, directory, ex);
			return false;
		}
	}

	private void cleanupWorkDirectory(String jobId, Path workDirectory) {
		if (workDirectory == null || !Files.exists(workDirectory)) {
			return;
		}
		try {
			boolean deleted = FileSystemUtils.deleteRecursively(workDirectory.toFile());
			if (deleted) {
				LOGGER.info("[{}] Đã dọn thư mục tạm sau render: {}", jobId, workDirectory);
			}
		}
		catch (RuntimeException ex) {
			LOGGER.warn("[{}] Không dọn được thư mục tạm sau render: {}", jobId, workDirectory, ex);
		}
	}

	private boolean deleteSplitClip(String jobId, int clipIndex) {
		Path directory = jobDirectory(jobId);
		Path manifest = directory.resolve("split-manifest.json");
		if (!Files.isRegularFile(manifest)) {
			LOGGER.info("[{}] Bỏ qua xóa clip {} vì không tìm thấy split manifest.", jobId, clipIndex);
			return false;
		}
		List<SplitClipHistoryItem> items = readSplitManifest(manifest);
		SplitClipHistoryItem target = items.stream()
				.filter(item -> item.getClipIndex() == clipIndex)
				.findFirst()
				.orElse(null);
		if (target == null) {
			LOGGER.info("[{}] Bỏ qua xóa clip {} vì không còn trong lịch sử.", jobId, clipIndex);
			return false;
		}

		String outputFileName = target.getOutputFileName() == null || target.getOutputFileName().isBlank()
				? String.format(Locale.ROOT, "clip-%02d.mp4", clipIndex)
				: target.getOutputFileName();
		Path outputRoot = directory.resolve("output").toAbsolutePath().normalize();
		Path output = outputRoot.resolve(outputFileName).toAbsolutePath().normalize();
		if (!output.startsWith(outputRoot)) {
			throw new IllegalArgumentException("Tên clip không hợp lệ.");
		}

		try {
			Files.deleteIfExists(output);
			List<SplitClipHistoryItem> remaining = items.stream()
					.filter(item -> item.getClipIndex() != clipIndex)
					.collect(Collectors.toList());
			if (remaining.isEmpty()) {
				deleteJobDirectory(jobId, "người dùng xóa clip cuối cùng trong job tách");
			}
			else {
				saveSplitManifest(directory, remaining);
			}
			LOGGER.info("[{}] Đã xóa clip {} khỏi ổ cứng và cập nhật lịch sử.", jobId, clipIndex);
			return true;
		}
		catch (IOException | RuntimeException ex) {
			LOGGER.warn("[{}] Không xóa được clip {} tại {}", jobId, clipIndex, output, ex);
			return false;
		}
	}

	private VideoEditResult createEditedHighlightRecord(Path source, VideoEditOptions options, MultipartFile musicFile, MultipartFile textOverlayFile, List<MultipartFile> textLayerOverlayFiles, String sourceTitle) {
		String sourceCategory = categoryForSource(source);
		String sourceDraftJobId = manualEditDraftJobIdForSource(source);
		String jobId = newJobId();
		Path jobDirectory = workspace.jobsDirectory().resolve(jobId).normalize();
		Path uploadDirectory = jobDirectory.resolve("upload");
		Path workDirectory = jobDirectory.resolve("work");
		Path outputDirectory = jobDirectory.resolve("output");
		Path output = outputDirectory.resolve("highlight.mp4");
		String title = editTitle(options, sourceTitle);
		try {
			Files.createDirectories(uploadDirectory);
			Files.createDirectories(workDirectory);
			Files.createDirectories(outputDirectory);
			Path sourceCopy = uploadDirectory.resolve("01-" + safeFileName(title + sourceExtension(source))).normalize();
			Files.copy(source, sourceCopy, StandardCopyOption.REPLACE_EXISTING);
			renderEditedToOutput(source, output, workDirectory, options, musicFile, textOverlayFile, textLayerOverlayFiles);
			double duration = ffmpegService.probeDuration(output);
			String now = Instant.now().toString();
			HighlightHistoryItem item = new HighlightHistoryItem(
					jobId,
					"ready",
					now,
					now,
					List.of(title),
					editSummary(options),
					duration,
					1,
					"/api/highlights/" + jobId + "/download",
					null);
			item.setCategory(CATEGORY_MANUAL_EDIT_DRAFT.equalsIgnoreCase(sourceCategory) ? CATEGORY_MANUAL_EDIT : sourceCategory);
			item.setOwner(ownerForSource(source));
			saveManifest(jobDirectory, item);
			if (sourceDraftJobId != null && !sourceDraftJobId.equals(jobId)) {
				deleteJobDirectory(sourceDraftJobId, "đã tạo bản chỉnh sửa mới nên xóa job upload nháp");
			}
			enforceHistoryLimit(jobId);
			LOGGER.info("[{}] Đã tạo bản ghi video chỉnh sửa mới từ {}", jobId, source);
			return new VideoEditResult(jobId, item.getDownloadUrl(), "/api/highlights/" + jobId + "/preview", "Đã tạo bản ghi video chỉnh sửa mới.");
		}
		catch (IOException ex) {
			throw new IllegalStateException("Không thể lưu bản chỉnh sửa mới.", ex);
		}
	}

	private VideoEditResult createEditedSplitClipRecord(Path source, VideoEditOptions options, MultipartFile musicFile, MultipartFile textOverlayFile, List<MultipartFile> textLayerOverlayFiles, String sourceTitle) {
		String jobId = newJobId();
		Path jobDirectory = workspace.jobsDirectory().resolve(jobId).normalize();
		Path uploadDirectory = jobDirectory.resolve("upload");
		Path workDirectory = jobDirectory.resolve("work");
		Path outputDirectory = jobDirectory.resolve("output");
		String outputFileName = "clip-01.mp4";
		Path output = outputDirectory.resolve(outputFileName);
		String title = editTitle(options, sourceTitle);
		try {
			Files.createDirectories(uploadDirectory);
			Files.createDirectories(workDirectory);
			Files.createDirectories(outputDirectory);
			Path sourceCopy = uploadDirectory.resolve("01-" + safeFileName(title + sourceExtension(source))).normalize();
			Files.copy(source, sourceCopy, StandardCopyOption.REPLACE_EXISTING);
			renderEditedToOutput(source, output, workDirectory, options, musicFile, textOverlayFile, textLayerOverlayFiles);
			double duration = ffmpegService.probeDuration(output);
			String now = Instant.now().toString();
			SplitClipHistoryItem item = new SplitClipHistoryItem(
					jobId,
					1,
					"ready",
					now,
					now,
					title,
					outputFileName,
					editSummary(options),
					0,
					duration,
					"/api/split-highlights/" + jobId + "/clips/1/download",
					null);
			item.setOwner(ownerForSource(source));
			saveSplitManifest(jobDirectory, List.of(item));
			enforceHistoryLimit(jobId);
			LOGGER.info("[{}] Đã tạo bản ghi clip tách chỉnh sửa mới từ {}", jobId, source);
			return new VideoEditResult(jobId,
					item.getDownloadUrl(),
					"/api/split-highlights/" + jobId + "/clips/1/preview",
					"Đã tạo clip tách chỉnh sửa mới.");
		}
		catch (IOException ex) {
			throw new IllegalStateException("Không thể lưu clip tách chỉnh sửa mới.", ex);
		}
	}

	private void renderEditedToOutput(Path source, Path output, Path workDirectory, VideoEditOptions options, MultipartFile musicFile, MultipartFile textOverlayFile,
			List<MultipartFile> textLayerOverlayFiles) {
		try {
			Files.createDirectories(workDirectory);
			Files.createDirectories(output.getParent());
			Path music = storeEditMusic(workDirectory, musicFile);
			Path textOverlay = storeTextOverlay(workDirectory, textOverlayFile);
			List<VideoTextLayer> textLayers = parseTextLayers(options.getTextLayersJson());
			List<Path> textLayerOverlays = storeTextLayerOverlays(workDirectory, textLayerOverlayFiles, textLayers.size());
			boolean hasTimedTextLayers = !textLayers.isEmpty() && textLayerOverlays.size() == textLayers.size();
			List<VideoEditSegment> segments = editSegments(source, options);
			Path baseOutput = workDirectory.resolve("edited-base-" + UUID.randomUUID().toString().substring(0, 8) + ".mp4");
			Path temp = hasTimedTextLayers
					? workDirectory.resolve("edited-text-" + UUID.randomUUID().toString().substring(0, 8) + ".mp4")
					: baseOutput;
			Path singleTextOverlay = hasTimedTextLayers ? null : textOverlay;
			if (segments.size() <= 1) {
				VideoEditOptions segmentOptions = copyOptionsForSegment(options, segments.get(0));
				if (hasTimedTextLayers) {
					segmentOptions = copyOptionsWithoutLegacyText(segmentOptions);
				}
				ffmpegService.renderEditedVideo(source, baseOutput, segmentOptions, music, singleTextOverlay);
			}
			else {
				List<Path> renderedSegments = new ArrayList<>();
				for (int i = 0; i < segments.size(); i++) {
					Path segmentOutput = workDirectory.resolve(String.format(Locale.ROOT, "edit-segment-%02d-%s.mp4", i + 1, UUID.randomUUID().toString().substring(0, 6)));
					VideoEditOptions segmentOptions = copyOptionsForSegment(options, segments.get(i));
					segmentOptions.setAudioMode("keep");
					if (hasTimedTextLayers) {
						segmentOptions = copyOptionsWithoutLegacyText(segmentOptions);
					}
					ffmpegService.renderEditedVideo(source, segmentOutput, segmentOptions, null, singleTextOverlay);
					renderedSegments.add(segmentOutput);
				}
				Path concatList = workDirectory.resolve("edit-concat-" + UUID.randomUUID().toString().substring(0, 6) + ".txt");
				writeConcatList(concatList, renderedSegments);
				Path concatOutput = workDirectory.resolve("edit-concat-" + UUID.randomUUID().toString().substring(0, 6) + ".mp4");
				ffmpegService.concatSegmentsFast(concatList, concatOutput);
				ffmpegService.applyEditAudio(concatOutput, baseOutput, options.getAudioMode(), music, Boolean.TRUE.equals(options.getMuteOriginalAudio()));
			}
			if (hasTimedTextLayers) {
				ffmpegService.renderTextLayerOverlays(baseOutput, temp, textLayers, textLayerOverlays);
			}
			Files.move(temp, output, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Không thể ghi file video đã chỉnh sửa.", ex);
		}
		finally {
			FileSystemUtils.deleteRecursively(workDirectory.toFile());
		}
	}

	private List<VideoEditSegment> editSegments(Path source, VideoEditOptions options) {
		double duration = ffmpegService.probeDuration(source);
		List<VideoEditSegment> parsed = parseEditSegments(options.getSegmentsJson(), duration);
		if (!parsed.isEmpty()) {
			return parsed;
		}
		double start = clamp(options.getStartSeconds() == null ? 0 : options.getStartSeconds(), 0, Math.max(0, duration - 0.2));
		double end = options.getEndSeconds() == null || options.getEndSeconds() <= start ? duration : options.getEndSeconds();
		end = clamp(end, start + Math.min(0.2, duration), duration);
		return List.of(new VideoEditSegment(round(start), round(end)));
	}

	private List<VideoEditSegment> parseEditSegments(String segmentsJson, double duration) {
		if (segmentsJson == null || segmentsJson.isBlank()) {
			return List.of();
		}
		try {
			VideoEditSegment[] rawSegments = objectMapper.readValue(segmentsJson, VideoEditSegment[].class);
			List<VideoEditSegment> segments = new ArrayList<>();
			for (VideoEditSegment segment : rawSegments) {
				if (segment == null) {
					continue;
				}
				double start = clamp(segment.getStartSeconds(), 0, Math.max(0, duration));
				double end = clamp(segment.getEndSeconds(), 0, Math.max(0, duration));
				if (end - start >= 0.2) {
					segments.add(new VideoEditSegment(round(start), round(end)));
				}
			}
			return segments;
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Dữ liệu timeline chỉnh sửa không hợp lệ.", ex);
		}
	}

	private VideoEditOptions copyOptionsForSegment(VideoEditOptions options, VideoEditSegment segment) {
		VideoEditOptions copy = new VideoEditOptions();
		copy.setSourceType(options.getSourceType());
		copy.setStartSeconds(segment.getStartSeconds());
		copy.setEndSeconds(segment.getEndSeconds());
		copy.setRotationDegrees(options.getRotationDegrees());
		copy.setVideoZoom(options.getVideoZoom());
		copy.setOutputWidth(options.getOutputWidth());
		copy.setOutputHeight(options.getOutputHeight());
		copy.setOverlayText(options.getOverlayText());
		copy.setTextXPercent(options.getTextXPercent());
		copy.setTextYPercent(options.getTextYPercent());
		copy.setTextSize(options.getTextSize());
		copy.setTextColor(options.getTextColor());
		copy.setTextFont(options.getTextFont());
		copy.setTextBackground(options.getTextBackground());
		copy.setTextPosition(options.getTextPosition());
		copy.setAudioMode(options.getAudioMode());
		copy.setMuteOriginalAudio(options.getMuteOriginalAudio());
		copy.setSaveMode(options.getSaveMode());
		copy.setTitle(options.getTitle());
		copy.setTextLayersJson(options.getTextLayersJson());
		return copy;
	}

	private VideoEditOptions copyOptionsWithoutLegacyText(VideoEditOptions options) {
		VideoEditOptions copy = new VideoEditOptions();
		copy.setSourceType(options.getSourceType());
		copy.setStartSeconds(options.getStartSeconds());
		copy.setEndSeconds(options.getEndSeconds());
		copy.setRotationDegrees(options.getRotationDegrees());
		copy.setVideoZoom(options.getVideoZoom());
		copy.setOutputWidth(options.getOutputWidth());
		copy.setOutputHeight(options.getOutputHeight());
		copy.setOverlayText(null);
		copy.setTextXPercent(null);
		copy.setTextYPercent(null);
		copy.setTextSize(null);
		copy.setTextColor(null);
		copy.setTextFont(null);
		copy.setTextBackground(null);
		copy.setTextPosition(null);
		copy.setAudioMode(options.getAudioMode());
		copy.setMuteOriginalAudio(options.getMuteOriginalAudio());
		copy.setSaveMode(options.getSaveMode());
		copy.setTitle(options.getTitle());
		copy.setTextLayersJson(options.getTextLayersJson());
		return copy;
	}

	private Path storeEditMusic(Path workDirectory, MultipartFile musicFile) throws IOException {
		if (musicFile == null || musicFile.isEmpty()) {
			return null;
		}
		Files.createDirectories(workDirectory);
		Path music = workDirectory.resolve("music-" + safeFileName(musicFile.getOriginalFilename())).normalize();
		musicFile.transferTo(music);
		return music;
	}

	private Path storeTextOverlay(Path workDirectory, MultipartFile textOverlayFile) throws IOException {
		if (textOverlayFile == null || textOverlayFile.isEmpty()) {
			return null;
		}
		Files.createDirectories(workDirectory);
		Path overlay = workDirectory.resolve("text-overlay-" + UUID.randomUUID().toString().substring(0, 8) + ".png").normalize();
		textOverlayFile.transferTo(overlay);
		return overlay;
	}

	private List<Path> storeTextLayerOverlays(Path workDirectory, List<MultipartFile> files, int expectedCount) throws IOException {
		if (expectedCount <= 0 || files == null || files.isEmpty()) {
			return List.of();
		}
		Files.createDirectories(workDirectory);
		List<Path> overlays = new ArrayList<>();
		for (int i = 0; i < files.size() && overlays.size() < expectedCount; i++) {
			MultipartFile file = files.get(i);
			if (file == null || file.isEmpty()) {
				continue;
			}
			Path overlay = workDirectory.resolve(String.format(Locale.ROOT, "text-layer-%02d-%s.png", overlays.size() + 1, UUID.randomUUID().toString().substring(0, 8))).normalize();
			file.transferTo(overlay);
			overlays.add(overlay);
		}
		if (overlays.size() != expectedCount) {
			throw new IllegalArgumentException("Số lượng file text layer không khớp dữ liệu timeline text.");
		}
		return overlays;
	}

	private List<VideoTextLayer> parseTextLayers(String textLayersJson) {
		if (textLayersJson == null || textLayersJson.isBlank()) {
			return List.of();
		}
		try {
			VideoTextLayer[] rawLayers = objectMapper.readValue(textLayersJson, VideoTextLayer[].class);
			List<VideoTextLayer> layers = new ArrayList<>();
			for (VideoTextLayer layer : rawLayers) {
				if (layer == null) {
					continue;
				}
				double start = Math.max(0, round(layer.getStartSeconds()));
				double end = Math.max(start + 0.1, round(layer.getEndSeconds()));
				VideoTextLayer safe = new VideoTextLayer();
				safe.setStartSeconds(start);
				safe.setEndSeconds(end);
				safe.setTextXPercent(layer.getTextXPercent());
				safe.setTextYPercent(layer.getTextYPercent());
				layers.add(safe);
			}
			return layers;
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Dữ liệu timeline text không hợp lệ.", ex);
		}
	}

	private Path editSourceForHighlight(String jobId, VideoEditOptions options) {
		if ("source".equalsIgnoreCase(options.getSourceType())) {
			return firstUploadedVideo(jobId);
		}
		try {
			return highlightOutputPath(jobId);
		}
		catch (IllegalArgumentException ex) {
			return firstUploadedVideo(jobId);
		}
	}

	private Path editSourceForSplitClip(String jobId, int clipIndex, VideoEditOptions options) {
		if ("source".equalsIgnoreCase(options.getSourceType())) {
			return firstUploadedVideo(jobId);
		}
		try {
			return splitClipOutputPath(jobId, clipIndex);
		}
		catch (IllegalArgumentException ex) {
			return firstUploadedVideo(jobId);
		}
	}

	private Path firstUploadedVideo(String jobId) {
		Path uploadDirectory = sourceDirectory(jobId);
		try (var stream = Files.list(uploadDirectory)) {
			return stream
					.filter(Files::isRegularFile)
					.filter(this::isVideoFile)
					.sorted(Comparator.comparing(path -> path.getFileName().toString()))
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy video gốc trong job: " + jobId));
		}
		catch (IOException ex) {
			throw new IllegalStateException("Không đọc được thư mục video gốc cho job: " + jobId, ex);
		}
	}

	private void updateHighlightManifest(String jobId, String title, Path output, VideoEditOptions options) {
		Path jobDirectory = jobDirectory(jobId);
		Path manifest = jobDirectory.resolve("manifest.json");
		HighlightHistoryItem item = Files.isRegularFile(manifest) ? readManifest(manifest) : null;
		String now = Instant.now().toString();
		if (item == null) {
			item = new HighlightHistoryItem();
			item.setJobId(jobId);
			item.setCreatedAt(now);
		}
		if (item.getCreatedAt() == null || item.getCreatedAt().isBlank()) {
			item.setCreatedAt(now);
		}
		item.setUpdatedAt(now);
		item.setStatus("ready");
		item.setInputFileNames(List.of(title));
		item.setCutNote(editSummary(options));
		item.setTotalDurationSeconds(ffmpegService.probeDuration(output));
		item.setClipsUsed(1);
		item.setDownloadUrl("/api/highlights/" + jobId + "/download");
		item.setError(null);
		if (CATEGORY_MANUAL_EDIT_DRAFT.equalsIgnoreCase(item.getCategory())) {
			item.setCategory(CATEGORY_MANUAL_EDIT);
		}
		else if (item.getCategory() == null || item.getCategory().isBlank()) {
			item.setCategory(CATEGORY_HIGHLIGHT);
		}
		saveManifest(jobDirectory, item);
	}

	private void updateSplitClipManifest(String jobId, int clipIndex, String title, Path output, VideoEditOptions options) {
		Path jobDirectory = jobDirectory(jobId);
		Path manifest = jobDirectory.resolve("split-manifest.json");
		List<SplitClipHistoryItem> items = readSplitManifest(manifest);
		String now = Instant.now().toString();
		boolean updated = false;
		for (SplitClipHistoryItem item : items) {
			if (item.getClipIndex() == clipIndex) {
				item.setUpdatedAt(now);
				item.setStatus("ready");
				item.setOriginalFileName(title);
				item.setCutNote(editSummary(options));
				item.setStartSeconds(0);
				item.setDurationSeconds(ffmpegService.probeDuration(output));
				item.setDownloadUrl("/api/split-highlights/" + jobId + "/clips/" + clipIndex + "/download");
				item.setError(null);
				updated = true;
				break;
			}
		}
		if (!updated) {
			items = new ArrayList<>(items);
			SplitClipHistoryItem newItem = new SplitClipHistoryItem(
					jobId,
					clipIndex,
					"ready",
					now,
					now,
					title,
					String.format(Locale.ROOT, "clip-%02d.mp4", clipIndex),
					editSummary(options),
					0,
					ffmpegService.probeDuration(output),
					"/api/split-highlights/" + jobId + "/clips/" + clipIndex + "/download",
					null);
			newItem.setOwner(ownerForJob(jobId));
			items.add(newItem);
		}
		saveSplitManifest(jobDirectory, items);
	}

	private String historyTitle(String jobId, String fallback) {
		Path manifest = jobDirectory(jobId).resolve("manifest.json");
		HighlightHistoryItem item = Files.isRegularFile(manifest) ? readManifest(manifest) : null;
		if (item != null && item.getInputFileNames() != null && !item.getInputFileNames().isEmpty()) {
			String value = item.getInputFileNames().get(0);
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		return fallback;
	}

	private String splitClipTitle(String jobId, int clipIndex, String fallback) {
		Path manifest = jobDirectory(jobId).resolve("split-manifest.json");
		List<SplitClipHistoryItem> items = readSplitManifest(manifest);
		for (SplitClipHistoryItem item : items) {
			if (item.getClipIndex() == clipIndex) {
				String outputName = item.getOutputFileName();
				if (outputName != null && !outputName.isBlank()) {
					return outputName + " từ " + item.getOriginalFileName();
				}
			}
		}
		return fallback;
	}

	private boolean overwriteEdit(VideoEditOptions options) {
		return options != null && "overwrite".equalsIgnoreCase(options.getSaveMode());
	}

	private String editTitle(VideoEditOptions options, String sourceTitle) {
		String custom = options == null ? "" : sanitizeNote(options.getTitle());
		if (!custom.isBlank()) {
			return custom;
		}
		String base = sourceTitle == null || sourceTitle.isBlank() ? "video" : sourceTitle;
		return "Đã chỉnh sửa từ " + base;
	}

	private String editSummary(VideoEditOptions options) {
		if (options == null) {
			return "Chỉnh sửa thủ công.";
		}
		List<String> parts = new ArrayList<>();
		if (options.getStartSeconds() != null || options.getEndSeconds() != null) {
			parts.add("cắt " + formatEditSecond(options.getStartSeconds()) + "-" + formatEditSecond(options.getEndSeconds()));
		}
		double rotation = options.getRotationDegrees() == null ? 0 : options.getRotationDegrees();
		if (Math.abs(rotation) > 0.001) {
			parts.add("xoay " + String.format(Locale.ROOT, "%.0f", rotation) + " độ");
		}
		double zoom = options.getVideoZoom() == null ? 1 : options.getVideoZoom();
		if (Math.abs(zoom - 1.0) > 0.001) {
			parts.add("zoom " + String.format(Locale.ROOT, "%.0f", zoom * 100) + "%");
		}
		if (options.getOverlayText() != null && !options.getOverlayText().isBlank()) {
			parts.add("chèn text");
		}
		if (options.getAudioMode() != null && !"keep".equalsIgnoreCase(options.getAudioMode())) {
			parts.add("âm thanh " + options.getAudioMode());
		}
		if (Boolean.TRUE.equals(options.getMuteOriginalAudio())) {
			parts.add("tắt tiếng gốc");
		}
		if (options.getOutputWidth() != null && options.getOutputHeight() != null) {
			parts.add("xuất " + options.getOutputWidth() + "x" + options.getOutputHeight());
		}
		return parts.isEmpty() ? "Chỉnh sửa thủ công." : "Chỉnh sửa thủ công: " + String.join(", ", parts) + ".";
	}

	private String formatEditSecond(Double value) {
		if (value == null) {
			return "";
		}
		return String.format(Locale.ROOT, "%.1fs", Math.max(0, value));
	}

	private String sourceExtension(Path source) {
		String name = source == null ? "" : source.getFileName().toString();
		int dot = name.lastIndexOf('.');
		if (dot >= 0 && dot < name.length() - 1) {
			return name.substring(dot);
		}
		return ".mp4";
	}

	private boolean isVideoFile(Path path) {
		String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
		return VIDEO_EXTENSIONS.stream().anyMatch(name::endsWith);
	}

	private boolean isManualEditHistory(HighlightHistoryItem item) {
		return item != null
				&& CATEGORY_MANUAL_EDIT.equalsIgnoreCase(item.getCategory())
				&& !isManualEditDraftNote(item);
	}

	private boolean isFacebookBatchHistory(HighlightHistoryItem item) {
		return item != null && CATEGORY_FACEBOOK_BATCH.equalsIgnoreCase(item.getCategory());
	}

	private boolean isHighlightHistory(HighlightHistoryItem item) {
		return item != null
				&& !CATEGORY_MANUAL_EDIT.equalsIgnoreCase(item.getCategory())
				&& !CATEGORY_MANUAL_EDIT_DRAFT.equalsIgnoreCase(item.getCategory())
				&& !CATEGORY_FACEBOOK_BATCH.equalsIgnoreCase(item.getCategory());
	}

	private boolean isManualEditDraftNote(HighlightHistoryItem item) {
		String note = item.getCutNote() == null ? "" : item.getCutNote().trim();
		return "Video upload để chỉnh sửa thủ công.".equals(note)
				|| "Video tải từ link để chỉnh sửa thủ công.".equals(note);
	}

	private String ownerForSource(Path source) {
		if (source == null) {
			return "local";
		}
		Path current = source.toAbsolutePath().normalize();
		while (current != null) {
			Path manifest = current.resolve("manifest.json");
			if (Files.isRegularFile(manifest)) {
				HighlightHistoryItem item = readManifest(manifest);
				if (item != null && item.getOwner() != null && !item.getOwner().isBlank()) {
					return item.getOwner();
				}
			}
			Path splitManifest = current.resolve("split-manifest.json");
			if (Files.isRegularFile(splitManifest)) {
				for (SplitClipHistoryItem item : readSplitManifest(splitManifest)) {
					if (item.getOwner() != null && !item.getOwner().isBlank()) {
						return item.getOwner();
					}
				}
			}
			current = current.getParent();
		}
		return "local";
	}

	private String ownerForJob(String jobId) {
		Path manifest = jobDirectory(jobId).resolve("manifest.json");
		if (Files.isRegularFile(manifest)) {
			HighlightHistoryItem item = readManifest(manifest);
			if (item != null && item.getOwner() != null && !item.getOwner().isBlank()) {
				return item.getOwner();
			}
		}
		Path splitManifest = jobDirectory(jobId).resolve("split-manifest.json");
		if (Files.isRegularFile(splitManifest)) {
			for (SplitClipHistoryItem item : readSplitManifest(splitManifest)) {
				if (item.getOwner() != null && !item.getOwner().isBlank()) {
					return item.getOwner();
				}
			}
		}
		HighlightJobStatus status = statuses.get(jobId);
		return status == null ? "local" : safeOwner(status.getOwner());
	}
	private String categoryForSource(Path source) {
		if (source == null) {
			return CATEGORY_HIGHLIGHT;
		}
		Path current = source.toAbsolutePath().normalize();
		while (current != null) {
			Path manifest = current.resolve("manifest.json");
			if (Files.isRegularFile(manifest)) {
				HighlightHistoryItem item = readManifest(manifest);
				if (item != null && item.getCategory() != null && !item.getCategory().isBlank()) {
					return item.getCategory();
				}
			}
			current = current.getParent();
		}
		return CATEGORY_HIGHLIGHT;
	}

	private String manualEditDraftJobIdForSource(Path source) {
		if (source == null) {
			return null;
		}
		Path current = source.toAbsolutePath().normalize();
		while (current != null) {
			Path manifest = current.resolve("manifest.json");
			if (Files.isRegularFile(manifest)) {
				HighlightHistoryItem item = readManifest(manifest);
				if (item != null && CATEGORY_MANUAL_EDIT_DRAFT.equalsIgnoreCase(item.getCategory())) {
					return item.getJobId() == null || item.getJobId().isBlank()
							? current.getFileName().toString()
							: item.getJobId();
				}
			}
			current = current.getParent();
		}
		return null;
	}

	private List<HighlightHistoryItem> readHistoryItems() {
		try (var stream = Files.list(workspace.jobsDirectory())) {
			return stream.filter(Files::isDirectory)
					.map(path -> path.resolve("manifest.json"))
					.filter(Files::isRegularFile)
					.map(this::readManifest)
					.filter(item -> item != null)
					.collect(Collectors.toList());
		}
		catch (IOException ex) {
			return List.of();
		}
	}

	private List<SplitClipHistoryItem> readSplitHistoryItems() {
		try (var stream = Files.list(workspace.jobsDirectory())) {
			return stream.filter(Files::isDirectory)
					.map(path -> path.resolve("split-manifest.json"))
					.filter(Files::isRegularFile)
					.map(this::readSplitManifest)
					.flatMap(List::stream)
					.collect(Collectors.toList());
		}
		catch (IOException ex) {
			return List.of();
		}
	}

	private List<HistoryJobRef> readHistoryJobRefs() {
		try (var stream = Files.list(workspace.jobsDirectory())) {
			return stream.filter(Files::isDirectory)
					.map(this::readHistoryJobRef)
					.filter(ref -> ref != null)
					.collect(Collectors.toList());
		}
		catch (IOException ex) {
			return List.of();
		}
	}

	private HistoryJobRef readHistoryJobRef(Path jobDirectory) {
		Path manifest = jobDirectory.resolve("manifest.json");
		if (Files.isRegularFile(manifest)) {
			HighlightHistoryItem item = readManifest(manifest);
			if (item != null) {
				return new HistoryJobRef(item.getJobId(), item.getCreatedAt());
			}
		}

		Path splitManifest = jobDirectory.resolve("split-manifest.json");
		if (Files.isRegularFile(splitManifest)) {
			List<SplitClipHistoryItem> items = readSplitManifest(splitManifest);
			if (!items.isEmpty()) {
				String jobId = items.stream()
						.map(SplitClipHistoryItem::getJobId)
						.filter(value -> value != null && !value.isBlank())
						.findFirst()
						.orElse(jobDirectory.getFileName().toString());
				String createdAt = items.stream()
						.map(SplitClipHistoryItem::getCreatedAt)
						.filter(value -> value != null && !value.isBlank())
						.min(String::compareTo)
						.orElse(null);
				return new HistoryJobRef(jobId, createdAt);
			}
			return new HistoryJobRef(jobDirectory.getFileName().toString(), jobDirectory.getFileName().toString());
		}
		return null;
	}

	private HighlightHistoryItem readManifest(Path manifest) {
		try {
			return objectMapper.readValue(manifest.toFile(), HighlightHistoryItem.class);
		}
		catch (IOException ex) {
			return null;
		}
	}

	private List<SplitClipHistoryItem> readSplitManifest(Path manifest) {
		try {
			SplitClipHistoryItem[] items = objectMapper.readValue(manifest.toFile(), SplitClipHistoryItem[].class);
			return new ArrayList<>(List.of(items));
		}
		catch (IOException ex) {
			return List.of();
		}
	}

	private void saveManifest(Path jobDirectory, HighlightHistoryItem item) {
		try {
			Files.createDirectories(jobDirectory);
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(jobDirectory.resolve("manifest.json").toFile(), item);
		}
		catch (IOException ex) {
			LOGGER.warn("Không ghi được manifest cho job {}", item.getJobId(), ex);
		}
	}

	private void saveSplitManifest(Path jobDirectory, List<SplitClipHistoryItem> items) {
		try {
			Files.createDirectories(jobDirectory);
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(jobDirectory.resolve("split-manifest.json").toFile(), items);
		}
		catch (IOException ex) {
			LOGGER.warn("Không ghi được split manifest cho job {}", jobDirectory.getFileName(), ex);
		}
	}

	private HighlightHistoryItem toHistory(HighlightJobStatus status, String state, double totalDuration, int clipsUsed, String downloadUrl, String error) {
		String now = Instant.now().toString();
		HighlightHistoryItem item = new HighlightHistoryItem(
				status.getJobId(),
				state,
				now,
				now,
				status.getInputFileNames(),
				status.getCutNote(),
				totalDuration,
				clipsUsed,
				downloadUrl,
				error);
		item.setOwner(status.getOwner());
		return item;
	}

	private SplitClipHistoryItem toSplitHistory(HighlightJobStatus status, int clipIndex, String state, String originalFileName,
			String outputFileName, double startSeconds, double durationSeconds, String downloadUrl, String error) {
		String now = Instant.now().toString();
		SplitClipHistoryItem item = new SplitClipHistoryItem(
				status.getJobId(),
				clipIndex,
				state,
				now,
				now,
				originalFileName,
				outputFileName,
				status.getCutNote(),
				startSeconds,
				durationSeconds,
				downloadUrl,
				error);
		item.setOwner(status.getOwner());
		return item;
	}

	private String safeOwner(String owner) {
		return owner == null || owner.isBlank() ? "local" : owner.trim();
	}

	private boolean ownerMatches(String itemOwner, String owner) {
		String safe = safeOwner(owner);
		return "*".equals(safe) || safe.equals(itemOwner == null ? "" : itemOwner.trim());
	}

	private boolean isJobOwnedBy(String jobId, String owner) {
		HighlightJobStatus status = statuses.get(jobId);
		if (status != null && !ownerMatches(status.getOwner(), owner)) {
			return false;
		}
		Path manifest = jobDirectory(jobId).resolve("manifest.json");
		if (Files.isRegularFile(manifest)) {
			HighlightHistoryItem item = readManifest(manifest);
			return item != null && ownerMatches(item.getOwner(), owner);
		}
		Path splitManifest = jobDirectory(jobId).resolve("split-manifest.json");
		if (Files.isRegularFile(splitManifest)) {
			List<SplitClipHistoryItem> items = readSplitManifest(splitManifest);
			return items.isEmpty() || items.stream().anyMatch(item -> ownerMatches(item.getOwner(), owner));
		}
		return false;
	}
	private String originalNameFor(HighlightJobStatus status, int sourceIndex) {
		List<String> names = status.getInputFileNames();
		if (names != null && sourceIndex >= 0 && sourceIndex < names.size()) {
			String name = names.get(sourceIndex);
			if (name != null && !name.isBlank()) {
				return name;
			}
		}
		return "video-" + (sourceIndex + 1);
	}

	private String splitClipKey(String jobId, int clipIndex) {
		return jobId + ":" + clipIndex;
	}

	private void progress(HighlightJobStatus status, int percent, String phase) {
		status.progress(percent, phase);
		LOGGER.info("[{}] {}% - {}", status.getJobId(), percent, phase);
	}

	private Path jobDirectory(String jobId) {
		if (jobId == null || !jobId.matches("[a-zA-Z0-9-]+")) {
			throw new IllegalArgumentException("Job id không hợp lệ.");
		}
		return workspace.jobsDirectory().resolve(jobId).normalize();
	}

	private void writeError(Path jobDirectory, Exception ex) {
		try {
			Files.createDirectories(jobDirectory);
			Files.writeString(jobDirectory.resolve("error.txt"), ex.toString(), StandardCharsets.UTF_8);
		}
		catch (IOException ignored) {
			// Error reporting is best effort.
		}
	}

	private String newJobId() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
				+ "-" + UUID.randomUUID().toString().substring(0, 8);
	}

	private String safeFileName(String value) {
		String name = value == null || value.isBlank() ? "upload.mp4" : value;
		name = name.replaceAll("[^a-zA-Z0-9._-]", "-");
		if (!name.contains(".")) {
			name += ".mp4";
		}
		return name;
	}

	private int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}

	private double round(double value) {
		return Math.round(value * 100.0) / 100.0;
	}

	private String sanitizeNote(String value) {
		if (value == null) {
			return "";
		}
		String trimmed = value.trim();
		if (trimmed.length() > 500) {
			return trimmed.substring(0, 500);
		}
		return trimmed;
	}

	private List<String> normalizeUrls(List<String> urls) {
		if (urls == null) {
			return List.of();
		}
		return urls.stream()
				.filter(url -> url != null && !url.isBlank())
				.map(String::trim)
				.distinct()
				.collect(Collectors.toList());
	}

	private List<String> normalizeBatchUrls(String value) {
		if (value == null || value.isBlank()) {
			return List.of();
		}
		String decoded = value
				.replace("\\/", "/")
				.replace("\\u002F", "/")
				.replace("&amp;", "&")
				.replace("&quot;", "\"")
				.replace("&#039;", "'");
		LinkedHashSet<String> urls = new LinkedHashSet<>();
		Matcher fullUrlMatcher = Pattern.compile("https?://(?:www\\.|m\\.|mbasic\\.)?facebook\\.com/[^\\s\"'<>]+", Pattern.CASE_INSENSITIVE)
				.matcher(decoded);
		while (fullUrlMatcher.find()) {
			urls.add(cleanFacebookUrl(fullUrlMatcher.group()));
		}
		Matcher reelMatcher = Pattern.compile("(?:(?:href|src)=['\"])?/(?:[^\\s\"'<>]+/)?reel/(\\d+)", Pattern.CASE_INSENSITIVE)
				.matcher(decoded);
		while (reelMatcher.find()) {
			urls.add("https://www.facebook.com/reel/" + reelMatcher.group(1) + "/");
		}
		if (!urls.isEmpty()) {
			return new ArrayList<>(urls);
		}
		return List.of(decoded.split("[\\s,;]+")).stream()
				.filter(url -> url != null && !url.isBlank())
				.map(this::cleanFacebookUrl)
				.distinct()
				.collect(Collectors.toList());
	}

	private String cleanFacebookUrl(String value) {
		String url = value == null ? "" : value.trim();
		url = url.replaceAll("[\"'<>]+$", "");
		Matcher reel = Pattern.compile(".*/reel/(\\d+).*", Pattern.CASE_INSENSITIVE).matcher(url);
		if (reel.matches()) {
			return "https://www.facebook.com/reel/" + reel.group(1) + "/";
		}
		return url;
	}

	private static String normalizeSearchText(String value) {
		String normalized = Normalizer.normalize(value == null ? "" : value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
		return normalized.replaceAll("\\p{M}", "");
	}

	private static boolean containsAny(String text, String... keywords) {
		for (String keyword : keywords) {
			if (text.contains(keyword)) {
				return true;
			}
		}
		return false;
	}

	private RenderProfile renderProfile(String aspectRatio) {
		String value = aspectRatio == null ? "" : aspectRatio.trim().toLowerCase(Locale.ROOT);
		if ("16:9".equals(value) || "16x9".equals(value) || "landscape".equals(value) || "ngang".equals(value)) {
			return new RenderProfile(1920, 1080, "16:9 ngang");
		}
		return new RenderProfile(DEFAULT_WIDTH, DEFAULT_HEIGHT, "9:16 dọc");
	}

	private static List<RequestedRange> parseRequestedRanges(String note) {
		String text = normalizeSearchText(note)
				.replaceAll("\\bgiay\\s+thu\\s+(\\d)", "$1")
				.replaceAll("\\bgiay\\s+(\\d)", "$1")
				.replaceAll("\\bphut\\s+(\\d)", "$1p");
		String timeToken = "(?:\\d{1,2}:\\d{2}(?::\\d{2})?|\\d+(?:[\\.,]\\d+)?\\s*(?:s|sec|giay|p|phut|m)?)";
		Pattern pattern = Pattern.compile("(?:(?:video|file|v)\\s*(\\d+)\\s*[:\\-]?\\s*)?(?:(?:tu|from|start|bat dau tu)\\s*)?("
				+ timeToken + ")\\s*(?:-|–|—|den|toi|to|->)\\s*(" + timeToken + ")");
		Matcher matcher = pattern.matcher(text);
		List<RequestedRange> ranges = new ArrayList<>();
		Integer lastSourceIndex = null;
		while (matcher.find() && ranges.size() < 30) {
			if (matcher.group(1) != null) {
				lastSourceIndex = Math.max(0, Integer.parseInt(matcher.group(1)) - 1);
			}
			Double start = parseTimeToken(matcher.group(2));
			Double end = parseTimeToken(matcher.group(3));
			if (start == null || end == null) {
				continue;
			}
			if (end < start) {
				double temp = start;
				start = end;
				end = temp;
			}
			ranges.add(new RequestedRange(lastSourceIndex, start, end));
		}
		return ranges;
	}

	private static Double parseTimeToken(String token) {
		if (token == null || token.isBlank()) {
			return null;
		}
		String value = token.trim().toLowerCase(Locale.ROOT).replace(',', '.').replaceAll("\\s+", "");
		if (value.contains(":")) {
			String[] parts = value.split(":");
			try {
				double seconds = 0;
				for (String part : parts) {
					seconds = seconds * 60 + Double.parseDouble(part);
				}
				return seconds;
			}
			catch (NumberFormatException ex) {
				return null;
			}
		}
		boolean minutes = value.endsWith("p") || value.endsWith("phut") || value.endsWith("m");
		value = value.replaceAll("(sec|giay|phut|s|p|m)$", "");
		try {
			double number = Double.parseDouble(value);
			return minutes ? number * 60 : number;
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}

	private static class HistoryJobRef {
		private final String jobId;
		private final String createdAt;

		HistoryJobRef(String jobId, String createdAt) {
			this.jobId = jobId;
			this.createdAt = createdAt;
		}

		String jobId() {
			return jobId;
		}

		String createdAt() {
			return createdAt;
		}
	}

	private static class RenderProfile {
		private final int width;
		private final int height;
		private final String label;

		RenderProfile(int width, int height, String label) {
			this.width = width;
			this.height = height;
			this.label = label;
		}

		int width() {
			return width;
		}

		int height() {
			return height;
		}

		String label() {
			return label;
		}
	}

	private static class RequestedRange {
		private final Integer sourceIndex;
		private final double start;
		private final Double end;

		RequestedRange(Integer sourceIndex, double start, Double end) {
			this.sourceIndex = sourceIndex;
			this.start = start;
			this.end = end;
		}

		Integer sourceIndex() {
			return sourceIndex;
		}

		double start() {
			return start;
		}

		Double end() {
			return end;
		}

		@Override
		public String toString() {
			String source = sourceIndex == null ? "video đầu tiên" : "video " + (sourceIndex + 1);
			return source + " " + start + "s-" + end + "s";
		}
	}

	private static class CutPreference {
		private final String rawNote;
		private final boolean preferAction;
		private final boolean preferTalking;
		private final boolean preferCalm;
		private final boolean preferStart;
		private final boolean preferEnd;
		private final boolean avoidIntroOutro;
		private final boolean spreadWide;
		private final List<RequestedRange> requestedRanges;

		private CutPreference(String rawNote, boolean preferAction, boolean preferTalking, boolean preferCalm,
				boolean preferStart, boolean preferEnd, boolean avoidIntroOutro, boolean spreadWide, List<RequestedRange> requestedRanges) {
			this.rawNote = rawNote;
			this.preferAction = preferAction;
			this.preferTalking = preferTalking;
			this.preferCalm = preferCalm;
			this.preferStart = preferStart;
			this.preferEnd = preferEnd;
			this.avoidIntroOutro = avoidIntroOutro;
			this.spreadWide = spreadWide;
			this.requestedRanges = requestedRanges;
		}

		static CutPreference from(String note) {
			String raw = note == null ? "" : note.trim();
			String normalized = normalizeSearchText(raw);
			List<RequestedRange> requestedRanges = parseRequestedRanges(raw);
			boolean avoidStart = containsAny(normalized, "bo intro", "bo dau", "tranh dau", "khong lay dau", "cat dau");
			boolean avoidEnd = containsAny(normalized, "bo cuoi", "tranh cuoi", "khong lay cuoi", "cat cuoi");
			boolean start = !avoidStart && containsAny(normalized, "lay dau", "doan dau", "mo dau", "intro");
			boolean end = !avoidEnd && containsAny(normalized, "lay cuoi", "doan cuoi", "ket video", "ending", "phan cuoi");
			return new CutPreference(
					raw,
					containsAny(normalized, "hanh dong", "chuyen dong", "nhanh", "cao trao", "gay can", "kich tinh", "soi dong", "drama", "bien hinh"),
					containsAny(normalized, "noi", "giai thich", "review", "tam su", "phong van", "hoi thoai", "voice", "am thanh", "nhac", "tieng"),
					containsAny(normalized, "cham", "it chuyen canh", "on dinh", "ro mat", "canh dep", "nhe nhang"),
					start,
					end,
					!start && !end,
					containsAny(normalized, "xa nhau", "rai deu", "khong sat", "dung sat", "gan nhau"),
					requestedRanges);
		}

		double score(double start, double duration, int sceneCount, double motionScore, double audioScore, double positionScore) {
			double motionWeight = preferAction ? 1.35 : (preferTalking || preferCalm ? 0.72 : 1.0);
			double audioWeight = preferTalking ? 1.45 : (preferAction ? 1.12 : 1.0);
			double positionWeight = avoidIntroOutro ? 1.25 : 1.0;
			double stabilityScore = Math.max(0, 1.0 - Math.min(sceneCount, 5) / 5.0) * 22.0;
			double chaosPenalty = Math.max(0, sceneCount - 5) * (preferCalm || preferTalking ? 12.0 : 7.0);
			double silentPenalty = audioScore <= 4.0 && !preferCalm ? 18.0 : 0;
			double deadScenePenalty = sceneCount == 0 && !preferCalm && !preferTalking ? 8.0 : 0;
			double noteBonus = 0;
			if (preferCalm || preferTalking) {
				noteBonus += stabilityScore;
			}
			if (preferAction && sceneCount >= 1 && sceneCount <= 4) {
				noteBonus += 10.0;
			}
			return (motionScore * motionWeight)
					+ (audioScore * audioWeight)
					+ (positionScore * positionWeight)
					+ windowBonus(start, duration)
					+ noteBonus
					- chaosPenalty
					- silentPenalty
					- deadScenePenalty;
		}

		double windowStart(double duration, double clipSeconds) {
			double maxStart = Math.max(0, duration - clipSeconds);
			if (preferStart) {
				return 0;
			}
			if (preferEnd) {
				return Math.min(maxStart, duration * 0.45);
			}
			return Math.min(maxStart, Math.max(clipSeconds, duration * 0.10));
		}

		double windowEnd(double duration, double clipSeconds) {
			double maxStart = Math.max(0, duration - clipSeconds);
			if (preferStart) {
				return Math.min(maxStart, duration * 0.55);
			}
			if (preferEnd) {
				return maxStart;
			}
			return Math.max(0, maxStart - duration * 0.10);
		}

		double minimumGapMultiplier() {
			return spreadWide ? 3.4 : 2.6;
		}

		List<RequestedRange> requestedRanges() {
			return requestedRanges;
		}

		String summary() {
			if (rawNote.isBlank()) {
				return "ưu tiên đoạn có chuyển động/âm thanh rõ, tránh intro và outro, rải đều timeline.";
			}
			List<String> tags = new ArrayList<>();
			if (!requestedRanges.isEmpty()) {
				tags.add(requestedRanges.size() + " khoảng thời gian cụ thể");
			}
			if (preferAction) {
				tags.add("hành động/chuyển động");
			}
			if (preferTalking) {
				tags.add("âm thanh/lời nói");
			}
			if (preferCalm) {
				tags.add("ổn định/ít chuyển cảnh");
			}
			if (preferStart) {
				tags.add("ưu tiên đầu video");
			}
			if (preferEnd) {
				tags.add("ưu tiên cuối video");
			}
			if (spreadWide) {
				tags.add("rải xa nhau");
			}
			if (tags.isEmpty()) {
				tags.add("ghi chú tự do");
			}
			return String.join(", ", tags);
		}

		private double windowBonus(double start, double duration) {
			if (duration <= 0) {
				return 0;
			}
			double ratio = start / duration;
			if (preferStart) {
				return ratio <= 0.35 ? 18.0 * (1.0 - ratio / 0.35) : -14.0;
			}
			if (preferEnd) {
				return ratio >= 0.60 ? 18.0 * ((ratio - 0.60) / 0.40) : -10.0;
			}
			if (!avoidIntroOutro) {
				return 0;
			}
			if (ratio < 0.10 || ratio > 0.88) {
				return -26.0;
			}
			if (ratio >= 0.18 && ratio <= 0.78) {
				return 8.0;
			}
			return 3.0;
		}
	}

	private static class VideoSource {
		private final int index;
		private final Path path;
		private final double duration;

		VideoSource(int index, Path path, double duration) {
			this.index = index;
			this.path = path;
			this.duration = duration;
		}

		int index() {
			return index;
		}

		Path path() {
			return path;
		}

		double duration() {
			return duration;
		}
	}

	private static class CandidateSeed {
		private final double start;
		private final int sceneCount;
		private final double motionScore;
		private final double positionScore;
		private final double preliminaryScore;

		CandidateSeed(double start, int sceneCount, double motionScore, double positionScore, double preliminaryScore) {
			this.start = start;
			this.sceneCount = sceneCount;
			this.motionScore = motionScore;
			this.positionScore = positionScore;
			this.preliminaryScore = preliminaryScore;
		}

		double start() {
			return start;
		}

		int sceneCount() {
			return sceneCount;
		}

		double motionScore() {
			return motionScore;
		}

		double positionScore() {
			return positionScore;
		}

		double preliminaryScore() {
			return preliminaryScore;
		}
	}

	private static class SelectedSegment {
		private final VideoSource source;
		private final double start;
		private final double duration;
		private final double score;
		private final double meanVolume;
		private final int sceneCount;
		private final double motionScore;

		SelectedSegment(VideoSource source, double start, double duration, double score, double meanVolume, int sceneCount, double motionScore) {
			this.source = source;
			this.start = start;
			this.duration = duration;
			this.score = score;
			this.meanVolume = meanVolume;
			this.sceneCount = sceneCount;
			this.motionScore = motionScore;
		}

		VideoSource source() {
			return source;
		}

		double start() {
			return start;
		}

		double duration() {
			return duration;
		}

		double end() {
			return start + duration;
		}

		double score() {
			return score;
		}

		double meanVolume() {
			return meanVolume;
		}

		int sceneCount() {
			return sceneCount;
		}

		double motionScore() {
			return motionScore;
		}

		@Override
		public String toString() {
			return source.path().getFileName() + "@" + start + "s-" + end() + "s";
		}
	}
}
