package org.granite.test.builder;

import org.granite.generator.Input;
import org.granite.generator.Listener;
import org.granite.generator.Output;

public class MockListener implements Listener {

	@Override
	public void generating(Input<?> input, Output<?> output) {
	}

	@Override
	public void generating(String file, String message) {
	}

	@Override
	public void skipping(Input<?> input, Output<?> output) {
	}

	@Override
	public void skipping(String file, String message) {
	}

	@Override
	public void debug(String message) {
	}

	@Override
	public void debug(String message, Throwable t) {
	}

	@Override
	public void info(String message) {
	}

	@Override
	public void info(String message, Throwable t) {
	}

	@Override
	public void warn(String message) {
	}

	@Override
	public void warn(String message, Throwable t) {
	}

	@Override
	public void error(String message) {
	}

	@Override
	public void error(String message, Throwable t) {
	}

	@Override
	public void removing(Input<?> input, Output<?> output) {
	}

	@Override
	public void removing(String file, String message) {
	}	
}
