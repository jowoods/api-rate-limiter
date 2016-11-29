# api-rate-limiter
Sample Exercise Limiting Access to API Using Token Bucket Technique for Rate Limiting

This is a simple Spring Boot REST Application.

The starting point for the application is the (src/main/java) HotelService.java file which contains the root path for the application.

The project uses a ToeknBucket approach to limit access to the API, it uses a Fixed interval to refresh the bucket of tokens 
for the api key for a given account.

config/application.properties files contains the configuration property for the location hotelsdb.csv file that is used as the data source


Requirements:

Java 8
Maven

1) To BUILD: 

   The applicatioon (as a self extracting .jar file) run the following command in the root directory of the application:
   mvn package

   the output of this will be in the target folder and a file named: agoda-0.0.1-SNAPSHOT.jar

2) To RUN:

   The application run the following command in the root directory of the application:
   java -jar -Dapi.rate.limits.path="C:\\projects\\heroku\\springbootselfex\\config\\apikeylimits.csv" target/agoda-0.0.1-SNAPSHOT.jar

   where the property api.rate.limits.path being passed in is the location of the API rate limit keys including the default/global values that will be loaded into 
   memory and checked to control the API rate limits and default as when there is no entry in the file/memory.

   Sample API key rate limits entries are in the config folder and named: apikeylimits.csv

   Sample API keys i.e.: GFTR and GHUIII from the above file

   The API-key defaults to DEFAULT but is otherwise expected to be provided as an HTTP Request HEADER named 'API-Key' 

3) To USE:

   To filter the results by city which is expeted to be provided to the api call as part of the path like so:
   http://localhost:8080/hotels/amsterdam or http://localhost:8080/hotels/bangkok

   To sort the Hotel results by Price in either ascending or descending order:

   in ascending (the default behavior):
   http://localhost:8080/hotels/bangkok&sortby=price&ascending=true

   or in descending order
   http://localhost:8080/hotels/bangkok?sortby=price&ascending=false

4) Rate limits:
   The API will currently show send a Response code of 429 when the api key for the time the API-Key is suspended if it exceeds its rate of requests/millis as per the rate limits file.  
  


