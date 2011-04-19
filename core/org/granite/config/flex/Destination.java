package org.granite.config.flex;

import org.granite.messaging.service.security.DestinationSecurizer;
import org.granite.util.XMap;

import java.util.List;

public interface Destination {

    public String getId();

    public List<String> getChannelRefs();

    public XMap getProperties();

    public boolean isSecured();

    public List<String> getRoles();

    public Adapter getAdapter();

    public Class<?> getScannedClass();

    public DestinationSecurizer getSecurizer();

}
