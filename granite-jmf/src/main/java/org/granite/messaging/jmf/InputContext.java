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

import org.granite.messaging.reflect.ClassDescriptor;

/**
 * @author Franck WOLFF
 */
public interface InputContext extends ExtendedObjectInput, JMFConstants {
	
	SharedContext getSharedContext();
	
	InputStream getInputStream();
	
	int safeRead() throws IOException;
	long safeReadLong() throws IOException;
	void safeReadFully(byte[] b) throws IOException;
	void safeReadFully(byte[] b, int off, int len) throws IOException;
	void safeSkip(long n) throws IOException;
	
	int addToClassNames(String s);
	String getClassName(int index);
	
	ClassDescriptor getClassDescriptor(Class<?> cls);
	ClassDescriptor getClassDescriptor(String className) throws ClassNotFoundException;
	
	int addToStrings(String s);
	String getString(int index);
	
	int addToObjects(Object o);
	Object getObject(int index);

	int addToUnresolvedObjects(String className);
	Object setUnresolvedObject(int index, Object o);
}
