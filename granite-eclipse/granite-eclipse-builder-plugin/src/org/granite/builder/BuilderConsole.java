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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * @author Franck WOLFF
 */
public class BuilderConsole {

	public enum MessageType {
		TITLE,
		DEBUG,
		INFO,
		WARNING,
		ERROR
	}

	private static final String INDENT = "  ";

	private static MessageConsole console = null;
	private static boolean debugEnabled = false;
	
	public static boolean isDebugEnabled() {
		return debugEnabled;
	}

	public static void setDebugEnabled(boolean debugEnabled) {
		BuilderConsole.debugEnabled = debugEnabled;
	}

	private static synchronized MessageConsole getConsole() {
		if (console == null) {
			console = new MessageConsole("Granite", null);
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{console});
		}
		return console;
	}
	
	private static MessageConsoleStream getConsoleStream(MessageType type) {		
		int color = SWT.COLOR_BLACK;
		int font = SWT.NORMAL;
		
		switch (type) {
			case DEBUG:
				color = SWT.COLOR_DARK_GREEN;
				font = SWT.ITALIC;
				break;
			case ERROR:
				color = SWT.COLOR_RED;
				break;
			case WARNING:
				color = SWT.COLOR_DARK_MAGENTA;
				break;
			case TITLE:
				color = SWT.COLOR_DARK_GRAY;
				font = SWT.BOLD | SWT.ITALIC;
				break;
			default:
				break;
		}

		MessageConsoleStream msgConsoleStream = getConsole().newMessageStream();
		msgConsoleStream.setFontStyle(font);
		msgConsoleStream.setColor(Display.getDefault().getSystemColor(color));
		return msgConsoleStream;
	}
	
	public static void println(String msg) {
		println(msg, MessageType.INFO);
	}
	
	public static void activate() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				getConsole().activate(); // show console view...
			}
		});
	}
	
	public static void println(final String msg, final MessageType type) {		
		if (msg == null || (!debugEnabled && type == MessageType.DEBUG))
			return;
		
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (type == MessageType.ERROR)
					getConsole().activate(); // show console view...
				String qMsg = (type == MessageType.TITLE ? msg : INDENT + "[" + type + "] " + msg);
				getConsoleStream(type).println(qMsg);
			}
		});
	}
}
