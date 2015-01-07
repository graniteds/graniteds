/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.messaging.jmf;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;

import org.granite.messaging.reflect.ClassDescriptor;
import org.granite.messaging.reflect.Property;

@SuppressWarnings("deprecation")
/**
 * @author Franck WOLFF
 */
public class JMFObjectOutputStream extends ObjectOutputStream {

	private final OutputContext out;
	private final ClassDescriptor desc;
	private final Object v;
	
	public JMFObjectOutputStream(OutputContext out, ClassDescriptor desc, Object v) throws IOException {
		super();

		this.out = out;
		this.desc = desc;
		this.v = v;
	}

	@Override
	protected void writeObjectOverride(Object obj) throws IOException {
		out.writeObject(obj);
	}

	@Override
	public void useProtocolVersion(int version) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeUnshared(Object obj) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void defaultWriteObject() throws IOException {
		for (Property property : desc.getSerializableProperties()) {
			try {
				out.getAndWriteProperty(v, property);
			}
			catch (IllegalAccessException e) {
				throw new IOException(e);
			}
			catch (InvocationTargetException e) {
				throw new IOException(e);
			}
		}
	}

	@Override
	public PutField putFields() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeFields() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void reset() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(int val) throws IOException {
		out.write(val);
	}

	@Override
	public void write(byte[] buf) throws IOException {
		out.write(buf);
	}

	@Override
	public void write(byte[] buf, int off, int len) throws IOException {
		out.write(buf, off, len);
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

	@Override
	public void close() throws IOException {
		out.close();
	}

	@Override
	public void writeBoolean(boolean val) throws IOException {
		out.writeBoolean(val);
	}

	@Override
	public void writeByte(int val) throws IOException {
		out.writeByte(val);
	}

	@Override
	public void writeShort(int val) throws IOException {
		out.writeShort(val);
	}

	@Override
	public void writeChar(int val) throws IOException {
		out.writeChar(val);
	}

	@Override
	public void writeInt(int val) throws IOException {
		out.writeInt(val);
	}

	@Override
	public void writeLong(long val) throws IOException {
		out.writeLong(val);
	}

	@Override
	public void writeFloat(float val) throws IOException {
		out.writeFloat(val);
	}

	@Override
	public void writeDouble(double val) throws IOException {
		out.writeDouble(val);
	}

	@Override
	public void writeBytes(String str) throws IOException {
		out.writeBytes(str);
	}

	@Override
	public void writeChars(String str) throws IOException {
		out.writeChars(str);
	}

	@Override
	public void writeUTF(String str) throws IOException {
		out.writeUTF(str);
	}
}
