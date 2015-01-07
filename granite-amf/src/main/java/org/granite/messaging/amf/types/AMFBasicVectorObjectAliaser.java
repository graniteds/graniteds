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
package org.granite.messaging.amf.types;

import java.util.Calendar;
import java.util.Date;

import org.w3c.dom.Document;

/**
 * @author Franck WOLFF
 */
public class AMFBasicVectorObjectAliaser implements AMFVectorObjectAliaser {

	public String aliasFor(Class<?> cls) {
		if (cls == String.class || cls == Character.class)
			return "String";
		if (Date.class.isAssignableFrom(cls) || Calendar.class.isAssignableFrom(cls))
			return "Date";
		if (Document.class.isAssignableFrom(cls))
			return "XML";
		if (cls == Object.class)
			return "Object";
		return cls.getName();
	}
}
