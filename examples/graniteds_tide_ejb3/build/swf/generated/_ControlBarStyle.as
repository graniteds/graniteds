
package 
{

import flash.display.Sprite;
import mx.core.IFlexModuleFactory;
import mx.core.mx_internal;
import mx.styles.CSSStyleDeclaration;
import mx.styles.StyleManager;

[ExcludeClass]

public class _ControlBarStyle
{

    public static function init(fbs:IFlexModuleFactory):void
    {
        var style:CSSStyleDeclaration = StyleManager.getStyleDeclaration("ControlBar");
    
        if (!style)
        {
            style = new CSSStyleDeclaration();
            StyleManager.setStyleDeclaration("ControlBar", style, false);
        }
    
        if (style.defaultFactory == null)
        {
            style.defaultFactory = function():void
            {
                this.disabledOverlayAlpha = 0;
                this.borderStyle = "controlBar";
                this.paddingTop = 10;
                this.verticalAlign = "middle";
                this.paddingLeft = 10;
                this.paddingBottom = 10;
                this.paddingRight = 10;
            };
        }
    }
}

}
