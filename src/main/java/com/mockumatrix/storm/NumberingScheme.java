/*
 * Copyright 2017 David R. Smith (@mockumatrix) All Rights Reserved
 */

package com.mockumatrix.storm;

public enum NumberingScheme {
	
	DEFAULT("x, /end"), LIST("x., /end"), ENDSLASH("x/, x/x"), Abramson("x/, /end"), PLAIN("None");
	
	final String description;
	
	NumberingScheme(String desc){
		description = desc;
	}
	
	public static NumberingScheme find(String val) {
		for(NumberingScheme n: values()) {
			String token = n.name()+" "+n.description;
			if(token.equals(val)) return n;
		}
		throw new RuntimeException("Cound not find: "+val);
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
