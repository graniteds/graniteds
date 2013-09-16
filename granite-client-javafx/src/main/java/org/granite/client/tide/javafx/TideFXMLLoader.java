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
package org.granite.client.tide.javafx;

import java.io.IOException;
import java.io.InputStream;

import javafx.fxml.FXMLLoader;
import javafx.util.Callback;

import javax.inject.Named;

import org.granite.client.tide.Context;

/**
 * @author William DRAI
 */
public class TideFXMLLoader {

    public static Object load(final Context context, String url, Class<?> controllerClass) throws IOException {
        InputStream fxmlStream = null;
        try {
            fxmlStream = controllerClass.getResourceAsStream(url);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(controllerClass.getResource(url));
            loader.setControllerFactory(new ControllerFactory(context));
        	loader.getNamespace().putAll(context.allByAnnotatedWith(Named.class));
            
            return loader.load(fxmlStream);
        }
        finally {
            if (fxmlStream != null) {
                fxmlStream.close();
            }
        }
    }
	
    public static Object load(String url, final Object controller) throws IOException {
        InputStream fxmlStream = null;
        try {
            fxmlStream = controller.getClass().getResourceAsStream(url);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(controller.getClass().getResource(url));
            loader.setControllerFactory(new Callback<Class<?>, Object>() {
				@Override
				public Object call(Class<?> type) {
					if (type.isInstance(controller))
						return controller;
					try {
						return type.newInstance();
					}
					catch (Exception e) {
						throw new RuntimeException("Could not instantiate controller of class " + type);
					}
				}
            });
            return loader.load(fxmlStream);
        }
        finally {
            if (fxmlStream != null) {
                fxmlStream.close();
            }
        }
    }
    
    public static class ControllerFactory implements Callback<Class<?>, Object> {
    	
    	private final Context context;
    	
    	public ControllerFactory(Context context) {
    		this.context = context;
    	}
    	
    	@Override
    	public Object call(Class<?> type) {
    		return context.byType(type);
    	}
    }
}
