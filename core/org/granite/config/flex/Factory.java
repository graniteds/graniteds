package org.granite.config.flex;

import org.granite.util.XMap;

public interface Factory {
    public String getId();

    public String getClassName();

    public XMap getProperties();
}
