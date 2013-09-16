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
package org.granite.gravity.selector;

import flex.messaging.messages.Message;

/**
 * MessageEvaluationContext is used to cache selection results.
 *
 * A message usually has multiple selectors applied against it. Some selector
 * have a high cost of evaluating against the message. Those selectors may whish
 * to cache evaluation results associated with the message in the
 * MessageEvaluationContext.
 *
 * @version $Revision: 1.4 $
 */
public class MessageEvaluationContext {

    private final Message message;

    public MessageEvaluationContext(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
