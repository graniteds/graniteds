package org.granite.test.jmf.model;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedHashMap;

public class BeanReadResolve implements Serializable {

	private static final long serialVersionUID = 1L;

	private int value = 0;
	
	// Just to make sure that the cache of stored objects isn't messed up...
	@SuppressWarnings("unused")
	private Object[] dummies;
	
	public BeanReadResolve() {
	}
	
	public BeanReadResolve(long value) {
		this.value = (int)value;
		
		Dummy dummy = new Dummy();
		this.dummies = new Object[] {
			dummy,
			new BigDecimal(4),
			new Dummy(),
			dummy,
			new LinkedHashMap<Object, Object>()
		};
	}
	
	private Object readResolve() throws IOException {
		return new BeanWriteReplace(value);
	}
}
