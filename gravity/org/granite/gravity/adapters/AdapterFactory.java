package org.granite.gravity.adapters;

import flex.messaging.messages.Message;
import org.granite.messaging.service.ServiceException;

import java.io.Serializable;

public interface AdapterFactory extends Serializable {
    public ServiceAdapter getServiceAdapter(Message request) throws ServiceException;

    public ServiceAdapter getServiceAdapter(String messageType, String destinationId) throws ServiceException;

    public void stopAll();
}
