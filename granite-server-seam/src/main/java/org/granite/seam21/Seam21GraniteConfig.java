/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
