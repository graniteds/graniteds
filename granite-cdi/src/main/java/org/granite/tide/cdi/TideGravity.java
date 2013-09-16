/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.tide.cdi;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.granite.context.GraniteContext;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityManager;
import org.granite.messaging.webapp.HttpGraniteContext;


/**
 * TideGravity produces the Gravity instance for injection in CDI beans
 * 
 * @author William DRAI
 */
public class TideGravity {

	@Inject
	Instance<ServletContext> servletContextInstance;
	
    @SuppressWarnings("serial")
    private static final AnnotationLiteral<Default> DEFAULT_LITERAL = new AnnotationLiteral<Default>() {}; 
    
    @Produces @RequestScoped
    public Gravity getGravity() {
    	
    	ServletContext servletContext = null;
    	
    	if (!servletContextInstance.isUnsatisfied()) {
    		// Use ServletContext exposed with @Default qualifier (with Seam Servlet for example)
    		servletContext = servletContextInstance.select(DEFAULT_LITERAL).get();
    	}
    	else {
    		// If not found, look in GraniteContext
	    	GraniteContext graniteContext = GraniteContext.getCurrentInstance();
	    	if (graniteContext == null || !(graniteContext instanceof HttpGraniteContext))
	    		throw new RuntimeException("Gravity not found: not in a GraniteDS HTTP context");
	    	
	    	servletContext = ((HttpGraniteContext)graniteContext).getServletContext();
    	}
    	
    	return GravityManager.getGravity(servletContext);
    }
}
