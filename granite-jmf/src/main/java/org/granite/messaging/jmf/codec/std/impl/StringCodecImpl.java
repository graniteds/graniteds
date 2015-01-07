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
package org.granite.messaging.jmf.codec.std.impl;

import java.io.IOException;
import java.io.OutputStream;

import org.granite.messaging.jmf.DumpContext;
import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.std.StringCodec;
import org.granite.messaging.jmf.codec.std.impl.util.IntegerUtil;

/**
 * @author Franck WOLFF
 */
public class StringCodecImpl extends AbstractStandardCodec<String> implements StringCodec {
	
	protected static final int INDEX_OR_LENGTH_BYTE_COUNT_OFFSET = 4;

	protected static final int UUID_FLAG = 0x40;
	protected static final int UUID_LOWERCASE_FLAG = 0x00;
	protected static final int UUID_UPPERCASE_FLAG = 0x10;
	
	protected static final char[] LOWER_HEX = "0123456789abcdef".toCharArray();
	protected static final char[] UPPER_HEX = "0123456789ABCDEF".toCharArray();
	
	protected static final int[] HEX_INDICES = new int[256];
	static {
		for (int i = 0; i < HEX_INDICES.length; i++) {
			if (i >= '0' && i <= '9')
				HEX_INDICES[i] = i - '0';
			else if (i >= 'a' && i <= 'f')
				HEX_INDICES[i] = i - 'a' + 10;
			else if (i >= 'A' && i <= 'F')
				HEX_INDICES[i] = i - 'A' + 10;
			else
				HEX_INDICES[i] = -1;
		}
	}
	
	public int getObjectType() {
		return JMF_STRING;
	}

	public Class<?> getObjectClass() {
		return String.class;
	}

	public void encode(OutputContext ctx, String v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		if (v.length() == 0) {
			os.write(JMF_STRING);
			os.write(0x00);
			return;
		}
		
		int indexOfStoredString = ctx.indexOfString(v);
		if (indexOfStoredString >= 0) {
			int count = IntegerUtil.significantIntegerBytesCount0(indexOfStoredString);
			os.write(0x80 | (count << INDEX_OR_LENGTH_BYTE_COUNT_OFFSET) | JMF_STRING);
			IntegerUtil.encodeInteger(ctx, indexOfStoredString, count);
		}
		else {
			ctx.addToStrings(v);
			
			int uuidCaseFlag = isUUID(v);
			if (uuidCaseFlag != -1)
				encodeUUID(ctx, v, uuidCaseFlag);
			else {
//				char[] cs = v.toCharArray();
//				byte[] bytes = new byte[cs.length * 3];
//				
//				int length = 0;
//				for (char c : cs) {
//					if (c <= 0x7F)
//						bytes[length++] = (byte)c;
//					else if (c <= 0x7FF) {
//						bytes[length++] = (byte)(0xC0 | ((c >> 6) & 0x1F));
//						bytes[length++] = (byte)(0x80 | ((c >> 0) & 0x3F));
//					}
//					else {
//						bytes[length++] = (byte)(0xE0 | ((c >> 12) & 0x0F));
//						bytes[length++] = (byte)(0x80 | ((c >> 6) & 0x3F));
//						bytes[length++] = (byte)(0xE0 | ((c >> 12) & 0x0F));
//					}
//				}
				
				byte[] bytes = v.getBytes(UTF8);
				int length = bytes.length;
				int count = IntegerUtil.significantIntegerBytesCount0(length);
				
				os.write((count << INDEX_OR_LENGTH_BYTE_COUNT_OFFSET) | JMF_STRING);
				IntegerUtil.encodeInteger(ctx, length, count);
				os.write(bytes, 0, length);
			}
		}
	}
	
	public String decode(InputContext ctx, int parameterizedJmfType) throws IOException {
		if ((parameterizedJmfType & 0x80) != 0) {
			int index = IntegerUtil.decodeInteger(ctx, (parameterizedJmfType >>> INDEX_OR_LENGTH_BYTE_COUNT_OFFSET) & 0x03);
			return ctx.getString(index);
		}
		
		if ((parameterizedJmfType & UUID_FLAG) != 0) {
			String uid = decodeUUID(ctx, parameterizedJmfType);
			ctx.addToStrings(uid);
			return uid;
		}
		
		int length = IntegerUtil.decodeInteger(ctx, (parameterizedJmfType >>> INDEX_OR_LENGTH_BYTE_COUNT_OFFSET) & 0x03);		
		if (length == 0)
			return "";
		
		byte[] bytes = new byte[length];
		ctx.safeReadFully(bytes);
		String s = new String(bytes, UTF8);
		ctx.addToStrings(s);
		
		return s;
	}
	
	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		ctx.indentPrintLn(String.class.getName() + ": \"" + escape(decode(ctx, parameterizedJmfType)) + "\"");
	}
	
	protected int isUUID(String v) {
		if (v.length() != 36 || v.charAt(8) != '-')
			return -1;
		
		int flag = -1;
		
		for (int i = 0; i < 36; i++) {
			char c = v.charAt(i);
			
			switch (i) {
			case 8: case 13: case 18: case 23:
				if (c != '-')
					return -1;
				break;
			default:
				if (!(c >= '0' && c <= '9')) {
					if (c >= 'a' && c <= 'f') {
						if (flag == -1)
							flag = UUID_LOWERCASE_FLAG;
						else if (flag != UUID_LOWERCASE_FLAG)
							return -1;
					}
					else if (c >= 'A' && c <= 'F') {
						if (flag == -1)
							flag = UUID_UPPERCASE_FLAG;
						else if (flag != UUID_UPPERCASE_FLAG)
							return -1;
					}
					else
						return -1;
				}
				break;
			}
		}
		
		// No letters...
		if (flag == -1)
			flag = UUID_LOWERCASE_FLAG;
		
		return flag;
	}
	
	protected void encodeUUID(OutputContext ctx, String v, int caseFlag) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		os.write(caseFlag | UUID_FLAG | JMF_STRING);
		
		byte[] bytes = new byte[16];

		int i = 0, j = 0;
		while (i < 36) {
			char c1 = v.charAt(i++);
			if (c1 == '-')
				c1 = v.charAt(i++);
			char c2 = v.charAt(i++);
			
			bytes[j++] = (byte)(HEX_INDICES[c1] << 4 | HEX_INDICES[c2]);
		}

		os.write(bytes);
	}
	
	protected String decodeUUID(InputContext ctx, int parameterizedJmfType) throws IOException {
		final char[] hex = (parameterizedJmfType & UUID_UPPERCASE_FLAG) != 0 ? UPPER_HEX : LOWER_HEX;
		
		byte[] bytes = new byte[16];
		ctx.safeReadFully(bytes);
		
		char[] chars = new char[36];
		int i = 0;
		for (byte b : bytes) {
			if (i == 8 || i == 13 || i == 18 || i == 23)
				chars[i++] = '-';
			chars[i++] = hex[(b & 0xF0) >>> 4];
			chars[i++] = hex[b & 0x0F];
		}
		
		return new String(chars);
	}
}
