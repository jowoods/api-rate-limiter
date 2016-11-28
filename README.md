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

To Build the applicatioon (as a self extracting .jar file) run the following command in the root directory of the application:
mvn package

To Run the application run the following command in the root directory of the application:
java -jar -Dapi.rate.limits.path="C:\\projects\\heroku\\springbootselfex\\config\\apikeylimits.csv" target/agoda-0.0.1-SNAPSHOT.jar

where the property api.rate.limits.path is the location of the API keys including the default/global values that will be loaded into 
memory and checked to control the API rate limits

