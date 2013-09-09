package org.granite.client.android.binding;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

public class TextViewWatcher extends ViewWatcher<TextView> {
	
	private TextWatcher textWatcher;

	public TextViewWatcher(TextView view, String property) {
		super(view, property);
	}
	
	@Override
	protected void clear() {
		super.clear();
		textWatcher = null;
	}
	
	@Override
	public void addProperty(String property) {
		if (property.equals("text")) {
			textWatcher = new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					String newValue = s != null ? s.toString() : null;
					pcs.firePropertyChange("text", getCurrentValue("text"), newValue);
					setCurrentValue("text", newValue);
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}
				
				@Override
				public void afterTextChanged(Editable s) {
				}
			};
			getView().addTextChangedListener(textWatcher);
			setCurrentValue(property, getView().getText() != null ? getView().getText().toString() : null);
			return;
		}
		super.addProperty(property);
	}
	
}