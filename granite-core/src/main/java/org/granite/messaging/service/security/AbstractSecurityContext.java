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

package org.granite.messaging.service.security;

import org.granite.config.flex.Destination;

import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractSecurityContext {

    private final Message message;
    private final Destination destination;

    public AbstractSecurityContext(Message message, Destination destination) {
        this.message = message;
        this.destination = destination;
    }

    public Message getMessage() {
        return message;
    }

    public Destination getDestination() {
        return destination;
    }

    public abstract Object invoke() throws Exception;
}
