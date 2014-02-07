package org.granite.test.jmf.model;

import java.io.IOException;
import java.io.Serializable;

public class BeanWriteReplace implements Serializable {

	private static final long serialVersionUID = 1L;

	private final long value;
	
	public BeanWriteReplace(long value) {
		this.value = value;
	}

	public long getValue() {
		return value;
	}
	
	private Object writeReplace() throws IOException {
		return new BeanReadResolve(value);
	}
}
