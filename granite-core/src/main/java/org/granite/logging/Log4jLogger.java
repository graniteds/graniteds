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

package org.granite.logging;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

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

    @Override
    public void fatal(String message, Object... args) {
        if (isFatalEnabled())
            getLoggerImpl().log(FQCN, Level.FATAL, getFormatter().format(message, args), null);
    }

    @Override
    public void fatal(Throwable t, String message, Object... args) {
        if (isFatalEnabled())
            getLoggerImpl().log(FQCN, Level.FATAL, getFormatter().format(message, args), t);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Configuration.

    @Override
    public void setLevel(org.granite.logging.Level level) {
    	Level log4jLevel = null;
    	switch (level) {
	    	case FATAL: log4jLevel = Level.FATAL; break;
	    	case ERROR: log4jLevel = Level.ERROR; break;
	    	case INFO: log4jLevel = Level.INFO; break;
	    	case TRACE: log4jLevel = Level.TRACE; break;
	    	case DEBUG: log4jLevel = Level.DEBUG; break;
	    	case WARN: log4jLevel = Level.WARN; break;
	    	default: throw new IllegalArgumentException("Unknown logging level: " + level);
    	}
        getLoggerImpl().setLevel(log4jLevel);
    }

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
