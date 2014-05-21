/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.messaging.jmf.codec.std.impl;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.granite.messaging.jmf.DumpContext;
import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.std.LocalDateTimeCodec;
import org.granite.messaging.jmf.codec.std.impl.util.LongUtil;

/**
 * @author Franck WOLFF
 */
public class LocalDateTimeCodecImpl extends AbstractStandardCodec<LocalDateTime> implements LocalDateTimeCodec {
	
	public int getObjectType() {
		return JMF_LOCALDATETIME;
	}

	public Class<?> getObjectClass() {
		return LocalDateTime.class;
	}
	
	public void encode(OutputContext ctx, LocalDateTime v) throws IOException {
		ctx.getOutputStream().write(JMF_LOCALDATETIME);
		try {
			Instant instant = v.toInstant(ZoneOffset.UTC);
			LongUtil.encodeLong(ctx, instant.toEpochMilli());
		}
		catch (Exception e) {
			throw new IOException("Could not get instant for LocalDate " + v, e);
		}
	}
	
	public LocalDateTime decode(InputContext ctx, int parameterizedJmfType) throws IOException {
		long epochMilli = LongUtil.decodeLong(ctx);
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneOffset.UTC);
	}
	
	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		switch (jmfType) {
		case JMF_LOCALDATETIME:
			ctx.indentPrintLn(LocalDate.class.getName() + ": " + decode(ctx, parameterizedJmfType));
			break;
		default:
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		}
	}
}
