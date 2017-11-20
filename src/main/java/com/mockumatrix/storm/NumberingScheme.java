/*
 * Copyright 2017 David R. Smith (@mockumatrix) All Rights Reserved
 */

package com.mockumatrix.storm;

public enum NumberingScheme {
	
	DEFAULT("1-x, /end"), PLAIN("None");
	
	final String description;
	
	NumberingScheme(String desc){
		description = desc;
	}
	
	public static String [] titles() {
		String [] titles = new String[values().length];
		int i = 0;
		for(NumberingScheme n: values()) {
			titles[i] = n.name()+" "+n.description;
			i++;
		}
		
		return titles;
	}
}
