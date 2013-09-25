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
package org.granite.test.tide.spring
{
    import org.flexunit.Assert;
    import org.flexunit.async.Async;
    import org.granite.meta;
    import org.granite.persistence.PersistentList;
    import org.granite.persistence.PersistentMap;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.test.tide.data.Diagnosis;
    import org.granite.test.tide.data.Patient4;
    import org.granite.test.tide.data.PatientStatus;
    import org.granite.test.tide.data.Visit2;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.data.ChangeMerger;
    import org.granite.tide.data.ChangeRef;
    import org.granite.tide.data.ChangeSet;
    import org.granite.tide.data.ChangeSetBuilder;
    import org.granite.tide.data.CollectionChanges;
    import org.granite.tide.events.TideResultEvent;
    
    
    public class TestSpringChangeSetEntityRemote3 {
		
		private var _ctx:BaseContext = MockSpring.getInstance().getContext();
		
		
		[Before]
		public function setUp():void {
			MockSpring.reset();
			_ctx = MockSpring.getInstance().getSpringContext();
			MockSpring.getInstance().token = new MockSimpleCallAsyncToken(_ctx);
			
			MockSpring.getInstance().addComponents([ChangeMerger]);
			_ctx.meta_uninitializeAllowed = false;
		}
        
        
        [Test(async)]
		public function testApplyRemoteChangeList1():void {
			var patient:Patient4 = new Patient4();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;
			patient.name = "Test";
			patient.diagnosis = new PersistentSet();
			patient.visits = new PersistentSet();
			var status:PatientStatus = new PatientStatus();
			status.uid = "S1";
			status.id = 1;
			status.version = 0;
			status.name = "Test";
			status.patient = patient;
			status.deathCauses = new PersistentList();			
			patient.status = status;
			var visit:Visit2 = new Visit2();
			visit.uid = "V1";
			visit.id = 1;
			visit.version = 0;
			visit.name = "Test";
			visit.patient = patient;
			visit.assessments = new PersistentSet();
			patient.visits.addItem(visit);
			var diagnosis:Diagnosis = new Diagnosis();
			diagnosis.uid = "D1";
			diagnosis.id = 1;
			diagnosis.version = 0;
			diagnosis.name = "Test";
			diagnosis.patient = patient;
			diagnosis.visit = visit;
			patient.diagnosis.addItem(diagnosis);
			patient.status.deathCauses.addItem(diagnosis);
			visit.assessments.addItem(diagnosis);
			
			_ctx.patient = _ctx.meta_mergeExternalData(patient);
			_ctx.meta_clearCache();
			
			patient = _ctx.patient;
			
			var diagnosis2:Diagnosis = new Diagnosis();
			diagnosis2.uid = "D2";
			diagnosis2.patient = patient;
			diagnosis2.visit = visit;
			patient.status.deathCauses.addItem(diagnosis2);
			// visit.assessments.addItem(diagnosis2);
			
			diagnosis2.name = "Test2";
			
			
			// Simulate remote call with local merge of arguments
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();

			_ctx.tideService.applyChangeSet1(changeSet, Async.asyncHandler(this, resultHandler1, 1000));
		}

		private function resultHandler1(event:TideResultEvent, pass:Object = null):void {
			
			var patient:Patient4 = Patient4(_ctx.patient);
			var status:PatientStatus = patient.status;
			
			Assert.assertStrictlyEquals("Patient em", _ctx, patient.meta::entityManager);
			Assert.assertStrictlyEquals("Diagnosis patient", patient, patient.diagnosis.getItemAt(1).patient);
			Assert.assertStrictlyEquals("Diagnosis patient", patient, status.deathCauses.getItemAt(1).patient);
			Assert.assertStrictlyEquals("Diagnosis em", _ctx, patient.diagnosis.getItemAt(1).meta::entityManager);
			Assert.assertStrictlyEquals("Diagnosis em", _ctx, status.deathCauses.getItemAt(1).meta::entityManager);
			Assert.assertEquals("Diagnosis added to patient", 2, patient.diagnosis.length);
			Assert.assertEquals("Diagnosis added to patient (id)", 2, patient.diagnosis.getItemAt(1).id);
			Assert.assertEquals("Diagnosis added to status", 2, status.deathCauses.length);
			Assert.assertEquals("Diagnosis added to status (id)", 2, status.deathCauses.getItemAt(1).id);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			var diagnosis3:Diagnosis = new Diagnosis();
			diagnosis3.uid = "D3";
			diagnosis3.name = "Test3";
			diagnosis3.patient = patient;
			diagnosis3.visit = patient.visits.getItemAt(0) as Visit2;
			status.deathCauses.addItem(diagnosis3);
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
		}

		
		[Test(async)]
		public function testApplyRemoteChangeList2():void {
			var patient:Patient4 = new Patient4();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;
			patient.name = "Test";
			patient.diagnosis = new PersistentSet();
			patient.visits = new PersistentSet();
			var status:PatientStatus = new PatientStatus();
			status.uid = "S1";
			status.id = 1;
			status.version = 0;
			status.name = "Test";
			status.patient = patient;
			status.deathCauses = new PersistentList();			
			patient.status = status;
			var visit:Visit2 = new Visit2();
			visit.uid = "V1";
			visit.id = 1;
			visit.version = 0;
			visit.name = "Test";
			visit.patient = patient;
			visit.assessments = new PersistentSet();
			patient.visits.addItem(visit);
			var diagnosis:Diagnosis = new Diagnosis();
			diagnosis.uid = "D1";
			diagnosis.id = 1;
			diagnosis.version = 0;
			diagnosis.name = "Test";
			diagnosis.patient = patient;
			diagnosis.visit = visit;
			patient.diagnosis.addItem(diagnosis);
			patient.status.deathCauses.addItem(diagnosis);
			visit.assessments.addItem(diagnosis);
			
			_ctx.patient = _ctx.meta_mergeExternalData(patient);
			_ctx.meta_clearCache();
			
			patient = _ctx.patient;
			
			var diagnosis2:Diagnosis = new Diagnosis();
			diagnosis2.uid = "D2";
			diagnosis2.patient = patient;
			diagnosis2.visit = visit;
			patient.status.deathCauses.addItem(diagnosis2);
			// visit.assessments.addItem(diagnosis2);
			
			diagnosis2.name = "Test2";
			
			
			// Simulate remote call with local merge of arguments
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			_ctx.tideService.applyChangeSet2(changeSet, Async.asyncHandler(this, resultHandler2, 1000));
		}
		
		private function resultHandler2(event:TideResultEvent, pass:Object = null):void {
			
			var patient:Patient4 = Patient4(_ctx.patient);
			var status:PatientStatus = patient.status;
			
			Assert.assertStrictlyEquals("Patient em", _ctx, patient.meta::entityManager);
			Assert.assertStrictlyEquals("Diagnosis patient", patient, patient.diagnosis.getItemAt(1).patient);
			Assert.assertStrictlyEquals("Diagnosis patient", patient, status.deathCauses.getItemAt(1).patient);
			Assert.assertStrictlyEquals("Diagnosis em", _ctx, patient.diagnosis.getItemAt(1).meta::entityManager);
			Assert.assertStrictlyEquals("Diagnosis em", _ctx, status.deathCauses.getItemAt(1).meta::entityManager);
			Assert.assertEquals("Diagnosis added to patient", 2, patient.diagnosis.length);
			Assert.assertEquals("Diagnosis added to patient (id)", 2, patient.diagnosis.getItemAt(1).id);
			Assert.assertEquals("Diagnosis added to status", 2, status.deathCauses.length);
			Assert.assertEquals("Diagnosis added to status (id)", 2, status.deathCauses.getItemAt(1).id);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			status.deathCauses.removeItemAt(0);
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
		}
	}
}


import mx.rpc.events.AbstractEvent;

import org.flexunit.Assert;
import org.granite.persistence.PersistentSet;
import org.granite.reflect.Type;
import org.granite.test.tide.data.Diagnosis;
import org.granite.test.tide.data.Patient;
import org.granite.test.tide.data.Patient4;
import org.granite.test.tide.data.PatientStatus;
import org.granite.test.tide.data.Visit2;
import org.granite.test.tide.spring.MockSpringAsyncToken;
import org.granite.tide.BaseContext;
import org.granite.tide.data.Change;
import org.granite.tide.data.ChangeRef;
import org.granite.tide.data.ChangeSet;
import org.granite.tide.data.CollectionChanges;
import org.granite.tide.invocation.InvocationCall;

class MockSimpleCallAsyncToken extends MockSpringAsyncToken {
	
	private var _ctx:BaseContext;
	
	function MockSimpleCallAsyncToken(ctx:BaseContext):void {
		super(null);
		_ctx = ctx;
	}
	
	protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
		if (componentName == "personService" && op == "modifyPerson") {
			if (!(params[0] is ChangeSet && params[0].changes.length == 1))
				return buildFault("Illegal.Argument");
			
			var changeSet:ChangeSet = params[0] as ChangeSet;
			Assert.assertEquals("ChangeSet count 4", 1, changeSet.length);
			Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).changes.lastName);
			var coll:CollectionChanges = changeSet.getChange(0).changes.contacts as CollectionChanges;
			Assert.assertEquals("ChangeSet collection", 4, coll.length);
			Assert.assertEquals("ChangeSet collection type", 1, coll.getChange(3).type);
			Assert.assertEquals("ChangeSet collection index", 2, coll.getChange(3).key);
			Assert.assertTrue("ChangeSet collection value", coll.getChange(3).value is ChangeRef);
			Assert.assertStrictlyEquals("ChangeSet collection value", "C3", coll.getChange(3).value.uid);
			return buildResult("ok");
		}
		else if (componentName == "tideService" && op.indexOf("applyChangeSet") == 0) {
			if (!(params[0] is ChangeSet))
				return buildFault("Illegal.Argument");

			var patient:Patient4 = Patient4(_ctx.patient);
			var status:PatientStatus = patient.status;
			var visit:Visit2 = Visit2(patient.visits.getItemAt(0));
			
			var patientb:Patient4 = new Patient4();
			patientb.id = patient.id;
			patientb.uid = patient.uid;
			patientb.version = patient.version;
			patientb.name = "Test";
			patientb.diagnosis = new PersistentSet(false);
			patientb.visits = new PersistentSet(false);
			var statusb:PatientStatus = new PatientStatus();
			statusb.id = status.id;
			statusb.uid = status.uid;
			statusb.version = status.version+1;
			statusb.deathCauses = new PersistentSet(false);
			patientb.status = statusb;
			var visitb:Visit2 = new Visit2();
			visitb.id = visit.id;
			visitb.uid = visit.uid;
			visitb.version = visit.version;
			visitb.assessments = new PersistentSet(false);
			visitb.patient = patientb;
			
			var diagnosis2b:Diagnosis = new Diagnosis();
			diagnosis2b.uid = "D2";
			diagnosis2b.id = 2;
			diagnosis2b.version = 0;
			diagnosis2b.name = "Test2";
			diagnosis2b.patient = patientb;
			diagnosis2b.visit = visitb;
			
			
			var change1:Change = new Change(Type.forClass(Patient4).name, patient.uid, patient.id, patientb.version);
			var collChanges1:CollectionChanges = new CollectionChanges();
			collChanges1.addChange(1, null, diagnosis2b);
			change1.changes.diagnosis = collChanges1;
			
			var change3:Change = new Change(Type.forClass(PatientStatus).name, status.uid, status.id, statusb.version+1);
			var collChanges3:CollectionChanges = new CollectionChanges();
			collChanges3.addChange(1, 1, diagnosis2b);
			change3.changes.deathCauses = collChanges3;
			
			if (op == "applyChangeSet1")			
				return buildResult(null, null, [[ 'UPDATE', new ChangeSet([ change1 ]) ], [ 'PERSIST', diagnosis2b ], [ 'UPDATE', new ChangeSet([ change3 ]) ]]);
			else if (op == "applyChangeSet2")
				return buildResult(null, null, [[ 'PERSIST', diagnosis2b ], [ 'UPDATE', new ChangeSet([ change1 ]) ], [ 'UPDATE', new ChangeSet([ change3 ]) ]]);
		}
		
		return buildFault("Server.Error");
	}
}
