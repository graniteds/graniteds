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

package org.granite.context;

import org.granite.messaging.amf.AMF0Message;

import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public class AMFContextImpl extends AMFContext {

    private AMF0Message amf0Request = null;
    private AMF0Message amf0Response = null;

    private Message currentAmf3Message = null;

    public AMF0Message getAMF0Request() {
        return amf0Request;
    }
    public void setAmf0Request(AMF0Message amf0Request) {
        this.amf0Request = amf0Request;
    }

    public AMF0Message getAmf0Response() {
        return amf0Response;
    }
    public void setAmf0Response(AMF0Message amf0Response) {
        this.amf0Response = amf0Response;
    }

    @Override
    public Message getRequest() {
        return currentAmf3Message;
    }
    public void setCurrentAmf3Message(Message currentAmf3Message) {
        this.currentAmf3Message = currentAmf3Message;
    }
}
