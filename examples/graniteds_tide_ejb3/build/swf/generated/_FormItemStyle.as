
package 
{

import flash.display.Sprite;
import mx.core.IFlexModuleFactory;
import mx.core.mx_internal;
import mx.styles.CSSStyleDeclaration;
import mx.styles.StyleManager;

[ExcludeClass]

public class _FormItemStyle
{
    [Embed(_resolvedSource='C:/flex_sdk_3/frameworks/libs/framework.swc$Assets.swf', symbol='mx.containers.FormItem.Required', source='C:/flex_sdk_3/frameworks/libs/framework.swc$Assets.swf', original='Assets.swf', _line='715', _pathsep='true', _file='C:/flex_sdk_3/frameworks/libs/framework.swc$defaults.css')]
    private static var _embed_css_Assets_swf_mx_containers_FormItem_Required_1753443146:Class;

    public static function init(fbs:IFlexModuleFactory):void
    {
        var style:CSSStyleDeclaration = StyleManager.getStyleDeclaration("FormItem");
    
        if (!style)
        {
            style = new CSSStyleDeclaration();
            StyleManager.setStyleDeclaration("FormItem", style, false);
        }
    
        if (style.defaultFactory == null)
        {
            style.defaultFactory = function():void
            {
                this.indicatorSkin = _embed_css_Assets_swf_mx_containers_FormItem_Required_1753443146;
            };
        }
    }
}

}
