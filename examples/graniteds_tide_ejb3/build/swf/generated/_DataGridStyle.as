
package 
{

import flash.display.Sprite;
import mx.core.IFlexModuleFactory;
import mx.core.mx_internal;
import mx.styles.CSSStyleDeclaration;
import mx.styles.StyleManager;
import mx.skins.halo.DataGridColumnDropIndicator;
import mx.skins.halo.DataGridSortArrow;
import mx.skins.halo.DataGridHeaderBackgroundSkin;
import mx.skins.halo.DataGridColumnResizeSkin;
import mx.skins.halo.DataGridHeaderSeparator;

[ExcludeClass]

public class _DataGridStyle
{
    [Embed(_resolvedSource='C:/flex_sdk_3/frameworks/libs/framework.swc$Assets.swf', symbol='cursorStretch', source='C:/flex_sdk_3/frameworks/libs/framework.swc$Assets.swf', original='Assets.swf', _line='529', _pathsep='true', _file='C:/flex_sdk_3/frameworks/libs/framework.swc$defaults.css')]
    private static var _embed_css_Assets_swf_cursorStretch_2032135452:Class;

    public static function init(fbs:IFlexModuleFactory):void
    {
        var style:CSSStyleDeclaration = StyleManager.getStyleDeclaration("DataGrid");
    
        if (!style)
        {
            style = new CSSStyleDeclaration();
            StyleManager.setStyleDeclaration("DataGrid", style, false);
        }
    
        if (style.defaultFactory == null)
        {
            style.defaultFactory = function():void
            {
                this.sortArrowSkin = mx.skins.halo.DataGridSortArrow;
                this.columnDropIndicatorSkin = mx.skins.halo.DataGridColumnDropIndicator;
                this.columnResizeSkin = mx.skins.halo.DataGridColumnResizeSkin;
                this.stretchCursor = _embed_css_Assets_swf_cursorStretch_2032135452;
                this.alternatingItemColors = [0xf7f7f7, 0xffffff];
                this.headerStyleName = "dataGridStyles";
                this.headerSeparatorSkin = mx.skins.halo.DataGridHeaderSeparator;
                this.headerBackgroundSkin = mx.skins.halo.DataGridHeaderBackgroundSkin;
                this.headerColors = [0xffffff, 0xe6e6e6];
                this.headerDragProxyStyleName = "headerDragProxyStyle";
                this.verticalGridLineColor = 0xcccccc;
            };
        }
    }
}

}
