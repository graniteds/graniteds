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
package org.granite.hibernate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import org.granite.collections.BasicMap;
import org.granite.config.GraniteConfig;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.util.ClassGetter;
import org.granite.messaging.amf.io.util.MethodProperty;
import org.granite.messaging.amf.io.util.Property;
import org.granite.messaging.amf.io.util.externalizer.DefaultExternalizer;
import org.granite.messaging.annotations.Include;
import org.granite.messaging.persistence.AbstractExternalizablePersistentCollection;
import org.granite.messaging.persistence.ExternalizablePersistentBag;
import org.granite.messaging.persistence.ExternalizablePersistentList;
import org.granite.messaging.persistence.ExternalizablePersistentMap;
import org.granite.messaging.persistence.ExternalizablePersistentSet;
import org.granite.util.StringUtil;
import org.granite.util.TypeUtil;
import org.granite.util.XMap;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.collection.PersistentBag;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.collection.PersistentList;
import org.hibernate.collection.PersistentMap;
import org.hibernate.collection.PersistentSet;
import org.hibernate.collection.PersistentSortedMap;
import org.hibernate.collection.PersistentSortedSet;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

/**
 * @author Franck WOLFF
 */
public class HibernateExternalizer extends DefaultExternalizer {

	private static final Logger log = Logger.getLogger(HibernateExternalizer.class);
	
    private final ConcurrentHashMap<String, ProxyFactory> proxyFactories = new ConcurrentHashMap<String, ProxyFactory>();
    
    static enum SerializeMetadata {
    	YES,
    	NO,
    	LAZY
    }
    
    private SerializeMetadata serializeMetadata = SerializeMetadata.NO;
    

    /**
     * Configure this externalizer with the values supplied in granite-config.xml.
     * 
     * <p>The only supported configuration option is 'hibernate-collection-metadata' with
     * values in ['no' (default), 'yes' and 'lazy']. By default, collection metadata (key,
     * role and snapshot) aren't serialized. If the value of the 'hibernate-collection-metadata'
     * node is 'yes', metadata will be always serialized, while the 'lazy' value tells the
     * externalizer to serialiaze metadata for uninitialized collections only.
     * 
     * <p>Configuration example (granite-config.xml):
     * <pre>
     * &lt;granite-config scan="true"&gt;
     *   &lt;externalizers&gt;
     *     &lt;configuration&gt;
     *       &lt;hibernate-collection-metadata&gt;lazy&lt;/hibernate-collection-metadata&gt;
     *     &lt;/configuration&gt;
     *   &lt;/externalizers&gt;
     * &lt;/granite-config&gt;
     * </pre>
     * 
     * @param properties an XMap instance that contains the configuration node.
     */
    @Override
	public void configure(XMap properties) {
    	super.configure(properties);
    	
    	if (properties != null) {
	    	String collectionmetadata = properties.get("hibernate-collection-metadata");
	    	if (collectionmetadata != null) {
	    		if ("no".equalsIgnoreCase(collectionmetadata))
	    			serializeMetadata = SerializeMetadata.NO;
	    		else if ("yes".equalsIgnoreCase(collectionmetadata))
	    			serializeMetadata = SerializeMetadata.YES;
	    		else if ("lazy".equalsIgnoreCase(collectionmetadata))
	    			serializeMetadata = SerializeMetadata.LAZY;
	    		else
	    			throw new RuntimeException("Illegal value for the 'hibernate-collection-metadata' option: " + collectionmetadata);
	    	}
    	}
	}

	@Override
    public Object newInstance(String type, ObjectInput in)
        throws IOException, ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException {

        // If type is not an entity (@Embeddable for example), we don't read initialized/detachedState
        // and we fall back to DefaultExternalizer behavior.
        Class<?> clazz = TypeUtil.forName(type);
        if (!isRegularEntity(clazz))
            return super.newInstance(type, in);
        
        // Read initialized flag.
        boolean initialized = ((Boolean)in.readObject()).booleanValue();

        // Read detachedState.
        String detachedState = (String)in.readObject();
        
        // New or initialized entity.
        if (initialized)
            return super.newInstance(type, in);

        // Actual proxy instantiation is deferred in order to keep consistent order in
        // stored objects list (see AMF3Deserializer).
        return newProxyInstantiator(proxyFactories, detachedState);
    }
	
	protected Object newProxyInstantiator(ConcurrentHashMap<String, ProxyFactory> proxyFactories, String detachedState) {
        return new HibernateProxyInstantiator(proxyFactories, detachedState);
	}

    @Override
    public void readExternal(Object o, ObjectInput in) throws IOException, ClassNotFoundException, IllegalAccessException {

        // Skip unserialized fields for proxies (only read id).
        if (o instanceof HibernateProxyInstantiator) {
        	log.debug("Reading Hibernate Proxy...");
            ((HibernateProxyInstantiator)o).readId(in);
        }
        // @Embeddable or others...
        else if (!isRegularEntity(o.getClass()) && !isEmbeddable(o.getClass())) {
        	log.debug("Delegating non regular entity reading to DefaultExternalizer...");
            super.readExternal(o, in);
        }
        // Regular @Entity or @MappedSuperclass
        else {
            GraniteConfig config = GraniteContext.getCurrentInstance().getGraniteConfig();

            Converters converters = config.getConverters();
            ClassGetter classGetter = config.getClassGetter();
            Class<?> oClass = classGetter.getClass(o);
            ParameterizedType[] declaringTypes = TypeUtil.getDeclaringTypes(oClass);

            List<Property> fields = findOrderedFields(oClass, false);
            log.debug("Reading entity %s with fields %s", oClass.getName(), fields);
            for (Property field : fields) {
                Object value = in.readObject();
                if (!(field instanceof MethodProperty && field.isAnnotationPresent(Include.class, true))) {
                    
                	if (value instanceof AbstractExternalizablePersistentCollection)
                		value = newHibernateCollection((AbstractExternalizablePersistentCollection)value, field);
                    else if (!(value instanceof HibernateProxy)) {
                    	Type targetType = TypeUtil.resolveTypeVariable(field.getType(), field.getDeclaringClass(), declaringTypes);
                    	value = converters.convert(value, targetType);
                    }

                	field.setProperty(o, value, false);
                }
            }
        }
    }
    
	protected PersistentCollection newHibernateCollection(AbstractExternalizablePersistentCollection value, Property field) {
    	final Type target = field.getType();
    	final boolean initialized = value.isInitialized();
    	final String metadata = value.getMetadata();
		final boolean dirty = value.isDirty();
    	final boolean sorted = (
    		SortedSet.class.isAssignableFrom(TypeUtil.classOfType(target)) ||
    		SortedMap.class.isAssignableFrom(TypeUtil.classOfType(target))
    	);
    	
    	Comparator<?> comparator = null;
    	if (sorted && field.isAnnotationPresent(Sort.class)) {
    		Sort sort = field.getAnnotation(Sort.class);
    		if (sort.type() == SortType.COMPARATOR) {
    			try {
    				comparator = TypeUtil.newInstance(sort.comparator(), Comparator.class);
    			} catch (Exception e) {
    				throw new RuntimeException("Could not create instance of Comparator: " + sort.comparator());
    			}
    		}
    	}
    	
    	PersistentCollection coll = null;
		if (value instanceof ExternalizablePersistentSet) {
    		if (initialized) {
    			Set<?> set = ((ExternalizablePersistentSet)value).getContentAsSet(target, comparator);
    			coll = (sorted ? new PersistentSortedSet(null, (SortedSet<?>)set) : new PersistentSet(null, set));
            }
    		else
                coll = (sorted ? new PersistentSortedSet() : new PersistentSet());
    	}
		else if (value instanceof ExternalizablePersistentBag) {
	        if (initialized) {
	            List<?> bag = ((ExternalizablePersistentBag)value).getContentAsList(target);
                coll = new PersistentBag(null, bag);
	        }
	        else
	            coll = new PersistentBag();
		}
		else if (value instanceof ExternalizablePersistentList) {
	        if (initialized) {
	            List<?> list = ((ExternalizablePersistentList)value).getContentAsList(target);
                coll = new PersistentList(null, list);
	        }
	        else
	            coll = new PersistentList();
		}
		else if (value instanceof ExternalizablePersistentMap) {
	    	if (initialized) {
	            Map<?, ?> map = ((ExternalizablePersistentMap)value).getContentAsMap(target, comparator);
	            coll = (sorted ? new PersistentSortedMap(null, (SortedMap<?, ?>)map) : new PersistentMap(null, map));
	        }
	    	else
	            coll = (sorted ? new PersistentSortedMap() : new PersistentMap());
		}
		else
			throw new RuntimeException("Illegal externalizable persitent class: " + value);
		
		if (metadata != null && serializeMetadata != SerializeMetadata.NO && (serializeMetadata == SerializeMetadata.YES || !initialized)) {
    		String[] toks = metadata.split(":", 3);
    		if (toks.length != 3)
    			throw new RuntimeException("Invalid collection metadata: " + metadata);
    		Serializable key = deserializeSerializable(StringUtil.hexStringToBytes(toks[0]));
    		Serializable snapshot = deserializeSerializable(StringUtil.hexStringToBytes(toks[1]));
    		String role = toks[2];
            coll.setSnapshot(key, role, snapshot);
		}
		
		if (initialized && dirty)
			coll.dirty();
    	
    	return coll;
    }

    @Override
    public void writeExternal(Object o, ObjectOutput out) throws IOException, IllegalAccessException {

        ClassGetter classGetter = GraniteContext.getCurrentInstance().getGraniteConfig().getClassGetter();
        Class<?> oClass = classGetter.getClass(o);

        String detachedState = null;
        
        if (o instanceof HibernateProxy) {        	
            HibernateProxy proxy = (HibernateProxy)o;
            detachedState = getProxyDetachedState(proxy);

            // Only write initialized flag & detachedState & entity id if proxy is uninitialized.
            if (proxy.getHibernateLazyInitializer().isUninitialized()) {
            	Serializable id = proxy.getHibernateLazyInitializer().getIdentifier();
            	log.debug("Writing uninitialized HibernateProxy %s with id %s", detachedState, id);
            	
            	// Write initialized flag.
            	out.writeObject(Boolean.FALSE);
            	// Write detachedState.
            	out.writeObject(detachedState);
            	// Write entity id.
                out.writeObject(id);
                return;
            }

            // Proxy is initialized, get the underlying persistent object.
        	log.debug("Writing initialized HibernateProxy...");
            o = proxy.getHibernateLazyInitializer().getImplementation();
        }

        if (!isRegularEntity(o.getClass()) && !isEmbeddable(o.getClass())) { // @Embeddable or others...
        	log.debug("Delegating non regular entity writing to DefaultExternalizer...");
            super.writeExternal(o, out);
        }
        else {
        	if (isRegularEntity(o.getClass())) {
	            // Write initialized flag.
	            out.writeObject(Boolean.TRUE);
	            // Write detachedState.
	            out.writeObject(detachedState);
        	}
	        
            // Externalize entity fields.
            List<Property> fields = findOrderedFields(oClass, false);
            log.debug("Writing entity %s with fields %s", o.getClass().getName(), fields);
            for (Property field : fields) {
                Object value = field.getProperty(o);
                
                // Persistent collections.
                if (value instanceof PersistentCollection)
                	value = newExternalizableCollection((PersistentCollection)value);
                // Transient maps.
                else if (value instanceof Map<?, ?>)
                	value = BasicMap.newInstance((Map<?, ?>)value);

                if (isValueIgnored(value))
                	out.writeObject(null);
                else
                	out.writeObject(value);
            }
        }
    }
    
    protected AbstractExternalizablePersistentCollection newExternalizableCollection(PersistentCollection value) {
    	final boolean initialized = Hibernate.isInitialized(value);
    	final boolean dirty = value.isDirty();
    	
    	AbstractExternalizablePersistentCollection coll = null;
        
    	if (value instanceof PersistentSet)
            coll = new ExternalizablePersistentSet(initialized ? (Set<?>)value : null, initialized, dirty);
        else if (value instanceof PersistentList)
            coll = new ExternalizablePersistentList(initialized ? (List<?>)value : null, initialized, dirty);
        else if (value instanceof PersistentBag)
            coll = new ExternalizablePersistentBag(initialized ? (List<?>)value : null, initialized, dirty);
        else if (value instanceof PersistentMap)
            coll = new ExternalizablePersistentMap(initialized ? (Map<?, ?>)value : null, initialized, dirty);
        else
            throw new UnsupportedOperationException("Unsupported Hibernate collection type: " + value);

    	if (serializeMetadata != SerializeMetadata.NO && (serializeMetadata == SerializeMetadata.YES || !initialized) && value.getRole() != null) {
    		char[] hexKey = StringUtil.bytesToHexChars(serializeSerializable(value.getKey()));
    		char[] hexSnapshot = StringUtil.bytesToHexChars(serializeSerializable(value.getStoredSnapshot()));
    		String metadata = new StringBuilder(hexKey.length + 1 + hexSnapshot.length + 1 + value.getRole().length())
				.append(hexKey).append(':')
    			.append(hexSnapshot).append(':')
    			.append(value.getRole())
    			.toString();
    		coll.setMetadata(metadata);
    	}
        
        return coll;
    }

    @Override
    public int accept(Class<?> clazz) {
        return (
            clazz.isAnnotationPresent(Entity.class) ||
            clazz.isAnnotationPresent(MappedSuperclass.class) ||
            clazz.isAnnotationPresent(Embeddable.class)
        ) ? 1 : -1;
    }

    protected String getProxyDetachedState(HibernateProxy proxy) {
        LazyInitializer initializer = proxy.getHibernateLazyInitializer();

        StringBuilder sb = new StringBuilder();

        sb.append(initializer.getClass().getName())
          .append(':');
        if (initializer.getPersistentClass() != null)
            sb.append(initializer.getPersistentClass().getName());
        sb.append(':');
        if (initializer.getEntityName() != null)
            sb.append(initializer.getEntityName());

        return sb.toString();
    }

    protected boolean isRegularEntity(Class<?> clazz) {
        return clazz.isAnnotationPresent(Entity.class) || clazz.isAnnotationPresent(MappedSuperclass.class);
    }
    
    protected boolean isEmbeddable(Class<?> clazz) {
        return clazz.isAnnotationPresent(Embeddable.class);
    }
    
    protected byte[] serializeSerializable(Serializable o) {
    	if (o == null)
    		return BYTES_0;
    	try {
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        ObjectOutputStream oos = new ObjectOutputStream(baos);
	        oos.writeObject(o);
	        return baos.toByteArray();
    	} catch (Exception e) {
    		throw new RuntimeException("Could not serialize: " + o);
    	}
    }
    
    protected Serializable deserializeSerializable(byte[] data) {
    	if (data.length == 0)
    		return null;
    	try {
	    	ByteArrayInputStream baos = new ByteArrayInputStream(data);
	        ObjectInputStream oos = new ObjectInputStream(baos);
	        return (Serializable)oos.readObject();
    	} catch (Exception e) {
    		throw new RuntimeException("Could not deserialize: " + data);
    	}
    }
}
