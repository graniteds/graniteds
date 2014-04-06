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
package org.granite.client.test;

import org.granite.client.messaging.channel.Channel;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.messaging.transport.TransportFuture;
import org.granite.client.messaging.transport.TransportMessage;
import org.granite.client.messaging.transport.TransportStatusHandler;
import org.granite.client.messaging.transport.TransportStopListener;

/**
* Created by william on 02/10/13.
*/
public class MockTransport implements Transport {

    @Override
    public void setContext(Object context) {
    }

    @Override
    public boolean isReconnectAfterReceive() {
        return false;
    }

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public void stop() {
    }

    @Override
    public void setStatusHandler(TransportStatusHandler statusHandler) {
    }

    @Override
    public TransportStatusHandler getStatusHandler() {
        return null;
    }

    @Override
    public void addStopListener(TransportStopListener listener) {
    }

    @Override
    public boolean removeStopListener(TransportStopListener listener) {
        return false;
    }

    @Override
    public TransportFuture send(Channel channel, TransportMessage message) throws TransportException {
        return null;
    }
}
