/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Cursor;
import javafx.stage.Stage;

import org.granite.client.tide.server.ServerSession.Status;

/**
 * @author William DRAI
 */
public class JavaFXServerSessionStatus implements Status {
	
	private Stage stage;
	
	private BooleanProperty busy = new ReadOnlyBooleanWrapper(this, "busy", false);
	private BooleanProperty connected = new ReadOnlyBooleanWrapper(this, "connected", false);
	private BooleanProperty showBusyCursor = new SimpleBooleanProperty(this, "showBusyCursor", true);
	

	public JavaFXServerSessionStatus() {
		this(null);
	}
	
	public JavaFXServerSessionStatus(Stage stage) {
		this.stage = stage;
	
		busy.addListener(new ChangeListener<Boolean>() {
			
			private Cursor saveCursor = Cursor.DEFAULT;
			
			@Override
			public void changed(ObservableValue<? extends Boolean> property, Boolean oldValue, Boolean newValue) {
				if (JavaFXServerSessionStatus.this.stage != null && JavaFXServerSessionStatus.this.stage.getScene() != null && showBusyCursor.get()) {
					if (Boolean.FALSE.equals(oldValue)) {
						saveCursor = JavaFXServerSessionStatus.this.stage.getScene().getCursor();
						JavaFXServerSessionStatus.this.stage.getScene().setCursor(Cursor.WAIT);
					}
					else
						JavaFXServerSessionStatus.this.stage.getScene().setCursor(saveCursor);
				}
			}
		});
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	
	public ReadOnlyBooleanProperty busyProperty() {
		return busy;
	}
	
	public ReadOnlyBooleanProperty connectedProperty() {
		return connected;
	}
	
	public BooleanProperty showBusyCursorProperty() {
		return showBusyCursor;
	}

	@Override
	public boolean isBusy() {
		return busy.get();
	}

	@Override
	public void setBusy(boolean busy) {
		this.busy.set(busy);
	}

	@Override
	public boolean isConnected() {
		return connected.get();
	}

	@Override
	public void setConnected(boolean connected) {
		this.connected.set(connected);
	}

	@Override
	public boolean isShowBusyCursor() {
		return showBusyCursor.get();
	}

	@Override
	public void setShowBusyCursor(boolean showBusyCursor) {
		this.showBusyCursor.set(showBusyCursor);
	}

}
