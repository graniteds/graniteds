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
package org.granite.test.generator.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.messaging.service.annotations.RemoteDestination;
import org.granite.test.generator.entities.Entity1;


@RemoteDestination
public interface MapService extends FilterableJpaRepository<Entity1, Long> {

	public Map<String, Object> findMap(String name);
	
	public HashMap<String, Object> findHashMap(String name); 
	
	public List<HashMap<String, Object>> findListHashMap(String name);
	
	public List<Map<String, Entity1>> findListMapEntity(String name);
	
	public List<Map<String, List<Entity1>>> findListMapListEntity(String name);
}
