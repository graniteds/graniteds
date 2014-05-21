/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.javafx.tide;

import java.io.IOException;
import java.io.InputStream;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Callback;

import javax.inject.Named;

import org.granite.client.tide.Context;
import org.granite.logging.Logger;


/**
 * @author William DRAI
 */
public abstract class ManagedView {
	
	private final String viewId;
	
	public ManagedView(String viewId) {
		this.viewId = viewId;
	}
	
	public String getViewId() {
		return viewId;
	}
	
	@Override
	public int hashCode() {
		return viewId.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof ManagedView && ((ManagedView)obj).getViewId().equals(viewId);
	}
	
	public abstract Parent load(Context context);
	
	public static SimpleView of(String viewId, Parent root) {
		return new SimpleView(viewId, root);
	}
	
	public static FXMLView fxml(String url) {
		return new FXMLView(url);
	}
	
	public static FXMLView fxml(String url, Class<?> resourceRoot) {
		return new FXMLView(url, resourceRoot);
	}
	
	public static FXMLView fxml(Class<?> resourceRoot) {
		return new FXMLView(resourceRoot.getSimpleName() + ".fxml", resourceRoot);
	}
	
	
	public static class SimpleView extends ManagedView {
		
		private Parent root;

		public SimpleView(String viewId, Parent root) {
			super(viewId);
			this.root = root;
		}
		
		public Parent load(Context context) {
			return root;
		}
	}
	
	
	public static class FXMLView extends ManagedView {
		
		private static final Logger log = Logger.getLogger(FXMLView.class);
		
		private final Class<?> resourceRoot;
		
		public FXMLView(String viewId) {
			super(viewId);
			this.resourceRoot = getClass();
		}
		
		public FXMLView(String viewId, Class<?> root) {
			super(viewId);
			this.resourceRoot = root;
		}
		
	    public Parent load(Context context) {
	        InputStream fxmlStream = null;
	        try {
	            fxmlStream = resourceRoot.getResourceAsStream(getViewId());
	            FXMLLoader loader = new FXMLLoader();
	            loader.setLocation(resourceRoot.getResource(getViewId()));
	            loader.setControllerFactory(new ControllerFactory(context));
	        	loader.getNamespace().putAll(context.allByAnnotatedWith(Named.class));
	            
	            return (Parent)loader.load(fxmlStream);
	        }
	        catch (IOException e) {
	        	throw new RuntimeException("Could not load view " + getViewId(), e);
	        }
	        finally {
	            if (fxmlStream != null) {
	                try {
						fxmlStream.close();
					} 
	                catch (IOException e) {
	                	log.warn(e, "Could not close fxml stream");
					}
	            }
	        }
	    }
	    
	    private class ControllerFactory implements Callback<Class<?>, Object> {
	    	private Context context;
	    	
	    	public ControllerFactory(Context context) {
	    		this.context = context;
	    	}
	    	
	    	@Override
	    	public Object call(Class<?> type) {
	    		return context.byType(type);
	    	}
	    }
	}
}
