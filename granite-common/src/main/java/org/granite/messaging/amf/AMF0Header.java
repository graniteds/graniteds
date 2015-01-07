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
/*
 * www.openamf.org
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.granite.messaging.amf;

import java.io.Serializable;

/**
 * AMF Header
 *
 * @author Jason Calabrese <jasonc@missionvi.com>
 * @author Pat Maddox <pergesu@users.sourceforge.net>
 * @see AMF0Body
 * @see AMF0Message
 * @version $Revision: 1.8 $, $Date: 2003/08/16 13:11:16 $
 */
public class AMF0Header implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String key;
    protected boolean required;
    protected Object value;

    public AMF0Header(String key, boolean required, Object value) {
        this.key = key;
        this.required = required;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return toString("");
    }

    public String toString(String indent) {
        return (new StringBuilder()
            .append(indent).append(AMF0Header.class.getName()).append(" {")
            .append('\n').append(indent).append("  key = ").append(key)
            .append('\n').append(indent).append("  required = ").append(required)
            .append('\n').append(indent).append("  value = ").append(value)
            .append('\n').append(indent).append("}")
            .toString()
        );
    }

}
