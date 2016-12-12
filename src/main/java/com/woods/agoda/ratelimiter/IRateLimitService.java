package com.woods.agoda.ratelimiter;

import java.util.concurrent.ConcurrentHashMap;

public interface IRateLimitService {
	public ConcurrentHashMap<String, TokenBucket> retrieveApiKeyLimits();
}