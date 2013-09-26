/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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

package org.granite.generator.as3;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import org.granite.generator.as3.reflect.JavaBean;
import org.granite.generator.as3.reflect.JavaEntityBean;
import org.granite.generator.as3.reflect.JavaFieldProperty;
import org.granite.generator.as3.reflect.JavaProperty;
import org.granite.generator.as3.reflect.JavaType;
import org.granite.generator.as3.reflect.JavaTypeFactory;


public class DefaultEntityFactory implements EntityFactory {

	@Override
	public boolean isEntity(Class<?> clazz) {
		return clazz.isAnnotationPresent(Entity.class) ||
    		clazz.isAnnotationPresent(MappedSuperclass.class) ||
    		clazz.isAnnotationPresent(PersistenceCapable.class);
	}
	
	@Override
	public JavaType newBean(JavaTypeFactory provider, Class<?> type, URL url) {
		return new JavaBean(provider, type, url);
	}
	
	@Override
	public JavaType newEntity(JavaTypeFactory provider, Class<?> type, URL url) {
		return new JavaEntityBean(provider, type, url);
	}
	
	@Override
	public boolean isId(JavaFieldProperty fieldProperty) {
        Field field = fieldProperty.getMember();
        Method getter = (fieldProperty.getReadMethod() != null ? fieldProperty.getReadMethod().getMember() : null);
        Method setter = (fieldProperty.getWriteMethod() != null ? fieldProperty.getWriteMethod().getMember() : null);

        if (field.isAnnotationPresent(Persistent.class)) {
        	Annotation annotation = field.getAnnotation(Persistent.class);
        	if (annotation instanceof Persistent) {
        		Persistent persistAnnotation = (Persistent)annotation;
        		String pk = persistAnnotation.primaryKey();
        		if (pk != null && pk.toLowerCase().equals("true"))
        			return true;
        	}
        	if (field.isAnnotationPresent(PrimaryKey.class))
        		return true;
        }
        
        return
            (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class)) ||
            (getter != null && (getter.isAnnotationPresent(Id.class) || getter.isAnnotationPresent(EmbeddedId.class))) ||
            (setter != null && (setter.isAnnotationPresent(Id.class) || setter.isAnnotationPresent(EmbeddedId.class)));
	}
	
	@Override
	public boolean isVersion(JavaProperty property) {
		if (property.isAnnotationPresent(Version.class))
			return true;
		
	    if (property.getType().isAnnotationPresent(javax.jdo.annotations.Version.class)) {
	    	// Get JDO version field using specific DataNucleus extension
	    	javax.jdo.annotations.Version versionAnnotation = property.getType().getAnnotation(javax.jdo.annotations.Version.class);
	    	javax.jdo.annotations.Extension[] extensions = versionAnnotation.extensions();
	    	for (javax.jdo.annotations.Extension extension : extensions) {
	    		if ("datanucleus".equals(extension.vendorName()) && "field-name".equals(extension.key()))
	    			return property.getName().equals(extension.value());
	    	}
	    }
	    return false;
	}
	
	@Override
	public boolean isLazy(JavaProperty property) {
		if (property.isAnnotationPresent(ManyToOne.class)) {
			ManyToOne manyToOne = property.getAnnotation(ManyToOne.class);
			return manyToOne.fetch().equals(FetchType.LAZY);
		}
		else if (property.isAnnotationPresent(OneToOne.class)) {
			OneToOne oneToOne = property.getAnnotation(OneToOne.class);
			return oneToOne.fetch().equals(FetchType.LAZY);
		}
		else if (property.isAnnotationPresent(OneToMany.class)) {
			OneToMany oneToMany = property.getAnnotation(OneToMany.class);
			return oneToMany.fetch().equals(FetchType.LAZY);
		}
		else if (property.isAnnotationPresent(ManyToMany.class)) {
			ManyToMany manyToMany = property.getAnnotation(ManyToMany.class);
			return manyToMany.fetch().equals(FetchType.LAZY);
		}
		return false;
	}
}
