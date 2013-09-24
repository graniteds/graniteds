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
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.data.Consent;
    import org.granite.test.tide.data.Document;
    import org.granite.test.tide.data.DocumentList;
    import org.granite.test.tide.data.DocumentPayload;
    import org.granite.test.tide.data.Patient5;
    import org.granite.tide.BaseContext;
    import org.granite.tide.data.ChangeMerger;
    import org.granite.tide.data.ChangeRef;
    import org.granite.tide.data.ChangeSet;
    import org.granite.tide.data.ChangeSetBuilder;
    import org.granite.tide.data.CollectionChanges;
    import org.granite.tide.events.TideResultEvent;
    
    
    public class TestSpringChangeSetEntityRemote7 {
		
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
		public function testChangeSetEntityRemote10():void {
			var patient:Patient5 = new Patient5();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;
			patient.consents = new PersistentSet();
			
			_ctx.patient = _ctx.meta_mergeExternalData(patient);
			_ctx.meta_clearCache();
			
			patient = _ctx.patient as Patient5;
			
			var consent:Consent = new Consent();
			consent.patient = patient;
			patient.consents.addItem(consent);
			
			// Simulate remote call with local merge of arguments
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			_ctx.tideService.applyChangeSet(changeSet, Async.asyncHandler(this, resultHandler, 1000));
		}

		private function resultHandler(event:TideResultEvent, pass:Object = null):void {
			
			var patient:Patient5 = Patient5(_ctx.patient);
			
			var consent:Consent = patient.consents.removeItemAt(0) as Consent;
			
			_ctx.tideService.removeConsent(consent, Async.asyncHandler(this, result2Handler, 1000));
		}
		
		private function result2Handler(event:TideResultEvent, pass:Object = null):void {
			var patient:Patient5 = Patient5(_ctx.patient);
			
			Assert.assertEquals("Consent list empty", 0, patient.consents.length);
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			patient.consents.addItem(new Consent());
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
		}
		
		
		[Test(async)]
		public function testChangeSetEntityRemote11():void {
			var patient:Patient5 = new Patient5();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;
			patient.consents = new PersistentSet();
			
			_ctx.patient = _ctx.meta_mergeExternalData(patient);
			_ctx.meta_clearCache();
			
			patient = _ctx.patient as Patient5;
			
			var consent:Consent = new Consent();
			consent.patient = patient;
			patient.consents.addItem(consent);
			
			// Simulate remote call with local merge of arguments
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			_ctx.tideService.applyChangeSet(changeSet, Async.asyncHandler(this, result11Handler, 1000));
		}
		
		private function result11Handler(event:TideResultEvent, pass:Object = null):void {
			
			var patient:Patient5 = Patient5(_ctx.patient);
			
			var consent:Consent = patient.consents.removeItemAt(0) as Consent;
			
			_ctx.tideService.removeConsent2(consent, Async.asyncHandler(this, result12Handler, 1000));
		}
		
		private function result12Handler(event:TideResultEvent, pass:Object = null):void {
			var patient:Patient5 = Patient5(_ctx.patient);
			
			Assert.assertEquals("Consent list empty", 0, patient.consents.length);
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			patient.consents.addItem(new Consent());
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
		}
	}
}


import mx.rpc.events.AbstractEvent;

import org.flexunit.Assert;
import org.granite.persistence.PersistentSet;
import org.granite.reflect.Type;
import org.granite.test.tide.data.Consent;
import org.granite.test.tide.data.Document;
import org.granite.test.tide.data.DocumentList;
import org.granite.test.tide.data.DocumentPayload;
import org.granite.test.tide.data.Patient5;
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
		var patient:Patient5 = Patient5(_ctx.patient);
		
		if (componentName == "tideService" && op.indexOf("applyChangeSet") == 0) {
			if (!(params[0] is ChangeSet))
				return buildFault("Illegal.Argument");

			var patientb:Patient5 = new Patient5();
			patientb.id = patient.id;
			patientb.uid = patient.uid;
			patientb.version = patient.version;
			patientb.name = patient.name;
			patientb.consents = new PersistentSet(false);
			
			var consentb:Consent = new Consent();
			consentb.uid = params[0].changes[0].changes.consents.changes[0].value.uid;
			consentb.id = 1;
			consentb.version = 0;
			consentb.patient = patientb;
			
			var change1:Change = new Change(Type.forClass(Patient5).name, patientb.uid, patientb.id, 0);
			var collChanges1:CollectionChanges = new CollectionChanges();
			collChanges1.addChange(1, null, consentb);
			change1.changes.consents = collChanges1;
			
			return buildResult(null, null, [[ 'PERSIST', consentb ], [ 'UPDATE', new ChangeSet([ change1 ]) ]]);
		}
		else if (componentName == "tideService" && op.indexOf("removeConsent") == 0) {
			var consentRef:ChangeRef = new ChangeRef(Type.forClass(Consent).name, params[0].uid, params[0].id);
			
			var consentRef0:ChangeRef = new ChangeRef(Type.forClass(Consent).name, params[0].uid, params[0].id);
			
			var change2:Change = new Change(Type.forClass(Patient5).name, patient.uid, patient.id, 0);
			var collChanges2:CollectionChanges = new CollectionChanges();
			collChanges2.addChange(-1, null, consentRef0);
			change2.changes.consents = collChanges2;
			
			if (op == "removeConsent")
				return buildResult(null, null, [[ 'UPDATE', new ChangeSet([ change2 ]) ], [ 'REMOVE', consentRef ]]);
			else if (op == "removeConsent2")
				return buildResult(null, null, [[ 'REMOVE', consentRef ]]);
		}
		
		return buildFault("Server.Error");
	}
}
