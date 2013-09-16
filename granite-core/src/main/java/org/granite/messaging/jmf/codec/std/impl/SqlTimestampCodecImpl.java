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

package org.granite.messaging.jmf.codec.std.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;

import org.granite.messaging.jmf.DumpContext;
import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.std.SqlTimestampCodec;

/**
 * @author Franck WOLFF
 */
public class SqlTimestampCodecImpl extends AbstractStandardCodec<Timestamp> implements SqlTimestampCodec {

	public int getObjectType() {
		return JMF_SQL_TIMESTAMP;
	}

	public Class<?> getObjectClass() {
		return Timestamp.class;
	}

	public void encode(OutputContext ctx, Timestamp v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		os.write(JMF_SQL_TIMESTAMP);
		
		long t = v.getTime();
		
		os.write((int)(t >> 56));
		os.write((int)(t >> 48));
		os.write((int)(t >> 40));
		os.write((int)(t >> 32));
		os.write((int)(t >> 24));
		os.write((int)(t >> 16));
		os.write((int)(t >> 8));
		os.write((int)t);
	}

	public Timestamp decode(InputContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_SQL_TIMESTAMP)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		long t = ((long)ctx.safeRead()) << 56;
		t |= ((long)ctx.safeRead()) << 48;
		t |= ((long)ctx.safeRead()) << 40;
		t |= ((long)ctx.safeRead()) << 32;
		t |= ((long)ctx.safeRead()) << 24;
		t |= ((long)ctx.safeRead()) << 16;
		t |= ((long)ctx.safeRead()) << 8;
		t |= ctx.safeRead();
		return new Timestamp(t);
	}
	
	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		switch (jmfType) {
		case JMF_SQL_TIMESTAMP:
			ctx.indentPrintLn(Timestamp.class.getName() + ": " + decode(ctx, parameterizedJmfType));
			break;
		default:
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		}
	}
}
