/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.android.tide;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import org.granite.binding.android.Binder;
import org.granite.client.tide.Context;
import org.granite.client.tide.Factory;
import org.granite.client.tide.impl.SimpleContextManager;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * @author William DRAI
 */
public class AndroidContextManager extends SimpleContextManager {
	
	private static WeakHashMap<Application, AndroidContextManager> instances = new WeakHashMap<Application, AndroidContextManager>();
	
	public static AndroidContextManager getInstance(Application application) {
		AndroidContextManager contextManager = instances.get(application);
		if (contextManager == null) {
			synchronized (application) {
				contextManager = new AndroidContextManager(application);
				
				String[] moduleClassNames = null;
				Bundle appMetaData = application.getApplicationInfo().metaData;
				if (appMetaData != null) {
					String modules = appMetaData.getString("tide.modules");
					if (modules != null)
						moduleClassNames = modules.split("[\\s,]");
				}
				
				contextManager.registerFactory(Binder.class, new BinderFactory());
				
				if (moduleClassNames != null)
					contextManager.initModules(moduleClassNames);
				else
					contextManager.scanModules(application.getPackageName());
				
				instances.put(application, contextManager);
			}
		}
		return contextManager;
	}
	
	private final static class BinderFactory implements Factory<Binder> {
		@Override
		public Binder create(Context context) {
			Binder binder = new Binder((Activity)context.getPlatformContext());
			binder.setBeanResolver(new TideBeanResolver(context));
			return binder;
		}
		
		@Override
		public boolean isSingleton() {
			return false;
		}
		
		@Override
		public String getName() {
			return null;
		}
		
		@Override
		public Set<Class<?>> getTargetTypes() {
			return Collections.<Class<?>>singleton(Binder.class);
		}
	}
	
	public static Context getContext(Application application) {
		return getInstance(application).getContext();
	}
	
	public static Context getContext(Activity activity) {
		return getInstance(activity.getApplication()).getActivityContext(activity);
	}
	
	public static Context getContext(Activity activity, Bundle bundle) {
		Context context = getInstance(activity.getApplication()).getActivityContext(activity);
		context.defineProperties(new BundleAdapter(bundle));
		return context;
	}
	
	public static void releaseContext(Activity activity) {
		getInstance(activity.getApplication()).destroyActivityContext(activity);
	}
	
	
	public AndroidContextManager(Application application) {
		super(new AndroidApplication(application));
	}
	
	protected Context getActivityContext(Activity activity) {
		Context context = getContext(activity.getComponentName().flattenToString());
		context.setPlatformContext(activity);
		return context;
	}
	
	protected void destroyActivityContext(Activity activity) {
		destroyContext(activity.getComponentName().flattenToString());
	}
	
	
	private static final class BundleAdapter implements Context.Properties {
		
		private final Bundle bundle;
		
		public BundleAdapter(Bundle bundle) {
			this.bundle = bundle;
		}
		
		@Override
		public Object get(String key) {
			return bundle.get(key);
		}

		@Override
		public Set<String> keySet() {
			if (bundle == null)
				return Collections.emptySet();
			return bundle.keySet();
		}
	}
}
