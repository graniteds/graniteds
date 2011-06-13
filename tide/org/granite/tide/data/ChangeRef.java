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
import java.io.Serializable;


/**
 * @author William DRAI
 */
public class ChangeRef implements Externalizable {
    
    private String className;
    private String uid;
    private Serializable id;
	

	public String getClassName() {
		return className;
	}

	public String getUid() {
		return uid;
	}

	public Serializable getId() {
		return id;
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(className);
		out.writeObject(uid);
		out.writeObject(id);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		className = (String)in.readObject();
		uid = (String)in.readObject();
		id = (Serializable)in.readObject();
	}
}
