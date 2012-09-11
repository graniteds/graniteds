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

package org.granite.clustering;

import javax.servlet.http.HttpSession;

import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.webapp.ServletGraniteContext;

/**
 * @author Franck WOLFF
 */
public class SessionDistributedDataFactory implements DistributedDataFactory {

	private static final Logger log = Logger.getLogger(SessionDistributedDataFactory.class);
	

	public DistributedData getInstance() {
		HttpSession session = null;
		GraniteContext context = GraniteContext.getCurrentInstance();
		if (context instanceof ServletGraniteContext)
			session = ((ServletGraniteContext)context).getSession(false);
		
		if (session == null) {
			log.debug("Could not get distributed data, no session or session expired");
			return null;
		}
		
		return new SessionDistributedData(session);
	}
}
