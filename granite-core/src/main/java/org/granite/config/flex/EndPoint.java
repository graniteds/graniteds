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

package org.granite.config.flex;

import org.granite.util.XMap;

/**
 * @author Franck WOLFF
 */
public class EndPoint {

    private final String uri;
    private final String className;

    public EndPoint(String uri, String className) {
        this.uri = uri;
        this.className = className;
    }

    public String getUri() {
        return uri;
    }

    public String getClassName() {
        return className;
    }

    public static EndPoint forElement(XMap element) {
        String uri = element.get("@uri");
        String className = element.get("@class");

        return new EndPoint(uri, className);
    }
}
