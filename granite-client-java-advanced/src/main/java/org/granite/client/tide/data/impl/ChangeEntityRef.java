package org.granite.client.tide.data.impl;

import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.tide.data.spi.EntityRef;
import org.granite.tide.data.Change;
import org.granite.tide.data.ChangeRef;

/**
 * Created by william on 05/03/14.
 */
public class ChangeEntityRef implements EntityRef {

    private String className;
    private String uid;
    
    public ChangeEntityRef(Object change, ClientAliasRegistry aliasRegistry) {
    	if (change instanceof Change) {
    		className = ((Change)change).getClassName();
    		uid = ((Change)change).getUid();
    	}
    	else if (change instanceof ChangeRef) {
    		className = ((ChangeRef)change).getClassName();
    		uid = ((ChangeRef)change).getUid();
    	}
    	if (aliasRegistry.getTypeForAlias(className) != null)
    		className = aliasRegistry.getTypeForAlias(className);
    }
    
    public String getClassName() {
        return className;
    }
    
    public String getUid() {
        return uid;
    }
}
