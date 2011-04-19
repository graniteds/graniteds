package org.granite.messaging.service;

import flex.messaging.messages.RemotingMessage;

public interface MainFactory {
     public ServiceFactory getFactoryInstance(RemotingMessage request) throws ServiceException;
}
