/*
 * Copyright 2017 David R. Smith (@mockumatrix) All Rights Reserved
 */

package com.mockumatrix.storm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AccountEditorDialog extends Dialog {

	private List<Text> textControlList = new ArrayList<Text>();

	private LinkedHashMap<String, String> map;
	
	private Group accountGroup, buttonGroup;

	//Display display;

	public AccountEditorDialog(Shell parent) {
		this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		//this.display = parent.getDisplay();
	}
	
	public AccountEditorDialog(Shell parent, LinkedHashMap<String, String> map) {
		this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		//this.display = parent.getDisplay();
		this.map = map;
	}

	private AccountEditorDialog(Shell parent, int style) {
		super(parent, style);
		setText("Twitter OAuth Account Dialog");
	}

	public LinkedHashMap<String, String> open() {
		if(map == null) {
			map = new LinkedHashMap<String,String>();
		}
		Shell shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		shell.setLayout(new GridLayout(1, false));
		createContents(shell);
		shell.pack();
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return map;
	}
	
	private void createRow(Group group, String name) {
		
		Label label = new Label(group, SWT.NONE);
		label.setText(name);
		Text text = new Text(group, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 4, 1));
		textControlList.add(text);
	}

	private void createContents(final Shell shell) {
		
		accountGroup = new Group(shell, SWT.NONE);
		GridData gd0 = new GridData();
		gd0.grabExcessHorizontalSpace = true;
		accountGroup.setLayoutData(gd0);
		accountGroup.setText("Account OAuth Data (From Twitter)");

		accountGroup.setLayout(new GridLayout(5, true));

		createRow(accountGroup, "Account");
		createRow(accountGroup, "AccessToken");
		createRow(accountGroup, "AccessTokenSecret");
		createRow(accountGroup, "ConsumerKey");
		createRow(accountGroup, "ConsumerSecret");
		
		if(map.size() > 0) {
			textControlList.get(0).setText(map.get("account"));
			textControlList.get(1).setText(map.get("accessToken"));
			textControlList.get(2).setText(map.get("accessTokenSecret"));
			textControlList.get(3).setText(map.get("consumerKey"));
			textControlList.get(4).setText(map.get("consumerSecret"));
		}
		
		
		buttonGroup = new Group(shell, SWT.NONE);
		gd0 = new GridData();
		gd0.grabExcessHorizontalSpace = true;
		gd0.horizontalSpan = 2;
		gd0.horizontalAlignment = GridData.FILL;
		buttonGroup.setLayoutData(gd0);
		buttonGroup.setText("");
		buttonGroup.setLayout(new GridLayout(6, true));
		
		Button test = new Button(buttonGroup, SWT.PUSH);
		test.setText("Test");
		test.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, true, 1, 1));
		test.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				
			}
		});
		
		
		new Label(buttonGroup, SWT.NONE);
		new Label(buttonGroup, SWT.NONE);
		new Label(buttonGroup, SWT.NONE);
	
		Button cancel = new Button(buttonGroup, SWT.PUSH);
		cancel.setText("Cancel");
		cancel.setLayoutData(new GridData(SWT.FILL, SWT.RIGHT, true, true, 1, 1));
		cancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				map = null;
				shell.close();
			}
		});

		Button ok = new Button(buttonGroup, SWT.PUSH);
		ok.setText("OK");
		ok.setLayoutData(new GridData(SWT.FILL, SWT.RIGHT, true, true, 1, 1));
		ok.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				String account = textControlList.get(0).getText();
				map.put("account", account);
				String accessToken = textControlList.get(1).getText();
				map.put("accessToken", accessToken);
				String accessTokenSecret = textControlList.get(2).getText();
				map.put("accessTokenSecret", accessTokenSecret);
				String consumerKey = textControlList.get(3).getText();
				map.put("consumerKey", consumerKey);
				String consumerSecret = textControlList.get(4).getText();
				map.put("consumerSecret", consumerSecret);
				shell.close();
			}
		});

		
		shell.setDefaultButton(ok);
	}
}
