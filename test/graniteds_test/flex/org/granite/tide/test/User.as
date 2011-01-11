package org.granite.tide.test
{
    import flash.events.TimerEvent;
    import flash.utils.Timer;
    import flash.utils.IExternalizable;
    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
    
    import flexunit.framework.TestCase;
    
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
    [RemoteClass(alias="org.granite.tide.test.User")]
    public class User implements IExternalizable {

        [Transient]
        meta var entityManager:IEntityManager = null;

        private var __initialized:Boolean = true;
        private var __detachedState:String = null;

        private var _name:String;
        private var _password:String;
        private var _username:String;

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
        public function get name():String {
            return _name;
        }

        public function set password(value:String):void {
            _password = value;
        }
        public function get password():String {
            return _password;
        }

        public function set username(value:String):void {
            _username = value;
        }
        [Id]
        public function get username():String {
            return _username;
        }

        public function set uid(value:String):void {
            // noop...
        }
        public function get uid():String {
            if (!_username)
                return "org.granite.tide.test.User";
            return "org.granite.tide.test.User:" + String(_username);
        }

        meta function merge(em:IEntityManager, obj:*):void {
            var src:User = User(obj);
            __initialized = src.__initialized;
            __detachedState = src.__detachedState;
            if (meta::isInitialized()) {
                em.meta_mergeExternal(src._name, _name, null, this, 'name', function setter(o:*):void{_name = o as String}) as String;
                em.meta_mergeExternal(src._password, _password, null, this, 'password', function setter(o:*):void{_password = o as String}) as String;
                em.meta_mergeExternal(src._username, _username, null, this, 'username', function setter(o:*):void{_username = o as String}) as String;
            }
            else {
                em.meta_mergeExternal(src._username, _username, null, this, 'username', function setter(o:*):void{_username = o as String}) as String;
            }
        }

        public function readExternal(input:IDataInput):void {
            __initialized = input.readObject() as Boolean;
            __detachedState = input.readObject() as String;
            if (meta::isInitialized()) {
                _name = input.readObject() as String;
                _password = input.readObject() as String;
                _username = input.readObject() as String;
            }
            else {
                _username = input.readObject() as String;
            }
        }

        public function writeExternal(output:IDataOutput):void {
            output.writeObject(__initialized);
            output.writeObject(__detachedState);
            if (meta::isInitialized()) {
                output.writeObject((_name is IPropertyHolder) ? IPropertyHolder(_name).object : _name);
                output.writeObject((_password is IPropertyHolder) ? IPropertyHolder(_password).object : _password);
                output.writeObject((_username is IPropertyHolder) ? IPropertyHolder(_username).object : _username);
            }
            else {
                output.writeObject(_username);
            }
        }
    }
}
