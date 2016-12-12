package com.woods.agoda;

import org.junit.Test;
import com.woods.agoda.data.*;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class HotelUtilTest {

  @Test
  public void testSizeOfCityFilteredHotels() {
   IHotelUtil hotelUtil = new HotelUtil(); 
   List<Hotel> hotels = hotelUtil.retrieveHotels("C:\\projects\\heroku\\springbootselfex\\test_hoteldb.csv", "Bangkok" ,null, null);
   assertEquals(7,hotels.size());  
  }

  @Test
  public void testFilteredHotelsOnlyIncludesCity() {
   IHotelUtil hotelUtil = new HotelUtil(); 
   List<Hotel> hotels = hotelUtil.retrieveHotels("C:\\projects\\heroku\\springbootselfex\\test_hoteldb.csv", "Bangkok" ,null, null);
   boolean includesOnlyFilteredCity = true;
   for(Hotel hotel: hotels) {
      if(hotel.getCity().trim().toLowerCase().indexOf("bangkok") < 0) {
        includesOnlyFilteredCity = false;
      }
   }
   assertTrue( hotels.size()> 0 && includesOnlyFilteredCity);  
  }

  @Test
  public void testListDescendingPriceOrder() {
   IHotelUtil hotelUtil = new HotelUtil(); 

   List<Hotel> hotels = hotelUtil.retrieveHotels("C:\\projects\\heroku\\springbootselfex\\test_hoteldb.csv", "Bangkok" ,"price", "false");

   assertTrue( hotels.size()>= 7 && hotels.get(3).getPrice() < hotels.get(2).getPrice() && hotels.get(2).getPrice() < hotels.get(1).getPrice() && hotels.get(1).getPrice() < hotels.get(0).getPrice());  
  }


}
