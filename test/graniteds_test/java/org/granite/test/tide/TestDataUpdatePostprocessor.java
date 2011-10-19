package org.granite.test.tide;

import javax.enterprise.inject.Alternative;

import org.granite.tide.data.DataUpdatePostprocessor;


@Alternative
public class TestDataUpdatePostprocessor implements DataUpdatePostprocessor {

	@Override
	public Object process(Object entity) {
		return new WrappedUpdate(entity);
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
