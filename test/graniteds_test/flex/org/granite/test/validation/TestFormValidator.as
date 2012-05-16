package org.granite.test.validation
{
	import org.flexunit.Assert;
	import org.fluint.uiImpersonation.UIImpersonator;

	
	public class TestFormValidator {
		
		[Test]
		public function testFormValidatorTextArea():void {
			var model:Model = new Model();
			model.field = new Field();
			
			var form:Form1 = new Form1();
			form.model = model;
			UIImpersonator.addChild(form);
			
			form.textArea1.text = "test";			
			
			Assert.assertFalse("Text 1 not validated", form.fv.validateEntity());
			
			form.textArea1.text = "testLongEnough";			

			Assert.assertTrue("Text 1 validated", form.fv.validateEntity());
			
			form.textArea2.text = "test";			
			
			Assert.assertFalse("Text 2 not validated", form.fv.validateEntity());
			
			form.textArea2.text = "testLongEnough";			
			
			Assert.assertTrue("Text 2 validated", form.fv.validateEntity());
			
			form.textArea3.text = "test";			
			
			Assert.assertFalse("Text 3 not validated", form.fv.validateEntity());
			
			form.textArea3.text = "testLongEnough";			
			
			Assert.assertTrue("Text 3 validated", form.fv.validateEntity());
			
			form.textArea4.text = "test";			
			
			Assert.assertFalse("Text 4 not validated", form.fv.validateEntity());
			
			form.textArea4.text = "testLongEnough";			
			
			Assert.assertTrue("Text 4 validated", form.fv.validateEntity());
		}
	}
}