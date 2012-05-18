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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.granite.builder.util.SWTUtil;
import org.granite.builder.util.StringUtil;

/**
 * @author Franck WOLFF
 */
public class IncludeExcludeOutputDialog extends SelectionStatusDialog {

	private static final Pattern CLUSION_PATTERN = Pattern.compile("[\\w\\*\\.\\$\\?/]+(\\[(\\w+=\\w+)(,\\w+=\\w+)*\\])?");
	private static final Pattern OUTPUT_PATTERN = Pattern.compile("[\\w\\-\\.\\/]*");
	
	private final String[] initialValues;
	
	private List includes = null;
	private List excludes = null;
	private Text output = null;
	private Text baseOutput = null; 
	
	public IncludeExcludeOutputDialog(Shell parent, String[] initialValues) {
		super(parent);
		
		if (initialValues == null)
			initialValues = new String[3];
		else if (initialValues.length != 3)
			throw new IllegalArgumentException("Bad initialValues length: " + initialValues.length);
		for (int i = 0; i < initialValues.length; i++) {
			if (initialValues[i] == null)
				initialValues[i] = "";
		}
		
		this.initialValues = initialValues;
		
		setSelectionResult(null);
		setStatusLineAboveButtons(true);

		int shellStyle = getShellStyle();
		setShellStyle(shellStyle | SWT.MAX | SWT.RESIZE);
	}
	
	@Override
	protected void cancelPressed() {
		setSelectionResult(initialValues);
		super.cancelPressed();
	}
	
	@Override
	protected void computeResult() {
		String[] result = new String[3];
		
		StringBuilder sb = new StringBuilder();
		for (String value : includes.getItems()) {
			if (sb.length() > 0)
				sb.append(';');
			sb.append(value);
		}
		result[0] = sb.toString();
		
		sb.setLength(0);
		for (String value : excludes.getItems()) {
			if (sb.length() > 0)
				sb.append(';');
			sb.append(value);
		}
		result[1] = sb.toString();
		
		sb.setLength(0);
		sb.append(output.getText()).append(';').append(baseOutput.getText());
		result[2] = sb.toString();
		
		setSelectionResult(result);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
        final Composite composite = (Composite)super.createDialogArea(parent);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = convertWidthInCharsToPixels(70);
		data.heightHint = convertHeightInCharsToPixels(26);
		composite.setLayoutData(data);
		composite.setLayout(new GridLayout(2, false));
		
		Label label = new Label(composite, SWT.NONE);
		label.setText("Inclusion Patterns:");
		label.setLayoutData(SWTUtil.newGridData(SWT.NONE, 2));
        
		includes = new List(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		includes.setLayoutData(new GridData(GridData.FILL_BOTH));
		for (String s : StringUtil.split(initialValues[0], ';')) {
			if (s.length() != 0)
				includes.add(s);
		}
		
		Composite includesButtons = new Composite(composite, SWT.NONE);
		includesButtons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		includesButtons.setLayout(new FillLayout(SWT.VERTICAL));
		
		SWTUtil.newButton(includesButtons, "Add...", true, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String pattern = Dialogs.prompt(
					getShell(),
					"Add Inclusion Pattern",
					"Allowed wildcards are '?', '*' and '**'",
					null,
					CLUSION_PATTERN
				);
				if (pattern != null && pattern.trim().length() > 0)
					includes.add(pattern.trim());
			}
		});
		final Button includesEditButton = SWTUtil.newButton(includesButtons, "Edit...", false, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectedIndex = includes.getSelectionIndex();
				String pattern = Dialogs.prompt(
					getShell(),
					"Edit Inclusion Pattern",
					"Allowed wildcards are '?', '*' and '**'",
					includes.getItem(selectedIndex),
					CLUSION_PATTERN
				);
				includes.setItem(selectedIndex, pattern.trim());
			}
		});
		final Button includesRemoveButton = SWTUtil.newButton(includesButtons, "Remove", false, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectedIndex = includes.getSelectionIndex();
				includes.remove(selectedIndex);
				
				if (includes.getItemCount() <= 0) {
					((Button)e.getSource()).setEnabled(false);
					includesEditButton.setEnabled(false);
				}
				else if (selectedIndex < includes.getItemCount())
					includes.setSelection(selectedIndex);
				else
					includes.setSelection(selectedIndex - 1);
			}
		});
		
		includes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				includesRemoveButton.setEnabled(true);
				includesEditButton.setEnabled(true);
			}
		});
		
		label = new Label(composite, SWT.NONE);
		label.setText("Exclusion Patterns:");
		label.setLayoutData(SWTUtil.newGridData(SWT.NONE, 2));
        
		excludes = new List(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		excludes.setLayoutData(new GridData(GridData.FILL_BOTH));
		for (String s : StringUtil.split(initialValues[1], ';')) {
			if (s.length() != 0)
				excludes.add(s);
		}
		
		Composite excludesButtons = new Composite(composite, SWT.NONE);
		excludesButtons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		excludesButtons.setLayout(new FillLayout(SWT.VERTICAL));
		
		SWTUtil.newButton(excludesButtons, "Add...", true, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String pattern = Dialogs.prompt(
					getShell(),
					"Add Exclusion Pattern",
					"Allowed wildcards are '?', '*' and '**'",
					null,
					CLUSION_PATTERN
				);
				if (pattern != null && pattern.trim().length() > 0)
					excludes.add(pattern.trim());
			}
		});
		final Button excludesEditButton = SWTUtil.newButton(excludesButtons, "Edit...", false, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectedIndex = excludes.getSelectionIndex();
				String pattern = Dialogs.prompt(
					getShell(),
					"Edit Exclusion Pattern",
					"Allowed wildcards are '?', '*' and '**'",
					excludes.getItem(selectedIndex),
					CLUSION_PATTERN
				);
				excludes.setItem(selectedIndex, pattern.trim());
			}
		});
		final Button excludesRemoveButton = SWTUtil.newButton(excludesButtons, "Remove", false, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectedIndex = excludes.getSelectionIndex();
				excludes.remove(selectedIndex);
				
				if (excludes.getItemCount() <= 0) {
					((Button)e.getSource()).setEnabled(false);
					excludesEditButton.setEnabled(false);
				}
				else if (selectedIndex < excludes.getItemCount())
					excludes.setSelection(selectedIndex);
				else
					excludes.setSelection(selectedIndex - 1);
			}
		});
		
		excludes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				excludesRemoveButton.setEnabled(true);
				excludesEditButton.setEnabled(true);
			}
		});

		String[] outputs = StringUtil.split(initialValues[2], ';');
		
		label = new Label(composite, SWT.NONE);
		label.setText("Output Directory (relative to project dir):");
		label.setLayoutData(SWTUtil.newGridData(SWT.NONE, 2));
        
        output = new Text(composite, SWT.BORDER);
        output.setLayoutData(SWTUtil.newGridData(GridData.FILL_HORIZONTAL, 2));
        if (outputs.length > 0)
        	output.setText(outputs[0]);
        output.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				if (OUTPUT_PATTERN.matcher(output.getText()).matches()) {
					output.setBackground(SWTUtil.getColor(getShell().getDisplay(), SWTUtil.WHITE));
					getOkButton().setEnabled(true);
				}
				else {
					output.setBackground(SWTUtil.getColor(getShell().getDisplay(), SWTUtil.LIGHT_RED));
					getOkButton().setEnabled(false);
				}
			}
		});

        label = new Label(composite, SWT.NONE);
        label.setText("Base Output Directory (optional, default to output above):");
        label.setLayoutData(SWTUtil.newGridData(SWT.NONE, 2));
        
        baseOutput = new Text(composite, SWT.BORDER);
        baseOutput.setLayoutData(SWTUtil.newGridData(GridData.FILL_HORIZONTAL, 2));
        if (outputs.length > 1)
        	baseOutput.setText(outputs[1]);
        baseOutput.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				if (OUTPUT_PATTERN.matcher(baseOutput.getText()).matches()) {
					baseOutput.setBackground(SWTUtil.getColor(getShell().getDisplay(), SWTUtil.WHITE));
					getOkButton().setEnabled(true);
				}
				else {
					baseOutput.setBackground(SWTUtil.getColor(getShell().getDisplay(), SWTUtil.LIGHT_RED));
					getOkButton().setEnabled(false);
				}
			}
		});

        applyDialogFont(composite);

        return composite;
	}
}
