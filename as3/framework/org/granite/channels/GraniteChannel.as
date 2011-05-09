package org.granite.channels
{

import mx.messaging.channels.AMFChannel
import mx.messaging.MessageAgent;
import mx.messaging.messages.IMessage;
import mx.messaging.messages.CommandMessage;
import mx.messaging.MessageResponder;
import flash.utils.ByteArray;
import org.granite.util.ClassUtil;
import org.granite.util.GraniteClassRegistry;

public class GraniteChannel extends AMFChannel {

    private static const BYTEARRAY_BODY_HEADER:String = "GDS_BYTEARRAY_BODY";

    public function GraniteChannel(id:String = null, uri:String = null)
    {
        super(id, uri);
    }

    /**
     *  @private
     */
    override public function send(agent:MessageAgent, message:IMessage):void
    {
        // Pack the body in a ByteArray (avoid direct deserialization on the server)
        if (message && !(message is CommandMessage) && message.body != null) {

            // Register classes following the destination
            var destination : String = message.destination;
            if (destination.length == 0)
                destination = agent.destination;
            GraniteClassRegistry.useClasses(destination);

            message.headers[BYTEARRAY_BODY_HEADER] = true;
            var data:ByteArray = new ByteArray();
            data.writeObject(message.body);
            message.body = data;
        }

        super.send(agent, message);
    }


    override protected function getDefaultMessageResponder(agent:MessageAgent, msg:IMessage):MessageResponder
    {
        return new GraniteMessageResponder(agent, msg, this);
    }
}
}