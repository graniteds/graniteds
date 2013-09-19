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
package org.granite.client.messaging.messages.responses;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

/**
 * @author Franck WOLFF
 */
public final class ResultMessage extends AbstractResponseMessage {

    private static final long serialVersionUID = 1L;
	
	private Object result;
	
	public ResultMessage() {
	}

	public ResultMessage(String clientId, String correlationId, Object result) {
		super(clientId, correlationId);
		
		this.result = result;
	}

	public ResultMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers,
		String correlationId,
		Object result) {
		
		super(id, clientId, timestamp, timeToLive, headers, correlationId);
		
		this.result = result;
	}

	@Override
	public Type getType() {
		return Type.RESULT;
	}

	@Override
	public Object getData() {
		return result;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	@Override
	public ResultMessage copy() {
		ResultMessage message = new ResultMessage();

		super.copy(message);
		
		message.result = result;
		
		return message;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		
		this.result = in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		out.writeObject(result);
	}

	@Override
	public StringBuilder toString(StringBuilder sb) {
		return super.toString(sb).append("\n    result=").append(result);
	}
}
