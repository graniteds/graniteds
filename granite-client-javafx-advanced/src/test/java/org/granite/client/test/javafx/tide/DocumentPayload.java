/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
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
