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
		
		[Test]
		public function testFormValidatorDeep():void {
			var model:Model = new Model();
			model.field = new Field();
			
			var form:Form2 = new Form2();
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
			
		}
			
	}
}