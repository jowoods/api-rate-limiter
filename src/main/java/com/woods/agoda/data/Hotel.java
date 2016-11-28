package com.woods.agoda.data;

import java.util.Comparator;

public class Hotel {

	private String city;
	private int hotelId;
	private String room;
	private int price;

	public Hotel(String city, int hotelId, String room, int price) {
		this.city = city;
		this.hotelId = hotelId;
		this.room = room;
		this.price = price;
	}

	public String getCity() {
		return this.city;
	}

	public int getHotelId() {
		return this.hotelId;
	}

	public String getRoom() {
		return this.room;
	}

	public int getPrice() {
		return this.price;
	}

	public static Comparator<Hotel> HotelCityComparator = new Comparator<Hotel>() {
	    public int compare(Hotel hotel1, Hotel hotel2) {

	      String hotelCity1 = hotel1.getCity().toUpperCase();
	      String hotelCity2 = hotel2.getCity().toUpperCase();

	      //ascending order
	      return hotelCity1.compareTo(hotelCity2);

	      //descending order
	      // return hotelCity2.compareTo(hotelCity1);
	    }
	};	
}