package org.granite.test.tide.spring
{
    import mx.collections.ArrayCollection;
    
    import org.flexunit.Assert;
    import org.flexunit.async.Async;
    import org.granite.collections.IPersistentCollection;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.data.Comment;
    import org.granite.test.tide.data.CommentList;
    import org.granite.test.tide.data.Consent;
    import org.granite.test.tide.data.Document;
    import org.granite.test.tide.data.DocumentList;
    import org.granite.test.tide.data.DocumentPayload;
    import org.granite.test.tide.data.Patient5;
    import org.granite.test.tide.data.Patient6;
    import org.granite.tide.BaseContext;
    import org.granite.tide.data.ChangeMerger;
    import org.granite.tide.data.ChangeRef;
    import org.granite.tide.data.ChangeSet;
    import org.granite.tide.data.ChangeSetBuilder;
    import org.granite.tide.data.CollectionChanges;
    import org.granite.tide.events.TideResultEvent;
    
    
    public class TestSpringChangeSetEntityRemote8 {
		
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
		public function testChangeSetEntityRemote12():void {
			var patient:Patient6 = new Patient6();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;
			
			_ctx.patient = _ctx.meta_mergeExternalData(patient);
			_ctx.meta_clearCache();
			
			patient = _ctx.patient as Patient6;
			
			var commentList:CommentList = new CommentList();
			commentList.comments = new ArrayCollection();
			var comment:Comment = new Comment();
			comment.commentList = commentList;
			commentList.comments.addItem(comment);
			patient.commentList = commentList;
			
			// Simulate remote call with local merge of arguments
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			_ctx.tideService.applyChangeSet1(changeSet, commentList, Async.asyncHandler(this, resultHandler, 1000));
		}

		private function resultHandler(event:TideResultEvent, pass:Object = null):void {
			
			var patient:Patient6 = Patient6(_ctx.patient);
			
			Assert.assertTrue("Comment list initialized", IPersistentCollection(patient.commentList.comments).isInitialized());
			Assert.assertEquals("Comment list size", 1, patient.commentList.comments.length);
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
		}
	}
}


import mx.rpc.events.AbstractEvent;

import org.flexunit.Assert;
import org.granite.persistence.PersistentSet;
import org.granite.reflect.Type;
import org.granite.test.tide.data.Comment;
import org.granite.test.tide.data.CommentList;
import org.granite.test.tide.data.Consent;
import org.granite.test.tide.data.Document;
import org.granite.test.tide.data.DocumentList;
import org.granite.test.tide.data.DocumentPayload;
import org.granite.test.tide.data.Patient5;
import org.granite.test.tide.data.Patient6;
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
		var patient:Patient6 = Patient6(_ctx.patient);
		
		if (componentName == "tideService" && op.indexOf("applyChangeSet") == 0) {
			if (!(params[0] is ChangeSet))
				return buildFault("Illegal.Argument");

			var commentListb:CommentList = new CommentList();
			commentListb.id = 1;
			commentListb.version = 0;
			commentListb.uid = params[1].uid;
			commentListb.comments = new PersistentSet();
			var commentb:Comment = new Comment();
			commentb.id = 1;
			commentb.version = 0;
			commentb.uid = Object(params[1].comments.getItemAt(0)).uid;
			commentb.commentList = commentListb;
			commentListb.comments.addItem(commentb);
			
			var change1:Change = new Change(Type.forClass(Patient6).name, patient.uid, patient.id, 1);
			change1.changes.commentList = commentListb;
			
			var change2:Change = new Change(Type.forClass(CommentList).name, commentListb.uid, commentListb.id, commentListb.version);
			var collChanges2:CollectionChanges = new CollectionChanges();
			collChanges2.addChange(1, null, commentb);
			change2.changes.comments = collChanges2;
			
			return buildResult(null, null, [[ 'PERSIST', commentListb ], [ 'PERSIST', commentb ],
				[ 'UPDATE', new ChangeSet([ change1 ]) ], [ 'UPDATE', new ChangeSet([ change2 ]) ]]);
		}
		
		return buildFault("Server.Error");
	}
}
