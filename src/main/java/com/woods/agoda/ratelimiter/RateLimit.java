package com.woods.agoda.ratelimiter;

public class RateLimit {

	private String apiKey;
	private int maxRequests;
	private int perMilliseconds; 

	public RateLimit(String apiKey, int maxRequests, int perMillseconds) {
		this.apiKey = apiKey;
		this.maxRequests = maxRequests;
		this.perMilliseconds = perMilliseconds;
	}

	public String getApiKey() {
		return apiKey;
	}

	public int getMaxRequests() {
		return maxRequests;
	}

	public int getPerMilliseconds() {
		return perMilliseconds;
	}

}