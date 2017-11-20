package com.mockumatrix.storm;

import java.util.LinkedHashMap;

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
	
  private String account, consumerKey, consumerSecret, accessToken, accessTokenSecret;
  
  
  private LinkedHashMap<String,String> map;

  public AccountEditorDialog(Shell parent) {
    this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
  }

  public AccountEditorDialog(Shell parent, int style) {
    super(parent, style);
    setText("Twitter OAuth Account Dialog");
  }

  public LinkedHashMap<String,String> open() {
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

  private void createContents(final Shell shell) {
	  
    shell.setLayout(new GridLayout(2, true));

    Label label = new Label(shell, SWT.NONE);
    label.setText("Account Name");
    GridData data = new GridData();
    data.horizontalSpan = 2;
    label.setLayoutData(data);

    final Text accountText = new Text(shell, SWT.BORDER);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    accountText.setLayoutData(data);

    Button ok = new Button(shell, SWT.PUSH);
    ok.setText("OK");
    data = new GridData(GridData.FILL_HORIZONTAL);
    ok.setLayoutData(data);
    ok.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        account = accountText.getText();
        shell.close();
      }
    });

    Button cancel = new Button(shell, SWT.PUSH);
    cancel.setText("Cancel");
    data = new GridData(GridData.FILL_HORIZONTAL);
    cancel.setLayoutData(data);
    cancel.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        account = null;
        shell.close();
      }
    });

    shell.setDefaultButton(ok);
  }
}

