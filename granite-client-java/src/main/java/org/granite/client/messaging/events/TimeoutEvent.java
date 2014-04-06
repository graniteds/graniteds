/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.messaging.events;

import org.granite.client.messaging.messages.RequestMessage;

/**
 * Event dispatched by a channel when the request is timed out
 *
 * @author Franck WOLFF
 */
public class TimeoutEvent extends AbstractIssueEvent {

	private final long time;
	
	public TimeoutEvent(RequestMessage request, long time) {
		super(request);
		
		this.time = time;
	}

	@Override
	public Type getType() {
		return Type.TIMEOUT;
	}

    /**
     * Time at which the time out occurred
     * @return timeout timestamp
     */
	public long getTime() {
		return time;
	}

	@Override
	public String toString() {
		return getClass().getName() + " {timestamp=" + getRequest().getTimestamp() + " + timeToLive=" + getRequest().getTimeToLive() + " > time=" + time + "}";
	}
}
