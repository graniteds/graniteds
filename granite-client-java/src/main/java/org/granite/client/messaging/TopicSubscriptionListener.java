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

import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.ResultEvent;


/**
 * Callback interface for message listeners
 * Called when an incoming message is received
 *
 * @author Franck WOLFF
 */
public interface TopicSubscriptionListener {

	void onSubscribing(Consumer consumer);
	
	void onSubscriptionSuccess(Consumer consumer, ResultEvent event, String subscriptionId);

	void onSubscriptionFault(Consumer consumer, IssueEvent event);

	void onUnsubscribing(Consumer consumer);
	
	void onUnsubscriptionSuccess(Consumer consumer, ResultEvent event, String subscriptionId);
	
	void onUnsubscriptionFault(Consumer consumer, IssueEvent event, String subscriptionId);
}
