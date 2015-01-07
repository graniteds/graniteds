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
package org.granite.messaging.amf.io;

/**
 * Implementations of this interface are used at deserialization time in
 * order to control arbitrary class instantiation that can result in potential
 * security exploits.
 * 
 * @author Franck WOLFF
 */
public interface AMF3DeserializerSecurizer {

	/**
	 * Check if it safe to instantiate the class denoted by the <code>className</code>
	 * parameter.
	 * 
	 * @param className the class name to check.
	 * @return <code>true</code> if it is safe to instantiate the given class,
	 * 		<code>false</code> otherwise.
	 */
	public boolean allowInstantiation(String className);
	
	/**
	 * An arbitrary string that may be used in order to configure this securizer.
	 * 
	 * @param param a string used in configuring this securizer.
	 */
	public void setParam(String param);
	
	/**
	 * Returns the string that is currently used for this securizer configuration.
	 * 
	 * @return the string that is currently used for this securizer configuration.
	 */
	public String getParam();
}
