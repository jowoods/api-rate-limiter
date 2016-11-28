package com.woods.agoda.ratelimiter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.concurrent.*;

// *****************************************************************
// RateLimitService class 
//
// provides read access to the comma separated file of api keys 
// the including the default values that will be used if an api key is not present for an account. 
// The CSV file should have headings as well indicating the usage of each field provided
// the window for the api rate limit should be specified in milliseconds
//*****************************************************************
public class RateLimitService {
	public static ConcurrentHashMap<String, TokenBucket> retrieveApiKeyLimits() {        
       	ConcurrentHashMap<String,TokenBucket> apiKeyLimits = new ConcurrentHashMap<String,TokenBucket>();
		BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {
            //***************************************************************************************************************
            // NOTE: Pass the property api.rate.limits.path in via system property i.e. -D only because Spring takes issue with 
            // @Value injection annoation for static variables
            //***************************************************************************************************************
            String pathToAPIRateLimits = System.getProperty("api.rate.limits.path"); 
            if(pathToAPIRateLimits!=null) {
                br = new BufferedReader(new FileReader(pathToAPIRateLimits));
                int idx = 0;
                while ((line = br.readLine()) != null) {
                    String[] entry = line.split(cvsSplitBy);
                	if(idx > 0 && entry.length >= 3) { // bypass 1st line in the file as it is for the headings of the CSV, there s/b at least 3 fields for each line of the api keys file

                        //***************************************************************************************************************    
                        // Consder default to DEFAULT Values if some of these are invalid i.e. cause NPE's or NumberFormatException
                        //***************************************************************************************************************    
                        TokenBucket bucket = TokenBuckets.newFixedIntervalRefill(Long.parseLong(entry[1].trim()),Long.parseLong(entry[1].trim()), Long.parseLong(entry[2].trim()), TimeUnit.MILLISECONDS); 

    	                // add apiKeyLimit for this customer/account to map by apiKey.	
    	                apiKeyLimits.put(entry[0].trim(),bucket);
             	    }
             	    idx++;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return apiKeyLimits;
	}
}