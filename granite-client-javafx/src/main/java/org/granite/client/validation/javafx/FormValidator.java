/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.validation.javafx;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Skinnable;
import javafx.scene.control.TextInputControl;

import javax.validation.ConstraintViolation;
import javax.validation.groups.Default;

import org.granite.client.validation.NotifyingValidator;
import org.granite.client.validation.NotifyingValidator.ConstraintViolationsHandler;
import org.granite.client.validation.NotifyingValidatorFactory;
import org.granite.logging.Logger;

/**
 * @author William DRAI
 */
public class FormValidator {
	
	private static final Logger log = Logger.getLogger(FormValidator.class);

	public static final String UNHANDLED_VIOLATIONS = "unhandledViolations";
	
	protected Parent form;
	
	protected List<Node> inputs = new ArrayList<Node>();
	protected Map<Node, Property<?>> inputProperties = new IdentityHashMap<Node, Property<?>>();
	protected Map<Node, Property<?>> entityProperties = new IdentityHashMap<Node, Property<?>>();
	protected Set<Node> focusedOutOnce = new HashSet<Node>();

	protected List<ConstraintViolation<?>> violations = new ArrayList<ConstraintViolation<?>>();
	protected ObservableList<ConstraintViolation<?>> unhandledViolations = FXCollections.observableArrayList();
	
	private final NotifyingValidator validator;
	
	
	/**
	 * The <code>Validator</code> to be used in the validation
	 * process (initialized with the the default instance).
	 */	
	public FormValidator(NotifyingValidatorFactory validatorFactory) {
	    this.validator = validatorFactory.getValidator();
	}
	
	/**
	 * Should validation be done on the fly? Otherwise, validation will be
	 * only done when an input loses focus. Default is true.
	 */
	public BooleanProperty validateOnChangeProperty = new SimpleBooleanProperty(this, "validateOnChange", true);
	
	public boolean isValidateOnChange() {
		return validateOnChangeProperty.get();
	}
	public void setValidateOnChange(boolean validateOnChange) {
		this.validateOnChangeProperty.set(validateOnChange);
	}
	
	
	/**
	 * The validation groups to be used, as an array of <code>Class</code>
	 * names. Default is null, meaning that the <code>Default</code> group
	 * will be used.
	 */
	public Class<?>[] groups = new Class<?>[] { Default.class };
	
	/**
	 * The form component that contains inputs bound to the entity properties
	 * (may be a <code>Form</code> or any other <code>Container</code>
	 * subclass).
	 */
	public Parent getForm() {
		return form;
	}
	public void setForm(Parent form) {
		if (form == this.form)
			return;
		
		if (this.form != null)
			setupForm(null);
		
		this.form = form;
		
		if (this.form != null)
			setupForm(this.form);
	}

	/**
	 * Returns the result of the last global validation as an array of
	 * <code>ConstraintViolation</code>s.
	 * 
	 * @return the result of the last global validation as an array of
	 * 		<code>ConstraintViolation</code>s.
	 */
	public List<ConstraintViolation<?>> getViolations() {
		return violations;
	}

	/**
	 * Returns the <i>unhandled</i> violations of the last global validation
	 * as an array of <code>ConstraintViolation</code>s. Unhandled violations
	 * are violations that couldn't be associated to any input during the
	 * last global validation (thus, they couldn't be displayed anywhere).
	 * 
	 * @return the <i>unhandled</i> violations of the last global validation
	 * 		as an array of <code>ConstraintViolation</code>s.
	 */
	public List<ConstraintViolation<?>> getUnhandledViolations() {
		return unhandledViolations;
	}
	
	
	protected void setupForm(Parent form) {
		// Untrack child nodes
		untrackNode(this.form);
		
		if (!inputs.isEmpty()) {
			inputs.clear();
			log.warn("Inputs were not cleared correctly");
		}
		if (!inputProperties.isEmpty()) {
			inputProperties.clear();
			log.warn("Input properties were not cleared correctly");
		}
		if (!entityProperties.isEmpty()) {
			entityProperties.clear();
			log.warn("Entity properties were not cleared correctly");
		}
		if (!trackedParents.isEmpty()) {
			trackedParents.clear();
			log.warn("Tracked parents were not cleared correctly");
		}
			
		focusedOutOnce.clear();

		if (form != null)
			trackNode(form);
	}
	

	private ListChangeListener<Node> childChangeListener = new ChildChangeListener();
    
    public class ChildChangeListener implements ListChangeListener<Node> {    	
		@Override
		public void onChanged(ListChangeListener.Change<? extends Node> change) {
			while (change.next()) {
				if (change.wasReplaced() && change.getRemovedSize() == 1 && change.getAddedSize() == 1 && change.getAddedSubList().get(0) == change.getRemoved().get(0))
					continue;
				
				if (change.wasRemoved()) {
					for (Node node : change.getRemoved())
						untrackNode(node);
				}
				if (change.wasAdded()) {
					for (Node node : change.getAddedSubList())
						trackNode(node);
				}
				if (change.wasPermutated()) {
					log.debug("Permutation ??");
				}
			}
		}
    }
    
    
    private IdentityHashMap<Parent, Boolean> trackedParents = new IdentityHashMap<Parent, Boolean>();
    

	protected void trackNode(Node node) {
		if (form == null)
			return;
		
		setupNode(node);
		
		if (node instanceof Skinnable && ((Skinnable)node).getSkin() != null)
			trackNode(((Skinnable)node).getSkin().getNode());
		
		if (node instanceof Parent && !trackedParents.containsKey(node)) {
			for (Node child : ((Parent)node).getChildrenUnmodifiable())
				trackNode(child);
			
			((Parent)node).getChildrenUnmodifiable().addListener(childChangeListener);
			trackedParents.put((Parent)node, true);
			
			log.debug("Setup children tracking for parent %s", node);
		}
	}
	
	protected void untrackNode(Node node) {
		if (form == null)
			return;
		
		if (node instanceof Parent) {
			((Parent)node).getChildrenUnmodifiable().removeListener(childChangeListener);
			trackedParents.remove(node);
			
			log.debug("Unset children tracking for parent %s", node.toString());
			
			for (Node child : ((Parent)node).getChildrenUnmodifiable())
				untrackNode(child);
		}
		
		if (node instanceof Skinnable && ((Skinnable)node).getSkin() != null)
			untrackNode(((Skinnable)node).getSkin().getNode());
		
		unsetupNode(node);
	}
	
	private void setupNode(Node node) {
		// If node is already tracked, clear everything in case user did not unbind old data
		if (inputProperties.containsKey(node)) {
			Property<?> entityProperty = entityProperties.remove(node);
			Property<?> inputProperty = inputProperties.remove(node);
			
			if (entityProperty != null && entityProperty.getBean() != null)
			    validator.removeConstraintViolationsHandler(entityProperty.getBean(), constraintViolationHandler);
			
			inputProperty.removeListener(valueChangeListener);
			node.focusedProperty().removeListener(inputFocusChangeListener);
			
			inputs.remove(node);
			log.debug("Cleanup old tracking for fantom node %s input %s entity %s", node, inputProperty.getName(), entityProperty);
		}
		
		Property<?> inputProperty = null;
		
		if (node instanceof TextInputControl)
			inputProperty = ((TextInputControl)node).textProperty();
		
		if (inputProperty != null) {
			Property<?> entityProperty = lookupBindingTarget(inputProperty);
			if (entityProperty != null) {
				inputProperties.put(node, inputProperty);
				entityProperties.put(node, entityProperty);
				
				if (entityProperty.getBean() != null)
					validator.addConstraintViolationsHandler(entityProperty.getBean(), constraintViolationHandler);
				
				inputProperty.addListener(valueChangeListener);
				node.focusedProperty().addListener(inputFocusChangeListener);
				
				inputs.add(node);
				
				log.debug("Setup tracking for node %s input %s entity %s", node, inputProperty.getName(), entityProperty);
			}
		}
	}
	
	private void unsetupNode(Node node) {
		int idx = inputs.indexOf(node);
		if (idx >= 0) {
			Property<?> entityProperty = entityProperties.remove(node);
			if (entityProperty.getBean() != null)
				validator.removeConstraintViolationsHandler(entityProperty.getBean(), constraintViolationHandler);
			
			node.fireEvent(new ValidationResultEvent(this, node, ValidationResultEvent.VALID, null));
			
			if (node instanceof TextInputControl)
				((TextInputControl)node).textProperty().removeListener(valueChangeListener);
			node.focusedProperty().removeListener(inputFocusChangeListener);
			
			Property<?> inputProperty = inputProperties.remove(node);			
			inputs.remove(idx);
			
			log.debug("Unsetup tracking for node %s input %s entity %s", node, inputProperty.getName(), entityProperty);
		}
	}
	
	/*
	 *	Ugly hack to determine target of bidirectional binding
	 */
	private Property<?> lookupBindingTarget(Property<?> inputProperty) {
		try {
			Field fh = inputProperty.getClass().getDeclaredField("helper");
			fh.setAccessible(true);
			Object helper = fh.get(inputProperty);
			Field fcl = helper.getClass().getDeclaredField("changeListeners");
			fcl.setAccessible(true);
			Object changeListeners = fcl.get(helper);
			if (changeListeners != null && Array.getLength(changeListeners) > 0) {
				ChangeListener<?> cl = (ChangeListener<?>)Array.get(changeListeners, 0);
				try {
					Field fpr = cl.getClass().getDeclaredField("propertyRef2");
					fpr.setAccessible(true);
					WeakReference<?> ref= (WeakReference<?>)fpr.get(cl);
					Property<?> p = (Property<?>)ref.get();
					return p;
				}
				catch (NoSuchFieldException e) {
					log.debug("Field propertyRef2 not found on " + cl + ", probably not a standard binding", e);
					return null;
				}
			}
			log.debug("Could not find target binding for property %s", inputProperty);
			return null;
		}
		catch (Exception e) {
			log.warn(e, "Could not find target binding for property %s", inputProperty);
			return null;
		}
	}
	
	
	private ChangeListener<Boolean> inputFocusChangeListener = new InputFocusChangeListener();
	private ChangeListener<Object> valueChangeListener = new ValueChangeListener();
	
	/**
	 * @private
	 */
	private class InputFocusChangeListener implements ChangeListener<Boolean> {
		@Override
		public void changed(ObservableValue<? extends Boolean> change, Boolean oldValue, Boolean newValue) {
			if (Boolean.TRUE.equals(oldValue) && Boolean.FALSE.equals(newValue))
				validateValue((Node)((ReadOnlyBooleanProperty)change).getBean(), true);
		}		
	}

	private class ValueChangeListener implements ChangeListener<Object> {
		@SuppressWarnings("unchecked")
		@Override
		public void changed(ObservableValue<?> change, Object oldValue, Object newValue) {
			if (validateOnChangeProperty.get())				
				validateValue((Node)((Property<Object>)change).getBean(), false);
		}		
	}
	
	
	private ConstraintViolationsHandler<Object> constraintViolationHandler = new ConstraintViolationHandlerImpl();
	
	private class ConstraintViolationHandlerImpl implements ConstraintViolationsHandler<Object> {
		@Override
		public void handle(Object entity, Set<ConstraintViolation<Object>> violations) {
	        focusedOutOnce.addAll(inputs);
	        
	        if (violations == null)
	            return;
	        
			for (ConstraintViolation<?> violation : violations) {
				Object leafBean = violation.getLeafBean();
				String property = null;
				Iterator<javax.validation.Path.Node> in = violation.getPropertyPath().iterator();
				while (in.hasNext()) {
					javax.validation.Path.Node n = in.next();
					property = n.getName();
				}
				String[] path = property.split("\\.");
				property = path[path.length-1];
				
				Node input = null;
				for (Entry<Node, Property<?>> me : entityProperties.entrySet()) {
					if (leafBean != null && leafBean.equals(me.getValue().getBean()) && me.getValue().getName().equals(property)) {
						input = me.getKey();
						break;
					}
				}
				
				if (input != null) {
					List<ValidationResult> results = Collections.singletonList(new ValidationResult(true, entityProperties.get(input), "constraintViolation", violation.getMessage()));					
					input.fireEvent(new ValidationResultEvent(this, input, ValidationResultEvent.INVALID, results));
				}
			}
		}
	}
	
	
	protected boolean validateValue(Node input, boolean focusOut) {
		Property<?> entityProperty = entityProperties.get(input);
		Property<?> inputProperty = inputProperties.get(input);
		if (entityProperty == null || inputProperty == null) {
			log.warn("validateValue called for untracked input " + input);
			return true;
		}
		
		if (focusOut)
			focusedOutOnce.add(input);
		
		boolean nulled = false;
		Object value = inputProperty.getValue();
		if ("".equals(value)) {
			value = null;
			nulled = true;
		}
		
		@SuppressWarnings("unchecked")
		Class<Object> entityClass = (Class<Object>)entityProperty.getBean().getClass();
		Set<ConstraintViolation<Object>> violations = validator.validateValue(entityClass, entityProperty.getName(), value, groups);
		if (violations == null)
			violations = Collections.emptySet();
		if (violations.isEmpty() && !nulled)
			focusedOutOnce.add(input);
		else if (!focusedOutOnce.contains(input))
			return true;
		
		handleViolations(input, violations);
		
		return violations.isEmpty();
	}

	/**
	 * @inheritDoc
	 */
	protected void handleViolations(Node input, Set<ConstraintViolation<Object>> violations) {
		List<ValidationResultEvent> resultEvents = new ArrayList<ValidationResultEvent>();
		
		if (input != null) {
			if (!violations.isEmpty()) {
				List<ValidationResult> results = new ArrayList<ValidationResult>();
				for (ConstraintViolation<?> violation : violations)
					results.add(new ValidationResult(true, entityProperties.get(input), "constraintViolation", violation.getMessage()));
				
				resultEvents.add(new ValidationResultEvent(this, input, ValidationResultEvent.INVALID, results));
			}
			else
				resultEvents.add(new ValidationResultEvent(this, input, ValidationResultEvent.VALID, null));
		}
		else {
			Set<ConstraintViolation<?>> unhandledViolations = new HashSet<ConstraintViolation<?>>(violations);
			
			for (Node inp : inputs) {
				List<ValidationResult> results = new ArrayList<ValidationResult>();
				
				Property<?> property = entityProperties.get(inp);
				Iterator<ConstraintViolation<?>> iv = unhandledViolations.iterator();
				while (iv.hasNext()) {
					ConstraintViolation<?> violation = iv.next();
					Iterator<javax.validation.Path.Node> in = violation.getPropertyPath().iterator();
					javax.validation.Path.Node n = null;
					while (in.hasNext())
						n = in.next();
					
					if (violation.getLeafBean().equals(property.getBean()) && n.getName().equals(property.getName())) {
						ValidationResult result = new ValidationResult(true, property, "constraintViolation", violation.getMessage());
						results.add(result);
						iv.remove();
					}
				}
				
				if (results.isEmpty()) {
					// No violation for this input : add a valid result
					resultEvents.add(new ValidationResultEvent(this, inp, ValidationResultEvent.VALID, null));
				}
				else {
					resultEvents.add(new ValidationResultEvent(this, inp, ValidationResultEvent.INVALID, results));
				}
			}
			
			this.unhandledViolations.clear();
			if (!unhandledViolations.isEmpty()) {
				this.unhandledViolations.addAll(unhandledViolations);
				
				List<ValidationResult> unhandledResults = new ArrayList<ValidationResult>();
				for (ConstraintViolation<?> violation : unhandledViolations)
					unhandledResults.add(new ValidationResult(true, null, "constraintViolation", violation.getMessage()));
				resultEvents.add(new ValidationResultEvent(this, form, ValidationResultEvent.UNHANDLED, unhandledResults));
			}
		}
		
		for (ValidationResultEvent resultEvent : resultEvents) {
			((Node)resultEvent.getTarget()).fireEvent(resultEvent);
		}
	}
}
