package com.example.tool.model;

public class FacebookBatchRequest {

	private String reelsUrl;
	private Integer startIndex;
	private Integer endIndex;
	private String cookiesFilePath;

	public String getReelsUrl() {
		return reelsUrl;
	}

	public void setReelsUrl(String reelsUrl) {
		this.reelsUrl = reelsUrl;
	}

	public Integer getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(Integer startIndex) {
		this.startIndex = startIndex;
	}

	public Integer getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(Integer endIndex) {
		this.endIndex = endIndex;
	}

	public String getCookiesFilePath() {
		return cookiesFilePath;
	}

	public void setCookiesFilePath(String cookiesFilePath) {
		this.cookiesFilePath = cookiesFilePath;
	}
}
