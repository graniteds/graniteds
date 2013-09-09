package org.granite.client.collection;

import org.granite.client.collection.CollectionChangeEvent.Kind;

public class CollectionChangeSupport {

	private final Object collection;
	private CollectionChangeListener[] listeners = null;
	
	public CollectionChangeSupport(Object collection) {
		this.collection = collection;
	}
	
	public void addCollectionChangeListener(CollectionChangeListener listener) {
		if (listeners == null)
			listeners = new CollectionChangeListener[] { listener };
		else {
			CollectionChangeListener[] newListeners = new CollectionChangeListener[listeners.length+1];
			System.arraycopy(listeners, 0, newListeners, 0, listeners.length);
			newListeners[listeners.length] = listener;
			listeners = newListeners;
		}
	}
	
	public void fireCollectionChangeEvent(Kind kind, Object key, Object[] values) {
		if (listeners == null)
			return;
		CollectionChangeEvent event = new CollectionChangeEvent(collection, kind, key, values);
		for (CollectionChangeListener listener : listeners)
			listener.collectionChange(event);
	}
	
	public void removeCollectionChangeListener(CollectionChangeListener listener) {
		if (listeners == null)
			return;
		if (listeners.length == 1) {
			if (listeners[0] == listener)
				listeners = null;
		}
		else {
			int index = -1;
			for (int i = 0; i < listeners.length; i++) {
				if (listeners[i] == listener) {
					index = i;
					break;
				}
			}
			if (index >= 0) {
				CollectionChangeListener[] newListeners = new CollectionChangeListener[listeners.length-1];
				if (index > 0)
					System.arraycopy(listeners, 0, newListeners, 0, index);
				if (index < listeners.length-1)
					System.arraycopy(listeners, index+1, newListeners, index, listeners.length-index-1);
				listeners = newListeners;
			}
		}
	}
}
