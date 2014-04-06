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
package org.granite.tide.seam21;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.config.flex.Destination;
import org.granite.logging.Logger;
import org.granite.messaging.service.ExtendedServiceExceptionHandler;
import org.granite.messaging.service.ServiceException;
import org.granite.tide.TideMessage;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;

import flex.messaging.messages.Message;


/**
 * @author Venkat DANDA
 * @author Cameron INGRAM
 *
 * Update services-config.xml to use the seam service exception handler
 * <factory id="tideSeamFactory" class="org.granite.tide.seam.SeamServiceFactory" >
 *	<properties>
 *		<service-exception-handler>org.granite.tide.seam.SeamServiceExceptionHandler</service-exception-handler>
 *	</properties>
 * </factory>
 */
public class Seam21ServiceExceptionHandler extends ExtendedServiceExceptionHandler {

    private static final long serialVersionUID = -1L;
    
    private static final Logger log = Logger.getLogger(Seam21ServiceExceptionHandler.class);

    
    public Seam21ServiceExceptionHandler() {
    	super(true);
    }

    public Seam21ServiceExceptionHandler(boolean logException) {
    	super(logException);
    }

    
	@Override
    protected ServiceException getServiceException(Message request, Destination destination, String method, Throwable t) {
        ServiceException se = super.getServiceException(request, destination, method, t);
        
        StatusMessages statusMessages = StatusMessages.instance();
        if (statusMessages != null) {
            List<TideMessage> tideMessages = new ArrayList<TideMessage>();
            Map<String, List<TideMessage>> tideKeyedMessages = new HashMap<String, List<TideMessage>>();
            try {
                // Execute and get the messages (once again reflection hack to use protected methods) 
                Method m = StatusMessages.class.getDeclaredMethod("doRunTasks");
                m.setAccessible(true);
                m.invoke(statusMessages);
                
                Method m2 = StatusMessages.class.getDeclaredMethod("getMessages");
                m2.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<StatusMessage> messages = (List<StatusMessage>)m2.invoke(statusMessages);
                
                for (StatusMessage msg : messages) {
                    String severity = null;
                    if (msg.getSeverity() == StatusMessage.Severity.INFO)
                        severity = TideMessage.INFO;
                    else if (msg.getSeverity() == StatusMessage.Severity.WARN)
                        severity = TideMessage.WARNING;
                    else if (msg.getSeverity() == StatusMessage.Severity.ERROR)
                        severity = TideMessage.ERROR;
                    else if (msg.getSeverity() == StatusMessage.Severity.FATAL)
                        severity = TideMessage.FATAL;
                    
                    tideMessages.add(new TideMessage(severity, msg.getSummary(), msg.getDetail()));
                }
                
                Method m3 = StatusMessages.class.getDeclaredMethod("getKeyedMessages");
                m3.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<String, List<StatusMessage>> keyedMessages = (Map<String, List<StatusMessage>>)m3.invoke(statusMessages);
                for (Map.Entry<String, List<StatusMessage>> me : keyedMessages.entrySet()) {
                	List<TideMessage> tmsgs = new ArrayList<TideMessage>(me.getValue().size());
    	            for (StatusMessage msg : me.getValue()) {
    	                String severity = null;
    	                if (msg.getSeverity() == StatusMessage.Severity.INFO)
    	                    severity = TideMessage.INFO;
    	                else if (msg.getSeverity() == StatusMessage.Severity.WARN)
    	                    severity = TideMessage.WARNING;
    	                else if (msg.getSeverity() == StatusMessage.Severity.ERROR)
    	                    severity = TideMessage.ERROR;
    	                else if (msg.getSeverity() == StatusMessage.Severity.FATAL)
    	                    severity = TideMessage.FATAL;
    	                
    	                tmsgs.add(new TideMessage(severity, msg.getSummary(), msg.getDetail()));
    	            }
    	            tideKeyedMessages.put(me.getKey(), tmsgs);
                }
            }
            catch (Exception e) {
                log.error("Could not get status messages", e);
            }
            se.getExtendedData().put("messages", tideMessages);
            se.getExtendedData().put("keyedMessages", tideKeyedMessages);
        }
        return se;
    }
}
