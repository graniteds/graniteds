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
package org.granite.messaging.webapp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.granite.logging.Logger;
import org.granite.context.AMFContextImpl;
import org.granite.context.GraniteContext;
import org.granite.messaging.amf.AMF0Message;
import org.granite.messaging.amf.process.AMF0MessageProcessor;

/**
 * @author Franck WOLFF
 */
public class AMFMessageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(AMFMessageServlet.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            GraniteContext context = GraniteContext.getCurrentInstance();
            if (context == null)
                throw new ServletException(
                    "No GraniteContext (" + AMFMessageFilter.class.getName() + " not configured in web.xml ?)");

            AMFContextImpl amf = (AMFContextImpl)context.getAMFContext();

            AMF0Message amf0Request = amf.getAMF0Request();

            log.debug(">> Processing AMF0 request: %s", amf0Request);

            AMF0Message amf0Response = AMF0MessageProcessor.process(amf0Request);

            log.debug("<< Returning AMF0 response: %s", amf0Response);

            amf.setAmf0Response(amf0Response);
        } catch (Exception e) {
            log.error(e, "AMF message error");
            throw new ServletException(e);
        }
    }
}
