/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.datanucleus.serialization;

import java.util.Properties;

import org.granite.test.externalizers.AbstractTestJDOExternalizer;


public class TestJDODataNucleusExternalizer extends AbstractTestJDOExternalizer {
	
	@Override
	protected String setProperties(Properties props) {
		props.put("datanucleus.ConnectionDriverName", "org.h2.Driver");
		props.put("datanucleus.ConnectionURL", "jdbc:h2:mem:testdb");
		props.put("datanucleus.ConnectionUserName", "sa");
		props.put("datanucleus.ConnectionPassword", "");
		props.put("datanucleus.autoCreateTables", "true");
		props.put("datanucleus.autoCreateColumns", "true");
		props.put("datanucleus.storeManagerType", "rdbms");
		props.put("datanucleus.DetachAllOnCommit", "true");
		return "datanucleus-jdo";
	}
	
//	@Test
//	@Ignore("DataNucleus does not work with this case, embedded objects detachedState is not serialized")
//	@Override
//	public void testSerializationLazyEmbeddedGDS838() throws Exception {		
//	}
}
