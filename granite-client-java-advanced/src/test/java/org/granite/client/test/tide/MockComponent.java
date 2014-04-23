package org.granite.client.test.tide;

import java.lang.reflect.Method;
import java.util.concurrent.Future;

import org.granite.client.tide.NameAware;
import org.granite.client.tide.server.Component;
import org.granite.client.tide.server.TideResponder;
import org.granite.tide.data.model.PageInfo;

public class MockComponent implements Component, NameAware {
	
	private String name;
	
	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public <T> Future<T> call(String operation, Object... args) {
		try {
			Method m = getClass().getMethod(operation, Object.class, PageInfo.class, TideResponder.class);
			m.setAccessible(true);
			m.invoke(this, args[0], args[1], args[2]);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
