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
import org.springframework.http.*;
import java.util.concurrent.ConcurrentHashMap;
import com.woods.agoda.ratelimiter.*;
import java.util.concurrent.atomic.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@EnableAutoConfiguration
public class HotelService {

    //******************************************************************** 
    // As configured in the application.properties file and inject the path
    // to the hotels data source
    //******************************************************************** 
    @Value("${com.woods.agoda.hotel.db.path}")
    private String hotelsDbPath;

    @Autowired
	private RateLimitService rateLimitService;

    @Autowired
    private HotelUtil hotelUtil;

    //******************************************************************** 
    // In Memory container for apiKeys and their limits including the default entry
    //******************************************************************** 
    private ConcurrentHashMap<String,TokenBucket> apiKeyLimits = rateLimitService.retrieveApiKeyLimits();

    //******************************************************************** 
    // Set up sortby and sortdirection as optional fields
    // todo try to negotiate xml or json response: 
    // http://www.oresteluci.com/java-rest-xml-json-response/
    // Note: retreiving API key field from header NOT as request parameter
    // default to DEFAULT api key if none provided for the 'free level' ;) 
    //********************************************************************
    @RequestMapping(value="/hotels/{city}",method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity home(@RequestHeader(value = "API-Key", defaultValue="DEFAULT") String apiKey, @PathVariable String city, @RequestParam("sortby") Optional<String> sortBy, @RequestParam("ascending") Optional<String> ascending) {
        String jsonOutput =  "{}";

        try{
            TokenBucket defBucket = apiKeyLimits.get("DEFAULT"); 

            //@Todo replace all logging with log4j or someother logging api that allows log levels etc.
            System.out.println(new StringBuilder("Does map of api key limits contain the key ").append(apiKey).append(" being passed? ").append(apiKeyLimits.contains(apiKey)));    // if not subject it to the global limits
            //*********************************************************************************************************
            // use the DEFAULT settings if no matching api key found in storage
            //*********************************************************************************************************
            TokenBucket bucket = apiKeyLimits.getOrDefault(apiKey,defBucket); 

            long preConsumeSize = bucket.getSize().get();
            System.out.println(new StringBuilder("Pre Consume size: (if > 0 and the unlock time is not in the future, request s/b handled below:) ").append(preConsumeSize) .append(", capacity: ").append(bucket.getCapacity()));

            //*********************************************************************************************************
            // If pre-consume size is >= 0 and the bucket is not currently suspended then allow the request thru
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
                List<Hotel> hotelList = hotelUtil.retrieveHotels(hotelsDbPath,city,sortBy.orElse("price"),ascending.orElse("true"));
                jsonOutput = hotelUtil.convertToJson(hotelList);

                return new ResponseEntity(jsonOutput,HttpStatus.OK);

            } else { // if no more tokens available OR the bucket is currently locked send message to the user with applicable response code.
                jsonOutput = "{'status':'Account currently suspended as you have exceeded rate limits for your account or the default / global limits'}";
                return new ResponseEntity<String>(HttpStatus.TOO_MANY_REQUESTS); // return code 429 to indicate too many request
            }
        } catch (Exception e) { // something went wrong!!!
            e.printStackTrace();
	        return new ResponseEntity(jsonOutput,HttpStatus.BAD_REQUEST);
        }
    }

    //*****************************************************************************************************************************************
    // Run using this java -jar -Dapi.rate.limits.path="C:\\projects\\heroku\\api-rate-limiter\\config\\apikeylimits.csv" target/agoda-0.0.1-SNAPSHOT.jar
    // To override the path to the api rate limits
    //*****************************************************************************************************************************************
    public static void main(String[] args) throws Exception {
        SpringApplication.run(HotelService.class, args);
    }
}