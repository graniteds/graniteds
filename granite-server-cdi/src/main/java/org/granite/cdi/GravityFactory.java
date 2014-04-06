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
package org.granite.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.granite.context.GraniteContext;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityManager;
import org.granite.gravity.GravityProxy;
import org.granite.messaging.webapp.ServletGraniteContext;


/**
 * TideGravity produces the Gravity instance for injection in CDI beans
 * 
 * @author William DRAI
 */
@ApplicationScoped
public class GravityFactory {

    private Gravity gravity;

    private GravityProxy gravityProxy = new CDIGravityProxy();

	@Inject
	Instance<ServletContext> servletContextInstance;

    @SuppressWarnings("serial")
    private static final AnnotationLiteral<Default> DEFAULT_LITERAL = new AnnotationLiteral<Default>() {};

    public void setGravity(Gravity gravity) {
        this.gravity = gravity;
    }
    
    @Produces
    public Gravity getGravity() {
        return gravityProxy;
    }

    private class CDIGravityProxy extends GravityProxy {

        @Override
        protected Gravity getGravity() {
            if (gravity != null)
                return gravity;

            ServletContext servletContext = null;

            if (!servletContextInstance.isUnsatisfied()) {
                // Use ServletContext exposed with @Default qualifier (by Apache DeltaSpike Servlet for example)
                servletContext = servletContextInstance.select(DEFAULT_LITERAL).get();
            }
            if (servletContext == null) {
                // If not found, lookup in GraniteContext
                GraniteContext graniteContext = GraniteContext.getCurrentInstance();
                if (graniteContext != null && graniteContext instanceof ServletGraniteContext)
                    servletContext = ((ServletGraniteContext)graniteContext).getServletContext();
            }

            return servletContext != null ? GravityManager.getGravity(servletContext) : null;
        }

    }
}
