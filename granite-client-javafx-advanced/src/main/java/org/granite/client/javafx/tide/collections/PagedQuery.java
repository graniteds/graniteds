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
package org.granite.client.javafx.tide.collections;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.annotation.PreDestroy;
import javax.inject.Named;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

import org.granite.client.tide.Context;
import org.granite.client.tide.ContextAware;
import org.granite.client.tide.Initializable;
import org.granite.client.tide.NameAware;
import org.granite.client.tide.impl.ComponentImpl;
import org.granite.client.tide.server.Component;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideResultEvent;
import org.granite.client.util.PropertyHolder;
import org.granite.logging.Logger;
import org.granite.tide.data.model.Page;
import org.granite.tide.data.model.PageInfo;
import org.granite.tide.data.model.SortInfo;
import org.granite.util.TypeUtil;

/**
 * 	Implementation of the Tide paged collection with an generic service backend.<br/>
 *  <br/>
 *  By default the corresponding service should have the same name and expose a 'find' method<br/>
 *  that returns a Map with the following properties :<br/>
 *  <pre>
 *  resultCount
 *  resultList
 *  firstResult
 *  maxResults
 *  </pre>
 * 
 *  The name of the remote service can be overriden by setting the remoteComponentName property.
 *  The name of the remote method can by set by the remoteMethodName property.
 * 
 * 	@author William DRAI
 */
@Named
public class PagedQuery<E, F> extends PagedCollection<E> implements Component, PropertyHolder, NameAware, ContextAware, Initializable {
    
	private static Logger log = Logger.getLogger(PagedQuery.class);
	
    protected Component component = null;
    
    private final ServerSession serverSession;
    private String componentName = null;
    private String remoteComponentName = null;
    private Class<? extends ComponentImpl> remoteComponentClass = null;
    private Context context = null;
	
    protected String methodName = "find";
    protected boolean methodNameSet = false;
    
    protected boolean usePage = false;
    
	private SortInfo sortInfo = new SortInfo();
	protected SortAdapter sortAdapter = null;
    
    private Map<String, Object> internalFilterMap = new HashMap<String, Object>();
    private ObservableMap<String, Object> filterMap = FXCollections.observableMap(Collections.synchronizedMap(internalFilterMap));
    private Class<F> filterClass = null;
    private ObjectProperty<F> filter = null;
	
    
    protected PagedQuery() {
    	this.serverSession = null;
    	// CDI proxying...
    }
    
    public PagedQuery(ServerSession serverSession) {
    	this.serverSession = serverSession;
    	
    	this.filterMap.addListener(new MapChangeListener<String, Object>() {
			@Override
			public void onChanged(MapChangeListener.Change<? extends String, ?> change) {
				fullRefresh = true;
				filterRefresh = true;
			}
    	});
    }
	
    public void setName(String componentName) {
    	this.componentName = componentName;
    }
    
    public void setContext(Context context) {
    	this.context = context;

        if (remoteComponentName != null)
            setRemoteComponentName(remoteComponentName);

        if (remoteComponentClass != null) {
            try {
                setRemoteComponentClass(remoteComponentClass);
            }
            catch (Exception e) {
                throw new RuntimeException("Could not init context", e);
            }
        }

    	if (component instanceof ContextAware)
    		((ContextAware)component).setContext(context);
    }
    
	public void init() {
        if (component == null) {
            component = new ComponentImpl(serverSession);
            ((ComponentImpl)component).setName(componentName);
            ((ComponentImpl)component).setContext(context);
        }
	}
	
	
	public void setSortAdapter(SortAdapter sortAdapter) {
		this.sortAdapter = sortAdapter;
		if (sortAdapter != null)
			sortAdapter.apply(sortInfo);
	}
	
	public SortAdapter getSortAdapter() {
		return sortAdapter;
	}
	
	public void resetSort() {
		this.sortAdapter = null;
		sortInfo.setOrder(null);
		sortInfo.setDesc(null);
	}
	
	
	public ObjectProperty<F> filterProperty() {
		return filter;
	}
	@SuppressWarnings("unchecked")
	public F getFilter() {
		if (filter != null)
			return filter.get();
		try {
			return (F)filterMap;
		}
		catch (ClassCastException e) {
			return null;
		}
	}
	public void setFilter(F filter) {
		this.filter.set(filter);
	}
	@SuppressWarnings("unchecked")
	public void setFilterClass(Class<F> filterClass) throws IllegalAccessException, InstantiationException {
		if (Map.class.isAssignableFrom(filterClass))
			return;
		this.filterClass = filterClass;
		this.filter = new SimpleObjectProperty<F>(this, "filter");
		setFilter((F)TypeUtil.newInstance(filterClass, Object.class));
	}
	
	@SuppressWarnings("unchecked")
	public void resetFilter() {
		internalFilterMap.clear();
		if (filterClass != null) {
			try {
				setFilter((F)TypeUtil.newInstance(filterClass, Object.class));
			}
			catch (Exception e) {
				log.error(e, "Could not reset typed filter for PagedQuery %s", getName());
			}
		}
	}
	
	@Override
	@PreDestroy
	public void clear() {
		super.clear();
	}
	
	public void reset() {
		resetFilter();
		resetSort();
		clear();
	}
	
	@Override
	public boolean refresh() {
		if (filter != null && this.context.getEntityManager().isDeepDirtyEntity(filter.get())) {
			filterRefresh = true;
			fullRefresh = true;
		}
		return super.refresh();
	}
	

	public String getName() {
	    return remoteComponentName;
	}

	public void setRemoteComponentName(String remoteComponentName) {
        if (remoteComponentName == null)
            throw new IllegalArgumentException("remoteComponentName cannot be null");

        this.remoteComponentName = remoteComponentName;
        if (context == null) {
            this.component = null;
            return;
        }

		component = context.byName(remoteComponentName);
        if (component == null || !(component instanceof ComponentImpl)) {
            component = new ComponentImpl(serverSession);
            context.set(remoteComponentName, component);
        }
	}
	
	public void setRemoteComponentClass(Class<? extends ComponentImpl> remoteComponentClass) throws IllegalAccessException, InstantiationException {
        if (remoteComponentClass == null)
            throw new IllegalArgumentException("remoteComponentClass cannot be null");

        this.remoteComponentClass = remoteComponentClass;
        if (context == null) {
            component = null;
            return;
        }

        component = context.byType(remoteComponentClass);
        if (component == null) {
            component = TypeUtil.newInstance(remoteComponentClass, new Class<?>[] { ServerSession.class }, new Object[] { serverSession });
            context.set(component);
        }
	}
	
	public void setMethodName(String methodName) {
		this.methodName = methodName;
		this.methodNameSet = true;
	}
	
	public void setUsePage(boolean usePage) {
		this.usePage = usePage;
	}
	
	
	/**
	 *	Trigger a results query for the current filter
	 *	@param first	: index of first required result
	 *  @param last     : index of last required result
	 */
	@Override
	protected Future<?> find(int first, int last) {
		int max = 0;
		if (this.initializing && this.max > 0)
			max = this.max;
		else if (!this.initializing)
		    max = last-first;
		
		PagedCollectionResponder findResponder = new PagedCollectionResponder(first, max);		
		Object filter = null;
		if (this.filter != null)
			filter = this.filter.get();
		else {
			// Copy filter map to avoid concurrent modifications
			synchronized (internalFilterMap) {
				filter = new HashMap<String, Object>(internalFilterMap);
			}
		}
		
		return doFind(filter, first, max, findResponder);
	}
	
	protected synchronized Future<?> doFind(Object filter, int first, int max, PagedCollectionResponder findResponder) {
		// Force evaluation of max, results and count
		if (sortAdapter != null)
			sortAdapter.retrieve(sortInfo);
		
		String[] order = sortInfo.getOrder();
		if (order != null && order.length == 0)
			order = null;
		boolean[] desc = sortInfo.getDesc();
		if (desc != null && desc.length == 0)
			desc = null;
		
		boolean usePage = this.usePage;
		try {
			for (Method m : component.getClass().getMethods()) {
				if (m.getName().equals(methodName) && m.getParameterTypes().length >= 2 
						&& PageInfo.class.isAssignableFrom(m.getParameterTypes()[1])) {
					usePage = true;
					break;
				}
			}
		}
		catch (Exception e) {
			// Untyped component proxy
		}
		
		if (usePage) {
			PageInfo pageInfo = new PageInfo(first, max, order, desc);
			return component.call(methodName, new Object[] { filter, pageInfo, findResponder });
		}
		return component.call(methodName, new Object[] { filter, first, max, order, desc, findResponder });
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected Page<E> getResult(TideResultEvent<?> event, int first, int max) {
		if (event.getResult() instanceof Page<?>)
			return (Page<E>)event.getResult();
		
		Map<String, Object> result = (Map<String, Object>)event.getResult();
		Page<E> page = new Page<E>(result.containsKey("firstResult") ? (Integer)result.get("firstResult") : first, 
				result.containsKey("maxResults") ? (Integer)result.get("maxResults") : max,
				((Number)result.get("resultCount")).intValue(), (List<E>)result.get("resultList"));
	    return page;
	}
	
	
	/**
	 * PropertyHolder interface
	 */
	public Object getObject() {
		if (component instanceof PropertyHolder)
	    	return ((PropertyHolder)component).getObject();
	    return null;
	}
	
    public void setProperty(String propName, Object value) {
    	if (component instanceof PropertyHolder)
    		((PropertyHolder)component).setProperty(propName, value);
    }

	
	@Override
	public <T> Future<T> call(String operation, Object... args) {
		throw new UnsupportedOperationException();
	}
	
}
