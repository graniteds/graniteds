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
package org.granite.messaging.amf.io.util.externalizer;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.granite.messaging.amf.io.util.Property;
import org.granite.util.XMap;

/**
 * @author Franck WOLFF
 */
public interface Externalizer {

	public void configure(XMap properties);
	
    public Object newInstance(String type, ObjectInput in)
        throws IOException, ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException;

    public void readExternal(Object o, ObjectInput in) throws IOException, ClassNotFoundException, IllegalAccessException;

    public void writeExternal(Object o, ObjectOutput out) throws IOException, IllegalAccessException;

    public List<Property> findOrderedFields(Class<?> clazz);

    public int accept(Class<?> clazz);
}
