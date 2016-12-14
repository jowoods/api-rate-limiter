package com.woods.agoda;

import com.woods.agoda.data.Hotel;
import java.util.List;

public interface IHotelUtil{
	public List<Hotel> retrieveHotels(String path, String cityFilter ,String sortBy, String ascending);
} 
