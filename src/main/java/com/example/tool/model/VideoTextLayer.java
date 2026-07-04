package com.example.tool.model;

public class VideoTextLayer {

	private double startSeconds;
	private double endSeconds;
	private Double textXPercent;
	private Double textYPercent;

	public double getStartSeconds() {
		return startSeconds;
	}

	public void setStartSeconds(double startSeconds) {
		this.startSeconds = startSeconds;
	}

	public double getEndSeconds() {
		return endSeconds;
	}

	public void setEndSeconds(double endSeconds) {
		this.endSeconds = endSeconds;
	}

	public Double getTextXPercent() {
		return textXPercent;
	}

	public void setTextXPercent(Double textXPercent) {
		this.textXPercent = textXPercent;
	}

	public Double getTextYPercent() {
		return textYPercent;
	}

	public void setTextYPercent(Double textYPercent) {
		this.textYPercent = textYPercent;
	}
}
