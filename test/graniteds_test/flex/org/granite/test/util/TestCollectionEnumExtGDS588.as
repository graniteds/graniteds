/*
GRANITE DATA SERVICES
Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

This file is part of Granite Data Services.

Granite Data Services is free software; you can redistribute it and/or modify
it under the terms of the GNU Library General Public License as published by
the Free Software Foundation; either version 2 of the License, or (at your
option) any later version.

Granite Data Services is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
for more details.

You should have received a copy of the GNU Library General Public License
along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.test.util {

	import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    import mx.utils.ObjectUtil;
    
    import org.granite.collections.BasicMap;
    
    
    public class TestCollectionEnumExtGDS588
    {
        [Test]
        public function testCollectionEnumExtGDS588():void {
        	var sal1:Salutation = Salutation.Mr;
        	var sal2:Salutation = Salutation.Ms;
			
			var sal:Salutation;
			
			var c:ArrayCollection = new ArrayCollection([sal1, sal2]);
			var d:ArrayCollection = ObjectUtil.copy(c) as ArrayCollection;
			for each (sal in d)
				Assert.assertTrue("Salutation " + sal.name, Salutation.constants.indexOf(sal) >= 0);
				
            var q:BasicMap = new BasicMap();
			q.put(sal1, "Zozo");
			q.put(sal2, "Zuzu");
			var r:BasicMap = ObjectUtil.copy(q) as BasicMap;
			for each (sal in r.keySet)
				Assert.assertTrue("Salutation " + sal.name, Salutation.constants.indexOf(sal) >= 0);
        }
    }
}
