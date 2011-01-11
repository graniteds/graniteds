
package 
{

import flash.display.Sprite;
import mx.core.IFlexModuleFactory;
import mx.core.mx_internal;
import mx.styles.CSSStyleDeclaration;
import mx.styles.StyleManager;
import mx.skins.halo.ScrollArrowSkin;
import mx.skins.halo.ScrollThumbSkin;
import mx.skins.halo.ScrollTrackSkin;

[ExcludeClass]

public class _ScrollBarStyle
{

    public static function init(fbs:IFlexModuleFactory):void
    {
        var style:CSSStyleDeclaration = StyleManager.getStyleDeclaration("ScrollBar");
    
        if (!style)
        {
            style = new CSSStyleDeclaration();
            StyleManager.setStyleDeclaration("ScrollBar", style, false);
        }
    
        if (style.defaultFactory == null)
        {
            style.defaultFactory = function():void
            {
                this.thumbOffset = 0;
                this.paddingTop = 0;
                this.borderColor = 0xb7babc;
                this.trackColors = [0x94999b, 0xe7e7e7];
                this.trackSkin = mx.skins.halo.ScrollTrackSkin;
                this.downArrowSkin = mx.skins.halo.ScrollArrowSkin;
                this.cornerRadius = 4;
                this.upArrowSkin = mx.skins.halo.ScrollArrowSkin;
                this.paddingLeft = 0;
                this.paddingBottom = 0;
                this.thumbSkin = mx.skins.halo.ScrollThumbSkin;
                this.paddingRight = 0;
            };
        }
    }
}

}
