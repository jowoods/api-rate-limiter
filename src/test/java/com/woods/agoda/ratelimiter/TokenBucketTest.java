package com.woods.agoda.ratelimiter;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import com.woods.agoda.exceptions.*;

public class TokenBucketTest {

  private static final long CAPACITY = 5;

  @Test
  public void testGetCapacity(){
    TokenBucket.RefillStrategy refillStrategy = new FixedIntervalRefillStrategy(5, 10000, TimeUnit.MILLISECONDS);
  	TokenBucket bucket = new TokenBucket(5, refillStrategy);
    assertEquals(6, bucket.getCapacity());
  }

  @Test
  public void testGetSizeAfterConsumingTokens() throws IllegalArgumentException, InterruptedException{
    TokenBucket.RefillStrategy refillStrategy = new FixedIntervalRefillStrategy(10, 10000, TimeUnit.MILLISECONDS);
  	TokenBucket bucket = new TokenBucket(10, refillStrategy);
  	bucket.consume(2); // consume 2 of the 10 tokens/capacity
    assertEquals(9, bucket.getSize().get());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTokensToConsumeGreaterThanZero()  throws IllegalArgumentException, InterruptedException{
    TokenBucket.RefillStrategy refillStrategy = new FixedIntervalRefillStrategy(CAPACITY, 10000, TimeUnit.MILLISECONDS);
    TokenBucket bucket = new TokenBucket(CAPACITY, refillStrategy);
    bucket.consume(-2);
  }

  @Test(expected = RefillInProgressException.class)
  public void testRefillInProgressException()  throws IllegalArgumentException, InterruptedException, RefillInProgressException{
    TokenBucket.RefillStrategy refillStrategy = new FixedIntervalRefillStrategy(CAPACITY, 10000, TimeUnit.MILLISECONDS);
    refillStrategy.refill();

    TokenBucket bucket = new TokenBucket(CAPACITY, refillStrategy);
    bucket.consume(1);
    bucket.consume(1);
    bucket.consume(1);
    bucket.consume(1);
    Thread.sleep(1000); // wait for 1 second and then request more within refill 5 second window
    bucket.consume(3); // exceed capacity before change to refill
  }

  @Test
  public void testNoRefillInProgressException()  throws IllegalArgumentException, InterruptedException, RefillInProgressException{
    TokenBucket.RefillStrategy refillStrategy = new FixedIntervalRefillStrategy(CAPACITY, 5000, TimeUnit.MILLISECONDS);
    refillStrategy.refill();

    TokenBucket bucket = new TokenBucket(CAPACITY, refillStrategy);

    Thread.sleep(10000); // wait for 10 seconds to give token bucket plenty of time fo fill

    // consume 5 entries of the 5 token capacity
    bucket.consume(1);
    bucket.consume(1);
    bucket.consume(1);
    bucket.consume(1);
    bucket.consume(1);

    Thread.sleep(refillStrategy.getIntervalInMillis()); // give the bucket a chance to refill
   
    bucket.consume(2); // exceed capacity before change to refill
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
    assertEquals(1, bucket.getSize().get());
  }
}
