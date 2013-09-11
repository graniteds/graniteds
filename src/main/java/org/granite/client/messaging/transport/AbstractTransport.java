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
package org.granite.client.messaging.transport;

import java.util.ArrayList;
import java.util.List;

import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.transport.TransportStatusHandler.LogEngineStatusHandler;
import org.granite.client.messaging.transport.TransportStatusHandler.NoopEngineStatusHandler;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractTransport<C> implements Transport {

	private volatile C context;
	private volatile Configuration config;
	private volatile TransportStatusHandler statusHandler = new LogEngineStatusHandler();
	
	protected final List<TransportStopListener> stopListeners = new ArrayList<TransportStopListener>();
	
	@SuppressWarnings("unchecked")
	@Override
	public void setContext(Object context) {
		this.context = (C)context;
	}

	@Override
	public C getContext() {
		return context;
	}

	@Override
	public void setConfiguration(Configuration config) {
		this.config = config;
	}

	@Override
	public Configuration getConfiguration() {
		return config;
	}

	@Override
	public void setStatusHandler(TransportStatusHandler statusHandler) {
		if (statusHandler == null)
			statusHandler = new NoopEngineStatusHandler();
		this.statusHandler = statusHandler;
	}

	@Override
	public TransportStatusHandler getStatusHandler() {
		return statusHandler;
	}

	@Override
	public void addStopListener(TransportStopListener listener) {
		synchronized (stopListeners) {
			if (!stopListeners.contains(listener))
				stopListeners.add(listener);
		}
	}

	@Override
	public boolean removeStopListener(TransportStopListener listener) {
		synchronized (stopListeners) {
			return stopListeners.remove(listener);
		}
	}

	@Override
	public void stop() {
		synchronized (stopListeners) {
			for (TransportStopListener listener : stopListeners) {
				try {
					listener.onStop(this);
				}
				catch (Exception e) {
				}
			}
			stopListeners.clear();
		}
	}
}
