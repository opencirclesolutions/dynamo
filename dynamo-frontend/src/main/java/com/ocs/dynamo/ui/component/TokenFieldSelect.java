/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.ui.component;

import com.explicatis.ext_token_field.ExtTokenField;
import com.explicatis.ext_token_field.Tokenizable;
import com.google.common.collect.Sets;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A multiple select component that displays tags/tokens to indicate which
 * values are selected
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key
 * @param <T> the type of the entity
 * 
 */
public class TokenFieldSelect<ID extends Serializable, T extends AbstractEntity<ID>>
		extends QuickAddEntityField<ID, T, Collection<T>> implements Refreshable {

	/**
	 * Wrapper around an item in order to display it as a token in the token field
	 *
	 * @author bas.rutten
	 *
	 */
	private final class BeanItemTokenizable implements Tokenizable {

		private final T item;

		private final String displayValue;

		private final Long id;

		/**
		 * Constructor
		 *
		 * @param item
		 * @param captionPropertyId
		 */
		private BeanItemTokenizable(T item, String captionPropertyId) {
			this.item = item;
			this.id = getTokenIdentifier(item);
			this.displayValue = getTokenDisplayName(item, captionPropertyId);
		}

		@Override
		public long getIdentifier() {
			return id;
		}

		public T getItem() {
			return item;
		}

		@Override
		public String getStringValue() {
			return displayValue;
		}

		private String getTokenDisplayName(T entity, String captionPropertyId) {
			return ClassUtils.getFieldValueAsString(entity, captionPropertyId);
		}

		private long getTokenIdentifier(T entity) {
			return Long.parseLong(ClassUtils.getFieldValueAsString(entity, DynamoConstants.ID));
		}
	}

	private static final long serialVersionUID = -1490179285573442827L;

	private final ExtTokenField extTokenField;

	private final EntityComboBox<ID, T> comboBox;

	private final ListDataProvider<T> provider;

	private final Collection<ValueChangeListener> valueChangeListeners;

	private boolean addAllowed = false;

	private boolean search;

	/**
	 * Constructor
	 *
	 * @param em
	 * @param attributeModel
	 * @param service
	 * @param filter
	 * @param search
	 * @param sortOrders
	 */
	@SafeVarargs
	public TokenFieldSelect(EntityModel<T> em, AttributeModel attributeModel, BaseService<ID, T> service,
			SerializablePredicate<T> filter, boolean search, SortOrder<?>... sortOrders) {
		super(service, em, attributeModel, filter);
		extTokenField = new ExtTokenField();
		comboBox = new EntityComboBox<>(em, attributeModel, service, filter, sortOrders);
		provider = new ListDataProvider<>(new ArrayList<>());
		valueChangeListeners = new ArrayList<>();
		this.search = search;
		this.addAllowed = !search && (attributeModel != null && attributeModel.isQuickAddAllowed());
	}

	/**
	 * Adds a token for every selected item
	 */
	private void addTokens() {
		extTokenField.clear();
		if (provider.getItems().size() > 0) {
			for (T item : provider.getItems()) {
				Tokenizable token = new BeanItemTokenizable(item, getEntityModel().getDisplayProperty());
				extTokenField.addTokenizable(token);
			}
		}
		for (ValueChangeListener valueChangeListener : valueChangeListeners) {
			valueChangeListener.valueChange(new ValueChangeEvent(TokenFieldSelect.this, "OLD", false));
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void afterNewEntityAdded(T entity) {
		ListDataProvider<T> provider = (ListDataProvider<T>) comboBox.getDataProvider();
		provider.getItems().add(entity);
		comboBox.setValue(entity);
	}

	/**
	 * Set up a listener to respond to a combo box selection change
	 */
	@SuppressWarnings("unchecked")
	private void attachComboBoxValueChange() {
		comboBox.addValueChangeListener(event -> {
			Object selectedObject = event.getValue();
			if (selectedObject != null) {
				T t = (T) selectedObject;
				provider.getItems().add(t);

				// reset the combo box
				comboBox.setValue(null);
				copyValueFromContainer();
			}
		});
	}

	/**
	 * Respond to a token removal by also removing the corresponding value from the
	 * container
	 */
	@SuppressWarnings("unchecked")
	private void attachTokenFieldValueChange() {
		extTokenField.addTokenRemovedListener(event -> {
			final BeanItemTokenizable tokenizable = (BeanItemTokenizable) event.getTokenizable();
			provider.getItems().remove(tokenizable.getItem());
			copyValueFromContainer();
		});
	}

	@Override
	public void clearAdditionalFilter() {
		super.clearAdditionalFilter();
		if (comboBox != null) {
			comboBox.refresh(getFilter());
			extTokenField.setInputField(comboBox);
		}
	}

	/**
	 * Copies the values from the container to the component
	 */
	private void copyValueFromContainer() {
		Collection<T> values = provider.getItems();
		setValue(Sets.newHashSet(values));
		setComboBoxWidth();
		// validate();
	}

	@Override
	public void focus() {
		super.focus();
		if (comboBox != null) {
			comboBox.focus();
		}
	}

	public EntityComboBox<ID, T> getComboBox() {
		return comboBox;
	}

	// @Override
	// protected List<T> getInternalValue() {
	// if (container.size() == 0) {
	// return new ArrayList<>();
	// }
	// return container.getItemIds();
	// }

	public ExtTokenField getTokenField() {
		return extTokenField;
	}

	@Override
	protected Component initContent() {
		HorizontalLayout layout = new DefaultHorizontalLayout(false, true, false);

		// comboBox.setInputPrompt(getMessageService().getMessage("ocs.type.to.add",
		// VaadinUtils.getLocale()));
		// comboBox.setFilteringMode(FilteringMode.CONTAINS);
		comboBox.setHeightUndefined();
		setComboBoxWidth();

		extTokenField.setInputField(comboBox);
		extTokenField.setEnableDefaultDeleteTokenAction(true);

		attachComboBoxValueChange();
		attachTokenFieldValueChange();
		setupContainerFieldSync();

		layout.addComponent(extTokenField);

		if (addAllowed) {
			Button addButton = constructAddButton();
			layout.addComponent(addButton);
			layout.setExpandRatio(extTokenField, 0.90f);
			layout.setExpandRatio(addButton, 0.10f);
		}

		// initial filling of the field
		addTokens();
		layout.setSizeFull();

		return layout;
	}

	@Override
	public void refresh() {
		if (comboBox != null) {
			comboBox.refresh();
		}
	}

	@Override
	public void refresh(SerializablePredicate<T> filter) {
		if (comboBox != null) {
			comboBox.refresh(filter);
		}
	}

	@Override
	public void setAdditionalFilter(SerializablePredicate<T> additionalFilter) {
		super.setAdditionalFilter(additionalFilter);
		if (comboBox != null) {
			setValue(null);
			comboBox.setValue(null);
			comboBox.refresh(getFilter() == null ? additionalFilter : getFilter().and(additionalFilter));
			extTokenField.setInputField(comboBox);
		}
	}

	/**
	 * Adapt the width of the combo box based on the number of items currently
	 * selected
	 */
	private void setComboBoxWidth() {
		// if selection is empty, set combo box to full width
		if (provider.getItems().size() > 0) {
			comboBox.setWidth(25, Unit.PERCENTAGE);
		} else {
			comboBox.setWidth(100, Unit.PERCENTAGE);
		}
	}


	/**
	 * Update token selections
	 */
	private void setupContainerFieldSync() {
		provider.addDataProviderListener(event -> {
			System.out.println("Adding tokens");
			addTokens();
		});
	}

	@Override
	public void setValue(Collection<T> values) {
		super.setValue(values);
		provider.getItems().clear();
		if (values != null) {
			provider.getItems().addAll(values);
		}
		//addTokens();
	}

	// @Override
	// public void validate() throws InvalidValueException {
	// if (!search && getAttributeModel() != null &&
	// getAttributeModel().isRequired()
	// && container.getItemIds().isEmpty()) {
	// throw new Validator.EmptyValueException(
	// getMessageService().getMessage("ocs.value.required",
	// VaadinUtils.getLocale()));
	// }
	// super.validate();
	// }

	@Override
	public void setComponentError(ErrorMessage componentError) {
		if (extTokenField != null) {
			extTokenField.setComponentError(componentError);
		}
	}

	@Override
	protected void doSetValue(Collection<T> value) {
		if (provider != null) {
			provider.getItems().clear();
			if (value != null && value instanceof Collection) {
				provider.getItems().addAll(value);
			}
		}

	}

	@Override
	public Collection<T> getValue() {
		return provider.getItems();
	}


	@Override
	public Registration addValueChangeListener(ValueChangeListener<Collection<T>> listener) {
		return extTokenField.addValueChangeListener(event ->
			listener.valueChange(new ValueChangeEvent<>(this, this, null, false)));
	}
}
