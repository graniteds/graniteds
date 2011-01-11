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

package org.granite.messaging.service;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Franck WOLFF
 */
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String code;
    private final String detail;
    private final Map<String,Object> extendedData;

    public ServiceException(String message) {
        this(message, null, null, null);
    }

    public ServiceException(String message, Throwable cause) {
        this(message, null, null, cause);
    }

    public ServiceException(String message, String detail) {
        this(null, message, null, null);
    }

    public ServiceException(String code, String message, String detail) {
        this(code, message, detail, null);
    }

    public ServiceException(String message, String detail, Throwable cause) {
        this(null, message, null, cause);
    }

    public ServiceException(String code, String message, String detail, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.detail = detail;
        this.extendedData = new HashMap<String,Object>();
    }

    public String getCode() {
        return code;
    }

    public String getDetail() {
        return detail;
    }

    public Map<String,Object> getExtendedData() {
        return extendedData;
    }

}
