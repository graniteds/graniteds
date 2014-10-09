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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import javax.annotation.PreDestroy;
import javax.inject.Named;

import org.granite.client.javafx.util.ListListenerHelper;
import org.granite.client.javafx.util.ListenerHelper;
import org.granite.client.tide.collection.AbstractPagedCollection;
import org.granite.client.tide.collection.PageFilterFinder;
import org.granite.client.tide.collection.SimpleFilterFinder;
import org.granite.client.tide.server.Component;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideResultEvent;
import org.granite.client.tide.server.TideRpcEvent;

/**
 * @author William DRAI
 */
@Named
public class PagedQuery<E, F> extends AbstractPagedCollection<E, F> implements ObservableList<E> {
	
    private List<E> internalWrappedList = new ArrayList<E>();
    protected ObservableList<E> wrappedList;
    
    private ListenerHelper<PageChangeListener<E, F>> pageChangeHelper = new ListenerHelper<PageChangeListener<E, F>>(PageChangeListener.class);
	
    private Map<String, Object> internalFilterMap;
    private ObservableMap<String, Object> filterMap;
    private ObjectProperty<F> filter;
    private final ReadOnlyIntegerWrapper count = new ReadOnlyIntegerWrapper(this, "count", 0);
    
    
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
	
	private void initWrappedList() {
		wrappedList = FXCollections.observableList(internalWrappedList);
		wrappedList.addListener(new WrappedListListChangeListener());
	}
	
	public ReadOnlyIntegerProperty countProperty() {
		return count.getReadOnlyProperty();
	}
	
	@Override
	protected void updateCount(int cnt) {
		super.updateCount(cnt);
		count.set(cnt);
	}
    
	@Override
    protected void initFilter() {
		this.internalFilterMap = new HashMap<String, Object>();
		this.filterMap = FXCollections.observableMap(Collections.synchronizedMap(internalFilterMap));
    	this.filterMap.addListener(new MapChangeListener<String, Object>() {
			@Override
			public void onChanged(MapChangeListener.Change<? extends String, ?> change) {
				fullRefresh = true;
				filterRefresh = true;
			}
    	});
		this.filter = new SimpleObjectProperty<F>(this, "filter");
    }
	
	
	public ObjectProperty<F> filterProperty() {
		return filter;
	}
	@SuppressWarnings("unchecked")
	public F getFilter() {
		if (filter.get() != null)
			return filter.get();
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
			this.filter.set(filter);
	}	
	@SuppressWarnings("unchecked")
	@Override
	protected F cloneFilter() {
		if (this.filter.get() != null)
			return this.filter.get();
		else {
			// Copy filter map to avoid concurrent modifications
			synchronized (internalFilterMap) {
				return (F)new HashMap<String, Object>(internalFilterMap);
			}
		}
	}
	
	
	@Override
	public boolean setAll(Collection<? extends E> coll) {
		if (!initializing)
			return fullRefresh();
		return false;
	}
	
	
	@Override
	@PreDestroy
	public void clear() {
		super.clear();
        helper.clear();
        pageChangeHelper.clear();
	}
	
	
	private ListListenerHelper<E> helper = new ListListenerHelper<E>();
	
	public void addListener(ListChangeListener<? super E> listener) {
		helper.addListener(listener);
    }

	public void removeListener(ListChangeListener<? super E> listener) {
		helper.removeListener(listener);
    }
	
	public void addListener(InvalidationListener listener) {
		helper.addListener(listener);
    }

	public void removeListener(InvalidationListener listener) {
		helper.removeListener(listener);
    }
	
	public void addListener(PageChangeListener<E, F> listener) {
		pageChangeHelper.addListener(listener);
	}
	
	public void removeListener(PageChangeListener<E, F> listener) {
		pageChangeHelper.removeListener(listener);
	}
	
	public void firePageChange(TideRpcEvent event, int previousFirst, int previousLast, List<E> savedSnapshot) {
		if (event instanceof TideResultEvent<?>)
			fireItemsUpdated(0, Math.min(super.count, this.last)-this.first, previousFirst, previousLast, savedSnapshot);
		
		pageChangeHelper.fireEvent(this, event);
	}
	
	
	public class WrappedListListChangeListener implements ListChangeListener<E> {		
	    @Override
	    public void onChanged(ListChangeListener.Change<? extends E> change) {
	    	ListChangeWrapper wrappedChange = new ListChangeWrapper(wrappedList, change);
    		helper.fireValueChangedEvent(wrappedChange);
	    }
	}
	
	public void fireItemsUpdated(final int from, final int to, int previousFrom, int previousTo, final List<E> savedSnapshot) {
		if (savedSnapshot != null) {
			// Detect removals
			final List<Integer> removals = new ArrayList<Integer>();
			for (int i = 0; i < savedSnapshot.size(); i++) {
				if (!getInternalWrappedList().contains(savedSnapshot.get(i)))
					removals.add(i);
			}
			
			// Detect additions
			final List<Integer> adds = new ArrayList<Integer>();
			if (from == previousFrom && to == previousTo) {
				for (int i = 0; i < getInternalWrappedList().size(); i++) {
					if (!savedSnapshot.contains(getInternalWrappedList().get(i)))
						adds.add(i);
				}
			}
			
			// Detect permutations
			int start = -1;
			for (int i = 0; i < savedSnapshot.size(); i++) {
				if (getInternalWrappedList().contains(savedSnapshot.get(i))) {
					start = i;
					break;
				}
			}
			final List<Integer> permutations = new ArrayList<Integer>();
			if (start >= 0) {
				for (int i = start; i < savedSnapshot.size(); i++) {
					int idx = getInternalWrappedList().indexOf(savedSnapshot.get(i));
					if (idx < 0)
						break;
					permutations.add(first+idx);
				}
			}
			final int[] perms = permutations.size() > 0 ? new int[permutations.size()] : null;
			for (int i = 0; i < permutations.size(); i++)
				perms[i] = permutations.get(i);
			
			final int permutationStart = start;
			
			// Notify of elements removed
			ListChangeListener.Change<E> change = new ListChangeListener.Change<E>(wrappedList) {
				private int changeIndex = -1;
				
				@Override
				public int getFrom() {
					if (changeIndex < removals.size())
						return first+removals.get(changeIndex);
					if (changeIndex >= removals.size() && changeIndex < removals.size()+adds.size())
						return first+adds.get(changeIndex-removals.size());
					if (perms != null)
						return first+permutationStart;
					return -1;
				}
				
				@Override
				public int getTo() {
					if (changeIndex < removals.size())
						return first+removals.get(changeIndex);
					if (changeIndex >= removals.size() && changeIndex < removals.size()+adds.size())
						return first+adds.get(changeIndex-removals.size());
					if (perms != null)
						return first+permutationStart+perms.length;
					return -1;
				}
				
				@Override
				public boolean wasUpdated() {
					return false;
				}

				@Override
				public boolean wasAdded() {
					return changeIndex >= removals.size() && changeIndex < removals.size()+adds.size();
				}
				
				@Override
				public int getAddedSize() {
					if (changeIndex >= removals.size() && changeIndex < removals.size()+adds.size())
						return 1;
					return -1;
				}
				
				@Override
				public List<E> getAddedSubList() {
					if (changeIndex >= removals.size() && changeIndex < removals.size()+adds.size())
						return internalWrappedList.subList(adds.get(changeIndex), adds.get(changeIndex)+1);
					return Collections.emptyList();
				}
				
				@Override
				protected int[] getPermutation() {
					if (changeIndex == removals.size()+adds.size())
						return perms;
					return EMPTY_PERMUTATION;
				}
			    
			    @Override
		        public int getPermutation(int i) {
		            if (changeIndex < removals.size()+adds.size()) {
		                throw new IllegalStateException("Not a permutation change");
		            }
		            if (i-getFrom() >= 0 && i-getFrom() < perms.length)
		            	return perms[i - getFrom()];
		            return -1;
		        }

				@Override
				public boolean wasRemoved() {
					return changeIndex < removals.size();
				}
				
				@Override
				public List<E> getRemoved() {
					if (changeIndex < removals.size())
						return Collections.singletonList(savedSnapshot.get(removals.get(changeIndex)));
					return Collections.emptyList();
				}
				
				@Override
				public boolean next() {
					changeIndex++;
					return perms != null ? changeIndex <= removals.size()+adds.size() : changeIndex < removals.size()+adds.size();
				}
				
				@Override
				public void reset() {
					changeIndex = -1;
				}			
			};
			helper.fireValueChangedEvent(change);
			
			return;
		}
		
		if (to < from)
			return;
		
        // Notify of content change
		ListChangeListener.Change<E> change = new ListChangeListener.Change<E>(wrappedList) {
			private boolean next = true;
			
			@Override
			public int getFrom() {
				return from;
			}

			@Override
			public int getTo() {
				return to;
			}
			
			@Override
			public boolean wasUpdated() {
				return true;
			}
			
			@Override
			protected int[] getPermutation() {
				return EMPTY_PERMUTATION;
			}

			@Override
			public List<E> getRemoved() {
				return Collections.emptyList();
			}
			
			@Override
			public boolean next() {
				if (next) {
					next = false;
					return true;
				}
				return false;
			}

			@Override
			public void reset() {
				next = true;
			}			
		};
		helper.fireValueChangedEvent(change);
	}

    private static final int[] EMPTY_PERMUTATION = new int[0];
    
	public class ListChangeWrapper extends ListChangeListener.Change<E> {            
	    private final ListChangeListener.Change<? extends E> wrappedChange;
	    
	    public ListChangeWrapper(ObservableList<E> list, ListChangeListener.Change<? extends E> wrappedChange) {
	        super(list);
	        this.wrappedChange = wrappedChange;
	    }

	    @Override
		public int getAddedSize() {
			return wrappedChange.getAddedSize();
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<E> getAddedSubList() {
			return (List<E>)wrappedChange.getAddedSubList();
		}

		@Override
		public int getRemovedSize() {
            return wrappedChange.getRemovedSize();
		}

		@Override
		public boolean wasAdded() {
			return wrappedChange.wasAdded();
		}

		@Override
		public boolean wasPermutated() {
			return wrappedChange.wasPermutated();
		}

		@Override
		public boolean wasRemoved() {
			return wrappedChange.wasRemoved();
		}

		@Override
		public boolean wasReplaced() {
			return wrappedChange.wasReplaced();
		}

		@Override
		public boolean wasUpdated() {
			return wrappedChange.wasUpdated();
		}

		@Override
	    public int getFrom() {
			int from = wrappedChange.getFrom();
	        return from+first;
	    }

	    @Override
	    public int getTo() {
	    	int to = wrappedChange.getTo();
	        return to+first;
	    }

	    @Override
	    protected int[] getPermutation() {
	    	return EMPTY_PERMUTATION;
	    }

	    @Override
	    public int getPermutation(int num) {
	        return wrappedChange.getPermutation(num);
	    }

	    @SuppressWarnings("unchecked")
		@Override
	    public List<E> getRemoved() {
	        return (List<E>)wrappedChange.getRemoved();
	    }

	    @Override
	    public boolean next() {
	        return wrappedChange.next();
	    }

	    @Override
	    public void reset() {
	        wrappedChange.reset();
	    }        
	}
	
	
	@Override
	public Object[] toArray() {
		return internalWrappedList.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return internalWrappedList.toArray(a);
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

	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(E... elements) {
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
	public void remove(int from, int to) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean removeAll(E... elements) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean retainAll(E... elements) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean setAll(E... elements) {
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
}
