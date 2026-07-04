package com.example.tool.model;

import java.util.ArrayList;
import java.util.List;

public class HighlightHistoryItem {

	private String jobId;
	private String status;
	private String createdAt;
	private String updatedAt;
	private List<String> inputFileNames = new ArrayList<>();
	private String cutNote;
	private double totalDurationSeconds;
	private int clipsUsed;
	private String downloadUrl;
	private String error;

	public HighlightHistoryItem() {
	}

	public HighlightHistoryItem(String jobId, String status, String createdAt, String updatedAt,
			List<String> inputFileNames, String cutNote, double totalDurationSeconds, int clipsUsed, String downloadUrl, String error) {
		this.jobId = jobId;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.inputFileNames = inputFileNames;
		this.cutNote = cutNote;
		this.totalDurationSeconds = totalDurationSeconds;
		this.clipsUsed = clipsUsed;
		this.downloadUrl = downloadUrl;
		this.error = error;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
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

	public List<String> getInputFileNames() {
		return inputFileNames;
	}

	public void setInputFileNames(List<String> inputFileNames) {
		this.inputFileNames = inputFileNames;
	}

	public String getCutNote() {
		return cutNote;
	}

	public void setCutNote(String cutNote) {
		this.cutNote = cutNote;
	}

	public double getTotalDurationSeconds() {
		return totalDurationSeconds;
	}

	public void setTotalDurationSeconds(double totalDurationSeconds) {
		this.totalDurationSeconds = totalDurationSeconds;
	}

	public int getClipsUsed() {
		return clipsUsed;
	}

	public void setClipsUsed(int clipsUsed) {
		this.clipsUsed = clipsUsed;
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
