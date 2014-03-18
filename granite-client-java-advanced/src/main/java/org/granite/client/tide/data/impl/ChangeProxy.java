package org.granite.client.tide.data.impl;

import java.util.Map;

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
}
