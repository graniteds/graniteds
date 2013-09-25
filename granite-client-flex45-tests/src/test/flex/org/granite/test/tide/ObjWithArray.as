/*
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
package org.granite.test.tide
{
    import flash.events.TimerEvent;
    import flash.utils.Timer;
    import flash.utils.IExternalizable;
    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
    
    import org.flexunit.Assert;
    
    import org.granite.meta;
    import org.granite.collections.IPersistentCollection;
    import org.granite.tide.IEntity;
    import org.granite.tide.IEntityManager;
    import org.granite.tide.IPropertyHolder;
    import org.granite.tide.seam.Context;
    import org.granite.tide.seam.Seam;
    import org.granite.tide.seam.SeamOperation;
    
    use namespace meta;
    
    
    [Managed]
    public class ObjWithArray implements IExternalizable {
    	
		[Transient]
        meta var entityManager:IEntityManager = null;

        private var __laziness:String = null;
        
        private var _name:String;
        private var _array:Array;

        meta function isInitialized(name:String = null):Boolean {
            if (!name)
                return __laziness === null;

            var property:* = this[name];
            return (
                (!(property is User) || (property as User).meta::isInitialized()) &&
                (!(property is IPersistentCollection) || (property as IPersistentCollection).isInitialized())
            );
        }

//        [Transient]
//        public function meta_getEntityManager():IEntityManager {
//            return _em;
//        }
//        public function meta_setEntityManager(em:IEntityManager):void {
//            _em = em;
//        }

        public function set name(value:String):void {
            _name = value;
        }
        public function get name():String {
            return _name;
        }

        public function set array(value:Array):void {
            _array = value;
        }
        public function get array():Array {
            return _array;
        }

        public function set uid(value:String):void {
            // noop...
        }
        public function get uid():String {
            if (!_name)
                return "org.granite.test.tide.ObjWithArray";
            return "org.granite.test.tide.ObjWithArray:" + String(_name);
        }

        public function meta_merge(em:IEntityManager, obj:*):void {
            var src:ObjWithArray = ObjWithArray(obj);
            __laziness = src.__laziness;
            if (meta::isInitialized()) {
                em.meta_mergeExternal(src._array, _array, null, this, 'array', function setter(o:*):void{_array = o as Array}) as Array;
                em.meta_mergeExternal(src._name, _name, null, this, 'name', function setter(o:*):void{_name = o as String}) as String;
            }
            else {
                em.meta_mergeExternal(src._name, _name, null, this, 'name', function setter(o:*):void{_name = o as String}) as String;
            }
        }

        public function readExternal(input:IDataInput):void {
            __laziness = input.readObject() as String;
            if (meta::isInitialized()) {
            	_array = input.readObject() as Array;
                _name = input.readObject() as String;
            }
            else {
                _name = input.readObject() as String;
            }
        }

        public function writeExternal(output:IDataOutput):void {
            output.writeObject(__laziness);
            if (meta::isInitialized()) {
                output.writeObject((_array is IPropertyHolder) ? IPropertyHolder(_array).object : _array);
                output.writeObject((_name is IPropertyHolder) ? IPropertyHolder(_name).object : _name);
            }
            else {
                output.writeObject(_name);
            }
        }
    }
}
