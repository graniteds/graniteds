/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.gravity.tomcat;

import java.io.IOException;
import java.io.InputStream;

/**
 * An unsynchronized input/output byte buffer that avoids useless byte array copies. 
 * 
 * @author Franck
 */
public class ByteArrayCometIO extends InputStream implements CometIO {

	private static final byte[] BYTES_0 = new byte[0];

	protected final int initialCapacity;
	protected byte buf[] = BYTES_0;
	protected int pos = 0;
	protected int mark = 0;
	protected int count = 0;
	
	public ByteArrayCometIO() {
		this(2048);
	}
	
	public ByteArrayCometIO(int initialCapacity) {
		if (initialCapacity < 1)
			throw new IllegalArgumentException("initialCapacity must be > 1: " + initialCapacity);
		this.initialCapacity = initialCapacity;
	}

	public int readFully(InputStream is) throws IOException {
		try {
			int b = -1;
			
			while ((b = is.read()) != -1) {
				if (count + 1 >= buf.length) {
					if (buf.length > 0) {
						byte[] tmp = new byte[buf.length << 1];
						System.arraycopy(buf, 0, tmp, 0, buf.length);
						buf = tmp;
					}
					else
						buf = new byte[initialCapacity];
				}
				buf[count++] = (byte)b;
			}
			
			return count;
		}
		finally {
			is.close();
		}
	}

	public boolean readAvailable(InputStream is) throws IOException {
		boolean eof = false;
		
		try {
			int available = -1;
			while ((available = is.available()) > 0) {
	
	            if (count > 0) {
	                byte[] newBytes = new byte[available + count + 1];
	                System.arraycopy(buf, 0, newBytes, 0, count);
	                buf = newBytes;
	            }
	            else
	                buf = new byte[available + 1];
	
	            if (is.read(buf, count, available) != available)
	                throw new IOException("Could not read available bytes: " + available);
	            
	            count += available;
	        }
			
			int b = is.read();
			if (b == -1) {
				eof = true;
				return false;
			}
			
			buf[buf.length - 1] = (byte)b;
			count++;
			
			return true;
		}
		finally {
			if (eof)
				is.close();
		}
	}
	
	public InputStream getInputStream() throws IOException {
		return this;
	}

	@Override
	public int read() throws IOException {
		return (pos < count) ? (buf[pos++] & 0xff) : -1;
	}
    
	@Override
	public int read(byte b[], int off, int len) {
    	if (b == null)
    	    throw new NullPointerException();
    	
    	if (off < 0 || len < 0 || len > b.length - off)
    	    throw new IndexOutOfBoundsException();

    	if (pos >= count)
    	    return -1;

    	if (pos + len > count)
    	    len = count - pos;

    	if (len <= 0)
    	    return 0;

    	System.arraycopy(buf, pos, b, off, len);
    	pos += len;
    	return len;
	}

	@Override
    public long skip(long n) {
    	if (pos + n > count)
    	    n = count - pos;

    	if (n < 0)
    	    return 0;

    	pos += n;
    	return n;
    }

	@Override
    public int available() {
    	return count - pos;
    }

	@Override
    public boolean markSupported() {
    	return true;
    }
	
	@Override
	public void mark(int readAheadLimit) {
		mark = pos;
	}

	@Override
    public void reset() {
    	pos = mark;
    }

    @Override
	public void close() throws IOException {
    }
}
