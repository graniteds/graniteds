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
public class CommandMessage extends AsyncMessage {

    private static final long serialVersionUID = 1L;

    public static final String SELECTOR_HEADER = "DSSelector";

    public static final int SUBSCRIBE_OPERATION = 0;
    public static final int UNSUBSCRIBE_OPERATION = 1;
    public static final int POLL_OPERATION = 2;
    public static final int CLIENT_SYNC_OPERATION = 4;
    public static final int CLIENT_PING_OPERATION = 5;
    public static final int CLUSTER_REQUEST_OPERATION = 7;
    public static final int LOGIN_OPERATION = 8;
    public static final int LOGOUT_OPERATION = 9;
    public static final int SESSION_INVALIDATE_OPERATION = 10;
    public static final int UNKNOWN_OPERATION = 10000;

    // Gravity extension (tunnel connection).
    public static final int CONNECT_OPERATION = 20;
    public static final int DISCONNECT_OPERATION = 21;

    private String messageRefType;
    private int operation;

    public CommandMessage() {
        super();
    }

    public String getMessageRefType() {
        return messageRefType;
    }
    public void setMessageRefType(String messageRefType) {
        this.messageRefType = messageRefType;
    }

    public int getOperation() {
        return operation;
    }
    public void setOperation(int operation) {
        this.operation = operation;
    }

    public boolean isSecurityOperation() {
        return isLoginOperation() || isLogoutOperation();
    }
    public boolean isLoginOperation() {
        return operation == LOGIN_OPERATION;
    }
    public boolean isLogoutOperation() {
        return operation == LOGOUT_OPERATION;
    }

    public boolean isClientPingOperation() {
        return operation == CLIENT_PING_OPERATION;
    }

    @Override
    public String toString() {
        return toString("");
    }

    @Override
    public String toString(String indent) {
        StringBuilder sb = new StringBuilder(512);
        sb.append(getClass().getName()).append(" {");
        sb.append('\n').append(indent).append("  messageRefType: ").append(messageRefType);
        sb.append('\n').append(indent).append("  operation: ").append(getReadableOperation(operation));
        super.toString(sb, indent, (isLoginOperation() ? HIDDEN_CREDENTIALS : null));
        sb.append('\n').append(indent).append('}');
        return sb.toString();
    }

    private static String getReadableOperation(int operation) {
        switch (operation) {
        case SUBSCRIBE_OPERATION:
            return "SUBSCRIBE";
        case UNSUBSCRIBE_OPERATION:
            return "UNSUBSCRIBE";
        case POLL_OPERATION:
            return "POLL";
        case CLIENT_SYNC_OPERATION:
            return "CLIENT_SYNC";
        case CLIENT_PING_OPERATION:
            return "CLIENT_PING";
        case CLUSTER_REQUEST_OPERATION:
            return "CLUSTER_REQUEST";
        case LOGIN_OPERATION:
            return "LOGIN";
        case LOGOUT_OPERATION:
            return "LOGOUT";
        case SESSION_INVALIDATE_OPERATION:
            return "SESSION_INVALIDATE";
        case UNKNOWN_OPERATION:
            return "UNKNOWN";

        // Gravity extension (tunnel [dis]connection).
        case CONNECT_OPERATION:
            return "CONNECT";
        case DISCONNECT_OPERATION:
            return "DISCONNECT";

        default:
            return "REALLY UNKNOWN: 0x" + Integer.toBinaryString(operation);
        }
    }
}
