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
package org.granite.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.granite.messaging.amf.AMF0Body;
import org.granite.messaging.amf.AMF0Header;
import org.granite.messaging.amf.AMF0Message;
import org.granite.messaging.amf.AMF3Object;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.RemotingMessage;

/**
 * @author Franck WOLFF
 */
public class JMFAMFUtil {
	
	public static List<String> AMF_DEFAULT_STORED_STRINGS = Collections.unmodifiableList(Arrays.asList(
		
		AMF0Message.class.getName(),
		AMF0Header.class.getName(),
		AMF0Body.class.getName(),
		AMF3Object.class.getName(),

		AcknowledgeMessage.class.getName(),
		AsyncMessage.class.getName(),
		CommandMessage.class.getName(),
		ErrorMessage.class.getName(),
		RemotingMessage.class.getName()
	));
}
