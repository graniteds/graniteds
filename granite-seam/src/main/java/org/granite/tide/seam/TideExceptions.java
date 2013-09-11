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

package org.granite.tide.seam;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.FRAMEWORK;

import org.granite.context.GraniteContext;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.exception.Exceptions;


/**
 * TideExceptions override to disable default JSF exceptions handling
 * 
 * @author William DRAI
 */
@Name("org.jboss.seam.exception.exceptions")
@Install(precedence=FRAMEWORK, classDependencies="javax.faces.context.FacesContext")
@Scope(APPLICATION)
@BypassInterceptors
public class TideExceptions extends Exceptions {
    
    @Override
    public void handle(Exception e) throws Exception {
        GraniteContext context = GraniteContext.getCurrentInstance();
        if (context == null)
            super.handle(e);
    }
}
