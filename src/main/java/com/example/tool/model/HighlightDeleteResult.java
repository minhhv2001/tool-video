package com.example.tool.model;

import java.util.ArrayList;
import java.util.List;

public class HighlightDeleteResult {

	private final int deletedCount;
	private final List<String> deletedJobIds;
	private final List<String> skippedJobIds;

	public HighlightDeleteResult(int deletedCount, List<String> deletedJobIds, List<String> skippedJobIds) {
		this.deletedCount = deletedCount;
		this.deletedJobIds = deletedJobIds == null ? new ArrayList<>() : deletedJobIds;
		this.skippedJobIds = skippedJobIds == null ? new ArrayList<>() : skippedJobIds;
	}

	public int getDeletedCount() {
		return deletedCount;
	}

	public List<String> getDeletedJobIds() {
		return deletedJobIds;
	}

	public List<String> getSkippedJobIds() {
		return skippedJobIds;
	}
}
