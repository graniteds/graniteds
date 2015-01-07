/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.tide.invocation;

import java.io.Serializable;

import org.granite.tide.Expression;


/**
 * @author William DRAI
 */
public class ContextResult implements Serializable, Expression {

    private static final long serialVersionUID = 1L;
    
    
    private String componentName;
    private String componentClassName;
    private String expression;
    
    
    public ContextResult() {
    }
    
    public ContextResult(String componentName, String expression) {
        this.componentName = componentName;
        this.expression = expression;
    }

    public String getComponentName() {
        return componentName;
    }
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentClassName() {
        return componentClassName;
    }
    public void setComponentClassName(String componentClassName) {
        this.componentClassName = componentClassName;
    }
    
    private Class<?> componentClass;
    
    public Class<?> getComponentClass() {
    	if (componentClassName == null)
    		return null;
    	
    	if (componentClass == null) {
	    	try {
	    		componentClass = Thread.currentThread().getContextClassLoader().loadClass(componentClassName);
	    	}
	    	catch (Exception e) {
	    		throw new RuntimeException("Component class not found", e);
	    	}
    	}
    	return componentClass;	    
    }
        
    public String getExpression() {
        return expression;
    }
    public void setExpression(String expression) {
        this.expression = expression;
    }
    
    public Boolean getRestrict() {
        return null;
    }
    
    public String getPath() {
        return componentName + (expression != null ? "." + expression : "");
    }
    
    public boolean matches(String componentName, String componentClassName, String expr) {
        if (this.componentClassName != null && componentClassName != null 
            && (this.componentClassName + (this.expression != null ? "." + this.expression : "")).indexOf(componentClassName + (expr != null ? "." + expr : "")) == 0) {
            return true;
        }
        return getPath().indexOf(componentName + (expr != null ? "." + expr : "")) == 0; 
    }

    
    @Override
    public String toString() {
        return (componentName != null ? componentName : "") 
        	+ (componentClassName != null ? "(" + componentClassName + ")" : "") 
        	+ (expression != null ? "." + expression : "");
    }

    
    @Override
    public int hashCode() {
        return (componentName + "(" + componentClassName + ")." + expression).hashCode();
    }
    
    @Override
    public boolean equals(Object object) {
        if (object == null || !object.getClass().equals(getClass()))
            return false;
        
        ContextResult result = (ContextResult)object;
        if (result.getComponentName() == null && componentName == null 
        		&& (!((result.getComponentClassName() == null && componentClassName == null) || result.getComponentClassName().equals(componentClassName))))
        	return false;
        
        if (result.getComponentName() != null 
        		&& !result.getComponentName().equals(componentName))
            return false;
        
        if (result.getComponentClassName() != null && componentClassName != null 
        		&& !result.getComponentClassName().equals(componentClassName))
        	return false;
        
        if (expression == null)
            return result.getExpression() == null;
        
        return expression.equals(result.getExpression());
    }
}
