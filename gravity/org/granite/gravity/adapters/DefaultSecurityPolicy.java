/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.gravity.adapters;

import org.granite.gravity.Channel;

import flex.messaging.messages.Message;

/**
 * A SecurityPolicy which allows everything, provided that the Channel isn't null. This
 * security policy is used by default unless a custom one is configured in the services-config.xml
 * of the current application.
 * <br><br>
 * Example (services-config.xml):
 * <pre>
 * ...
 * &lt;adapters&gt;
 *   &lt;adapter-definition
 *     id="default" class="org.granite.gravity.adapters.SimpleServiceAdapter"
 *     default="true"&gt;
 *     &lt;properties&gt;
 *       &lt;security-policy&gt;path.to.MySecurityPolicy&lt;/security-policy&gt;
 *     &lt;/properties&gt;
 *   &lt;/adapter-definition&gt;
 * &lt;/adapters&gt;
 * ...
 * </pre>
 * 
 * @author Franck WOLFF
 */
public class DefaultSecurityPolicy implements SecurityPolicy {

	public boolean canCreate(Channel client, String channel, Message message) {
        return client != null;
	}

	public boolean canSubscribe(Channel client, String channel, Message messsage) {
        return client != null;
	}

	public boolean canPublish(Channel client, String channel, Message messsage) {
        return client != null;
	}
}
