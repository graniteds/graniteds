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
package org.granite.client.android.logging;

import org.granite.logging.Logger;
import org.granite.logging.LoggingFormatter;

import android.util.Log;

/**
 * @author Franck WOLFF
 */
public class AndroidLogger extends Logger {

	private final String tag;
	
	public AndroidLogger(String tag, LoggingFormatter formatter) {
		super(null, formatter);
		
		if (tag.length() > 23) {
			int dot = tag.lastIndexOf('.');
			if (dot >= 0 && dot < tag.length() - 1)
				tag = tag.substring(dot + 1);
			if (tag.length() > 23)
				tag = tag.substring(tag.length() - 23);
		}
		this.tag = tag;
	}

	@Override
	public void info(String message, Object... args) {
		if (Log.isLoggable(tag, Log.INFO))
			Log.i(tag, getFormatter().format(message, args));
	}

	@Override
	public void info(Throwable t, String message, Object... args) {
		if (Log.isLoggable(tag, Log.INFO))
			Log.i(tag, getFormatter().format(message, args), t);
	}

	@Override
	public void trace(String message, Object... args) {
		if (Log.isLoggable(tag, Log.VERBOSE))
			Log.v(tag, getFormatter().format(message, args));
	}

	@Override
	public void trace(Throwable t, String message, Object... args) {
		if (Log.isLoggable(tag, Log.VERBOSE))
			Log.v(tag, getFormatter().format(message, args), t);
	}

	@Override
	public void warn(String message, Object... args) {
		if (Log.isLoggable(tag, Log.WARN))
			Log.w(tag, getFormatter().format(message, args));
	}

	@Override
	public void warn(Throwable t, String message, Object... args) {
		if (Log.isLoggable(tag, Log.WARN))
			Log.w(tag, getFormatter().format(message, args), t);
	}

	@Override
	public void debug(String message, Object... args) {
		if (Log.isLoggable(tag, Log.DEBUG))
			Log.d(tag, getFormatter().format(message, args));
	}

	@Override
	public void debug(Throwable t, String message, Object... args) {
		if (Log.isLoggable(tag, Log.DEBUG))
			Log.d(tag, getFormatter().format(message, args), t);
	}

	@Override
	public void error(String message, Object... args) {
		if (Log.isLoggable(tag, Log.ERROR))
			Log.e(tag, getFormatter().format(message, args));
	}

	@Override
	public void error(Throwable t, String message, Object... args) {
		if (Log.isLoggable(tag, Log.ERROR))
			Log.e(tag, getFormatter().format(message, args), t);
	}

	@Override
	public boolean isDebugEnabled() {
		return Log.isLoggable(tag, Log.DEBUG);
	}

	@Override
	public boolean isErrorEnabled() {
		return Log.isLoggable(tag, Log.ERROR);
	}

	@Override
	public boolean isFatalEnabled() {
		return true;
	}

	@Override
	public boolean isInfoEnabled() {
		return Log.isLoggable(tag, Log.INFO);
	}

	@Override
	public boolean isTraceEnabled() {
		return Log.isLoggable(tag, Log.VERBOSE);
	}

	@Override
	public boolean isWarnEnabled() {
		return Log.isLoggable(tag, Log.WARN);
	}
}
