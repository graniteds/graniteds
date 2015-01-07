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
package org.granite.messaging.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <tt>Include</tt> annotation can be used to include a property with no
 * corresponding Java field and which content has to be retrieved from a getter.
 * <p>
 * Typical usage:
 * <pre>
 * {@literal @}Include
 * public String getComputedData() {
 *     return "something that can only be computed on the server";
 * }
 * </pre>
 * The pseudo-field will be serialized just like if there were a field named
 * "calculatedData". The implementation accepts the get/is convention of Java
 * beans. Note that even if the client is actually serializing non empty data
 * when the property is sent back to the server, its content will be ignored
 * on the server side.
 * </p>
 * 
 * @author Franck WOLFF
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Include {

}
