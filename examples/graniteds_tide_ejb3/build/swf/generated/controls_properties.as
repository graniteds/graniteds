package 
{

import mx.resources.ResourceBundle;

[ExcludeClass]

public class en_US$controls_properties extends ResourceBundle
{

    public function en_US$controls_properties()
    {
		 super("en_US", "controls");
    }

    override protected function getContent():Object
    {
        var content:Object =
        {
            "undefinedParameter": "CuePoint parameter undefined.",
            "nullURL": "Null URL sent to VideoPlayer.load.",
            "incorrectType": "Type must be 0, 1 or 2.",
            "okLabel": "OK",
            "noLabel": "No",
            "wrongNumParams": "Num params must be number.",
            "wrongDisabled": "Disabled must be number.",
            "wrongTime": "Time must be number.",
            "dayNamesShortest": "S,M,T,W,T,F,S",
            "wrongType": "Type must be number.",
            "firstDayOfWeek": "0",
            "rootNotSMIL": "URL: '{0}' Root node not smil: '{1}'.",
            "errorMessages": "Unable to make connection to server or to find FLV on server.,No matching cue point found.,Illegal cue point.,Invalid seek.,Invalid contentPath.,Invalid XML.,No bitrate match; must be no default FLV.,Cannot delete default VideoPlayer.",
            "unexpectedEnd": "Unexpected end of cuePoint param string.",
            "rootNotFound": "URL: '{0}' No root node found; if file is an flv, it must have a .flv extension.",
            "errWrongContainer": "ERROR: The dataProvider of '{0}' must not contain objects of type flash.display.DisplayObject.",
            "invalidCall": "Cannot call reconnect on an http connection.",
            "cancelLabel": "Cancel",
            "errWrongType": "ERROR: The dataProvider of '{0}' must be String, ViewStack, Array, or IList.",
            "badArgs": "Bad args to _play.",
            "missingRoot": "URL: '{0}' No root node found; if URL is for an FLV, it must have a .flv extension and take no parameters.",
            "notLoadable": "Unable to load '{0}'.",
            "wrongName": "Name cannot be undefined or null.",
            "wrongTimeName": "Time must be number and/or name must not be undefined or null.",
            "yesLabel": "Yes",
            "undefinedArray": "CuePoint.array undefined.",
            "missingProxy": "URL: '{0}' fpad xml requires proxy tag.",
            "unknownInput": "Unknown inputType '{0}'.",
            "missingAttributeSrc": "URL: '{0}' Attribute src is required in '{1}' tag.",
            "yearSymbol": "",
            "wrongIndex": "CuePoint.index must be number between -1 and cuePoint.array.length.",
            "notImplemented": "'{0}' not implemented yet.",
            "label": "LOADING %3%%",
            "wrongFormat": "Unexpected cuePoint parameter format.",
            "tagNotFound": "URL: '{0}' At least one video of ref tag is required.",
            "unsupportedMode": "IMEMode '{0}' not supported.",
            "cannotDisable": "Cannot disable actionscript cue points.",
            "missingAttributes": "URL: '{0}' Tag '{1}' requires attributes id, width, and height. Width and height must be numbers greater than or equal to 0.",
            "notfpad": "URL: '{0}' Root node not fpad."
        };
        return content;
    }
}



}
