package com.mockumatrix.storm.formatter;

import com.mockumatrix.storm.StormEntry;

public class PlainStormFormatter extends StormFormatter {

	public PlainStormFormatter(StormEntry currentItem, int currentIndex, int listSize) {
		super(currentItem, currentIndex, listSize);
	}
	
	public String format() {
		return entry.tweetText; 
	}

}
