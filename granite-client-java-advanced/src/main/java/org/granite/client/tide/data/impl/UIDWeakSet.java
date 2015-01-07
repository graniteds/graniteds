/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.tide.data.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import org.granite.client.tide.data.spi.DataManager;

/**
 *  Implementation of HashSet that holds weak references to UID entities 
 *  
 *  @author William DRAI
 */
public class UIDWeakSet {
    
	private final DataManager dataManager;
    private final WeakHashMap<Object, Object>[] table;
    
    
    public UIDWeakSet(DataManager dataManager) {
        this(dataManager, 64);
    }
    
    @SuppressWarnings("unchecked")
    public UIDWeakSet(DataManager dataManager, int capacity) {
    	this.dataManager = dataManager;
        table = new WeakHashMap[capacity];  
    }
    
    public void clear() {
        for (int i = 0; i < table.length; i++)
            table[i] = null;
    }
    
    public Object put(Object uidObject) {
        int h = hash(dataManager.getCacheKey(uidObject));
        
        WeakHashMap<Object, Object> dic = table[h];
        if (dic == null) {
            dic = new WeakHashMap<Object, Object>();
            table[h] = dic;
        }
        
        Object old = null;
        for (Object o : dic.keySet()) {
            if (o == uidObject)
                return o;
            
            if (dataManager.getUid(o) == dataManager.getUid(uidObject) && o.getClass().getName().equals(uidObject.getClass().getName())) {
                old = o;
                dic.remove(o);
                break;
            }
        }
        
        dic.put(uidObject, null);
        
        return old;
    }
    
    public Object get(String cacheKey) {
        int h = hash(cacheKey);
        
        Object uidObject = null;
        
        WeakHashMap<Object, Object> dic = table[h];
        if (dic != null) {
            for (Object o : dic.keySet()) {
                if (dataManager.getCacheKey(o).equals(cacheKey)) {
                    uidObject = o;
                    break;
                }
            }
        }
        
        return uidObject;
    }

    public static interface Matcher {
        
        public boolean match(Object o);
    }
    
    public Object find(Matcher matcher) {
        for (int i = 0; i < table.length; i++) {
            WeakHashMap<Object, Object> dic = table[i];
            if (dic != null) {
                for (Object o : dic.keySet()) {
                    if (matcher.match(o))
                        return o;
                }
            }
        }
        return null;
    }

    public static interface Operation {
        
        public void apply(Object o);
    }
    
    public void apply(Operation operation) {
        for (int i = 0; i < table.length; i++) {
            WeakHashMap<Object, Object> dic = table[i];
            if (dic != null) {
                for (Object o : dic.keySet())
                    operation.apply(o);
            }
        }
    }
    
    public Object remove(String cacheKey) {
        int h = hash(cacheKey);
        
        Object uidObject = null;
        
        WeakHashMap<Object, Object> dic = table[h];
        if (dic != null) {
            for (Object o : dic.keySet()) {
                if (dataManager.getCacheKey(o).equals(cacheKey)) {
                    uidObject = o;
                    dic.remove(o);
                    break;
                }
            }
        }
        
        return uidObject;
    }
    
    public int size() {
        int size = 0;
        
        for (int i = 0; i < table.length; i++) {
            WeakHashMap<Object, Object> dic = table[i];
            if (dic != null)
                size += dic.size();
        }
        
        return size;
    }
    
    public List<Object> data() {
        List<Object> d = new ArrayList<Object>();
        
        for (int i = 0; i < table.length; i++) {
            WeakHashMap<Object, Object> dic = table[i];
            if (dic != null)
                d.addAll(dic.keySet());
        }
        return d;
    }
    
    private int hash(String uid) {
        int h = 0;
        int max = uid.length();
        for (int i = 0; i < max; i++)
            h = (31 * h) + uid.charAt(i);
        return (Math.abs(h) % table.length);
    }
}