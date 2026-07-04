package com.example.tool.model;

import java.util.ArrayList;
import java.util.List;

public class SplitClipDeleteResult {

	private final int deletedCount;
	private final List<String> deletedClips;
	private final List<String> skippedClips;

	public SplitClipDeleteResult(int deletedCount, List<String> deletedClips, List<String> skippedClips) {
		this.deletedCount = deletedCount;
		this.deletedClips = deletedClips == null ? new ArrayList<>() : deletedClips;
		this.skippedClips = skippedClips == null ? new ArrayList<>() : skippedClips;
	}

	public int getDeletedCount() {
		return deletedCount;
	}

	public List<String> getDeletedClips() {
		return deletedClips;
	}

	public List<String> getSkippedClips() {
		return skippedClips;
	}
}
