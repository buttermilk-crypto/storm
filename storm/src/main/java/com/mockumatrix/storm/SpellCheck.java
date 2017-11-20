package com.mockumatrix.storm;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;


import org.eclipse.swt.widgets.Text;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.RuleMatch;

public class SpellCheck implements Runnable {
	
	final JLanguageTool langTool;
	final Text validationOutputText;
	final String input;
	
	final String [] ignoreWordsIfContains = {
		"jpg", "png", "@", "http://", "https://", ".com"	
	};
	
	public SpellCheck( JLanguageTool langTool, Text validationOutputText, String input) {
		super();

		this.langTool = langTool;
		this.validationOutputText = validationOutputText;
		this.input = input;
	}

	@Override
	public void run() {
		
		try {
				
			List<RuleMatch> matches = langTool.check(input);
			if (matches == null) {
				validationOutputText.append("Looks good.\n");
				return;
			}
	
		start:	for (RuleMatch match : matches) {
			
				// ignore this message about smart quotes
				if(match.getMessage().contains("smart")) continue;
				// ignore this message about whitespace
				if(match.getMessage().contains("you repeated a whitespace")) continue;
				
				// words which spell checker gives false-positive
				String word = input.substring(match.getFromPos(), match.getToPos());
				
				for(String item: this.ignoreWordsIfContains) {
					if(word.contains(item)) continue start;
				}
				// if start with capital, assume it is a name, etc.
				char ch = word.charAt(0);
				if(Character.isUpperCase(ch)) {
					continue;
				}
				validationOutputText.append(match.getMessage()+": "+input.substring(match.getFromPos(), match.getToPos()) +"\n");
				List<String> replace = match.getSuggestedReplacements();
				if(replace.size()>0) validationOutputText.append(replace.toString()+"\n");
			}
			
		}catch(IOException x) {
			StringWriter w = new StringWriter();
			PrintWriter s = new PrintWriter(w);
			x.printStackTrace(s);
			validationOutputText.append(w.toString()+"\n");
		}
		
	}

}
