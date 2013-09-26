package org.granite.generator.as3;

public enum PropertyType {
	SIMPLE,
	PROPERTY,
	READONLY_PROPERTY;
	
	public boolean isProperty() {
		return this == PROPERTY || this == READONLY_PROPERTY;
	}
}