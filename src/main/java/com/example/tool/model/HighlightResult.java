package com.example.tool.model;

import java.util.List;

public class HighlightResult {

	private final String jobId;
	private final String status;
	private final String originalFileName;
	private final double sourceDurationSeconds;
	private final int clipsUsed;
	private final String downloadUrl;
	private final List<Double> selectedStarts;
	private final List<String> warnings;

	public HighlightResult(String jobId, String status, String originalFileName, double sourceDurationSeconds,
			int clipsUsed, String downloadUrl, List<Double> selectedStarts, List<String> warnings) {
		this.jobId = jobId;
		this.status = status;
		this.originalFileName = originalFileName;
		this.sourceDurationSeconds = sourceDurationSeconds;
		this.clipsUsed = clipsUsed;
		this.downloadUrl = downloadUrl;
		this.selectedStarts = selectedStarts;
		this.warnings = warnings;
	}

	public String getJobId() {
		return jobId;
	}

	public String getStatus() {
		return status;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public double getSourceDurationSeconds() {
		return sourceDurationSeconds;
	}

	public int getClipsUsed() {
		return clipsUsed;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public List<Double> getSelectedStarts() {
		return selectedStarts;
	}

	public List<String> getWarnings() {
		return warnings;
	}
}
