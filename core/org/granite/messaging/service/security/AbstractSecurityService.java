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

package org.granite.messaging.service.security;

import java.io.UnsupportedEncodingException;

import org.granite.util.Base64;

import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractSecurityService implements SecurityService {

    public static final String AUTH_TYPE = "granite-security";

    protected void startAuthorization(AbstractSecurityContext context) throws Exception {
        // RemoteObject.setRemoteCredentials().
        Object credentials = context.getMessage().getHeaders().get(Message.REMOTE_CREDENTIALS_HEADER);
        if (credentials != null && !("".equals(credentials)))
            login(credentials);
    }

    protected Object endAuthorization(AbstractSecurityContext context) throws Exception {
        return context.invoke();
    }

    protected String[] decodeBase64Credentials(Object credentials) {
        if (!(credentials instanceof String))
            throw new IllegalArgumentException("Credentials should be a non null String: " +
                (credentials != null ? credentials.getClass().getName() : null));

        byte[] bytes = Base64.decode((String)credentials);
        String decoded = "";
        try {
        	decoded = new String(bytes, "ISO-8859-1");
        }
        catch (UnsupportedEncodingException e) {
            throw SecurityServiceException.newInvalidCredentialsException("ISO-8859-1 encoding not supported ???");
        }

        int colon = decoded.indexOf(':');
        if (colon == -1)
            throw SecurityServiceException.newInvalidCredentialsException("No colon");

        return new String[] {decoded.substring(0, colon), decoded.substring(colon + 1)};
    }
    
    public void handleSecurityException(SecurityServiceException e) {
    }
}
