/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.test.javafx.tide;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.Entity;

@Entity
@RemoteAlias("org.granite.test.tide.DocumentPayload")
public class DocumentPayload extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	private ObjectProperty<byte[]> payload = new SimpleObjectProperty<byte[]>(this, "payload");
    private ObjectProperty<Document> document = new SimpleObjectProperty<Document>(this, "document");


    public DocumentPayload() {
        super();
    }

    public DocumentPayload(Long id, Long version, String uid, Document document) {
        super(id, version, uid);
        this.document.set(document);
    }

    public DocumentPayload(Long id, boolean initialized, String detachedState) {
        super(id, initialized, detachedState);
    }

    public ObjectProperty<byte[]> payloadProperty() {
        return payload;
    }
    public byte[] getPayload() {
        return payload.get();
    }
    public void setPayload(byte[] payload) {
        this.payload.set(payload);
    }

    public ObjectProperty<Document> documentProperty() {
        return document;
    }
    public Document getDocument() {
        return document.get();
    }
    public void setDocument(Document document) {
        this.document.set(document);
    }
}
