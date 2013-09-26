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

package org.granite.generator.ant;

import java.lang.reflect.Field;

import org.granite.generator.Transformer;
import org.granite.generator.as3.As3TypeFactory;
import org.granite.generator.as3.DefaultAs3TypeFactory;
import org.granite.generator.as3.JavaAs3GroovyTransformer;
import org.granite.generator.template.StandardTemplateUris;

/**
 * @author Franck WOLFF
 */
public class AntJavaAs3Task extends AbstractAntJavaGenTask {

    ///////////////////////////////////////////////////////////////////////////
    // Configurable fields (xml attributes).

    private boolean externalizelong = false;
    private boolean externalizebiginteger = false;
    private boolean externalizebigdecimal = false;
    
    ///////////////////////////////////////////////////////////////////////////
    // Task attributes.

    public void setExternalizelong(boolean externalizelong) {
		this.externalizelong = externalizelong;
	}

    public void setExternalizebiginteger(boolean externalizebiginteger) {
		this.externalizebiginteger = externalizebiginteger;
	}

	public void setExternalizebigdecimal(boolean externalizebigdecimal) {
		this.externalizebigdecimal = externalizebigdecimal;
	}

    ///////////////////////////////////////////////////////////////////////////
    // Task execution.

    @Override
	protected As3TypeFactory initDefaultClientTypeFactory() {
    	As3TypeFactory clientTypeFactoryImpl = new DefaultAs3TypeFactory();
    	clientTypeFactoryImpl.configure(externalizelong, externalizebiginteger, externalizebigdecimal);
    	return clientTypeFactoryImpl;
    }
    
    @Override
    protected Transformer<?, ?, ?> initDefaultTransformer() {
    	return new JavaAs3GroovyTransformer();
    }
    
    @Override
    protected String defaultTemplateUri(String type) {
    	try {
	    	Field field = StandardTemplateUris.class.getField(type);
	    	return (String)field.get(null);
    	}
    	catch (Exception e) {
    		throw new RuntimeException("Default template not found for type " + type);
    	}
    }
}
