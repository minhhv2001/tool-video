package com.example.tool.model;

public class SourceCandidate {

	private final String provider;
	private final String title;
	private final String license;
	private final String creator;
	private final String pageUrl;
	private final String downloadUrl;
	private final Integer width;
	private final Integer height;
	private final Double durationSeconds;

	public SourceCandidate(String provider, String title, String license, String creator, String pageUrl,
			String downloadUrl, Integer width, Integer height, Double durationSeconds) {
		this.provider = provider;
		this.title = title;
		this.license = license;
		this.creator = creator;
		this.pageUrl = pageUrl;
		this.downloadUrl = downloadUrl;
		this.width = width;
		this.height = height;
		this.durationSeconds = durationSeconds;
	}

	public String getProvider() {
		return provider;
	}

	public String getTitle() {
		return title;
	}

	public String getLicense() {
		return license;
	}

	public String getCreator() {
		return creator;
	}

	public String getPageUrl() {
		return pageUrl;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public Integer getWidth() {
		return width;
	}

	public Integer getHeight() {
		return height;
	}

	public Double getDurationSeconds() {
		return durationSeconds;
	}
}
