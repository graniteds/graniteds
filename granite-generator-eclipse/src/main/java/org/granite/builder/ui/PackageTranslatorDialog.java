/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
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
public class PackageTranslatorDialog extends SelectionStatusDialog {

	private final String initialJavaPath;
	private final String initialAs3Path;

	private Text javaPath = null;
	private Text as3Path = null;

	public PackageTranslatorDialog(Shell parent, String initialJavaPath, String initialAs3Path) {
		super(parent);
		this.initialJavaPath = initialJavaPath;
		this.initialAs3Path = initialAs3Path;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite)super.createDialogArea(parent);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = convertWidthInCharsToPixels(70);
		composite.setLayoutData(data);

		Label label = new Label(composite, SWT.NONE);
		label.setText("Java Path (eg: my.path):");
		
		javaPath = new Text(composite, SWT.BORDER);
		javaPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (initialJavaPath != null)
			javaPath.setText(initialJavaPath);
		javaPath.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				if (javaPath.getText().trim().length() == 0)
					updateStatus(ProjectUtil.createErrorStatus("Tempate URI is mandatory"));
				else
					updateStatus(new Status(IStatus.OK, GraniteActivator.PLUGIN_ID, ""));
			}
		});

		label = new Label(composite, SWT.NONE);
		label.setText("ActionScript3 Path (eg: my.translated.path):");
		
		as3Path = new Text(composite, SWT.BORDER);
		as3Path.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (initialAs3Path != null)
			as3Path.setText(initialAs3Path);
		
		applyDialogFont(composite);
		return composite;
	}
	
	@Override
	protected void okPressed() {
		if (javaPath.getText().trim().length() == 0 || as3Path.getText().trim().length() == 0)
			updateStatus(ProjectUtil.createErrorStatus("Java and As3 path are mandatory"));
		else
			super.okPressed();
	}

	@Override
	protected void computeResult() {
		setSelectionResult(new String[]{javaPath.getText().trim(), as3Path.getText().trim()});
	}
	
	@Override
	protected void cancelPressed() {
		setSelectionResult(null);
		super.cancelPressed();
	}
}