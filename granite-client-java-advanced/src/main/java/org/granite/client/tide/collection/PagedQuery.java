/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.inject.Named;

import org.granite.binding.collection.CollectionChangeEvent;
import org.granite.binding.collection.CollectionChangeEvent.Kind;
import org.granite.binding.collection.CollectionChangeListener;
import org.granite.binding.collection.CollectionChangeSupport;
import org.granite.binding.collection.ObservableList;
import org.granite.binding.collection.ObservableListWrapper;
import org.granite.binding.collection.ObservableMap;
import org.granite.binding.collection.ObservableStringMapWrapper;
import org.granite.client.tide.server.Component;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideRpcEvent;

/**
 * @author William DRAI
 */
@Named
public class PagedQuery<E, F> extends AbstractPagedCollection<E, F> implements ObservableList<E> {
	
	protected CollectionChangeSupport ccs = new CollectionChangeSupport(this);
    private List<E> internalWrappedList = new ArrayList<E>();
    protected ObservableList<E> wrappedList;
    
    private Map<String, Object> internalFilterMap;
    private ObservableMap<String, Object> filterMap;
    private F filter;
    
    
    protected PagedQuery() {
    	// CDI proxying...
    }
    
    public PagedQuery(ServerSession serverSession) {
    	super(serverSession);
    	initWrappedList();
    }
    
    public PagedQuery(Component remoteComponent, String methodName, int maxResults) {
    	super(remoteComponent, methodName, maxResults);
    	initWrappedList();
    }
    
    public PagedQuery(Component remoteComponent, PageFilterFinder<E> finder, int maxResults) {
    	super(remoteComponent, finder, maxResults);
    	initWrappedList();
    }
    
    public PagedQuery(Component remoteComponent, SimpleFilterFinder<E> finder, int maxResults) {
    	super(remoteComponent, finder, maxResults);
    	initWrappedList();
    }

    @Override
    protected List<E> getInternalWrappedList() {
    	return internalWrappedList;
    }
    
    @Override
    protected List<E> getWrappedList() {
    	return wrappedList;
    }
	
	public void addCollectionChangeListener(CollectionChangeListener listener) {
		ccs.addCollectionChangeListener(listener);
	}
	
	public void removeCollectionChangeListener(CollectionChangeListener listener) {
		ccs.removeCollectionChangeListener(listener);
	}
	
	private void initWrappedList() {
		wrappedList = new ObservableListWrapper<E>(internalWrappedList);
		wrappedList.addCollectionChangeListener(new WrappedListCollectionChangeListener());
	}
    	
	@Override
    protected void initFilter() {
		this.internalFilterMap = new HashMap<String, Object>();
		this.filterMap = new ObservableStringMapWrapper<Object>(Collections.synchronizedMap(internalFilterMap));
    	this.filterMap.addCollectionChangeListener(new CollectionChangeListener() {
			@Override
			public void collectionChange(CollectionChangeEvent event) {
				fullRefresh = true;
				filterRefresh = true;
			}
		});
		this.filter = null;
    }
	
	
	@SuppressWarnings("unchecked")
	public F getFilter() {
		if (filter != null)
			return filter;
		try {
			return (F)filterMap;
		}
		catch (ClassCastException e) {
			return null;
		}
	}
	public void setFilter(F filter) {
		if (filter == null)
			internalFilterMap.clear();
		else
			this.filter = filter;
	}	
	@SuppressWarnings("unchecked")
	@Override
	protected F cloneFilter() {
		if (filter != null)
			return filter;

		// Copy filter map to avoid concurrent modifications
		synchronized (internalFilterMap) {
			return (F)new HashMap<String, Object>(internalFilterMap);
		}
	}
	
	
	@Override
	@PreDestroy
	public void clear() {
		super.clear();
		
		ccs = new CollectionChangeSupport(this);
	}


	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException();
	}
	
	
	public void firePageChange(TideRpcEvent event, int previousFirst, int previousLast, List<E> savedSnapshot) {
		ccs.fireCollectionChangeEvent(Kind.PAGE_CHANGE, event, null);
	}
	
	
	public class WrappedListCollectionChangeListener implements CollectionChangeListener {		
		@Override
		public void collectionChange(CollectionChangeEvent event) {
			ccs.fireCollectionChangeEvent(event.getKind(), event.getKey(), event.getValues());
		}
	}
}
