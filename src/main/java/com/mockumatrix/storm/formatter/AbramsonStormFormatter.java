package com.mockumatrix.storm.formatter;

import com.mockumatrix.storm.StormEntry;

public class AbramsonStormFormatter {
		
	StormEntry entry;
	int currentIndex;
	int listSize;
	
	public AbramsonStormFormatter(StormEntry currentItem, int currentIndex, int listSize) {
		super();
		this.entry = currentItem;
		this.currentIndex = currentIndex;
		this.listSize = listSize;
		
	}
	
	public String format() {
		StringBuffer buf = new StringBuffer();
		
		int c = currentIndex+1;
		
		if (currentIndex == listSize - 1) {
			buf.append(c); 
			buf.append("/ "); 
			buf.append(entry.tweetText); 
			buf.append(" /end");
		} else {
			buf.append(c); 
			buf.append("/ ");
			buf.append(entry.tweetText);
		}
		
		return buf.toString();
	}
	
}
