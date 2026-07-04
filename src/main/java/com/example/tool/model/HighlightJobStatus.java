package com.example.tool.model;

import java.util.ArrayList;
import java.util.List;

public class HighlightJobStatus {

	private final String jobId;
	private volatile String status;
	private volatile int progress;
	private volatile String phase;
	private volatile String originalFileName;
	private volatile List<String> inputFileNames;
	private volatile String cutNote;
	private volatile double sourceDurationSeconds;
	private volatile double totalDurationSeconds;
	private volatile int clipsUsed;
	private volatile String downloadUrl;
	private volatile List<Double> selectedStarts;
	private volatile List<String> warnings;
	private volatile String error;

	public HighlightJobStatus(String jobId, String originalFileName) {
		this.jobId = jobId;
		this.originalFileName = originalFileName;
		this.inputFileNames = new ArrayList<>();
		this.inputFileNames.add(originalFileName);
		this.cutNote = "";
		this.status = "processing";
		this.progress = 5;
		this.phase = "Đã nhận video, đang chuẩn bị xử lý.";
		this.selectedStarts = new ArrayList<>();
		this.warnings = new ArrayList<>();
	}

	public String getJobId() {
		return jobId;
	}

	public String getStatus() {
		return status;
	}

	public int getProgress() {
		return progress;
	}

	public String getPhase() {
		return phase;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public List<String> getInputFileNames() {
		return inputFileNames;
	}

	public String getCutNote() {
		return cutNote;
	}

	public void setCutNote(String cutNote) {
		this.cutNote = cutNote == null ? "" : cutNote;
	}

	public void setInputFileNames(List<String> inputFileNames) {
		this.inputFileNames = inputFileNames;
		if (inputFileNames != null && !inputFileNames.isEmpty()) {
			this.originalFileName = inputFileNames.get(0);
		}
	}

	public double getSourceDurationSeconds() {
		return sourceDurationSeconds;
	}

	public double getTotalDurationSeconds() {
		return totalDurationSeconds;
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

	public String getError() {
		return error;
	}

	public void progress(int progress, String phase) {
		this.progress = Math.max(0, Math.min(100, progress));
		this.phase = phase;
	}

	public void ready(double sourceDurationSeconds, int clipsUsed, String downloadUrl, List<Double> selectedStarts, List<String> warnings) {
		this.status = "ready";
		this.progress = 100;
		this.phase = "Hoàn tất. Video highlight đã sẵn sàng tải xuống.";
		this.sourceDurationSeconds = sourceDurationSeconds;
		this.totalDurationSeconds = sourceDurationSeconds;
		this.clipsUsed = clipsUsed;
		this.downloadUrl = downloadUrl;
		this.selectedStarts = selectedStarts;
		this.warnings = warnings;
	}

	public void ready(double sourceDurationSeconds, double totalDurationSeconds, int clipsUsed, String downloadUrl, List<Double> selectedStarts, List<String> warnings) {
		ready(sourceDurationSeconds, clipsUsed, downloadUrl, selectedStarts, warnings);
		this.totalDurationSeconds = totalDurationSeconds;
	}

	public void failed(String message) {
		this.status = "error";
		this.progress = 100;
		this.phase = "Xử lý thất bại.";
		this.error = message;
	}
}
