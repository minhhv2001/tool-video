package com.example.tool.model;

import java.util.ArrayList;
import java.util.List;

public class SplitClipDeleteRequest {

	private List<ClipRef> clips = new ArrayList<>();

	public List<ClipRef> getClips() {
		return clips;
	}

	public void setClips(List<ClipRef> clips) {
		this.clips = clips;
	}

	public static class ClipRef {
		private String jobId;
		private int clipIndex;

		public ClipRef() {
		}

		public ClipRef(String jobId, int clipIndex) {
			this.jobId = jobId;
			this.clipIndex = clipIndex;
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
	}
}
