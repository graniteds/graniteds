/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.generator;

import java.io.IOException;

import org.granite.generator.exception.GenerationException;

/**
 * @author Franck WOLFF
 */
public abstract class Transformer<I extends Input<?>, O extends Output<?>, C extends Configuration> {

	private C config = null;
	private Listener listener = null;
	
	public Transformer() {
	}
	
	public Transformer(Configuration config, Listener listener) {
		setConfig(config);
		this.listener = listener;
	}
	
	public C getConfig() {
		return config;
	}

	@SuppressWarnings("unchecked")
	public void setConfig(Configuration config) {
		this.config = (C)config;
	}
	
	public Listener getListener() {
		return listener;
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}
	
	protected abstract boolean accept(Input<?> input);
	
	protected abstract O[] getOutputs(I input) throws IOException, GenerationException;

	@SuppressWarnings("unchecked")
	public Output<?>[] generate(Input<?> input) throws IOException, GenerationException {
		O[] outputs = getOutputs((I)input);
		
		for (O output : outputs) {
			if (output.isOutdated()) {
				listener.generating(input, output);
				generate((I)input, output);
			}
			else
				listener.skipping(input, output);
		}
		
		return outputs;
	}
	
	protected abstract void generate(I input, O output) throws IOException, GenerationException;
	
	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.getClass().equals(getClass());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public String toString() {
		return getClass().getName();
	}
}
