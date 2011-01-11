package 
{

import mx.resources.ResourceBundle;

[ExcludeClass]

public class en_US$rpc_properties extends ResourceBundle
{

    public function en_US$rpc_properties()
    {
		 super("en_US", "rpc");
    }

    override protected function getContent():Object
    {
        var content:Object =
        {
            "noBaseSchemaAddress": "Cannot resolve relative schema import without a fully qualified base address.",
            "unrecognizedPortTypeName": "The WSDL parser couldn't find a portType named '{0}' in namespace '{1}'",
            "noListenerForHeader": "No event listener for header {0}",
            "cannotConnectToDestination": "Couldn't establish a connection to '{0}'",
            "unexpectedException": "Runtime exception {0}",
            "noServiceElement.details": "No <wsdl:service> elements found in WSDL at {0}.",
            "unableToLoadWSDL": "Unable to load WSDL. If currently online, please verify the URI and/or format of the WSDL ({0})",
            "errorWhileLoadingFromParent": "Error while loading imported schema from parent location: {0}",
            "unexpectedSchemaException": "Error while importing schema: {0}",
            "unrecognizedBindingName": "The WSDL parser couldn't find a binding named '{0}' in namespace '{1}'",
            "cannotResetService": "Cannot reset the service of an Operation",
            "mustSpecifyWSDLLocation": "You must specify the WSDL location with useProxy set to false.",
            "urlNotSpecified": "A URL must be specified with useProxy set to false.",
            "unexpectedInputParameter": "Unexpected parameter '{0}' found in input arguments.",
            "noBaseWSDLAddress": "Cannot resolve relative WSDL import without a fully qualified base address.",
            "noListenerForEvent": "An event was received for which no listener was defined. Please add an event listener. {0}",
            "unknownSchemaVersion": "Unknown schema version",
            "missingInputParameter": "Array of input arguments did not contain a required parameter at position {0}",
            "unrecognizedNamespace": "The WSDL parser had no registered document for the namespace '{0}'",
            "multiplePortsFound": "A valid port was not specified. Unable to select a default port as there are multiple ports in the WSDL file.",
            "wsdlDefinitionsNotFirst": "Definitions must be the first element in a WSDL document",
            "xmlEncodeReturnNoXMLNode": "xmlEncode did not return XMLNode",
            "xmlDecodeReturnNull": "xmlDecode returned null",
            "tooFewInputParameters": "Too few parameters - expected at least {0} but found {1}",
            "noPortsInWSDL": "There are no valid ports in the WSDL file for the {0} service.",
            "invalidResultFormat": "Invalid resultFormat '{0}' valid formats are [{1}, {2}, {3}, {4}, {5}]",
            "unrecognizedMessageName": "The WSDL parser couldn't find a message named '{0}' in namespace '{1}'",
            "operationsNotAllowedInService": "Cannot assign operations into an RPC Service ({0})",
            "badSchemaNode": "Bad schema node",
            "noSuchServiceInWSDL": "The requested service '{0}' was not found in the WSDL file.",
            "destinationOrWSDLNotSpecified": "A destination and/or WSDL must be specified.",
            "noBaseWSDLAddress.details": "Please specify the location of the WSDL document for the WebService.",
            "missingInputParameterWithName": "Required parameter '{0}' not found in input arguments.",
            "badElement": "Element {0}:{1} not resolvable",
            "overloadedOperation": "The WSDL contains an overloaded operation ({0}) - we do not currently support this usage.",
            "defaultDecoderFailed": "Default decoder could not decode result",
            "faultyWSDLFormat": "Faulty WSDL format",
            "soapVersionMismatch": "Request implements version: {0}, Response implements version {1}",
            "badType": "Type {0} not resolvable",
            "noSuchService": "Couldn't find service '{0}'",
            "cannotResetOperationName": "Cannot reset the name of an Operation",
            "unknownSchemaElement": "Unknown element: {0}",
            "pendingCallExists": "Attempt to invoke while another call is pending.  Either change concurrency options or avoid multiple calls.",
            "noServiceAndPort": "Couldn't find a matching port (service = '{0}', port = '{1}')",
            "noServices": "There are no valid services in the WSDL file.",
            "unknownProtocol": "Unknown protocol '{0}'",
            "unknownSchemaType": "Unknown schema type system",
            "invalidSoapResultFormat": "Invalid resultFormat '{0}'. Valid formats are 'object', 'xml', and 'e4x'",
            "xmlEncodeReturnNull": "xmlEncode returned null",
            "cannotFindType": "Cannot find type for: {0}",
            "noServiceElement": "Could not load WSDL"
        };
        return content;
    }
}



}
