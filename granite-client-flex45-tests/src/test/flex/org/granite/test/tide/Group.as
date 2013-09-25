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
    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
    import flash.utils.IExternalizable;
    
    import org.granite.collections.IPersistentCollection;
    import org.granite.meta;
    import org.granite.tide.IEntityManager;
    import org.granite.tide.IPropertyHolder;
    
    use namespace meta;
    
    
    [Managed]
    [RemoteClass(alias="org.granite.test.tide.Group")]
    public class Group implements IExternalizable {

        [Transient]
        meta var entityManager:IEntityManager = null;

        private var __initialized:Boolean = true;
        private var __detachedState:String = null;

        private var _name:String;
        private var _user:User;

        meta function isInitialized(name:String = null):Boolean {
            if (!name)
                return __initialized;

            var property:* = this[name];
            return (
                (!(property is AbstractEntity) || (property as AbstractEntity).meta::isInitialized()) &&
                (!(property is IPersistentCollection) || (property as IPersistentCollection).isInitialized())
            );
        }
        meta function setInitialized(init:Boolean):void {
        	__initialized = init;
        }

        public function set name(value:String):void {
            _name = value;
        }
        [Id]
        public function get name():String {
            return _name;
        }

        public function set user(value:User):void {
            _user = value;
        }
        public function get user():User {
            return _user;
        }

        public function set uid(value:String):void {
            // noop...
        }
        public function get uid():String {
            if (!_name)
                return "org.granite.tide.test.Group";
            return "org.granite.tide.test.Group:" + String(_name);
        }

        meta function merge(em:IEntityManager, obj:*):void {
            var src:Group = Group(obj);
            __initialized = src.__initialized;
            __detachedState = src.__detachedState;
            if (meta::isInitialized()) {
                em.meta_mergeExternal(src._name, _name, null, this, 'name', function setter(o:*):void{_name = o as String}) as String;
                em.meta_mergeExternal(src._user, _user, null, this, 'user', function setter(o:*):void{_user = o as User}) as User;
            }
            else {
                em.meta_mergeExternal(src._name, _name, null, this, 'name', function setter(o:*):void{_name = o as String}) as String;
            }
        }

        public function readExternal(input:IDataInput):void {
            __initialized = input.readObject() as Boolean;
            __detachedState = input.readObject() as String;
            if (meta::isInitialized()) {
                _name = input.readObject() as String;
                _user = input.readObject() as User;
            }
            else {
                _name = input.readObject() as String;
            }
        }

        public function writeExternal(output:IDataOutput):void {
            output.writeObject(__initialized);
            output.writeObject(__detachedState);
            if (meta::isInitialized()) {
                output.writeObject((_name is IPropertyHolder) ? IPropertyHolder(_name).object : _name);
                output.writeObject((_user is IPropertyHolder) ? IPropertyHolder(_user).object : _user);
            }
            else {
                output.writeObject(_name);
            }
        }
    }
}
