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

package org.granite.seam {

    import mx.utils.ArrayUtil;
    import mx.rpc.remoting.mxml.Operation;

    import org.granite.rpc.remoting.mxml.SecureRemoteObject;

    /*
      This dynamic class extends remote object to provide conversation support for Seam. If
      conversation object is not set passed in then a default one will be used.

      @author Cameron Ingram, Venkat Danda
    */
    public dynamic class SeamRemoteObject extends SecureRemoteObject {

        [Bindable]
        private var _conversation:Conversation;

        [Bindable]
        private var _task:Task;

        /*
          Default constructor creates a default conversation
        */
        public function SeamRemoteObject(destination:String = null) : void {
            super(destination);

            if (conversation == null)
                this._conversation = new Conversation();
            if (task == null)
                this._task = new Task();

            this.showBusyCursor = true;
        }

        /*
          Returns the current conversation object
        */
        public function get conversation(): Conversation {
            return _conversation;
        }

        /*
          Returns the current task object
        */
        public function get task(): Task {
            return _task;
        }

        /*
          This nasty little hack is here because we can't override the
          <mx:method> compiler template. The generated code for <mx:method>
          will use a Operation class, SeamRemoteObject needs to use a SeamOperation to
          handle the conversation passing. This is really only intended to be used
          in mxml. In an .as file the normal setting of the operation should be used because
          if Adobe allows us to create our own templates or override <mx:method> this method will
          eventually go away.
        */
        public function set addOperations(operations:Array) : void {
           var tmpArray:Object = new Object();

           for (var i:int = 0; i  < operations.length; i++) {
                var operation:Operation = operations[i];
                var operationName:String = operation.name;

                if (!tmpArray.hasOwnProperty(operationName)) {
                    tmpArray[operationName] = operation;
                }
           }

           super.operations = tmpArray;
        }


        /*
          Set the converation
        */
        public function set conversation(conversation:Conversation): void {
            _conversation = conversation;
        }

        /*
          Set the task
        */
        public function set task(task:Task): void {
            _task = task;
        }
    }
}