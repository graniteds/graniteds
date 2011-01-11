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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.granite.builder.GraniteBuilderContext;
import org.granite.builder.GraniteNature;
import org.granite.builder.ToggleNatureAction;
import org.granite.builder.properties.Gas3Transformer;
import org.granite.builder.properties.GraniteProperties;
import org.granite.builder.properties.GranitePropertiesLoader;
import org.granite.builder.util.ProjectUtil;
import org.granite.builder.util.SWTUtil;

/**
 * @author Franck WOLFF
 */
public class AddNatureWizard extends Wizard {

	private static final String SOURCES_PANEL = "sourcesPanel";
	private static final String PROJECTS_PANEL = "projectsPanel";
	private static final String CLASSPATHS_PANEL = "classpathsPanel";
	private static final String TEMPLATES_PANEL = "templatesPanel";
	private static final String OPTIONS_PANEL = "optionsPanel";
	
	private final GraniteBuilderContext context;
	
	private WizardDialog dialog = null;
	
	public AddNatureWizard(IProject project) throws CoreException {
		this.context = new GraniteBuilderContext(project);
	}

	@Override
	public void addPages() {
		setWindowTitle("Add Granite Nature Wizard");
		setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(
			SWTUtil.getImage(getShell().getDisplay(), "icons/gdswiz.gif"))
		);
		
		addPage(new WizardPage(SOURCES_PANEL) {
			public void createControl(Composite parent) {
				try {
					setControl(new SourcesPanel(parent, context));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				setTitle("Source Folder Configuration");
				setDescription("Step 1: select Java source folders, included/excluded patterns and output folders...");
			}
		});
		
		addPage(new WizardPage(PROJECTS_PANEL) {
			public void createControl(Composite parent) {
				try {
					setControl(new ProjectsPanel(parent, context));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				setTitle("Dependent Projects Configuration");
				setDescription("Step 2: select dependent granite projects...");
			}
		});
		
		addPage(new WizardPage(CLASSPATHS_PANEL) {
			public void createControl(Composite parent) {
				try {
					setControl(new ClasspathsPanel(parent, context));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				setTitle("Classpath Configuration");
				setDescription("Step 3: select jars or class folders used as classpath...");
			}
		});
		
		addPage(new WizardPage(TEMPLATES_PANEL) {
			public void createControl(Composite parent) {
				try {
					setControl(new TemplatesPanel(parent, context));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				setTitle("Templates Configuration");
				setDescription("Step 4: select templates that will be used for generation...");
			}
		});
		
		addPage(new WizardPage(OPTIONS_PANEL) {
			public void createControl(Composite parent) {
				try {
					setControl(new OptionsPanel(parent, context));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				setTitle("Miscanellous Options");
				setDescription("Step 5: modify various options that control file generation...");
			}
		});
	}

	@Override
	public boolean performFinish() {
        GraniteProperties properties = GraniteProperties.getDefaultProperties();
        
        SourcesPanel sourcesPanel = (SourcesPanel)getPage(SOURCES_PANEL).getControl();
        properties.getGas3().getSources().addAll(sourcesPanel.getSources());

        ProjectsPanel projectsPanel = (ProjectsPanel)getPage(PROJECTS_PANEL).getControl();
        properties.getGas3().getProjects().addAll(projectsPanel.getProjects());
        
        ClasspathsPanel classpathsPanel = (ClasspathsPanel)getPage(CLASSPATHS_PANEL).getControl();
        properties.getGas3().getClasspaths().addAll(classpathsPanel.getClasspaths());
        
        TemplatesPanel templatesPanel = (TemplatesPanel)getPage(TEMPLATES_PANEL).getControl();
        properties.getGas3().getTemplates().clear();
        properties.getGas3().getTemplates().addAll(templatesPanel.getTemplates());
        
        OptionsPanel optionsPanel = (OptionsPanel)getPage(OPTIONS_PANEL).getControl();
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
            GranitePropertiesLoader.save(context.getJavaProject().getProject(), properties);
        } catch (IOException e) {
            dialog.setErrorMessage("Could not save Granite properties: " + e.toString());
            return false;
        }
		
		return true;
	}
	
	@Override
	public boolean performCancel() {
		try {
			IProject project = context.getJavaProject().getProject();
			if (project.getDescription().hasNature(GraniteNature.NATURE_ID))
				ToggleNatureAction.toggleNature(project);
		} catch (CoreException e) {
		}
		return true;
	}

	public static void run(final IProject project) throws CoreException {
		final Display display = (Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault());
		try {
			display.syncExec(new Runnable() {
				public void run() {
					try {
						Shell shell = new Shell(display);
				    	AddNatureWizard wizard = new AddNatureWizard(project);
				    	final WizardDialog dialog = new WizardDialog(shell, wizard);
				    	wizard.dialog = dialog;
				    	dialog.setPageSize(640, 589);
				    	dialog.setHelpAvailable(false);
				    	dialog.create();
						dialog.open();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
		} catch (Exception e) {
			if (e.getCause() instanceof CoreException)
				throw (CoreException)e.getCause();
			throw new CoreException(ProjectUtil.createErrorStatus("Could not run wizard: " + e.toString(), null));
		}
	}
}
