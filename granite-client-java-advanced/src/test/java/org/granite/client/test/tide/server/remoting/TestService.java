package org.granite.client.test.tide.server.remoting;

import org.granite.messaging.service.annotations.RemoteDestination;

/**
 * Created by william on 13/02/14.
 */
@RemoteDestination(id="testService")
public class TestService {

    public void fail() throws Exception {
        throw new RuntimeException("fail");
    }
}
