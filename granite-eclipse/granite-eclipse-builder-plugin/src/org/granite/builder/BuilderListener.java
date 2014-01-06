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

package org.granite.builder;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.granite.builder.BuilderConsole.MessageType;
import org.granite.generator.Input;
import org.granite.generator.Listener;
import org.granite.generator.Output;

/**
 * @author Franck WOLFF
 */
public class BuilderListener implements Listener {

	public void title(String msg) {
		BuilderConsole.println(msg, MessageType.TITLE);
	}
	
	@Override
	public void generating(Input<?> input, Output<?> output) {
		BuilderConsole.println("Generating: " + output.getDescription() + " (" + output.getMessage() + ")");
	}

	@Override
	public void generating(String file, String message) {
		BuilderConsole.println("Generating: " + file + " (" + message + ")");
	}

	@Override
	public void removing(Input<?> input, Output<?> output) {
		BuilderConsole.println("Hidding: " + output.getDescription() + " (" + output.getMessage() + ")");
	}

	@Override
	public void removing(String file, String message) {
		BuilderConsole.println("Hidding: " + file + " (" + message + ")");
	}

	@Override
	public void skipping(Input<?> input, Output<?> output) {
		BuilderConsole.println("Skipping: " + output.getDescription() + " (" + output.getMessage() + ")", MessageType.DEBUG);
	}

	@Override
	public void skipping(String file, String message) {
		BuilderConsole.println("Skipping: " + file + " (" + message + ")", MessageType.DEBUG);
	}

	@Override
	public void debug(String message, Throwable t) {
		BuilderConsole.println(message + "\n" + getStackTrace(t), MessageType.DEBUG);
	}

	@Override
	public void debug(String message) {
		BuilderConsole.println(message, MessageType.DEBUG);
	}

	@Override
	public void error(String message, Throwable t) {
		BuilderConsole.println(message + "\n" + getStackTrace(t), MessageType.ERROR);
	}

	@Override
	public void error(String message) {
		BuilderConsole.println(message, MessageType.ERROR);
	}

	@Override
	public void info(String message, Throwable t) {
		BuilderConsole.println(message + "\n" + getStackTrace(t), MessageType.INFO);
	}

	@Override
	public void info(String message) {
		BuilderConsole.println(message, MessageType.INFO);
	}

	@Override
	public void warn(String message, Throwable t) {
		BuilderConsole.println(message + "\n" + getStackTrace(t), MessageType.WARNING);
	}

	@Override
	public void warn(String message) {
		BuilderConsole.println(message, MessageType.WARNING);
	}

    private static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}
