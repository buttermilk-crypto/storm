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

import org.eclipse.swt.widgets.Text;

import com.mockumatrix.storm.formatter.EndSlashStormFormatter;
import com.mockumatrix.storm.formatter.PlainStormFormatter;
import com.mockumatrix.storm.formatter.StormFormatter;
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

	public static final ArrayList<StormEntry> prepareStormText(File infile, final Text out, final String formatterKey) {

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
			
			if(buf.length() > 0) list.add(buf.toString());
			
			out.append("Found "+list.size()+" tweets.\n");

			// convert emojicodes into UTF-8 and parse attachments
			ArrayList<StormEntry> tmp = new ArrayList<StormEntry>();
			for (String text : list) {
				String res = replaceEmojiCodes(text);
				tmp.add( removeAttachments(res) );
			}

			NumberingScheme scheme = NumberingScheme.find(formatterKey);
			
			// format with twitter "thread" semantics
			int count = 0;
			for (StormEntry entry : tmp) {
				
				switch(scheme) {
					case DEFAULT: entry.tweetText = new StormFormatter(entry,count,tmp.size()).format(); break;
					case LIST: entry.tweetText = new StormFormatter(entry,count,tmp.size(), true).format(); break;
					case ENDSLASH: entry.tweetText = new EndSlashStormFormatter(entry,count,tmp.size()).format(); break;
					case PLAIN: entry.tweetText = new PlainStormFormatter(entry,count,tmp.size()).format(); break;
					default: entry.tweetText = new StormFormatter(entry,count,tmp.size()).format(); break;
				}
				
				Validator v = new Validator();
				if (v.isValidTweet(entry.tweetText)) {
					out.append("is valid: " + entry.tweetText+"\n");
				} else {
					out.append("is NOT valid: ["
							+ v.getTweetLength(entry.tweetText) + "] " + entry.tweetText+"\n");
					
				}
				
				if(entry.attachmentPaths.size()>0) {
					String p = entry.attachmentPaths.get(0);
					File newFile = new File(parent,p);
					if(!newFile.exists()){
						out.append("File not found: "+p+". This must be fixed!\n");
					}else{
						entry.attachmentPaths.set(0, newFile.getCanonicalPath());
					}
				}
				
				count++;
				collect.add(entry);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null; // errors in text should kill us before sending
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
