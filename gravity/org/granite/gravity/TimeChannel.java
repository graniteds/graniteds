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

package org.granite.gravity;

import java.util.TimerTask;

/**
 * @author Franck WOLFF
 */
public class TimeChannel {

	private final Channel channel;
	private TimerTask timerTask;
	
	public TimeChannel(Channel channel) {
		this(channel, null);
	}
	
	public TimeChannel(Channel channel, TimerTask timerTask) {
		if (channel == null)
			throw new NullPointerException("Channel cannot be null");
		this.channel = channel;
		this.timerTask = timerTask;
	}

	public Channel getChannel() {
		return channel;
	}

	public TimerTask getTimerTask() {
		return timerTask;
	}

	public void setTimerTask(TimerTask timerTask) {
		this.timerTask = timerTask;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof TimeChannel && channel.equals(((TimeChannel)obj).channel);
	}

	@Override
	public int hashCode() {
		return channel.hashCode();
	}
}
