
package 
{

import flash.display.Sprite;
import mx.core.IFlexModuleFactory;
import mx.core.mx_internal;
import mx.styles.CSSStyleDeclaration;
import mx.styles.StyleManager;
import mx.skins.halo.ListDropIndicator;

[ExcludeClass]

public class _ListBaseStyle
{

    public static function init(fbs:IFlexModuleFactory):void
    {
        var style:CSSStyleDeclaration = StyleManager.getStyleDeclaration("ListBase");
    
        if (!style)
        {
            style = new CSSStyleDeclaration();
            StyleManager.setStyleDeclaration("ListBase", style, false);
        }
    
        if (style.defaultFactory == null)
        {
            style.defaultFactory = function():void
            {
                this.borderStyle = "solid";
                this.paddingTop = 2;
                this.backgroundColor = 0xffffff;
                this.backgroundDisabledColor = 0xdddddd;
                this.dropIndicatorSkin = mx.skins.halo.ListDropIndicator;
                this.paddingLeft = 2;
                this.paddingBottom = 2;
                this.paddingRight = 0;
            };
        }
    }
}

}
