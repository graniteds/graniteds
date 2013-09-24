/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.tide.data {

	import mx.data.utils.Managed;
	import mx.logging.ILogger;
    import mx.logging.Log;

	import org.granite.math.IllegalArgumentError;
	import org.granite.reflect.Annotation;
	import org.granite.reflect.Method;

	import org.granite.reflect.Type;
    import org.granite.tide.BaseContext;
    import org.granite.tide.EntityDescriptor;
    import org.granite.tide.IArgumentPreprocessor;
    import org.granite.tide.IEntity;

    /**
     * @author William DRAI
     */
    public class ChangeArgumentPreprocessor implements IArgumentPreprocessor {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.data.ChangeArgumentPreprocessor");

        public function preprocess(method:Method, args:Array):Array {
            if (method == null)
                return args;
			var lazys:Array = method.getAnnotationsNoCache("Lazy");
			if (lazys == null || lazys.length == 0)
				return args;
			var all:Boolean = false;
			var indices:Array = [];
			for each (var lazy:Annotation in lazys) {
				if (lazy.getArgValue("") == null || lazy.getArgValue("") == "") {
					all = true;
					break;
				}
				indices.push(int(lazy.getArgValue("")));
			}

            var context:BaseContext = null;
            var csb:ChangeSetBuilder = null;
            for (var idx:uint = 0; idx < args.length; idx++) {
                if (args[idx] is IEntity && (all || indices.indexOf(idx) >= 0)) {
                    var entity:IEntity = IEntity(args[idx]);
                    if (context == null) {
                        context = BaseContext(Managed.getEntityManager(entity));
                        csb = new ChangeSetBuilder(context);
                    }
                    else if (Managed.getEntityManager(entity) !== context)
                        throw new IllegalArgumentError("All arguments passed to remote call should be in the same context");
                    if (context != null) {
                        var desc:EntityDescriptor = context.meta_tide.getEntityDescriptor(entity);
                        if (desc.versionPropertyName != null && !isNaN(entity[desc.versionPropertyName])) {
                        	log.debug("Building ChangeSet for argument {0}", BaseContext.toString(entity))
                            args[idx] = csb.buildEntityChangeSet(entity);
                        }
                    }
                }
            }
            return args;
        }
    }
}
