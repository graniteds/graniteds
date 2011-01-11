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

package org.granite.generator.exception;

import org.granite.generator.Input;

/**
 * @author Franck WOLFF
 */
public class TransformerNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private final Input<?> input;

	public TransformerNotFoundException(Input<?> input) {
		super(initMessage(input, null));
		this.input = input;
	}

	public TransformerNotFoundException(Input<?> input, String message, Throwable cause) {
		super(initMessage(input, message), cause);
		this.input = input;
	}

	public TransformerNotFoundException(Input<?> input, String message) {
		super(initMessage(input, message));
		this.input = input;
	}

	public TransformerNotFoundException(Input<?> input, Throwable cause) {
		super(initMessage(input, null));
		this.input = input;
	}

	public Input<?> getInput() {
		return input;
	}
	
	private static String initMessage(Input<?> input, String message) {
		if (message == null)
			message = "Could not find any Transformer for input: " + input;
		return message;
	}
}
