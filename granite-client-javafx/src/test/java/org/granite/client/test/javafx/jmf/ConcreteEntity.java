package org.granite.client.test.javafx.jmf;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class ConcreteEntity extends ModifiableBaseEntity {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "BLA")
	private String bla;
	
	@Column(name = "CLA")
	private String cla;

	public String getBla() {
		return bla;
	}
	public void setBla(String bla) {
		this.bla = bla;
	}

	public String getCla() {
		return cla;
	}
	public void setCla(String cla) {
		this.cla = cla;
	}
}
