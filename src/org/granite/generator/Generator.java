/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.granite.generator.exception.GenerationException;
import org.granite.generator.exception.TransformerNotFoundException;

/**
 * @author Franck WOLFF
 */
public class Generator extends ArrayList<Transformer<?, ?, ?>> {

	///////////////////////////////////////////////////////////////////////////
	// Fields.

	private static final long serialVersionUID = 1L;

	public static final String VERSION = "2.2.1";
	
	private Configuration config = null;

	///////////////////////////////////////////////////////////////////////////
	// Constructors.
	
	public Generator() {
	}
	
	public Generator(Configuration config) {
		this.config = config;
	}

	///////////////////////////////////////////////////////////////////////////
	// Getters/Setters.
	
	public Configuration getConfig() {
		return config;
	}

	public void setConfig(Configuration config) {
		this.config = config;
	}

	///////////////////////////////////////////////////////////////////////////
	// Generation method.
	
	public Output<?>[] generate(Input<?> input) throws IOException, GenerationException {
		updateTransformers();
		
		Transformer<?, ?, ?> transformer = null;
		
		for (Transformer<?, ?, ?> t : this) {
			if (t.accept(input)) {
				transformer = t;
				break;
			}
		}
		
		if (transformer == null)
			throw new TransformerNotFoundException(input);
		
		return transformer.generate(input);
	}

	///////////////////////////////////////////////////////////////////////////
	// Utilities.

	protected void updateTransformers() {
		for (Transformer<?, ?, ?> transformer : this)
			transformer.setConfig(config);
	}
	
	protected boolean checkAdd(Transformer<?, ?, ?> transformer) {
		if (transformer == null)
			throw new NullPointerException("A transformer cannot be null");
		return !contains(transformer);
	}

	///////////////////////////////////////////////////////////////////////////
	// ArrayList overridden methods (check for null & duplicates).

	@Override
	public void add(int index, Transformer<?, ?, ?> transformer) {
		if (checkAdd(transformer))
			super.add(index, transformer);
	}

	@Override
	public boolean add(Transformer<?, ?, ?> transformer) {
		if (checkAdd(transformer))
			return super.add(transformer);
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends Transformer<?, ?, ?>> transformers) {
		boolean changed = false;
		for (Transformer<?, ?, ?> transformer : transformers) {
			if (checkAdd(transformer)) {
				super.add(transformer);
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public boolean addAll(int index, Collection<? extends Transformer<?, ?, ?>> transformers) {
		boolean changed = false;
		for (Transformer<?, ?, ?> transformer : transformers) {
			if (checkAdd(transformer)) {
				super.add(index++, transformer);
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public Transformer<?, ?, ?> set(int index, Transformer<?, ?, ?> transformer) {
		if (checkAdd(transformer))
			return super.set(index, transformer);
		return null;
	}
}
