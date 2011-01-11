package 
{

import mx.resources.ResourceBundle;

[ExcludeClass]

public class en_US$messaging_properties extends ResourceBundle
{

    public function en_US$messaging_properties()
    {
		 super("en_US", "messaging");
    }

    override protected function getContent():Object
    {
        var content:Object =
        {
            "httpRequestError.details": "Error: {0}",
            "errorReadingIExternalizable": "Error encountered while reading IExternalizable. {0}",
            "couldNotLoadCache": "The cache could not be loaded into the message store.",
            "cannotRemoveWhenConfigured": "Channels cannot be removed from a ChannelSet that targets a configured destination.",
            "deliveryInDoubt.details": "Channel disconnected before an acknowledgement was received",
            "requestTimedOut": "Request timed out",
            "securityError": "Security error accessing url",
            "invalidURL": "Invalid URL",
            "messageQueueFailedInitialize": "Message store initialization failed.",
            "noErrorForMessage.details": "Was expecting message '{0}' but received '{1}'.",
            "couldNotSaveCache": "The cache could not be saved.",
            "unknownChannelWithId": "Channel '{0}' does not exist in the configuration.",
            "producerConnectError": "Producer connect error",
            "noAckMessage.details": "Was expecting mx.messaging.messages.AcknowledgeMessage, but received {0}",
            "destinationNotSet": "The MessageAgent's destination must be set to send messages.",
            "requestTimedOut.details": "The request timeout for the sent message was reached without receiving a response from the server.",
            "couldNotRemoveMessageFromQueue": "The message could not be removed from the message store before being sent.",
            "unknownReference": "Unknown reference {0}",
            "noDestinationSpecified": "A destination name must be specified.",
            "wrongMessageQueueForProducerDetails": "The message did not come from the message store associated with this producer.",
            "emptySessionClientId": "Session clientId's must be non-zero in length.",
            "unknownTraitReference": "Unknown trait reference {0}",
            "producerSendErrorDetails": "The producer is not connected and the message cannot be sent.",
            "connectTimedOut": "Connect attempt timed out.",
            "httpRequestError": "HTTP request error",
            "noErrorForMessage": "Didn't receive an error for message",
            "pollingNotSupportedAMF": "StreamingAMFChannel does not support polling. ",
            "pollingNotSupportedHTTP": "StreamingHTTPChannel does not support polling. ",
            "noAvailableChannels": "No Channels are available for use.",
            "ackFailed.details": "Was expecting message '{0}' but received '{1}'.",
            "couldNotLoadCacheIds": "The list of cache ids could not be loaded.",
            "noAMFXBody": "Invalid AMFX packet. Could not find message body",
            "couldNotClearCache": "The cache could not be cleared.",
            "unknownDestination": "Unknown destination '{0}'.",
            "invalidRequestMethod": "Invalid method specified.",
            "failedToSubscribe": "The consumer was not able to subscribe to its target destination.",
            "destinationWithInvalidMessageType": "Destination '{0}' cannot service messages of type '{1}'.",
            "couldNotAddMessageToQueue": "The message store could not store the message and the producer is not connected. The FaultEvent dispatched by the message store provides additional information.",
            "unsupportedAMFXVersion": "Unsupported AMFX version: {0}",
            "producerSendError": "Send failed",
            "noURLSpecified": "No url was specified for the channel.",
            "cannotAddWhenConfigured": "Channels cannot be added to a ChannelSet that targets a configured destination.",
            "unknownChannelClass": "The channel class '{0}' specified was not found.",
            "pollingIntervalNonPositive": "Channel pollingInterval may only be set to a positive value.",
            "cannotAddNullIdChannelWhenClustered": "Cannot add a channel with null id to ChannelSet when its clustered property is true.",
            "securityError.details": "Destination: {0}",
            "referenceMissingId": "A reference must have an id.",
            "sendFailed": "Send failed",
            "lsoStorageNotAllowed": "The message store cannot initialize because local storage is not allowed. Please ensure that local storage is enabled for the Flash Player and that sufficient storage space is configured.",
            "ackFailed": "Didn't receive an acknowledgement of message",
            "cannotSetClusteredWithdNullChannelIds": "Cannot change clustered property of ChannelSet to true when it contains channels with null ids. ",
            "cannotConnectToDestination": "No connection could be made to the message destination.",
            "reconnectIntervalNegative": "reconnectInterval cannot take a negative value.",
            "noURIAllowed": "Error for DirectHTTPChannel. No URI can be specified.",
            "messageQueueSendError": "Send failed",
            "noServiceForMessageType": "No service is configured to handle messages of type '{0}'.",
            "AMFXTraitsNotFirst": "Invalid object. A single set of traits must be supplied as the first entry in an object.",
            "consumerSubscribeError": "Consumer subscribe error",
            "noChannelForDestination": "Destination '{0}' either does not exist or the destination has no channels defined (and the application does not define any default channels.)",
            "emptyDestinationName": "'{0}' is not a valid destination.",
            "failedToConnect": "The producer was not able to connect to its target destination.",
            "noAMFXNode": "Invalid AMFX packet. Content must start with an <amfx> node",
            "resubscribeIntervalNegative": "resubscribeInterval cannot take a negative value.",
            "noAckMessage": "Didn't receive an acknowledge message",
            "pollingRequestNotAllowed": "Poll request made on '{0}' when polling is not enabled.",
            "queuedMessagesNotAllowedDetails": "This producer does not have an assigned message queue so queued messages cannot be sent.",
            "unknownStringReference": "Unknown string reference {0}",
            "authenticationNotSupported": "Authentication not supported on DirectHTTPChannel (no proxy).",
            "deliveryInDoubt": "Channel disconnected",
            "messageQueueNotInitialized": "The message store has not been initialized.",
            "notImplementingIExternalizable": "Class {0} must implement flash.util.IExternalizable.",
            "receivedNull": "Received null.",
            "unknownDestinationForService": "Unknown destination '{1}' for service with id '{0}'."
        };
        return content;
    }
}



}
