/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package org.granite.jmx;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanConstructorInfo;
import javax.management.openmbean.OpenMBeanInfoSupport;
import javax.management.openmbean.OpenMBeanOperationInfo;
import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfo;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

/**
 * The OpenMBean class wraps an instance of any bean and introspects its properties and methods
 * for MBeanAttributes and MBeanOperations. It implements all functionalities required by a
 * {@link DynamicMBean} and returns an OpenMBeanInfo object.
 * <br>
 * <br>
 * Limitations:
 * <ul>
 * <li>only attributes and operations are supported (no contructor and no notification),</li>
 * <li>only {@link SimpleType} and {@link ArrayType} are supported (no composite type).</li>
 * </ul>
 * 
 * @author Franck WOLFF
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class OpenMBean implements DynamicMBean {

	///////////////////////////////////////////////////////////////////////////
	// Fields.

	private static final Map<Class<?>, SimpleType> SIMPLE_TYPES = new HashMap<Class<?>, SimpleType>();
	static {
		SIMPLE_TYPES.put(Void.class, SimpleType.VOID);
		SIMPLE_TYPES.put(Void.TYPE, SimpleType.VOID);
		SIMPLE_TYPES.put(Boolean.class, SimpleType.BOOLEAN);
		SIMPLE_TYPES.put(Boolean.TYPE, SimpleType.BOOLEAN);
		SIMPLE_TYPES.put(Character.class, SimpleType.CHARACTER);
		SIMPLE_TYPES.put(Character.TYPE, SimpleType.CHARACTER);
		SIMPLE_TYPES.put(Byte.class, SimpleType.BYTE);
		SIMPLE_TYPES.put(Byte.TYPE, SimpleType.BYTE);
		SIMPLE_TYPES.put(Short.class, SimpleType.SHORT);
		SIMPLE_TYPES.put(Short.TYPE, SimpleType.SHORT);
		SIMPLE_TYPES.put(Integer.class, SimpleType.INTEGER);
		SIMPLE_TYPES.put(Integer.TYPE, SimpleType.INTEGER);
		SIMPLE_TYPES.put(Long.class, SimpleType.LONG);
		SIMPLE_TYPES.put(Long.TYPE, SimpleType.LONG);
		SIMPLE_TYPES.put(Float.class, SimpleType.FLOAT);
		SIMPLE_TYPES.put(Float.TYPE, SimpleType.FLOAT);
		SIMPLE_TYPES.put(Double.class, SimpleType.DOUBLE);
		SIMPLE_TYPES.put(Double.TYPE, SimpleType.DOUBLE);
		SIMPLE_TYPES.put(String.class, SimpleType.STRING);
		SIMPLE_TYPES.put(BigDecimal.class, SimpleType.BIGDECIMAL);
		SIMPLE_TYPES.put(BigInteger.class, SimpleType.BIGINTEGER);
		SIMPLE_TYPES.put(Date.class, SimpleType.DATE);
		SIMPLE_TYPES.put(ObjectName.class, SimpleType.OBJECTNAME);
	}
	
	private final MBeanInfo info;
	private final Object instance;
	
	private final Map<String, PropertyDescriptor> attributesMap = new HashMap<String, PropertyDescriptor>();
	private final Map<String, MethodDescriptor> operationsMap = new HashMap<String, MethodDescriptor>();

	///////////////////////////////////////////////////////////////////////////
	// Constructors.
	
	/**
	 * Creates a new OpenMBean instance and instrospects its child class for attributes
	 * and operations.
	 */
	protected OpenMBean() {
		this.info = init(getClass(), OpenMBean.class, attributesMap, operationsMap);
		this.instance = this;
	}
	
	private OpenMBean(Class<?> beanClass, Class<?> stopClass, Object instance) {
		this.info = init(beanClass, stopClass, attributesMap, operationsMap);
		this.instance = instance;
	}

	///////////////////////////////////////////////////////////////////////////
	// Static OpenMBean creators.

	/**
	 * Creates a new OpenMBean by introspecting and wrapping the <tt>instance</tt> parameter.
	 * This method search for an interface named <tt>instance.getClass().getSimpleName() + "MBean"</tt>
	 * and, if it finds it, uses it for introspection. Otherwise, the class of the <tt>instance</tt>
	 * object is used instead.
	 * 
	 * @param instance an instance of a bean to introspect.
	 * @return a new OpenMBean instance that wraps the instance bean.
	 */
	public static OpenMBean createMBean(Object instance) {
		
		Class<?> beanClass = null;
		Class<?> instanceClass = instance.getClass();
		while (instanceClass != null) {
			String interMBeanName = instanceClass.getSimpleName() + "MBean";
			for (Class<?> inter : instanceClass.getInterfaces()) {
				if (interMBeanName.equals(inter.getSimpleName())) {
					beanClass = inter;
					break;
				}
			}
			if (beanClass != null)
				break;
			instanceClass = instanceClass.getSuperclass();
		}
		
		if (beanClass == null)
			beanClass = instance.getClass();
		
		Class<?> stopClass = null;
		if (!beanClass.isInterface()) {
			stopClass = beanClass.getSuperclass();
			if (stopClass == null)
				stopClass = Object.class;
		}
		
		return new OpenMBean(beanClass, stopClass, instance);
	}
	
	/**
	 * Creates a new OpenMBean by introspecting the <tt>beanClass</tt> parameter and wrapping
	 * the <tt>instance</tt> parameter.
	 * 
	 * @param beanClass a class (or interface) used for introspection.
	 * @param instance the bean to encapsulate.
	 * @return a new OpenMBean instance that wraps the instance bean.
	 * @throws IllegalArgumentException if instance is not an instance of beanClass.
	 */
	public static OpenMBean createMBean(Class<?> beanClass, Object instance) {
		if (!beanClass.isAssignableFrom(instance.getClass()))
			throw new IllegalArgumentException("Instance " + instance + " should be an instance of " + beanClass);

		Class<?> stopClass = null;
		if (!beanClass.isInterface()) {
			stopClass = beanClass.getSuperclass();
			if (stopClass == null)
				stopClass = Object.class;
		}

		return new OpenMBean(beanClass, stopClass, instance);
	}

	///////////////////////////////////////////////////////////////////////////
	// Static initialization methods.
	
	private static MBeanInfo init(
		Class<?> beanClass,
		Class<?> stopClass,
		Map<String, PropertyDescriptor> attributesMap,
		Map<String, MethodDescriptor> operationsMap) {
		
		MBean mb = beanClass.getAnnotation(MBean.class);
		
		String description = null;
		if (mb != null)
			description = mb.description();
		if (description == null)
			description = beanClass.getSimpleName() + " MBean";
		
		List<OpenMBeanAttributeInfo> attributes = new ArrayList<OpenMBeanAttributeInfo>();
		List<OpenMBeanOperationInfo> operations = new ArrayList<OpenMBeanOperationInfo>();
		
		try {
			BeanInfo beanInfo = (
				stopClass == null ?
				Introspector.getBeanInfo(beanClass) :
				Introspector.getBeanInfo(beanClass, stopClass)
			);
			
			Set<Method> attributeMethods = new HashSet<Method>();
			
			// Attributes.
			for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {

				MBeanAttribute mba = null;
				if (property.getReadMethod() != null)
					mba = property.getReadMethod().getAnnotation(MBeanAttribute.class);
				if (mba == null && property.getWriteMethod() != null)
					mba = property.getWriteMethod().getAnnotation(MBeanAttribute.class);
				
				if (mba == null)
					continue;
				
				String name = property.getName();
				
				String desc = mba.description();
				if (desc == null)
					desc = name.substring(0, 1).toUpperCase() + name.substring(1) + " Attribute";
				
				OpenType type = getOpenType(property.getPropertyType());
				
				attributes.add(new OpenMBeanAttributeInfoSupport(
					name,
					desc,
					type,
					property.getReadMethod() != null,
					property.getWriteMethod() != null,
					property.getReadMethod() != null && property.getReadMethod().getName().startsWith("is")
				));
				attributesMap.put(property.getName(), property);
				
				if (property.getReadMethod() != null)
					attributeMethods.add(property.getReadMethod());
				if (property.getWriteMethod() != null)
					attributeMethods.add(property.getWriteMethod());
			}
			
			// Operations
			for (MethodDescriptor method : beanInfo.getMethodDescriptors()) {
				
				if (attributeMethods.contains(method.getMethod()))
					continue;
				
				MBeanOperation mbo = method.getMethod().getAnnotation(MBeanOperation.class);
				
				if (mbo == null)
					continue;
				
				String name = method.getName();

				String desc = mbo.description();
				if (desc == null)
					desc = name.substring(0, 1).toUpperCase() + name.substring(1) + " Operation";
				
				List<OpenMBeanParameterInfo> parameters = new ArrayList<OpenMBeanParameterInfo>();
				Annotation[][] annotations = method.getMethod().getParameterAnnotations();
				
				int i = 0;
				for (Class<?> parameter : method.getMethod().getParameterTypes()) {
					String paramName = getParameterName(annotations, i);
					String paramDesc = getParameterDescription(annotations, i);
					OpenType paramType = getOpenType(parameter);
					
					parameters.add(new OpenMBeanParameterInfoSupport(paramName, paramDesc, paramType));
					
					i++;
				}
				
				OpenType returnedType = getOpenType(method.getMethod().getReturnType());
				
				int impact = MBeanOperationInfo.UNKNOWN;
				if (mbo.impact() != null) {
					switch (mbo.impact()) {
						case ACTION: impact = MBeanOperationInfo.ACTION; break;
						case ACTION_INFO: impact = MBeanOperationInfo.ACTION_INFO; break;
						case INFO: impact = MBeanOperationInfo.INFO; break;
						default: impact = MBeanOperationInfo.UNKNOWN;
					}
				}
				
				operations.add(new OpenMBeanOperationInfoSupport(
					name,
					desc,
					parameters.toArray(new OpenMBeanParameterInfo[parameters.size()]),
					returnedType,
					impact
				));
				
				String[] paramClasses = new String[parameters.size()];
				for (i = 0; i < parameters.size(); i++)
					paramClasses[i] = parameters.get(i).getOpenType().getTypeName().toString();
				String qName = name + Arrays.toString(paramClasses);
				
				operationsMap.put(qName, method);
			}
		}
		catch (Exception e) {
			throw new RuntimeException("Could not introspect MBean class: " + beanClass, e);
		}
		
		return new OpenMBeanInfoSupport(
			beanClass.getName(),
			description,
			attributes.toArray(new OpenMBeanAttributeInfo[attributes.size()]),
			new OpenMBeanConstructorInfo[0],
			operations.toArray(new OpenMBeanOperationInfo[operations.size()]),
			new MBeanNotificationInfo[0]
		);
	}
	
	private static String getParameterName(Annotation[][] paramAnnotations, int index) {
		String name = null;
		if (paramAnnotations != null && paramAnnotations.length > index) {
			for (Annotation annot : paramAnnotations[index]) {
				if (MBeanParameter.class.equals(annot.annotationType())) {
					name = ((MBeanParameter)annot).name();
					break;
				}
			}
		}
		return (name != null ? name : "arg" + (index + 1));
	}
	
	private static String getParameterDescription(Annotation[][] paramAnnotations, int index) {
		String description = null;
		if (paramAnnotations != null && paramAnnotations.length > index) {
			for (Annotation annot : paramAnnotations[index]) {
				if (MBeanParameter.class.equals(annot.annotationType())) {
					description = ((MBeanParameter)annot).description();
					break;
				}
			}
		}
		return (description != null ? description : "Operation Parameter " + (index + 1));
	}
	
	private static OpenType getOpenType(Class<?> clazz) throws OpenDataException {
		
		if (SIMPLE_TYPES.containsKey(clazz))
			return SIMPLE_TYPES.get(clazz);
		
		if (clazz.isArray()) {
			int dimension = 1;
			Class<?> componentType = clazz.getComponentType();
			while (componentType.isArray()) {
				dimension++;
				componentType = componentType.getComponentType();
			}
			return new ArrayType(dimension, getOpenType(componentType));
		}
		
		throw new OpenDataException("Unsupported type: " + clazz);
	}

	///////////////////////////////////////////////////////////////////////////
	// DynamicMBean implementation.

	/**
	 * {@inheritDoc}
	 */
	public synchronized Object getAttribute(String attribute)
		throws AttributeNotFoundException, MBeanException, ReflectionException {
		
		PropertyDescriptor property = attributesMap.get(attribute);
		if (property != null && property.getReadMethod() != null) {
			try {
				return property.getReadMethod().invoke(instance);
			}
			catch (Exception e) {
				throw new ReflectionException(e, "Could not get attribute value: " + attribute);
			}
		}
		throw new AttributeNotFoundException("Attribute " + attribute + " not found");
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized AttributeList getAttributes(String[] names) {
		AttributeList attributes = new AttributeList();
		for (String name : names) {
			try {
				attributes.add(new Attribute(name, getAttribute(name)));
			}
			catch (Exception e) {
				// ignore...
			}
		}
		return attributes;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized MBeanInfo getMBeanInfo() {
		return info;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized Object invoke(String actionName, Object[] params, String[] signature)
		throws MBeanException, ReflectionException {
		
		if (signature == null)
			signature = new String[0];
		
		String qName = actionName + Arrays.toString(signature);
		
		MethodDescriptor method = operationsMap.get(qName);
		if (method == null)
			throw new RuntimeException("Method not found: " + qName);

		try {
			return method.getMethod().invoke(instance, params);
		}
		catch (Exception e) {
			throw new ReflectionException(e, "Could not invoke operation: " + qName);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void setAttribute(Attribute attribute)
		throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		
		PropertyDescriptor property = attributesMap.get(attribute.getName());
		if (property != null && property.getWriteMethod() != null) {
			try {
				property.getWriteMethod().invoke(instance, attribute.getValue());
			}
			catch (Exception e) {
				throw new ReflectionException(e, "Could not set attribute value: " + attribute.getName() + "=" + attribute.getValue());
			}
		}
		else
			throw new AttributeNotFoundException("Attribute " + attribute.getName() + " not found");
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized AttributeList setAttributes(AttributeList attributes) {
		AttributeList returnedAttributes = new AttributeList();
		for (Object a : attributes) {
			Attribute attribute = (Attribute)a;
			try {
				setAttribute(attribute);
				returnedAttributes.add(new Attribute(attribute.getName(), getAttribute(attribute.getName())));
			}
			catch (Exception e) {
				// Ignore...
			}
		}
		return returnedAttributes;
	}
}
