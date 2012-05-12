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

package org.granite.generator.ant;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.granite.generator.Input;
import org.granite.generator.Listener;
import org.granite.generator.Output;

/**
 * @author Franck WOLFF
 */
public class AntListener implements Listener {

    private final Task task;

    public AntListener(Task task) {
        this.task = task;
    }

	public void generating(Input<?> input, Output<?> output) {
		log("  Generating: " + output.getDescription() + " (" + output.getMessage() + ")", Project.MSG_INFO, null);
	}

	public void generating(String file, String message) {
		log("  Generating: " + file + " (" + message + ")", Project.MSG_INFO, null);
	}

	public void removing(Input<?> input, Output<?> output) {
	}

	public void removing(String file, String message) {
	}

	public void skipping(Input<?> input, Output<?> output) {
		log("  Skipping: " + output.getDescription() + " (" + output.getMessage() + ")", Project.MSG_DEBUG, null);
	}

	public void skipping(String file, String message) {
		log("  Skipping: " + file + " (" + message + ")", Project.MSG_DEBUG, null);
	}

	public void debug(String message) {
        log(message, Project.MSG_DEBUG, null);
	}

	public void debug(String message, Throwable t) {
        log(message, Project.MSG_DEBUG, t);
	}

    public void info(String message) {
        log(message, Project.MSG_INFO, null);
    }
    public void info(String message, Throwable t) {
        log(message, Project.MSG_INFO, t);
    }

    public void warn(String message) {
        log(message, Project.MSG_WARN, null);
    }
    public void warn(String message, Throwable t) {
        log(message, Project.MSG_WARN, t);
    }

    public void error(String message) {
        log(message, Project.MSG_ERR, null);
    }
    public void error(String message, Throwable t) {
        log(message, Project.MSG_ERR, t);
    }

    private void log(String message, int level, Throwable t) {
        if (t != null)
            message += "\n" + getStackTrace(t);
        task.log(message, level);
    }

    private static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}
