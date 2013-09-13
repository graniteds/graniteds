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
package org.granite.client.messaging.channel;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.granite.util.Base64;

/**
 * @author Franck WOLFF
 */
public final class UsernamePasswordCredentials implements Credentials {

	private final String username;
	private final String password;
	private final Charset charset;

	public UsernamePasswordCredentials(String username, String password) {
		this(username, password, null);
	}

	public UsernamePasswordCredentials(String username, String password, Charset charset) {
		this.username = username;
		this.password = password;
		this.charset = (charset != null ? charset : Charset.defaultCharset());
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public Charset getCharset() {
		return charset;
	}
	
	public String encodeBase64() throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		if (username != null) {
			if (username.indexOf(':') != -1)
				throw new UnsupportedEncodingException("Username cannot contain ':' characters: " + username);
			sb.append(username);
		}
		sb.append(':');
		if (username != null)
			sb.append(password);
		return Base64.encodeToString(sb.toString().getBytes(charset.name()), false);
	}

	@Override
	public String toString() {
		return getClass().getName() + " {username=***, password=***, charset=" + charset + "}";
	}
}
