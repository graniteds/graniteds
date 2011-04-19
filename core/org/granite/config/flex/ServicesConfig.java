package org.granite.config.flex;

import org.granite.config.api.Configuration;

import java.util.List;

public interface ServicesConfig {

    public Service findServiceById(String id);

    public List<Service> findServicesByMessageType(String messageType);

    public void addService(Service service);

    public void removeService(String service);

    public Channel findChannelById(String id);

    public void addChannel(Channel channel);

    public void removeChannel(String channel);

    public Factory findFactoryById(String id);

    public void addFactory(Factory factory);

    public Factory removeFactory(String factory);

    public Destination findDestinationById(String messageType, String id);

    public void scan(Configuration configuration);
}
