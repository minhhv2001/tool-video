package com.example.tool.model;

public class VideoEditResult {

	private final String jobId;
	private final String downloadUrl;
	private final String previewUrl;
	private final String message;

	public VideoEditResult(String jobId, String downloadUrl, String previewUrl, String message) {
		this.jobId = jobId;
		this.downloadUrl = downloadUrl;
		this.previewUrl = previewUrl;
		this.message = message;
	}

	public String getJobId() {
		return jobId;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public String getPreviewUrl() {
		return previewUrl;
	}

	public String getMessage() {
		return message;
	}
}
