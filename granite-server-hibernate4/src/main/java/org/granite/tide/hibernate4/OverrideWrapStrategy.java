package org.granite.tide.hibernate4;

import org.hibernate.event.service.spi.DuplicationStrategy;

public class OverrideWrapStrategy<E> implements DuplicationStrategy {

	@SuppressWarnings("unchecked")
	public boolean areMatch(Object listener, Object original) {
		Class<?> clazz = original.getClass();
		while (clazz != null && !clazz.equals(Object.class)) {
			for (Class<?> spiInterface : clazz.getInterfaces()) {
				if (spiInterface.getName().startsWith("org.hibernate.event.spi.")) {
					if (listener instanceof EventListenerWrapper<?> && spiInterface.isInstance(listener)) {
						((EventListenerWrapper<E>)listener).setWrappedListener((E)original);
						return true;
					}
				}				
			}
			clazz = clazz.getSuperclass();
		}
		return false;
	}
	
	public Action getAction() {
		return Action.REPLACE_ORIGINAL;
	}
	
}