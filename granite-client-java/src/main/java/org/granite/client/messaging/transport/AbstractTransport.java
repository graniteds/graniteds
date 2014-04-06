/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.messaging.transport;

import java.util.ArrayList;
import java.util.List;

import org.granite.client.messaging.transport.TransportStatusHandler.LogEngineStatusHandler;
import org.granite.client.messaging.transport.TransportStatusHandler.NoopEngineStatusHandler;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractTransport<C> implements Transport {

	private volatile C context;
	private volatile TransportStatusHandler statusHandler = new LogEngineStatusHandler();
	
	protected final List<TransportStopListener> stopListeners = new ArrayList<TransportStopListener>();


    public AbstractTransport() {
    }

    public AbstractTransport(C context) {
        this.context = context;
    }
	
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
