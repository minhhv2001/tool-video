package com.example.tool.model;

public class SplitClipHistoryItem {

	private String jobId;
	private int clipIndex;
	private String status;
	private String createdAt;
	private String updatedAt;
	private String originalFileName;
	private String outputFileName;
	private String cutNote;
	private double startSeconds;
	private double durationSeconds;
	private String downloadUrl;
	private String error;

	public SplitClipHistoryItem() {
	}

	public SplitClipHistoryItem(String jobId, int clipIndex, String status, String createdAt, String updatedAt,
			String originalFileName, String outputFileName, String cutNote, double startSeconds, double durationSeconds,
			String downloadUrl, String error) {
		this.jobId = jobId;
		this.clipIndex = clipIndex;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.originalFileName = originalFileName;
		this.outputFileName = outputFileName;
		this.cutNote = cutNote;
		this.startSeconds = startSeconds;
		this.durationSeconds = durationSeconds;
		this.downloadUrl = downloadUrl;
		this.error = error;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public int getClipIndex() {
		return clipIndex;
	}

	public void setClipIndex(int clipIndex) {
		this.clipIndex = clipIndex;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public void setOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	public String getCutNote() {
		return cutNote;
	}

	public void setCutNote(String cutNote) {
		this.cutNote = cutNote;
	}

	public double getStartSeconds() {
		return startSeconds;
	}

	public void setStartSeconds(double startSeconds) {
		this.startSeconds = startSeconds;
	}

	public double getDurationSeconds() {
		return durationSeconds;
	}

	public void setDurationSeconds(double durationSeconds) {
		this.durationSeconds = durationSeconds;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}
