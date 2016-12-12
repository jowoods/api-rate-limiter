package com.woods.agoda.ratelimiter;

import com.woods.agoda.exceptions.*;
import java.util.concurrent.atomic.*;
import java.util.Optional;

/*
    This is a TokenBucket implementation for throttling Requests for EACH account represented by an apiKey
*/

public class TokenBucket {
    private final RefillStrategy refillStrategy;
    private final long capacity;

    private long defaultSuspensionTimeMillis = 300000;

    // Optional<String> propDefaultuspensionTimeMillis = Optional.of(System.getProperty("com.agoda.suspension.time.millis"));
    
    //***************************************************************************
    // Using Atomic variables here to try to ensure thread safety of these requests
    // when updating the size and the unlockTimestamp
    //***************************************************************************
    private AtomicLong size;

    //***************************************************************************
    // Stores the last suspended time for this bucket/account, mainly used to determine if it is currently 
    // suspended...if its current value is after the current time then the account/apiKey has been suspended
    //***************************************************************************
    private AtomicLong unlockSuspendedTimeInMillis = new AtomicLong(0L); 

    public TokenBucket(long capacity, RefillStrategy refillStrategy) {
        this.refillStrategy = refillStrategy;
        this.capacity = capacity + 1;
        this.size = new AtomicLong(capacity + 1);
    }

    /*
       consume: this checks the current number of tokens available and adjusts accordingly
       This method to see if it needs to suspend the bucket if the current number of tokens allowed over the given period of time
       goes to zero then it is suspended
       after check to make sure we are not requesting more than an are allowable for this provide the number of tokens desired
    */
    public AtomicLong consume(long numTokens) throws InterruptedException, RefillInProgressException {
        if (numTokens < 0)
            throw new IllegalArgumentException("Number of tokens to consume must be a greater than zero");
        if (numTokens >= capacity)
            throw new IllegalArgumentException("Number of tokens to consume cannot be greater than or equal to the capacity of the bucket");

        long newTokens = Math.max(0, refillStrategy.refill());
        while (!Thread.currentThread().isInterrupted()) {
            long existingSize = size.get();
            long newValue = Math.max(0, Math.min(existingSize + newTokens, capacity));
            if (numTokens <= newValue) {
                newValue -= numTokens;
                if (size.compareAndSet(existingSize, newValue))
                    break;
            } else { 
                //************************************************************************
                // Here the api user has not exceeded their rate limit they are just at a point where the current number of tokens reuqetsted exceeds the number available 
                // thus exceeding the refill window
                // Options include sending them a 429 and suspending their key which is not desirable and may violate SLA
                // or sending the content not modified indication in conjunction with http cache headers since their last request, will use this a clue to send them a not modified
                // Thread.sleep(refillStrategy.getIntervalInMillis()); // sleeping for existing request 
                newTokens = Math.max(0, refillStrategy.refill());
                System.out.println("numTokens: " + numTokens + " > newTokens: "+ newTokens + " sending clue to tell the API user that there is an issue or non-modified exception");
                throw new RefillInProgressException("Refill in progress for current api key, use this to send no content modfication response so as not to disrupt service");
                //************************************************************************
            }
        }

        //**************************** CHECK IF NEED TO SUSPEND THE BUCKET/ACCOUNT/API KEY *********************************************
        // Using compareAndSet instead of set here to ensure atomicity comparing it to the currently stored value and then changing it
        //***********************************************************************************************************************************
        if(size.get() == 0L && unlockSuspendedTimeInMillis.get() < System.currentTimeMillis() && unlockSuspendedTimeInMillis.compareAndSet(this.unlockSuspendedTimeInMillis.get(),System.currentTimeMillis() + defaultSuspensionTimeMillis)) {
            System.out.println("This bucket is being locked as it has exceeded it's rate limit.");
        }
        return size;
   }

  public AtomicLong getSize() {
    return size;
  }

  public long getCapacity() {
    return capacity;
  }

  public AtomicLong getUnlockTime() {
    return unlockSuspendedTimeInMillis;
  }

  public static interface RefillStrategy {
         long refill();
         long getIntervalInMillis();
   }
}