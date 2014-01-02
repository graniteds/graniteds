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
package org.granite.client.javafx.tide.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PreDestroy;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.granite.client.javafx.util.ListListenerHelper;
import org.granite.client.javafx.util.ListenerHelper;
import org.granite.client.tide.collection.AbstractPagedCollection;
import org.granite.client.tide.server.TideRpcEvent;

/**
 * @author William DRAI
 */
public abstract class PagedCollection<E> extends AbstractPagedCollection<E> implements ObservableList<E> {
	
    private List<E> internalWrappedList = new ArrayList<E>();
    protected ObservableList<E> wrappedList;
    
    private ListenerHelper<PageChangeListener<E>> pageChangeHelper = new ListenerHelper<PageChangeListener<E>>(PageChangeListener.class);
    
    
    @Override
    protected List<E> getInternalWrappedList() {
    	return internalWrappedList;
    }
    
    @Override
    protected List<E> getWrappedList() {
    	return wrappedList;
    }
	

	public PagedCollection() {
		super();
		
		wrappedList = FXCollections.observableList(internalWrappedList);
		wrappedList.addListener(new WrappedListListChangeListener());
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
	
	public void addListener(PageChangeListener<E> listener) {
		pageChangeHelper.addListener(listener);
	}
	
	public void removeListener(PageChangeListener<E> listener) {
		pageChangeHelper.removeListener(listener);
	}
	
	public void firePageChange(TideRpcEvent event) {
		fireItemsUpdated(0, Math.min(this.count, this.last)-this.first);
		pageChangeHelper.fireEvent(this, event);
	}
	
	
	public class WrappedListListChangeListener implements ListChangeListener<E> {		
	    @Override
	    public void onChanged(ListChangeListener.Change<? extends E> change) {
	    	ListChangeWrapper wrappedChange = new ListChangeWrapper(wrappedList, change);
    		helper.fireValueChangedEvent(wrappedChange);
	    }
	}
	
	public void fireItemsUpdated(final int from, final int to) {
        if (to <= from)
            return;
		ListChangeListener.Change<E> change = new ListChangeListener.Change<E>(wrappedList) {
			@Override
			public int getFrom() {
				return first+from;
			}

			@Override
			public int getTo() {
				return first+to;
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
				return null;
			}

			@Override
			public boolean next() {
				return false;
			}

			@Override
			public void reset() {
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
		return wrappedList.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return wrappedList.toArray(a);
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
