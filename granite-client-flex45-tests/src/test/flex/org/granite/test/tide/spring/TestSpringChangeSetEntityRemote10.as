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
package org.granite.test.tide.spring
{
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.messaging.Producer;
    
    import org.flexunit.Assert;
    import org.flexunit.async.Async;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.data.Consent;
    import org.granite.test.tide.data.Document;
    import org.granite.test.tide.data.DocumentList;
    import org.granite.test.tide.data.DocumentPayload;
    import org.granite.test.tide.data.Patient5;
    import org.granite.test.tide.data.Patient7;
    import org.granite.test.tide.data.PatientProvider;
    import org.granite.test.tide.data.Provider;
    import org.granite.tide.BaseContext;
    import org.granite.tide.data.ChangeMerger;
    import org.granite.tide.data.ChangeRef;
    import org.granite.tide.data.ChangeSet;
    import org.granite.tide.data.ChangeSetBuilder;
    import org.granite.tide.data.CollectionChanges;
    import org.granite.tide.events.TideResultEvent;
    
    
    public class TestSpringChangeSetEntityRemote10 {
		
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
		public function testChangeSetEntityRemote14():void {
			var patient:Patient7 = new Patient7();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;
			patient.providers = new PersistentSet();
			
			var provider:Provider = new Provider();
			provider.uid = "PR1";
			provider.id = 1;
			provider.version = 0;
			provider.name = "Prov1";
			var provider2:Provider = new Provider();
			provider2.uid = "PR2";
			provider2.id = 2;
			provider2.version = 0;
			provider2.name = "Prov2";
			
			var pprovider:PatientProvider = new PatientProvider();
			pprovider.uid = "PPR1";
			pprovider.id = 1;
			pprovider.version = 0;
			pprovider.name = "clo";
			pprovider.patient = patient;
			pprovider.provider = provider;
			patient.providers.addItem(pprovider);
			
			_ctx.patient = _ctx.meta_mergeExternalData(patient);
			provider2 = _ctx.provider2 = _ctx.meta_mergeExternal(provider2);
			_ctx.meta_clearCache();
			
			patient.providers.sort = new Sort();
			patient.providers.sort.fields = [ new SortField("name") ];
			patient.providers.refresh();
			
			patient = _ctx.patient as Patient7;
			
			patient.providers.removeItemAt(0);
			
			var pprovider2:PatientProvider = new PatientProvider();
			pprovider2.name = "clo";
			pprovider2.patient = patient;
			pprovider2.provider = provider2;
			patient.providers.addItem(pprovider2);
			
			// Simulate remote call with local merge of arguments
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			_ctx.tideService.applyChangeSet(changeSet, Async.asyncHandler(this, resultHandler, 1000));
		}

		private function resultHandler(event:TideResultEvent, pass:Object = null):void {
			var patient:Patient7 = Patient7(_ctx.patient);
			
			Assert.assertEquals("Provider list elements", 1, patient.providers.length);
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			patient.providers.removeItemAt(0);
			
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
import org.granite.test.tide.data.Patient7;
import org.granite.test.tide.data.PatientProvider;
import org.granite.test.tide.data.Provider;
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
		var patient:Patient7 = Patient7(_ctx.patient);
		var provider2:Provider = Provider(_ctx.provider2);
		
		if (componentName == "tideService" && op.indexOf("applyChangeSet") == 0) {
			if (!(params[0] is ChangeSet))
				return buildFault("Illegal.Argument");
			
			var patientb:Patient7 = new Patient7();
			patientb.id = patient.id;
			patientb.uid = patient.uid;
			patientb.version = patient.version+1;
			patientb.name = patient.name;
			patientb.providers = new PersistentSet(false);
			
			var pproviderb:PatientProvider = new PatientProvider();
			pproviderb.uid = params[0].changes[0].changes.providers.changes[1].value.uid;
			pproviderb.id = 2;
			pproviderb.version = 0;
			pproviderb.name = "blo";
			pproviderb.patient = patientb;
			// patientb.consents.addItem(consentb);
			
			var pproviderref:ChangeRef = new ChangeRef(Type.forClass(PatientProvider).name, 
				params[0].changes[0].changes.providers.changes[0].value.uid, params[0].changes[0].changes.providers.changes[0].value.id);
			
			var change:Change = new Change(Type.forClass(Patient7).name, patientb.uid, patientb.id, 0);
			var collChanges:CollectionChanges = new CollectionChanges();
			collChanges.addChange(-1, null, pproviderref);
			collChanges.addChange(1, null, pproviderb);
			change.changes.providers = collChanges;
			
			return buildResult(null, null, [[ 'PERSIST', pproviderb ], [ 'UPDATE', new ChangeSet([ change ]) ], [ 'REMOVE', pproviderref ]]);
		}
		
		return buildFault("Server.Error");
	}
}
