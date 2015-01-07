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
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    import mx.utils.ObjectUtil;
    
    import org.granite.collections.BasicMap;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestCollectionEnumGDS588 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testCollectionEnumGDS588():void {
        	var sal1:Salutation = Salutation.Mr;
        	var sal2:Salutation = Salutation.Ms;
			
			var sal:Salutation;
			
			var c:ArrayCollection = new ArrayCollection([sal1, sal2]);
			var d:ArrayCollection = ObjectUtil.copy(c) as ArrayCollection;
			d = _ctx.meta_mergeExternalData(d) as ArrayCollection;
			for each (sal in d)
				Assert.assertTrue("Salutation " + sal.name, Salutation.constants.indexOf(sal) >= 0);
				
            var q:BasicMap = new BasicMap();
			q.put(sal1, "Zozo");
			q.put(sal2, "Zuzu");
			var r:BasicMap = ObjectUtil.copy(q) as BasicMap;
			r = _ctx.meta_mergeExternalData(r) as BasicMap;
			for each (sal in r.keySet)
				Assert.assertTrue("Salutation " + sal.name, Salutation.constants.indexOf(sal) >= 0);
        }
    }
}
