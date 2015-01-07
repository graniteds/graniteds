/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.gravity.udp;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Franck WOLFF
 */
public class UdpOutputStream extends OutputStream {

	private static final int MAX_SIZE = 64 * 1024; // 64K.
	
	private byte[] buffer = new byte[512];
	private int size = 0;
	
	public UdpOutputStream() {
	}

	@Override
	public void write(int b) throws IOException {
		if (size >= buffer.length) {
			if (size >= MAX_SIZE)
				throw new UdpPacketTooLargeException("Max capacity of 64K reached");

			byte[] newBuffer = new byte[buffer.length << 1];
			System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
			buffer = newBuffer;
		}
		buffer[size++] = (byte)b;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (off < 0 || len < 0 || off + len > b.length)
			throw new IndexOutOfBoundsException();
		
		if (len == 0)
			return;
		
		int newSize = len + size;
		if (newSize > MAX_SIZE)
			throw new UdpPacketTooLargeException("Max capacity of 64K reached");
		
		if (newSize > buffer.length) {
			int newCapacity = buffer.length << 1;
			while (newCapacity < newSize)
				newCapacity <<= 1;
			
			byte[] newBuffer = new byte[newCapacity];
			System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
			buffer = newBuffer;
		}

		System.arraycopy(b, off, buffer, size, len);
		size = newSize;
	}
	
	public byte[] buffer() {
		return buffer;
	}
	
	public int size() {
		return size;
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() {
	}
}
