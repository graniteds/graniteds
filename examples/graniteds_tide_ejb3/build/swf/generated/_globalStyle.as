
package 
{

import flash.display.Sprite;
import mx.core.IFlexModuleFactory;
import mx.core.mx_internal;
import mx.styles.CSSStyleDeclaration;
import mx.styles.StyleManager;
import mx.skins.halo.HaloFocusRect;
import mx.skins.halo.HaloBorder;

[ExcludeClass]

public class _globalStyle
{

    public static function init(fbs:IFlexModuleFactory):void
    {
        var style:CSSStyleDeclaration = StyleManager.getStyleDeclaration("global");
    
        if (!style)
        {
            style = new CSSStyleDeclaration();
            StyleManager.setStyleDeclaration("global", style, false);
        }
    
        if (style.defaultFactory == null)
        {
            style.defaultFactory = function():void
            {
                this.fillColor = 0xffffff;
                this.kerning = false;
                this.iconColor = 0x111111;
                this.textRollOverColor = 0x2b333c;
                this.horizontalAlign = "left";
                this.shadowCapColor = 0xd5dddd;
                this.backgroundAlpha = 1.0;
                this.filled = true;
                this.textDecoration = "none";
                this.roundedBottomCorners = true;
                this.fontThickness = 0;
                this.focusBlendMode = "normal";
                this.fillColors = [0xffffff, 0xcccccc, 0xffffff, 0xeeeeee];
                this.horizontalGap = 8;
                this.borderCapColor = 0x919999;
                this.buttonColor = 0x6f7777;
                this.indentation = 17;
                this.selectionDisabledColor = 0xdddddd;
                this.closeDuration = 250;
                this.embedFonts = false;
                this.paddingTop = 0;
                this.letterSpacing = 0;
                this.focusAlpha = 0.4;
                this.bevel = true;
                this.fontSize = 10;
                this.shadowColor = 0xeeeeee;
                this.borderAlpha = 1.0;
                this.paddingLeft = 0;
                this.fontWeight = "normal";
                this.indicatorGap = 14;
                this.focusSkin = mx.skins.halo.HaloFocusRect;
                this.dropShadowEnabled = false;
                this.leading = 2;
                this.borderSkin = mx.skins.halo.HaloBorder;
                this.fontSharpness = 0;
                this.modalTransparencyDuration = 100;
                this.borderThickness = 1;
                this.backgroundSize = "auto";
                this.borderStyle = "inset";
                this.borderColor = 0xb7babc;
                this.fontAntiAliasType = "advanced";
                this.errorColor = 0xff0000;
                this.shadowDistance = 2;
                this.horizontalGridLineColor = 0xf7f7f7;
                this.stroked = false;
                this.modalTransparencyColor = 0xdddddd;
                this.cornerRadius = 0;
                this.verticalAlign = "top";
                this.textIndent = 0;
                this.fillAlphas = [0.6, 0.4, 0.75, 0.65];
                this.verticalGridLineColor = 0xd5dddd;
                this.themeColor = 0x009dff;
                this.version = "3.0.0";
                this.shadowDirection = "center";
                this.modalTransparency = 0.5;
                this.repeatInterval = 35;
                this.openDuration = 250;
                this.textAlign = "left";
                this.fontFamily = "Verdana";
                this.textSelectedColor = 0x2b333c;
                this.paddingBottom = 0;
                this.strokeWidth = 1;
                this.fontGridFitType = "pixel";
                this.horizontalGridLines = false;
                this.useRollOver = true;
                this.verticalGridLines = true;
                this.repeatDelay = 500;
                this.fontStyle = "normal";
                this.dropShadowColor = 0x000000;
                this.focusThickness = 2;
                this.verticalGap = 6;
                this.disabledColor = 0xaab3b3;
                this.paddingRight = 0;
                this.focusRoundedCorners = "tl tr bl br";
                this.borderSides = "left top right bottom";
                this.disabledIconColor = 0x999999;
                this.modalTransparencyBlur = 3;
                this.color = 0x0b333c;
                this.selectionDuration = 250;
                this.highlightAlphas = [0.3, 0];
            };
        }
    }
}

}
