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

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.granite.config.GraniteConfigListener;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.messaging.webapp.ServletGraniteContext;

/**
 * @author Franck WOLFF
 */
public class SessionDistributedDataFactory implements DistributedDataFactory {

	private static final Logger log = Logger.getLogger(SessionDistributedDataFactory.class);
	

	public DistributedData getInstance() {
		HttpSession session = null;
		GraniteContext context = GraniteContext.getCurrentInstance();
		if (context instanceof HttpGraniteContext)
			session = ((HttpGraniteContext)context).getSession(false);
		else if (context instanceof ServletGraniteContext) {
			ServletContext servletContext = ((ServletGraniteContext)context).getServletContext();
			@SuppressWarnings("unchecked")
			Map<String, HttpSession> sessionMap = (Map<String, HttpSession>)servletContext.getAttribute(GraniteConfigListener.GRANITE_SESSION_MAP);
			if (sessionMap != null)
				session = sessionMap.get(context.getSessionId());
		}
		
		if (session == null) {
			log.warn("Could not get distributed data, no HTTP session");
			return null;
		}
		
		return new SessionDistributedData(session);
	}
}
