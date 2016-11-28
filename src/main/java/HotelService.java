import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PathVariable;

import com.woods.agoda.HotelUtil;
import com.woods.agoda.data.Hotel;
import java.util.List;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.util.concurrent.ConcurrentHashMap;
import com.woods.agoda.ratelimiter.*;
import java.util.concurrent.atomic.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@RestController
@EnableAutoConfiguration
public class HotelService {

    //******************************************************************** 
    // As configured in the application.properties file and inject the path
    // to the hotels data source
    //******************************************************************** 
    @Value("${com.woods.agoda.hotel.db.path}")
    private String hotelsDbPath;

    //******************************************************************** 
    // In Memory container for apiKeys and their limits including the default entry
    //******************************************************************** 
    private static ConcurrentHashMap<String,TokenBucket> apiKeyLimits = RateLimitService.retrieveApiKeyLimits();

    //******************************************************************** 
    // Set up sortby and sortdirection as optional fields
    // todo try to negotiate xml or json response: 
    // http://www.oresteluci.com/java-rest-xml-json-response/
    // Note: retreiving API key field from header NOT as request parameter
    // default to DEFAULT api key if none provided for the 'free level' ;) 
    //********************************************************************
    @RequestMapping(value="/hotels/{city}",method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    String home(@RequestHeader(value = "API-Key", defaultValue="DEFAULT") String apiKey, @PathVariable String city, @RequestParam("sortby") Optional<String> sortBy, @RequestParam("ascending") Optional<String> ascending) {
        String jsonOutput =  "{}";

        try{
            TokenBucket defBucket = apiKeyLimits.get("DEFAULT"); 

            System.out.println(new StringBuilder("Does map of api key limits contain the key ").append(apiKey).append(" being passed? ").append(apiKeyLimits.contains(apiKey)));    // if not subject it to the global limits
            //*********************************************************************************************************
            // use the DEFAULT settings if no matching api key found in storage
            //*********************************************************************************************************
            TokenBucket bucket = apiKeyLimits.getOrDefault(apiKey,defBucket); 

            long preConsumeSize = bucket.getSize().get();
            System.out.println(new StringBuilder("Pre Consume size: (if > 0 and the unlock time is not in the future, request s/b handled below:) ").append(preConsumeSize) .append(", capacity: ").append(bucket.getCapacity()));

            //*********************************************************************************************************
            // If pre-consume size is >= 0 and the bucket is not currently ssupended then allow the request thru
            //*********************************************************************************************************
            if(preConsumeSize >= 0 && bucket.getUnlockTime().get() <= System.currentTimeMillis()) {  
                // CONSUME TOKEN from set for this request for this api key
                AtomicLong size = bucket.consume(1);
                System.out.println(new StringBuilder("Post Consume size: ").append(size.get()));

                //*********************************************************************************************************************** 
                // Being sure to store this under the key being used not the DEFAULT for the accounts that are using the default setting
                // this updates the existing entry if present, adjusting the size of the tokens for that bucket/account/apiKey
                //*********************************************************************************************************************** 
                apiKeyLimits.put(apiKey,bucket); 

                System.out.println(new StringBuilder("Request allowed for apiKey: ").append(apiKey));
                //*********************************************************** 
                // default to sorting by price 
                // default to sorting in ascending order i.e. for price or city name... regardless of field if any being used to sort on 
                //*********************************************************** 
                List<Hotel> hotelList = HotelUtil.retrieveHotels(hotelsDbPath,city,sortBy.orElse("price"),ascending.orElse("true"));
                jsonOutput = HotelUtil.convertToJson(hotelList);

            } else { // if no more tokens available OR the bucket is currently locked send message to the user with applicable response code.
                jsonOutput = "{'status':'Account currently suspended as you have exceeded rate limits for your account or the default / global limits'}";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return jsonOutput;
    }

    //*****************************************************************************************************************************************
    // Run using this java -jar -Dapi.rate.limits.path="C:\\projects\\heroku\\springbootselfex\\config\\apikeylimits.csv" target/agoda-0.0.1-SNAPSHOT.jar
    // To override the path to the api rate limits
    //*****************************************************************************************************************************************
    public static void main(String[] args) throws Exception {
        SpringApplication.run(HotelService.class, args);
    }
}