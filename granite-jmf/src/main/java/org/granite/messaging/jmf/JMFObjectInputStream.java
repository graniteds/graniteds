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
import java.io.InvalidObjectException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;
import java.lang.reflect.InvocationTargetException;

import org.granite.messaging.reflect.ClassDescriptor;
import org.granite.messaging.reflect.Property;

@SuppressWarnings("deprecation")
/**
 * @author Franck WOLFF
 */
public class JMFObjectInputStream extends ObjectInputStream {

	private final InputContext in;
	private final ClassDescriptor desc;
	private final Object v;

	public JMFObjectInputStream(InputContext in, ClassDescriptor desc, Object v) throws IOException {
		super();
		
		this.in = in;
		this.desc = desc;
		this.v = v;
	}

	@Override
	protected Object readObjectOverride() throws ClassNotFoundException, IOException {
		return in.readObject();
	}

	@Override
	public void defaultReadObject() throws IOException, ClassNotFoundException {
		for (Property property : desc.getSerializableProperties()) {
			try {
				in.readAndSetProperty(v, property);
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
	public Object readUnshared() throws IOException, ClassNotFoundException {
		throw new UnsupportedOperationException();
	}

	@Override
	public GetField readFields() throws IOException, ClassNotFoundException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void registerValidation(ObjectInputValidation obj, int prio)
			throws NotActiveException, InvalidObjectException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int read() throws IOException {
		return in.read();
	}

	@Override
	public int read(byte[] buf, int off, int len) throws IOException {
		return in.read(buf, off, len);
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}

	@Override
	public void close() throws IOException {
		in.close();
	}

	@Override
	public boolean readBoolean() throws IOException {
		return in.readBoolean();
	}

	@Override
	public byte readByte() throws IOException {
		return in.readByte();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return in.readUnsignedByte();
	}

	@Override
	public char readChar() throws IOException {
		return in.readChar();
	}

	@Override
	public short readShort() throws IOException {
		return in.readShort();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return in.readUnsignedShort();
	}

	@Override
	public int readInt() throws IOException {
		return in.readInt();
	}

	@Override
	public long readLong() throws IOException {
		return in.readLong();
	}

	@Override
	public float readFloat() throws IOException {
		return in.readFloat();
	}

	@Override
	public double readDouble() throws IOException {
		return in.readDouble();
	}

	@Override
	public void readFully(byte[] buf) throws IOException {
		in.readFully(buf);
	}

	@Override
	public void readFully(byte[] buf, int off, int len) throws IOException {
		in.readFully(buf, off, len);
	}

	@Override
	public int skipBytes(int len) throws IOException {
		return in.skipBytes(len);
	}

	@Override
	public String readLine() throws IOException {
		return in.readLine();
	}

	@Override
	public String readUTF() throws IOException {
		return in.readUTF();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return in.read(b);
	}

	@Override
	public long skip(long n) throws IOException {
		return in.skip(n);
	}

	@Override
	public synchronized void mark(int readlimit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean markSupported() {
		return false;
	}
}
