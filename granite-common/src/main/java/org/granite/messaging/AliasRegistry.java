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
package org.granite.messaging;

import java.util.Set;

/**
 * SPI for registry of class name aliases used during AMF/JMF serialization.
 * An alias registry should store correspondences between server and client class names
 * The alias registry will scan specified packages to find classes annotated with @RemoteAlias
 *
 * @author William DRAI
 */
public interface AliasRegistry {

    /**
     * Scan the specified packages for aliases classes
     * This is called when the channel factory starts
     * @param packageNames
     */
	public void scan(Set<String> packageNames);

    /**
     * Return the client class name for a specified server class alias
     * @param alias server class name
     * @return corresponding client class name
     */
    public String getTypeForAlias(String alias);

    /**
     * Return the aliased server class name for a specified client class name
     * @param className client class name
     * @return corresponding server class name
     */
    public String getAliasForType(String className);
}
