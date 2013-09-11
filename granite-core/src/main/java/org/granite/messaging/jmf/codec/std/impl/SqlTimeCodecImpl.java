/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
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
import java.sql.Time;

import org.granite.messaging.jmf.DumpContext;
import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.std.SqlTimeCodec;

/**
 * @author Franck WOLFF
 */
public class SqlTimeCodecImpl extends AbstractStandardCodec<Time> implements SqlTimeCodec {

	public int getObjectType() {
		return JMF_SQL_TIME;
	}

	public Class<?> getObjectClass() {
		return Time.class;
	}

	public void encode(OutputContext ctx, Time v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		os.write(JMF_SQL_TIME);
		
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

	public Time decode(InputContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_SQL_TIME)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		long t = ((long)ctx.safeRead()) << 56;
		t |= ((long)ctx.safeRead()) << 48;
		t |= ((long)ctx.safeRead()) << 40;
		t |= ((long)ctx.safeRead()) << 32;
		t |= ((long)ctx.safeRead()) << 24;
		t |= ((long)ctx.safeRead()) << 16;
		t |= ((long)ctx.safeRead()) << 8;
		t |= ctx.safeRead();
		return new Time(t);
	}
	
	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		switch (jmfType) {
		case JMF_SQL_TIME:
			ctx.indentPrintLn(Time.class.getName() + ": " + decode(ctx, parameterizedJmfType));
			break;
		default:
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		}
	}
}
