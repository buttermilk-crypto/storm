package com.mockumatrix.storm.formatter;

import com.mockumatrix.storm.StormEntry;

public class StormFormatter {
		
	StormEntry entry;
	int currentIndex;
	int listSize;
	
	boolean usePeriod;
	
	
	public StormFormatter(StormEntry currentItem, int currentIndex, int listSize) {
		super();
		this.entry = currentItem;
		this.currentIndex = currentIndex;
		this.listSize = listSize;
		this.usePeriod = false;
	}
	
	public StormFormatter(StormEntry currentItem, int currentIndex, int listSize, boolean usePeriod) {
		super();
		this.entry = currentItem;
		this.currentIndex = currentIndex;
		this.listSize = listSize;
		this.usePeriod = usePeriod;
	}
	
	public String format() {
		StringBuffer buf = new StringBuffer();
		
		if (currentIndex == 0) {
			buf.append(entry.tweetText); 
			buf.append(" [Thread]");
		} else if (currentIndex == listSize - 1) {
			buf.append( currentIndex); 
			if(usePeriod) buf.append(".");
			buf.append(" "); 
			buf.append(entry.tweetText); 
			buf.append(" /end");
		} else {
			buf.append(currentIndex); 
			if(usePeriod) buf.append(".");
			buf.append(" ");
			buf.append(entry.tweetText);
		}
		
		return buf.toString();
	}
	
}
