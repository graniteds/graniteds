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

package org.granite.clustering;

import java.io.Serializable;

/**
 * This class holds a <tt>transient</tt> reference to the object given to its
 * constructor. When this class is serialized, the reference to the object is
 * lost.
 * 
 * @author Franck WOLFF
 * 
 * @see org.granite.messaging.webapp.HttpGraniteContext
 * @see TransientReference
 */
public final class TransientReferenceHolder implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final transient Object object;
	
	public TransientReferenceHolder(Object object) {
		this.object = object;
	}

	public Object get() {
		return object;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof TransientReferenceHolder))
			return false;
		Object reference = ((TransientReferenceHolder)obj).get();
		if (reference == object)
			return true;
		if (object == null)
			return false; 
		return object.equals(reference);
	}

	@Override
	public int hashCode() {
		if (object != null)
			return object.hashCode();
		return 0;
	}

	@Override
	public String toString() {
		return getClass().getName() + ": " + object;
	}
}
