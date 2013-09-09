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

import org.granite.client.messaging.messages.RequestMessage;
import org.granite.client.messaging.messages.responses.ResultMessage;

/**
 * @author Franck WOLFF
 */
public class ResultEvent extends AbstractResponseEvent<ResultMessage> {

	public ResultEvent(RequestMessage request, ResultMessage response) {
		super(request, response);
	}

	@Override
	public Type getType() {
		return Type.RESULT;
	}
	
	public Object getResult() {
		return response.getResult();
	}

	@Override
	public String toString() {
		return getClass().getName() + " {result=" + response.getResult() + "}";
	}
}
