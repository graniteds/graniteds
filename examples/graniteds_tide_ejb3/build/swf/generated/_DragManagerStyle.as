
package 
{

import flash.display.Sprite;
import mx.core.IFlexModuleFactory;
import mx.core.mx_internal;
import mx.styles.CSSStyleDeclaration;
import mx.styles.StyleManager;
import mx.skins.halo.DefaultDragImage;

[ExcludeClass]

public class _DragManagerStyle
{
    [Embed(_resolvedSource='C:/flex_sdk_3/frameworks/libs/framework.swc$Assets.swf', symbol='mx.skins.cursor.DragMove', source='C:/flex_sdk_3/frameworks/libs/framework.swc$Assets.swf', original='Assets.swf', _line='654', _pathsep='true', _file='C:/flex_sdk_3/frameworks/libs/framework.swc$defaults.css')]
    private static var _embed_css_Assets_swf_mx_skins_cursor_DragMove_2123130923:Class;
    [Embed(_resolvedSource='C:/flex_sdk_3/frameworks/libs/framework.swc$Assets.swf', symbol='mx.skins.cursor.DragLink', source='C:/flex_sdk_3/frameworks/libs/framework.swc$Assets.swf', original='Assets.swf', _line='653', _pathsep='true', _file='C:/flex_sdk_3/frameworks/libs/framework.swc$defaults.css')]
    private static var _embed_css_Assets_swf_mx_skins_cursor_DragLink_2123106240:Class;
    [Embed(_resolvedSource='C:/flex_sdk_3/frameworks/libs/framework.swc$Assets.swf', symbol='mx.skins.cursor.DragReject', source='C:/flex_sdk_3/frameworks/libs/framework.swc$Assets.swf', original='Assets.swf', _line='655', _pathsep='true', _file='C:/flex_sdk_3/frameworks/libs/framework.swc$defaults.css')]
    private static var _embed_css_Assets_swf_mx_skins_cursor_DragReject_673555563:Class;
    [Embed(_resolvedSource='C:/flex_sdk_3/frameworks/libs/framework.swc$Assets.swf', symbol='mx.skins.cursor.DragCopy', source='C:/flex_sdk_3/frameworks/libs/framework.swc$Assets.swf', original='Assets.swf', _line='651', _pathsep='true', _file='C:/flex_sdk_3/frameworks/libs/framework.swc$defaults.css')]
    private static var _embed_css_Assets_swf_mx_skins_cursor_DragCopy_2122319695:Class;

    public static function init(fbs:IFlexModuleFactory):void
    {
        var style:CSSStyleDeclaration = StyleManager.getStyleDeclaration("DragManager");
    
        if (!style)
        {
            style = new CSSStyleDeclaration();
            StyleManager.setStyleDeclaration("DragManager", style, false);
        }
    
        if (style.defaultFactory == null)
        {
            style.defaultFactory = function():void
            {
                this.copyCursor = _embed_css_Assets_swf_mx_skins_cursor_DragCopy_2122319695;
                this.moveCursor = _embed_css_Assets_swf_mx_skins_cursor_DragMove_2123130923;
                this.rejectCursor = _embed_css_Assets_swf_mx_skins_cursor_DragReject_673555563;
                this.linkCursor = _embed_css_Assets_swf_mx_skins_cursor_DragLink_2123106240;
                this.defaultDragImageSkin = mx.skins.halo.DefaultDragImage;
            };
        }
    }
}

}
