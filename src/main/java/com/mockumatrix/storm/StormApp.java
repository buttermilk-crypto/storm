/*
 * Copyright 2017 David R. Smith (@mockumatrix) All Rights Reserved
 */

package com.mockumatrix.storm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class StormApp {

	private static ResourceBundle resourceBundle = ResourceBundle.getBundle("storm_ui");

	private JLanguageTool langTool = new JLanguageTool(new AmericanEnglish());

	private Display display;
	private Shell shell;

	private boolean flag;

	Combo combo1;
	Combo numberSchemeCombo;
	Group accountsGroup, numberingSchemeGroup, settingsGroup, fileDialogGroup, outputGroup, buttonGroup;

	FileDialog fileDialog;
	Text inputFileText;
	Text outputText;
	Button pinToProfileButton;

	AccountManager accountManager;
	PropertiesManager propsManager;

	// used to hold reference for disposal when program closes
	final List<Image> imageList = new ArrayList<Image>();
	
	FrameSender sender;
	Thread senderThread;

	public StormApp() {
		super();

		// looking for storm properties
		File userDir = new File(System.getProperty("user.dir"));
		File stormAccountsFile = new File(userDir, "storm.json");
		File stormProps = new File(userDir, "storm.properties");

		accountManager = new AccountManager(stormAccountsFile);
		propsManager = new PropertiesManager(stormProps);

		if (!stormAccountsFile.exists()) {
			System.err.println("Did not find any accounts to load...");
		} else {
			accountManager.load();
		}

	}

	public static void main(String[] args) {

		Display display = new Display();
		StormApp application = new StormApp();
		Shell shell = application.open(display);

		// monitor resize
		shell.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				
				Rectangle rect = shell.getBounds();
				System.err.println(rect);
				if(rect.x < 0) return;
				if(rect.y < 0) return;
				if(rect.height < 0) return;
				if(rect.width < 0) return;
				application.propsManager.getProperties().put("shell.size", rect.toString());

			}
		});

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		application.close();
		display.dispose();

	}

	/**
	 * Opens the main program.
	 */
	public Shell open(Display display) {
		this.display = display;
		shell = new Shell();

		if (!propsManager.getProperties().containsKey("shell.size")) {

			Monitor primary = display.getPrimaryMonitor();
			Rectangle bounds = primary.getBounds();
			Rectangle rect = shell.getBounds();

			int x = bounds.x + (bounds.width - rect.width) / 2;
			int y = bounds.y + (bounds.height - rect.height) / 2;

			shell.setLocation(x, y);

			propsManager.save("shell.size", rect.toString());

		} else {

			// restore initial location from props.
			Monitor primary = display.getPrimaryMonitor();
			Rectangle bounds = primary.getBounds();
			Rectangle rect = propsManager.shellSize();
			shell.setBounds(rect);

			int x = bounds.x + (bounds.width - rect.width) / 2;
			int y = bounds.y + (bounds.height - rect.height) / 2;

			shell.setLocation(x, y);
		}

		createShellContents();
		shell.open();
		return shell;
	}

	/**
	 * Closes the main program.
	 */
	void close() {
		for (Image img : imageList) {
			img.dispose();
		}
		// save props - note that app gui already destroyed by this point
		propsManager.save();
	}

	/**
	 * Construct the UI
	 * 
	 * @param container
	 *            the ShellContainer managing the Shell we are rendering inside
	 */
	private void createShellContents() {

		shell.setText(getResourceString("window.title", new Object[] { "" }));
		// shell.setImage(iconCache.stockImages[iconCache.shellIcon]);
		Menu bar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(bar);
		createFileMenu(bar);
		// createHelpMenu(bar);

		GridLayout layout = new GridLayout(2, false);

		shell.setLayout(layout);

		createAccountGroup();
		createNumberingSchemeGroup();
		createSettingsGroup();
		createFileDialogGroup();
		createOutputGroup();
		createButtons();

	}

	private void createAccountGroup() {

		/* Create a group for the combo box */
		accountsGroup = new Group(shell, SWT.NONE);
		GridData gd0 = new GridData();
		gd0.grabExcessHorizontalSpace = true;
		gd0.horizontalAlignment = GridData.FILL;
		accountsGroup.setLayoutData(gd0);
		accountsGroup.setText("Accounts");
		accountsGroup.setLayout(new RowLayout());

		String[] listData = accountManager.accounts();
		/* Create the example widgets */
		combo1 = new Combo(accountsGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo1.setItems(listData);

		String acctSelPrev = propsManager.getProperties().get("account.selected", "");
		if (!acctSelPrev.equals("")) {
			int count = combo1.getItemCount();
			for (int i = 0; i < count; i++) {
				if (acctSelPrev.equals(combo1.getItem(i))) {
					combo1.select(i);
				}
			}
		}

		combo1.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				String accountName = combo1.getText();
				accountManager.find(accountName); // side effect is to set selectedAccount in the AccountManager.
				propsManager.getProperties().put("account.selected", accountName); // save state
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		Button button1 = new Button(accountsGroup, SWT.PUSH);
		InputStream in = this.getClass().getResourceAsStream("/com/mockumatrix/storm/add.png");
		Image add = new Image(display, in);
		imageList.add(add);
		button1.setImage(add);
		button1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AccountEditorDialog dlg = new AccountEditorDialog(shell);
				LinkedHashMap<String, String> input = dlg.open();
				if (input != null) {
					if (input.size() > 0) {
						accountManager.add(input);
						accountManager.save();
						String[] listData = accountManager.accounts();
						combo1.setItems(listData);
						propsManager.getProperties().put("account.selected", input.get("account"));
						String acctSelPrev = propsManager.getProperties().get("account.selected", "");
						if (!acctSelPrev.equals("")) {
							int count = combo1.getItemCount();
							for (int i = 0; i < count; i++) {
								if (acctSelPrev.equals(combo1.getItem(i))) {
									combo1.select(i);
								}
							}
						}
					}
				}

			}
		});

		Button button2 = new Button(accountsGroup, SWT.PUSH);
		in = this.getClass().getResourceAsStream("/com/mockumatrix/storm/delete.png");
		Image del = new Image(display, in);
		button2.setImage(del);
		imageList.add(del);
		button2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String selected = combo1.getText();
				accountManager.delete(selected);
				propsManager.getProperties().deleteKey("account.selected");
				String[] listData = accountManager.accounts();
				combo1.setItems(listData);
			}
		});

		Button button3 = new Button(accountsGroup, SWT.PUSH);
		in = this.getClass().getResourceAsStream("/com/mockumatrix/storm/edit.png");
		Image edit = new Image(display, in);
		button3.setImage(edit);
		imageList.add(edit);
		button3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String selected = combo1.getText();
				accountManager.find(selected);
				AccountEditorDialog dlg = new AccountEditorDialog(shell, accountManager.getSelectedAccount());
				LinkedHashMap<String, String> input = dlg.open();
				if (input != null) {
					accountManager.save();
					String[] listData = accountManager.accounts();
					combo1.setItems(listData);
					propsManager.getProperties().put("account.selected", input.get("account"));
					String acctSelPrev = propsManager.getProperties().get("account.selected", "");
					if (!acctSelPrev.equals("")) {
						int count = combo1.getItemCount();
						for (int i = 0; i < count; i++) {
							if (acctSelPrev.equals(combo1.getItem(i))) {
								combo1.select(i);
							}
						}
					}
				}
			}
		});

	}

	private void createNumberingSchemeGroup() {

		/* Create a group for the combo box */
		numberingSchemeGroup = new Group(shell, SWT.NONE);
		GridData gd0 = new GridData();
		gd0.grabExcessHorizontalSpace = true;
		gd0.horizontalAlignment = GridData.FILL;
		numberingSchemeGroup.setLayoutData(gd0);
		numberingSchemeGroup.setText("Numbering Scheme");
		numberingSchemeGroup.setLayout(new RowLayout());

		/* Create the example widgets */
		numberSchemeCombo = new Combo(numberingSchemeGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		numberSchemeCombo.setItems(NumberingScheme.titles());

		String schemePrev = propsManager.getProperties().get("numberscheme.selected", "");
		if (!schemePrev.equals("")) {
			int count = numberSchemeCombo.getItemCount();
			for (int i = 0; i < count; i++) {
				if (schemePrev.equals(numberSchemeCombo.getItem(i))) {
					numberSchemeCombo.select(i);
				}
			}
		}

		numberSchemeCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				propsManager.getProperties().put("numberscheme.selected", numberSchemeCombo.getText()); // save state
				System.out.println(numberSchemeCombo.getText());
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

	}

	private void createSettingsGroup() {

		settingsGroup = new Group(shell, SWT.NONE);
		GridData gd0 = new GridData();
		gd0.grabExcessHorizontalSpace = true;
		gd0.horizontalSpan = 2;
		gd0.horizontalAlignment = GridData.FILL;
		settingsGroup.setLayoutData(gd0);
		settingsGroup.setText("Misc. Settings");
		settingsGroup.setLayout(new GridLayout(8, false));

		pinToProfileButton = new Button(settingsGroup, SWT.CHECK);

		pinToProfileButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		pinToProfileButton.setText("Pin to Profile");

		String check = propsManager.getProperties().get("pin.to.profile.selected", "");
		if (Boolean.valueOf(check).booleanValue()) {
			pinToProfileButton.setSelection(true);
		} else {
			pinToProfileButton.setSelection(false);
		}

		pinToProfileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				propsManager.getProperties().put("pin.to.profile.selected",
						String.valueOf(pinToProfileButton.getSelection()));
			}
		});

	}

	private void createFileDialogGroup() {

		fileDialogGroup = new Group(shell, SWT.NONE);
		GridData gd0 = new GridData();
		gd0.grabExcessHorizontalSpace = true;
		gd0.horizontalSpan = 2;
		gd0.horizontalAlignment = GridData.FILL;
		fileDialogGroup.setLayoutData(gd0);
		fileDialogGroup.setText("Path To Input File");
		fileDialogGroup.setLayout(new GridLayout(8, false));

		Button button1 = new Button(fileDialogGroup, SWT.PUSH);

		button1.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		button1.setText("File...");
		button1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fileDialog = new FileDialog(shell, SWT.OPEN);
				String[] filterNames = new String[] { "Text Files", "All Files (*)" };
				String[] filterExtensions = new String[] { "*.txt;*.text", "*" };
				String filterPath = System.getProperty("user.home");

				fileDialog.setFilterNames(filterNames);
				fileDialog.setFilterExtensions(filterExtensions);
				fileDialog.setFilterPath(filterPath);
				// fileDialog.setFileName ("myfile");
				String result = fileDialog.open();
				if (result != null) {
					inputFileText.setText(result);
					propsManager.getProperties().put("file.selected", result);
				}
			}
		});

		inputFileText = new Text(fileDialogGroup, SWT.SINGLE | SWT.BORDER);
		inputFileText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 7, 1));
		String lastFileSelected = propsManager.getProperties().get("file.selected", "");
		if (!lastFileSelected.equals("")) {
			inputFileText.setText(lastFileSelected);
		}
	}

	private void createOutputGroup() {
		outputGroup = new Group(shell, SWT.NONE);
		outputGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		outputGroup.setText("Validation Output");
		outputGroup.setLayout(new GridLayout(1, false));
		outputText = new Text(outputGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

		outputText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

	}

	private void createButtons() {

		buttonGroup = new Group(shell, SWT.NONE);
		GridData gd0 = new GridData();
		gd0.grabExcessHorizontalSpace = true;
		gd0.horizontalSpan = 2;
		gd0.horizontalAlignment = GridData.FILL;
		buttonGroup.setLayoutData(gd0);
		buttonGroup.setText("");
		buttonGroup.setLayout(new GridLayout(6, true));
		new Label(buttonGroup, SWT.NONE);
		new Label(buttonGroup, SWT.NONE);
	//	new Label(buttonGroup, SWT.NONE);
		// new Label(buttonGroup, SWT.NONE);

		Button button0 = new Button(buttonGroup, SWT.PUSH);
		button0.setLayoutData(new GridData(SWT.FILL, SWT.RIGHT, true, true, 1, 1));
		button0.setText("Spell Check Only");
		button0.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// need file to be set
				String inputFilePath = inputFileText.getText();
				if (inputFilePath == null || inputFilePath.trim().equals("")) {
					outputText.setText("Please select an input file");
					return;
				}

				File inputFile = new File(inputFilePath);
				if (!inputFile.exists()) {
					outputText.setText("This file does not exist: " + inputFilePath);
					return;
				}
				// do spell check
				runSpellCheck(inputFile);

			}
		});

		Button button1 = new Button(buttonGroup, SWT.PUSH);
		button1.setLayoutData(new GridData(SWT.FILL, SWT.RIGHT, true, true, 1, 1));
		button1.setText("Validate");
		button1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// need file to be set
				String inputFilePath = inputFileText.getText();
				if (inputFilePath == null || inputFilePath.trim().equals("")) {
					outputText.setText("Please select an input file");
					return;
				}

				File inputFile = new File(inputFilePath);
				if (!inputFile.exists()) {
					outputText.setText("This file does not exist: " + inputFilePath);
					return;
				}

				runValidation(inputFile);
			}
		});

		Button button2 = new Button(buttonGroup, SWT.PUSH);
		button2.setLayoutData(new GridData(SWT.FILL, SWT.RIGHT, true, true, 1, 1));
		button2.setText("Send");
		button2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
				dialog.setText("Send Tweet Storm");
				dialog.setMessage("Do you really want to do this?");

				// open dialog and await user selection
				int returnCode = dialog.open();
				if (returnCode == SWT.OK) {
					File inputFile = new File(inputFileText.getText().trim());
					if (!inputFile.exists()) {
						try {
							outputText.append("The input file doesn't exist: " + inputFile.getCanonicalPath());
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						return;
					}

					StormFrame frame = null;

					// see if frame already exists
					File frameFile = new File(inputFile.getParentFile(), inputFile.getName() + ".json");
					
					if (frameFile.exists()) {

						// load it
						frame = new StormFrame(inputFile, new ArrayList<StormEntry>());
						frame.load();
						
					} else {

						// no existing frame
						// validate - this will cause program to exit if anything is invalid
						String format = numberSchemeCombo.getText();
						ArrayList<StormEntry> entries = TextUtil.prepareStormText(inputFile, outputText, format);
						frame = new StormFrame(inputFile, entries);
						// not saving yet
					}

					accountManager.find(combo1.getText()); // side effect is to set currently selected account internally
					
					if(senderThread == null) {
						sender = new FrameSender(display, frame, accountManager, propsManager, outputText);
						sender.setPinToProfile(pinToProfileButton.getSelection());
						sender.configure();
						senderThread = new Thread(sender);
						senderThread.start();
					}else {
						if(senderThread.isAlive()) {
							
							dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
							dialog.setText("Sender Thread");
							dialog.setMessage("There is a running sender thread. Do you want to kill it?");

							returnCode = dialog.open();
							if (returnCode == SWT.OK) {
								try {
									if(sender != null) {
									  sender.stop();
									  senderThread.interrupt();
									  senderThread = null;
								//	  outputText.append("The sending thread was alive has been sent stop notice.\n");
									}
								}catch(Exception x) {
									x.printStackTrace();
								}
							}else {
								
							}
						}else {
						//	 outputText.append("The sending thread was not alive has been sent stop notice.\n");
							senderThread.interrupt();
							senderThread = null;
						}
					}

				} else {
					// do nothing
				}
			}
		});
		
		Button button3 = new Button(buttonGroup, SWT.PUSH);
		button3.setLayoutData(new GridData(SWT.FILL, SWT.RIGHT, true, true, 1, 1));
		button3.setText("Stop");
		button3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(sender != null) {
					 sender.stop();
					if(senderThread != null && senderThread.isAlive()) {
					  senderThread.interrupt();
					//  outputText.append("The sending thread has been sent stop notice.\n");
					}
				}else {
					outputText.append("Nothing to do.\n");
				}
			}
		});

	}

	private void runValidation(File inputFile) {

		// runs validation, will fail as side effect if required
		this.outputText.setText("");
		ArrayList<StormEntry> list = TextUtil.prepareStormText(inputFile, this.outputText, numberSchemeCombo.getText());

		if (list.size() == 0) {
			outputText.setText("The input text contained no strings, bailing out");
			return;
		}
	}

	private void createFileMenu(Menu parent) {
		Menu menu = new Menu(parent);
		MenuItem header = new MenuItem(parent, SWT.CASCADE);
		header.setText(getResourceString("menu.file.text"));
		header.setMenu(menu);

		final MenuItem simulateItem = new MenuItem(menu, SWT.CHECK);
		simulateItem.setText(getResourceString("menu.file.flag.text"));
		simulateItem.setSelection(flag);
		simulateItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				flag = simulateItem.getSelection();
			}
		});

		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText(getResourceString("menu.file.close.text"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
	}

	private void runSpellCheck(File textFile) {

		try {
			outputText.setText(
					"Validating " + textFile.getCanonicalPath() + "\n" + "This takes a moment to get going...\n");
			String str = new String(Files.readAllBytes(textFile.toPath()), StandardCharsets.UTF_8);
			if (str.trim().length() == 0) {
				outputText.append("Nothing here to validate, seems to be empty.");
				return;
			}
			display.asyncExec(new SpellCheck(langTool, outputText, str));

		} catch (IOException x) {
			x.printStackTrace();
		}
	}

	/**
	 * Returns a string from the resource bundle. We don't want to crash because of
	 * a missing String. Returns the key if not found.
	 */
	static String getResourceString(String key) {
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}
	}

	/**
	 * Returns a string from the resource bundle and binds it with the given
	 * arguments. If the key is not found, return the key.
	 */
	static String getResourceString(String key, Object[] args) {
		try {
			return MessageFormat.format(getResourceString(key), args);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}
	}
}
