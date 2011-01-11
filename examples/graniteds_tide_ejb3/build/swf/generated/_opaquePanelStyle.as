
package 
{

import flash.display.Sprite;
import mx.core.IFlexModuleFactory;
import mx.core.mx_internal;
import mx.styles.CSSStyleDeclaration;
import mx.styles.StyleManager;

[ExcludeClass]

public class _opaquePanelStyle
{

    public static function init(fbs:IFlexModuleFactory):void
    {
        var style:CSSStyleDeclaration = StyleManager.getStyleDeclaration(".opaquePanel");
    
        if (!style)
        {
            style = new CSSStyleDeclaration();
            StyleManager.setStyleDeclaration(".opaquePanel", style, false);
        }
    
        if (style.defaultFactory == null)
        {
            style.defaultFactory = function():void
            {
                this.borderColor = 0xffffff;
                this.backgroundColor = 0xffffff;
                this.headerColors = [0xe7e7e7, 0xd9d9d9];
                this.footerColors = [0xe7e7e7, 0xc7c7c7];
                this.borderAlpha = 1;
            };
        }
    }
}

}
