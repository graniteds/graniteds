package 
{

import mx.resources.ResourceBundle;

[ExcludeClass]

public class en_US$styles_properties extends ResourceBundle
{

    public function en_US$styles_properties()
    {
		 super("en_US", "styles");
    }

    override protected function getContent():Object
    {
        var content:Object =
        {
            "unableToLoad": "Unable to load style({0}): {1}."
        };
        return content;
    }
}



}
