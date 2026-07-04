package com.example.tool.model;

import java.util.List;

public class BatchResult {

	private final String jobId;
	private final String status;
	private final String brief;
	private final List<String> outputs;
	private final List<String> warnings;

	public BatchResult(String jobId, String status, String brief, List<String> outputs, List<String> warnings) {
		this.jobId = jobId;
		this.status = status;
		this.brief = brief;
		this.outputs = outputs;
		this.warnings = warnings;
	}

	public String getJobId() {
		return jobId;
	}

	public String getStatus() {
		return status;
	}

	public String getBrief() {
		return brief;
	}

	public List<String> getOutputs() {
		return outputs;
	}

	public List<String> getWarnings() {
		return warnings;
	}
}
