/*
  GRANITE DATA SERVICES
  Copyright (C) 2013 GRANITE DATA SERVICES S.A.S.

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

package org.granite.util;

/**
 * @author Franck WOLFF
 */
public enum ContentType {

	AMF("application/x-amf"),
	JMF_AMF("application/x-jmf+amf");
	
	private final String mimeType;
	
	ContentType(String mimeType) {
		if (mimeType == null)
			throw new NullPointerException("mimeType cannot be null");
		this.mimeType = mimeType;
	}

	public String mimeType() {
		return mimeType;
	}
	
	public static ContentType forMimeType(String mimeType) {
		for (ContentType type : values()) {
			if (type.mimeType.equals(mimeType))
				return type;
		}
		return null;
	}
}
