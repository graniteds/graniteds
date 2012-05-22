package org.granite.test.externalizers;

import org.granite.messaging.amf.io.util.externalizer.DefaultExternalizer;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedBean;

@ExternalizedBean(type=DefaultExternalizer.class)
public class ConcreteBean extends AbstractBean {

	private String prop4;

	public String getProp4() {
		return prop4;
	}

	public void setProp4(String prop4) {
		this.prop4 = prop4;
	}

	@Override
	public String getProp2() {
		return "prop2 impl of class " + this.getClass().getName();
	}
}
