package com.example.tool.service;

import com.example.tool.model.DownloadRequest;
import com.example.tool.model.DownloadResult;
import com.example.tool.model.SourceCandidate;
import com.example.tool.model.SourceSearchRequest;
import com.example.tool.model.SourceSearchResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SourceVideoService {

	private static final List<String> VIDEO_EXTENSIONS = List.of(".mp4", ".mov", ".m4v", ".webm", ".mkv");

	private final MediaWorkspace workspace;
	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;
	private final String pexelsApiKey;
	private final String pixabayApiKey;

	public SourceVideoService(
			MediaWorkspace workspace,
			ObjectMapper objectMapper,
			@Value("${app.sources.pexels-api-key:}") String pexelsApiKey,
			@Value("${app.sources.pixabay-api-key:}") String pixabayApiKey) {
		this.workspace = workspace;
		this.objectMapper = objectMapper;
		this.pexelsApiKey = pexelsApiKey;
		this.pixabayApiKey = pixabayApiKey;
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(20))
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build();
	}

	public SourceSearchResult search(SourceSearchRequest rawRequest) {
		SourceSearchRequest request = rawRequest.withDefaults();
		if ("pexels".equals(request.provider())) {
			return searchPexels(request);
		}
		if ("pixabay".equals(request.provider())) {
			return searchPixabay(request);
		}
		return searchLocal(request);
	}

	public DownloadResult download(DownloadRequest request) {
		if (request == null || request.downloadUrl() == null || request.downloadUrl().isBlank()) {
			throw new IllegalArgumentException("downloadUrl is required.");
		}
		URI uri = URI.create(request.downloadUrl());
		String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
		if (!host.contains("pexels.com") && !host.contains("pixabay.com") && !host.contains("videos.pexels.com")) {
			throw new IllegalArgumentException("Only Pexels/Pixabay download URLs are accepted by this endpoint.");
		}
		workspace.ensureBaseDirectories();
		String fileName = sanitizeFileName(request.fileName());
		if (fileName == null || fileName.isBlank()) {
			fileName = "source-" + System.currentTimeMillis() + ".mp4";
		}
		Path output = workspace.sourcesDirectory().resolve(fileName).normalize();
		try {
			HttpRequest httpRequest = HttpRequest.newBuilder(uri)
					.timeout(Duration.ofMinutes(3))
					.GET()
					.build();
			HttpResponse<Path> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofFile(output));
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				Files.deleteIfExists(output);
				throw new IllegalStateException("Download failed with HTTP " + response.statusCode());
			}
			return new DownloadResult(output.toString(), Files.size(output));
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not download source video.", ex);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Download interrupted.", ex);
		}
	}

	private SourceSearchResult searchLocal(SourceSearchRequest request) {
		workspace.ensureBaseDirectories();
		List<SourceCandidate> candidates = new ArrayList<>();
		try (var stream = Files.list(workspace.sourcesDirectory())) {
			stream.filter(Files::isRegularFile)
					.filter(this::isVideo)
					.limit(request.limit())
					.forEach(path -> candidates.add(new SourceCandidate(
							"local",
							path.getFileName().toString(),
							"user-provided",
							null,
							path.toUri().toString(),
							path.toAbsolutePath().toString(),
							null,
							null,
							null)));
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not list local source videos.", ex);
		}
		return new SourceSearchResult(
				"local",
				request.topic(),
				candidates,
				List.of("Local mode reads files from " + workspace.sourcesDirectory()));
	}

	private SourceSearchResult searchPexels(SourceSearchRequest request) {
		if (pexelsApiKey == null || pexelsApiKey.isBlank()) {
			return new SourceSearchResult("pexels", request.topic(), List.of(),
					List.of("Set PEXELS_API_KEY or app.sources.pexels-api-key to enable Pexels search."));
		}
		String query = urlEncode(request.topic());
		URI uri = URI.create("https://api.pexels.com/videos/search?query=" + query + "&per_page=" + request.limit());
		HttpRequest httpRequest = HttpRequest.newBuilder(uri)
				.timeout(Duration.ofSeconds(30))
				.header("Authorization", pexelsApiKey)
				.GET()
				.build();
		try {
			HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				return new SourceSearchResult("pexels", request.topic(), List.of(),
						List.of("Pexels search failed with HTTP " + response.statusCode()));
			}
			JsonNode root = objectMapper.readTree(response.body());
			List<SourceCandidate> candidates = new ArrayList<>();
			for (JsonNode video : root.path("videos")) {
				JsonNode bestFile = bestPexelsFile(video.path("video_files"));
				if (bestFile == null || bestFile.path("link").asText().isBlank()) {
					continue;
				}
				JsonNode user = video.path("user");
				candidates.add(new SourceCandidate(
						"pexels",
						"pexels-" + video.path("id").asText(),
						"Pexels License",
						user.path("name").asText(null),
						video.path("url").asText(null),
						bestFile.path("link").asText(),
						bestFile.path("width").isNumber() ? bestFile.path("width").asInt() : null,
						bestFile.path("height").isNumber() ? bestFile.path("height").asInt() : null,
						video.path("duration").isNumber() ? video.path("duration").asDouble() : null));
			}
			return new SourceSearchResult("pexels", request.topic(), candidates,
					List.of("Review the Pexels license and attribution needs before publishing."));
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not search Pexels.", ex);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Pexels search interrupted.", ex);
		}
	}

	private SourceSearchResult searchPixabay(SourceSearchRequest request) {
		if (pixabayApiKey == null || pixabayApiKey.isBlank()) {
			return new SourceSearchResult("pixabay", request.topic(), List.of(),
					List.of("Set PIXABAY_API_KEY or app.sources.pixabay-api-key to enable Pixabay search."));
		}
		String query = urlEncode(request.topic());
		URI uri = URI.create("https://pixabay.com/api/videos/?key=" + pixabayApiKey + "&q=" + query + "&per_page=" + request.limit());
		HttpRequest httpRequest = HttpRequest.newBuilder(uri)
				.timeout(Duration.ofSeconds(30))
				.GET()
				.build();
		try {
			HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				return new SourceSearchResult("pixabay", request.topic(), List.of(),
						List.of("Pixabay search failed with HTTP " + response.statusCode()));
			}
			JsonNode root = objectMapper.readTree(response.body());
			List<SourceCandidate> candidates = new ArrayList<>();
			for (JsonNode hit : root.path("hits")) {
				JsonNode file = bestPixabayFile(hit.path("videos"));
				if (file == null || file.path("url").asText().isBlank()) {
					continue;
				}
				candidates.add(new SourceCandidate(
						"pixabay",
						"pixabay-" + hit.path("id").asText(),
						"Pixabay Content License",
						hit.path("user").asText(null),
						hit.path("pageURL").asText(null),
						file.path("url").asText(),
						file.path("width").isNumber() ? file.path("width").asInt() : null,
						file.path("height").isNumber() ? file.path("height").asInt() : null,
						hit.path("duration").isNumber() ? hit.path("duration").asDouble() : null));
			}
			return new SourceSearchResult("pixabay", request.topic(), candidates,
					List.of("Review the Pixabay license and platform rules before publishing."));
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not search Pixabay.", ex);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Pixabay search interrupted.", ex);
		}
	}

	private JsonNode bestPexelsFile(JsonNode files) {
		JsonNode best = null;
		int bestScore = -1;
		for (JsonNode file : files) {
			int width = file.path("width").asInt(0);
			int height = file.path("height").asInt(0);
			int score = width * height;
			if (height >= width) {
				score += 10_000_000;
			}
			if (score > bestScore) {
				best = file;
				bestScore = score;
			}
		}
		return best;
	}

	private JsonNode bestPixabayFile(JsonNode videos) {
		for (String key : List.of("large", "medium", "small", "tiny")) {
			JsonNode node = videos.path(key);
			if (!node.isMissingNode() && !node.path("url").asText().isBlank()) {
				return node;
			}
		}
		return null;
	}

	private boolean isVideo(Path path) {
		String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
		return VIDEO_EXTENSIONS.stream().anyMatch(name::endsWith);
	}

	private String sanitizeFileName(String value) {
		if (value == null) {
			return null;
		}
		String cleaned = value.replaceAll("[^a-zA-Z0-9._-]", "-");
		if (!cleaned.toLowerCase(Locale.ROOT).endsWith(".mp4")) {
			cleaned += ".mp4";
		}
		return cleaned;
	}

	private String urlEncode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
