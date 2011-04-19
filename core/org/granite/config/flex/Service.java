package org.granite.config.flex;

public interface Service {


    public String getId();

    public String getClassName();

    public String getMessageTypes();

    public Destination findDestinationById(String id);

    public void addDestination(Destination destination);

    public Destination removeDestination(String destination);

    public Adapter findAdapterById(String id);


    public Adapter getDefaultAdapter();

    public void addAdapter(Adapter adapter);

    public Adapter removeAdapter(String adapter);
}
