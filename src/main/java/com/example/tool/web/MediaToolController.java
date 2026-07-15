package com.example.tool.web;

import com.example.tool.model.BatchRequest;
import com.example.tool.model.BatchResult;
import com.example.tool.model.DownloadRequest;
import com.example.tool.model.DownloadResult;
import com.example.tool.model.FacebookBatchRequest;
import com.example.tool.model.HealthResult;
import com.example.tool.model.HighlightDeleteRequest;
import com.example.tool.model.HighlightDeleteResult;
import com.example.tool.model.HighlightHistoryPage;
import com.example.tool.model.HighlightJobStatus;
import com.example.tool.model.NetworkVideoRequest;
import com.example.tool.model.SplitClipDeleteRequest;
import com.example.tool.model.SplitClipDeleteResult;
import com.example.tool.model.SplitClipHistoryPage;
import com.example.tool.model.SourceSearchRequest;
import com.example.tool.model.SourceSearchResult;
import com.example.tool.model.VideoEditOptions;
import com.example.tool.model.VideoEditResult;
import com.example.tool.service.FfmpegService;
import com.example.tool.service.HighlightService;
import com.example.tool.service.LocalFileLocationService;
import com.example.tool.service.MediaWorkspace;
import com.example.tool.service.SourceVideoService;
import com.example.tool.service.VideoBatchService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api")
public class MediaToolController {

	private final MediaWorkspace workspace;
	private final FfmpegService ffmpegService;
	private final HighlightService highlightService;
	private final LocalFileLocationService localFileLocationService;
	private final SourceVideoService sourceVideoService;
	private final VideoBatchService videoBatchService;

	public MediaToolController(
			MediaWorkspace workspace,
			FfmpegService ffmpegService,
			HighlightService highlightService,
			LocalFileLocationService localFileLocationService,
			SourceVideoService sourceVideoService,
			VideoBatchService videoBatchService) {
		this.workspace = workspace;
		this.ffmpegService = ffmpegService;
		this.highlightService = highlightService;
		this.localFileLocationService = localFileLocationService;
		this.sourceVideoService = sourceVideoService;
		this.videoBatchService = videoBatchService;
	}

	@GetMapping("/health")
	public HealthResult health() {
		workspace.ensureBaseDirectories();
		boolean ffmpeg = ffmpegService.ffmpegAvailable();
		boolean ffprobe = ffmpegService.ffprobeAvailable();
		String message = ffmpeg && ffprobe
				? "Ready"
				: "Install ffmpeg and ffprobe, or configure app.media.ffmpeg-path/app.media.ffprobe-path.";
		return new HealthResult(
				ffmpeg,
				ffprobe,
				workspace.workspace().toString(),
				workspace.sourcesDirectory().toString(),
				workspace.outputsDirectory().toString(),
				message);
	}

	@PostMapping("/locations/jobs/open")
	public OpenLocationResult openJobsLocation() {
		workspace.ensureBaseDirectories();
		return openLocation(workspace.jobsDirectory());
	}

	@PostMapping("/sources/search")
	public SourceSearchResult searchSources(@RequestBody SourceSearchRequest request) {
		return sourceVideoService.search(request);
	}

	@PostMapping("/sources/download")
	public DownloadResult download(@RequestBody DownloadRequest request) {
		return sourceVideoService.download(request);
	}

	@PostMapping("/videos/batch")
	public BatchResult createBatch(@RequestBody BatchRequest request) {
		return videoBatchService.createBatch(request);
	}

	@PostMapping(value = "/edit-videos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public VideoEditResult createEditableVideo(@RequestParam("video") MultipartFile video) {
		return highlightService.createEditableVideo(video, currentOwner());
	}

	@PostMapping("/edit-videos/from-url")
	public VideoEditResult createEditableVideoFromUrl(@RequestBody NetworkVideoRequest request) {
		return highlightService.createEditableVideoFromUrls(
				request.normalizedUrls(),
				request.getCookiesFilePath(),
				currentOwner());
	}

	@GetMapping("/edit-videos")
	public HighlightHistoryPage manualEditHistory(
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		return highlightService.manualEditHistory(page, size, currentOwner());
	}

	@PostMapping("/edit-videos/delete")
	public HighlightDeleteResult deleteManualEditVideos(@RequestBody HighlightDeleteRequest request) {
		return highlightService.deleteManualEditVideos(request.getJobIds(), currentOwner());
	}

	@PostMapping("/facebook-batches")
	public HighlightJobStatus createFacebookBatch(@RequestBody FacebookBatchRequest request) {
		return highlightService.createFacebookBatchDownload(
				request.getReelsUrl(),
				request.getStartIndex(),
				request.getEndIndex(),
				request.getCookiesFilePath(),
				currentOwner());
	}

	@GetMapping("/facebook-batches/{jobId}")
	public HighlightJobStatus facebookBatchStatus(@PathVariable String jobId) {
		return highlightService.status(jobId, currentOwner());
	}

	@GetMapping("/facebook-batches")
	public HighlightHistoryPage facebookBatchHistory(
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		return highlightService.facebookBatchHistory(page, size, currentOwner());
	}

	@PostMapping("/facebook-batches/delete")
	public HighlightDeleteResult deleteFacebookBatches(@RequestBody HighlightDeleteRequest request) {
		return highlightService.deleteFacebookBatchVideos(request.getJobIds(), currentOwner());
	}

	@PostMapping("/facebook-batches/{jobId}/open-location")
	public OpenLocationResult openFacebookBatchLocation(@PathVariable String jobId) {
		highlightService.assertJobOwner(jobId, currentOwner());
		return openLocation(highlightService.sourceDirectory(jobId));
	}

	@PostMapping(value = "/highlights", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public HighlightJobStatus createHighlight(
			@RequestParam(value = "videos", required = false) List<MultipartFile> videos,
			@RequestParam(value = "video", required = false) MultipartFile video,
			@RequestParam(value = "clipCount", required = false) Integer clipCount,
			@RequestParam(value = "clipSeconds", required = false) Double clipSeconds,
			@RequestParam(value = "cutNote", required = false) String cutNote,
			@RequestParam(value = "aspectRatio", required = false) String aspectRatio) {
		List<MultipartFile> allVideos = new ArrayList<>();
		if (videos != null) {
			allVideos.addAll(videos);
		}
		if (video != null && !video.isEmpty()) {
			allVideos.add(video);
		}
		return highlightService.createHighlight(allVideos, clipCount, clipSeconds, cutNote, aspectRatio, currentOwner());
	}

	@PostMapping("/highlights/from-url")
	public HighlightJobStatus createHighlightFromUrl(@RequestBody NetworkVideoRequest request) {
		return highlightService.createHighlightFromUrls(
				request.normalizedUrls(),
				request.getClipCount(),
				request.getClipSeconds(),
				request.getCutNote(),
				request.getCookiesFilePath(),
				request.getAspectRatio(),
				currentOwner());
	}

	@PostMapping(value = "/split-highlights", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public HighlightJobStatus createSplitHighlight(
			@RequestParam(value = "videos", required = false) List<MultipartFile> videos,
			@RequestParam(value = "video", required = false) MultipartFile video,
			@RequestParam(value = "clipCount", required = false) Integer clipCount,
			@RequestParam(value = "clipSeconds", required = false) Double clipSeconds,
			@RequestParam(value = "cutNote", required = false) String cutNote,
			@RequestParam(value = "aspectRatio", required = false) String aspectRatio) {
		List<MultipartFile> allVideos = new ArrayList<>();
		if (videos != null) {
			allVideos.addAll(videos);
		}
		if (video != null && !video.isEmpty()) {
			allVideos.add(video);
		}
		return highlightService.createSplitClips(allVideos, clipCount, clipSeconds, cutNote, aspectRatio, currentOwner());
	}

	@PostMapping("/split-highlights/from-url")
	public HighlightJobStatus createSplitHighlightFromUrl(@RequestBody NetworkVideoRequest request) {
		return highlightService.createSplitClipsFromUrls(
				request.normalizedUrls(),
				request.getClipCount(),
				request.getClipSeconds(),
				request.getCutNote(),
				request.getCookiesFilePath(),
				request.getAspectRatio(),
				currentOwner());
	}

	@GetMapping("/split-highlights/{jobId}")
	public HighlightJobStatus splitHighlightStatus(@PathVariable String jobId) {
		return highlightService.status(jobId, currentOwner());
	}

	@GetMapping("/split-highlights")
	public SplitClipHistoryPage splitHighlightHistory(
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		return highlightService.splitHistory(page, size, currentOwner());
	}

	@DeleteMapping("/split-highlights/{jobId}/clips/{clipIndex}")
	public SplitClipDeleteResult deleteSplitClip(@PathVariable String jobId, @PathVariable int clipIndex) {
		return highlightService.deleteSplitClips(List.of(new SplitClipDeleteRequest.ClipRef(jobId, clipIndex)), currentOwner());
	}

	@PostMapping("/split-highlights/delete")
	public SplitClipDeleteResult deleteSplitClips(@RequestBody SplitClipDeleteRequest request) {
		return highlightService.deleteSplitClips(request.getClips(), currentOwner());
	}

	@PostMapping(value = "/split-highlights/{jobId}/clips/{clipIndex}/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public VideoEditResult editSplitClip(
			@PathVariable String jobId,
			@PathVariable int clipIndex,
			@RequestParam(value = "sourceType", required = false) String sourceType,
			@RequestParam(value = "startSeconds", required = false) Double startSeconds,
			@RequestParam(value = "endSeconds", required = false) Double endSeconds,
			@RequestParam(value = "rotationDegrees", required = false) Double rotationDegrees,
			@RequestParam(value = "videoZoom", required = false) Double videoZoom,
			@RequestParam(value = "outputWidth", required = false) Integer outputWidth,
			@RequestParam(value = "outputHeight", required = false) Integer outputHeight,
			@RequestParam(value = "overlayText", required = false) String overlayText,
			@RequestParam(value = "textXPercent", required = false) Double textXPercent,
			@RequestParam(value = "textYPercent", required = false) Double textYPercent,
			@RequestParam(value = "textSize", required = false) Integer textSize,
			@RequestParam(value = "textColor", required = false) String textColor,
			@RequestParam(value = "textFont", required = false) String textFont,
			@RequestParam(value = "textBackground", required = false) String textBackground,
			@RequestParam(value = "textPosition", required = false) String textPosition,
			@RequestParam(value = "audioMode", required = false) String audioMode,
			@RequestParam(value = "muteOriginalAudio", required = false) Boolean muteOriginalAudio,
			@RequestParam(value = "saveMode", required = false) String saveMode,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "segmentsJson", required = false) String segmentsJson,
			@RequestParam(value = "textLayersJson", required = false) String textLayersJson,
			@RequestParam(value = "music", required = false) MultipartFile music,
			@RequestParam(value = "textOverlay", required = false) MultipartFile textOverlay,
			@RequestParam(value = "textLayerOverlays", required = false) List<MultipartFile> textLayerOverlays) {
		highlightService.assertJobOwner(jobId, currentOwner());
		return highlightService.editSplitClip(jobId, clipIndex, editOptions(sourceType, startSeconds, endSeconds, rotationDegrees, videoZoom,
				outputWidth, outputHeight, overlayText, textXPercent, textYPercent, textSize, textColor, textFont, textBackground,
				textPosition, audioMode, muteOriginalAudio, saveMode, title, segmentsJson, textLayersJson), music, textOverlay, textLayerOverlays);
	}

	@GetMapping("/split-highlights/{jobId}/clips/{clipIndex}/download")
	public ResponseEntity<StreamingResponseBody> downloadSplitClip(@PathVariable String jobId, @PathVariable int clipIndex) throws IOException {
		highlightService.assertJobOwner(jobId, currentOwner());
		Path file = highlightService.splitClipDownloadPath(jobId, clipIndex);
		StreamingResponseBody body = outputStream -> {
			Files.copy(file, outputStream);
			outputStream.flush();
		};
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
				.contentLength(Files.size(file))
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(body);
	}

	@GetMapping("/split-highlights/{jobId}/clips/{clipIndex}/preview")
	public ResponseEntity<Resource> previewSplitClip(@PathVariable String jobId, @PathVariable int clipIndex) throws IOException {
		highlightService.assertJobOwner(jobId, currentOwner());
		Path file = highlightService.splitClipDownloadPath(jobId, clipIndex);
		return inlineVideo(file, file.getFileName().toString());
	}

	@PostMapping("/split-highlights/{jobId}/clips/{clipIndex}/open-location")
	public OpenLocationResult openSplitClipLocation(
			@PathVariable String jobId,
			@PathVariable int clipIndex,
			@RequestParam(value = "target", defaultValue = "output") String target) {
		highlightService.assertJobOwner(jobId, currentOwner());
		Path path = "source".equalsIgnoreCase(target)
				? highlightService.sourceDirectory(jobId)
				: highlightService.splitClipOutputPath(jobId, clipIndex);
		return openLocation(path);
	}

	@GetMapping("/highlights/{jobId}")
	public HighlightJobStatus highlightStatus(@PathVariable String jobId) {
		return highlightService.status(jobId, currentOwner());
	}

	@GetMapping("/highlights")
	public HighlightHistoryPage highlightHistory(
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		return highlightService.history(page, size, currentOwner());
	}

	@DeleteMapping("/highlights/{jobId}")
	public HighlightDeleteResult deleteHighlight(@PathVariable String jobId) {
		return highlightService.deleteHighlights(List.of(jobId), currentOwner());
	}

	@PostMapping("/highlights/delete")
	public HighlightDeleteResult deleteHighlights(@RequestBody HighlightDeleteRequest request) {
		return highlightService.deleteHighlights(request.getJobIds(), currentOwner());
	}

	@PostMapping(value = "/highlights/{jobId}/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public VideoEditResult editHighlight(
			@PathVariable String jobId,
			@RequestParam(value = "sourceType", required = false) String sourceType,
			@RequestParam(value = "startSeconds", required = false) Double startSeconds,
			@RequestParam(value = "endSeconds", required = false) Double endSeconds,
			@RequestParam(value = "rotationDegrees", required = false) Double rotationDegrees,
			@RequestParam(value = "videoZoom", required = false) Double videoZoom,
			@RequestParam(value = "outputWidth", required = false) Integer outputWidth,
			@RequestParam(value = "outputHeight", required = false) Integer outputHeight,
			@RequestParam(value = "overlayText", required = false) String overlayText,
			@RequestParam(value = "textXPercent", required = false) Double textXPercent,
			@RequestParam(value = "textYPercent", required = false) Double textYPercent,
			@RequestParam(value = "textSize", required = false) Integer textSize,
			@RequestParam(value = "textColor", required = false) String textColor,
			@RequestParam(value = "textFont", required = false) String textFont,
			@RequestParam(value = "textBackground", required = false) String textBackground,
			@RequestParam(value = "textPosition", required = false) String textPosition,
			@RequestParam(value = "audioMode", required = false) String audioMode,
			@RequestParam(value = "muteOriginalAudio", required = false) Boolean muteOriginalAudio,
			@RequestParam(value = "saveMode", required = false) String saveMode,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "segmentsJson", required = false) String segmentsJson,
			@RequestParam(value = "textLayersJson", required = false) String textLayersJson,
			@RequestParam(value = "music", required = false) MultipartFile music,
			@RequestParam(value = "textOverlay", required = false) MultipartFile textOverlay,
			@RequestParam(value = "textLayerOverlays", required = false) List<MultipartFile> textLayerOverlays) {
		highlightService.assertJobOwner(jobId, currentOwner());
		return highlightService.editHighlight(jobId, editOptions(sourceType, startSeconds, endSeconds, rotationDegrees, videoZoom,
				outputWidth, outputHeight, overlayText, textXPercent, textYPercent, textSize, textColor, textFont, textBackground,
				textPosition, audioMode, muteOriginalAudio, saveMode, title, segmentsJson, textLayersJson), music, textOverlay, textLayerOverlays);
	}

	@GetMapping("/highlights/{jobId}/download")
	public ResponseEntity<StreamingResponseBody> downloadHighlight(@PathVariable String jobId) throws IOException {
		highlightService.assertJobOwner(jobId, currentOwner());
		Path file = highlightService.downloadPath(jobId);
		StreamingResponseBody body = outputStream -> {
			try {
				Files.copy(file, outputStream);
				outputStream.flush();
			}
			finally {
				highlightService.cleanupJob(jobId);
			}
		};
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"highlight.mp4\"")
				.contentLength(Files.size(file))
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(body);
	}

	@GetMapping("/highlights/{jobId}/preview")
	public ResponseEntity<Resource> previewHighlight(@PathVariable String jobId) throws IOException {
		highlightService.assertJobOwner(jobId, currentOwner());
		Path file = highlightService.downloadPath(jobId);
		return inlineVideo(file, "highlight.mp4");
	}

	@PostMapping("/highlights/{jobId}/open-location")
	public OpenLocationResult openHighlightLocation(
			@PathVariable String jobId,
			@RequestParam(value = "target", defaultValue = "output") String target) {
		highlightService.assertJobOwner(jobId, currentOwner());
		Path path = "source".equalsIgnoreCase(target)
				? highlightService.sourceDirectory(jobId)
				: highlightService.highlightOutputPath(jobId);
		return openLocation(path);
	}

	private String currentOwner() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpSession session = attributes == null ? null : attributes.getRequest().getSession(false);
		if (AuthSession.admin(session)) {
			return "*";
		}
		String username = AuthSession.username(session);
		return username == null || username.isBlank() ? "local" : username;
	}
	private OpenLocationResult openLocation(Path path) {
		Path openedPath = localFileLocationService.open(path);
		return new OpenLocationResult(
				openedPath.toString(),
				Files.isDirectory(openedPath) ? "Đã mở thư mục lưu video." : "Đã mở vị trí file video.");
	}

	private ResponseEntity<Resource> inlineVideo(Path file, String fileName) throws IOException {
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
				.header(HttpHeaders.ACCEPT_RANGES, "bytes")
				.contentLength(Files.size(file))
				.contentType(MediaType.parseMediaType("video/mp4"))
				.body(new FileSystemResource(file));
	}

	private VideoEditOptions editOptions(String sourceType, Double startSeconds, Double endSeconds, Double rotationDegrees, Double videoZoom,
			Integer outputWidth, Integer outputHeight, String overlayText, Double textXPercent, Double textYPercent, Integer textSize,
			String textColor, String textFont, String textBackground, String textPosition, String audioMode, Boolean muteOriginalAudio,
			String saveMode, String title, String segmentsJson, String textLayersJson) {
		VideoEditOptions options = new VideoEditOptions();
		options.setSourceType(sourceType);
		options.setStartSeconds(startSeconds);
		options.setEndSeconds(endSeconds);
		options.setRotationDegrees(rotationDegrees);
		options.setVideoZoom(videoZoom);
		options.setOutputWidth(outputWidth);
		options.setOutputHeight(outputHeight);
		options.setOverlayText(overlayText);
		options.setTextXPercent(textXPercent);
		options.setTextYPercent(textYPercent);
		options.setTextSize(textSize);
		options.setTextColor(textColor);
		options.setTextFont(textFont);
		options.setTextBackground(textBackground);
		options.setTextPosition(textPosition);
		options.setAudioMode(audioMode);
		options.setMuteOriginalAudio(muteOriginalAudio);
		options.setSaveMode(saveMode);
		options.setTitle(title);
		options.setSegmentsJson(segmentsJson);
		options.setTextLayersJson(textLayersJson);
		return options;
	}

	@ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
	public ResponseEntity<ApiError> handleBadRequest(RuntimeException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(ex.getMessage()));
	}

	public static class ApiError {
		private final String error;

		public ApiError(String error) {
			this.error = error;
		}

		public String getError() {
			return error;
		}
	}

	public static class OpenLocationResult {
		private final String path;
		private final String message;

		public OpenLocationResult(String path, String message) {
			this.path = path;
			this.message = message;
		}

		public String getPath() {
			return path;
		}

		public String getMessage() {
			return message;
		}
	}
}
