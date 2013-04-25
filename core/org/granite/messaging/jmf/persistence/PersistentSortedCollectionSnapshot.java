/*
  GRANITE DATA SERVICES
  Copyright (C) 2013 GRANITE DATA SERVICES S.A.S.

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

package org.granite.messaging.jmf.persistence;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author Franck WOLFF
 */
public class PersistentSortedCollectionSnapshot extends PersistentCollectionSnapshot {

	private String comparatorClassName = null;
	
	public PersistentSortedCollectionSnapshot() {
	}

	public PersistentSortedCollectionSnapshot(Object[] elements, boolean dirty, String comparatorClassName) {
		super(elements, dirty);
		this.comparatorClassName = comparatorClassName;
	}

	public String getComparatorClassName() {
		return comparatorClassName;
	}

	@Override
	protected void writeExtendedInitializationData(ObjectOutput out) throws IOException {
		out.writeObject(comparatorClassName);
	}

	@Override
	public SortedInitializationData readInitializationData(ObjectInput in) throws IOException, ClassNotFoundException {
		InitializationData initializationData = super.readInitializationData(in);
		
		if (initializationData.isInitialized())
			comparatorClassName = (String)in.readObject();
		
		return new SortedInitializationData(initializationData, comparatorClassName);
	}

	public static class SortedInitializationData extends InitializationData {
		
		private String comparatorClassName = null;

		public SortedInitializationData(InitializationData initializationData, String comparatorClassName) {
			super(initializationData);
			this.comparatorClassName = comparatorClassName;
		}

		public String getComparatorClassName() {
			return comparatorClassName;
		}
	}
}
