package org.granite.builder;

import java.io.File;
import java.util.Map;

import org.granite.builder.properties.Gas3Source;
import org.granite.generator.as3.JavaAs3Input;

/**
 * @author Franck WOLFF
 */
public class BuilderJavaClientInput extends JavaAs3Input {

	private final Gas3Source gas3Source;
	
	public BuilderJavaClientInput(Class<?> type, File file, Gas3Source gas3Source, Map<String, String> attributes) {
		super(type, file, attributes);
		if (gas3Source == null)
			throw new NullPointerException("gas3Source cannot be null");
		this.gas3Source = gas3Source;
	}

	public Gas3Source getGas3Source() {
		return gas3Source;
	}
}
