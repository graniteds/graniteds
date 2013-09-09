package org.granite.client.android.binding;

public interface Getter<T, V> {
	
	public V getValue(T instance) throws Exception;
}
