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

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.granite.logging.Logger;
import org.granite.logging.LoggingFormatter;

/**
 * @author Franck WOLFF
 */
public class Log4jLogger extends Logger {
	
    ///////////////////////////////////////////////////////////////////////////
    // Fields.

	private static final String FQCN = Log4jLogger.class.getName();
	
    ///////////////////////////////////////////////////////////////////////////
    // Constructor.

    public Log4jLogger(String name, LoggingFormatter formatter) {
    	super(LogManager.getLogger(name), formatter);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utility getter.

    @Override
	protected org.apache.log4j.Logger getLoggerImpl() {
    	return (org.apache.log4j.Logger)super.getLoggerImpl();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Logging methods.

    @Override
	public void info(String message, Object... args) {
        if (isInfoEnabled())
            getLoggerImpl().log(FQCN, Level.INFO, getFormatter().format(message, args), null);
    }

    @Override
    public void info(Throwable t, String message, Object... args) {
        if (isInfoEnabled())
            getLoggerImpl().log(FQCN, Level.INFO, getFormatter().format(message, args), t);
    }

    @Override
    public void trace(String message, Object... args) {
        if (isTraceEnabled())
            getLoggerImpl().log(FQCN, Level.TRACE, getFormatter().format(message, args), null);
    }

    @Override
    public void trace(Throwable t, String message, Object... args) {
        if (isTraceEnabled())
            getLoggerImpl().log(FQCN, Level.TRACE, getFormatter().format(message, args), t);
    }

    @Override
    public void warn(String message, Object... args) {
        if (isWarnEnabled())
            getLoggerImpl().log(FQCN, Level.WARN, getFormatter().format(message, args), null);
    }

    @Override
    public void warn(Throwable t, String message, Object... args) {
        if (isWarnEnabled())
            getLoggerImpl().log(FQCN, Level.WARN, getFormatter().format(message, args), t);
    }

    @Override
    public void debug(String message, Object... args) {
        if (isDebugEnabled())
            getLoggerImpl().log(FQCN, Level.DEBUG, getFormatter().format(message, args), null);
    }

    @Override
    public void debug(Throwable t, String message, Object... args) {
        if (isDebugEnabled())
            getLoggerImpl().log(FQCN, Level.DEBUG, getFormatter().format(message, args), t);
    }

    @Override
    public void error(String message, Object... args) {
        if (isErrorEnabled())
            getLoggerImpl().log(FQCN, Level.ERROR, getFormatter().format(message, args), null);
    }

    @Override
    public void error(Throwable t, String message, Object... args) {
        if (isErrorEnabled())
            getLoggerImpl().log(FQCN, Level.ERROR, getFormatter().format(message, args), t);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Configuration.

    @Override
    public boolean isDebugEnabled() {
        return getLoggerImpl().isEnabledFor(Level.DEBUG);
    }

    @Override
    public boolean isErrorEnabled() {
        return getLoggerImpl().isEnabledFor(Level.ERROR);
    }

    @Override
    public boolean isFatalEnabled() {
        return getLoggerImpl().isEnabledFor(Level.FATAL);
    }

    @Override
    public boolean isInfoEnabled() {
        return getLoggerImpl().isEnabledFor(Level.INFO);
    }

    @Override
    public boolean isTraceEnabled() {
        return getLoggerImpl().isEnabledFor(Level.TRACE);
    }

    @Override
    public boolean isWarnEnabled() {
        return getLoggerImpl().isEnabledFor(Level.WARN);
    }
}
