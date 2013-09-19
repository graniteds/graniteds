/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.validation;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.granite.client.messaging.RemoteAlias;

/**
 * @author William DRAI
 */
@RemoteAlias("org.granite.tide.validators.InvalidValue")
public class InvalidValue implements Externalizable {

    private static final long serialVersionUID = 1L;
	
	private Object rootBean;
    private Object bean;
    private Class<?> beanClass;
    private String path;
    private Object value;
    private String message;

    
    public InvalidValue(Object rootBean, Object bean, String path, Object value, String message) {
        if (bean == null || path == null)
            throw new NullPointerException("bean and path parameters cannot be null");
        this.rootBean = rootBean;
        this.bean = bean;
        this.beanClass = bean.getClass();
        this.path = path;
        this.value = value;
        this.message = message;
    }
    
    public Object getRootBean() {
		return rootBean;
	}

	public Object getBean() {
		return bean;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public String getPath() {
		return path;
	}

	public Object getValue() {
		return value;
	}

	public String getMessage() {
		return message;
	}


	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	rootBean = in.readObject();
    	bean = in.readObject();
    	in.readObject();
    	path = (String)in.readObject();
    	value = in.readObject();
    	message = (String)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
    	throw new IOException("Cannot serialize InvalidValue from client");
    }
}
