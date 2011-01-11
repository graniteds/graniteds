
package 
{

import flash.display.Sprite;
import mx.core.IFlexModuleFactory;
import mx.core.mx_internal;
import mx.styles.CSSStyleDeclaration;
import mx.styles.StyleManager;
import mx.skins.halo.ButtonSkin;

[ExcludeClass]

public class _ButtonStyle
{

    public static function init(fbs:IFlexModuleFactory):void
    {
        var style:CSSStyleDeclaration = StyleManager.getStyleDeclaration("Button");
    
        if (!style)
        {
            style = new CSSStyleDeclaration();
            StyleManager.setStyleDeclaration("Button", style, false);
        }
    
        if (style.defaultFactory == null)
        {
            style.defaultFactory = function():void
            {
                this.fontWeight = "bold";
                this.paddingTop = 2;
                this.cornerRadius = 4;
                this.textAlign = "center";
                this.verticalGap = 2;
                this.horizontalGap = 2;
                this.skin = mx.skins.halo.ButtonSkin;
                this.paddingLeft = 10;
                this.paddingBottom = 2;
                this.paddingRight = 10;
            };
        }
    }
}

}
