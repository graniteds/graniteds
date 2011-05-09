package org.granite.channels
{
import flash.utils.ByteArray;
import org.granite.util.ClassUtil;
import org.granite.util.GraniteClassRegistry;

import flash.utils.Timer;
import flash.net.NetConnection;
import flash.events.TimerEvent;

import mx.core.mx_internal;
import mx.messaging.channels.NetConnectionChannel;
import mx.messaging.events.ChannelEvent;
import mx.messaging.events.ChannelFaultEvent;
import mx.messaging.MessageAgent;
import mx.messaging.MessageResponder;
import mx.messaging.messages.IMessage;
import mx.messaging.messages.AcknowledgeMessage;
import mx.messaging.messages.AsyncMessage;
import mx.messaging.messages.CommandMessage;
import mx.messaging.messages.ErrorMessage;
import mx.resources.IResourceManager;
import mx.resources.ResourceManager;
import mx.utils.StringUtil;


[ResourceBundle("messaging")]

/**
 *  @private
 *  This class provides the responder level interface for dispatching message
 *  results from a remote destination.
 *  The NetConnectionChannel creates this handler to manage
 *  the results of a pending operation started when a message is sent.
 *  The message handler is always associated with a MessageAgent
 *  (the object that sent the message) and calls its <code>fault()</code>,
 *  <code>acknowledge()</code>, or <code>message()</code> method as appopriate.
 */
class GraniteMessageResponder extends MessageResponder
{
    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------

    /**
     *  Initializes this instance of the message responder with the specified
     *  agent.
     *
     *  @param agent MessageAgent that this responder should call back when a
     *            message is received.
     *
     *  @param msg The outbound message.
     *
     *  @param channel The channel this responder is using.
     */
    public function GraniteMessageResponder(agent:MessageAgent,
                                    msg:IMessage, channel:GraniteChannel)
    {
        super(agent, msg, channel);
        channel.addEventListener(ChannelEvent.DISCONNECT, channelDisconnectHandler);
        channel.addEventListener(ChannelFaultEvent.FAULT, channelFaultHandler);
    }

    //--------------------------------------------------------------------------
    //
    // Variables
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     */
    private var handled:Boolean;
    private static const BYTEARRAY_BODY_HEADER:String = "GDS_BYTEARRAY_BODY";

    /**
     *  @private
     */
    private var resourceManager:IResourceManager = ResourceManager.getInstance();

    //--------------------------------------------------------------------------
    //
    // Overridden Methods
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     *  Called when the result of sending a message is received.
     *
     *  @param msg NetConnectionChannel-specific message data.
     */
    override protected function resultHandler(msg:IMessage):void
    {

        // Unpack the body of a ByteArray
        if (msg.headers[BYTEARRAY_BODY_HEADER]) {
            GraniteClassRegistry.useClasses(msg.destination);
	        msg.body = ByteArray(msg.body).readObject();
            delete msg.headers[BYTEARRAY_BODY_HEADER];
        }

        if (handled)
            return;

        disconnect();
        if (msg is AsyncMessage)
        {
            if (AsyncMessage(msg).correlationId == message.messageId)
            {
                agent.acknowledge(msg as AcknowledgeMessage, message);
            }
            else
            {
                errorMsg = new ErrorMessage();
                errorMsg.faultCode = "Server.Acknowledge.Failed";
                errorMsg.faultString = resourceManager.getString(
                    "messaging", "ackFailed");
                errorMsg.faultDetail = resourceManager.getString(
                    "messaging", "ackFailed.details",
                    [ message.messageId, AsyncMessage(msg).correlationId ]);
                errorMsg.correlationId = message.messageId;
                agent.fault(errorMsg, message);
                //@TODO: need to add constants here
            }
        }
        else
        {
            var errorMsg:ErrorMessage;
            errorMsg = new ErrorMessage();
            errorMsg.faultCode = "Server.Acknowledge.Failed";
            errorMsg.faultString = resourceManager.getString(
                "messaging", "noAckMessage");
            errorMsg.faultDetail = resourceManager.getString(
                "messaging", "noAckMessage.details",
                [ msg ? msg.toString() : "null" ]);
            errorMsg.correlationId = message.messageId;
            agent.fault(errorMsg, message);
        }
    }

    /**
     *  @private
     *  Called when the current invocation fails.
     *  Passes the fault information on to the associated agent that made
     *  the request.
     *
     *  @param msg NetConnectionMessageResponder status information.
     */
    override protected function statusHandler(msg:IMessage):void
    {
        if (handled)
            return;

        disconnect();

        // even a fault is still an acknowledgement of a message sent so pass it on...
        if (msg is AsyncMessage)
        {
            if (AsyncMessage(msg).correlationId == message.messageId)
            {
                // pass the ack on...
                var ack:AcknowledgeMessage = new AcknowledgeMessage();
                ack.correlationId = AsyncMessage(msg).correlationId;
                ack.headers[AcknowledgeMessage.ERROR_HINT_HEADER] = true; // add a hint this is for an error
                agent.acknowledge(ack, message);
                // send the fault on...
                agent.fault(msg as ErrorMessage, message);
            }
            else if (msg is ErrorMessage)
            {
                // we can't find a correlation id but do have some sort of error message so just forward
                agent.fault(msg as ErrorMessage, message);
            }
            else
            {
                var errorMsg:ErrorMessage;
                errorMsg = new ErrorMessage();
                errorMsg.faultCode = "Server.Acknowledge.Failed";
                errorMsg.faultString = resourceManager.getString(
                    "messaging", "noErrorForMessage");
                errorMsg.faultDetail = resourceManager.getString(
                    "messaging", "noErrorForMessage.details",
                    [ message.messageId, AsyncMessage(msg).correlationId ]);
                errorMsg.correlationId = message.messageId;
                agent.fault(errorMsg, message);
            }
        }
        else
        {
            errorMsg = new ErrorMessage();
            errorMsg.faultCode = "Server.Acknowledge.Failed";
            errorMsg.faultString = resourceManager.getString(
                "messaging", "noAckMessage");
            errorMsg.faultDetail = resourceManager.getString(
                "messaging", "noAckMessage.details",
                [ msg ? msg.toString(): "null" ]);
            errorMsg.correlationId = message.messageId;
            agent.fault(errorMsg, message);
        }
    }

    //--------------------------------------------------------------------------
    //
    // Overridden Protected Methods
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     *  Handle a request timeout by removing ourselves as a listener on the
     *  NetConnection and faulting the message to the agent.
     */
    override protected function requestTimedOut():void
    {
        statusHandler(createRequestTimeoutErrorMessage());
    }

    //--------------------------------------------------------------------------
    //
    // Protected Methods
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     *  Handles a disconnect of the underlying Channel before a response is
     *  returned to the responder.
     *  The sent message is faulted and flagged with the ErrorMessage.MESSAGE_DELIVERY_IN_DOUBT
     *  code.
     *
     *  @param event The DISCONNECT event.
     */
    protected function channelDisconnectHandler(event:ChannelEvent):void
    {
        if (handled)
            return;

        disconnect();
        var errorMsg:ErrorMessage = new ErrorMessage();
        errorMsg.correlationId = message.messageId;
        errorMsg.faultString = resourceManager.getString(
            "messaging", "deliveryInDoubt");
        errorMsg.faultDetail = resourceManager.getString
            ("messaging", "deliveryInDoubt.details");
        errorMsg.faultCode = ErrorMessage.MESSAGE_DELIVERY_IN_DOUBT;
        errorMsg.rootCause = event;
        agent.fault(errorMsg, message);
    }

    /**
     *  @private
     *  Handles a fault of the underlying Channel before a response is
     *  returned to the responder.
     *  The sent message is faulted and flagged with the ErrorMessage.MESSAGE_DELIVERY_IN_DOUBT
     *  code.
     *
     *  @param event The ChannelFaultEvent.
     */
    protected function channelFaultHandler(event:ChannelFaultEvent):void
    {
        if (handled)
            return;

        disconnect();
        var errorMsg:ErrorMessage = event.createErrorMessage();
        errorMsg.correlationId = message.messageId;
        // if the channel is no longer connected then we don't really
        // know if the message made it to the server.
        if (!event.channel.connected)
        {
            errorMsg.faultCode = ErrorMessage.MESSAGE_DELIVERY_IN_DOUBT;
        }
        errorMsg.rootCause = event;
        agent.fault(errorMsg, message);
    }

    //--------------------------------------------------------------------------
    //
    // Private Methods
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     *  Disconnects the responder from the underlying Channel.
     */
    private function disconnect():void
    {
        handled = true;
        channel.removeEventListener(ChannelEvent.DISCONNECT, channelDisconnectHandler);
        channel.removeEventListener(ChannelFaultEvent.FAULT, channelFaultHandler);
    }
}
}