package 
{

import mx.resources.ResourceBundle;

[ExcludeClass]

public class en_US$skins_properties extends ResourceBundle
{

    public function en_US$skins_properties()
    {
		 super("en_US", "skins");
    }

    override protected function getContent():Object
    {
        var content:Object =
        {
            "notLoaded": "Unable to load '{0}'."
        };
        return content;
    }
}



}
