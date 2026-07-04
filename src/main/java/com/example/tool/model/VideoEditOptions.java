package com.example.tool.model;

public class VideoEditOptions {

	private String sourceType;
	private Double startSeconds;
	private Double endSeconds;
	private Double rotationDegrees;
	private Double videoZoom;
	private Integer outputWidth;
	private Integer outputHeight;
	private String overlayText;
	private Double textXPercent;
	private Double textYPercent;
	private Integer textSize;
	private String textColor;
	private String textFont;
	private String textBackground;
	private String textPosition;
	private String audioMode;
	private Boolean muteOriginalAudio;
	private String saveMode;
	private String title;
	private String segmentsJson;

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public Double getStartSeconds() {
		return startSeconds;
	}

	public void setStartSeconds(Double startSeconds) {
		this.startSeconds = startSeconds;
	}

	public Double getEndSeconds() {
		return endSeconds;
	}

	public void setEndSeconds(Double endSeconds) {
		this.endSeconds = endSeconds;
	}

	public Double getRotationDegrees() {
		return rotationDegrees;
	}

	public void setRotationDegrees(Double rotationDegrees) {
		this.rotationDegrees = rotationDegrees;
	}

	public Double getVideoZoom() {
		return videoZoom;
	}

	public void setVideoZoom(Double videoZoom) {
		this.videoZoom = videoZoom;
	}

	public Integer getOutputWidth() {
		return outputWidth;
	}

	public void setOutputWidth(Integer outputWidth) {
		this.outputWidth = outputWidth;
	}

	public Integer getOutputHeight() {
		return outputHeight;
	}

	public void setOutputHeight(Integer outputHeight) {
		this.outputHeight = outputHeight;
	}

	public String getOverlayText() {
		return overlayText;
	}

	public void setOverlayText(String overlayText) {
		this.overlayText = overlayText;
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

	public Integer getTextSize() {
		return textSize;
	}

	public void setTextSize(Integer textSize) {
		this.textSize = textSize;
	}

	public String getTextColor() {
		return textColor;
	}

	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}

	public String getTextFont() {
		return textFont;
	}

	public void setTextFont(String textFont) {
		this.textFont = textFont;
	}

	public String getTextBackground() {
		return textBackground;
	}

	public void setTextBackground(String textBackground) {
		this.textBackground = textBackground;
	}

	public String getTextPosition() {
		return textPosition;
	}

	public void setTextPosition(String textPosition) {
		this.textPosition = textPosition;
	}

	public String getAudioMode() {
		return audioMode;
	}

	public void setAudioMode(String audioMode) {
		this.audioMode = audioMode;
	}

	public Boolean getMuteOriginalAudio() {
		return muteOriginalAudio;
	}

	public void setMuteOriginalAudio(Boolean muteOriginalAudio) {
		this.muteOriginalAudio = muteOriginalAudio;
	}

	public String getSaveMode() {
		return saveMode;
	}

	public void setSaveMode(String saveMode) {
		this.saveMode = saveMode;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSegmentsJson() {
		return segmentsJson;
	}

	public void setSegmentsJson(String segmentsJson) {
		this.segmentsJson = segmentsJson;
	}
}
