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
package org.granite.client.validation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;

/**
 * Represents a constraint violation received from the server.
 * 
 * @author William DRAI
 */
public class ServerConstraintViolation implements ConstraintViolation<Object> {
	
	private InvalidValue invalidValue;
	private Object rootBean;
	private Object bean;
	private Path propertyPath;
	private String message;
	
	
	/**
	 * Constructs a new <code>ServerConstraintViolation</code> instance.
	 * 
	 * @param invalidValue serialized server-side ConstraintViolation
	 * @param rootBean root bean
	 * @param bean leaf bean
	 */
	public ServerConstraintViolation(InvalidValue invalidValue, Object rootBean, Object bean) {
		this.rootBean = rootBean;
		this.bean = bean;		
		this.propertyPath = new PathImpl(invalidValue.getPath());
		this.message = invalidValue.getMessage();
	}


	public InvalidValue getInvalidValue() {
		return invalidValue;
	}

	public Object getRootBean() {
		return rootBean;
	}

	@Override
	public Class<Object> getRootBeanClass() {
		return Object.class;
	}

	public Object getLeafBean() {
		return bean;
	}

	public Path getPropertyPath() {
		return propertyPath;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String getMessageTemplate() {
		return message;
	}

	@Override
	public ConstraintDescriptor<?> getConstraintDescriptor() {
		return null;
	}
	
	public class PathImpl implements Path {
		
		private List<Node> nodeList = new ArrayList<Node>();
		
		public PathImpl(final String path) {
			nodeList.add(new Node() {
				@Override
				public boolean isInIterable() {
					return true;
				}
				
				@Override
				public String getName() {
					return path;
				}
				
				@Override
				public Object getKey() {
					return null;
				}
				
				@Override
				public Integer getIndex() {
					return null;
				}
			});
		}
		
		@Override
		public Iterator<Node> iterator() {
			return nodeList.iterator();
		}
		
	}
}
