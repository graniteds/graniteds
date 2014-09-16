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
package org.granite.client.tide.collection;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.annotation.PreDestroy;

import org.granite.client.tide.Context;
import org.granite.client.tide.ContextAware;
import org.granite.client.tide.Initializable;
import org.granite.client.tide.NameAware;
import org.granite.client.tide.data.EntityManager.UpdateKind;
import org.granite.client.tide.events.TideEvent;
import org.granite.client.tide.events.TideEventObserver;
import org.granite.client.tide.impl.ComponentImpl;
import org.granite.client.tide.server.Component;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.client.tide.server.TideResponder;
import org.granite.client.tide.server.TideResultEvent;
import org.granite.client.tide.server.TideRpcEvent;
import org.granite.client.util.PropertyHolder;
import org.granite.logging.Logger;
import org.granite.tide.data.model.Page;
import org.granite.tide.data.model.PageInfo;
import org.granite.tide.data.model.SortInfo;
import org.granite.util.TypeUtil;

/**
 * @author William DRAI
 */
public abstract class AbstractPagedCollection<E, F> implements List<E>, Component, PropertyHolder, NameAware, ContextAware, Initializable, TideEventObserver {
	
    private static final Logger log = Logger.getLogger(AbstractPagedCollection.class);
    
    private final ServerSession serverSession;
    private String componentName = null;
    private String remoteComponentName = null;
    private Class<? extends Component> remoteComponentClass = null;
    private Component component = null;
    private Context context = null;
	
    private String methodName = "find";
    private boolean usePage = false;
    private PageFilterFinder<E> pageFilterFinder = null;
    private SimpleFilterFinder<E> simpleFilterFinder = null;
    
	protected SortAdapter sortAdapter = null;
	private SortInfo sortInfo = new SortInfo();
	
    private Class<F> filterClass = null;
	
    protected boolean initializing = false;
    private boolean initSent = false;
    
	protected int first;
    protected int last;			// Current last index of local data
    protected int max;           // Page size
    protected int count;         // Result count
    private E[] localIndex = null;
    
	protected boolean fullRefresh = false;
	protected boolean filterRefresh = false;
	
	private boolean cancelPendingCalls = false;
	

	public AbstractPagedCollection() {
		// CDI proxying..
		this.serverSession = null;
		initCollection();
	}
    
    public AbstractPagedCollection(ServerSession serverSession) {
    	this.serverSession = serverSession;
    	initFilter();
    	initCollection();
    }

    public AbstractPagedCollection(Component remoteComponent, String methodName, int maxResults) {
    	this.component = remoteComponent;
    	this.methodName = methodName;
    	this.max = maxResults;
    	this.serverSession = null;
    	initFilter();
    	initCollection();
    	initFilterFinder();
    }
    
    public AbstractPagedCollection(Component remoteComponent, PageFilterFinder<E> finder, int maxResults) {
    	this.component = remoteComponent;
    	this.pageFilterFinder = finder;
    	this.max = maxResults;
    	this.serverSession = null;
    	initFilter();
    	initCollection();
    }
    
    public AbstractPagedCollection(Component remoteComponent, SimpleFilterFinder<E> finder, int maxResults) {
    	this.component = remoteComponent;
    	this.simpleFilterFinder = finder;
    	this.max = maxResults;
    	this.serverSession = null;
    	initFilter();
    	initCollection();
    }
    
	private void initCollection() {
	    log.debug("create collection");
		first = 0;
		last = 0;
		count = 0;
		initializing = true;
		
		initComponent();
	}
	
	@SuppressWarnings("unchecked")
	private void initComponent() {
		Type superclass = getClass().getGenericSuperclass();
		if (superclass instanceof ParameterizedType) {
			ParameterizedType supertype = (ParameterizedType)superclass;
			if (supertype.getActualTypeArguments()[0] instanceof Class<?>)
				setElementClass((Class<E>)supertype.getActualTypeArguments()[0]);
			if (supertype.getActualTypeArguments()[1] instanceof Class<?>) {
				try {
					setFilterClass((Class<F>)supertype.getActualTypeArguments()[1]);
				} 
				catch (Exception e) {
					throw new RuntimeException("Could not init filter for type " + supertype.getActualTypeArguments()[1], e);
				}
			}
		}
    }
	
    protected abstract void initFilter();
	
	
	/**
	 *	Get total number of elements
	 *  
	 *  @return collection total size
	 */
	@Override
	public int size() {
        initialFind();
        if (localIndex != null)
            return count;
	    return 0;
	}
	
	/**
	 *  Set the page size. The collection will store in memory twice this page size, and each server call
	 *  will return at most the page size.
	 * 
	 *  @param max maximum number of requested elements
	 */
	public void setMaxResults(int max) {
		this.max = max;
	}
	
	
	private Class<? extends E> elementClass;
	private String elementName;	
	private Set<String> entityNames = new HashSet<String>();
	
	public void setElementClass(Class<? extends E> elementClass) {
		this.elementClass = elementClass;
		
		if (this.elementName != null)
			entityNames.remove(elementName);
		
		elementName = elementClass != null ? elementClass.getSimpleName() : null;
		
		if (this.elementName != null)
			entityNames.add(this.elementName);
	}
	
	public Class<? extends E> getElementClass() {
		return elementClass;
	}
	
	
	public void setCancelPendingCalls(boolean cancel) {
		this.cancelPendingCalls = cancel;
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
	
	public void setRemoteComponentClass(Class<? extends Component> remoteComponentClass) throws IllegalAccessException, InstantiationException {
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
	
	public void setRemoteComponent(Component remoteComponent) {
        if (remoteComponent == null)
            throw new IllegalArgumentException("remoteComponent cannot be null");
        
		this.component = remoteComponent;
	}
	
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	public void setPageFilterFinder(PageFilterFinder<E> finder) {
		this.pageFilterFinder = finder;
	}
	
	public void setSimpleFilterFinder(SimpleFilterFinder<E> finder) {
		this.simpleFilterFinder = finder;
	}
	
	public void setUsePage(boolean usePage) {
		this.usePage = usePage;
	}
    
	public void init() {
        if (component != null)
        	return;
        
        component = new ComponentImpl(serverSession);
        ((ComponentImpl)component).setName(componentName);
        ((ComponentImpl)component).setContext(context);
	}
	
	
	public void setSortAdapter(SortAdapter sortAdapter) {
		this.sortAdapter = sortAdapter;
		if (sortAdapter != null)
			sortAdapter.retrieve(sortInfo);
	}
	
	public SortAdapter getSortAdapter() {
		return sortAdapter;
	}
	
	public void resetSort() {
		this.sortAdapter = null;
		sortInfo.setOrder(null);
		sortInfo.setDesc(null);
	}
	
	public void setFilterClass(Class<F> filterClass) throws IllegalAccessException, InstantiationException {
		if (Map.class.isAssignableFrom(filterClass)) {
			setFilter(null);
			return;
		}
		this.filterClass = filterClass;
		setFilter(TypeUtil.newInstance(filterClass, filterClass));
	}
	
	public void resetFilter() {
		if (filterClass == null) {
			setFilter(null);
			return;
		}
		try {
			setFilter(TypeUtil.newInstance(filterClass, filterClass));
		}
		catch (Exception e) {
			log.error(e, "Could not reset typed filter for PagedQuery %s", getName());
		}
	}
	
	public abstract void setFilter(F filter);
	
	public void reset() {
		resetFilter();
		resetSort();
		clear();
	}
	

	@Override
	public void handleEvent(TideEvent event) {
		if (event.getType().startsWith(UpdateKind.REFRESH.eventName() + ".")) {
			String entityName = event.getType().substring(UpdateKind.REFRESH.eventName().length()+1);
			if (entityNames.contains(entityName))
				fullRefresh();
		}
	}	
	
	
	/**
	 * 	Clear collection content
	 */
	@Override
	@PreDestroy
	public void clear() {
        clearLocalIndex();
        first = 0;
        last = first+max;
        count = 0;
        getWrappedList().clear();
		initializing = true;
		initSent = false;
        fullRefresh = false;
        filterRefresh = false;
	}
	
	
	private List<Integer[]> pendingRanges = new ArrayList<Integer[]>();
	private List<Future<?>> pendingCalls = new ArrayList<Future<?>>();
	
	
	private void executeFind(int first, int last) {
		log.debug("find from %d to %d", first, last);
		
		if (cancelPendingCalls) {
			for (Future<?> pendingCall : pendingCalls)
				pendingCall.cancel(true);
		}
		
		pendingRanges.add(0, new Integer[] { first, last });
		pendingCalls.add(0, find(first, last));
	}
	
	/**
	 *	Trigger a results query for the current filter
	 *	@param first	: index of first required result
	 *  @param last     : index of last required result
	 */
	protected Future<?> find(int first, int last) {
		int max = 0;
		if (this.initializing && this.max > 0)
			max = this.max;
		else if (!this.initializing)
		    max = last-first;
		
		Object filter = cloneFilter();		
		return doFind(filter, first, max);
	}
	
	protected synchronized Future<?> doFind(Object filter, int first, int max) {
		// Force evaluation of max, results and count
		if (sortAdapter != null)
			sortAdapter.retrieve(sortInfo);
		
		String[] order = sortInfo.getOrder();
		if (order != null && order.length == 0)
			order = null;
		boolean[] desc = sortInfo.getDesc();
		if (desc != null && desc.length == 0)
			desc = null;
		
		initFilterFinder();
		if (pageFilterFinder != null) {
			PageInfo pageInfo = new PageInfo(first, max, order, desc);
			PagedCollectionResponder<Page<E>> findResponder = new PagedCollectionResponder<Page<E>>(first, max);
			return pageFilterFinder.find(filter, pageInfo, findResponder);
		}
		PagedCollectionResponder<Map<String, Object>> findResponder = new PagedCollectionResponder<Map<String, Object>>(first, max);
		return simpleFilterFinder.find(filter, first, max, order, desc, findResponder);
	}
	
	public abstract F getFilter();
	
	protected abstract Object cloneFilter();
	
	private void initFilterFinder() {
		if (pageFilterFinder != null || simpleFilterFinder != null)
			return;
		
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
		if (usePage)
			pageFilterFinder = new ComponentPageFilterFinder(component, methodName);
		else
			simpleFilterFinder = new ComponentSimpleFilterFinder(component, methodName);
	}
	
	private final class ComponentPageFilterFinder implements PageFilterFinder<E> {
		private final Component component;
		private final String methodName;
		
		public ComponentPageFilterFinder(Component component, String methodName) {
			this.component = component;
			this.methodName = methodName;
		}

		@Override
		public Future<Page<E>> find(Object filter, PageInfo pageInfo, TideResponder<Page<E>> responder) {
			return component.call(methodName, filter, pageInfo, responder);
		}
	}
	
	private final class ComponentSimpleFilterFinder implements SimpleFilterFinder<E> {
		private final Component component;
		private final String methodName;
		
		public ComponentSimpleFilterFinder(Component component, String methodName) {
			this.component = component;
			this.methodName = methodName;
		}

		@Override
		public Future<Map<String, Object>> find(Object filter, int first, int max, String[] order, boolean[] desc, TideResponder<Map<String, Object>> responder) {
			return component.call(methodName, filter, first, max, order, desc, responder);
		}
	}
	
	
	/**
	 *  Build a result object from the result event
	 *  
	 *  @param event the result event
	 *  @param first first index requested
	 *  @param max max elements requested
	 *   
	 *  @return a Page object containing data from the collection
	 *      resultList   : the retrieved data
	 *      resultCount  : the total count of elements (non paged)
	 *      firstResult  : the index of the first retrieved element
	 *      maxResults   : the maximum count of retrieved elements 
	 */
	
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
	 *	Force refresh of collection when filter/sort have been changed
	 * 
	 *  @return always false
	 */
	public boolean fullRefresh() {
	    this.fullRefresh = true;
	    return refresh();
	}
	
	/**
	 *	Refresh collection with new filter/sort parameters
	 * 
	 *  @return always false
	 */
	public boolean refresh() {
		Object filter = getFilter();
		if (filter != null && this.context.getEntityManager().isDeepDirtyEntity(filter)) {
			filterRefresh = true;
			fullRefresh = true;
		}
		
		// Recheck sort fields to listen for asc/desc change events
		pendingRanges.clear();
		pendingCalls.clear();
		
		if (fullRefresh) {
			log.debug("full refresh");
			
			clearLocalIndex();
			
			fullRefresh = false;
			if (filterRefresh) {
			    first = 0;
			    last = first+max;
			    filterRefresh = false;
			}
        }
        else
			log.debug("refresh");			
        
		if (!initialFind())
			executeFind(first, last);
		return true;
	}
	
	private boolean initialFind() {
		if (max > 0 && !initializing)
			return false;
		
		if (!initSent) {
			log.debug("initial find");
			executeFind(0, max);
			initSent = true;
		}
		return true;
	}
	
	private void clearLocalIndex() {
		localIndex = null;
	}
	
	
	/**
	 *  Notify listeners of remote page result
	 *  
	 *  @param event the remote event (ResultEvent or FaultEvent)
	 *  @param previousFirst index of first element before last updated list
	 *  @param previousLast index of last element before last updated list
	 *  @param savedSnapshot collection snapshot before last change
	 */
	protected abstract void firePageChange(TideRpcEvent event, int previousFirst, int previousLast, List<E> savedSnapshot);
	
	
	/**
	 *  Initialize collection after first find
	 *   
	 *  @param event the result event of the first find
	 */
	protected void initialize(TideResultEvent<?> event) {
	}
	
	/**
	 *	Event handler for results query
	 * 
	 *  @param event the result event
	 *  @param first first requested index
	 *  @param max max elements requested
	 */
	protected void findResult(TideResultEvent<?> event, int first, int max) {
	    Page<E> page = getResult(event, first, max);
    	
	    handleResult(page, event, first, max);
	}
	
	/**
	 *	Event handler for results query
	 *
	 *  @param page the result page
	 *  @param event the result event
	 *  @param first first requested index
	 *  @param max max elements requested
	 */
	@SuppressWarnings("unchecked")
	protected void handleResult(Page<E> page, TideResultEvent<?> event, int first, int max) {
		List<E> list = page.getResultList();
		
		log.debug("handle result %d - %d (%s)", first, max, pendingRanges);
		
		int pendingIndex = -1;
		for (Iterator<Integer[]> ipr = pendingRanges.iterator(); ipr.hasNext(); ) {
			Integer[] pr = ipr.next();
			pendingIndex++;
			if (pr[0] == first && pr[1] == first+max) {
				ipr.remove();
				pendingCalls.remove(pendingIndex);
				break;
			}
		}
		
		if (initializing && event != null) {
			if (this.max == 0 && page.getMaxResults() > 0)
		    	this.max = page.getMaxResults();
		    initialize(event);
		}
		
		if (pendingIndex > 0)
			return;
		
		int nextFirst = page.getFirstResult();
		int nextLast = nextFirst + page.getMaxResults();
		
		int pageNum = max > 0 ? nextFirst / max : 0;
		log.debug("handle result page %d (%d - %d)", pageNum, nextFirst, nextLast);

        count = page.getResultCount();

        if (localIndex != null) {
            List<String> entityNames = new ArrayList<String>();
            for (int i = 0; i < localIndex.length; i++) {
            	if (localIndex[i] == null)
            		continue;
            	
                String entityName = localIndex[i].getClass().getSimpleName();
                if (!entityName.equals(elementName))
                    entityNames.remove(entityName);
            }
        }
        for (Object o : list) {
            if (elementClass == null || (o != null && o.getClass().isAssignableFrom(elementClass)))
                elementClass = (Class<? extends E>)o.getClass();
        }
        
        if (elementClass == null) {
        	localIndex = (E[])new Object[0];
        	log.warn("Cannot determine elementClass from empty content, consider calling setElementClass manually");
        }
        else {
	        localIndex = (E[])Array.newInstance(elementClass, list.size());
	        localIndex = list.toArray(localIndex);
	        if (localIndex != null) {
	            for (int i = 0; i < localIndex.length; i++) {
	            	if (localIndex[i] == null)
	            		continue;
	            	
	                String entityName = localIndex[i].getClass().getSimpleName();
	                if (!entityName.equals(elementName))
	                    entityNames.add(entityName);
	            }
	        }
        }
        
        int previousFirst = this.first;
        int previousLast = this.last;

        this.first = nextFirst;
        this.last = nextLast;

        List<E> savedSnapshot = null;
        
    	if (initializing) {
            initializing = false;

            getWrappedList().addAll(list);
        }
        else {
    		log.debug("Adjusting from %d-%d to %d-%d size %d", previousFirst, previousLast, nextFirst, nextLast, list.size());
    		
    		// Adjust internal list to expected results without triggering events
	    	if (nextFirst > previousFirst && nextFirst < previousLast) {
    			getInternalWrappedList().subList(0, Math.min(getInternalWrappedList().size(), nextFirst - previousFirst)).clear();
	    		for (int i = 0; i < nextFirst - previousFirst && previousLast - nextFirst + i < list.size(); i++) {
	    			E elt = list.get(previousLast - nextFirst + i);
    				getInternalWrappedList().add(elt);
	    		}
	    	}
	    	else if (nextFirst == previousFirst && nextLast > previousLast) {
	    		for (int i = 0; i < (nextLast-nextFirst)-(previousLast-previousFirst) && previousLast + i < list.size(); i++) {
	    			E elt = list.get(previousLast + i);
    				getInternalWrappedList().add(elt);
	    		}
	    	}
	    	else if (nextLast > previousFirst && nextLast < previousLast) {
	    		if (nextLast-previousFirst < getInternalWrappedList().size())
	    			getInternalWrappedList().subList(nextLast-previousFirst, getInternalWrappedList().size()).clear();
	    		else
	    			getInternalWrappedList().clear();
	    		for (int i = 0; i < previousFirst - nextFirst && i < list.size(); i++) {
	    			E elt = list.get(i);
    				getInternalWrappedList().add(i, elt);
	    		}
	    	}
	    	else if (nextFirst >= this.last || nextLast <= previousFirst) {
	    		getInternalWrappedList().clear();
	    		for (int i = 0; i < list.size(); i++) {
	    			E elt = list.get(i);
    				getInternalWrappedList().add(i, elt);
	    		}
	    	}
	    	else {
	    		savedSnapshot = new ArrayList<E>(getInternalWrappedList());
	    		getInternalWrappedList().clear();
	    		getInternalWrappedList().addAll(list);
	    	}
    	}
		
		firePageChange(event, previousFirst, previousLast, savedSnapshot);
	}
	
	/**
	 *	Event handler for results fault
	 *  
	 *  @param event the fault event
	 *  @param first first requested index
	 *  @param max max elements requested
	 */
	protected void findFault(TideFaultEvent event, int first, int max) {
		handleFault(event, first, max);
	}
	
	/**
	 *	Event handler for results query fault
	 * 
	 *  @param event the fault event
	 */
	@SuppressWarnings("unchecked")
	protected void handleFault(TideFaultEvent event, int first, int max) {
		log.debug("findFault: %s", event);
		
		int pendingIndex = -1;
		for (Iterator<Integer[]> ipr = pendingRanges.iterator(); ipr.hasNext(); ) {
			Integer[] pr = ipr.next();
			pendingIndex++;
			if (pr[0] == first && pr[1] == first+max) {
				ipr.remove();
				pendingCalls.remove(pendingIndex);
				break;
			}
		}
	    
		if (initializing)
			initSent = false;
		
		firePageChange(event, this.first, this.last, Collections.EMPTY_LIST);
	}
	
	
	protected abstract List<E> getInternalWrappedList();
	
	protected abstract List<E> getWrappedList();
	
	
	/**
	 * 	Override of get() with lazy page loading
	 * 
	 *	@param index index of requested item
	 *  @return object at specified index
	 */
	@Override
	public E get(int index) {
		if (index < 0)
			return null;
	
		if (initialFind())
			return null;

		if (localIndex != null && index >= first && index < last) {	// Local data available for index
		    int j = index-first;
		    if (j >= 0 && j < localIndex.length)
		    	return localIndex[j];
		    // Index not in current loaded range, max is more than last page size
		    return null;
		}
		
		// If already in a pending range, return null
		for (Integer[] pendingRange : pendingRanges) {
			if (index >= pendingRange[0] && index < pendingRange[1])
				return null;
		}
	    
	    int page = index / max;
	    
		// Trigger a results query for requested page
		int nfi = 0;
		int nla = 0;
		@SuppressWarnings("unused")
		int idx = page * max;
		if (index >= last && index < last + max) {
			nfi = first;
			nla = last + max;
			if (nla > nfi + 2*max)
			    nfi = nla - 2*max;
			if (nfi < 0)
			    nfi = 0;
			if (nla > count)
			    nla = count;
		}
		else if (index < first && index >= first - max) {
			nfi = first - max;
			if (nfi < 0)
				nfi = 0;
			nla = last;
			if (nla > nfi + 2*max)
			    nla = nfi + 2*max;
			if (nla > count)
			    nla = count;
		}
		else {
			nfi = index - max;
			nla = nfi + 2 * max;
			if (nfi < 0)
				nfi = 0;
			if (nla > count)
			    nla = count;
		}
		log.debug("request find for index " + index);
		executeFind(nfi, nla);
		return null;
	}
	
	
	@Override
	public boolean isEmpty() {
		return size() == 0;
	}


	@Override
	public boolean contains(Object o) {
		if (o == null)
			return false;
		
		if (localIndex != null) {
			for (Object obj : localIndex) {
				if (o.equals(obj))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return false;
	}

	@Override
	public int indexOf(Object o) {
		if (o == null)
			return -1;
		
		if (localIndex != null) {
			for (int i = 0; i < localIndex.length; i++) {
				if (o.equals(localIndex[i]))
					return first+i;
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		if (o == null)
			return -1;
				
		if (localIndex != null) {
			int index = -1;
			for (int i = 0; i < localIndex.length; i++) {
				if (o.equals(localIndex[i]))
					index = first+i;;
			}
			return index;
		}
		return -1;
	}

	@Override
	public Iterator<E> iterator() {
		return new PagedCollectionIterator();
	}

	@Override
	public ListIterator<E> listIterator() {
		return new PagedCollectionIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return new PagedCollectionIterator();
	}
	
	
	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	
	public class PagedCollectionIterator implements ListIterator<E> {
		
		private ListIterator<E> wrappedListIterator;
		
		public PagedCollectionIterator() {
			wrappedListIterator = getWrappedList().listIterator();
		}

		public PagedCollectionIterator(int index) {
			wrappedListIterator = getWrappedList().listIterator(index);
		}

		@Override
		public boolean hasNext() {
			return wrappedListIterator.hasNext();
		}
	
		@Override
		public E next() {
			return wrappedListIterator.next();
		}
	
		@Override
		public boolean hasPrevious() {
			return wrappedListIterator.hasPrevious();
		}
	
		@Override
		public E previous() {
			return wrappedListIterator.previous();
		}
	
		@Override
		public int nextIndex() {
			return wrappedListIterator.nextIndex();
		}
	
		@Override
		public int previousIndex() {
			return wrappedListIterator.previousIndex();
		}
	
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	
		@Override
		public void set(E e) {
			throw new UnsupportedOperationException();
		}
	
		@Override
		public void add(E e) {
			throw new UnsupportedOperationException();
		}
		
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
	
	
	private class PagedCollectionResponder<R> implements TideResponder<R> {
	    
	    private int first;
	    private int max;
	    
	    
	    public PagedCollectionResponder(int first, int max) {
	        this.first = first;
	        this.max = max;
	    }
	    
	    @Override
	    public void result(TideResultEvent<R> event) {
            findResult(event, first, max);
	    }
	    
	    public void fault(TideFaultEvent event) {
            findFault(event, first, max);
	    } 
	}
}
