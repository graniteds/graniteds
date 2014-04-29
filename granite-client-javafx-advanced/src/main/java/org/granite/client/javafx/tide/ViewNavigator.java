/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.javafx.tide;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.granite.client.tide.Context;
import org.granite.client.tide.ContextAware;
import org.granite.client.tide.ViewScopeHolder;

/**
 * @author William DRAI
 */
public class ViewNavigator implements ContextAware {
	
	private Context context;
	private Stage stage;
	
	private ObjectProperty<ManagedView> currentView = new SimpleObjectProperty<ManagedView>(this, "currentView");
	
	public ViewNavigator() {
		currentView.addListener(new ChangeListener<ManagedView>() {
			@Override
			public void changed(ObservableValue<? extends ManagedView> view, ManagedView oldView, ManagedView newView) {
				show(newView);
			}
		});
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
		this.stage = context.byType(Stage.class);
	}
	
	public Stage getStage() {
		return stage;
	}
	
	public Parent getRoot() {
		return stage.getScene().getRoot();
	}
	
	public ObjectProperty<ManagedView> currentViewProperty() {
		return currentView;
	}
	public ManagedView getCurrentView() {
		return currentView.get();
	}
	public void setCurrentView(ManagedView view) {
		currentView.set(view);
	}
    
    protected Parent show(ManagedView view) {
		// Clear view scope if necessary before loading new view
		ViewScopeHolder.get().ensureViewId(view.getViewId());
		
    	preChangeView(view, stage);
    	
    	// Load view
    	Parent root = view.load(context);
    	
        Scene scene = prepareScene(view, stage, root);
        stage.setScene(scene);
        
        postChangeView(view, stage);
    	
    	return root;
    }
    
    protected void preChangeView(ManagedView view, Stage stage) {    	
    }
    
    protected void postChangeView(ManagedView view, Stage stage) {    	
        if (!stage.isShowing())
        	stage.show();
    }
    
    protected Scene prepareScene(ManagedView view, Stage stage, Parent root) {
    	return new Scene(root);
    }
}
