package org.granite.client.android.tide;

import org.granite.binding.android.BeanSetter;
import org.granite.binding.android.Binder.BeanResolver;
import org.granite.client.tide.Context;
import org.granite.client.tide.data.impl.JavaBeanDataManager;
import org.granite.util.TypeUtil;


public class TideBeanResolver implements BeanResolver {
	
	private final Context context;
	private final JavaBeanDataManager dataManager;
	
	private final BeanSetter<Object> beanSetter = new DataManagerBeanSetter();
	
	public TideBeanResolver(Context context) {
		this.context = context;
		this.dataManager = (JavaBeanDataManager)context.getEntityManager().getDataManager();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T resolveBean(Object ref) {
		if (ref instanceof String) {
			String[] path = ((String)ref).split("\\.");
			Object bean = context.byName(path[0]);
			if (path.length > 0) {
				for (int i = 1; i < path.length; i++)
					bean = TypeUtil.getProperty(bean, path[i]);
			}
			return (T)bean;
		}
		else if (ref instanceof Class<?>)
			return context.byType((Class<T>)ref);
		return (T)ref;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> BeanSetter<T> getBeanSetter(T bean, String propertyName) {
		return (BeanSetter<T>)beanSetter;
	}
	
	
	private class DataManagerBeanSetter implements BeanSetter<Object> {		
		@Override
		public void setValue(Object instance, String propertyName, Object value) throws Exception {
			dataManager.setPropertyValue(instance, propertyName, value);			
		}		
	}
}
