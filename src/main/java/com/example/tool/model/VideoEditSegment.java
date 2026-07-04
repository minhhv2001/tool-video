package com.example.tool.model;

public class VideoEditSegment {

	private double startSeconds;
	private double endSeconds;

	public VideoEditSegment() {
	}

	public VideoEditSegment(double startSeconds, double endSeconds) {
		this.startSeconds = startSeconds;
		this.endSeconds = endSeconds;
	}

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

	public double durationSeconds() {
		return Math.max(0, endSeconds - startSeconds);
	}
}
