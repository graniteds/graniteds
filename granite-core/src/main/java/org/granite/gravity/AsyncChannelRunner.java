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
package org.granite.gravity;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Franck WOLFF
 */
public abstract class AsyncChannelRunner implements Runnable {

	protected final Channel channel;
	private final AtomicBoolean queued = new AtomicBoolean(false);
	
	public AsyncChannelRunner(Channel channel) {
		this.channel = channel;
	}
	
	public final boolean queue(Gravity gravity) {
		if (queued.getAndSet(true))
			return false;
		gravity.execute(this);
		return true;
	}
	
	public final void reset() {
		queued.set(false);
	}
	
	protected abstract void doRun();
	
	public final void run() {
		try {
			doRun();
		}
		finally {
			queued.set(false);
		}
	}

	@Override
	public boolean equals(Object obj) {
		return getClass().isInstance(obj) && channel.equals(((AsyncChannelRunner)obj).channel);
	}

	@Override
	public int hashCode() {
		return channel.hashCode();
	}
}
