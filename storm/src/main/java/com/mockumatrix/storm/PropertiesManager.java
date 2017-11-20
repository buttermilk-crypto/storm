package com.mockumatrix.storm;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.Rectangle;

import asia.redact.bracket.properties.Properties;
import asia.redact.bracket.properties.impl.PropertiesImpl;
import asia.redact.bracket.properties.io.InputAdapter;
import asia.redact.bracket.properties.io.OutputAdapter;
import asia.redact.bracket.properties.io.PlainOutputFormat;

public class PropertiesManager {

	File stormProps;
	Properties props;
	
	Pattern rectPat = Pattern.compile("Rectangle \\{(\\d+), (\\d+), (\\d+), (\\d+)\\}");

	public PropertiesManager(File stormProperties) {
		super();
		this.stormProps = stormProperties;
		init();
	}
	
	private void init() {
		if(stormProps.exists()) {
			InputAdapter ia = new InputAdapter();
			try {
				ia.readFile(stormProps.getCanonicalFile(), StandardCharsets.UTF_8);
				props = ia.props;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			props = new PropertiesImpl(false).init();
		}
		
	}
	
	public Properties getProperties() {
		return props;
	}
	
	public void save(String key, String value) {
		props.put(key, value);
		OutputAdapter oa = new OutputAdapter(props);
		oa.writeTo(stormProps, new PlainOutputFormat(), StandardCharsets.UTF_8);
	}
	
	public void save() {
		OutputAdapter oa = new OutputAdapter(props);
		oa.writeTo(stormProps, new PlainOutputFormat(), StandardCharsets.UTF_8);
	}
	
	public Rectangle shellSize() {
		String rect = props.get("shell.size", "Rectangle {75, 75, 900, 600}");
		Matcher m = rectPat.matcher(rect);
		if(m.matches()) {
			return new Rectangle(
					Integer.parseInt(m.group(1)),
					Integer.parseInt(m.group(2)), 
					Integer.parseInt(m.group(3)), 
					Integer.parseInt(m.group(4))
			);
		}
		
		throw new RuntimeException("Count not return a Rectangle. Bailing.");
		
	}
	
}
