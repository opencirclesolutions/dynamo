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
import java.util.Collection;
import java.util.Set;

import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.utils.EntityModelUtils;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;

/**
 * A ListSelect component with an extra combo box for easily searching items.
 * The combo box holds the list of available items. Items that are selected, are
 * added to a ListSelect, which is kept in sync with the actual component value
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the ID of the entity
 * @param <T> the type of the entity
 * @param Set<T> the type of the value (can be a single object or a collection)
 */
public class FancyListSelect<ID extends Serializable, T extends AbstractEntity<ID>>
		extends QuickAddEntityField<ID, T, Collection<T>> implements Refreshable {

	private static final long serialVersionUID = 8129335343598146079L;

	/**
	 * Indicates whether it is allowed to add items
	 */
	private boolean addAllowed;

	/**
	 * Button for clearing the selection
	 */
	private Button clearButton;

	/**
	 * The combo box for selecting an item
	 */
	private EntityComboBox<ID, T> comboBox;

	/**
	 * the bean containers that holds the selected values
	 */
	private ListDataProvider<T> dataProvider;

	/**
	 * The ListSelect component that shows the selected values
	 */
	private ListSelect<T> listSelect;

	/**
	 * The button for removing an item
	 */
	private Button removeButton;

	/**
	 * The button that brings up the search dialog
	 */
	private Button selectButton;

	/**
	 * The sort order to apply to the combo box
	 */
	private SortOrder<?>[] sortOrders;

	/**
	 * Constructor
	 * 
	 * @param service        the service used to query the database
	 * @param entityModel    the entity model
	 * @param attributeModel the attribute mode
	 * @param filter         the filter to apply when searching
	 * @param search         whether the component is used in a search screen
	 * @param sortOrders     the sort order
	 */
	public FancyListSelect(BaseService<ID, T> service, EntityModel<T> entityModel, AttributeModel attributeModel,
			SerializablePredicate<T> filter, boolean search, SortOrder<?>... sortOrders) {
		super(service, entityModel, attributeModel, filter);
		this.sortOrders = sortOrders;
		this.addAllowed = !search && (attributeModel != null && attributeModel.isQuickAddAllowed());
		dataProvider = new ListDataProvider<>(Lists.newArrayList());
		listSelect = new ListSelect<>();
		listSelect.setDataProvider(dataProvider);
		comboBox = new EntityComboBox<>(getEntityModel(), getAttributeModel(), getService(), getFilter(), sortOrders);
	}

	@Override
	public Registration addValueChangeListener(ValueChangeListener<Collection<T>> listener) {
		if (listSelect != null) {
			ValueChangeListener<Set<T>> list = event -> listener.valueChange(new ValueChangeEvent<Collection<T>>(
					FancyListSelect.this, FancyListSelect.this, (Collection<T>) listSelect.getValue(), true));
			return listSelect.addValueChangeListener(list);
		}
		return null;
	}

	@Override
	protected void afterNewEntityAdded(T entity) {
		comboBox.addEntity(entity);
		dataProvider.getItems().add(entity);
		listSelect.getDataProvider().refreshAll();
	}

	@Override
	public void clearAdditionalFilter() {
		super.clearAdditionalFilter();
		if (comboBox != null) {
			comboBox.refresh(getFilter());
		}
	}

	@Override
	protected void doSetValue(Collection<T> value) {
		repopulateContainer(value);
	}

	@Override
	public void focus() {
		super.focus();
		if (comboBox != null) {
			comboBox.focus();
		}
	}

	public Button getClearButton() {
		return clearButton;
	}

	public EntityComboBox<ID, T> getComboBox() {
		return comboBox;
	}

	public int getDataProviderSize() {
		return dataProvider.getItems().size();
	}

	public ListSelect<T> getListSelect() {
		return listSelect;
	}

	public Button getRemoveButton() {
		return removeButton;
	}

	public Button getSelectButton() {
		return selectButton;
	}

	public SortOrder<?>[] getSortOrders() {
		return sortOrders;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<T> getValue() {
		if (listSelect != null) {
			return (Collection<T>) convertToCorrectCollection(listSelect.getValue());
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Component initContent() {
		VerticalLayout layout = new DefaultVerticalLayout(false, false);

		HorizontalLayout firstBar = new DefaultHorizontalLayout(false, true, true);
		firstBar.setSizeFull();

		comboBox.setCaption(null);
		comboBox.setSizeFull();
		firstBar.addComponent(comboBox);

		layout.addComponent(firstBar);

		HorizontalLayout secondBar = new DefaultHorizontalLayout(false, true, true);
		firstBar.addComponent(secondBar);

		// button for selecting an item
		selectButton = new Button(getMessageService().getMessage("ocs.select", VaadinUtils.getLocale()));
		selectButton.addClickListener(event -> {
			if (comboBox.getValue() != null && !dataProvider.getItems().contains(comboBox.getValue())) {
				dataProvider.getItems().add(comboBox.getValue());
				// copyValueFromContainer();
				listSelect.select(comboBox.getValue());
			}
			comboBox.setValue(null);
		});
		secondBar.addComponent(selectButton);

		// adds a button for removing the selected items from the list select
		removeButton = new Button(getMessageService().getMessage("ocs.remove", VaadinUtils.getLocale()));
		removeButton.addClickListener(event -> {
			Object value = listSelect.getValue();
			if (value instanceof Collection) {
				Collection<T> col = (Collection<T>) value;
				for (T t : col) {
					listSelect.deselect(t);
					dataProvider.getItems().remove(t);
				}
			}
		});
		secondBar.addComponent(removeButton);

		// add a button for removing all items at once
		clearButton = new Button(getMessageService().getMessage("ocs.clear", VaadinUtils.getLocale()));
		clearButton.addClickListener(event -> {
			// clear the container
			listSelect.deselectAll();
			dataProvider.getItems().clear();
			listSelect.getDataProvider().refreshAll();
		});
		secondBar.addComponent(clearButton);

		// add a quick add button
		if (addAllowed) {
			Button addButton = constructAddButton();
			secondBar.addComponent(addButton);
		}

		// the list select component shows the currently selected values
		listSelect.setSizeFull();
		listSelect.setItemCaptionGenerator(t -> EntityModelUtils.getDisplayPropertyValue(t, getEntityModel()));
		layout.addComponent(listSelect);

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
		setFilter(filter);
		if (comboBox != null) {
			comboBox.refresh(filter);
		}
	}

	/**
	 * Refill the container after a value change
	 * 
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	private void repopulateContainer(Object value) {
		if (dataProvider != null) {
			dataProvider.getItems().clear();
			if (value != null && value instanceof Collection) {
				dataProvider.getItems().addAll((Collection<T>) value);
			}
		}
	}

	@Override
	public void setAdditionalFilter(SerializablePredicate<T> additionalFilter) {
		super.setAdditionalFilter(additionalFilter);
		if (comboBox != null) {
			comboBox.setValue(null);
			comboBox.refresh(getFilter() == null ? additionalFilter : getFilter().and(additionalFilter));
		}
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
		if (listSelect != null) {
			comboBox.setComponentError(componentError);
			listSelect.setComponentError(componentError);
		}
	}

	public void setRows(int rows) {
		if (listSelect != null) {
			listSelect.setRows(rows);
		}
	}

	@Override
	public void setValue(Collection<T> newFieldValue) {
		super.setValue(newFieldValue);
		repopulateContainer(newFieldValue);
	}
	

}
