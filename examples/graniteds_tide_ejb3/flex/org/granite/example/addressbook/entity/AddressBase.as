/**
 * Generated by Gas3 v2.3.0 (Granite Data Services).
 *
 * WARNING: DO NOT CHANGE THIS FILE. IT MAY BE OVERWRITTEN EACH TIME YOU USE
 * THE GENERATOR. INSTEAD, EDIT THE INHERITED CLASS (Address.as).
 */

package org.granite.example.addressbook.entity {

    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
    import org.granite.meta;
    import org.granite.tide.IEntityManager;
    import org.granite.tide.IPropertyHolder;

    use namespace meta;

    [Managed]
    public class AddressBase extends AbstractEntity {

        private var _address1:String;
        private var _address2:String;
        private var _city:String;
        private var _country:Country;
        private var _zipcode:String;

        public function set address1(value:String):void {
            _address1 = value;
        }
        public function get address1():String {
            return _address1;
        }

        public function set address2(value:String):void {
            _address2 = value;
        }
        public function get address2():String {
            return _address2;
        }

        public function set city(value:String):void {
            _city = value;
        }
        public function get city():String {
            return _city;
        }

        public function set country(value:Country):void {
            _country = value;
        }
        public function get country():Country {
            return _country;
        }

        public function set zipcode(value:String):void {
            _zipcode = value;
        }
        public function get zipcode():String {
            return _zipcode;
        }

        meta override function merge(em:IEntityManager, obj:*):void {
            var src:AddressBase = AddressBase(obj);
            super.meta::merge(em, obj);
            if (meta::isInitialized()) {
               em.meta_mergeExternal(src._address1, _address1, null, this, 'address1', function setter(o:*):void{_address1 = o as String}, false);
               em.meta_mergeExternal(src._address2, _address2, null, this, 'address2', function setter(o:*):void{_address2 = o as String}, false);
               em.meta_mergeExternal(src._city, _city, null, this, 'city', function setter(o:*):void{_city = o as String}, false);
               em.meta_mergeExternal(src._country, _country, null, this, 'country', function setter(o:*):void{_country = o as Country}, false);
               em.meta_mergeExternal(src._zipcode, _zipcode, null, this, 'zipcode', function setter(o:*):void{_zipcode = o as String}, false);
            }
        }

        public override function readExternal(input:IDataInput):void {
            super.readExternal(input);
            if (meta::isInitialized()) {
                _address1 = input.readObject() as String;
                _address2 = input.readObject() as String;
                _city = input.readObject() as String;
                _country = input.readObject() as Country;
                _zipcode = input.readObject() as String;
            }
        }

        public override function writeExternal(output:IDataOutput):void {
            super.writeExternal(output);
            if (meta::isInitialized()) {
                output.writeObject((_address1 is IPropertyHolder) ? IPropertyHolder(_address1).object : _address1);
                output.writeObject((_address2 is IPropertyHolder) ? IPropertyHolder(_address2).object : _address2);
                output.writeObject((_city is IPropertyHolder) ? IPropertyHolder(_city).object : _city);
                output.writeObject((_country is IPropertyHolder) ? IPropertyHolder(_country).object : _country);
                output.writeObject((_zipcode is IPropertyHolder) ? IPropertyHolder(_zipcode).object : _zipcode);
            }
        }
    }
}
