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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.Serializable;

/**
 * AMF Message
 *
 * @author Jason Calabrese <jasonc@missionvi.com>
 * @author Pat Maddox <pergesu@users.sourceforge.net>
 * @see AMF0Header
 * @see AMF0Body
 * @version $Revision: 1.13 $, $Date: 2003/11/30 02:25:00 $
 */
public class AMF0Message implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int CURRENT_VERSION = 3;

    protected int version = CURRENT_VERSION;
    protected final List<AMF0Header> headers = new ArrayList<AMF0Header>();
    protected final List<AMF0Body> bodies = new ArrayList<AMF0Body>();

    public void addHeader(String key, boolean required, Object value) {
        addHeader(new AMF0Header(key, required, value));
    }

    public void addHeader(AMF0Header header) {
        headers.add(header);
    }

    public int getHeaderCount() {
        return headers.size();
    }

    public AMF0Header getHeader(int index) {
        return headers.get(index);
    }

    /**
     *
     * @return a List that contains zero or more {@link AMF0Header} objects
     *
     */
    public List<AMF0Header> getHeaders() {
        return headers;
    }

    public AMF0Body addBody(String target, String response, Object value, byte type) {
        return addBody(new AMF0Body(target, response, value, type));
    }

    public AMF0Body addBody(AMF0Body body) {
        bodies.add(body);
        return body;
    }

    public int getBodyCount() {
        return bodies.size();
    }

    public AMF0Body getBody(int index) {
        return bodies.get(index);
    }

    public Iterator<AMF0Body> getBodies() {
        return bodies.iterator();
    }

    public boolean isFirstMessage() {
        if (bodies.size() == 1)
            return bodies.get(0).isFirstBody();

        for (AMF0Body body : bodies) {
            if (body.isFirstBody())
                return true;
        }

        return false;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getBodiesString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bodies.size(); i++) {
            if (i > 0) {
                sb.append('\n');
            }
            AMF0Body amfBody = bodies.get(i);
            sb.append(amfBody);
        }
        return sb.toString();
    }

    /*
     * AMFMessage content
     */

    @Override
    public String toString() {
        return toString("");
    }

    public String toString(String indent) {
        final String innerIndent = indent + "    ";

        StringBuilder sb = new StringBuilder(2048);
        sb.append('\n').append(indent).append(AMF0Message.class.getName()).append(" {");

        // Print version.
        sb.append('\n').append(indent).append("  version = ").append(version);

        // Print headers.
        sb.append('\n').append(indent).append("  headers = [");
        for (int i = 0; i < headers.size(); i++) {
            AMF0Header amfHeader = headers.get(i);
            sb.append(amfHeader.toString(innerIndent));
        }
        if (headers.size() > 0)
            sb.append('\n').append(indent).append("  ");
        sb.append(']');

        // Print bodies.
        sb.append('\n').append(indent).append("  bodies = [");
        for (int i = 0; i < bodies.size(); i++) {
            if (i > 0)
                sb.append(',');
            AMF0Body amfBody = bodies.get(i);
            sb.append(amfBody.toString(innerIndent));
        }
        if (bodies.size() > 0)
            sb.append('\n').append(indent).append("  ");
        sb.append(']');

        sb.append('\n').append(indent).append("}");

        return sb.toString();
    }

}
