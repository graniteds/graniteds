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
package org.granite.client.tide.collection;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.granite.client.tide.data.EntityManager.UpdateKind;
import org.granite.client.tide.events.TideEvent;
import org.granite.client.tide.events.TideEventObserver;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.client.tide.server.TideResponder;
import org.granite.client.tide.server.TideResultEvent;
import org.granite.client.tide.server.TideRpcEvent;
import org.granite.logging.Logger;
import org.granite.tide.data.model.Page;

/**
 * @author William DRAI
 */
public abstract class AbstractPagedCollection<E> implements List<E>, TideEventObserver {
	
    private static final Logger log = Logger.getLogger(AbstractPagedCollection.class);
    
	
    protected boolean initializing = false;
    private boolean initSent = false;
    
	protected int first;
    protected int last;			// Current last index of local data
    protected int max;           // Page size
    protected int count;         // Result count
    private E[] localIndex = null;
    
	protected boolean fullRefresh = false;
	protected boolean filterRefresh = false;
	

	public AbstractPagedCollection() {
		super();
	    log.debug("create collection");
		first = 0;
		last = 0;
		count = 0;
		initializing = true;
	}
	
	
	/**
	 *	Get total number of elements
	 *  
	 *  @return collection total size
	 */
	@Override
	public int size() {
		if (initialFind())
	        return 0;
	    else if (localIndex == null)
	        return 0;
		return count;
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
		initializing = true;
		initSent = false;
		getInternalWrappedList().clear();
		clearLocalIndex();
		first = 0;
		last = first+max;
		fullRefresh = false;
		filterRefresh = false;
	}
	
	
	private List<Integer[]> pendingRanges = new ArrayList<Integer[]>();
	
	/**
	 *	Abstract method: trigger a results query for the current filter
	 *	@param first	: index of first required result
	 *  @param last     : index of last required result
	 */
	protected void find(int first, int last) {
		log.debug("find from %d to %d", first, last);
		
		pendingRanges.add(new Integer[] { first, last });
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
		// Recheck sort fields to listen for asc/desc change events
		pendingRanges.clear();
		
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
			find(first, last);
		return true;
	}
	
	private boolean initialFind() {
		if (max > 0 && !initializing)
			return false;
		
		if (!initSent) {
			log.debug("initial find");
			find(0, max);
			initSent = true;
		}
		return true;
	}
	
	private void clearLocalIndex() {
		localIndex = null;
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
	protected abstract Page<E> getResult(TideResultEvent<?> event, int first, int max);
	
	
	/**
	 *  Notify listeners of remote page result
	 *  
	 *  @param event the remote event (ResultEvent or FaultEvent)
	 */
	protected abstract void firePageChange(TideRpcEvent event);
	
	
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
		List<E> list = (List<E>)page.getResultList();

		for (Iterator<Integer[]> ipr = pendingRanges.iterator(); ipr.hasNext(); ) {
			Integer[] pr = ipr.next();
			if (pr[0] == first && pr[1] == first+max) {
				ipr.remove();
				break;
			}
		}
		
		if (initializing && event != null) {
			if (this.max == 0 && page.getMaxResults() > 0)
		    	this.max = page.getMaxResults();
		    initialize(event);
		}
		
		int nextFirst = (Integer)page.getFirstResult();
		int nextLast = nextFirst + (Integer)page.getMaxResults();
		
		int pageNum = max > 0 ? nextFirst / max : 0;
		log.debug("handle result page %d (%d - %d)", pageNum, nextFirst, nextLast);

    	if (!initializing) {
    		log.debug("Adjusting from %d-%d to %d-%d size %d", AbstractPagedCollection.this.first, AbstractPagedCollection.this.last, nextFirst, nextLast, list.size());
    		// Adjust internal list to expected results without triggering events
	    	if (nextFirst > AbstractPagedCollection.this.first && nextFirst < AbstractPagedCollection.this.last) {
    			getInternalWrappedList().subList(0, Math.min(getInternalWrappedList().size(), nextFirst - AbstractPagedCollection.this.first)).clear();
	    		for (int i = 0; i < nextFirst - AbstractPagedCollection.this.first && AbstractPagedCollection.this.last - nextFirst + i < list.size(); i++) {
	    			E elt = list.get(AbstractPagedCollection.this.last - nextFirst + i);
    				getInternalWrappedList().add(elt);
	    		}
	    	}
	    	else if (nextFirst == AbstractPagedCollection.this.first && nextLast > AbstractPagedCollection.this.last) {
	    		for (int i = 0; i < (nextLast-nextFirst)-(AbstractPagedCollection.this.last-AbstractPagedCollection.this.first) && AbstractPagedCollection.this.last + i < list.size(); i++) {
	    			E elt = list.get(AbstractPagedCollection.this.last + i);
    				getInternalWrappedList().add(elt);
	    		}
	    	}
	    	else if (nextLast > AbstractPagedCollection.this.first && nextLast < AbstractPagedCollection.this.last) {
	    		if (nextLast-AbstractPagedCollection.this.first < getInternalWrappedList().size())
	    			getInternalWrappedList().subList(nextLast-AbstractPagedCollection.this.first, getInternalWrappedList().size()).clear();
	    		else
	    			getInternalWrappedList().clear();
	    		for (int i = 0; i < AbstractPagedCollection.this.first - nextFirst && i < list.size(); i++) {
	    			E elt = list.get(i);
    				getInternalWrappedList().add(i, elt);
	    		}
	    	}
	    	else if (nextFirst >= AbstractPagedCollection.this.last || nextLast <= AbstractPagedCollection.this.first) {
	    		getInternalWrappedList().clear();
	    		for (int i = 0; i < list.size(); i++) {
	    			E elt = list.get(i);
    				getInternalWrappedList().add(i, elt);
	    		}
	    	}
    	}
    	else
    		getWrappedList().addAll(list);
		
		count = page.getResultCount();
		
	    initializing = false;
		
	    if (localIndex != null) {
	    	List<String> entityNames = new ArrayList<String>();
	        for (int i = 0; i < localIndex.length; i++) {
				String entityName = localIndex[i].getClass().getSimpleName();
				if (!entityName.equals(elementName))
					entityNames.remove(entityName);
	        }
	    }
	    for (Object o : list) {
	    	if (elementClass == null || (o != null && o.getClass().isAssignableFrom(elementClass)))
	    		elementClass = (Class<? extends E>)o.getClass();
	    }
	    localIndex = (E[])Array.newInstance(elementClass, list.size());
		localIndex = list.toArray(localIndex);
	    if (localIndex != null) {
	        for (int i = 0; i < localIndex.length; i++) {
				String entityName = localIndex[i].getClass().getSimpleName();
				if (!entityName.equals(elementName))
					entityNames.add(entityName);
	        }
	    }
	    
		this.first = nextFirst;
		this.last = nextLast;
	    
		pendingRanges.clear();
		
		firePageChange(event);
	}
	
	/**
	 *	Event handler for results fault
	 *  
	 *  @param event the fault event
	 *  @param first first requested index
	 *  @param max max elements requested
	 */
	protected void findFault(TideFaultEvent event, int first, int max) {
		handleFault(event);
	}
	
	/**
	 *	Event handler for results query fault
	 * 
	 *  @param event the fault event
	 */
	protected void handleFault(TideFaultEvent event) {
		log.debug("findFault: %s", event);
		
		for (Iterator<Integer[]> ipr = pendingRanges.iterator(); ipr.hasNext(); ) {
			Integer[] pr = ipr.next();
			if (pr[0] == first && pr[1] == first+max) {
				ipr.remove();
				break;
			}
		}
	    
		if (initializing)
			initSent = false;
		
		firePageChange(event);
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
		find(nfi, nla);
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
					return first+i;;
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
	
	
	public class PagedCollectionResponder implements TideResponder<Object> {
	    
	    private int first;
	    private int max;
	    
	    
	    public PagedCollectionResponder(int first, int max) {
	        this.first = first;
	        this.max = max;
	    }
	    
	    @Override
	    public void result(TideResultEvent<Object> event) {
            findResult(event, first, max);
	    }
	    
	    public void fault(TideFaultEvent event) {
            findFault(event, first, max);
	    } 
	}
}
