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
package org.granite.util;

/**
 * Supported encodings for remoting/messaging channels
 *
 * @author Franck WOLFF
 */
public enum ContentType {

	AMF("application/x-amf"),
	JMF_AMF("application/x-jmf+amf");
	
	public static final String KEY = "Content-Type";
	
	private final String mimeType;
	
	ContentType(String mimeType) {
		if (mimeType == null)
			throw new NullPointerException("mimeType cannot be null");
		this.mimeType = mimeType;
	}

    /**
     * Associated MIME type
     * @return MIME type
     */
	public String mimeType() {
		return mimeType;
	}

    /**
     * Lookup the ContentType from a MIME type
     * @param mimeType MIME type
     * @return corresponding encoding/content type or null if no content type found
     */
	public static ContentType forMimeType(String mimeType) {
		for (ContentType type : values()) {
			if (type.mimeType.equals(mimeType))
				return type;
		}
		return null;
	}
}
