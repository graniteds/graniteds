/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.hibernate4;

import java.io.Serializable;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.StaleStateException;

/**
 * @author William DRAI
 */
public class HibernateOptimisticLockException extends StaleStateException {

	private static final long serialVersionUID = 1L;

	private Object entity;
	
	public HibernateOptimisticLockException(String message, Throwable cause, Object entity) {
		super(message);
		this.entity = entity;
	}
	
	public Object getEntity() {
		return entity;
	}
	
	public static void rethrowOptimisticLockException(Session session, StaleObjectStateException sose) {
		Serializable identifier = sose.getIdentifier();
		if (identifier != null) {
			try {
				Object entity = session.load(sose.getEntityName(), identifier);
				if (entity instanceof Serializable) {
					//avoid some user errors regarding boundary crossing
					throw new HibernateOptimisticLockException(null, sose, entity);
				}
			}
			catch (ObjectNotFoundException onfe) {
				// Ignored, StaleStateException will be rethrown
			}
		}
	}
}
