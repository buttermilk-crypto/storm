/*
 * Copyright 2017 David R. Smith (@mockumatrix) All Rights Reserved
 */

package com.mockumatrix.storm;

import java.io.File;
import java.util.LinkedHashMap;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("unused")
public class AccountManagerTest {
/*
	@Test
	public void test0() {
		File flatfile = new File("./src/test/resources/storm-test-data.json");
		AccountManager ac = new AccountManager(flatfile);
		ac.load();
		ac.find("testaccount1");
		LinkedHashMap<String, String> map = ac.getSelectedAccount();
		Assert.assertNotNull(map);
		Assert.assertEquals("jlkoui", map.get("accessToken"));
	}

	@Test
	public void test1() {
		File flatfile = new File("./src/test/resources/storm-test-data.json");
		AccountManager ac = new AccountManager(flatfile);
		ac.load();
		String[] accounts = ac.accounts();
		Assert.assertNotNull(accounts.length == 2);

	}
*/
	

	@Test
	public void test2() {
		Display display = new Display();
		Shell shell = new Shell(display);

		AccountEditorDialog dlg = new AccountEditorDialog(shell);
		LinkedHashMap<String, String> input = dlg.open();
		if (input != null) {
			// User clicked OK; set the text into the label
			System.out.println(input);
		}

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
		shell.close();
		
	}

}
