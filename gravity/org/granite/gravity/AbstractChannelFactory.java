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

package org.granite.gravity;

import javax.servlet.ServletConfig;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractChannelFactory implements ChannelFactory {

	private GravityConfig gravityConfig = null;
	private ServletConfig servletConfig = null;
	
	public void init(GravityConfig gravityConfig, ServletConfig servletConfig) {
		this.gravityConfig = gravityConfig;
		this.servletConfig = servletConfig;
	}

	public GravityConfig getGravityConfig() {
		return gravityConfig;
	}

	public ServletConfig getServletConfig() {
		return servletConfig;
	}

	public void destroy() {
		this.gravityConfig = null;
		this.servletConfig = null;
	}
}
