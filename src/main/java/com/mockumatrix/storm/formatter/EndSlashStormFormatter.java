package com.mockumatrix.storm.formatter;

import com.mockumatrix.storm.StormEntry;

public class EndSlashStormFormatter {
		
	StormEntry entry;
	int currentIndex;
	int listSize;
	
	
	public EndSlashStormFormatter(StormEntry currentItem, int currentIndex, int listSize) {
		super();
		this.entry = currentItem;
		this.currentIndex = currentIndex;
		this.listSize = listSize;
	}
	
	public String format() {
		StringBuffer buf = new StringBuffer();
		
		int c = currentIndex+1;
		
		 if (currentIndex == listSize - 1) {
			buf.append(entry.tweetText); 
			buf.append(" ");
			buf.append( c); 
			buf.append("/");
			buf.append(c);
			
		} else {
			buf.append(entry.tweetText); 
			buf.append(" ");
			buf.append( c); 
			buf.append("/");
		}
		
		return buf.toString();
	}
	
}
