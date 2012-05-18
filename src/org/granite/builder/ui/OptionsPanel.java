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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.granite.builder.GraniteBuilderContext;
import org.granite.builder.properties.Gas3;
import org.granite.builder.properties.Gas3Translator;
import org.granite.builder.properties.GraniteProperties;
import org.granite.builder.util.SWTUtil;
import org.granite.builder.util.StringUtil;

/**
 * @author Franck WOLFF
 */
public class OptionsPanel extends Composite implements PropertyChangeListener {

	private static final String TRANSLATOR_SEPARATOR = " -> ";
	
	private final GraniteProperties properties;
	
	private Text uid = null;
	private Text as3TypeFactory = null;
	private Text entityFactory = null;
	private Text remoteDestinationFactory = null;
	private Text transformer = null;
	private List translators = null;
	private Button debugEnabled = null;
	private Button flexConfig = null;
	private Button externalizeLong = null;
	private Button externalizeBigInteger = null;
	private Button externalizeBigDecimal = null;
	
	private boolean initialized = false;
	
	public OptionsPanel(Composite parent, GraniteBuilderContext context) throws CoreException {
        super(parent, SWT.NONE);
        this.properties = context.getProperties();
        initializeComponents();
	}
	
	public String getUid() {
		if (!initialized)
			return properties.getGas3().getUid();
		return uid.getText();
	}

	public String getAs3TypeFactory() {
		if (!initialized)
			return properties.getGas3().getAs3TypeFactory();
		return as3TypeFactory.getText();
	}

	public String getEntityFactory() {
		if (!initialized)
			return properties.getGas3().getEntityFactory();
		return entityFactory.getText();
	}

	public String getRemoteDestinationFactory() {
		if (!initialized)
			return properties.getGas3().getRemoteDestinationFactory();
		return remoteDestinationFactory.getText();
	}
	
	public String getTransformer() {
		if (!initialized) {
			if (properties.getGas3().getTransformers().isEmpty())
				return "";
			return properties.getGas3().getTransformers().get(0).getType();
		}
		return transformer.getText();
	}
	
	public Set<Gas3Translator> getTranslators() {
		if (!initialized)
			return properties.getGas3().getTranslators();
		
		Set<Gas3Translator> translatorsSet = new HashSet<Gas3Translator>();
		for (String translator : translators.getItems()) {
			String[] values = StringUtil.split(translator, TRANSLATOR_SEPARATOR);
			if (values.length == 2)
				translatorsSet.add(new Gas3Translator(values[0], values[1]));
		}
		return translatorsSet;
	}
	
	public boolean isDebugEnabled() {
		return debugEnabled.getSelection();
	}
	
	public boolean isFlexConfig() {
		return flexConfig.getSelection();
	}

	public boolean isExternalizeLong() {
		return externalizeLong.getSelection();
	}

	public boolean isExternalizeBigInteger() {
		return externalizeBigInteger.getSelection();
	}

	public boolean isExternalizeBigDecimal() {
		return externalizeBigDecimal.getSelection();
	}

	@Override
	public Rectangle getClientArea() {
		initializeContent();
		return super.getClientArea();
	}
	
	private void initializeContent() {
		if (!initialized) {
	        if (properties.getGas3().getUid() != null)
	        	uid.setText(properties.getGas3().getUid());

	        if (properties.getGas3().getAs3TypeFactory() != null)
	        	as3TypeFactory.setText(properties.getGas3().getAs3TypeFactory());

	        if (properties.getGas3().getEntityFactory() != null)
	        	entityFactory.setText(properties.getGas3().getEntityFactory());

	        if (properties.getGas3().getRemoteDestinationFactory() != null)
	        	remoteDestinationFactory.setText(properties.getGas3().getRemoteDestinationFactory());
	        
	        if (!properties.getGas3().getTransformers().isEmpty())
	        	transformer.setText(properties.getGas3().getTransformers().get(0).getType());
	        
	        for (Gas3Translator translator : properties.getGas3().getTranslators())
	        	translators.add(translator.getJava() + TRANSLATOR_SEPARATOR + translator.getAs3());
	        
	        debugEnabled.setSelection(properties.getGas3().isDebugEnabled());
	        flexConfig.setSelection(properties.getGas3().isFlexConfig());
	        externalizeLong.setSelection(properties.getGas3().isExternalizeLong());
	        externalizeBigInteger.setSelection(properties.getGas3().isExternalizeBigInteger());
	        externalizeBigDecimal.setSelection(properties.getGas3().isExternalizeBigDecimal());
	        
			initialized = true;
	        
	        this.properties.getGas3().removePropertyChangeListener(this);
	        this.properties.getGas3().addPropertyChangeListener(this);
		}
	}
    
	public void propertyChange(PropertyChangeEvent evt) {
		initialized = false;
		initializeContent();
	}

	private void initializeComponents() {
        setLayout(new GridLayout());

        Label label = new Label(this, SWT.NONE);
        label.setText("UID property name (leave empty if you don't want this feature):");
        
        uid = new Text(this, SWT.BORDER);
        uid.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        label = new Label(this, SWT.NONE);
        label.setText("As3TypeFactory class:");
        
        as3TypeFactory = new Text(this, SWT.BORDER);
        as3TypeFactory.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        label = new Label(this, SWT.NONE);
        label.setText("EntityFactory class:");
        
        entityFactory = new Text(this, SWT.BORDER);
        entityFactory.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        label = new Label(this, SWT.NONE);
        label.setText("RemoteDestinationFactory class:");
        
        remoteDestinationFactory = new Text(this, SWT.BORDER);
        remoteDestinationFactory.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        label = new Label(this, SWT.NONE);
        label.setText("Transformer class:");
        
        transformer = new Text(this, SWT.BORDER);
        transformer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        label = new Label(this, SWT.NONE);
        label.setText("Package translators:");
        
        Composite translatorsComposite = new Composite(this, SWT.BORDER);
        translatorsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        translatorsComposite.setLayout(new GridLayout(2, false));
        
        translators = new List(translatorsComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
        translators.setLayoutData(new GridData(GridData.FILL_BOTH));
        
		Composite translatorsCompositeButtons = new Composite(translatorsComposite, SWT.NONE);
		translatorsCompositeButtons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		translatorsCompositeButtons.setLayout(new FillLayout(SWT.VERTICAL));

        SWTUtil.newButton(translatorsCompositeButtons, "Add Translator...", true, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String[] values = Dialogs.addPackageTranslator(getDisplay().getActiveShell(), "Add Translator");
				if (values != null && values.length == 2)
					translators.add(StringUtil.join(values, TRANSLATOR_SEPARATOR));
			}
		});

        final Button editTranslatorButton = SWTUtil.newButton(translatorsCompositeButtons, "Edit Translator...", false, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectedIndex = translators.getSelectionIndex();
				String selected = translators.getItem(selectedIndex);
				String[] values = StringUtil.split(selected, TRANSLATOR_SEPARATOR);
				
				values = Dialogs.editPackageTranslator(
					getDisplay().getActiveShell(),
					"Edit Translator",
					(values.length > 0 ? values[0] : null),
					(values.length > 1 ? values[1] : null)
				);
				
				if (values != null && values.length == 2)
					translators.setItem(selectedIndex, StringUtil.join(values, TRANSLATOR_SEPARATOR));
			}
		});

        final Button removeTranslatorButton = SWTUtil.newButton(translatorsCompositeButtons, "Remove Translators", false, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (translators.getSelectionCount() > 0)
					translators.remove(translators.getSelectionIndices());
			}
		});
        
        translators.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editTranslatorButton.setEnabled(translators.getSelectionCount() == 1);
				removeTranslatorButton.setEnabled(translators.getSelectionCount() > 0);
			}
		});
        
        debugEnabled = new Button(this, SWT.CHECK);
        debugEnabled.setText("Show debug information in console");
        
        flexConfig = new Button(this, SWT.CHECK);
        flexConfig.setText("Generate a Flex Builder configuration file");
        
        externalizeLong = new Button(this, SWT.CHECK);
        externalizeLong.setText("Use org.granite.math.Long");
        
        externalizeBigInteger = new Button(this, SWT.CHECK);
        externalizeBigInteger.setText("Use org.granite.math.BigInteger");
        
        externalizeBigDecimal = new Button(this, SWT.CHECK);
        externalizeBigDecimal.setText("Use org.granite.math.BigDecimal");
        
        SWTUtil.newButton(this, "Reset to default values", true, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Gas3 defaultGas3 = GraniteProperties.getDefaultProperties().getGas3();
				uid.setText(defaultGas3.getUid());
				as3TypeFactory.setText(defaultGas3.getAs3TypeFactory());
				entityFactory.setText(defaultGas3.getEntityFactory());
				remoteDestinationFactory.setText(defaultGas3.getRemoteDestinationFactory());
				transformer.setText(defaultGas3.getTransformers().get(0).getType());
				debugEnabled.setSelection(false);
				flexConfig.setSelection(false);
				externalizeLong.setSelection(false);
				externalizeBigInteger.setSelection(false);
				externalizeBigDecimal.setSelection(false);
			}
		});
	}
}
