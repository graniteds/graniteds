/*
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
package org.granite.test.tide.data
{
    import mx.collections.ArrayCollection;
    
    import org.flexunit.Assert;
    import org.flexunit.async.Async;
    import org.granite.meta;
    import org.granite.persistence.PersistentMap;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.test.tide.spring.MockSpring;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.data.ChangeMerger;
    import org.granite.tide.data.ChangeRef;
    import org.granite.tide.data.ChangeSet;
    import org.granite.tide.data.ChangeSetBuilder;
    import org.granite.tide.data.CollectionChange;
    import org.granite.tide.data.CollectionChanges;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.invocation.InvocationResult;
    import org.granite.tide.spring.Spring;
    
    
    public class TestChangeSetEntityDeep {
		
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
			MockSpring.reset();
			_ctx = MockSpring.getInstance().getSpringContext();
			MockSpring.getInstance().token = new MockRemoteCallAsyncToken();
			
			MockSpring.getInstance().addComponents([ChangeMerger]);
        }
        
        
        [Test]
        public function testChangeSetEntityDeep():void {
        	var patient:Patient = new Patient();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;        	
			patient.name = "Walter White";
        	patient.medicationList = new PersistentSet();
			var medication:Medication = new Medication();
			medication.uid = "M1";
			medication.id = 1;
			medication.version = 0;
			medication.name = "Cancer";
			medication.patient = patient;
			medication.prescriptionList = new PersistentSet();			
			var prescription:Prescription = new Prescription();
			prescription.uid = "PR1";
			prescription.id = 1;
			prescription.version = 0;
			prescription.medication = medication;
			prescription.name = "Aspirin";
			medication.prescriptionList.addItem(prescription);
			patient.medicationList.addItem(medication);
			patient = _ctx.patient = _ctx.meta_mergeExternalData(patient);
			
			var medication2:Medication = new Medication();
			medication2.uid = "M2";
			medication2.name = "Flu";
			medication2.patient = patient;
			medication2.prescriptionList = new PersistentSet();
			var prescription2:Prescription = new Prescription();
			prescription2.uid = "PR2";
			prescription2.name = "Xanax";
			prescription2.medication = medication2;
			medication2.prescriptionList.addItem(prescription2);
			patient.medicationList.addItem(medication2);

			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet with one add", 1, changeSet.length);
			Assert.assertEquals("ChangeSet contains the new entity", 1, changeSet.changes[0].changes.medicationList.length);
			Assert.assertEquals("ChangeSet contains an add", 1, CollectionChanges(changeSet.changes[0].changes.medicationList).changes[0].type);
			var med:Medication = CollectionChanges(changeSet.changes[0].changes.medicationList).changes[0].value;
			Assert.assertTrue("ChangeSet contains the new entity", med.uid == medication2.uid);
        }
		
		
		[Test]
		public function testChangeSetEntityDeep2():void {
			var patient:Patient2 = new Patient2();
			patient.uid = "PA1";
			patient.id = 1;
			patient.version = 0;        	
			patient.name = "Tom";
			patient.alerts = new PersistentSet();
			var account:Account2 = new Account2();
			account.uid = "AC1";
			account.id = 1;
			account.version = 0;        	
			account.name = "Jerry";
			account.alerts = new PersistentSet();
			var alert:Alert2 = new Alert2();
			alert.uid = "AL1";
			alert.id = 1;
			alert.version = 0;
			alert.name = "Alert";
			alert.patient = patient;
			alert.account = account;
			account.alerts.addItem(alert);
			patient.alerts.addItem(alert);
			
			patient = _ctx.patient = _ctx.meta_mergeExternalData(patient);
			
			var alert2:Alert2 = new Alert2();
			alert2.uid = "AL2";
			alert2.name = "Alert 2";
			alert2.patient = patient;
			alert2.account = account;
			patient.alerts.addItem(alert2);
			account.alerts.addItem(alert2);
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet with one add", 2, changeSet.length);
			Assert.assertEquals("ChangeSet contains the new entity", 1, changeSet.changes[0].changes.alerts.changes.length);
			Assert.assertEquals("ChangeSet contains an add", 1, CollectionChanges(changeSet.changes[0].changes.alerts).changes[0].type);
			Assert.assertStrictlyEquals("ChangeSet same add in both collections", changeSet.changes[0].changes.alerts.changes[0].value, changeSet.changes[1].changes.alerts.changes[0].value);
		}
		
		
		[Test(async)]
		public function testChangeSetEntityDeepMerge():void {
			var patient:Patient = new Patient();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;       	
			patient.name = "Walter White";
			patient.medicationList = new PersistentSet();
			
			patient = _ctx.patient = Patient(_ctx.meta_mergeExternalData(patient));
			
			var medication:Medication = new Medication();
			medication.name = "Test";
			medication.patient = patient;
			medication.prescriptionList = new PersistentSet();			
			patient.medicationList.addItem(medication);
			
			var prescription:Prescription = new Prescription();
			prescription.name = "Bla";
			prescription.medication = medication;
			medication.prescriptionList.addItem(prescription);

			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			// Simulate remote call
			_ctx.tideService.applyChanges(changeSet, Async.asyncHandler(this, function(event:TideResultEvent, pass:Object = null):void {
				Assert.assertEquals("New medication id", 1, medication.id);
				Assert.assertEquals("New prescription id", 1, prescription.id);
				
				Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
				
				prescription.name = "Truc";
				
				Assert.assertTrue("Context dirty", _ctx.meta_dirty);
			}, 1000));
		}
		
		
		[Test]
		public function testChangeSetEntityDeepUninit():void {
			var patient:Patient = new Patient();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;        	
			patient.name = "Walter White";
			patient.medicationList = new PersistentSet();
			var medication:Medication = new Medication();
			medication.uid = "M1";
			medication.id = 1;
			medication.version = 0;
			medication.name = "Cancer";
			medication.patient = patient;
			medication.prescriptionList = new PersistentSet();			
			var prescription:Prescription = new Prescription();
			prescription.uid = "PR1";
			prescription.id = 1;
			prescription.version = 0;
			prescription.medication = medication;
			prescription.name = "Aspirin";
			medication.prescriptionList.addItem(prescription);
			patient.medicationList.addItem(medication);
			var medication2:Medication = new Medication();
			medication2.id = 2;
			medication2.uid = "M2";
			medication2.version = 0;
			medication2.name = "Flu";
			medication2.patient = patient;
			medication2.prescriptionList = new PersistentSet();
			var prescription2:Prescription = new Prescription();
			prescription2.id = 2;
			prescription2.uid = "PR2";
			prescription2.version = 0;
			prescription2.name = "Xanax";
			prescription2.medication = medication2;
			medication2.prescriptionList.addItem(prescription2);
			patient.medicationList.addItem(medication2);				
			
			patient = _ctx.patient = _ctx.meta_mergeExternalData(patient);
			
			var prescription3:Prescription = new Prescription();
			prescription3.uid = "PR3";
			prescription3.name = "Subutex";
			prescription3.medication = medication2;
			medication2.prescriptionList.addItem(prescription3);
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet with one add", 1, changeSet.length);
			Assert.assertEquals("ChangeSet contains the new entity", 1, changeSet.changes[0].changes.prescriptionList.length);
			Assert.assertTrue("Change contains prescription", _ctx.meta_objectEquals(prescription3, changeSet.changes[0].changes.prescriptionList.changes[0].value));
			Assert.assertFalse("ChangeSet prescription list uninitialized", changeSet.changes[0].changes.prescriptionList.changes[0].value.medication.prescriptionList.isInitialized());
			Assert.assertFalse("ChangeSet medication list uninitialized", changeSet.changes[0].changes.prescriptionList.changes[0].value.medication.patient.medicationList.isInitialized());
		}
		
		[Test]
		public function testChangeSetEntityDeepUninitB():void {
			var patient:Patient = new Patient();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;        	
			patient.name = "Walter White";
			patient.medicationList = new PersistentSet();
			var medication:Medication = new Medication();
			medication.uid = "M1";
			medication.id = 1;
			medication.version = 0;
			medication.name = "Cancer";
			medication.patient = patient;
			medication.prescriptionList = new PersistentSet();			
			var prescription:Prescription = new Prescription();
			prescription.uid = "PR1";
			prescription.id = 1;
			prescription.version = 0;
			prescription.medication = medication;
			prescription.name = "Aspirin";
			medication.prescriptionList.addItem(prescription);
			patient.medicationList.addItem(medication);
			var medication2:Medication = new Medication();
			medication2.id = 2;
			medication2.uid = "M2";
			medication2.version = 0;
			medication2.name = "Flu";
			medication2.patient = patient;
			medication2.prescriptionList = new PersistentSet();
			var prescription2:Prescription = new Prescription();
			prescription2.id = 2;
			prescription2.uid = "PR2";
			prescription2.version = 0;
			prescription2.name = "Xanax";
			prescription2.medication = medication2;
			medication2.prescriptionList.addItem(prescription2);
			patient.medicationList.addItem(medication2);				
			
			patient = _ctx.patient = _ctx.meta_mergeExternalData(patient);
			_ctx.meta_clearCache();
			
			var medication3:Medication = new Medication();
			medication3.uid = "M3";
			medication3.name = "Flu2";
			medication3.patient = patient;
			medication3.prescriptionList = new PersistentSet();
			var prescription3:Prescription = new Prescription();
			prescription3.uid = "PR3";
			prescription3.name = "Subutex";
			prescription3.medication = medication3;
			medication3.prescriptionList.addItem(prescription3);
			patient.medicationList.addItem(medication3);
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet with one add", 1, changeSet.length);
			Assert.assertEquals("ChangeSet contains the new entity", 1, changeSet.changes[0].changes.medicationList.length);
			Assert.assertTrue("Change contains prescription", _ctx.meta_objectEquals(prescription3, changeSet.changes[0].changes.medicationList.changes[0].value.prescriptionList.getItemAt(0)));
			Assert.assertFalse("ChangeSet medication list uninitialized", changeSet.changes[0].changes.medicationList.changes[0].value.patient.medicationList.isInitialized());
		}
		
		[Test]
		public function testChangeSetEntityDeepUninit2():void {
			var patient:Patient = new Patient();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;        	
			patient.name = "Walter White";
			patient.medicationList = new PersistentSet();
			var medication:Medication = new Medication();
			medication.uid = "M1";
			medication.id = 1;
			medication.version = 0;
			medication.name = "Cancer";
			medication.patient = patient;
			medication.prescriptionList = new PersistentSet();			
			var prescription:Prescription = new Prescription();
			prescription.uid = "PR1";
			prescription.id = 1;
			prescription.version = 0;
			prescription.medication = medication;
			prescription.name = "Aspirin";
			medication.prescriptionList.addItem(prescription);
			patient.medicationList.addItem(medication);
			
			patient = _ctx.patient = _ctx.meta_mergeExternalData(patient);
			
			var medication2:Medication = new Medication();
			medication2.uid = "M2";
			medication2.name = "Flu";
			medication2.patient = patient;
			medication2.prescriptionList = new PersistentSet();
			var prescription2:Prescription = new Prescription();
			prescription2.uid = "PR2";
			prescription2.name = "Xanax";
			prescription2.medication = medication2;
			prescription2.fills = new ArrayCollection();
			medication2.prescriptionList.addItem(prescription2);
			patient.medicationList.addItem(medication2);				
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet with one add", 1, changeSet.length);
			Assert.assertEquals("ChangeSet contains the new entity", 1, changeSet.changes[0].changes.medicationList.length);
			Assert.assertTrue("ChangeSet collection with init elts", changeSet.changes[0].changes.medicationList.changes[0].value.prescriptionList.isInitialized());
		}
		
		[Test]
		public function testChangeSetEntityDeepUninit3():void {
			var patient:Patient = new Patient();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;        	
			patient.name = "Walter White";
			patient.medicationList = new PersistentSet();
			var medication:Medication = new Medication();
			medication.uid = "M1";
			medication.id = 1;
			medication.version = 0;
			medication.name = "Cancer";
			medication.patient = patient;
			medication.prescriptionList = new PersistentSet();			
			patient.medicationList.addItem(medication);
			
			patient = _ctx.patient = _ctx.meta_mergeExternalData(patient);
			
			var medication2:Medication = new Medication();
			medication2.uid = "M2";
			medication2.name = "Flu";
			medication2.patient = patient;
			medication2.prescriptionList = new PersistentSet();
			patient.medicationList.addItem(medication2);				
			var medication3:Medication = new Medication();
			medication3.uid = "M3";
			medication3.name = "Flo";
			medication3.patient = patient;
			medication3.prescriptionList = new PersistentSet();
			patient.medicationList.addItem(medication3);				
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet with one add", 1, changeSet.length);
			Assert.assertEquals("ChangeSet contains the new entity", 2, changeSet.changes[0].changes.medicationList.length);
			Assert.assertTrue("ChangeSet new medication 1 prescriptions initialized", changeSet.changes[0].changes.medicationList.changes[0].value.prescriptionList.isInitialized());
			Assert.assertTrue("ChangeSet new medication 2 prescriptions initialized", changeSet.changes[0].changes.medicationList.changes[1].value.prescriptionList.isInitialized());
			Assert.assertFalse("ChangeSet patient mediations not initialized", changeSet.changes[0].changes.medicationList.changes[0].value.patient.medicationList.isInitialized());
		}
	}
}


import flash.events.TimerEvent;
import flash.utils.Timer;

import mx.collections.ArrayCollection;
import mx.messaging.messages.AcknowledgeMessage;
import mx.messaging.messages.ErrorMessage;
import mx.messaging.messages.IMessage;
import mx.rpc.AsyncToken;
import mx.rpc.Fault;
import mx.rpc.IResponder;
import mx.rpc.events.AbstractEvent;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;

import org.flexunit.Assert;
import org.granite.persistence.PersistentSet;
import org.granite.test.tide.Person;
import org.granite.test.tide.data.Medication;
import org.granite.test.tide.data.Patient;
import org.granite.test.tide.data.Prescription;
import org.granite.test.tide.spring.MockSpringAsyncToken;
import org.granite.tide.data.ChangeSet;
import org.granite.tide.invocation.ContextUpdate;
import org.granite.tide.invocation.InvocationCall;
import org.granite.tide.invocation.InvocationResult;
import org.granite.tide.spring.Spring;


class MockRemoteCallAsyncToken extends MockSpringAsyncToken {
	
	function MockRemoteCallAsyncToken() {
		super(null);
	}
	
	protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
		if (componentName == "tideService" && op == "applyChanges") {
			// Build the result message
			
			Assert.assertTrue("ChangeSet received", params[0] is ChangeSet);
			
			var patient:Patient = Spring.getInstance().getSpringContext().patient as Patient;
			var medication:Medication = patient.medicationList.getItemAt(0) as Medication;
			var prescription:Prescription = medication.prescriptionList.getItemAt(0) as Prescription;
			
			var updates:Array = [];
			
			var p1:Patient = new Patient();
			p1.id = patient.id;
			p1.uid = patient.uid;
			p1.version = patient.version;
			p1.name = patient.name;
			p1.medicationList = new PersistentSet(false);
			
			var m1:Medication = new Medication();
			m1.id = 1;
			m1.version = 0;
			m1.uid = medication.uid;
			m1.name = medication.name;
			m1.patient = p1;
			m1.prescriptionList = new PersistentSet(false);
			
			var pr1:Prescription = new Prescription();
			pr1.id = 1;
			pr1.version = 0;
			pr1.uid = prescription.uid;
			pr1.name = prescription.name;
			pr1.medication = m1;
			
			updates.push([ "PERSIST", pr1 ]);
			
			var p2:Patient = new Patient();
			p2.id = patient.id;
			p2.uid = patient.uid;
			p2.version = patient.version;
			p2.name = patient.name;
			p2.medicationList = new PersistentSet();
			
			var m2:Medication = new Medication();
			m2.id = 1;
			m2.version = 0;
			m2.uid = medication.uid;
			m2.name = medication.name;
			m2.patient = p2;
			m2.prescriptionList = new PersistentSet(false);			
			p2.medicationList.addItem(m2);
			
			updates.push([ "UPDATE", p2 ]);
			
			var p3:Patient = new Patient();
			p3.id = patient.id;
			p3.uid = patient.uid;
			p3.version = patient.version;
			p3.name = patient.name;
			p3.medicationList = new PersistentSet(false);
			
			var m3:Medication = new Medication();
			m3.id = 1;
			m3.version = 0;
			m3.uid = medication.uid;
			m3.name = medication.name;
			m3.patient = p3;
			m3.prescriptionList = new PersistentSet();
			
			var pr3:Prescription = new Prescription();
			pr3.id = 1;
			pr3.version = 0;
			pr3.uid = prescription.uid;
			pr3.name = prescription.name;
			pr3.medication = m3;
			m3.prescriptionList.addItem(pr3);
			
			updates.push([ "PERSIST", m3 ]);
			
			var p4:Patient = new Patient();
			p4.id = patient.id;
			p4.uid = patient.uid;
			p4.version = patient.version;
			p4.name = patient.name;
			p4.medicationList = new PersistentSet();
			
			var m4:Medication = new Medication();
			m4.id = 1;
			m4.uid = medication.uid;
			m4.version = 0;
			m4.name = medication.name;
			m4.patient = p4;
			m4.prescriptionList = new PersistentSet(false);			
			p4.medicationList.addItem(m4);
			
			updates.push([ "UPDATE", p4 ]);
			
			var p5:Patient = new Patient();
			p5.id = patient.id;
			p5.uid = patient.uid;
			p5.version = patient.version;
			p5.name = patient.name;
			p5.medicationList = new PersistentSet(false);
			
			var m5:Medication = new Medication();
			m5.id = 1;
			m5.uid = medication.uid;
			m5.version = 0;
			m5.name = medication.name;
			m5.patient = p5;
			m5.prescriptionList = new PersistentSet();
			
			var pr5:Prescription = new Prescription();
			pr5.id = 1;
			pr5.uid = prescription.uid;
			pr5.version = 0;
			pr5.name = prescription.name;
			pr5.medication = m5;
			m5.prescriptionList.addItem(pr5);
			
			updates.push([ "UPDATE", m5 ]);
			
			var p6:Patient = new Patient();
			p6.id = patient.id;
			p6.uid = patient.uid;
			p6.version = patient.version;
			p6.name = patient.name;
			p6.medicationList = new PersistentSet();
			
			var m6:Medication = new Medication();
			m6.id = 1;
			m6.uid = medication.uid;
			m6.version = 0;
			m6.name = medication.name;
			m6.patient = p6;
			m6.prescriptionList = new PersistentSet(false);			
			p6.medicationList.addItem(m6);
			
			var res:ResultEvent = buildResult(p6);
			InvocationResult(res.result).updates = updates;
			return res;
		}
		return buildFault("Server.Error");
	}
}
