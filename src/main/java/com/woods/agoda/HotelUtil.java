package com.woods.agoda;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import com.woods.agoda.data.Hotel;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.*;
import com.google.gson.Gson;

public class HotelUtil {
	public static List<Hotel> retrieveHotels(String path, String cityFilter ,String sortBy, String ascending) {      
        String jsonOutput = "[]";  

       	List<Hotel> hotels = new ArrayList<Hotel>();

		BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {
            br = new BufferedReader(new FileReader(path));
            int idx = 0;
            while ((line = br.readLine()) != null) {
            	if(idx > 0) { // bypass line 1 as it is for the headings of the CSV
	                String[] entry = line.split(cvsSplitBy);

	                Hotel hotel = new Hotel(entry[0].trim(),Integer.parseInt(entry[1].trim()),entry[2].trim(),Integer.parseInt(entry[3].trim()));

	                // add hotel to list	
	                hotels.add(hotel);
         	    }
         	    idx++;
            }

            if(cityFilter!=null && cityFilter.trim().length() > 0) {
            	// use Java 8 Lanbda expression to filter the results if a city filter string is provided
            	hotels = hotels.stream().filter(h -> h.getCity().toLowerCase().indexOf(cityFilter.toLowerCase())>-1).collect(Collectors.toList());	
			}

	        if(sortBy!=null && sortBy.equals("city") && ascending!=null && ascending.equals("true")) {
            	// use Java 8 Lanbda expression for comparator for sorting by city name
				Collections.sort(hotels,(hotel1,hotel2)->(hotel1.getCity().toUpperCase().compareTo(hotel2.getCity().toUpperCase())));
	        }

	        if(sortBy!=null && sortBy.equals("city") && ascending!=null && ascending.equals("false")) {
            	// use Java 8 Lanbda expression for comparator for sorting by city name
				Collections.sort(hotels,(hotel1,hotel2)->(hotel2.getCity().toUpperCase().compareTo(hotel1.getCity().toUpperCase())));
	        }

            if(sortBy!=null && sortBy.equals("room") && ascending!=null && ascending.equals("true")) {
                // use Java 8 Lanbda expression for comparator for sorting by room type
                Collections.sort(hotels,(hotel1,hotel2)->(hotel1.getRoom().toUpperCase().compareTo(hotel2.getRoom().toUpperCase())));
            }

            if(sortBy!=null && sortBy.equals("room") && ascending!=null && ascending.equals("false")) {
                // use Java 8 Lanbda expression for comparator for sorting by room type
                Collections.sort(hotels,(hotel1,hotel2)->(hotel2.getRoom().toUpperCase().compareTo(hotel1.getRoom().toUpperCase())));
            }

	        if(sortBy!=null && sortBy.equals("price") && ascending!=null && ascending.equals("true")) {
            	// use Java 8 Lanbda expression for comparator for sorting by price
				Collections.sort(hotels,(hotel1,hotel2)->(hotel1.getPrice() - hotel2.getPrice()));
	        }

	        if(sortBy!=null && sortBy.equals("price") && ascending!=null && ascending.equals("false")) {
            	// use Java 8 Lanbda expression for comparator for sorting by price
				Collections.sort(hotels,(hotel1,hotel2)->(hotel2.getPrice() - hotel1.getPrice()));
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
        return hotels;
	}

    public static String convertToJson(List<Hotel> hotels) {
            Gson gson = new Gson();
            gson = new Gson();

            return gson.toJson(hotels);

    } 
}