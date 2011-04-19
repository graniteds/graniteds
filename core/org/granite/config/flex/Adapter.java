package org.granite.config.flex;

import org.granite.util.XMap;

public interface Adapter {
    public String getId();

    public String getClassName();

    public XMap getProperties();
}
