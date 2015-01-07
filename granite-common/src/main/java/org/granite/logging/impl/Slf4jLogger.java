/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.logging.impl;

import org.granite.logging.Logger;
import org.granite.logging.LoggingFormatter;
import org.slf4j.LoggerFactory;

/**
 * @author Franck WOLFF
 */
public class Slf4jLogger extends Logger {

    ///////////////////////////////////////////////////////////////////////////
    // Constructor.

    public Slf4jLogger(String name, LoggingFormatter formatter) {
    	super(LoggerFactory.getLogger(name), formatter);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utility getter.

    @Override
	protected org.slf4j.Logger getLoggerImpl() {
    	return (org.slf4j.Logger)super.getLoggerImpl();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Logging methods.

    @Override
	public void info(String message, Object... args) {
        if (isInfoEnabled())
            getLoggerImpl().info(getFormatter().format(message, args));
    }

    @Override
    public void info(Throwable t, String message, Object... args) {
        if (isInfoEnabled())
            getLoggerImpl().info(getFormatter().format(message, args), t);
    }

    @Override
    public void trace(String message, Object... args) {
        if (isTraceEnabled())
            getLoggerImpl().trace(getFormatter().format(message, args));
    }

    @Override
    public void trace(Throwable t, String message, Object... args) {
        if (isTraceEnabled())
            getLoggerImpl().trace(getFormatter().format(message, args), t);
    }

    @Override
    public void warn(String message, Object... args) {
        if (isWarnEnabled())
            getLoggerImpl().warn(getFormatter().format(message, args));
    }

    @Override
    public void warn(Throwable t, String message, Object... args) {
        if (isWarnEnabled())
            getLoggerImpl().warn(getFormatter().format(message, args), t);
    }

    @Override
    public void debug(String message, Object... args) {
        if (isDebugEnabled())
            getLoggerImpl().debug(getFormatter().format(message, args));
    }

    @Override
    public void debug(Throwable t, String message, Object... args) {
        if (isDebugEnabled())
            getLoggerImpl().debug(getFormatter().format(message, args), t);
    }

    @Override
    public void error(String message, Object... args) {
        if (isErrorEnabled())
            getLoggerImpl().error(getFormatter().format(message, args));
    }

    @Override
    public void error(Throwable t, String message, Object... args) {
        if (isErrorEnabled())
            getLoggerImpl().error(getFormatter().format(message, args), t);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Configuration.

    @Override
    public boolean isDebugEnabled() {
        return getLoggerImpl().isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return getLoggerImpl().isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return getLoggerImpl().isErrorEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return getLoggerImpl().isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return getLoggerImpl().isTraceEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return getLoggerImpl().isWarnEnabled();
    }
}
