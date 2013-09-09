package org.granite.client.collection;


public interface ObservableCollection {
	
	public void addCollectionChangeListener(CollectionChangeListener listener);
	
	public void removeCollectionChangeListener(CollectionChangeListener listener);
	
}
