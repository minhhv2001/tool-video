package com.example.tool.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NetworkVideoRequest {

	private String videoUrl;
	private List<String> videoUrls = new ArrayList<>();
	private Integer clipCount;
	private Double clipSeconds;
	private String cutNote;
	private String cookiesFilePath;
	private String aspectRatio;

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public List<String> getVideoUrls() {
		return videoUrls;
	}

	public void setVideoUrls(List<String> videoUrls) {
		this.videoUrls = videoUrls;
	}

	public Integer getClipCount() {
		return clipCount;
	}

	public void setClipCount(Integer clipCount) {
		this.clipCount = clipCount;
	}

	public Double getClipSeconds() {
		return clipSeconds;
	}

	public void setClipSeconds(Double clipSeconds) {
		this.clipSeconds = clipSeconds;
	}

	public String getCutNote() {
		return cutNote;
	}

	public void setCutNote(String cutNote) {
		this.cutNote = cutNote;
	}

	public String getCookiesFilePath() {
		return cookiesFilePath;
	}

	public void setCookiesFilePath(String cookiesFilePath) {
		this.cookiesFilePath = cookiesFilePath;
	}

	public String getAspectRatio() {
		return aspectRatio;
	}

	public void setAspectRatio(String aspectRatio) {
		this.aspectRatio = aspectRatio;
	}

	public List<String> normalizedUrls() {
		List<String> urls = new ArrayList<>();
		if (videoUrl != null && !videoUrl.isBlank()) {
			urls.add(videoUrl);
		}
		if (videoUrls != null) {
			urls.addAll(videoUrls);
		}
		return urls.stream()
				.filter(url -> url != null && !url.isBlank())
				.map(String::trim)
				.distinct()
				.collect(Collectors.toList());
	}
}
