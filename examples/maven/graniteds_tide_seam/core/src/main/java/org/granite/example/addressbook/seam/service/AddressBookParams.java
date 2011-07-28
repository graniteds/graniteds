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

package org.granite.example.addressbook.seam.service;

import org.granite.example.addressbook.entity.AbstractEntity;
import org.granite.tide.data.DataObserveParams;
import org.granite.tide.data.DataPublishParams;
import org.granite.tide.data.DataTopicParams;
import org.jboss.seam.security.Identity;



public class AddressBookParams implements DataTopicParams {
	
	public void observes(DataObserveParams params) {
		params.addValue("user", Identity.instance().getCredentials().getUsername());
		params.addValue("user", "__public__");
	}
	
	public void publishes(DataPublishParams params, Object entity) {
		if (((AbstractEntity)entity).isRestricted())
			params.setValue("user", ((AbstractEntity)entity).getCreatedBy());
		else
			params.setValue("user", "__public__");
	}
}