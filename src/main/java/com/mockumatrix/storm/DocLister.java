package com.mockumatrix.storm;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;


public class DocLister {

	public static void main(String[] args) {
	
		File folder = new File("C:/Users/Dave/Desktop/tweet-storms");
		File [] files = folder.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				if(file.getName().endsWith(".json")) return true;
				return false;
			}
			
		});
		
		Arrays.sort(files, new Comparator<File>() {

			@Override
			public int compare(File arg0, File arg1) {
				if(arg0.lastModified()<arg1.lastModified()) return -1;
				else if(arg0.lastModified()==arg1.lastModified()) return 0;
				else  return 1;
			}
			
		});
		
		for(File f: files) {
			if(!f.exists()) continue;
			
			// storm frame needs the base file name
			File basefile = new File(f.getParentFile(), f.getName().substring(0, f.getName().length()-5));
			StormFrame frame = new StormFrame(basefile,  new ArrayList<StormEntry>());
			frame.load();
			
			// load top item
			StormEntry se = frame.entries.get(0);
			long id = se.getTweetId();
			System.err.println(se.getTweetText());
			System.err.println("https://twitter.com/mockumatrix/status/"+String.valueOf(id));
			System.err.println("");
		}
	}

}
