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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.granite.builder.GraniteActivator;
import org.granite.builder.util.ProjectUtil;

/**
 * @author Franck WOLFF
 */
public class TemplateUrisDialog extends SelectionStatusDialog {

	private final String initialTemplateUri;
	private final String initialBaseTemplateUri;

	private Text templateUri = null;
	private Text baseTemplateUri = null;

	public TemplateUrisDialog(Shell parent, String initialTemplateUri, String initialBaseTemplateUri) {
		super(parent);
		this.initialTemplateUri = initialTemplateUri;
		this.initialBaseTemplateUri = initialBaseTemplateUri;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite)super.createDialogArea(parent);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = convertWidthInCharsToPixels(70);
		composite.setLayoutData(data);

		Label label = new Label(composite, SWT.NONE);
		label.setText("Template URI:");
		
		templateUri = new Text(composite, SWT.BORDER);
		templateUri.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (initialTemplateUri != null)
			templateUri.setText(initialTemplateUri);
		templateUri.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				if (templateUri.getText().trim().length() == 0)
					updateStatus(ProjectUtil.createErrorStatus("Tempate URI is mandatory"));
				else
					updateStatus(new Status(IStatus.OK, GraniteActivator.PLUGIN_ID, ""));
			}
		});

		label = new Label(composite, SWT.NONE);
		label.setText("Base template URI (optional):");
		
		baseTemplateUri = new Text(composite, SWT.BORDER);
		baseTemplateUri.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (initialBaseTemplateUri != null)
			baseTemplateUri.setText(initialBaseTemplateUri);
		
		applyDialogFont(composite);
		return composite;
	}
	
	@Override
	protected void okPressed() {
		if (templateUri.getText().trim().length() == 0)
			updateStatus(ProjectUtil.createErrorStatus("Tempate URI is mandatory"));
		else
			super.okPressed();
	}

	@Override
	protected void computeResult() {
		setSelectionResult(new String[]{templateUri.getText().trim(), baseTemplateUri.getText().trim()});
	}
	
	@Override
	protected void cancelPressed() {
		setSelectionResult(null);
		super.cancelPressed();
	}
}