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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.explicatis.ext_token_field.ExtTokenField;
import com.explicatis.ext_token_field.SimpleTokenizable;
import com.explicatis.ext_token_field.Tokenizable;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.FilterConverter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Refreshable;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.Registration;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;

/**
 * A token field that displays the distinct values for a basic property of an
 * entity
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key
 * @param <S> the type of the entity
 * @param <T> the type of the basic property
 */
public class SimpleTokenFieldSelect<ID extends Serializable, S extends AbstractEntity<ID>, T extends Comparable<T>>
		extends CustomField<Collection<T>> implements Refreshable {

	private static final long serialVersionUID = -1490179285573442827L;

	/**
	 * The attribute model
	 */
	private AttributeModel attributeModel;

	/**
	 * The combo box that holds the values to select from
	 */
	private final ComboBox<T> comboBox;

	private String distinctField;

	/**
	 * Whether to take the values from an element collection grid
	 */
	private final boolean elementCollection;

	/**
	 * 
	 */
	private Class<T> elementType;

	/**
	 * The entity model
	 */
	private EntityModel<S> entityModel;

	/**
	 * The token field
	 */
	private final ExtTokenField extTokenField;

	/**
	 * 
	 */
	private SerializablePredicate<S> fieldFilter;

	/**
	 * Data provider that contains the selected items
	 */
	private final ListDataProvider<T> provider;

	/**
	 * Service for querying the database
	 */
	private BaseService<ID, S> service;

	/**
	 * Value change listeners
	 */
	private final Collection<ValueChangeListener<Collection<T>>> valueChangeListeners;

	/**
	 * Constructor
	 *
	 * @param service
	 *
	 * @param entityModel
	 *
	 * @param attributeModel the attribute model
	 * @param fieldFilter    the list of items to display
	 * @param distinctField
	 * @param elementType    the type of the items to display
	 * @param sortOrders     sort orders to apply
	 */
	public SimpleTokenFieldSelect(BaseService<ID, S> service, EntityModel<S> entityModel, AttributeModel attributeModel,
			SerializablePredicate<S> fieldFilter, String distinctField, Class<T> elementType,
			boolean elementCollection) {
		this.service = service;
		this.entityModel = entityModel;
		this.fieldFilter = fieldFilter;
		this.distinctField = distinctField;
		this.elementType = elementType;
		this.elementCollection = elementCollection;
		this.attributeModel = attributeModel;

		setCaption(attributeModel.getDisplayName());

		extTokenField = new ExtTokenField();

		provider = new ListDataProvider<T>(new ArrayList<>());
		comboBox = new ComboBox<T>();

		fillComboBox(this.elementCollection);
		valueChangeListeners = new ArrayList<>();
	}

	private void addTokens() {
		extTokenField.clear();
		if (provider.getItems().size() > 0) {
			for (T item : provider.getItems()) {
				Tokenizable token = new SimpleTokenizable(System.nanoTime(), item.toString());
				extTokenField.addTokenizable(token);
			}
		}

		for (ValueChangeListener<Collection<T>> valueChangeListener : valueChangeListeners) {
			valueChangeListener.valueChange(new ValueChangeEvent<>(SimpleTokenFieldSelect.this, null, false));
		}
	}

	@Override
	public Registration addValueChangeListener(ValueChangeListener<Collection<T>> listener) {
		valueChangeListeners.add(listener);
		return null;
	}

	/**
	 * Sets up a listener that adds a token in response to a selection in the combo
	 * box
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
				provider.refreshAll();
			}
		});
	}

	/**
	 * Respond to a token removal by also removing the corresponding value from the
	 * container
	 */
	private void attachTokenFieldValueChange() {
		extTokenField.addTokenRemovedListener(event -> {
			final SimpleTokenizable tokenizable = (SimpleTokenizable) event.getTokenizable();
			provider.getItems().remove(tokenizable.getStringValue());
			provider.refreshAll();
		});
	}

	@Override
	protected void doSetValue(Collection<T> value) {
		if (provider != null) {
			provider.getItems().clear();
			if (value != null && value instanceof Collection) {
				provider.getItems().addAll(value);
			}
			provider.refreshAll();
		}
	}

	/**
	 * Fills the combo box with the avaiable values
	 * 
	 * @param elementCollection
	 */
	private void fillComboBox(boolean elementCollection) {
		List<T> items = null;
		if (elementCollection) {
			// search element collection table
			items = service.findDistinctInCollectionTable(attributeModel.getCollectionTableName(),
					attributeModel.getCollectionTableFieldName(), elementType);
		} else {
			// search field in regular table
			items = service.findDistinct(new FilterConverter<S>(entityModel).convert(fieldFilter), distinctField,
					elementType);
		}

		items = items.stream().filter(i -> i != null).collect(Collectors.toList());
		items.sort(Comparator.naturalOrder());
		comboBox.setDataProvider(new ListDataProvider<T>(items));

	}

	public ComboBox<T> getComboBox() {
		return comboBox;
	}

	public ExtTokenField getTokenField() {
		return extTokenField;
	}

	@Override
	public Collection<T> getValue() {
		return provider.getItems();
	}

	@Override
	protected Component initContent() {
		HorizontalLayout layout = new DefaultHorizontalLayout(false, true, false);

		comboBox.setHeightUndefined();

		extTokenField.setInputField(comboBox);
		extTokenField.setEnableDefaultDeleteTokenAction(true);

		attachComboBoxValueChange();
		attachTokenFieldValueChange();
		setupProviderSync();

		layout.addComponent(extTokenField);
		layout.setSizeFull();

		return layout;
	}

	@Override
	public void refresh() {
		if (comboBox != null) {
			fillComboBox(elementCollection);
		}
	}

	/**
	 * Update token selections
	 */
	private void setupProviderSync() {
		provider.addDataProviderListener(event -> addTokens());
	}

}
