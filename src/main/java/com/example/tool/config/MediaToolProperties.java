package com.example.tool.config;

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
		return ffmpegPath;
	}

	public void setFfmpegPath(String ffmpegPath) {
		this.ffmpegPath = ffmpegPath;
	}

	public String getFfprobePath() {
		return ffprobePath;
	}

	public void setFfprobePath(String ffprobePath) {
		this.ffprobePath = ffprobePath;
	}

	public String getYtDlpPath() {
		return ytDlpPath;
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
}
