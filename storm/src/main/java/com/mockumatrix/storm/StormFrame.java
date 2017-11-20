package com.mockumatrix.storm;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


/**
 * <p>This is a "frame" on which to hang tweet text and other data. This frame
 * will be serialized and if things stop in mid-storm, we can restart where
 * we left off. This means among other things, our message sends will be idempotent.</p> 
 * 
 * <p>The input format is a plain text file with a line between the entries.<p/>
 * 
 * <p>For an image attachment, use [./path/to/file.jpg] format where "." is the directory
 * where the text file being parsed is located. Up to 4 attachments are possible</p>
 * 
 * <p>The location of the input file will become the location of the base directory of attachments
 * and also where we save the frame file itself. You can think of it like a base project
 * folder.</p>
 * 
 * <p>The base folder can have a properties file, called storm.properties</p>
 * 
 * @author Dave
 *
 */
public class StormFrame {

	File inputFile;
	File baseFolder;
	
	List<StormEntry> entries;

	// define the baseFolder based on the location of the path to the frame file.
	// assume entries have been pre-validated
	public StormFrame(File inputFile, List<StormEntry> entries) {
		super();
		this.inputFile = inputFile;
		baseFolder = inputFile.getParentFile();
		this.entries = entries;
		//entries must be set
		if(this.entries == null) throw new RuntimeException("Please set entries, programming error not to.");
	}
	
	public void load() {
		//locate our file and check it exists
		String name = inputFile.getName();
		File frameFile = new File(baseFolder, name+".json");
		if(!frameFile.exists()){
			throw new RuntimeException("attempting to load frame, but file not found.");
		}
		
		// load the contents
		ObjectMapper mapper = new ObjectMapper();
		try {
			@SuppressWarnings("unchecked")
			ArrayList<LinkedHashMap<String,?>> list = mapper.readValue(frameFile, ArrayList.class);
			for(LinkedHashMap<String,?> lhm: list){
				addEntry(lhm);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	private void addEntry(LinkedHashMap<String,?> lhm){
		Integer tweetId = (Integer) lhm.get("tweetId");
		String tweetText = (String) lhm.get("tweetText");
		@SuppressWarnings("unchecked")
		ArrayList<String> attachments = (ArrayList<String>) lhm.get("attachmentPaths");
		StormEntry e = new StormEntry(tweetId,tweetText,attachments);
		this.entries.add(e);
	}
	
	/**
	 * save entries to file called "inputFile".json
	 */
	public void save() {
		
		if(entries.size() == 0) {
			throw new RuntimeException("Sorry, no entries. No point in proceeding.");
		}
		
		// ok, now save as a frame in same folder
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT); // pretty print
		StringWriter writer = new StringWriter();
		try {
			mapper.writeValue(writer, entries);
			String name = inputFile.getName();
			File frame = new File(baseFolder, name+".json");
			Files.write(frame.toPath(), writer.toString().getBytes("UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}



