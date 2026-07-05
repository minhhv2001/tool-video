package com.example.tool.service;

import com.example.tool.config.MediaToolProperties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

	public List<Path> downloadPlaylistBestUpTo2k(String url, Path outputDirectory, int startIndex, int endIndex, String cookiesFilePath) {
		String safeUrl = validateUrl(url);
		int safeStart = Math.max(1, startIndex);
		int safeEnd = Math.max(safeStart, endIndex);
		try {
			Files.createDirectories(outputDirectory);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Không tạo được thư mục tải video từ Facebook.", ex);
		}

		String baseName = "facebook-batch";
		String playlistItems = safeStart + "-" + safeEnd;
		LOGGER.info("Bắt đầu tải hàng loạt Facebook bằng yt-dlp: {}, playlistItems={}", safeUrl, playlistItems);
		try {
			DownloadAttempt attempt = runPlaylistDownloadCommand(safeUrl, outputDirectory, baseName, playlistItems, null);
			Path cookiesFile = resolveCookiesFile(cookiesFilePath);
			if (!attempt.success() && cookiesFile != null && shouldRetryWithCookies(attempt.output())) {
				LOGGER.info("Thử tải hàng loạt Facebook lại bằng cookies file: {}", cookiesFile);
				attempt = runPlaylistDownloadCommandWithCookiesFile(safeUrl, outputDirectory, baseName, playlistItems, cookiesFile);
			}
			List<String> browsers = cookieBrowsers();
			if (!attempt.success() && !browsers.isEmpty() && shouldRetryWithCookies(attempt.output())) {
				for (String browser : browsers) {
					DownloadAttempt cookieAttempt = runPlaylistDownloadCommand(safeUrl, outputDirectory, baseName, playlistItems, browser);
					if (cookieAttempt.success()) {
						List<Path> downloaded = findDownloadedFiles(outputDirectory, baseName);
						LOGGER.info("Tải hàng loạt Facebook hoàn tất bằng cookie {}: {} file", browser, downloaded.size());
						return downloaded;
					}
					attempt = cookieAttempt;
				}
			}
			if (!attempt.success()) {
				if (isFacebookReelsPage(safeUrl) && unsupportedUrl(attempt.output())) {
					LOGGER.info("yt-dlp không hỗ trợ trực tiếp trang reels. Chuyển sang bóc link reel con từ HTML Facebook.");
					return downloadFacebookReelsPageByScraping(safeUrl, outputDirectory, safeStart, safeEnd, cookiesFilePath);
				}
				LOGGER.warn("yt-dlp tải hàng loạt Facebook thất bại. Output: {}", attempt.output());
				throw new IllegalStateException(downloadErrorMessage(safeUrl, attempt.output()));
			}
			List<Path> downloaded = findDownloadedFiles(outputDirectory, baseName);
			LOGGER.info("Tải hàng loạt Facebook hoàn tất: {} file", downloaded.size());
			return downloaded;
		}
		catch (IOException ex) {
			throw new IllegalStateException("Không chạy được yt-dlp. Hãy cài yt-dlp hoặc cấu hình app.media.yt-dlp-path.", ex);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Tải hàng loạt Facebook bị gián đoạn.", ex);
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

	private DownloadAttempt runPlaylistDownloadCommand(String url, Path outputDirectory, String baseName, String playlistItems, String cookiesFromBrowser)
			throws IOException, InterruptedException {
		List<String> command = playlistDownloadCommand(url, outputDirectory, baseName, playlistItems);
		if (cookiesFromBrowser != null && !cookiesFromBrowser.isBlank()) {
			int insertAt = command.size() - 1;
			command.add(insertAt, "--cookies-from-browser");
			command.add(insertAt + 1, cookiesFromBrowser.trim());
		}
		String logSuffix = cookiesFromBrowser == null || cookiesFromBrowser.isBlank() ? "" : "." + safeLogName(cookiesFromBrowser);
		return runCommand(command, outputDirectory.resolve(baseName + logSuffix + ".download.log"));
	}

	private DownloadAttempt runPlaylistDownloadCommandWithCookiesFile(String url, Path outputDirectory, String baseName, String playlistItems, Path cookiesFile)
			throws IOException, InterruptedException {
		List<String> command = playlistDownloadCommand(url, outputDirectory, baseName, playlistItems);
		int insertAt = command.size() - 1;
		command.add(insertAt, "--cookies");
		command.add(insertAt + 1, cookiesFile.toString());
		return runCommand(command, outputDirectory.resolve(baseName + ".cookies-file.download.log"));
	}

	private List<String> playlistDownloadCommand(String url, Path outputDirectory, String baseName, String playlistItems) {
		List<String> command = new ArrayList<>();
		command.add(ytDlpPath());
		command.add("--yes-playlist");
		command.add("--playlist-items");
		command.add(playlistItems);
		command.add("--merge-output-format");
		command.add("mp4");
		command.add("--format");
		command.add(BEST_UP_TO_2K_FORMAT);
		command.add("--output");
		command.add(outputDirectory.resolve(baseName + "-%(playlist_index)03d-%(title).80s.%(ext)s").toString());
		addFfmpegLocation(command);
		command.add(url);
		return command;
	}

	private DownloadAttempt runCommand(List<String> command, Path logFile) throws IOException, InterruptedException {
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.redirectErrorStream(true);
		builder.redirectOutput(logFile.toFile());
		Process process = builder.start();
		boolean finished = process.waitFor(DOWNLOAD_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
		String output = Files.isRegularFile(logFile) ? Files.readString(logFile, StandardCharsets.UTF_8) : "";
		if (!finished) {
			process.destroyForcibly();
			return new DownloadAttempt(false, "Tải video quá lâu nên đã dừng. Hãy thử khoảng ít video hơn.\n" + output);
		}
		return new DownloadAttempt(process.exitValue() == 0, output);
	}

	private List<Path> downloadFacebookReelsPageByScraping(String url, Path outputDirectory, int startIndex, int endIndex, String cookiesFilePath)
			throws IOException, InterruptedException {
		List<String> reelUrls = extractFacebookReelUrlsWithCookies(url, cookiesFilePath);
		if (reelUrls.isEmpty()) {
			throw new IllegalStateException("Facebook không trả danh sách reel cho server từ link /reels/ này. Hãy dán nhiều link reel cụ thể, hoặc copy HTML vùng danh sách reels có href=\"/reel/...\" rồi dán vào ô link batch.");
		}
		if (reelUrls.isEmpty()) {
			throw new IllegalStateException("Không tìm thấy link reel con trong trang Facebook này. Nếu trang cần đăng nhập, hãy dùng cookies.txt hoặc dán từng link reel cụ thể.");
		}
		int from = Math.max(0, startIndex - 1);
		int to = Math.min(reelUrls.size(), endIndex);
		if (from >= reelUrls.size() || from >= to) {
			throw new IllegalStateException("Trang chỉ tìm thấy " + reelUrls.size() + " reel, không đủ tới khoảng " + startIndex + "-" + endIndex + ".");
		}
		List<String> selectedUrls = reelUrls.subList(from, to);
		LOGGER.info("Đã bóc được {} reel từ trang Facebook, tải khoảng {}-{} tương ứng {} video.", reelUrls.size(), startIndex, endIndex, selectedUrls.size());
		List<Path> downloaded = new ArrayList<>();
		for (int i = 0; i < selectedUrls.size(); i++) {
			downloaded.add(downloadBestUpTo2k(selectedUrls.get(i), outputDirectory, i + 1, cookiesFilePath));
		}
		return downloaded;
	}

	private List<String> extractFacebookReelUrlsWithCookies(String url, String cookiesFilePath) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(20))
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build();
		String cookieHeader = facebookCookieHeader(cookiesFilePath);
		int lastStatus = 0;
		for (String candidate : facebookReelsCandidates(url)) {
			HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(candidate))
					.timeout(Duration.ofSeconds(30))
					.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36")
					.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
					.header("Accept-Language", "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7")
					.header("Cache-Control", "no-cache")
					.GET();
			if (!cookieHeader.isBlank()) {
				builder.header("Cookie", cookieHeader);
			}
			HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			lastStatus = response.statusCode();
			if (response.statusCode() >= 400) {
				LOGGER.warn("Facebook trả HTTP {} khi đọc {}, thử URL khác.", response.statusCode(), candidate);
				continue;
			}
			List<String> urls = parseFacebookReelUrls(response.body());
			if (!urls.isEmpty()) {
				LOGGER.info("Đọc trang Facebook reels thành công từ {}, tìm thấy {} reel.", candidate, urls.size());
				return urls;
			}
		}
		if (lastStatus >= 400) {
			throw new IllegalStateException("Facebook trả lỗi HTTP " + lastStatus + " khi đọc trang reels. Hãy kiểm tra cookies.txt hoặc dán từng link reel cụ thể.");
		}
		return List.of();
	}

	private List<String> extractFacebookReelUrls(String url, String cookiesFilePath) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(20))
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build();
		HttpRequest request = HttpRequest.newBuilder(URI.create(facebookBasicReelsUrl(url)))
				.timeout(Duration.ofSeconds(30))
				.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36")
				.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
				.header("Accept-Language", "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7")
				.GET()
				.build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		if (response.statusCode() >= 400) {
			throw new IllegalStateException("Facebook trả lỗi HTTP " + response.statusCode() + " khi đọc trang reels.");
		}
		String html = response.body() == null ? "" : response.body()
				.replace("\\/", "/")
				.replace("\\u0025", "%")
				.replace("&amp;", "&")
				.replace("\\\"", "\"");
		Set<String> ids = new LinkedHashSet<>();
		addMatches(ids, html, Pattern.compile("facebook\\.com/(?:[^\"'<>\\s]+/)?reel/(\\d+)", Pattern.CASE_INSENSITIVE));
		addMatches(ids, html, Pattern.compile("/(?:[^\"'<>\\s]+/)?reel/(\\d+)", Pattern.CASE_INSENSITIVE));
		addMatches(ids, html, Pattern.compile("\"reel_id\"\\s*:\\s*\"?(\\d+)\"?", Pattern.CASE_INSENSITIVE));
		addMatches(ids, html, Pattern.compile("\"video_id\"\\s*:\\s*\"?(\\d+)\"?", Pattern.CASE_INSENSITIVE));
		return ids.stream()
				.map(id -> "https://www.facebook.com/reel/" + id + "/")
				.collect(Collectors.toList());
	}

	private String facebookBasicReelsUrl(String url) {
		URI uri = URI.create(url);
		String path = uri.getPath() == null || uri.getPath().isBlank() ? "/reels/" : uri.getPath();
		if (!path.endsWith("/")) {
			path += "/";
		}
		return "https://mbasic.facebook.com" + path;
	}

	private List<String> facebookReelsCandidates(String url) {
		URI uri = URI.create(url);
		String path = uri.getPath() == null || uri.getPath().isBlank() ? "/reels/" : uri.getPath();
		if (!path.endsWith("/")) {
			path += "/";
		}
		return List.of(
				"https://www.facebook.com" + path,
				"https://m.facebook.com" + path,
				"https://mbasic.facebook.com" + path);
	}

	private List<String> parseFacebookReelUrls(String body) {
		String html = body == null ? "" : body
				.replace("\\/", "/")
				.replace("\\u002F", "/")
				.replace("\\u0025", "%")
				.replace("&amp;", "&")
				.replace("\\\"", "\"");
		Set<String> ids = new LinkedHashSet<>();
		addMatches(ids, html, Pattern.compile("facebook\\.com/(?:[^\"'<>\\s]+/)?reel/(\\d+)", Pattern.CASE_INSENSITIVE));
		addMatches(ids, html, Pattern.compile("/(?:[^\"'<>\\s]+/)?reel/(\\d+)", Pattern.CASE_INSENSITIVE));
		addMatches(ids, html, Pattern.compile("\"reel_id\"\\s*:\\s*\"?(\\d+)\"?", Pattern.CASE_INSENSITIVE));
		addMatches(ids, html, Pattern.compile("\"video_id\"\\s*:\\s*\"?(\\d+)\"?", Pattern.CASE_INSENSITIVE));
		return ids.stream()
				.map(id -> "https://www.facebook.com/reel/" + id + "/")
				.collect(Collectors.toList());
	}

	private String facebookCookieHeader(String cookiesFilePath) {
		Path cookiesFile = resolveCookiesFile(cookiesFilePath);
		if (cookiesFile == null) {
			return "";
		}
		try {
			Map<String, String> cookies = new LinkedHashMap<>();
			for (String line : Files.readAllLines(cookiesFile, StandardCharsets.UTF_8)) {
				String trimmed = line.trim();
				if (trimmed.isBlank() || (trimmed.startsWith("#") && !trimmed.startsWith("#HttpOnly_"))) {
					continue;
				}
				if (trimmed.startsWith("#HttpOnly_")) {
					trimmed = trimmed.substring("#HttpOnly_".length());
				}
				String[] parts = trimmed.split("\\t");
				if (parts.length >= 7 && parts[0].contains("facebook.com")) {
					cookies.put(parts[5], parts[6]);
				}
			}
			LOGGER.info("Đã đọc {} cookie Facebook từ file cookies.txt.", cookies.size());
			return cookies.entrySet().stream()
					.map(entry -> entry.getKey() + "=" + entry.getValue())
					.collect(Collectors.joining("; "));
		}
		catch (IOException ex) {
			throw new IllegalStateException("Không đọc được file cookies.txt Facebook: " + cookiesFile, ex);
		}
	}

	private void addMatches(Set<String> ids, String html, Pattern pattern) {
		Matcher matcher = pattern.matcher(html);
		while (matcher.find() && ids.size() < 300) {
			String id = matcher.group(1);
			if (id != null && id.matches("\\d{5,}")) {
				ids.add(id);
			}
		}
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
		return (normalizedHost.contains("instagram.com") || normalizedHost.contains("facebook.com"))
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

	private boolean unsupportedUrl(String output) {
		return output != null && output.toLowerCase(Locale.ROOT).contains("unsupported url");
	}

	private boolean isFacebookReelsPage(String url) {
		URI uri = URI.create(url);
		String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
		String path = uri.getPath() == null ? "" : uri.getPath().toLowerCase(Locale.ROOT);
		return host.contains("facebook.com") && (path.endsWith("/reels/") || path.endsWith("/reels"));
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

	private List<Path> findDownloadedFiles(Path outputDirectory, String baseName) {
		try (var stream = Files.list(outputDirectory)) {
			List<Path> matches = stream
					.filter(Files::isRegularFile)
					.filter(path -> path.getFileName().toString().startsWith(baseName + "-"))
					.filter(this::isVideoFile)
					.sorted(Comparator.comparing(path -> path.getFileName().toString()))
					.collect(Collectors.toList());
			if (!matches.isEmpty()) {
				return matches;
			}
		}
		catch (IOException ex) {
			throw new IllegalStateException("Không đọc được file video vừa tải.", ex);
		}
		throw new IllegalStateException("Không tìm thấy file video nào sau khi tải hàng loạt Facebook.");
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
