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
package org.granite.client.tide.data.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.granite.client.persistence.Entity;
import org.granite.client.persistence.Id;
import org.granite.client.persistence.Uid;
import org.granite.client.persistence.Version;
import org.granite.client.tide.data.EntityProxy;

/**
 * Created by william on 07/03/14.
 */
@Entity
public class ChangeProxy implements EntityProxy {

    private String uidPropertyName;
    @Uid
    private String uid;
    private String idPropertyName;
    @Id
    private Object id;
    private String versionPropertyName;
    @Version
    private Number version;
    private Map<String, Object> changes;
    private Object templateObject;


    public ChangeProxy(String uidPropertyName, String uid, String idPropertyName, Object id, String versionPropertyName, Number version, Map<String, Object> changes, Object templateObject) {
    	this.uidPropertyName = uidPropertyName;
        this.uid = uid;
        this.idPropertyName = idPropertyName;
        this.id = id;
        this.versionPropertyName = versionPropertyName;
        this.version = version;
        this.changes = changes;
        this.templateObject = templateObject;
    }

    public Object getProperty(String propName) {
        if (propName.equals(idPropertyName))
            return id;
        else if (propName.equals(versionPropertyName))
            return version;
        else if (propName.equals(uidPropertyName))
        	return uid;
        else if (changes.containsKey(propName))
            return changes.get(propName);
        return null;
    }

    public boolean hasProperty(String propName) {
        if (propName.equals(idPropertyName))
            return true;
        else if (propName.equals(versionPropertyName))
            return true;
        else if (propName.equals(uidPropertyName))
        	return true;
        return changes.containsKey(propName);
    }

    public Object getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public String getClassName() {
        return templateObject.getClass().getName();
    }

    public Object getWrappedObject() {
        return templateObject;
    }

    @Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (getClassName().indexOf(".") > 0)
			sb.append(getClassName().substring(getClassName().lastIndexOf(".")+1));
		else
			sb.append(getClassName());
		sb.append(':').append(getUid()).append(":").append(getId()).append(':').append(version).append("={");
		boolean first = true;
		for (Entry<String, Object> change : changes.entrySet()) {
			if (first)
				first = false;
			else
				sb.append(", ");
			sb.append(change.getKey()).append(": ").append(change.getValue());
		}
		sb.append("}");
		return sb.toString();
	}
}
