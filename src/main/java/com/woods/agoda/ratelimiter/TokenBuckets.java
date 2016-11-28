package com.woods.agoda.ratelimiter;

import java.util.concurrent.TimeUnit;

public final class TokenBuckets {

    private TokenBuckets() {}

    //******************************************************************************************** 
    // for each bucket. Use the Fixed Interval Refill strategy which says refill the available tokens when 
    // when there are none after the period / unit value passes
    //******************************************************************************************** 
    public static TokenBucket newFixedIntervalRefill(long capacityTokens, long refillTokens, long period, TimeUnit unit) {
        TokenBucket.RefillStrategy strategy = new FixedIntervalRefillStrategy(refillTokens, period, unit);
        return new TokenBucket(capacityTokens, strategy);
    }
}