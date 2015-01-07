/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package flex.messaging.messages;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Franck WOLFF
 */
public interface Message extends Serializable {

    public static final String ENDPOINT_HEADER = "DSEndpoint";
    public static final String CREDENTIALS_CHARSET_HEADER = "DSCredentialsCharset";
    public static final String REMOTE_CREDENTIALS_HEADER = "DSRemoteCredentials";
    public static final String REMOTE_CREDENTIALS_CHARSET_HEADER = "DSRemoteCredentialsCharset";
    public static final String DS_ID_HEADER = "DSId";

    public static final String HIDDEN_CREDENTIALS = "****** (credentials)";

    public Object getBody();
    public Object getClientId();
    public String getDestination();
    public Object getHeader(String name);
    public Map<String, Object> getHeaders();
    public String getMessageId();
    public long getTimestamp();
    public long getTimeToLive();
    public boolean headerExists(String name);
    public void setBody(Object value);
    public void setClientId(Object value);
    public void setDestination(String value);
    public void setHeader(String name, Object value);
    public void setHeaders(Map<String, Object> value);
    public void setMessageId(String value);
    public void setTimestamp(long value);
    public void setTimeToLive(long value);

    public String toString(String indent);
}
