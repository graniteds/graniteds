package 
{

import mx.resources.ResourceBundle;

[ExcludeClass]

public class en_US$states_properties extends ResourceBundle
{

    public function en_US$states_properties()
    {
		 super("en_US", "states");
    }

    override protected function getContent():Object
    {
        var content:Object =
        {
            "alreadyParented": "Cannot add a child that is already parented."
        };
        return content;
    }
}



}
