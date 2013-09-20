/**
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
    import mx.collections.Sort;
    import mx.collections.SortField;
    
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
    
    
    public class TestSpringChangeSetEntityRemote9 {
		
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
		public function testChangeSetEntityRemote13():void {
			var patient:Patient5 = new Patient5();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;
			patient.consents = new PersistentSet();
			var consent:Consent = new Consent();
			consent.uid = "C1";
			consent.id = 1;
			consent.version = 0;
			consent.name = "clo";
			consent.patient = patient;
			patient.consents.addItem(consent);
			
			_ctx.patient = _ctx.meta_mergeExternalData(patient);
			_ctx.meta_clearCache();
			
			patient.consents.sort = new Sort();
			patient.consents.sort.fields = [ new SortField("name") ];
			patient.consents.refresh();
			
			patient = _ctx.patient as Patient5;
			
			patient.consents.removeItemAt(0);
			
			var consent2:Consent = new Consent();
			consent2.patient = patient;
			consent2.name = "blo";
			patient.consents.addItem(consent2);
			
			// Simulate remote call with local merge of arguments
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			_ctx.tideService.applyChangeSet(changeSet, Async.asyncHandler(this, resultHandler, 1000));
		}

		private function resultHandler(event:TideResultEvent, pass:Object = null):void {
			var patient:Patient5 = Patient5(_ctx.patient);
			
			Assert.assertEquals("Consent list elements", 1, patient.consents.length);
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			patient.consents.removeItemAt(0);
			
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
			consentb.uid = params[0].changes[0].changes.consents.changes[1].value.uid;
			consentb.id = 2;
			consentb.version = 0;
			consentb.name = "blo";
			consentb.patient = patientb;
			
			var consentref:ChangeRef = new ChangeRef(Type.forClass(Consent).name, params[0].changes[0].changes.consents.changes[0].value.uid, params[0].changes[0].changes.consents.changes[0].value.id);
			
			var change:Change = new Change(Type.forClass(Patient5).name, patientb.uid, patientb.id, 0);
			var collChanges:CollectionChanges = new CollectionChanges();
			collChanges.addChange(-1, null, new Consent());
			collChanges.addChange(1, null, consentb);
			change.changes.consents = collChanges;
			
			return buildResult(null, null, [[ 'PERSIST', consentb ], [ 'UPDATE', new ChangeSet([ change ]) ], [ 'REMOVE', consentref ]]);
		}
		
		return buildFault("Server.Error");
	}
}
