/*
 * Copyright 2017 David R. Smith (@mockumatrix) All Rights Reserved
 */

package com.mockumatrix.storm;

public enum NumberingScheme {
	
	DEFAULT("1-x, /end"), LIST("1.-x, /end"), ENDSLASH("1/ x/x"), PLAIN("None");
	
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
