/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.messaging.channel;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.granite.util.Base64;

/**
 * Simple username/password credentials
 *
 * @author Franck WOLFF
 */
public final class UsernamePasswordCredentials implements Credentials {

	private final String username;
	private final String password;
	private final Charset charset;

    /**
     * Create credentials with the specified username and password
     * @param username username
     * @param password password
     */
	public UsernamePasswordCredentials(String username, String password) {
		this(username, password, null);
	}

    /**
     * Create credentials with the specified username, password and charset (for localized usernames)
     * @param username username
     * @param password password
     * @param charset charset
     */
	public UsernamePasswordCredentials(String username, String password, Charset charset) {
		this.username = username;
		this.password = password;
		this.charset = (charset != null ? charset : Charset.defaultCharset());
	}

    /**
     * Current username
     * @return username
     */
	public String getUsername() {
		return username;
	}

    /**
     * Current password
     * @return password
     */
	public String getPassword() {
		return password;
	}

    /**
     * Current charset
     * @return charset
     */
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
