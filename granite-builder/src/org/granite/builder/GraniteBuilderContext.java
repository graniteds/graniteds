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

package org.granite.builder;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.granite.builder.properties.GraniteProperties;
import org.granite.builder.properties.GranitePropertiesLoader;
import org.granite.builder.util.ProjectUtil;

/**
 * @author Franck WOLFF
 */
public class GraniteBuilderContext {

    private final IJavaProject javaProject;
    private GraniteProperties properties = null;

    public GraniteBuilderContext(IProject project) throws CoreException {
        this.javaProject = JavaCore.create(project);
        if (javaProject == null)
            throw new CoreException(ProjectUtil.createErrorStatus("Not a Java Project: " + project, null));
    }

    public IJavaProject getJavaProject() {
        return javaProject;
    }

    public GraniteProperties getProperties() throws CoreException {
    	if (GranitePropertiesLoader.isOutdated(javaProject.getProject(), properties)) {
    		try {
    			properties = GranitePropertiesLoader.load(javaProject.getProject());
    		} catch (IOException e) {
                throw new CoreException(
                	ProjectUtil.createErrorStatus("Could not load Granite properties for: " + javaProject.getProject(),
                	e
                ));
            }
    	}
        return properties;
    }
}
