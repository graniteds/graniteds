
package 
{

import flash.display.Sprite;
import mx.core.IFlexModuleFactory;
import mx.core.mx_internal;
import mx.styles.CSSStyleDeclaration;
import mx.styles.StyleManager;

[ExcludeClass]

public class _errorTipStyle
{

    public static function init(fbs:IFlexModuleFactory):void
    {
        var style:CSSStyleDeclaration = StyleManager.getStyleDeclaration(".errorTip");
    
        if (!style)
        {
            style = new CSSStyleDeclaration();
            StyleManager.setStyleDeclaration(".errorTip", style, false);
        }
    
        if (style.defaultFactory == null)
        {
            style.defaultFactory = function():void
            {
                this.fontWeight = "bold";
                this.borderStyle = "errorTipRight";
                this.paddingTop = 4;
                this.borderColor = 0xce2929;
                this.color = 0xffffff;
                this.fontSize = 9;
                this.shadowColor = 0x000000;
                this.paddingLeft = 4;
                this.paddingBottom = 4;
                this.paddingRight = 4;
            };
        }
    }
}

}
