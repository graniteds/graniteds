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

import org.granite.gravity.config.AbstractActiveMQTopicDestination;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;


@Scope(ScopeType.APPLICATION)
@Startup
@Install(precedence=Install.BUILT_IN, value=false, classDependencies={"org.granite.seam21.Seam21GraniteConfig"})
public class ActiveMQTopicDestination extends AbstractActiveMQTopicDestination {
	
	@Create
    public void seamInit() {
		Seam21GraniteConfig seam21GraniteConfig = (Seam21GraniteConfig)Component.getInstance(Seam21GraniteConfig.class, true);
		
		super.init(seam21GraniteConfig);
    }
	
}
