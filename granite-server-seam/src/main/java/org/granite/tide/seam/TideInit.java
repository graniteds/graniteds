/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.tide.seam;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.granite.tide.annotations.BypassTideInterceptor;
import org.granite.tide.async.AsyncPublisher;
import org.granite.tide.data.DataEnabled;
import org.granite.tide.seam.async.TideAsynchronousInterceptor;
import org.jboss.seam.Component;
import org.jboss.seam.Namespace;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.intercept.InterceptorType;
import org.jboss.seam.async.AsynchronousInterceptor;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Init;
import org.jboss.seam.core.Init.FactoryExpression;
import org.jboss.seam.core.Init.FactoryMethod;
import org.jboss.seam.init.Initialization;
import org.jboss.seam.intercept.Interceptor;
import org.jboss.seam.log.Log;
import org.jboss.seam.util.Reflections;


/**
 * Particular initializer for Tide which instruments all Seam components
 * - Tide bijection/invocation interceptor is added
 * - Tide asynchronous interceptor is added
 * 
 * @author William DRAI
 */
@Scope(ScopeType.APPLICATION)
@Name("org.granite.tide.seam.init")
@Install(precedence=FRAMEWORK)
@Startup
@BypassInterceptors
public class TideInit {
	
	@Logger
	private Log log;
	
	private Map<Component, Set<FactoryVariable>> factoryComponents = new HashMap<Component, Set<FactoryVariable>>();
	
    
    public TideInit() {
        Context context = Contexts.getApplicationContext();
        for (String name : context.getNames()) {
            if (!name.endsWith(Initialization.COMPONENT_SUFFIX))
                continue;
            Object object = context.get(name);
            if (object instanceof Component) {
                Component component = (Component) object;
                if (component.isInterceptionEnabled() && component.getScope() != ScopeType.APPLICATION)
                    instrumentComponent(component);
            }
        }
        
        Init.instance().importNamespace("org.granite.tide");
        
        scanFactoryComponents();
    }
    
    
    public static Set<FactoryVariable> getFactoredVariables(Component component) {
        TideInit init = (TideInit)Contexts.getApplicationContext().get(TideInit.class);    	
    	return init.factoryComponents.get(component);
    }
    
    
    @SuppressWarnings("unchecked")
    private void scanFactoryComponents() {
        try {
        	Init init = Init.instance();
        	
        	Field field = init.getClass().getDeclaredField("factories");
        	field.setAccessible(true);
        	Map<String, FactoryMethod> factories = (Map<String, FactoryMethod>)field.get(init);        	
        	for (Map.Entry<String, FactoryMethod> factory : factories.entrySet()) {
        		String componentName = factory.getValue().getComponent().getName();
        		addFactoryVariable(componentName, factory.getKey(), factory.getValue().getScope());
        	}
        	
        	field = init.getClass().getDeclaredField("factoryMethodExpressions");
        	field.setAccessible(true);
        	Map<String, FactoryExpression> factoryExpressions = (Map<String, FactoryExpression>)field.get(init);        	
        	for (Map.Entry<String, FactoryExpression> factoryExpression : factoryExpressions.entrySet()) {
        		String expressionString = factoryExpression.getValue().getMethodBinding().getExpressionString();
        		String componentName = resolveComponentName(expressionString);
        		if (componentName != null)
        			addFactoryVariable(componentName, factoryExpression.getKey(), factoryExpression.getValue().getScope());
        	}
        	
        	field = init.getClass().getDeclaredField("factoryValueExpressions");
        	field.setAccessible(true);
        	factoryExpressions = (Map<String, FactoryExpression>)field.get(init);        	
        	for (Map.Entry<String, FactoryExpression> factoryExpression : factoryExpressions.entrySet()) {
        		String expressionString = factoryExpression.getValue().getMethodBinding().getExpressionString();
        		String componentName = resolveComponentName(expressionString);
        		if (componentName != null)
        			addFactoryVariable(componentName, factoryExpression.getKey(), factoryExpression.getValue().getScope());
        	}
        }
        catch (Exception e) {
        	log.error("Could not initialize factory components map correctly", e);
        }
    }
    
    
    private String resolveComponentName(String expressionString) {
        Init init = Init.instance();
        
    	String expr = expressionString.startsWith("#{") ? expressionString.substring(2, expressionString.length()-1) : expressionString;
		int idx = expr.indexOf('.');
		String componentName = idx >= 0 ? expr.substring(0, idx) : expr;
		Component component = lookupComponent(componentName);
		if (component != null)
			return componentName;
    	
		if (idx < 0)
			return null;
		
    	Namespace ns = init.getRootNamespace().getChild(componentName);    	
    	while (ns != null) {
    		expr = expr.substring(idx+1);
    		idx = expr.indexOf('.');
    		String name = idx >= 0 ? expr.substring(0, idx) : expr;
    		
    		if (ns.hasChild(name))
    			ns = ns.getChild(name);
    		else {
        		try {
        			// Must use this hack to get the right name as all methods are private in Seam Namespace object
        			Method m = ns.getClass().getDeclaredMethod("qualifyName", String.class);
        			m.setAccessible(true);
        			componentName = (String)Reflections.invoke(m, ns, name);
        			component = Component.forName(componentName);
        			return component != null ? componentName : null;
        		}
        		catch (Exception e) {
        			// Ignore
        		}
    		}
    	}
    	
    	return null;
    }
    
    
    private void addFactoryVariable(String componentName, String variableName, ScopeType scope) {
    	Component component = lookupComponent(componentName);
		Set<FactoryVariable> variables = factoryComponents.get(component);
		if (variables == null) {
			variables = new HashSet<FactoryVariable>();
			factoryComponents.put(component, variables);
		}
		variables.add(new FactoryVariable(variableName, scope));
    }
    
    
    public static final class FactoryVariable {
    	private final String variableName;
    	private final ScopeType scope;
    	
    	public FactoryVariable(String variableName, ScopeType scope) {
    		this.variableName = variableName;
    		this.scope = scope;
    	}
    	
    	public String getVariableName() {
    		return variableName;
    	}
    	
    	public ScopeType getScope() {
    		return scope;
    	}
    	
    	@Override
    	public boolean equals(Object obj) {
    		if (!(obj instanceof FactoryVariable))
    			return false;
    		return ((FactoryVariable)obj).variableName.equals(variableName) && ((FactoryVariable)obj).scope.equals(scope);    
    	}
    	
    	@Override
    	public int hashCode() {
    		return (variableName + "@" + scope).hashCode();
    	}
    }
    
    
    /**
     * Implementation of component lookup for Seam
     * Uses a hack to handle imports and fully qualified names
     * 
     * @param componentName name of the component
     * @return Component object
     */
    static Component lookupComponent(String componentName) {
        Init init = Init.instance();
        
        Component component = Component.forName(componentName);
        if (component != null)
        	return component;
        
    	// Look for the component in the imported namespaces (always give precedence to org.granite.tide overriden components)
    	for (Namespace ns : init.getGlobalImports()) {
            try {
                // Must use this hack to get the right name as all methods are private in Seam Namespace object
                Method m = ns.getClass().getDeclaredMethod("qualifyName", String.class);
                m.setAccessible(true);
                String nsName = (String)Reflections.invoke(m, ns, componentName);
                if (!nsName.startsWith("org.granite.tide"))
                	continue;
                component = Component.forName(nsName);
                if (component != null)
                	return component;
            }
            catch (Exception e) {
                // Ignore
            }
        }
        
    	for (Namespace ns : init.getGlobalImports()) {
    		try {
    			// Must use this hack to get the right name as all methods are private in Seam Namespace object
    			Method m = ns.getClass().getDeclaredMethod("qualifyName", String.class);
    			m.setAccessible(true);
    			String nsName = (String)Reflections.invoke(m, ns, componentName);
    			component = Component.forName(nsName);
    			if (component != null)
    				return component;
    		}
    		catch (Exception e) {
    			// Ignore
    		}
    	}
    	return null;
    }
    
    
    /**
     * Implementation of factory lookup for Seam
     * Uses a hack to manage imports and fully qualified names
     * 
     * @param componentName name of the factored component
     * @return FactoryMethod object
     */
    static FactoryMethod lookupFactory(String componentName) {
        Init init = Init.instance();
        
        FactoryMethod factoryMethod = init.getFactory(componentName);
        if (factoryMethod != null)
        	return factoryMethod;
        
    	// Look for the factory in the imported namespaces (always give precedence to org.granite.tide overriden components)
    	for (Namespace ns : init.getGlobalImports()) {
    		try {
    			// Must use this hack to get the right name as all methods are private in Seam Namespace object
    			Method m = ns.getClass().getDeclaredMethod("qualifyName", String.class);
    			m.setAccessible(true);
    			String nsName = (String)Reflections.invoke(m, ns, componentName);
    			if (!nsName.startsWith("org.granite.tide"))
    				continue;
    			factoryMethod = init.getFactory(nsName);
    			if (factoryMethod != null)
    				return factoryMethod;
    		}
    		catch (Exception e) {
    			// Ignore
    		}
    	}
    	
    	for (Namespace ns : init.getGlobalImports()) {
    		try {
    			// Must use this hack to get the right name as all methods are private in Seam Namespace object
    			Method m = ns.getClass().getDeclaredMethod("qualifyName", String.class);
    			m.setAccessible(true);
    			String nsName = (String)Reflections.invoke(m, ns, componentName);
    			factoryMethod = init.getFactory(nsName);
    			if (factoryMethod != null)
    				return factoryMethod;
    		}
    		catch (Exception e) {
    			// Ignore
    		}
    	}
    	
    	return null;
    }
    
    /**
     * Implementation of factory expression lookup for Seam
     * Uses a hack to manage imports and fully qualified names
     * 
     * @param componentName name of the factored component
     * @return FactoryMethod object
     */
    static FactoryExpression lookupFactoryExpression(String componentName) {
        Init init = Init.instance();
        
        FactoryExpression factoryExpression = init.getFactoryMethodExpression(componentName);
        if (factoryExpression != null)
        	return factoryExpression;
        factoryExpression = init.getFactoryValueExpression(componentName);
        if (factoryExpression != null)
        	return factoryExpression;
        
    	// Look for the factory expression in the imported namespaces (always give precedence to org.granite.tide overriden components)
    	for (Namespace ns : init.getGlobalImports()) {
    		try {
    			// Must use this hack to get the right name as all methods are private in Seam Namespace object
    			Method m = ns.getClass().getDeclaredMethod("qualifyName", String.class);
    			m.setAccessible(true);
    			String nsName = (String)Reflections.invoke(m, ns, componentName);
    			if (!nsName.startsWith("org.granite.tide"))
    				continue;
    			
    	        factoryExpression = init.getFactoryMethodExpression(nsName);
    	        if (factoryExpression != null)
    	        	return factoryExpression;
    	        factoryExpression = init.getFactoryValueExpression(nsName);
    	        if (factoryExpression != null)
    	        	return factoryExpression;
    		}
    		catch (Exception e) {
    			// Ignore
    		}
    	}
    	for (Namespace ns : init.getGlobalImports()) {
    		try {
    			// Must use this hack to get the right name as all methods are private in Seam Namespace object
    			Method m = ns.getClass().getDeclaredMethod("qualifyName", String.class);
    			m.setAccessible(true);
    			String nsName = (String)Reflections.invoke(m, ns, componentName);
    			
    	        factoryExpression = init.getFactoryMethodExpression(nsName);
    	        if (factoryExpression != null)
    	        	return factoryExpression;
    	        factoryExpression = init.getFactoryValueExpression(nsName);
    	        if (factoryExpression != null)
    	        	return factoryExpression;
    		}
    		catch (Exception e) {
    			// Ignore
    		}
    	}
    	
    	return null;
    }
    
    
    private void instrumentComponent(Component component) {
        if (component.getBeanClass().isAnnotationPresent(BypassTideInterceptor.class))
        	return;
        
        List<Interceptor> li = component.getInterceptors(InterceptorType.ANY);
        
        boolean newSortServer = false, newSortClient = false;
        	
        boolean found = false;
        for (Interceptor i : li) {
            if (i.getUserInterceptorClass().equals(TideInterceptor.class)) {
                found = true;
                break;
            }
        }
        if (!found) {
            component.addInterceptor(new Interceptor(new TideInterceptor(), component));
            newSortServer = true;
        }
        
        if (component.beanClassHasAnnotation(DataEnabled.class) && component.getBeanClass().getAnnotation(DataEnabled.class).useInterceptor()) {
        	found = false;
	        for (Interceptor i : li) {
	            if (i.getUserInterceptorClass().equals(TideDataPublishingInterceptor.class)) {
	                found = true;
	                break;
	            }
	        }
	        if (!found) {
	            component.addInterceptor(new Interceptor(new TideDataPublishingInterceptor(), component));
	            newSortServer = true;
	        }
        }
 
        // Check if AsyncPublisher installed
        AsyncPublisher asyncPublisher = (AsyncPublisher)Component.getInstance("org.granite.tide.seam.async.publisher");
        if (asyncPublisher != null) {
            boolean async = false;
            li = component.getClientSideInterceptors();
            for (Iterator<Interceptor> i = li.iterator(); i.hasNext(); ) {
                Interceptor in = i.next();
                if (in.getUserInterceptorClass().equals(AsynchronousInterceptor.class)) {
                    async = true;
                    break;
                }
            }
            if (async) {
                component.addInterceptor(new Interceptor(new TideAsynchronousInterceptor(), component));
                newSortClient = true;
            }
        }
        
        if (newSortServer || newSortClient) {
            // Force correct sorting of interceptors: hack because newSort is private
            try {
                Method m = component.getClass().getDeclaredMethod("newSort", List.class);
                m.setAccessible(true);
                if (newSortServer)
                    m.invoke(component, component.getInterceptors(InterceptorType.SERVER));
                if (newSortClient)
                    m.invoke(component, component.getInterceptors(InterceptorType.CLIENT));
            }
            catch (Exception e) {
                // Ignore all
            }
        }
    }
}
