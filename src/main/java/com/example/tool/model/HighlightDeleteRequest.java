package com.example.tool.model;

import java.util.ArrayList;
import java.util.List;

public class HighlightDeleteRequest {

	private List<String> jobIds = new ArrayList<>();

	public List<String> getJobIds() {
		return jobIds;
	}

	public void setJobIds(List<String> jobIds) {
		this.jobIds = jobIds;
	}
}
