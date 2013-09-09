package org.granite.client.collection;

public class CollectionChangeEvent {

	private Object collection;
	private Kind kind;
	private Object key;
	private Object[] values;
	
	
	public CollectionChangeEvent(Object collection, Kind kind, Object key, Object[] values) {
		this.collection = collection;
		this.kind = kind;
		this.key = key;
		this.values = values;
	}
	
	public Object getCollection() {
		return collection;
	}
	
	public Kind getKind() {
		return kind;
	}
	
	public Object getKey() {
		return key;
	}

	public Object[] getValues() {
		return values;
	}


	public enum Kind {
		ADD,
		REMOVE,
		REPLACE,
		UPDATE,
		CLEAR
	}
}
