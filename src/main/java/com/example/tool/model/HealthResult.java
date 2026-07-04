package com.example.tool.model;

public class HealthResult {

	private final boolean ffmpegAvailable;
	private final boolean ffprobeAvailable;
	private final String workspace;
	private final String sourcesDirectory;
	private final String outputsDirectory;
	private final String message;

	public HealthResult(boolean ffmpegAvailable, boolean ffprobeAvailable, String workspace, String sourcesDirectory,
			String outputsDirectory, String message) {
		this.ffmpegAvailable = ffmpegAvailable;
		this.ffprobeAvailable = ffprobeAvailable;
		this.workspace = workspace;
		this.sourcesDirectory = sourcesDirectory;
		this.outputsDirectory = outputsDirectory;
		this.message = message;
	}

	public boolean isFfmpegAvailable() {
		return ffmpegAvailable;
	}

	public boolean isFfprobeAvailable() {
		return ffprobeAvailable;
	}

	public String getWorkspace() {
		return workspace;
	}

	public String getSourcesDirectory() {
		return sourcesDirectory;
	}

	public String getOutputsDirectory() {
		return outputsDirectory;
	}

	public String getMessage() {
		return message;
	}
}
