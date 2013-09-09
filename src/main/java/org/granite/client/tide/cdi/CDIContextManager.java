/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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

package org.granite.client.tide.cdi;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.granite.client.tide.Context;
import org.granite.client.tide.EventBus;
import org.granite.client.tide.Application;
import org.granite.client.tide.data.Conflicts;
import org.granite.client.tide.data.DataConflictListener;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.EntityManager.UpdateKind;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.impl.SimpleContextManager;

/**
 * @author William DRAI
 */
@ApplicationScoped
public class CDIContextManager extends SimpleContextManager {
	
	@Inject
	private BeanManager beanManager;
	
	protected CDIContextManager() {
		super();
		// CDI proxying...
	}
	
	@Inject
	public CDIContextManager(Application application, EventBus eventBus) {
		super(application, eventBus);
	}
	
	@PostConstruct
	public void init() {
		setInstanceStoreFactory(new CDIInstanceStoreFactory(beanManager));
		getContext().getEntityManager().addListener(new CDIDataConflictListener());
	}
	
	@Produces
	public Context getContext() {
		return getContext(null);
	}
	
	@Produces
	public EntityManager getEntityManager() {
		return getContext(null).getEntityManager();
	}
	
	@Produces
	public DataManager getDataManager() {
		return getContext(null).getDataManager();
	}
	
	private final class CDIDataConflictListener implements DataConflictListener {
		@Override
		public void onConflict(EntityManager entityManager, Conflicts conflicts) {
			TideApplicationEvent event = new TideApplicationEvent(getContext(null), UpdateKind.CONFLICT.eventName(), conflicts);
			beanManager.fireEvent(event);
		}
	}
}
