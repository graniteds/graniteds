/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.builder.ui;

import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.granite.builder.util.SWTUtil;

/**
 * @author Franck WOLFF
 */
public class PromptDialog extends SelectionStatusDialog {

	private final String initialValue;
	private final Pattern valuePattern;

	private Text input = null;

	public PromptDialog(Shell parent, String initialValue, Pattern valuePattern) {
		super(parent);
		this.initialValue = initialValue;
		this.valuePattern = valuePattern;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite)super.createDialogArea(parent);
		createMessageArea(composite);
		input = new Text(composite, SWT.BORDER);
		input.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (initialValue != null)
			input.setText(initialValue);
		input.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				String value = input.getText();
				if (valuePattern != null) {
					if (valuePattern.matcher(value).matches()) {
						input.setBackground(SWTUtil.getColor(getShell().getDisplay(), SWTUtil.WHITE));
						getOkButton().setEnabled(true);
					}
					else {
						input.setBackground(SWTUtil.getColor(getShell().getDisplay(), SWTUtil.LIGHT_RED));
						getOkButton().setEnabled(false);
					}
				}
			}
		});
		
		applyDialogFont(composite);
		return composite;
	}
	
	@Override
	protected void computeResult() {
		if (input != null && input.getText() != null)
			setSelectionResult(new String[]{input.getText().trim()});
	}
	
	@Override
	protected void cancelPressed() {
		setSelectionResult(null);
		super.cancelPressed();
	}
}