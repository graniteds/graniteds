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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author Franck WOLFF
 */
public class PersistentCollectionSnapshot implements Externalizable {
	
	protected Object[] elements = null;
	protected boolean dirty = false;
	
	public PersistentCollectionSnapshot() {
	}

	public PersistentCollectionSnapshot(Object[] elements, boolean dirty) {
		this.elements = elements;
		this.dirty = dirty;
	}
	
	public boolean isInitialized() {
		return elements != null;
	}

	public Object[] getElements() {
		return elements;
	}

	public boolean isDirty() {
		return dirty;
	}
	
	@SuppressWarnings("unused")
	protected void writeExtendedInitializationData(ObjectOutput out) throws IOException {
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {
		if (elements == null)
			out.writeBoolean(false);
		else {
			out.writeBoolean(true);
			writeExtendedInitializationData(out);
			out.writeBoolean(dirty);
			out.writeObject(elements);
		}
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		if (readInitializationData(in).isInitialized())
			readCoreData(in);
	}
	
	@SuppressWarnings("unused")
	public InitializationData readInitializationData(ObjectInput in) throws IOException, ClassNotFoundException {
		return new InitializationData(in.readBoolean());
	}
	
	public CoreData readCoreData(ObjectInput in) throws IOException, ClassNotFoundException {
		this.dirty = in.readBoolean();
		this.elements = (Object[])in.readObject();
		
		return new CoreData(dirty, elements);
	}
	
	public static class InitializationData {
		
		private final boolean initialized;
		
		public InitializationData(boolean initialized) {
			this.initialized = initialized;
		}
		
		public InitializationData(InitializationData initializationData) {
			this.initialized = initializationData.initialized;
		}

		public boolean isInitialized() {
			return initialized;
		}
	}
	
	public static class CoreData {
		
		private final boolean dirty;
		private final Object[] elements;
		
		public CoreData(boolean dirty, Object[] elements) {
			this.dirty = dirty;
			this.elements = elements;
		}

		public boolean isDirty() {
			return dirty;
		}

		public Object[] getElements() {
			return elements;
		}
	}
}