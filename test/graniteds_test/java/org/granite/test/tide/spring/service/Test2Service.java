package org.granite.test.tide.spring.service;

import java.math.BigDecimal;

import org.granite.messaging.service.annotations.RemoteDestination;


@RemoteDestination
public interface Test2Service {

    public String test(String name, BigDecimal value);

}
