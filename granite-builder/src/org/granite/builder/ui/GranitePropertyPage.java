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

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.granite.builder.GraniteBuilderContext;
import org.granite.builder.GraniteRebuildJob;
import org.granite.builder.properties.Gas3Transformer;
import org.granite.builder.properties.GraniteProperties;
import org.granite.builder.properties.GranitePropertiesLoader;
import org.granite.builder.util.SWTUtil;
import org.granite.builder.util.StringUtil;

/**
 * @author Franck WOLFF
 */
public class GranitePropertyPage extends PropertyPage {
	
	private GraniteBuilderContext context = null;
	
	private SourcesPanel sourcesPanel = null;
	private ProjectsPanel projectsPanel = null;
	private ClasspathsPanel classpathsPanel = null;
	private TemplatesPanel templatesPanel = null;
	private OptionsPanel optionsPanel = null;

    public GranitePropertyPage() {
        super();
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new FillLayout());

        try {
    		this.context = new GraniteBuilderContext((IProject)getElement());
	        
	        TabFolder tabFolder = new TabFolder(composite, SWT.FILL);
	        
	        addTabItem1(tabFolder);
	        addTabItem2(tabFolder);
	        addTabItem3(tabFolder);
	        addTabItem4(tabFolder);
	        addTabItem5(tabFolder);
	        
	        noDefaultAndApplyButton();
	        
	        return composite;
        } catch (Exception e) {
            Text text = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY);
            text.setText(StringUtil.tokenize("Granite properties load failed: " + e.toString(), 80, Text.DELIMITER));
            return composite;
        }
    }
    
    private void addTabItem1(TabFolder tabFolder) throws CoreException {
        TabItem item = new TabItem(tabFolder, SWT.NONE);
        item.setText("Java Sources");
        item.setImage(SWTUtil.getImage(item.getDisplay(), SWTUtil.IMG_PKG_FOLDER));
        sourcesPanel = new SourcesPanel(tabFolder, context);
        item.setControl(sourcesPanel);
    }
    
    private void addTabItem2(TabFolder tabFolder) throws CoreException {
        TabItem item = new TabItem(tabFolder, SWT.NONE);
        item.setText("Projects");
        item.setImage(SWTUtil.getImage(item.getDisplay(), SWTUtil.IMG_PROJECTS));
        projectsPanel = new ProjectsPanel(tabFolder, context);
        item.setControl(projectsPanel);
    }
    
    private void addTabItem3(TabFolder tabFolder) throws CoreException {
        TabItem item = new TabItem(tabFolder, SWT.NONE);
        item.setText("Libraries");
        item.setImage(SWTUtil.getImage(item.getDisplay(), SWTUtil.IMG_LIBRARY));
        classpathsPanel = new ClasspathsPanel(tabFolder, context);
        item.setControl(classpathsPanel);
    }
    
    private void addTabItem4(TabFolder tabFolder) throws CoreException {
        TabItem item = new TabItem(tabFolder, SWT.NONE);
        item.setText("Templates");
        item.setImage(SWTUtil.getImage(item.getDisplay(), SWTUtil.IMG_TEMPLATE));
        templatesPanel = new TemplatesPanel(tabFolder, context);
        item.setControl(templatesPanel);
    }
    
    private void addTabItem5(TabFolder tabFolder) throws CoreException {
        TabItem item = new TabItem(tabFolder, SWT.NONE);
        item.setText("Options");
        item.setImage(SWTUtil.getImage(item.getDisplay(), SWTUtil.IMG_SETTINGS));
        optionsPanel = new OptionsPanel(tabFolder, context);
        item.setControl(optionsPanel);
    }

	@Override
    public boolean performOk() {
        GraniteProperties properties = GraniteProperties.getDefaultProperties();
        
        properties.getGas3().getSources().addAll(sourcesPanel.getSources());

        properties.getGas3().getProjects().addAll(projectsPanel.getProjects());
        
        properties.getGas3().getClasspaths().addAll(classpathsPanel.getClasspaths());
        
        properties.getGas3().getTemplates().clear();
        properties.getGas3().getTemplates().addAll(templatesPanel.getTemplates());
        
        properties.getGas3().setUid(optionsPanel.getUid());
        properties.getGas3().setAs3TypeFactory(optionsPanel.getAs3TypeFactory());
        properties.getGas3().setEntityFactory(optionsPanel.getEntityFactory());
        properties.getGas3().setRemoteDestinationFactory(optionsPanel.getRemoteDestinationFactory());
        properties.getGas3().getTransformers().clear();
        properties.getGas3().getTransformers().add(new Gas3Transformer(optionsPanel.getTransformer()));
        properties.getGas3().getTranslators().clear();
        properties.getGas3().getTranslators().addAll(optionsPanel.getTranslators());
        properties.getGas3().setDebugEnabled(optionsPanel.isDebugEnabled());
        properties.getGas3().setFlexConfig(optionsPanel.isFlexConfig());
        properties.getGas3().setExternalizeLong(optionsPanel.isExternalizeLong());
        properties.getGas3().setExternalizeBigInteger(optionsPanel.isExternalizeBigInteger());
        properties.getGas3().setExternalizeBigDecimal(optionsPanel.isExternalizeBigDecimal());
        
        try {
            GranitePropertiesLoader.save((IProject)getElement(), properties);
        } catch (IOException e) {
            setErrorMessage("Could not save Granite properties: " + e.toString());
            return false;
        }
        
        GraniteRebuildJob rebuild = new GraniteRebuildJob((IProject)getElement(), true);
        rebuild.schedule();
        
        return true;
    }
}