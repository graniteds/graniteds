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

/**
 * @author Franck WOLFF
 */
public class RemotingMessage extends AsyncMessage {

    private static final long serialVersionUID = 1L;

    private String source;
    private String operation;
    private Object[] parameters;	// Since BlazeDS 4
    private String remoteUsername;	// Since BlazeDS 4
    private String remotePassword;  // Since BlazeDS 4


    public RemotingMessage() {
        super();
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public String getRemoteUsername() {
		return remoteUsername;
	}

	public void setRemoteUsername(String remoteUsername) {
		this.remoteUsername = remoteUsername;
	}

	public String getRemotePassword() {
		return remotePassword;
	}

	public void setRemotePassword(String remotePassword) {
		this.remotePassword = remotePassword;
	}

	@Override
    public String toString() {
        return toString("");
    }

    @Override
    public String toString(String indent) {
        StringBuilder sb = new StringBuilder(512);
        sb.append(getClass().getName()).append(" {");
        sb.append('\n').append(indent).append("  source = ").append(source);
        sb.append('\n').append(indent).append("  operation = ").append(operation);
        sb.append('\n').append(indent).append("  remoteUsername = ").append(remoteUsername);
        sb.append('\n').append(indent).append("  remotePassword = ").append(remotePassword != null ? "****" : "null");
        super.toString(sb, indent, null);
        sb.append('\n').append(indent).append('}');
        return sb.toString();
    }
}
