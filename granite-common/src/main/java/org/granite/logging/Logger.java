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
package org.granite.logging;

import java.lang.reflect.Constructor;

import org.granite.util.ServiceLoader;
import org.granite.util.TypeUtil;

/**
 * @author Franck WOLFF
 */
public abstract class Logger {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

	public static final String LOGGER_IMPL_SYSTEM_PROPERTY = "org.granite.logger.impl";

    private static final boolean slf4jAvailable;
	private static final boolean log4jAvailable;
	static {
		boolean slf4j = false;
        boolean log4j = false;
		try {
            TypeUtil.forName("org.slf4j.Logger");
            slf4j = true;
        }
        catch (Exception e) {
            try {
			    TypeUtil.forName("org.apache.log4j.Logger");
			    log4j = true;
		    }
            catch (Exception f) {
            }
		}
        slf4jAvailable = slf4j;
		log4jAvailable = log4j;
	}
	
    private final Object loggerImpl;
    private final LoggingFormatter formatter;

    ///////////////////////////////////////////////////////////////////////////
    // Constructor.
    
    protected Logger(Object loggerImpl, LoggingFormatter formatter) {
        this.loggerImpl = loggerImpl;
        this.formatter = formatter;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Getters.
    
    protected Object getLoggerImpl() {
    	return loggerImpl;
    }
    
    protected LoggingFormatter getFormatter() {
		return formatter;
	}

    ///////////////////////////////////////////////////////////////////////////
    // Static initializers.

	public static Logger getLogger() {
        return getLogger(new DefaultLoggingFormatter());
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName(), new DefaultLoggingFormatter());
    }

    public static Logger getLogger(String name) {
        return getLogger(name, new DefaultLoggingFormatter());
    }

    public static Logger getLogger(LoggingFormatter formatter) {
        Throwable t = new Throwable();
        StackTraceElement[] stes = t.getStackTrace();
        if (stes.length < 2)
            throw new RuntimeException("Illegal instantiation context (stacktrace elements should be of length >= 2)", t);
        return getLogger(stes[1].getClassName());
    }

    public static Logger getLogger(Class<?> clazz, LoggingFormatter formatter) {
        return getLogger(clazz.getName(), formatter);
    }

    public static Logger getLogger(String name, LoggingFormatter formatter) {
    	String loggerImplClass = System.getProperty(LOGGER_IMPL_SYSTEM_PROPERTY);
    	if (loggerImplClass != null) {
    		try {
        		Class<? extends Logger> clazz = TypeUtil.forName(loggerImplClass, Logger.class);
        		Constructor<? extends Logger> constructor = clazz.getConstructor(String.class, LoggingFormatter.class);
        		return constructor.newInstance(name, formatter);
			} catch (Exception e) {
				throw new RuntimeException(
					"Could not create instance of: " + loggerImplClass +
					" (" + LOGGER_IMPL_SYSTEM_PROPERTY + " system property)", e);
			}
    	}
    	
    	ServiceLoader<Logger> loader = ServiceLoader.load(Logger.class);
    	loader.setConstructorParameters(new Class<?>[]{String.class, LoggingFormatter.class}, new Object[]{name, formatter});
    	for (Logger logger : loader)
    		return logger;
    	
		try {
	        return slf4jAvailable ? (Logger)TypeUtil.newInstance("org.granite.logging.impl.Slf4jLogger", new Class[] { String.class, LoggingFormatter.class }, new Object[] { name, formatter }) :
	                (log4jAvailable ? (Logger)TypeUtil.newInstance("org.granite.logging.impl.Log4jLogger", new Class[] { String.class, LoggingFormatter.class }, new Object[] { name, formatter }) : 
	                	(Logger)TypeUtil.newInstance("org.granite.logging.impl.JdkLogger", new Class[] { String.class, LoggingFormatter.class }, new Object[] { name, formatter }));
		} 
		catch (Exception e) {
			throw new RuntimeException("Could not create instance of logger", e);
		}
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Logging methods.

    public abstract void info(String message, Object... args);
    public abstract void info(Throwable t, String message, Object... args);

    public abstract void trace(String message, Object... args);
    public abstract void trace(Throwable t, String message, Object... args);
    
    public abstract void warn(String message, Object... args);
    public abstract void warn(Throwable t, String message, Object... args);

    public abstract void debug(String message, Object... args);
    public abstract void debug(Throwable t, String message, Object... args);

    public abstract void error(String message, Object... args);
    public abstract void error(Throwable t, String message, Object... args);

    ///////////////////////////////////////////////////////////////////////////
    // Configuration.

    public abstract boolean isDebugEnabled();

    public abstract boolean isErrorEnabled();

    public abstract boolean isFatalEnabled();

    public abstract boolean isInfoEnabled();

    public abstract boolean isTraceEnabled();

    public abstract boolean isWarnEnabled();
}
