package test.granite.ctl
{
    import mx.collections.ArrayCollection;
    import mx.controls.Alert;
    import mx.events.CloseEvent;
    import mx.events.PropertyChangeEvent;
    import mx.logging.Log;
    import mx.logging.targets.TraceTarget;
    import mx.data.utils.Managed;
    
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.spring.Context;
    
    import test.granite.spring.entity.Address;
    import test.granite.spring.entity.Contact;
    import test.granite.spring.entity.Person;
    
    
    [Bindable]
    [Name("addressBookCtl")]
    public class AddressBookCtl {
        
        [In]
        public var context:Context;
        
        [In]
        public var personController:Object;
        
        [In]
        public var application:Object;
        
        [In] [Out]
        public var person:Person;
        
        [Out]
        public var contact:Contact;
        
        
        public function init():void {
            var t:TraceTarget = new TraceTarget();
            t.filters = ["org.granite.*"];
            Log.addTarget(t);
        }
        
        
        [Observer("selectPerson")]
        public function selectPerson(selectedPerson:Person):void {
            if (selectedPerson)
                person = selectedPerson;
        }
        
        [Observer("newPerson")]
        public function newPerson():void {
            person = new Person();
            person.contacts = new ArrayCollection();
            application.currentState = "CreatePerson";
        }

        [Observer("createPerson")]
        public function createPerson():void {
            personController.create({person: person}, personCreated);
        }
        
        private function personCreated(event:TideResultEvent):void {
            application.currentState = "";
        }

        [Observer("editPerson")]
        public function editPerson(selectedPerson:Person):void {
        	person = selectedPerson;
            application.currentState = "EditPerson";
        }
        
        [Observer("cancelPerson")]
        public function cancelPerson():void {
            Managed.resetEntity(person);
            application.currentState = "";
        }

        [Observer("modifyPerson")]
        public function modifyPerson():void {
            personController.update({person: person}, personModified);
        }
        
        private function personModified(event:TideResultEvent):void {
            application.currentState = "";
        }

        
        [Observer("askDeletePerson")]
        public function askDeletePerson():void {
            Alert.show('Do you really want to delete this person ?', 'Confirmation', (Alert.YES | Alert.NO), 
                null, deletePerson);
        }
        
        private function deletePerson(event:CloseEvent):void {
            if (event.detail == Alert.YES) {
                personController.remove({personId: person.id}, personDeleted);
            }
        }
        
        private function personDeleted(event:TideResultEvent):void {
            person = null;
        }
        
        
        [Observer("newContact")]
        public function newContact():void {
            var newContact:Contact = new Contact();
            newContact.address = new Address();
            newContact.person = person;
            person.contacts.addItem(newContact);
            contact = newContact;
            application.currentState = "CreateContact";
        }
        
        [Observer("editContact")]
        public function editContact(selectedContact:Contact):void {
            contact = selectedContact;
            application.currentState = "EditContact";
        }
        
        [Observer("cancelContact")]
        public function cancelContact():void {
            Managed.resetEntity(person);
            contact = null;
            application.currentState = "";
        }

        [Observer("askDeleteContact")]
        public function askDeleteContact(selectedContact:Contact):void {
            contact = selectedContact;
            Alert.show('Do you really want to delete this contact ?', 'Confirmation', (Alert.YES | Alert.NO), 
                null, deleteContact);
        }
        
        private function deleteContact(event:CloseEvent):void {
            if (event.detail == Alert.YES) {
                var contactIndex:int = person.contacts.getItemIndex(contact);
                person.contacts.removeItemAt(contactIndex);
                personController.update({person: person});
            }
        }

        [Observer("createContact")]
        public function createContact():void {
            personController.update({person: person}, contactCreated);
        }
        
        private function contactCreated(event:TideResultEvent):void {
            application.currentState = "";
            contact = null;
        }

        [Observer("modifyContact")]
        public function modifyContact():void {
            personController.update({person: person}, contactModified);
        }
        
        private function contactModified(event:TideResultEvent):void {
            application.currentState = "";
        }

        [Observer("create100")]
        public function create100():void {
            personController.create100();
        }
    }
}
