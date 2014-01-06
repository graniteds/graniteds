/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */

package org.granite.builder.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

/**
 * @author Franck WOLFF
 */
public class SWTUtil {

	private static final String PREFIX_KEY = "PREFIX";
	private static final String EMPTY_KEY = "EMPTY";
	private static final String VALUE_KEY = "VALUE";
	
	public static final String IMG_PKG_FOLDER = "icons/packagefolder_obj.gif";
	public static final String IMG_PKG_FOLDER_ERROR = "icons/packagefolder_obj_error.gif";
	public static final String IMG_LIBRARY = "icons/library_obj.gif";
	public static final String IMG_JAR = "icons/jar_obj.gif";
	public static final String IMG_JAR_LIBRARY = "icons/jar_l_obj.gif";
	public static final String IMG_SETTINGS = "icons/settings_obj.gif";
	public static final String IMG_INCLUDES = "icons/inclusion_filter_attrib.gif";
	public static final String IMG_EXCLUDES = "icons/exclusion_filter_attrib.gif";
	public static final String IMG_OUT_FOLDER = "icons/externalize.gif";
	public static final String IMG_WARNING = "icons/warning_obj.gif";
	public static final String IMG_TEMPLATE = "icons/template_obj.gif";
	public static final String IMG_FILE = "icons/file_obj.gif";
	public static final String IMG_PROJECTS = "icons/projects.gif";
	public static final String IMG_GPROJECT = "icons/gproject.gif";
	public static final String IMG_GPROJECT_ERROR = "icons/gproject_error.gif";
	public static final String IMG_WIZARD = "icons/gdswiz.gif";
	
	public static final RGB WHITE = new RGB(0xff, 0xff, 0xff);
	public static final RGB LIGHT_RED = new RGB(0xff, 0x80, 0x80);
	
	public static final Map<String, Image> IMAGES_CACHE = new HashMap<String, Image>();
	public static final Map<RGB, Color> COLORS_CACHE = new HashMap<RGB, Color>();
    
    public static Button newButton(Composite parent, String text, boolean enabled, SelectionListener listener) {
    	Button button = new Button(parent, SWT.NONE);
		button.setText("    " + text + "    ");
		button.setEnabled(enabled);
		if (listener != null)
			button.addSelectionListener(listener);
		return button;
    }
    
    public static Composite createGridComposite(Composite parent, int numColumns) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = numColumns;
        composite.setLayout(layout);

        GridData data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        composite.setLayoutData(data);

        return composite;
    }
    
    public static TreeItem addTreeItem(Tree tree, String image, String text, String prefix, String empty) {
    	TreeItem item = new TreeItem(tree, SWT.NONE);

    	item.setImage(getImage(tree.getDisplay(), image));
    	
    	prefix = StringUtil.unNull(prefix);
    	empty = StringUtil.unNull(empty);
    	text = StringUtil.unNull(text);
    	
    	item.setData(PREFIX_KEY, prefix);
    	item.setData(EMPTY_KEY, empty);
    	item.setData(VALUE_KEY, text);
    	
    	text = prefix + (text.length() == 0 ? empty : text);
    	
        item.setText(text);
        
        return item;
    }
    
    public static TreeItem addTreeItem(TreeItem treeItem, String image, String text, String prefix, String empty) {
    	TreeItem item = new TreeItem(treeItem, SWT.NONE);

    	item.setImage(getImage(treeItem.getDisplay(), image));
    	
    	prefix = StringUtil.unNull(prefix);
    	empty = StringUtil.unNull(empty);
    	text = StringUtil.unNull(text);
    	
    	item.setData(PREFIX_KEY, prefix);
    	item.setData(EMPTY_KEY, empty);
    	item.setData(VALUE_KEY, text);
    	
    	text = prefix + (text.length() == 0 ? empty : text);
    	
        item.setText(text);
        
        return item;
    }
    
    public static void setTreeItemText(TreeItem item, String text) {
    	if (item.getData(VALUE_KEY) != null) {
	    	String prefix = StringUtil.unNull((String)item.getData(PREFIX_KEY));
	    	String empty = StringUtil.unNull((String)item.getData(EMPTY_KEY));
	    	
	    	item.setData(VALUE_KEY, text);
	    	
	    	text = prefix + (text.length() == 0 ? empty : text);
    	}
    	item.setText(text);
    }
    
    public static String getTreeItemText(TreeItem item) {
    	if (item.getData(VALUE_KEY) != null)
    		return (String)item.getData(VALUE_KEY);
    	return item.getText();
    }
    
    public static GridData newGridData(int style, int horizontalSpan) {
    	GridData gd = new GridData(style);
    	gd.horizontalSpan = horizontalSpan;
    	return gd;
    }
    
    public static Image getImage(Device device, String path) {
    	Image image = IMAGES_CACHE.get(path);
    	if (image == null) {
    		image = new Image(device, SWTUtil.class.getClassLoader().getResourceAsStream(path));
    		IMAGES_CACHE.put(path, image);
    	}
    	return image;
    }
    
    public static Color getColor(Device device, RGB rgb) {
    	Color color = COLORS_CACHE.get(rgb);
    	if (color == null) {
    		color = new Color(device, rgb);
    		COLORS_CACHE.put(rgb, color);
    	}
    	return color;
    }
    
    public static Display getCurrentDisplay() {
		Display display = Display.getCurrent();
		if (display == null)
			display = PlatformUI.createDisplay();
		return display;
    }
}
