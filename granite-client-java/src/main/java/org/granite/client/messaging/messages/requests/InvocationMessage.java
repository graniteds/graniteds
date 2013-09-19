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
package org.granite.client.messaging.messages.requests;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.granite.client.messaging.messages.MessageChain;

/**
 * @author Franck WOLFF
 */
public final class InvocationMessage extends AbstractRequestMessage implements MessageChain<InvocationMessage> {

    private static final long serialVersionUID = 1L;
	
	private String serviceId = null;
	private String method = null;
	private Object[] parameters = null;
	
	private InvocationMessage next = null;
	
	public InvocationMessage() {
	}

	public InvocationMessage(String serviceId, String method, Object[] parameters) {
		this(null, serviceId, method, parameters);
	}

	public InvocationMessage(String clientId, String serviceId, String method, Object[] parameters) {
		super(clientId);

		this.serviceId = serviceId;
		this.method = method;
		this.parameters = parameters;
	}

	public InvocationMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers,
		String serviceId,
		String method,
		Object[] parameters) {
		
		super(id, clientId, timestamp, timeToLive, headers);
		
		this.serviceId = serviceId;
		this.method = method;
		this.parameters = parameters;
	}

	@Override
	public Type getType() {
		return Type.INVOCATION;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}

	@Override
	public void setNext(InvocationMessage next) {
		for (InvocationMessage n = next; n != null; n = n.getNext()) {
			if (n == this)
				throw new RuntimeException("Circular chaining to this: " + next);
		}
		this.next = next;
	}

	@Override
	public InvocationMessage getNext() {
		return next;
	}
	
	@Override
	public Iterator<InvocationMessage> iterator() {
		
		final InvocationMessage first = this;
		
		return new Iterator<InvocationMessage>() {

			private InvocationMessage current = first;
			
			@Override
			public boolean hasNext() {
				return current != null;
			}

			@Override
			public InvocationMessage next() {
				if (current == null)
					throw new NoSuchElementException();
				InvocationMessage c = current;
				current = current.getNext();
				return c;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public InvocationMessage copy() {
		InvocationMessage message = new InvocationMessage();

		copy(message);
		
		message.serviceId = serviceId;
		message.method = method;
		message.parameters = parameters;
		
		return message;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		
		this.serviceId = in.readUTF();
		this.method = in.readUTF();
		this.parameters = (Object[])in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		out.writeUTF(serviceId);
		out.writeUTF(method);
		out.writeObject(parameters);
	}
	
	@Override
	public StringBuilder toString(StringBuilder sb) {
		return super.toString(sb)
			.append("\n    serviceId=").append(serviceId)
			.append("\n    method=").append(method)
			.append("\n    parameters=").append(parameters == null ? null : Arrays.toString(parameters));
	}
}
