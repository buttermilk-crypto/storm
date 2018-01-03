package com.mockumatrix.storm;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import org.junit.Assert;

public class TextUtilTest {

	@Test
	public  void test0() {
		File infile = new File("./src/test/resources/dash-test.txt");
		try {
			String [] array = TextUtil.parseOnDashes(infile);
			Assert.assertTrue(array.length == 3);
			
			System.err.println(array[0]);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
