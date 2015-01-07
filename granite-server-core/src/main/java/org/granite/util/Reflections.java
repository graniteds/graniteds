/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Reflections class copied from JBoss Seam.
 * www.seamframework.org 
 * jboss-seam-2.0.0.GA
 * Author unattributed
 *
 */
public class Reflections
{
   
   public static Object invoke(Method method, Object target, Object... args) throws Exception
   {
      try
      {
         return method.invoke( target, args );
      }
      catch (IllegalArgumentException iae)
      {
         String message = "Could not invoke method by reflection: " + toString(method);
         if (args!=null && args.length>0) 
         {
            message += " with parameters: (" + Strings.toClassNameString(", ", args) + ')';
         }
         message += " on: " + target.getClass().getName();
         throw new IllegalArgumentException(message, iae);
      }
      catch (InvocationTargetException ite)
      {
         if ( ite.getCause() instanceof Exception )
         {
            throw (Exception) ite.getCause();
         }
         
         throw ite;
      }
   }
   
   public static Object get(Field field, Object target) throws Exception
   {
      try
      {
         return field.get(target);
      }
      catch (IllegalArgumentException iae)
      {
         String message = "Could not get field value by reflection: " + toString(field) + 
            " on: " + target.getClass().getName();
         throw new IllegalArgumentException(message, iae);
      }
   }
   
   public static void set(Field field, Object target, Object value) throws Exception
   {
      try
      {
         field.set(target, value);
      }
      catch (IllegalArgumentException iae)
      {
         // target may be null if field is static so use field.getDeclaringClass() instead
         String message = "Could not set field value by reflection: " + toString(field) +
            " on: " + field.getDeclaringClass().getName();
         if (value==null)
         {
            message += " with null value";
         }
         else
         {
            message += " with value: " + value.getClass();
         }
         throw new IllegalArgumentException(message, iae);
      }
   }
   
   public static Object getAndWrap(Field field, Object target)
   {
      try
      {
         return get(field, target);
      }
      catch (Exception e)
      {
         if (e instanceof RuntimeException)
         {
            throw (RuntimeException) e;
         }
         
         throw new IllegalArgumentException("exception setting: " + field.getName(), e);
      }
   }
   
   public static void setAndWrap(Field field, Object target, Object value)
   {
      try
      {
         set(field, target, value);
      }
      catch (Exception e)
      {
         if (e instanceof RuntimeException)
         {
            throw (RuntimeException) e;
         }
         
         throw new IllegalArgumentException("exception setting: " + field.getName(), e);
      }
   }
   
   public static Object invokeAndWrap(Method method, Object target, Object... args)
   {
      try
      {
         return invoke(method, target, args);
      }
      catch (Exception e)
      {
         if (e instanceof RuntimeException)
         {
            throw (RuntimeException) e;
         }
         
         throw new RuntimeException("exception invoking: " + method.getName(), e);
      }
   }
   
   private static String toString(Method method)
   {
      return Strings.unqualify( method.getDeclaringClass().getName() ) + 
            '.' + 
            method.getName() + 
            '(' + 
            Strings.toString( ", ", method.getParameterTypes() ) + 
            ')';
   }
   
   private static String toString(Field field)
   {
      return Strings.unqualify( field.getDeclaringClass().getName() ) + 
            '.' + 
            field.getName();
   }
   
   public static Class<?> classForName(String name) throws ClassNotFoundException
   {
      try 
      {
         return Thread.currentThread().getContextClassLoader().loadClass(name);
      }
      catch (Exception e)
      {
         return Class.forName(name);
      }
   }
   
   /**
    * Return's true if the class can be loaded using Reflections.classForName()
    */
   public static boolean isClassAvailable(String name)
   {
      try 
      {
         classForName(name);
      }
      catch (ClassNotFoundException e) {
         return false;
      }
      return true;
   }

   public static Class<?> getCollectionElementType(Type collectionType)
   {
      if ( !(collectionType instanceof ParameterizedType) )
      {
         throw new IllegalArgumentException("collection type not parameterized");
      }
      Type[] typeArguments = ( (ParameterizedType) collectionType ).getActualTypeArguments();
      if (typeArguments.length==0)
      {
         throw new IllegalArgumentException("no type arguments for collection type");
      }
      Type typeArgument = typeArguments.length==1 ? typeArguments[0] : typeArguments[1]; //handle Maps
      if ( !(typeArgument instanceof Class<?>) )
      {
         throw new IllegalArgumentException("type argument not a class");
      }
      return (Class<?>) typeArgument;
   }
   
   public static Class<?> getMapKeyType(Type collectionType)
   {
      if ( !(collectionType instanceof ParameterizedType) )
      {
         throw new IllegalArgumentException("collection type not parameterized");
      }
      Type[] typeArguments = ( (ParameterizedType) collectionType ).getActualTypeArguments();
      if (typeArguments.length==0)
      {
         throw new IllegalArgumentException("no type arguments for collection type");
      }
      Type typeArgument = typeArguments[0];
      if ( !(typeArgument instanceof Class<?>) )
      {
         throw new IllegalArgumentException("type argument not a class");
      }
      return (Class<?>) typeArgument;
   }
   
   public static Method getSetterMethod(Class<?> clazz, String name)
   {
      Method[] methods = clazz.getMethods();
      for (Method method: methods)
      {
         String methodName = method.getName();
         if ( methodName.startsWith("set") && method.getParameterTypes().length==1 )
         {
            if ( Introspector.decapitalize( methodName.substring(3) ).equals(name) )
            {
               return method;
            }
         }
      }
      throw new IllegalArgumentException("no such setter method: " + clazz.getName() + '.' + name);
   }
   
   public static Method getGetterMethod(Class<?> clazz, String name) {
       Method[] methods = clazz.getMethods();
       for (Method method : methods) {
           String methodName = method.getName();
           if (methodName.matches("^(get|is).*") && method.getParameterTypes().length == 0) {
               int idx = methodName.startsWith("get") ? 3 : 2;
               if (Introspector.decapitalize(methodName.substring(idx)).equals(name))
                   return method;
           }
       }
       throw new IllegalArgumentException("no such getter method: " + clazz.getName() + '.' + name);
    }
   
   /**
    * Get all the getter methods annotated with the given annotation. Returns an empty list if
    * none are found
    */
   public static List<Method> getGetterMethods(Class<?> clazz, Class<? extends Annotation> annotation) 
   {
      List<Method> methods = new ArrayList<Method>();
      for (Method method : clazz.getMethods())
      {
         if (method.isAnnotationPresent(annotation))
         {
            methods.add(method);
         }
      }
      return methods;
   }
   
   public static Field getField(Class<?> clazz, String name)
   {
      for ( Class<?> superClass = clazz; superClass!=Object.class; superClass=superClass.getSuperclass() )
      {
         try
         {
            return superClass.getDeclaredField(name);
         }
         catch (NoSuchFieldException nsfe) {}
      }
      throw new IllegalArgumentException("no such field: " + clazz.getName() + '.' + name);
   }
   
   /**
    * Get all the fields which are annotated with the given annotation. Returns an empty list
    * if none are found
    */
   public static List<Field> getFields(Class<?> clazz, Class<? extends Annotation> annotation)
   {
      List<Field> fields = new ArrayList<Field>();
      for (Class<?> superClass = clazz; superClass!=Object.class; superClass=superClass.getSuperclass())
      {
         for (Field field : superClass.getDeclaredFields())
         {
            if (field.isAnnotationPresent(annotation))
            {
               fields.add(field);
            }
         }
      }
      return fields;
   }

   public static Method getMethod(Annotation annotation, String name)
   {
      try
      {
         return annotation.annotationType().getMethod(name);
      }
      catch (NoSuchMethodException nsme)
      {
         return null;
      }
   }
   
   public static boolean isInstanceOf(Class<?> clazz, String name)
   {
      if (name == null)
      {
         throw new IllegalArgumentException("name cannot be null");
      }
      for (Class<?> c = clazz; c != Object.class; c = c.getSuperclass())
      {
         if (name.equals(c.getName()))
         {
            return true;
         }
      }
      for (Class<?> c : clazz.getInterfaces())
      {
         if (name.equals(c.getName()))
         {
            return true;
         }
      }
      return false;
   }

	
   public static Object get(Object object, String fieldName) {
	   Field field = null;
	   for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
		   try {
			   field = superClass.getDeclaredField(fieldName);
				break;
		   }
		   catch (NoSuchFieldException nsfe) {
		   }
	   }
	   if (field == null)
		   throw new RuntimeException("Could not find field " + fieldName + " of " + object);
	   field.setAccessible(true);
	   try {
		   return field.get(object);
	   }
	   catch (Exception e) {
		   throw new RuntimeException("Could not get field " + fieldName + " of " + object, e);
	   }
   }
   
   @SuppressWarnings("unchecked")
   public static <T> T get(Object object, String fieldName, Class<T> valueClass) {
	   Field field = null;
	   for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
		   try {
			   field = superClass.getDeclaredField(fieldName);
			   break;
		   }
		   catch (NoSuchFieldException nsfe) {
		   }
	   }
	   if (field == null)
		   throw new RuntimeException("Could not find field " + fieldName + " of " + object);
	   field.setAccessible(true);
	   try {
		   return (T)field.get(object);
	   }
	   catch (Exception e) {
		   throw new RuntimeException("Could not get field " + fieldName + " of " + object, e);
	   }
   }
}
