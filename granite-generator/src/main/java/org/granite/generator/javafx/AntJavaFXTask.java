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

package org.granite.generator.javafx;

import java.lang.reflect.Field;

import org.granite.generator.Transformer;
import org.granite.generator.ant.AbstractAntJavaGenTask;
import org.granite.generator.as3.As3TypeFactory;
import org.granite.generator.java.JavaGroovyTransformer;
import org.granite.generator.javafx.template.JavaFXTemplateUris;

/**
 * @author Franck WOLFF
 */
public class AntJavaFXTask extends AbstractAntJavaGenTask {

    ///////////////////////////////////////////////////////////////////////////
    // Task execution.

    @Override
	protected As3TypeFactory initDefaultClientTypeFactory() {
    	As3TypeFactory clientTypeFactoryImpl = new DefaultJavaFXTypeFactory();
    	return clientTypeFactoryImpl;
    }
    
    @Override
    protected Transformer<?, ?, ?> initDefaultTransformer() {
    	return new JavaGroovyTransformer();
    }
    
    @Override
    protected String defaultTemplateUri(String type) {
    	try {
	    	Field field = JavaFXTemplateUris.class.getField(type);
	    	return (String)field.get(null);
    	}
    	catch (Exception e) {
    		throw new RuntimeException("Default template not found for type " + type);
    	}
    }
}
