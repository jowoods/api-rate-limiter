package com.woods.agoda.ratelimiter;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class TokenBucketTest {

  private static final long CAPACITY = 5;

  @Test
  public void testGetCapacity(){
    TokenBucket.RefillStrategy refillStrategy = new FixedIntervalRefillStrategy(5, 10000, TimeUnit.MILLISECONDS);
  	TokenBucket bucket = new TokenBucket(5, refillStrategy);
    assertEquals(5, bucket.getCapacity());
  }

  @Test
  public void testGetSizeAfterConsumingTokens() throws IllegalArgumentException, InterruptedException{
    TokenBucket.RefillStrategy refillStrategy = new FixedIntervalRefillStrategy(10, 10000, TimeUnit.MILLISECONDS);
  	TokenBucket bucket = new TokenBucket(10, refillStrategy);
  	bucket.consume(2); // consume 2 of the 10 tokens/capacity
    assertEquals(8, bucket.getSize().get());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTokensToConsumeGreaterThanZero()  throws IllegalArgumentException, InterruptedException{
    TokenBucket.RefillStrategy refillStrategy = new FixedIntervalRefillStrategy(CAPACITY, 10000, TimeUnit.MILLISECONDS);
    TokenBucket bucket = new TokenBucket(CAPACITY, refillStrategy);
    bucket.consume(-2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTokensToConsumeLessThanCapacity()  throws IllegalArgumentException, InterruptedException{
    TokenBucket.RefillStrategy refillStrategy = new FixedIntervalRefillStrategy(CAPACITY, 10000, TimeUnit.MILLISECONDS);
    TokenBucket bucket = new TokenBucket(CAPACITY, refillStrategy);
    bucket.consume(10);
  }

  @Test
  public void testEmptyBucketHasZeroTokens() {
    TokenBucket.RefillStrategy refillStrategy = new FixedIntervalRefillStrategy(0, 10000, TimeUnit.MILLISECONDS);
  	TokenBucket bucket = new TokenBucket(0, refillStrategy);
    assertEquals(0, bucket.getSize().get());
  }
}
