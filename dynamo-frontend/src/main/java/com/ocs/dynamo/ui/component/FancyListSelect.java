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
import java.util.HashSet;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.And;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
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
 * @param <ID>
 *            the type of the ID of the entity
 * @param <T>
 *            the type of the entity
 * @param <Object>
 *            the type of the value (can be a single object or a collection)
 */
public class FancyListSelect<ID extends Serializable, T extends AbstractEntity<ID>>
		extends QuickAddEntityField<ID, T, Object> implements Refreshable {

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
	private BeanItemContainer<T> container;

	/**
	 * The ListSelect component that shows the selected values
	 */
	private ListSelect listSelect;

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
	private SortOrder[] sortOrders;

	private boolean search;

	/**
	 * Constructor
	 * 
	 * @param service
	 *            the service used to query the database
	 * @param entityModel
	 *            the entity model
	 * @param attributeModel
	 *            the attribute mode
	 * @param filters
	 *            the filter to apply when searching
	 * @param search
	 *            whether the component is used in a search screen
	 * @param sortOrder
	 *            the sort order
	 * @param joins
	 *            the joins to use when fetching data when filling the popup dialog
	 */
	public FancyListSelect(BaseService<ID, T> service, EntityModel<T> entityModel, AttributeModel attributeModel,
			Filter filter, boolean search, SortOrder... sortOrders) {
		super(service, entityModel, attributeModel, filter);
		this.sortOrders = sortOrders;
		this.search = search;
		this.addAllowed = !search && (attributeModel != null && attributeModel.isQuickAddAllowed());
		container = new BeanItemContainer<>(getEntityModel().getEntityClass());
		listSelect = new ListSelect(null, container);
		comboBox = new EntityComboBox<>(getEntityModel(), getAttributeModel(), getService(), getFilter(), sortOrders);
	}

	@Override
	protected void afterNewEntityAdded(T entity) {
		comboBox.addEntity(entity);
		container.addBean(entity);
		copyValueFromContainer();
	}

	@Override
	public void clearAdditionalFilter() {
		super.clearAdditionalFilter();
		if (comboBox != null) {
			comboBox.refresh(getFilter());
		}
	}

	/**
	 * Copies the selected values from the container behind the ListSelect to the
	 * component value
	 */
	private void copyValueFromContainer() {
		Collection<T> values = container.getItemIds();
		setValue(new HashSet<>(values));
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

	public ListSelect getListSelect() {
		return listSelect;
	}

	public Button getRemoveButton() {
		return removeButton;
	}

	public Button getSelectButton() {
		return selectButton;
	}

	public SortOrder[] getSortOrders() {
		return sortOrders;
	}

	@Override
	public Class<?> getType() {
		return Object.class;
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
			if (comboBox.getValue() != null && !container.containsId(comboBox.getValue())) {
				container.addBean((T) comboBox.getValue());
				copyValueFromContainer();
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
					container.removeItem(t);
					copyValueFromContainer();
				}
			}
		});
		secondBar.addComponent(removeButton);

		// add a button for removing all items at once
		clearButton = new Button(getMessageService().getMessage("ocs.clear", VaadinUtils.getLocale()));
		clearButton.addClickListener(event -> {
			// clear the container
			setValue(new HashSet<>());
			copyValueFromContainer();
		});
		secondBar.addComponent(clearButton);

		// add a quick add button
		if (addAllowed) {
			Button addButton = constructAddButton();
			secondBar.addComponent(addButton);
		}

		// the list select component shows the currently selected values
		listSelect.setSizeFull();
		listSelect.setNullSelectionAllowed(false);
		listSelect.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		listSelect.setItemCaptionPropertyId(getEntityModel().getDisplayProperty());
		listSelect.setMultiSelect(true);
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
	public void refresh(Filter filter) {
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
		if (container != null) {
			container.removeAllItems();
			if (value != null && value instanceof Collection) {
				container.addAll((Collection<T>) value);
			}
		}
	}

	@Override
	public void setAdditionalFilter(Filter additionalFilter) {
		super.setAdditionalFilter(additionalFilter);
		if (comboBox != null) {
			comboBox.setValue(null);
			comboBox.refresh(getFilter() == null ? additionalFilter : new And(getFilter(), additionalFilter));
		}
	}

	@Override
	protected void setInternalValue(Object newValue) {
		super.setInternalValue(newValue);
		repopulateContainer(newValue);
	}

	public void setRows(int rows) {
		if (listSelect != null) {
			listSelect.setRows(rows);
		}
	}

	@Override
	public void setValue(Object newFieldValue) {
		super.setValue(newFieldValue);
		repopulateContainer(newFieldValue);
	}

	@Override
	public void validate() throws InvalidValueException {
		if (!search && getAttributeModel() != null && getAttributeModel().isRequired()
				&& container.getItemIds().isEmpty()) {
			throw new Validator.EmptyValueException(
					getMessageService().getMessage("ocs.value.required", VaadinUtils.getLocale()));
		}
		super.validate();
	}

}
