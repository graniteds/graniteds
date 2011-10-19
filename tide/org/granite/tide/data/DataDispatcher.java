/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.tide.data;


/**
 *  Interface for data update dispatchers.
 *  The dispatch is a three step process :
 * 
 * 	<ul>
 *  <li>Initialization in the constructor (specific for each implementation), at initialization of <code>DataContext</code></li>
 *  <li><code>observe()</code> builds the server selector depending on the data that are processed</li>
 *  <li><code>publish()</code> handles the actual publishing</li>
 *  </ul>
 * 
 *  @see DataContext
 * 
 *  @author William Drai
 */
public interface DataDispatcher {
	
	public static final String TIDE_DATA_SUBTOPIC = "tideDataTopic";
	public static final String GDS_SESSION_ID = "GDSSessionID";
	public static final String TIDE_DATA_TYPE_KEY = "type";
	public static final String TIDE_DATA_TYPE_VALUE = "DATA";
	public static final String SERVER_DISPATCHER_GDS_SESSION_ID = "__GDS_SERVER_DISPATCHER__";

	public void observe();
	
	public void publish(Object[][] dataUpdates);
}
