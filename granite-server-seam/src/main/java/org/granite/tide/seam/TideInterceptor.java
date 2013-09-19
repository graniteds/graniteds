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
package org.granite.tide.seam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.granite.tide.TidePersistenceManager;
import org.granite.tide.annotations.BypassTideInterceptor;
import org.granite.tide.invocation.ContextUpdate;
import org.granite.tide.seam.TideInit.FactoryVariable;
import org.granite.tide.seam.lazy.SeamInitializer;
import org.granite.tide.seam.lazy.TidePersistenceFactory;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.Component.BijectedAttribute;
import org.jboss.seam.annotations.DataBinderClass;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.bpm.BusinessProcessInterceptor;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.BijectionInterceptor;
import org.jboss.seam.core.EventInterceptor;
import org.jboss.seam.databinding.DataBinder;
import org.jboss.seam.intercept.AbstractInterceptor;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;


/**
 * This interceptor has 4 activities :
 * - Updating the context with data received from the Flex client, remerging client data in the persistence context when needed
 * - Intercept outjected values to return it to the client
 * - Determine the Persistence Context being used for the conversation and creating a lazyinitializer
 *   storing it in the current conversation
 * - Return all changed values to the client
 *
 * @author Venkat DANDA
 * @author Cameron INGRAM
 * @author William DRAI
 */


@Interceptor(around={BijectionInterceptor.class, EventInterceptor.class, BusinessProcessInterceptor.class})
public class TideInterceptor extends AbstractInterceptor {

    private static final long serialVersionUID = 1L;

    
    private static final LogProvider log = Logging.getLogProvider(TideInterceptor.class);

    private boolean reentrant; //OK, since all Seam components are single-threaded


    @AroundInvoke
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object aroundInvoke(InvocationContext invocation) throws Exception {

        if (reentrant) {
            log.trace("About to invoke method");

            if (log.isTraceEnabled())
                log.trace("reentrant call to component: " + getComponent().getName() );

            Object result = invocation.proceed();

            log.trace("Method invoked");
 
            return result;
        }

        reentrant = true;
  
        try {
            if (getComponent().getBeanClass().isAnnotationPresent(BypassTideInterceptor.class))
            	return invocation.proceed();
            
            TideInvocation tideInvocation = TideInvocation.get();
            
            if (tideInvocation == null || tideInvocation.isLocked())
                return invocation.proceed();

            AbstractSeamServiceContext tideContext = null;
            if (Contexts.isSessionContextActive()) 
            	tideContext = (AbstractSeamServiceContext)Component.getInstance(AbstractSeamServiceContext.COMPONENT_NAME, true); 
            
            if (tideContext == null)
                return invocation.proceed();
            
            // Ignore lifecycle methods
            if (SeamUtils.isLifecycleMethod(getComponent(), invocation.getMethod())) {
                tideInvocation.lock();

                Object result = invocation.proceed();

                tideInvocation.unlock();

                return result;
            }

            
            boolean evaluate = false;
            
            //Check for persistence
            checkForPersistenceContexts(invocation);

            // Ignore inner interceptions of other components during processing
            if (tideInvocation.isEnabled() && !tideInvocation.isUpdated()) {
                List<ContextUpdate> updates = new ArrayList<ContextUpdate>(tideInvocation.getUpdates());
                tideInvocation.updated();
                tideContext.restoreContext(updates, getComponent(), invocation.getTarget());
                evaluate = true;

                // Inject DataModel selections
                Field field = getComponent().getClass().getDeclaredField("dataModelGetters");
                field.setAccessible(true);
                List<BijectedAttribute<?>> dataModelGetters = (List<BijectedAttribute<?>>)field.get(getComponent());
                for (BijectedAttribute<?> getter : dataModelGetters) {
                    Annotation dataModelAnn = getter.getAnnotation();
                    DataBinder wrapper = dataModelAnn.annotationType().getAnnotation(DataBinderClass.class).value().newInstance();
                    String name = getter.getName();
                    ScopeType scope = wrapper.getVariableScope(dataModelAnn);
                    if (scope == ScopeType.UNSPECIFIED) {
                        scope = getComponent().getScope();
                        if (scope == ScopeType.STATELESS)
                            scope = ScopeType.EVENT;
                    }
                    Object dataModel = scope.getContext().get(name);
                    if (dataModel != null && dataModel instanceof TideDataModel) {
                        Field field2 = getComponent().getClass().getDeclaredField("dataModelSelectionSetters");
                        field2.setAccessible(true);
                        Map<String, BijectedAttribute<?>> setters = (Map<String, BijectedAttribute<?>>)field2.get(getComponent());
                        BijectedAttribute setter = setters.get(name);
                        if (setter != null) {
                            Object value = setter.get(invocation.getTarget());
                            ((TideDataModel)dataModel).setRowData(value);
                        }
                    }
                }
            }
            
            // Do invocation
            Object result = invocation.proceed();

            boolean restrict = getComponent().beanClassHasAnnotation(Restrict.class);
            
            // Intercept outjected values
            if (getComponent().needsOutjection()) {               
                List<BijectedAttribute<Out>> li = getComponent().getOutAttributes();
                for (BijectedAttribute<Out> att: li) {
                    ScopeType scope = att.getAnnotation().scope();
                    if (ScopeType.UNSPECIFIED.equals(scope)) {
                        Component outComponent = Component.forName(att.getName());
                        if (outComponent != null)
                            scope = outComponent.getScope();
                        else
                            scope = getComponent().getScope();
                    }
                    if (ScopeType.STATELESS.equals(scope))
                        scope = ScopeType.EVENT;
                    
                    if (!(ScopeType.EVENT.equals(scope))) {
                        Context context = Contexts.getEventContext();
                        if (context.get(att.getName() + "_tide_unspecified_") != null) {
                            context.remove(att.getName() + "_tide_unspecified_");
                            context.remove(att.getName());
                        }
                    }    
                    
                    tideContext.addResultEval(new ScopedContextResult(att.getName(), null, scope, restrict));
                }

                Field field = getComponent().getClass().getDeclaredField("dataModelGetters");
                field.setAccessible(true);
                List<BijectedAttribute<?>> dataModelGetters = (List<BijectedAttribute<?>>)field.get(getComponent());
                for (BijectedAttribute<?> getter : dataModelGetters) {
                    Annotation anno = getter.getAnnotation();
                    DataBinder wrapper = anno.annotationType().getAnnotation(DataBinderClass.class).value().newInstance();
                    ScopeType scope = wrapper.getVariableScope(anno);
                    if (ScopeType.UNSPECIFIED.equals(scope))
                        scope = getComponent().getScope();
                    if (ScopeType.STATELESS.equals(scope))
                        scope = ScopeType.EVENT;
                    
                    if (!(ScopeType.EVENT.equals(scope))) {
                        Context context = Contexts.getEventContext();
                        if (context.get(getter.getName() + "_tide_unspecified_") != null) {
                            context.remove(getter.getName() + "_tide_unspecified_");
                            context.remove(getter.getName());
                        }
                    }
                    
                    tideContext.addResultEval(new ScopedContextResult(getter.getName(), null, scope, restrict));
                }
            }
            
            // Force evaluation of factory components dependent on the called component
            Set<FactoryVariable> factoredVariables = TideInit.getFactoredVariables(getComponent());
            if (factoredVariables != null) {
	            for (FactoryVariable variable : factoredVariables) {
	            	ScopeType scope = variable.getScope();
	            	if (ScopeType.UNSPECIFIED.equals(scope))
	            		scope = getComponent().getScope();
	            	if (ScopeType.STATELESS.equals(scope))
	            		scope = ScopeType.EVENT;
	            	
	                tideContext.addResultEval(new ScopedContextResult(variable.getVariableName(), null, scope, restrict));
	            }
            }
            

            if (evaluate)
                tideInvocation.evaluated(tideContext.evaluateResults(getComponent(), invocation.getTarget(), false));

            return result;
        }
        finally {
            reentrant = false;
        }
    } 
    
    /**
     * Try to determine what the PersistenceContext is and create an appropriate 
     * lazy initializer for it.
     * @param ctx the bean bieng accessed.
     */
    private void checkForPersistenceContexts(InvocationContext ctx) {        
        Object bean = ctx.getTarget();
        TidePersistenceManager pm = null;
        
        for ( BijectedAttribute<?> ba: getComponent().getPersistenceContextAttributes() ) {
   		    Object object = ba.get(bean);
   		    SeamInitializer.instance().setTidePersistenceManager(TidePersistenceFactory.createTidePersistence(getComponent(), object));
   		    return;
        }
        
   	    if (getComponent().needsInjection()) {
		    List<BijectedAttribute<In>> li = getComponent().getInAttributes();
		    
			for (BijectedAttribute<In> att: li) {
				
				try {
					pm = TidePersistenceFactory.createTidePersistence(getComponent(), att);
				} catch (RuntimeException ex) {
					continue;
				}
				
				if (pm != null) {
			        SeamInitializer.instance().setTidePersistenceManager(pm);
			        return;
				} 
			}
		}
   	    
   	    //Last chance to see a PersistenceManager can be found for this invocation
   	    pm = TidePersistenceFactory.createTidePersistence(getComponent(), ctx.getTarget());
   	    if (pm != null)
    	    SeamInitializer.instance().setTidePersistenceManager(pm);
    }
    
	// Needed for Seam 2.1
    public boolean isInterceptorEnabled() {
        return true;
    }
}
