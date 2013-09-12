/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.tide;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

@SuppressWarnings("deprecation")
public class MockHttpSession implements HttpSession {
	
	private String id;
	private ServletContext servletContext;
	
	public MockHttpSession(String id, ServletContext servletContext) {
		this.id = id;
		this.servletContext = servletContext;
	}

	@Override
	public Object getAttribute(String arg0) {
		return null;
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return null;
	}

	@Override
	public long getCreationTime() {
		return 0;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public long getLastAccessedTime() {
		return 0;
	}

	@Override
	public int getMaxInactiveInterval() {
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

	@Override
	public HttpSessionContext getSessionContext() {
		return null;
	}

	@Override
	public Object getValue(String arg0) {
		return null;
	}

	@Override
	public String[] getValueNames() {
		return null;
	}

	@Override
	public void invalidate() {
	}

	@Override
	public boolean isNew() {
		return false;
	}

	@Override
	public void putValue(String arg0, Object arg1) {
	}

	@Override
	public void removeAttribute(String arg0) {
	}

	@Override
	public void removeValue(String arg0) {
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
	}

	@Override
	public void setMaxInactiveInterval(int arg0) {
	}

}
