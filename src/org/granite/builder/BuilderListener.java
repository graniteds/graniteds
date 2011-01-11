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
	
	public void generating(Input<?> input, Output<?> output) {
		BuilderConsole.println("Generating: " + output.getDescription() + " (" + output.getMessage() + ")");
	}

	public void generating(String file, String message) {
		BuilderConsole.println("Generating: " + file + " (" + message + ")");
	}

	public void skipping(Input<?> input, Output<?> output) {
		BuilderConsole.println("Skipping: " + output.getDescription() + " (" + output.getMessage() + ")", MessageType.DEBUG);
	}

	public void skipping(String file, String message) {
		BuilderConsole.println("Skipping: " + file + " (" + message + ")", MessageType.DEBUG);
	}

	public void debug(String message, Throwable t) {
		BuilderConsole.println(message + "\n" + getStackTrace(t), MessageType.DEBUG);
	}

	public void debug(String message) {
		BuilderConsole.println(message, MessageType.DEBUG);
	}

	public void error(String message, Throwable t) {
		BuilderConsole.println(message + "\n" + getStackTrace(t), MessageType.ERROR);
	}

	public void error(String message) {
		BuilderConsole.println(message, MessageType.ERROR);
	}

	public void info(String message, Throwable t) {
		BuilderConsole.println(message + "\n" + getStackTrace(t), MessageType.INFO);
	}

	public void info(String message) {
		BuilderConsole.println(message, MessageType.INFO);
	}

	public void warn(String message, Throwable t) {
		BuilderConsole.println(message + "\n" + getStackTrace(t), MessageType.WARNING);
	}

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
