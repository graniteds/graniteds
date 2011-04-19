package org.granite.config.flex;

import org.granite.util.XMap;

public interface Channel {

    public String getId();

    public String getClassName() ;

    public EndPoint getEndPoint();

    public XMap getProperties() ;

    public boolean isLegacyXmlSerialization() ;

    public boolean isLegacyCollectionSerialization();

}
