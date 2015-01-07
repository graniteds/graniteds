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
/**
 * www.openamf.org
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package flex.messaging.io;

import java.util.HashMap;

/**
 * Implementation of MM's flashgateway.io.ASObject so that we can use
 * ASTranslator
 *
 * @author Jason Calabrese <mail@jasoncalabrese.com>
 * @author Sean C. Sullivan
 *
 * @version $Revision: 1.11 $, $Date: 2004/02/06 02:48:59 $
 */
public class ASObject extends HashMap<String, Object> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Object type
     */
    private String type;

    public ASObject() {
        super();
    }

    /**
     * Creates ASObject with type
     *
     * @param type
     */
    public ASObject(String type) {
        super();
        this.type = type;
    }

    /**
     * Gets object type
     *
     * @return type 
     * @see #setType(String)
     */
    public String getType() {
        return type;
    }

    /**
     * Sets object type
     *
     * @param type
     *
     * @see #getType()
     *
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key. <br>
     *
     * @param key
     *                The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified
     *            key.
     */
    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(toLowerCase(key));
    }

    /**
     * Returns the value to which the specified key is mapped in this identity
     * hash map, or <tt>null</tt> if the map contains no mapping for this
     * key. A return value of <tt>null</tt> does not <i>necessarily</i>
     * indicate that the map contains no mapping for the key; it is also
     * possible that the map explicitly maps the key to <tt>null</tt>. The
     * <tt>containsKey</tt> method may be used to distinguish these two
     * cases.
     *
     * @param key
     *                the key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or <tt>null</tt>
     *            if the map contains no mapping for this key.
     * @see #put(Object, Object)
     */
    @Override
    public Object get(Object key) {
        return super.get(toLowerCase(key));
    }

    /**
     * Associates the specified value with the specified key in this map. If
     * the map previously contained a mapping for this key, the old value is
     * replaced.
     *
     * @param key
     *                key with which the specified value is to be associated.
     * @param value
     *                value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     *            if there was no mapping for key. A <tt>null</tt> return can
     *            also indicate that the HashMap previously associated <tt>null</tt>
     *            with the specified key.
     */
    @Override
    public Object put(String key, Object value) {
        return super.put((String)toLowerCase(key), value);
    }

    /**
     * Removes the mapping for this key from this map if present.
     *
     * @param key
     *                key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     *            if there was no mapping for key. A <tt>null</tt> return can
     *            also indicate that the map previously associated <tt>null</tt>
     *            with the specified key.
     */
    @Override
    public Object remove(Object key) {
        return super.remove(toLowerCase(key));
    }

    /**
     * Gets lower case object if object was instance of String
     *
     * @param key
     * @return lower case
     */
    private Object toLowerCase(Object key) {
        /*if (key != null
            && key instanceof String
            && amfSerializerConfig.forceLowerCaseKeys()) {
            key = ((String) key).toLowerCase();
        }*/
        return key;
    }

    /**
     * @return this method may return null
     *
     * @see #setType(String)
     * @see #getType()
     *
     */
    public Object instantiate() {
        Object ret;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class<?> clazz = loader.loadClass(type);
            ret = clazz.newInstance();
        } catch (Exception e) {
            ret = null;
        }
        return ret;
    }

    @Override
    public String toString() {
        return "ASObject[type=" + getType() + "," + super.toString() + "]";
    }
}
