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

package org.granite.generator.java;

import java.lang.reflect.Field;

import org.granite.generator.Transformer;
import org.granite.generator.ant.AbstractAntJavaGenTask;
import org.granite.generator.as3.As3TypeFactory;
import org.granite.generator.java.template.JavaTemplateUris;

/**
 * @author Franck WOLFF
 */
public class AntJavaTask extends AbstractAntJavaGenTask {

    ///////////////////////////////////////////////////////////////////////////
    // Task execution.

    @Override
	protected As3TypeFactory initDefaultClientTypeFactory() {
    	As3TypeFactory clientTypeFactoryImpl = new DefaultJavaTypeFactory();
    	return clientTypeFactoryImpl;
    }
    
    @Override
    protected Transformer<?, ?, ?> initDefaultTransformer() {
    	return new JavaGroovyTransformer();
    }
    
    @Override
    protected String defaultTemplateUri(String type) {
    	try {
	    	Field field = JavaTemplateUris.class.getField(type);
	    	return (String)field.get(null);
    	}
    	catch (Exception e) {
    		throw new RuntimeException("Default template not found for type " + type);
    	}
    }
}
