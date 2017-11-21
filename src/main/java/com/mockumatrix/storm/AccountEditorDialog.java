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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AccountEditorDialog extends Dialog {

	private List<Text> textControlList = new ArrayList<Text>();

	private LinkedHashMap<String, String> map;

	Display display;

	public AccountEditorDialog(Shell parent) {
		this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.display = parent.getDisplay();
	}
	
	public AccountEditorDialog(Shell parent, LinkedHashMap<String, String> map) {
		this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.display = parent.getDisplay();
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
	
	private void createRow(Shell shell, String name) {
		
		Label label = new Label(shell, SWT.NONE);
		label.setText(name);
		Text text = new Text(shell, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 3, 1));
		textControlList.add(text);
	}

	private void createContents(final Shell shell) {

		shell.setLayout(new GridLayout(4, true));

		createRow(shell, "Account");
		createRow(shell, "AccessToken");
		createRow(shell, "AccessTokenSecret");
		createRow(shell, "ConsumerKey");
		createRow(shell, "ConsumerSecret");
		
		if(map.size() > 0) {
			textControlList.get(0).setText(map.get("account"));
			textControlList.get(1).setText(map.get("accessToken"));
			textControlList.get(2).setText(map.get("accessTokenSecret"));
			textControlList.get(3).setText(map.get("consumerKey"));
			textControlList.get(4).setText(map.get("consumerSecret"));
		}
		
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
	

		Button ok = new Button(shell, SWT.PUSH);
		ok.setText("OK");
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		ok.setLayoutData(data);
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

		Button cancel = new Button(shell, SWT.PUSH);
		cancel.setText("Cancel");
		data = new GridData(GridData.FILL_HORIZONTAL);
		cancel.setLayoutData(data);
		cancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				map = null;
				shell.close();
			}
		});

		shell.setDefaultButton(ok);
	}
}
