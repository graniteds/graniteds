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
package org.granite.client.messaging;

import org.granite.client.messaging.events.CancelledEvent;
import org.granite.client.messaging.events.Event;
import org.granite.client.messaging.events.FailureEvent;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.events.TimeoutEvent;

/**
 * Convenience abstract listener for response events
 *
 * @author Franck WOLFF
 */
public abstract class AllInOneResponseListener implements ResponseListener {

    /**
     * Callback called for any kind of channel event
     * @param event channel event
     */
	public abstract void onEvent(Event event);
	
	@Override
	public void onResult(ResultEvent event) {
		onEvent(event);
	}

	@Override
	public void onFault(FaultEvent event) {
		onEvent(event);
	}

	@Override
	public void onFailure(FailureEvent event) {
		onEvent(event);
	}

	@Override
	public void onTimeout(TimeoutEvent event) {
		onEvent(event);
	}

	@Override
	public void onCancelled(CancelledEvent event) {
		onEvent(event);
	}
}
