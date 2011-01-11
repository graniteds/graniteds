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

package test.granite.ejb3.entity.embed;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Embeddable;

/**
 * @author Franck WOLFF
 */
@Embeddable
public class Document implements Serializable {

    private static final long serialVersionUID = 1L;

    @Basic
    private String name;

    @Basic
    private String contentType;

    @Basic
    private byte[] content;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getContent() {
        return content;
    }
    public void setContent(byte[] content) {
        this.content = content;
    }
}
