package org.granite.test.tide.data
{
    import flash.events.Event;
    import flash.system.System;
    import flash.utils.getQualifiedClassName;
    
    import mx.collections.ArrayCollection;
    import mx.core.FlexGlobals;
    import mx.events.CollectionEvent;
    import mx.events.PropertyChangeEvent;
    import mx.rpc.events.ResultEvent;
    
    import org.flexunit.Assert;
    import org.flexunit.async.Async;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Contact;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.collections.PersistentCollection;
    
    import spark.components.Application;


	public class TestMergeEntityRemove
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }

		
        [Test(async)]
        public function testMergeEntityRemove():void {
			var a:ObjectA = new ObjectA();
			a.id = 1;
			a.uid = "A1";
			a.version = 0;
			a.name = "A1";
			var b:ObjectB = new ObjectB();
			b.id = 1;
			b.uid = "B1";
			b.version = 0;
			b.name = "B1";
			b.objectA = a;
			a.objectB = b;
			var c1:ObjectC = new ObjectC();
			c1.id = 1;
			c1.uid = "C1";
			c1.version = 0;
			c1.objectB = b;
			b.list = new PersistentSet();
			b.list.addItem(c1);
			
			_ctx.meta_mergeExternalData(a);
			_ctx.meta_clearCache();
			
			var a2:ObjectA = new ObjectA();
			a2.name = a.name;
			a2.objectB = b;
			
			_ctx.meta_mergeExternal(a2);			
			_ctx.meta_clearCache();
			
			FlexGlobals.topLevelApplication.callLater(Async.asyncHandler(this, testMergeEntityRemoveAfter, 1000, a));
			
			System.gc();
		}
		
		private function testMergeEntityRemoveAfter(event:Event = null, pass:Object = null):void {
			var na:ObjectA = new ObjectA();
			na.id = 1;
			na.uid = "A1";
			na.version = 1;
			na.name = "A1";
			na.objectB = nb;
			
			var nb:ObjectB = new ObjectB();
			nb.id = 1;
			nb.uid = "B1";
			nb.version = 0;
			nb.name = "B1";
			nb.objectA = na;
			nb.list = new PersistentSet();
			var nc1:ObjectC = new ObjectC();
			nc1.id = 1;
			nc1.uid = "C1";
			nc1.version = 0;
			nc1.objectB = nb;
			
			_ctx.meta_mergeExternalData(na, null, null, [ nb, nc1 ], null);
        }
    }

}
