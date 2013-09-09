/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */

package org.granite.client.validation.javafx;

import java.util.List;

import org.granite.client.validation.javafx.ValidationResult;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/**
 * @author William DRAI
 */
public class ValidationResultEvent extends Event {

	private static final long serialVersionUID = 1L;
	
	public static EventType<ValidationResultEvent> ANY = new EventType<ValidationResultEvent>(Event.ANY);
	public static EventType<ValidationResultEvent> VALID = new EventType<ValidationResultEvent>(ANY, "valid");
	public static EventType<ValidationResultEvent> INVALID = new EventType<ValidationResultEvent>(ANY, "invalid");
	public static EventType<ValidationResultEvent> UNHANDLED = new EventType<ValidationResultEvent>(ANY, "unhandled");
	

	private final List<ValidationResult> errorResults;
	
	public ValidationResultEvent(Object source, EventTarget target, EventType<ValidationResultEvent> type, List<ValidationResult> errorResults) {
		super(source, target, type);
		this.errorResults = errorResults;
	}

	public List<ValidationResult> getErrorResults() {
		return errorResults;
	}
}
