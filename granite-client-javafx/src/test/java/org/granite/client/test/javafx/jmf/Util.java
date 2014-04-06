/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.test.javafx.jmf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Level;

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.DefaultSharedContext;
import org.granite.messaging.jmf.JMFDeserializer;
import org.granite.messaging.jmf.JMFDumper;
import org.granite.messaging.jmf.JMFSerializer;
import org.granite.messaging.jmf.SharedContext;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.messaging.jmf.codec.std.impl.util.IntegerUtil;
import org.granite.messaging.jmf.codec.std.impl.util.LongUtil;

public class Util {
	
	public static byte[] bytes(int... values) {
		byte[] bytes = new byte[values.length];
		for (int i = 0; i < values.length; i++)
			bytes[i] = (byte)values[i];
		return bytes;
	}
	
	public static String toHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < bytes.length; i++) {
			if (i > 0)
				sb.append(", ");
			sb.append(String.format("0x%02X", bytes[i] & 0xFF));
		}
		sb.append("]");
		return sb.toString();
	}
	
	public static class ByteArrayJMFSerializer extends JMFSerializer {
		
		public ByteArrayJMFSerializer() {
			this(new DefaultCodecRegistry());
		}
		
		public ByteArrayJMFSerializer(CodecRegistry codecRegistry) {
			this(new DefaultSharedContext(codecRegistry));
		}
		
		public ByteArrayJMFSerializer(List<ExtendedObjectCodec> extendedCodecs) {
			this(new DefaultSharedContext(new DefaultCodecRegistry(extendedCodecs)));
		}
		
		public ByteArrayJMFSerializer(SharedContext sharedContext) {
			super(new ByteArrayOutputStream(), sharedContext);
		}
		
		public byte[] toByteArray() {
			return ((ByteArrayOutputStream)outputStream).toByteArray();
		}
		
		public void writeVariableInt(int v) throws IOException {
			IntegerUtil.encodeVariableInteger(this, v);
		}
		
		public void writeVariableLong(long v) throws IOException {
			LongUtil.encodeVariableLong(this, v);
		}
	}
	
	public static class ByteArrayJMFDeserializer extends JMFDeserializer {
		
		public ByteArrayJMFDeserializer(byte[] bytes) {
			this(bytes, new DefaultCodecRegistry());
		}
		
		public ByteArrayJMFDeserializer(byte[] bytes, CodecRegistry codecRegistry) {
			this(bytes, new DefaultSharedContext(codecRegistry));
		}
		
		public ByteArrayJMFDeserializer(byte[] bytes, List<ExtendedObjectCodec> extendedCodecs) {
			this(bytes, new DefaultSharedContext(new DefaultCodecRegistry(extendedCodecs)));
		}
		
		public ByteArrayJMFDeserializer(byte[] bytes, SharedContext sharedContext) {
			super(new ByteArrayInputStream(bytes), sharedContext);
		}

		public int readVariableInt() throws IOException {
			return IntegerUtil.decodeVariableInteger(this);
		}

		public long readVariableLong() throws IOException {
			return LongUtil.decodeVariableLong(this);
		}
	}
	
	public static class ByteArrayJMFDumper extends JMFDumper {
		
		public ByteArrayJMFDumper(byte[] bytes, PrintStream ps) {
			this(bytes, new DefaultCodecRegistry(), ps);
		}
		
		public ByteArrayJMFDumper(byte[] bytes, CodecRegistry codecRegistry, PrintStream ps) {
			this(bytes, new DefaultSharedContext(codecRegistry), ps);
		}
		
		public ByteArrayJMFDumper(byte[] bytes, SharedContext sharedContext, PrintStream ps) {
			super(new ByteArrayInputStream(bytes), sharedContext, ps);
		}
	}
	
	public static PrintStream newNullPrintStream() {
		return new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
			}
		});
	}
	
	public static void setupConsoleLogger(Level level) {
		
//		Logger log = LogManager.getLogManager().getLogger(JMFSerializer.class.getName());
//		if (log == null) {
//			new JMFSerializer(null);
//			
//			log = LogManager.getLogManager().getLogger(JMFSerializer.class.getName());
//			if (log == null)
//				throw new NullPointerException("JMFSerializer logger is null");
//		
//			log.setLevel(level);
//			Handler consoleHandler = new ConsoleHandler();
//			consoleHandler.setLevel(Level.FINE);
//			consoleHandler.setFormatter(new Formatter() {
//				@Override
//				public String format(LogRecord record) {
//					return "[DEBUG] JMFSerializer." + record.getMessage();
//				}
//			});
//			log.addHandler(consoleHandler);
//		}
//		else
//			log.setLevel(level);
	}
	
	public static byte[] serializeJava(Object o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();
		return baos.toByteArray();
	}
}
