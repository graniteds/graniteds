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

package org.granite.messaging.amf.io.convert;

/**
 * @author Franck WOLFF
 */
public interface Reverter {

    /**
     * Returns true if this reverter can revert the supplied value.
     *
     * @param value the value to test for possible reversion.
     * @return true if this reverter can revert the supplied value.
     */
    public boolean canRevert(Object value);

    /**
     * Converts the supplied object to a well known Java type before AMF3 serialization, for
     * example a JodaTime to a standard Java {@link java.util.Date}.
     *
     * @param value the object to be reverted (converted back to a standard Java type)
     * @return the reverted object.
     */
    public Object revert(Object value);
}
