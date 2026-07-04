package com.example.tool.model;

public class SourceSearchRequest {

	private String topic;
	private String provider;
	private int limit;

	public SourceSearchRequest() {
	}

	public SourceSearchRequest(String topic, String provider, int limit) {
		this.topic = topic;
		this.provider = provider;
		this.limit = limit;
	}

	public SourceSearchRequest withDefaults() {
		return new SourceSearchRequest(
				topic == null || topic.isBlank() ? "nature" : topic.trim(),
				provider == null || provider.isBlank() ? "local" : provider.trim().toLowerCase(),
				limit <= 0 ? 10 : Math.min(limit, 30));
	}

	public String topic() {
		return topic;
	}

	public String provider() {
		return provider;
	}

	public int limit() {
		return limit;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}
}
