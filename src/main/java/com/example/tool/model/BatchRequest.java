package com.example.tool.model;

public class BatchRequest {

	private String topic;
	private String sourceDirectory;
	private String outputDirectory;
	private String backgroundAudioPath;
	private int videosToGenerate;
	private int clipsPerVideo;
	private double minClipSeconds;
	private double maxClipSeconds;
	private int targetWidth;
	private int targetHeight;
	private boolean shuffleClips;
	private boolean mirrorEveryOtherVideo;
	private Long seed;

	public BatchRequest() {
	}

	public BatchRequest(String topic, String sourceDirectory, String outputDirectory, String backgroundAudioPath,
			int videosToGenerate, int clipsPerVideo, double minClipSeconds, double maxClipSeconds, int targetWidth,
			int targetHeight, boolean shuffleClips, boolean mirrorEveryOtherVideo, Long seed) {
		this.topic = topic;
		this.sourceDirectory = sourceDirectory;
		this.outputDirectory = outputDirectory;
		this.backgroundAudioPath = backgroundAudioPath;
		this.videosToGenerate = videosToGenerate;
		this.clipsPerVideo = clipsPerVideo;
		this.minClipSeconds = minClipSeconds;
		this.maxClipSeconds = maxClipSeconds;
		this.targetWidth = targetWidth;
		this.targetHeight = targetHeight;
		this.shuffleClips = shuffleClips;
		this.mirrorEveryOtherVideo = mirrorEveryOtherVideo;
		this.seed = seed;
	}

	public BatchRequest withDefaults() {
		return new BatchRequest(
				blank(topic) ? "untitled-topic" : topic.trim(),
				blank(sourceDirectory) ? "sources" : sourceDirectory.trim(),
				blank(outputDirectory) ? "outputs" : outputDirectory.trim(),
				blank(backgroundAudioPath) ? null : backgroundAudioPath.trim(),
				videosToGenerate <= 0 ? 3 : videosToGenerate,
				clipsPerVideo <= 0 ? 5 : clipsPerVideo,
				minClipSeconds <= 0 ? 2.5 : minClipSeconds,
				maxClipSeconds <= 0 ? 5.0 : maxClipSeconds,
				targetWidth <= 0 ? 1080 : targetWidth,
				targetHeight <= 0 ? 1920 : targetHeight,
				shuffleClips,
				mirrorEveryOtherVideo,
				seed);
	}

	public String topic() {
		return topic;
	}

	public String sourceDirectory() {
		return sourceDirectory;
	}

	public String outputDirectory() {
		return outputDirectory;
	}

	public String backgroundAudioPath() {
		return backgroundAudioPath;
	}

	public int videosToGenerate() {
		return videosToGenerate;
	}

	public int clipsPerVideo() {
		return clipsPerVideo;
	}

	public double minClipSeconds() {
		return minClipSeconds;
	}

	public double maxClipSeconds() {
		return maxClipSeconds;
	}

	public int targetWidth() {
		return targetWidth;
	}

	public int targetHeight() {
		return targetHeight;
	}

	public boolean shuffleClips() {
		return shuffleClips;
	}

	public boolean mirrorEveryOtherVideo() {
		return mirrorEveryOtherVideo;
	}

	public Long seed() {
		return seed;
	}

	public String getTopic() {
		return topic;
	}

	public String getSourceDirectory() {
		return sourceDirectory;
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

	public String getBackgroundAudioPath() {
		return backgroundAudioPath;
	}

	public int getVideosToGenerate() {
		return videosToGenerate;
	}

	public int getClipsPerVideo() {
		return clipsPerVideo;
	}

	public double getMinClipSeconds() {
		return minClipSeconds;
	}

	public double getMaxClipSeconds() {
		return maxClipSeconds;
	}

	public int getTargetWidth() {
		return targetWidth;
	}

	public int getTargetHeight() {
		return targetHeight;
	}

	public boolean isShuffleClips() {
		return shuffleClips;
	}

	public boolean isMirrorEveryOtherVideo() {
		return mirrorEveryOtherVideo;
	}

	public Long getSeed() {
		return seed;
	}

	private static boolean blank(String value) {
		return value == null || value.isBlank();
	}
}
