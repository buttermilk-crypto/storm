/*
 * Copyright 2017 David R. Smith (@mockumatrix) All Rights Reserved
 */

package com.mockumatrix.storm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Manage a json-encoded config file containing various account OAuth keys for use on twitter.
 * 
 * @author Dave
 *
 */
public class AccountManager {
	
	ObjectMapper mapper;
	File accountFile; //storm.json
	
	List<LinkedHashMap<String,String>> list; // parsed from storm.json data
	
	LinkedHashMap<String,String> selectedAccount; // this is set as a side-effect of find()

	public AccountManager(File accountFile) {
		super();
		this.accountFile = accountFile;
		mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		if(!accountFile.exists()) {
			initialize();
		}
	}
	
	private void initialize() {
		if(list == null) {
			list = new ArrayList<LinkedHashMap<String,String>>();
		}
		LinkedHashMap<String,String> m = new LinkedHashMap<String,String>();
		m.put("account", "dummy account name (update me)");
		m.put("accessToken", "access token goes here");
		m.put("accessTokenSecret", "access token secret goes here");
		m.put("consumerKey", "consumer key value goes here");
		m.put("consumerSecret", "consumer secret goes here");
		
		list.add(m);
		this.save();
		
	}
	
	@SuppressWarnings("unchecked")
	public void load() {
		
		try {
			list = mapper.readValue(accountFile, ArrayList.class);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void save() {
		try {
			mapper.writeValue(accountFile, list);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Works by side-effect - sets selectedAccount with what we find. 
	 * 
	 * @param accountName
	 * @return
	 */
	public void find(String accountName) {
		for(LinkedHashMap<String,String> hm : list) {
			String val = hm.get("account");
			val = val.toLowerCase();
			if(accountName.toLowerCase().equals(val)) {
				selectedAccount = hm;
				return;
			}
		}
		
		throw new RuntimeException("Count not find that account: "+accountName);
	}
	
	public String [] accounts() {
		if(list == null) return new String[0];
		String [] accounts = new String[list.size()];
		int i = 0;
		for(LinkedHashMap<String,String> hm : list) {
			String val = hm.get("account");
			val = val.toLowerCase();
			accounts[i] = val;
			i++;
		}
		return accounts;
	}

	public LinkedHashMap<String, String> getSelectedAccount() {
		return selectedAccount;
	}
	
	

}
