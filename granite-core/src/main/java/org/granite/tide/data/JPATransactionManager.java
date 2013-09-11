/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package org.granite.tide.data;

import javax.persistence.EntityTransaction;


/**
 * Responsible for attaching a entity with the entity mangager
 * @author cingram
 *
 */
public class JPATransactionManager extends JPALocalTransactionManager {
	
	private JTATransactionManager jtaTm = new JTATransactionManager();
	
	
	@Override
	public Object begin(TideTransactionPersistenceManager pm) {
		Object tx = null;
		try {
			tx = super.begin(pm);
		}
		catch (IllegalStateException e) {
			tx = jtaTm.begin(pm);			
		}
		return tx;
	}
	
	@Override
	public void commit(Object tx) throws Exception {
		if (tx instanceof EntityTransaction)
			super.commit(tx);
		else
			jtaTm.commit(tx);
	}
	
	@Override
	public void rollback(Object tx) {
		if (tx instanceof EntityTransaction)
			super.rollback(tx);
		else
			jtaTm.rollback(tx);
	}
}
