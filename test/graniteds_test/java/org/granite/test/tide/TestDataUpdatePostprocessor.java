package org.granite.test.tide;

import java.util.List;

import javax.enterprise.inject.Alternative;

import org.granite.tide.data.DataContext.EntityUpdate;
import org.granite.tide.data.DataUpdatePostprocessor;


@Alternative
public class TestDataUpdatePostprocessor implements DataUpdatePostprocessor {

	@Override
	public List<EntityUpdate> process(List<EntityUpdate> updates) {
		for (EntityUpdate update : updates)
			update.entity = new WrappedUpdate(update.entity);
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
