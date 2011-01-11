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

package org.granite.generator.gsp.token;

/**
 * @author Franck WOLFF
 */
public abstract class Token {

    private final int index;
    private final String content;

    public Token(int index, String content) {
        if (content == null)
            throw new NullPointerException("content cannot be null");
        this.index = index;
        this.content = content;
    }

    public final String getContent() {
        return content;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " [" + index + ", " + content + ']';
    }
}
