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
package org.granite.config.flex;

import org.granite.util.XMap;

/**
 * @author Franck WOLFF
 */
public class Channel {

    private static final String LEGACY_XML = "serialization/legacy-xml";
    private static final String LEGACY_COLLECTION = "serialization/legacy-collection";

    private final String id;
    private final String className;
    private final EndPoint endPoint;
    private final XMap properties;

    private final boolean legacyXml;
    private final boolean legacyCollection;

    public Channel(String id, String className, EndPoint endPoint, XMap properties) {
        this.id = id;
        this.className = className;
        this.endPoint = endPoint;
        this.properties = properties;
        this.legacyCollection = Boolean.TRUE.toString().equals(properties.get(LEGACY_COLLECTION));
        this.legacyXml = Boolean.TRUE.toString().equals(properties.get(LEGACY_XML));
    }

    public String getId() {
        return id;
    }

    public String getClassName() {
        return className;
    }

    public EndPoint getEndPoint() {
        return endPoint;
    }

    public XMap getProperties() {
        return properties;
    }

    public boolean isLegacyXmlSerialization() {
        return legacyXml;
    }

    public boolean isLegacyCollectionSerialization() {
        return legacyCollection;
    }

    public static Channel forElement(XMap element) {
        String id = element.get("@id");
        String className = element.get("@class");

        XMap endPointElt = element.getOne("endpoint");
        if (endPointElt == null)
            throw new RuntimeException("Excepting a 'endpoint' element in 'channel-definition': " + id);
        EndPoint endPoint = EndPoint.forElement(endPointElt);

        XMap properties = new XMap(element.getOne("properties"));

        return new Channel(id, className, endPoint, properties);
    }
}
