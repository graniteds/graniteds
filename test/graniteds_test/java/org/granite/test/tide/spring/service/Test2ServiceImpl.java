package org.granite.test.tide.spring.service;

import java.math.BigDecimal;

import org.granite.tide.data.DataEnabled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service("test2Service")
@Transactional
@DataEnabled(topic="testTopic")
public class Test2ServiceImpl implements Test2Service, Test3Service<String, BigDecimal> {
	
    @Override
    public String test(String name, BigDecimal value) {
        if (value.intValue() == 2)
            return "ok";
        return "nok";
    }
}
