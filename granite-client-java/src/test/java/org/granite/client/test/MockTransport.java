package org.granite.client.test;

import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.channel.Channel;
import org.granite.client.messaging.transport.*;

/**
* Created by william on 02/10/13.
*/
public class MockTransport implements Transport {

    @Override
    public void setContext(Object context) {
    }

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public void setConfiguration(Configuration config) {
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    @Override
    public boolean start() {
        return false;
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public void stop() {
    }

    @Override
    public void setStatusHandler(TransportStatusHandler statusHandler) {
    }

    @Override
    public TransportStatusHandler getStatusHandler() {
        return null;
    }

    @Override
    public void addStopListener(TransportStopListener listener) {
    }

    @Override
    public boolean removeStopListener(TransportStopListener listener) {
        return false;
    }

    @Override
    public TransportFuture send(Channel channel, TransportMessage message) throws TransportException {
        return null;
    }

    @Override
    public void poll(Channel channel, TransportMessage message) throws TransportException {
    }
}
