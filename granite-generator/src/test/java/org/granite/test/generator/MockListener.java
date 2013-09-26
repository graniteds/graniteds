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
package org.granite.test.generator;

import org.granite.generator.Input;
import org.granite.generator.Listener;
import org.granite.generator.Output;

public class MockListener implements Listener {

	@Override
	public void generating(Input<?> input, Output<?> output) {
	}

	@Override
	public void generating(String file, String message) {
	}

	@Override
	public void skipping(Input<?> input, Output<?> output) {
	}

	@Override
	public void skipping(String file, String message) {
	}

	@Override
	public void debug(String message) {
	}

	@Override
	public void debug(String message, Throwable t) {
	}

	@Override
	public void info(String message) {
	}

	@Override
	public void info(String message, Throwable t) {
	}

	@Override
	public void warn(String message) {
	}

	@Override
	public void warn(String message, Throwable t) {
	}

	@Override
	public void error(String message) {
	}

	@Override
	public void error(String message, Throwable t) {
	}

	@Override
	public void removing(Input<?> input, Output<?> output) {
	}

	@Override
	public void removing(String file, String message) {
	}	
}
