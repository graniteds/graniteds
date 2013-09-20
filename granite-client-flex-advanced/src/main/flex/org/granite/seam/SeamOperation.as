/**
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

package org.granite.seam {

    import mx.rpc.remoting.mxml.Operation;
    import mx.core.mx_internal;
    use namespace mx_internal;
    import mx.rpc.AsyncToken;
    import mx.messaging.messages.IMessage;
    import mx.rpc.remoting.mxml.RemoteObject;
    import mx.rpc.events.ResultEvent;

    /*
      Class that passes the conversationId in the header. The conversation id is retrieved from
      the passed in RemoteObject.

      @author Cameron Ingram, Venkat Danda
    */
    public class SeamOperation extends Operation {

        private const CONVERSATION_TAG: String = "conversationId";
        private const TASKID_TAG: String = "taskId";

        /*
          constructor used to setup the onResult event so that the conversationId can be retrieved.
        */
        public function SeamOperation(svc:RemoteObject = null, name:String = null) : void {

            this.addEventListener(ResultEvent.RESULT,onResult);

            super(svc, name);
        }

        /*
          Overriden invoke so that the conversation id can get passed with every invokation.  For some reason
          RemoteObject will only pass the conversation id once, after that every invokation will not set the header.
          Seems like a bug to me...

        */
        mx_internal override function invoke(msg:IMessage, token:AsyncToken=null):AsyncToken {
            
            var conversation:Conversation = SeamRemoteObject(this.service).conversation;
            msg.headers[CONVERSATION_TAG] = conversation.conversationId;

            var objTask : Task = SeamRemoteObject(this.service).task;
            if (objTask != null && objTask.taskId != null)
                msg.headers[TASKID_TAG] = SeamRemoteObject(this.service).task.taskId;

            return super.invoke(msg, token);
        }

        /*
          Grab the converationId from the returned header
        */
        public function onResult(event:ResultEvent) : void {
            SeamRemoteObject(this.service).conversation.conversationId = event.message.headers[CONVERSATION_TAG];
            if (event.message.headers[TASKID_TAG] != null)
                SeamRemoteObject(this.service).task.taskId = event.message.headers[TASKID_TAG];
        }
    }
}