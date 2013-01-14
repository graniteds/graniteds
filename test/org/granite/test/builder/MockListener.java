package org.granite.test.builder;

import org.granite.generator.Input;
import org.granite.generator.Listener;
import org.granite.generator.Output;

public class MockListener implements Listener {

	public void generating(Input<?> input, Output<?> output) {
	}

	public void generating(String file, String message) {
	}

	public void skipping(Input<?> input, Output<?> output) {
	}

	public void skipping(String file, String message) {
	}

	public void debug(String message) {
	}

	public void debug(String message, Throwable t) {
	}

	public void info(String message) {
	}

	public void info(String message, Throwable t) {
	}

	public void warn(String message) {
	}

	public void warn(String message, Throwable t) {
	}

	public void error(String message) {
	}

	public void error(String message, Throwable t) {
	}

	public void removing(Input<?> input, Output<?> output) {
	}

	public void removing(String file, String message) {
	}
	
}
