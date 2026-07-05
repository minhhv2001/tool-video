package com.example.tool.config;

import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.media")
public class MediaToolProperties {

	private String workspace = "./media";
	private String ffmpegPath = "ffmpeg";
	private String ffprobePath = "ffprobe";
	private String ytDlpPath = "yt-dlp";
	private String ytDlpCookiesFromBrowser = "";
	private String ytDlpCookiesFile = "";

	public String getWorkspace() {
		return workspace;
	}

	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}

	public String getFfmpegPath() {
		return resolveExecutable(ffmpegPath);
	}

	public void setFfmpegPath(String ffmpegPath) {
		this.ffmpegPath = ffmpegPath;
	}

	public String getFfprobePath() {
		return resolveExecutable(ffprobePath);
	}

	public void setFfprobePath(String ffprobePath) {
		this.ffprobePath = ffprobePath;
	}

	public String getYtDlpPath() {
		return resolveExecutable(ytDlpPath);
	}

	public void setYtDlpPath(String ytDlpPath) {
		this.ytDlpPath = ytDlpPath;
	}

	public String getYtDlpCookiesFromBrowser() {
		return ytDlpCookiesFromBrowser;
	}

	public void setYtDlpCookiesFromBrowser(String ytDlpCookiesFromBrowser) {
		this.ytDlpCookiesFromBrowser = ytDlpCookiesFromBrowser;
	}

	public String getYtDlpCookiesFile() {
		return ytDlpCookiesFile;
	}

	public void setYtDlpCookiesFile(String ytDlpCookiesFile) {
		this.ytDlpCookiesFile = ytDlpCookiesFile;
	}

	private String resolveExecutable(String configuredPath) {
		if (configuredPath == null || configuredPath.isBlank()) {
			return configuredPath;
		}
		Path configured = Path.of(configuredPath.trim());
		if (configured.isAbsolute() || !looksLikePath(configuredPath)) {
			return configuredPath;
		}
		Path userDir = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
		String normalized = configuredPath.replace("\\", "/");
		for (Path base : candidateBases(userDir)) {
			Path candidate = base.resolve(configured).toAbsolutePath().normalize();
			if (Files.isRegularFile(candidate)) {
				return candidate.toString();
			}
			if (normalized.startsWith("../vendor/")) {
				Path vendorCandidate = base.resolve(normalized.substring(3)).toAbsolutePath().normalize();
				if (Files.isRegularFile(vendorCandidate)) {
					return vendorCandidate.toString();
				}
			}
		}
		return configuredPath;
	}

	private boolean looksLikePath(String value) {
		return value.contains("/") || value.contains("\\");
	}

	private Path[] candidateBases(Path userDir) {
		Path parent = userDir.getParent();
		Path childTool = userDir.resolve("tool");
		return parent == null
				? new Path[] { userDir, childTool }
				: new Path[] { userDir, parent, childTool };
	}
}
