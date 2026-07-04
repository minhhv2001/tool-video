package com.example.tool.model;

public class DownloadResult {

	private final String path;
	private final long bytes;

	public DownloadResult(String path, long bytes) {
		this.path = path;
		this.bytes = bytes;
	}

	public String getPath() {
		return path;
	}

	public long getBytes() {
		return bytes;
	}
}
