package org.granite.test.externalizers;

import org.granite.messaging.amf.io.util.externalizer.DefaultExternalizer;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedBean;
import org.granite.messaging.annotations.Include;

@ExternalizedBean(type=DefaultExternalizer.class)
public abstract class AbstractBean {

	private String prop1;
	private String prop3;

	public String getProp1() {
		return prop1;
	}

	public void setProp1(String prop1) {
		this.prop1 = prop1;
	}
	
	@Include
    public abstract String getProp2();

	public String getProp3() {
		return prop3;
	}

	public void setProp3(String prop3) {
		this.prop3 = prop3;
	}
}
