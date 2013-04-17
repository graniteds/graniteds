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

package org.granite.messaging.jmf;

import java.io.IOException;
import java.io.ObjectInput;
import java.lang.reflect.Field;

/**
 * @author Franck WOLFF
 */
public interface ExtendedObjectInput extends ObjectInput {

	void readField(Object obj, Field field) throws IOException, ClassNotFoundException, IllegalAccessException;
	
	@Deprecated
	public int read() throws IOException;

	@Deprecated
	public int read(byte[] b) throws IOException;

	@Deprecated
	public int read(byte[] b, int off, int len) throws IOException;

	@Deprecated
	public void readFully(byte[] b) throws IOException;

	@Deprecated
	public void readFully(byte[] b, int off, int len) throws IOException;
	
	@Deprecated
	public String readLine() throws IOException;

	@Deprecated
	public int skipBytes(int n) throws IOException;

	@Deprecated
	public long skip(long n) throws IOException;
}
