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
import java.io.InputStream;
import java.io.PrintStream;

import org.granite.messaging.jmf.codec.StandardCodec;

/**
 * @author Franck WOLFF
 */
public class JMFDumper extends JMFDeserializer implements DumpContext {
	
	protected static final int DEFAULT_MAX_ARRAY_ELEMENTS = 32;
	
	protected final PrintStream ps;
	protected final int maxArrayElements;

	protected int indentCount = 0;
	
	public JMFDumper(InputStream is, SharedContext context, PrintStream ps) {
		this(is, context, ps, DEFAULT_MAX_ARRAY_ELEMENTS);
	}
	
	public JMFDumper(InputStream is, SharedContext context, PrintStream ps, int maxArrayElements) {
		super(is, context);
		this.ps = ps;
		this.maxArrayElements = maxArrayElements;
	}

	public int getMaxArrayElements() {
		return maxArrayElements;
	}

	public void dump() throws IOException {
		int parameterizedJmfType;
		
		while ((parameterizedJmfType = inputStream.read()) != -1) {
			int jmfType = codecRegistry.extractJmfType(parameterizedJmfType);
			
			StandardCodec<?> codec = codecRegistry.getCodec(jmfType);
			if (codec == null)
				throw new JMFEncodingException("Unsupported type: " + jmfType);
			
			codec.dump(this, parameterizedJmfType);
		}
	}

	public void incrIndent(int off) {
		this.indentCount += off;
		if (indentCount < 0)
			indentCount = 0;
	}

	public void indentPrint(String message) throws IOException {
		for (int i = 0; i < indentCount; i++)
			ps.print("    ");
		ps.print(message);
	}

	public void print(String message) throws IOException {
		ps.print(message);
	}

	public void noIndentPrintLn(String message) throws IOException {
		ps.println(message);
	}

	public void indentPrintLn(String message) throws IOException {
		for (int i = 0; i < indentCount; i++)
			ps.print("    ");
		ps.println(message);
	}
}
