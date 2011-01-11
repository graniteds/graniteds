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

package org.granite.seam21;

import java.io.IOException;

import org.granite.config.AbstractFrameworkGraniteConfig;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.ServletLifecycle;
import org.xml.sax.SAXException;


@Name("org.granite.seam21.graniteConfig")
@Scope(ScopeType.APPLICATION)
public class Seam21GraniteConfig extends AbstractFrameworkGraniteConfig {

    @Create
    public void init() throws IOException, SAXException {
    	configuration.setFlexServicesConfigProperties("seam.properties");
    	
    	super.init(ServletLifecycle.getServletContext(), "org/granite/seam21/granite-config-seam21.xml");
    }
}
