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
import java.io.ObjectInput;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.messaging.reflect.Property;
import org.granite.messaging.reflect.Reflection;

/**
 * The <tt>ExtendedObjectInput</tt> interface extends <tt>ObjectInput</tt> and add two methods that
 * help dealing with reflection ({@link #getReflection()}) and field assignments
 * ({@link #readAndSetProperty(Object, Property)}).
 * 
 * <p>
 * Implementation instances of this interface are passed as parameter to
 * {@link ExtendedObjectCodec#decode(ExtendedObjectInput, Object)} method calls.
 * </p>
 * 
 * <p>
 * Several methods of the <tt>ObjectInput</tt> interface are tagged as deprecated and should
 * never be used within <tt>ExtendedObjectCodec.decode(...)</tt> calls (implementations must throw
 * <tt>UnsupportedOperationException</tt> errors).
 * </p>
 * 
 * @author Franck WOLFF
 * 
 * @see ExtendedObjectCodec
 * @see ExtendedObjectOutput
 * @see JMFDeserializer
 * @see InputContext
 */
public interface ExtendedObjectInput extends ObjectInput {

	/**
	 * Return the {@link Reflection} registered in the global JMF {@link SharedContext}.
	 * 
	 * @return A <tt>Reflection</tt> utility that can be used to load classes during the
	 * 		deserialization process.
	 */
	Reflection getReflection();
	
	String getAlias(String className);

	/**
	 * Read the next data in the current input stream and set the given <tt>field</tt> of
	 * the given Object <tt>obj</tt> with this data. This method is only a convenient shortcut
	 * for reading data from the input stream and setting the value of a {@link Field} to this
	 * data without knowing its type.
	 * 
	 * For example, given a field <tt>f1</tt> of type <tt>boolean</tt> (the primitive type) and 
	 * a field <tt>f2</tt> of type <tt>int</tt> (the primitive type): 
	 * 
	 * <pre>
	 * in.readAndSetProperty(obj, f1);
	 * in.readAndSetProperty(obj, f2);
	 * </pre>
	 * is equivalent to:
	 * <pre>
	 * f1.setBoolean(obj, in.readBoolean());
	 * f2.setInt(obj, in.readInt());
	 * </pre>
	 * 
	 * @param obj The instance of the class declaring the property.
	 * @param property The property to set with the read value.
	 * 
	 * @throws IOException If any I/O error occur.
	 * @throws JMFEncodingException If read data isn't of expected type (ie. the type of the given property).
	 * @throws ClassNotFoundException If the class of a serialized object cannot be found.
	 * @throws IllegalAccessException If the property cannot be accessed.
	 */
	void readAndSetProperty(Object obj, Property property) throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException;

	/**
	 * Should never be used with JMF {@link ExtendedObjectCodec}.
	 * Use {@link #readByte()} instead.
	 * 
	 * @deprecated Implementation must throw a {@link UnsupportedOperationException} error.
	 */
	@Deprecated
	public int read() throws IOException;

	/**
	 * Should never be used with JMF {@link ExtendedObjectCodec}.
	 * Use <tt>(byte[])</tt>{@link #readObject()} instead.
	 * 
	 * @deprecated Implementation must throw a {@link UnsupportedOperationException} error.
	 */
	@Deprecated
	public int read(byte[] b) throws IOException;

	/**
	 * Should never be used with JMF {@link ExtendedObjectCodec}.
	 * Use <tt>(byte[])</tt>{@link #readObject()} instead.
	 * 
	 * @deprecated Implementation must throw a {@link UnsupportedOperationException} error.
	 */
	@Deprecated
	public int read(byte[] b, int off, int len) throws IOException;

	/**
	 * Should never be used with JMF {@link ExtendedObjectCodec}.
	 * Use <tt>(byte[])</tt>{@link #readObject()} instead.
	 * 
	 * @deprecated Implementation must throw a {@link UnsupportedOperationException} error.
	 */
	@Deprecated
	public void readFully(byte[] b) throws IOException;

	/**
	 * Should never be used with JMF {@link ExtendedObjectCodec}.
	 * Use <tt>(byte[])</tt>{@link #readObject()} instead.
	 * 
	 * @deprecated Implementation must throw a {@link UnsupportedOperationException} error.
	 */
	@Deprecated
	public void readFully(byte[] b, int off, int len) throws IOException;
	
	/**
	 * Should never be used with JMF {@link ExtendedObjectCodec}.
	 * Use {@link #readUTF()} instead and parse the String.
	 * 
	 * @deprecated Implementation must throw a {@link UnsupportedOperationException} error.
	 */
	@Deprecated
	public String readLine() throws IOException;

	/**
	 * Should never be used with JMF {@link ExtendedObjectCodec}.
	 * 
	 * @deprecated Implementation must throw a {@link UnsupportedOperationException} error.
	 */
	@Deprecated
	public int skipBytes(int n) throws IOException;

	/**
	 * Should never be used with JMF {@link ExtendedObjectCodec}.
	 * 
	 * @deprecated Implementation must throw a {@link UnsupportedOperationException} error.
	 */
	@Deprecated
	public long skip(long n) throws IOException;
}
