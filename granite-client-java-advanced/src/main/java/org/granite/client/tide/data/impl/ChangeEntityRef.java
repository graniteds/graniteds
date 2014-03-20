package org.granite.client.tide.data.impl;

import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.tide.data.spi.EntityRef;
import org.granite.tide.data.Change;

/**
 * Created by william on 05/03/14.
 */
public class ChangeEntityRef implements EntityRef {

    private Change change;
    private ClientAliasRegistry aliasRegistry;

    public ChangeEntityRef(Change change, ClientAliasRegistry aliasRegistry) {
        this.change = change;
        this.aliasRegistry = aliasRegistry;
    }

    public String getClassName() {
        return aliasRegistry.getTypeForAlias(change.getClassName());
    }

    public String getUid() {
        return change.getUid();
    }
}
