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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.granite.builder.GraniteBuilderContext;
import org.granite.builder.properties.Gas3Template;
import org.granite.builder.properties.GraniteProperties;
import org.granite.builder.util.SWTUtil;
import org.granite.generator.TemplateUri;
import org.granite.generator.as3.reflect.JavaType.Kind;
import org.granite.generator.template.StandardTemplateUris;

/**
 * @author Franck WOLFF
 */
public class TemplatesPanel extends Composite {

	private final GraniteProperties properties;
	
	private Tree templatesTree = null;
	private boolean initialized = false;

	public TemplatesPanel(Composite parent, GraniteBuilderContext context) throws CoreException {
        super(parent, SWT.NONE);
        this.properties = context.getProperties();
        initializeComponents();
	}

	public Set<Gas3Template> getTemplates() {
		if (!initialized)
			return properties.getGas3().getTemplates();
		
		Set<Gas3Template> templates = new HashSet<Gas3Template>(templatesTree.getItemCount());
		for (TreeItem kindItem : templatesTree.getItems()) {
			StringBuilder sb = new StringBuilder();
			if (kindItem.getItemCount() > 0)
				sb.append((String)kindItem.getItem(0).getData());
			if (kindItem.getItemCount() > 1)
				sb.append(';').append((String)kindItem.getItem(1).getData());
			templates.add(new Gas3Template((Kind)kindItem.getData(), sb.toString()));
		}
		return templates;
	}

	@Override
	public Rectangle getClientArea() {
		initializeContent();
		return super.getClientArea();
	}

	private void initializeContent() {
		if (!initialized) {
			for (Kind kind : Kind.values()) {
				TreeItem kindItem = SWTUtil.addTreeItem(templatesTree, SWTUtil.IMG_TEMPLATE, kind.name(), null, null);
				kindItem.setData(kind);
				TemplateUri[] uris = properties.getGas3().getMatchingTemplateUris(kind);
				for (TemplateUri uri : uris) {
					TreeItem uriItem = SWTUtil.addTreeItem(kindItem, SWTUtil.IMG_FILE, uri.getUri() + (uri.isBase() ? " (base)" : ""), null, null);
					uriItem.setData(uri.getUri());
				}
				kindItem.setExpanded(true);
			}
			initialized = true;
		}
	}
	
	private void initializeComponents() {
        setLayout(new GridLayout(2, false));
        
        Label text = new Label(this, SWT.NONE);
        text.setText("Templates used for generation:");
        text.setLayoutData(SWTUtil.newGridData(SWT.NONE, 2));
        
        templatesTree = new Tree(this, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        templatesTree.setLayoutData(new GridData(
            GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL |
            GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL
        ));
        
		
		Composite buttons = new Composite(this, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		buttons.setLayout(new FillLayout(SWT.VERTICAL));
		
		final Button editButton = SWTUtil.newButton(buttons, "Edit...", false, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editTemplatesHandler(e);
			}
		});
		
		SWTUtil.newButton(buttons, "Reset to default", true, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				properties.getGas3().getTemplates().clear();
				properties.getGas3().getTemplates().addAll(GraniteProperties.getDefaultProperties().getGas3().getTemplates());
				for (TreeItem item : templatesTree.getItems())
					item.dispose();
				initialized = false;
				initializeContent();
			}
		});
		
		SWTUtil.newButton(buttons, "Use Tide", true, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Gas3Template template = properties.getGas3().getTemplate(Kind.ENTITY);
				template.setUri(StandardTemplateUris.TIDE_ENTITY_BASE, true);
				template = properties.getGas3().getTemplate(Kind.REMOTE_DESTINATION);
				template.setUri(StandardTemplateUris.TIDE_REMOTE_BASE, true);
				for (TreeItem item : templatesTree.getItems())
					item.dispose();
				initialized = false;
				initializeContent();
			}
		});
		
		templatesTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Enable/Disable buttons based on selected tree item.
				editButton.setEnabled(templatesTree.getSelection() != null && templatesTree.getSelection().length > 0);
			}
		});
	}
	
	private void editTemplatesHandler(SelectionEvent e) {
		if (templatesTree.getSelection() == null || templatesTree.getSelection().length == 0)
			return;
		
		TreeItem kindItem = templatesTree.getSelection()[0];
		while (kindItem.getParentItem() != null)
			kindItem = kindItem.getParentItem();
		
		String templateUri = (kindItem.getItemCount() <= 0 ? "" : (String)kindItem.getItems()[0].getData());
		String baseTemplateUri = (kindItem.getItemCount() <= 1 ? "" : (String)kindItem.getItems()[1].getData());
		
		String[] uris = Dialogs.editTemplateUris(
			getDisplay().getActiveShell(),
			"Templates for " + kindItem.getText() + " type",
			templateUri,
			baseTemplateUri
		);
		
		if (uris != null) {
			if (kindItem.getItemCount() > 0) {
				kindItem.getItem(0).setText(uris[0]);
				kindItem.getItem(0).setData(uris[0]);
			}
			else {
				TreeItem uriItem = SWTUtil.addTreeItem(kindItem, SWTUtil.IMG_FILE, uris[0], null, null);
				uriItem.setData(uris[0]);
			}
			
			if (uris[1].length() > 0) {
				if (kindItem.getItemCount() > 1) {
					kindItem.getItem(1).setText(uris[1]);
					kindItem.getItem(1).setData(uris[1]);
				}
				else {
					TreeItem uriItem = SWTUtil.addTreeItem(kindItem, SWTUtil.IMG_FILE, uris[1], null, null);
					uriItem.setData(uris[1]);
				}
			}
			else if (kindItem.getItemCount() > 1)
				kindItem.getItem(1).dispose();
		}
	}
}
