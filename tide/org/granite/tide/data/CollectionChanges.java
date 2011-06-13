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

package org.granite.tide.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * @author William DRAI
 */
public class CollectionChanges implements Externalizable {
    
    private CollectionChange[] changes;
	

	public CollectionChange[] getChanges() {
		return changes;
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(changes);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		Object[] cs = (Object[])in.readObject();
		changes = new CollectionChange[cs.length];
		for (int i = 0; i < cs.length; i++)
			changes[i] = (CollectionChange)cs[i];
	}
}
