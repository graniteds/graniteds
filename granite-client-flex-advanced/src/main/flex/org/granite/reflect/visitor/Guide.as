/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.reflect.visitor {

	import flash.utils.Dictionary;
	
	import org.granite.reflect.Type;
	import org.granite.reflect.visitor.handlers.BeanHandler;
	import org.granite.reflect.visitor.handlers.CollectionHandler;
	import org.granite.reflect.visitor.handlers.DictionaryHandler;
	import org.granite.reflect.visitor.handlers.MapHandler;
	import org.granite.reflect.visitor.handlers.NullHandler;
	import org.granite.reflect.visitor.handlers.ObjectHandler;
	import org.granite.reflect.visitor.handlers.PrimitiveHandler;
	import org.granite.reflect.visitor.handlers.VectorHandler;

	/**
	 * The <code>Guide</code> class is the entry point of the visitor pattern
	 * implementation. It uses an array of <code>IHandler</code>s in order to
	 * explore and expose to a visitor the properties of a given bean.
	 * Exploring a beans graph is a sequence of two-phases processes: for each
	 * bean, first, the visitor is asked if it accepts to visit each property
	 * of the current bean; second, for each accepted property of this bean,
	 * the property is visited and cascaded.
	 * 
	 * <p>
	 * A <code>Guide</code> instance is not intended to be used more than once.
	 * Any attempt to reuse an already used intance will throw an error.
	 * </p>
	 * 
	 * <p>
	 * A typical usage would be:
	 * </p>
	 * 
	 * <listing>
	 * var guide:Guide = new Guide(new MyVisitorImpl());
	 * guide.explore(myBean);</listing>
	 * 
	 * @author Franck WOLFF
	 * 
	 * @see IHandler
	 * @see IVisitor
	 */
	public class Guide {
		
		/**
		 * The constant for "dictionary" recursion strategy: an instance of a
		 * bean is only visited once, even if it appears several times in an
		 * objects graph (this is the default). 
		 */
		public static const RS_DICTIONARY:String = "dictionary";
		
		/**
		 * The constant for "stack" recursion strategy: an instance of a
		 * bean is visited if and only if it wasn't already visited in the
		 * current path fo the objects graph.  
		 */
		public static const RS_STACK:String = "stack";

		/**
		 * @private
		 */
		private static const _NULL_HANDLER:NullHandler = new NullHandler();
		
		/**
		 * Order is important!
		 * @private
		 */
		private static const _DEFAULT_HANDLERS:Array = [
			new PrimitiveHandler(),
			new CollectionHandler(),
			new VectorHandler(),
			new DictionaryHandler(),
			new MapHandler(),
			new ObjectHandler(),
			new BeanHandler()
		];
		
		/**
		 * The array of the default handlers (not including the
		 * <code>NullHandler</code>)used when visiting a given bean.
		 * This array may be modified in order to globally add a specific
		 * handler (or replace an existing one).
		 */
		public static function get defaultHandlers():Array {
			return _DEFAULT_HANDLERS;
		}
		
		/**
		 * @private
		 */
		private var _visitor:IVisitor;

		/**
		 * @private
		 */
		private var _handlers:Array;
		
		/**
		 * @private
		 */
		private var _recursionStrategy:String;
		
		/**
		 * @private
		 */
		private var _context:Object;
		
		/**
		 * Constructs a new <code>Guide</code> that will explore a given bean and
		 * expose its properties to the supplied visitor implementation.
		 * 
		 * @param visitor a <code>IVisitor</code> implementation.
		 * @param handlers an array of specific handlers to insert between the
		 * 		<code>NullHandler</code> and the default handlers.
		 * @param recursionStrategy the recursion strategy to be used when visiting a
		 * 		bean (must be RS_DICTIONARY or RS_STACK). Default is RS_DICTIONARY.
		 * 
		 * @see org.granite.reflect.visitor.handlers.NullHandler
		 * @see #RS_DICTIONARY
		 */
		function Guide(visitor:IVisitor, handlers:Array = null, recursionStrategy:String = RS_DICTIONARY) {

			// Check and save visitor
			if (visitor == null)
				throw new ArgumentError("Parameter visitor cannot be null");
			_visitor = visitor;
			
			// Check and configure handlers
			_handlers = new Array();
			_handlers.push(_NULL_HANDLER);
			if (handlers != null) {
				for each (var handler:IHandler in handlers)
					_handlers.push(handler);
			}
			for each (handler in _DEFAULT_HANDLERS)
				_handlers.push(handler);

			// Check and save recursion startegy
			if ([RS_DICTIONARY, RS_STACK].indexOf(recursionStrategy) == -1)
				throw new ArgumentError("Unknown recursion startegy type: " + recursionStrategy);
			_recursionStrategy = recursionStrategy;
		}
		
		/**
		 * The <code>IVisitor</code> implementation used by this <code>Guide</code> instance.
		 */
		public function get visitor():IVisitor {
			return _visitor;
		}
		
		/**
		 * The array <code>IHandlers</code> used by this <code>Guide</code> instance.
		 */
		public function get handlers():Array {
			return _handlers;
		}
		
		/**
		 * The recursion strategy used by this <code>Guide</code> instance.
		 */
		public function get recursionStrategy():String {
			return _recursionStrategy;
		}

		/**
		 * Explore the supplied bean, exposing its properties to the current visitor.
		 * 
		 * <p>
		 * This method is a shortcut for:
		 * <code>exploreVisitable(new Visitable(null, Type.forInstance(o), o))</code>.
		 * </p>
		 * 
		 * @param o the bean to explore.
		 */
		public function explore(o:*):void {
			if (o == null || o == undefined)
				return;

			exploreVisitable(new Visitable(null, Type.forInstance(o), o));
		}

		/**
		 * Explore the supplied <code>Visitable</code>, exposing its properties to the
		 * current visitor.
		 * 
		 * @param visitable the visitable to explore.
		 */
		public function exploreVisitable(visitable:Visitable):void {
			if (visitable == null)
				return;

			if (_context != null)
				throw new Error("Guide instance is currently in use");
			
			if (_recursionStrategy == RS_DICTIONARY)
				_context = new Dictionary();
			else
				_context = new Array();

			try {
				if (_visitor.accept(visitable))
					accept(_visitor, visitable);
			}
			catch (e:InterruptVisitError) {
				// visit interrupted by visitor.
			}
			_context = null;
		}
		
		/**
		 * Checks if the supplied parameter is already in the recursion context (ie. already
		 * visited).
		 * 
		 * @param value the value to check against the current recursion context.
		 * @return <code>true</code> if value is already in the recursion context, <code>false</code>
		 * 		otherwise. 
		 */
		protected function isInContext(value:*):Boolean {
			if (_context is Dictionary)
				return (_context as Dictionary).hasOwnProperty(value);
			return ((_context as Array).indexOf(value) != 1);
		}
		
		/**
		 * Put the supplied parameter in the recursion context (so it is marked as already visited).
		 * 
		 * @param value the value to put in the recursion context.
		 */
		protected function pushInContext(value:*):void {
			if (_context is Dictionary)
				(_context as Dictionary)[value] = true;
			else
				(_context as Array).push(value);
		}
		
		/**
		 * Remove the supplied parameter from the recursion context (this method does nothing when
		 * the recursion strategy is "dictionary").
		 * 
		 * @param value the value to remove from the recursion context.
		 */
		protected function popFromContext(value:*):void {
			if (_context is Array)
				(_context as Array).pop();
		}
		
		/**
		 * Try to find a <code>IHandler</code> instance that will handle the visit of the visitable
		 * item by the visitor object.
		 * 
		 * @param visitor the <code>IVisitor</code> that will visit the item.
		 * @param visitable the <code>Visitable</code> item to visit.
		 */
		protected function accept(visitor:IVisitor, visitable:Visitable):void {

			function filter(visitable:Visitable):Boolean {
				return visitor.accept(visitable);
			}
			
			if (visitor.visit(visitable) && !isInContext(visitable.value)) {

				pushInContext(visitable.value);

				var children:Array = null;
				for each (var handler:IHandler in _handlers) {
					if (handler.canHandle(visitable)) {
						children = handler.handle(visitable, filter);
						break;
					}
				}
				
				if (children != null && children.length > 0) {
					for each (var child:Visitable in children)
						accept(visitor, child);
				}

				popFromContext(visitable.value);
			}
		}
	}
}