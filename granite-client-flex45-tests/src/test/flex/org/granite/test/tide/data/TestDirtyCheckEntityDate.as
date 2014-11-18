/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    import mx.utils.ObjectUtil;
    
    import org.flexunit.Assert;
    import org.granite.persistence.PersistentSet;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestDirtyCheckEntityDate
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public var ctxDirty:Boolean;
        public var patientDirty:Boolean;
        
        [Test]
        public function testDirtyCheckEntityDate():void {
        	var patient:Patient4 = new Patient4();
			patient.uid = "P1";
			patient.version = 0;
			patient.id = 1;
			patient.name = "test";
			
			var visit:Visit3 = new Visit3();
			visit.uid = "V1";
			visit.version = 0;
			visit.id = 1;
			visit.patient = patient;
			visit.name = "test";
			patient.visits = new PersistentSet();
			patient.visits.addItem(visit);
        	
        	BindingUtils.bindProperty(this, "ctxDirty", _ctx, "meta_dirty");
        	BindingUtils.bindProperty(this, "patientDirty", patient, "meta_dirty");
        	
        	patient = _ctx.patient = _ctx.meta_mergeExternalData(patient);
        	
        	Assert.assertFalse("Patient not dirty", _ctx.meta_isEntityChanged(patient));
        	
			visit = patient.visits.getItemAt(0) as Visit3;
			
			visit.dateUTC = new Date(2014, 11, 17, 23, 23, 23, 989);
			
			Assert.assertTrue("Patient dirty", _ctx.meta_dirty);
			
			var patientb:Patient4 = ObjectUtil.copy(patient) as Patient4;
			patientb.visits.getItemAt(0).version = 1;
			
			_ctx.meta_mergeExternalData(patientb);
        	
        	Assert.assertFalse("Context not dirty", ctxDirty);
        }
    }
}
