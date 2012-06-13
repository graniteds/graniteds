package org.granite.test.tide;

import java.util.Set;

import javax.enterprise.inject.Alternative;

import org.granite.tide.data.DataUpdatePostprocessor;


@Alternative
public class TestDataUpdatePostprocessor implements DataUpdatePostprocessor {

	@Override
	public Set<Object[]> process(Set<Object[]> updates) {
		for (Object[] update : updates)
			update[1] = new WrappedUpdate(update[1]);
		return updates;
	}

	public static class WrappedUpdate {
		
		private Object entity;
		
		public WrappedUpdate(Object entity) {
			this.entity = entity;
		}
		
		public Object getEntity() {
			return entity;
		}
	}
}
