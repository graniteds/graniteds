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
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.granite.client.messaging.messages.AbstractMessage;
import org.granite.client.messaging.messages.ResponseMessage;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractResponseMessage extends AbstractMessage implements ResponseMessage {

	private String correlationId;
	private ResponseMessage next;
	
	public AbstractResponseMessage() {
	}

	public AbstractResponseMessage(String clientId, String correlationId) {
		super(clientId);
		
		this.correlationId = correlationId;
	}

	public AbstractResponseMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers,
		String correlationId) {
		
		super(id, clientId, timestamp, timeToLive, headers);
		
		this.correlationId = correlationId;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}
	
	@Override
	public void setNext(ResponseMessage next) {
		for (ResponseMessage n = next; n != null; n = n.getNext()) {
			if (n == this)
				throw new RuntimeException("Circular chaining to this: " + next);
		}
		this.next = next;
	}

	@Override
	public ResponseMessage getNext() {
		return next;
	}
	
	@Override
	public Iterator<ResponseMessage> iterator() {
		
		final ResponseMessage first = this;
		
		return new Iterator<ResponseMessage>() {

			private ResponseMessage current = first;
			
			@Override
			public boolean hasNext() {
				return current != null;
			}

			@Override
			public ResponseMessage next() {
				if (current == null)
					throw new NoSuchElementException();
				ResponseMessage c = current;
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
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		
		this.correlationId = in.readUTF();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		if (correlationId != null)
			out.writeUTF(correlationId);
		else
			out.writeObject(null);
	}

	@Override
	protected void copy(AbstractMessage message) {
		copy((AbstractResponseMessage)message, correlationId);
	}

	protected void copy(AbstractResponseMessage message, String correlationId) {
		super.copy(message);
		
		message.correlationId = correlationId;
	}

	public ResponseMessage copy(String correlationId) {
		AbstractResponseMessage message = (AbstractResponseMessage)copy();
		
		message.correlationId = correlationId;
		
		return message;
	}

	@Override
	public StringBuilder toString(StringBuilder sb) {
		return super.toString(sb).append("\n    correlationId=").append(correlationId);
	}
}
