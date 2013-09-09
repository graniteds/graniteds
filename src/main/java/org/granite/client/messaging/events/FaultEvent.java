/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package org.granite.client.messaging.events;

import java.util.Map;

import org.granite.client.messaging.messages.RequestMessage;
import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.messaging.messages.responses.FaultMessage.Code;

/**
 * @author Franck WOLFF
 */
public class FaultEvent extends AbstractResponseEvent<FaultMessage> implements IssueEvent {

	public FaultEvent(RequestMessage request, FaultMessage response) {
		super(request, response);
	}

	@Override
	public Type getType() {
		return Type.FAULT;
	}
	
	public Code getCode() {
		return response.getCode();
	}

	public String getDescription() {
		return response.getDescription();
	}

	public String getDetails() {
		return response.getDetails();
	}

	public Object getCause() {
		return response.getCause();
	}

	public Map<String, Object> getExtended() {
		return response.getExtended();
	}

	public String getUnknownCode() {
		return response.getUnknownCode();
	}

	@Override
	public String toString() {
		return getClass().getName() + "{code=" + getCode() +
			", description=" + getDescription() +
			", details=" + getDetails() +
			", cause=" + getCause() +
			", extended=" + getExtended() +
			", unknownCode=" + getUnknownCode() +
		"}";
	}
}
