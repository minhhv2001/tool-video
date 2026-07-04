package com.example.tool.model;

public class DownloadRequest {

	private String downloadUrl;
	private String fileName;

	public DownloadRequest() {
	}

	public DownloadRequest(String downloadUrl, String fileName) {
		this.downloadUrl = downloadUrl;
		this.fileName = fileName;
	}

	public String downloadUrl() {
		return downloadUrl;
	}

	public String fileName() {
		return fileName;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
