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

import org.granite.tide.invocation.ContextResult;
import org.jboss.seam.ScopeType;


/**
 * @author William DRAI
 */
public class ScopedContextResult extends ContextResult {

    private static final long serialVersionUID = 1L;
    
    
    private ScopeType scope;
    private Boolean restrict;
    
    
    public ScopedContextResult() {
    }
    
    public ScopedContextResult(String componentName, String expression, ScopeType scope, Boolean restrict) {
        super(componentName, expression);
        this.scope = scope;
        this.restrict = restrict;
    }
    
    public ScopeType getScope() {
        return scope;
    }
    
    @Override
    public Boolean getRestrict() {
        return restrict;
    }
}
