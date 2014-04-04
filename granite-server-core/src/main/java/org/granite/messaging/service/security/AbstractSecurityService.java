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
package org.granite.messaging.service.security;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Date;

import javax.servlet.http.HttpSession;

import org.granite.clustering.DistributedData;
import org.granite.config.GraniteConfig;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.process.AMF3MessageProcessor;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.messaging.webapp.ServletGraniteContext;
import org.granite.util.Base64;

import flex.messaging.messages.Message;

/**
 * Abstract implementation of the {@link SecurityService} interface. This class mainly contains
 * utility methods helping with actual implementations.
 * 
 * @author Franck WOLFF
 */
public abstract class AbstractSecurityService implements SecurityService {

    private static final Logger log = Logger.getLogger(AbstractSecurityService.class);

    public static final String AUTH_TYPE = "granite-security";


    public void prelogin(HttpSession session, Object request, String servletName) {
    }


    /**
     * A default implementation of the basic login method, passing null as the extra charset
     * parameter. Mainly here for compatibility purpose.
     * 
     * @param credentials the login:password pair (must be a base64/ISO-8859-1 encoded string).
     */
    public Principal login(Object credentials) throws SecurityServiceException {
    	return login(credentials, null);
	}

	/**
     * Try to login by using remote credentials (see Flex method RemoteObject.setRemoteCredentials()).
     * This method must be called at the beginning of {@link SecurityService#authorize(AbstractSecurityContext)}.
     * 
     * @param context the current security context.
     * @throws SecurityServiceException if login fails.
     */
    protected void startAuthorization(AbstractSecurityContext context) throws SecurityServiceException {
        // Get credentials set with RemoteObject.setRemoteCredentials() and login.
        Object credentials = context.getMessage().getHeader(Message.REMOTE_CREDENTIALS_HEADER);
        if (credentials != null && !("".equals(credentials)))
            login(credentials, (String)context.getMessage().getHeader(Message.REMOTE_CREDENTIALS_CHARSET_HEADER));
        
        // Check session expiration
        if (GraniteContext.getCurrentInstance() instanceof ServletGraniteContext) {
        	HttpSession session = ((ServletGraniteContext)GraniteContext.getCurrentInstance()).getSession(false);
        	if (session == null)
        		return;
        	
        	long serverTime = new Date().getTime();
        	Long lastAccessedTime = (Long)session.getAttribute(GraniteContext.SESSION_LAST_ACCESSED_TIME_KEY);
        	if (lastAccessedTime != null && lastAccessedTime + session.getMaxInactiveInterval()*1000L + 1000L < serverTime) {
        		log.info("No user-initiated action since last access, force session invalidation");
        		session.invalidate();
        	}
        }
    }

    /**
     * Invoke a service method (EJB3, Spring, Seam, etc...) after a successful authorization.
     * This method must be called at the end of {@link SecurityService#authorize(AbstractSecurityContext)}.
     * 
     * @param context the current security context.
     * @throws Exception if anything goes wrong with service invocation.
     */
    protected Object endAuthorization(AbstractSecurityContext context) throws Exception {
        return context.invoke();
    }
    
    /**
     * A security service can optionally indicate that it's able to authorize requests that are not HTTP requests
     * (websockets). In this case the method {@link SecurityService#authorize(AbstractSecurityContext)} will be 
     * invoked in a {@link ServletGraniteContext} and not in a {@link HttpGraniteContext}
     * @return true is a {@link HttpGraniteContext} is mandated
     */
    public boolean acceptsContext() {
    	return GraniteContext.getCurrentInstance() instanceof ServletGraniteContext;
    }

    /**
     * Decode credentials encoded in base 64 (in the form of "username:password"), as they have been
     * sent by a RemoteObject.
     * 
     * @param credentials base 64 encoded credentials.
     * @return an array containing two decoded Strings, username and password.
     * @throws IllegalArgumentException if credentials isn't a String.
     * @throws SecurityServiceException if credentials are invalid (bad encoding or missing ':').
     */
    protected String[] decodeBase64Credentials(Object credentials, String charset) {
        if (!(credentials instanceof String))
            throw new IllegalArgumentException("Credentials should be a non null String: " +
                (credentials != null ? credentials.getClass().getName() : null));

        if (charset == null)
        	charset = "ISO-8859-1";
        
        byte[] bytes = Base64.decode((String)credentials);
        String decoded;
        try {
        	decoded = new String(bytes, charset);
        }
        catch (UnsupportedEncodingException e) {
            throw SecurityServiceException.newInvalidCredentialsException("ISO-8859-1 encoding not supported ???");
        }

        int colon = decoded.indexOf(':');
        if (colon == -1)
            throw SecurityServiceException.newInvalidCredentialsException("No colon");

        return new String[] {decoded.substring(0, colon), decoded.substring(colon + 1)};
    }
    
    /**
     * Handle a security exception. This method is called in
     * {@link AMF3MessageProcessor#processCommandMessage(flex.messaging.messages.CommandMessage)}
     * whenever a SecurityService occurs and does nothing by default.
     * 
     * @param e the security exception.
     */
	public void handleSecurityException(SecurityServiceException e) {
    }

    /**
     * Try to save current credentials in distributed data, typically a user session attribute. This method
     * must be called at the end of a successful {@link SecurityService#login(Object)} operation and is useful
     * in clustered environments with session replication in order to transparently re-authenticate the
     * user when failing over.
     * 
     * @param credentials the credentials to be saved in distributed data.
     */
	protected void endLogin(Object credentials, String charset) {
		try {
			DistributedData gdd = ((GraniteConfig)GraniteContext.getCurrentInstance().getGraniteConfig()).getDistributedDataFactory().getInstance();
			if (gdd != null) {
				gdd.setCredentials(credentials);
				gdd.setCredentialsCharset(charset);
			}
		}
		catch (Exception e) {
			log.error(e, "Could not save credentials in distributed data");
		}
    }
    
	/**
	 * Try to re-authenticate the current user with credentials previously saved in distributed data.
	 * This method must be called in the {@link SecurityService#authorize(AbstractSecurityContext)}
	 * method when the current user principal is null.
	 * 
	 * @return <tt>true</tt> if relogin was successful, <tt>false</tt> otherwise.
	 * 
	 * @see #endLogin(Object, String)
	 */
    protected boolean tryRelogin() {
    	try {
			DistributedData gdd = ((GraniteConfig)GraniteContext.getCurrentInstance().getGraniteConfig()).getDistributedDataFactory().getInstance();
			if (gdd != null) {
				Object credentials = gdd.getCredentials();
	        	if (credentials != null) {
	        		String charset = gdd.getCredentialsCharset();
	        		try {
	        			login(credentials, charset);
		        		return true;
	        		}
	        		catch (SecurityServiceException e) {
	        		}
	        	}
			}
    	}
    	catch (Exception e) {
    		log.error(e, "Could not relogin with credentials found in distributed data");
    	}
        return false;
    }

    /**
     * Try to remove credentials previously saved in distributed data. This method must be called in the
     * {@link SecurityService#logout()} method.
     * 
	 * @see #endLogin(Object, String)
     */
	protected void endLogout() {
		try {
			DistributedData gdd = ((GraniteConfig)GraniteContext.getCurrentInstance().getGraniteConfig()).getDistributedDataFactory().getInstance();
			if (gdd != null) {
				gdd.removeCredentials();
				gdd.removeCredentialsCharset();
			}
		}
		catch (Exception e) {
			log.error(e, "Could not remove credentials from distributed data");
		}
    }
}
