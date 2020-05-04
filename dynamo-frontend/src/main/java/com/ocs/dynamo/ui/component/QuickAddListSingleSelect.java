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

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.SharedProvider;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

/**
 * 
 * A ListSelect field that allows the quick addition of simple entities.
 * Supports single select use cases
 *
 * @param <ID> the type of the primary key of the entity that is being displayed
 * @param <T>  the type of the entity that is being displayed
 */
public class QuickAddListSingleSelect<ID extends Serializable, T extends AbstractEntity<ID>>
		extends QuickAddEntityField<ID, T, T> implements Refreshable, SharedProvider<T> {

	private static final long serialVersionUID = 4246187881499965296L;

	/**
	 * Whether direct navigation is allowed
	 */
	private boolean directNavigationAllowed;

	/**
	 * The list select component
	 */
	private EntityListSingleSelect<ID, T> listSelect;

	/**
	 * Whether quick adding is allowed
	 */
	private boolean quickAddAllowed;

	/**
	 * Constructor
	 *
	 * @param entityModel
	 * @param attributeModel
	 * @param service
	 * @param filter
	 * @param multiSelect
	 * @param rows
	 * @param sortOrder
	 */
	@SafeVarargs
	public QuickAddListSingleSelect(EntityModel<T> entityModel, AttributeModel attributeModel,
			BaseService<ID, T> service, SerializablePredicate<T> filter, ListDataProvider<T> sharedProvider,
			boolean search, SortOrder<?>... sortOrder) {
		super(service, entityModel, attributeModel, filter);
		listSelect = new EntityListSingleSelect<>(entityModel, attributeModel, service, filter, sharedProvider,
				sortOrder);
		this.quickAddAllowed = !search && attributeModel != null && attributeModel.isQuickAddAllowed();
		this.directNavigationAllowed = !search && attributeModel != null && attributeModel.isNavigable();
		initContent();
	}

	@Override
	public Registration addValueChangeListener(
			ValueChangeListener<? super ComponentValueChangeEvent<CustomField<T>, T>> listener) {
		if (listSelect != null) {
			return listSelect
					.addValueChangeListener(event -> listener.valueChanged(new ComponentValueChangeEvent<>(this,
							event.getHasValue(), event.getValue(), event.isFromClient())));
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void afterNewEntityAdded(T entity) {
		// add to the container
		ListDataProvider<T> provider = (ListDataProvider<T>) listSelect.getDataProvider();
		provider.getItems().add(entity);
		listSelect.setValue(entity);
	}

	@Override
	public void clear() {
		if (listSelect != null) {
			listSelect.clear();
		}
	}

	@Override
	public void clearAdditionalFilter() {
		super.clearAdditionalFilter();
		if (listSelect != null) {
			listSelect.refresh(getFilter());
		}
	}

	@Override
	protected T generateModelValue() {
		if (listSelect != null) {
			return listSelect.getValue();
		}
		return null;
	}

	public EntityListSingleSelect<ID, T> getListSelect() {
		return listSelect;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ListDataProvider<T> getSharedProvider() {
		return (ListDataProvider<T>) listSelect.getDataProvider();
	}

	@Override
	public T getValue() {
		if (listSelect != null) {
			return listSelect.getValue();
		}
		return null;
	}

	protected void initContent() {
		if (!quickAddAllowed && !directNavigationAllowed) {
			// just add the list select, no need for any buttons
			listSelect.setWidthFull();
			add(listSelect);
		} else {
			HorizontalLayout bar = new HorizontalLayout();
			bar.setSizeFull();

			if (this.getAttributeModel() != null) {
				this.setLabel(getAttributeModel().getDisplayName(VaadinUtils.getLocale()));
			}

			listSelect.addValueChangeListener(event -> setValue(event.getValue()));

			bar.add(listSelect);
			if (quickAddAllowed) {
				Button addButton = constructAddButton();
				addButton.setSizeFull();
				bar.add(addButton);

			}
			if (directNavigationAllowed) {
				Button directNavigationButton = constructDirectNavigationButton();
				directNavigationButton.setSizeFull();
				bar.add(directNavigationButton);
			}

			add(bar);
		}
	}

	/**
	 * Refreshes the data in the list
	 */
	@Override
	public void refresh() {
		if (listSelect != null) {
			listSelect.refresh(getFilter());
		}
	}

	@Override
	public void refresh(SerializablePredicate<T> filter) {
		setFilter(filter);
		if (listSelect != null) {
			listSelect.refresh(filter);
		}
	}

	@Override
	public void setAdditionalFilter(SerializablePredicate<T> additionalFilter) {
		super.setAdditionalFilter(additionalFilter);
		if (listSelect != null) {
			listSelect.refresh(getFilter() == null ? additionalFilter : getFilter().and(additionalFilter));
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void setPresentationValue(T value) {
		if (listSelect != null) {
			// select the item if it's included in the item list
			ListDataProvider<T> provider = (ListDataProvider<T>) listSelect.getDataProvider();
			if (provider.getItems().contains(value)) {
				listSelect.setValue(value);
			}
		}
	}

}
