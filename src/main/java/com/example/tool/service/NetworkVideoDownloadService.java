package com.example.tool.service;

import com.example.tool.config.MediaToolProperties;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class NetworkVideoDownloadService {

	private static final Logger LOGGER = LogManager.getLogger(NetworkVideoDownloadService.class);
	private static final List<String> VIDEO_EXTENSIONS = List.of(".mp4", ".mkv", ".webm", ".mov", ".m4v");
	private static final Duration DOWNLOAD_TIMEOUT = Duration.ofMinutes(20);
	private static final String BEST_UP_TO_2K_FORMAT =
			"bestvideo[height<=1440][ext=mp4]+bestaudio[ext=m4a]/bestvideo[height<=1440]+bestaudio/best[height<=1440]/best[height<=1440][ext=mp4]";

	private final MediaToolProperties properties;

	public NetworkVideoDownloadService(MediaToolProperties properties) {
		this.properties = properties;
	}

	public Path downloadBestUpTo2k(String url, Path outputDirectory, int index) {
		return downloadBestUpTo2k(url, outputDirectory, index, null);
	}

	public Path downloadBestUpTo2k(String url, Path outputDirectory, int index, String cookiesFilePath) {
		String safeUrl = validateUrl(url);
		try {
			Files.createDirectories(outputDirectory);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Không tạo được thư mục tải video từ mạng.", ex);
		}

		String baseName = String.format(Locale.ROOT, "network-%02d", index);
		LOGGER.info("Bắt đầu tải video từ mạng bằng yt-dlp: {}", safeUrl);
		try {
			DownloadAttempt attempt = runDownloadCommand(safeUrl, outputDirectory, baseName, null);
			Path cookiesFile = resolveCookiesFile(cookiesFilePath);
			if (!attempt.success() && cookiesFile != null && shouldRetryWithCookies(attempt.output())) {
				LOGGER.info("Thử tải lại bằng cookies file: {}", cookiesFile);
				attempt = runDownloadCommandWithCookiesFile(safeUrl, outputDirectory, baseName, cookiesFile);
			}
			List<String> browsers = cookieBrowsers();
			if (!attempt.success() && !browsers.isEmpty() && shouldRetryWithBrowserCookies(safeUrl, attempt.output())) {
				LOGGER.info("Link có thể cần cookie đăng nhập. Thử tải lại bằng cookie trình duyệt.");
				for (String browser : browsers) {
					DownloadAttempt cookieAttempt = runDownloadCommand(safeUrl, outputDirectory, baseName, browser);
					if (cookieAttempt.success()) {
						Path downloaded = findDownloadedFile(outputDirectory, baseName);
						LOGGER.info("Tải video từ mạng hoàn tất bằng cookie {}: {}", browser, downloaded);
						return downloaded;
					}
					attempt = cookieAttempt;
				}
			}
			if (!attempt.success()) {
				LOGGER.warn("yt-dlp tải video thất bại. Output: {}", attempt.output());
				throw new IllegalStateException(downloadErrorMessage(safeUrl, attempt.output()));
			}
			Path downloaded = findDownloadedFile(outputDirectory, baseName);
			LOGGER.info("Tải video từ mạng hoàn tất: {}", downloaded);
			return downloaded;
		}
		catch (IOException ex) {
			throw new IllegalStateException("Không chạy được yt-dlp. Hãy cài yt-dlp hoặc cấu hình app.media.yt-dlp-path.", ex);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Tải video từ mạng bị gián đoạn.", ex);
		}
	}

	private DownloadAttempt runDownloadCommand(String url, Path outputDirectory, String baseName, String cookiesFromBrowser)
			throws IOException, InterruptedException {
		List<String> command = new ArrayList<>();
		command.add(ytDlpPath());
		command.add("--no-playlist");
		command.add("--merge-output-format");
		command.add("mp4");
		command.add("--format");
		command.add(BEST_UP_TO_2K_FORMAT);
		if (cookiesFromBrowser != null && !cookiesFromBrowser.isBlank()) {
			command.add("--cookies-from-browser");
			command.add(cookiesFromBrowser.trim());
		}
		command.add("--output");
		command.add(outputDirectory.resolve(baseName + ".%(ext)s").toString());
		addFfmpegLocation(command);
		command.add(url);

		String logSuffix = cookiesFromBrowser == null || cookiesFromBrowser.isBlank() ? "" : "." + safeLogName(cookiesFromBrowser);
		Path logFile = outputDirectory.resolve(baseName + logSuffix + ".download.log");
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.redirectErrorStream(true);
		builder.redirectOutput(logFile.toFile());
		Process process = builder.start();
		boolean finished = process.waitFor(DOWNLOAD_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
		String output = Files.isRegularFile(logFile) ? Files.readString(logFile, StandardCharsets.UTF_8) : "";
		if (!finished) {
			process.destroyForcibly();
			return new DownloadAttempt(false, "Tải video quá lâu nên đã dừng. Hãy thử link khác hoặc video ngắn hơn.\n" + output);
		}
		return new DownloadAttempt(process.exitValue() == 0, output);
	}

	private DownloadAttempt runDownloadCommandWithCookiesFile(String url, Path outputDirectory, String baseName, Path cookiesFile)
			throws IOException, InterruptedException {
		List<String> command = new ArrayList<>();
		command.add(ytDlpPath());
		command.add("--no-playlist");
		command.add("--merge-output-format");
		command.add("mp4");
		command.add("--format");
		command.add(BEST_UP_TO_2K_FORMAT);
		command.add("--cookies");
		command.add(cookiesFile.toString());
		command.add("--output");
		command.add(outputDirectory.resolve(baseName + ".%(ext)s").toString());
		addFfmpegLocation(command);
		command.add(url);

		Path logFile = outputDirectory.resolve(baseName + ".cookies-file.download.log");
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.redirectErrorStream(true);
		builder.redirectOutput(logFile.toFile());
		Process process = builder.start();
		boolean finished = process.waitFor(DOWNLOAD_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
		String output = Files.isRegularFile(logFile) ? Files.readString(logFile, StandardCharsets.UTF_8) : "";
		if (!finished) {
			process.destroyForcibly();
			return new DownloadAttempt(false, "Tải video quá lâu nên đã dừng. Hãy thử link khác hoặc video ngắn hơn.\n" + output);
		}
		return new DownloadAttempt(process.exitValue() == 0, output);
	}

	private String validateUrl(String url) {
		if (url == null || url.isBlank()) {
			throw new IllegalArgumentException("Hãy nhập link video.");
		}
		URI uri = URI.create(url.trim());
		String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
		if (!"http".equals(scheme) && !"https".equals(scheme)) {
			throw new IllegalArgumentException("Link video phải bắt đầu bằng http hoặc https.");
		}
		return uri.toString();
	}

	private boolean shouldRetryWithBrowserCookies(String url, String output) {
		String host = URI.create(url).getHost();
		String normalizedHost = host == null ? "" : host.toLowerCase(Locale.ROOT);
		String normalizedOutput = output == null ? "" : output.toLowerCase(Locale.ROOT);
		return normalizedHost.contains("instagram.com")
				&& (normalizedOutput.contains("cookies-from-browser")
				|| normalizedOutput.contains("empty media response")
				|| normalizedOutput.contains("login")
				|| normalizedOutput.contains("not logged in")
				|| normalizedOutput.contains("authentication"));
	}

	private boolean shouldRetryWithCookies(String output) {
		String normalizedOutput = output == null ? "" : output.toLowerCase(Locale.ROOT);
		return normalizedOutput.contains("cookies-from-browser")
				|| normalizedOutput.contains("empty media response")
				|| normalizedOutput.contains("login")
				|| normalizedOutput.contains("not logged in")
				|| normalizedOutput.contains("authentication")
				|| normalizedOutput.contains("dpapi");
	}

	private String downloadErrorMessage(String url, String output) {
		String host = URI.create(url).getHost();
		String normalizedHost = host == null ? "" : host.toLowerCase(Locale.ROOT);
		String normalizedOutput = output == null ? "" : output.toLowerCase(Locale.ROOT);
		if (normalizedOutput.contains("dpapi")) {
			return "Không đọc được cookie Chrome/Edge vì lỗi DPAPI của Windows. Hãy export cookie Instagram ra file cookies.txt rồi nhập đường dẫn file đó trong ô Cookies.txt của tool.";
		}
		if (normalizedHost.contains("instagram.com")
				&& (normalizedOutput.contains("empty media response") || normalizedOutput.contains("cookies-from-browser"))) {
			return "Instagram cần cookie đăng nhập nên tool không thể tải link này ở chế độ công khai. Hãy export cookie Instagram ra file cookies.txt rồi nhập đường dẫn file đó trong ô Cookies.txt của tool.";
		}
		String detail = lastInterestingLine(output);
		if (!detail.isBlank()) {
			return "Không tải được video từ link. Chi tiết yt-dlp: " + detail;
		}
		return "Không tải được video từ link. Hãy kiểm tra link, quyền truy cập, hoặc cấu hình yt-dlp.";
	}

	private String lastInterestingLine(String output) {
		if (output == null || output.isBlank()) {
			return "";
		}
		String[] lines = output.replace("\r", "").split("\n");
		for (int i = lines.length - 1; i >= 0; i--) {
			String line = lines[i].trim();
			if (!line.isBlank()) {
				return line.length() > 260 ? line.substring(0, 260) + "..." : line;
			}
		}
		return "";
	}

	private List<String> cookieBrowsers() {
		String raw = properties.getYtDlpCookiesFromBrowser();
		if (raw == null || raw.isBlank()) {
			return List.of();
		}
		return List.of(raw.split(",")).stream()
				.map(String::trim)
				.filter(value -> !value.isBlank())
				.distinct()
				.collect(Collectors.toList());
	}

	private Path resolveCookiesFile(String cookiesFilePath) {
		String configured = cookiesFilePath != null && !cookiesFilePath.isBlank()
				? cookiesFilePath
				: properties.getYtDlpCookiesFile();
		if (configured == null || configured.isBlank()) {
			return null;
		}
		Path path = Path.of(configured.trim()).toAbsolutePath().normalize();
		if (!Files.isRegularFile(path)) {
			throw new IllegalStateException("Không tìm thấy file cookies.txt: " + path);
		}
		return path;
	}

	private Path findDownloadedFile(Path outputDirectory, String baseName) {
		try (var stream = Files.list(outputDirectory)) {
			List<Path> matches = stream
					.filter(Files::isRegularFile)
					.filter(path -> path.getFileName().toString().startsWith(baseName + "."))
					.filter(this::isVideoFile)
					.sorted(Comparator.comparing(path -> path.getFileName().toString()))
					.collect(Collectors.toList());
			if (!matches.isEmpty()) {
				return matches.get(0);
			}
		}
		catch (IOException ex) {
			throw new IllegalStateException("Không đọc được file video vừa tải.", ex);
		}
		throw new IllegalStateException("Không tìm thấy file video sau khi tải từ mạng.");
	}

	private boolean isVideoFile(Path path) {
		String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
		return VIDEO_EXTENSIONS.stream().anyMatch(name::endsWith);
	}

	private String ytDlpPath() {
		String path = properties.getYtDlpPath();
		return path == null || path.isBlank() ? "yt-dlp" : path;
	}

	private String safeLogName(String value) {
		return value.replaceAll("[^a-zA-Z0-9._-]", "-");
	}

	private void addFfmpegLocation(List<String> command) {
		String ffmpegPath = properties.getFfmpegPath();
		if (ffmpegPath == null || ffmpegPath.isBlank() || "ffmpeg".equals(ffmpegPath)) {
			return;
		}
		Path path = Path.of(ffmpegPath);
		Path location = Files.isDirectory(path) ? path : path.getParent();
		if (location != null) {
			command.add("--ffmpeg-location");
			command.add(location.toString());
		}
	}

	private static class DownloadAttempt {
		private final boolean success;
		private final String output;

		DownloadAttempt(boolean success, String output) {
			this.success = success;
			this.output = output == null ? "" : output;
		}

		boolean success() {
			return success;
		}

		String output() {
			return output;
		}
	}
}
