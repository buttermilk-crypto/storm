/*
 * Copyright 2017 David R. Smith (@mockumatrix) All Rights Reserved
 */

package com.mockumatrix.storm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.twitter.Validator;
import com.vdurmont.emoji.EmojiParser;

/**
 * Prepares the input text, including emoji replacement, and builds a list of StormEntry objects.
 * As a side effect, it validates the formatted entry text.
 * 
 * @author Dave
 *
 */
public class TextUtil {

	public static final ArrayList<StormEntry> prepareStormText(File infile) {

		ArrayList<String> list = new ArrayList<String>();
		ArrayList<StormEntry> collect = new ArrayList<StormEntry>();
		
		File parent = infile.getParentFile();

		try (FileInputStream fin = new FileInputStream(infile);
				InputStreamReader reader = new InputStreamReader(fin);
				BufferedReader breader = new BufferedReader(reader);) {

			String s = null;
			StringBuffer buf = new StringBuffer();
			while( (s = breader.readLine()) != null){
				if(s.trim().equals("")){
					String text = buf.toString().trim();
					if(text.length()==0)continue;
					list.add(text);
					buf = new StringBuffer();
				}else{
					if(s.trim().startsWith("#")) continue;
					buf.append(s);
					if(!Character.isWhitespace(buf.charAt(buf.length()-1))) buf.append(" ");
				}
			}
			
			System.err.println("Found "+list.size()+" tweets.");

			// convert emojicodes into UTF-8 and parse attachments
			ArrayList<StormEntry> tmp = new ArrayList<StormEntry>();
			for (String text : list) {
				String res = replaceEmojiCodes(text);
				tmp.add( removeAttachments(res) );
			}

			
			// format with twitter "thread" semantics
			int count = 0;
			for (StormEntry entry : tmp) {

				String numbered = "";
				if (count == 0)
					numbered = entry.tweetText + " [Thread]";
				else if (count == list.size() - 1) {
					numbered = count + " " + entry.tweetText + " /end";
				} else {
					numbered = count + " " + entry.tweetText;
				}
				
				entry.tweetText = numbered;

				Validator v = new Validator();
				if (v.isValidTweet(numbered)) {
					System.err.println("is valid: " + numbered);
				} else {
					System.err.println("is NOT valid: ["
							+ v.getTweetLength(numbered) + "] " + numbered);
					throw new RuntimeException("Failed, bailing out!");
				}
				
				if(entry.attachmentPaths.size()>0) {
					String p = entry.attachmentPaths.get(0);
					File newFile = new File(parent,p);
					if(!newFile.exists()){
						throw new RuntimeException("File not found: "+p+". Failed, bailing out!");
					}else{
						entry.attachmentPaths.set(0, newFile.getCanonicalPath());
					}
				}
				
				count++;
				collect.add(entry);
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(2); // errors in text should kill us before sending
							// anything
		}

		return collect;

	}

	static String replaceEmojiCodes(String text) {
		if(text == null) return null;
		if(!text.contains(":")) return text;
		return EmojiParser.parseToUnicode(text);
	}
	
	static StormEntry removeAttachments(String text) {
		
		StormEntry s = new StormEntry(text);
		if(text.indexOf("[") == -1) return s; //short circuit, no attachments
		
		StringBuffer sb = new StringBuffer();
		Pattern p = Pattern.compile("\\[([^\\]]+)\\]");
		Matcher m = p.matcher(text);

		while (m.find()) {
			String val = m.group(1);
		//	System.err.println(val);
			if (val != null) {
				m.appendReplacement(sb, "");
				s.attachmentPaths.add(val);
			}
		}
		m.appendTail(sb);
		s.setTweetText(sb.toString());
		return s;
	}

}
