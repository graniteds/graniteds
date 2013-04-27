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

package org.granite.tide.hibernate4;

import org.granite.tide.data.DataContext;
import org.granite.tide.data.DataEnabled.PublishMode;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.engine.spi.SessionImplementor;

/**
 */
public class HibernateDataPublishingProcess implements BeforeTransactionCompletionProcess {
	
	private boolean removeContext;
	
	public HibernateDataPublishingProcess(boolean removeContext) {
		this.removeContext = removeContext;
	}
	
	public void doBeforeTransactionCompletion(SessionImplementor session) {
		DataContext.publish(PublishMode.ON_COMMIT);
		if (removeContext)
			DataContext.remove();
	}
}
