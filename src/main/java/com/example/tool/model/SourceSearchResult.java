package com.example.tool.model;

import java.util.List;

public class SourceSearchResult {

	private final String provider;
	private final String query;
	private final List<SourceCandidate> candidates;
	private final List<String> warnings;

	public SourceSearchResult(String provider, String query, List<SourceCandidate> candidates, List<String> warnings) {
		this.provider = provider;
		this.query = query;
		this.candidates = candidates;
		this.warnings = warnings;
	}

	public String getProvider() {
		return provider;
	}

	public String getQuery() {
		return query;
	}

	public List<SourceCandidate> getCandidates() {
		return candidates;
	}

	public List<String> getWarnings() {
		return warnings;
	}
}
