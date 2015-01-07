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
package org.granite.messaging.service.security;

import org.granite.messaging.service.ServiceException;

/**
 * @author Franck WOLFF
 */
public class SecurityServiceException extends ServiceException {

    private static final long serialVersionUID = 1L;

    /** Code for invalid credentails (wrong username or password) */
    public static String CODE_INVALID_CREDENTIALS = "Server.Security.InvalidCredentials";
    /** Code for other types of authentication errors */
    public static String CODE_AUTHENTICATION_FAILED = "Server.Security.AuthenticationFailed";
    /** Code for illegal access to a service or method that requires authentication */
    public static String CODE_NOT_LOGGED_IN = "Server.Security.NotLoggedIn";
    /** Code for user session timeout */
    public static String CODE_SESSION_EXPIRED = "Server.Security.SessionExpired";
    /** Code for illegal access to a service or method that requires special role or profile */
    public static String CODE_ACCESS_DENIED = "Server.Security.AccessDenied";


    public static SecurityServiceException newInvalidCredentialsException() {
        return new SecurityServiceException(CODE_INVALID_CREDENTIALS);
    }
    public static SecurityServiceException newInvalidCredentialsException(String message) {
        return new SecurityServiceException(CODE_INVALID_CREDENTIALS, message);
    }
    public static SecurityServiceException newInvalidCredentialsException(String message, String details) {
        return new SecurityServiceException(CODE_INVALID_CREDENTIALS, message, details);
    }
    public static SecurityServiceException newAuthenticationFailedException(String message) {
        return new SecurityServiceException(CODE_AUTHENTICATION_FAILED, message);
    }
    
    public static SecurityServiceException newNotLoggedInException() {
        return new SecurityServiceException(CODE_NOT_LOGGED_IN);
    }
    public static SecurityServiceException newNotLoggedInException(String message) {
        return new SecurityServiceException(CODE_NOT_LOGGED_IN, message);
    }
    public static SecurityServiceException newNotLoggedInException(String message, String details) {
        return new SecurityServiceException(CODE_NOT_LOGGED_IN, message, details);
    }

    public static SecurityServiceException newSessionExpiredException() {
        return new SecurityServiceException(CODE_SESSION_EXPIRED);
    }
    public static SecurityServiceException newSessionExpiredException(String message) {
        return new SecurityServiceException(CODE_SESSION_EXPIRED, message);
    }
    public static SecurityServiceException newSessionExpiredException(String message, String details) {
        return new SecurityServiceException(CODE_SESSION_EXPIRED, message, details);
    }

    public static SecurityServiceException newAccessDeniedException() {
        return new SecurityServiceException(CODE_ACCESS_DENIED);
    }
    public static SecurityServiceException newAccessDeniedException(String message) {
        return new SecurityServiceException(CODE_ACCESS_DENIED, message);
    }
    public static SecurityServiceException newAccessDeniedException(String message, String details) {
        return new SecurityServiceException(CODE_ACCESS_DENIED, message, details);
    }


    public SecurityServiceException(String code) {
        this(code, null, null);
    }
    public SecurityServiceException(String code, String message) {
        this(code, message, null);
    }
    public SecurityServiceException(String code, String message, String details) {
        super(code, message, details);
    }
}
