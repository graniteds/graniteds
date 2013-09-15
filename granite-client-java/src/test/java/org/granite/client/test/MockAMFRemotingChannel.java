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
package org.granite.client.test;

import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.channel.AsyncToken;
import org.granite.client.messaging.channel.Channel;
import org.granite.client.messaging.channel.RemotingChannel;
import org.granite.client.messaging.channel.amf.AMFRemotingChannel;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.messaging.transport.TransportFuture;
import org.granite.client.messaging.transport.TransportMessage;
import org.granite.client.messaging.transport.TransportStatusHandler;
import org.granite.client.messaging.transport.TransportStopListener;

public class MockAMFRemotingChannel extends AMFRemotingChannel {

	public MockAMFRemotingChannel(Configuration configuration) {
		super(new MockTransport(), configuration, "test", URI.create("/temp"), RemotingChannel.DEFAULT_MAX_CONCURRENT_REQUESTS);
	}
	
	public TransportMessage createMessage(AsyncToken token) throws UnsupportedEncodingException {
		return createTransportMessage(token);
	}

	private static class MockTransport implements Transport {

		@Override
		public void setContext(Object context) {
		}

		@Override
		public Object getContext() {
			return null;
		}

		@Override
		public void setConfiguration(Configuration config) {
		}

		@Override
		public Configuration getConfiguration() {
			return null;
		}

		@Override
		public boolean start() {
			return false;
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

		@Override
		public void poll(Channel channel, TransportMessage message) throws TransportException {
		}
	}
}
