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
import java.util.List;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.AndPredicate;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.SharedProvider;
import com.ocs.dynamo.ui.component.EntityComboBox.SelectMode;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

/**
 * A component that contains a combo box for selecting an entity, plus the
 * option to add new values on the fly
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key of the entity
 * @param <T>  the type of the entity
 */
public class QuickAddEntityComboBox<ID extends Serializable, T extends AbstractEntity<ID>>
		extends QuickAddEntityField<ID, T, T> implements Refreshable, SharedProvider<T> {

	private static final long serialVersionUID = 4246187881499965296L;

	/**
	 * Whether quick addition of new domain values is allowed
	 */
	private final boolean quickAddAllowed;

	/**
	 * Whether direct navigation from edit screen or grid to another screen is
	 * allowed
	 */
	private final boolean directNavigationAllowed;

	/**
	 * The combo box that holds the actual values
	 */
	private EntityComboBox<ID, T> comboBox;

	/**
	 * Constructor
	 *
	 * @param entityModel    the entity model
	 * @param attributeModel the attribute model
	 * @param service        the service
	 * @param mode           the mode
	 * @param filter         the filter that is used for filtering the data
	 * @param items          the fixed collection of items to display
	 * @param sortOrder      the desired sort order
	 */
	@SafeVarargs
	public QuickAddEntityComboBox(EntityModel<T> entityModel, AttributeModel attributeModel, BaseService<ID, T> service,
			SelectMode mode, SerializablePredicate<T> filter, boolean search, ListDataProvider<T> sharedProvider,
			List<T> items, SortOrder<?>... sortOrder) {
		super(service, entityModel, attributeModel, filter);
		this.comboBox = new EntityComboBox<>(entityModel, attributeModel, service, mode, filter, sharedProvider, items,
				sortOrder);
		this.quickAddAllowed = attributeModel != null && attributeModel.isQuickAddAllowed() && !search;
		this.directNavigationAllowed = attributeModel != null && attributeModel.isNavigable() && !search;
		initContent();
	}

	@Override
	public Registration addValueChangeListener(
			ValueChangeListener<? super ComponentValueChangeEvent<CustomField<T>, T>> listener) {
		if (comboBox != null) {
			comboBox.addValueChangeListener(event -> listener.valueChanged(new ComponentValueChangeEvent<>(this,
					event.getHasValue(), event.getValue(), event.isFromClient())));
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void afterNewEntityAdded(T entity) {
		ListDataProvider<T> provider = (ListDataProvider<T>) comboBox.getDataProvider();
		provider.getItems().add(entity);
		comboBox.setValue(entity);
	}

	@Override
	public void clearAdditionalFilter() {
		super.clearAdditionalFilter();
		if (comboBox != null) {
			comboBox.refresh(getFilter());
		}
	}

	@Override
	public void focus() {
		if (comboBox != null) {
			comboBox.focus();
		}
	}

	@Override
	protected T generateModelValue() {
		return comboBox.getValue();
	}

	public EntityComboBox<ID, T> getComboBox() {
		return comboBox;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ListDataProvider<T> getSharedProvider() {
		return (ListDataProvider<T>) comboBox.getDataProvider();
	}

	@Override
	public T getValue() {
		return comboBox.getValue();
	}

	protected void initContent() {

		HorizontalLayout bar = new HorizontalLayout();
		if (this.getAttributeModel() != null) {
			this.setLabel(getAttributeModel().getDisplayName(VaadinUtils.getLocale()));
		}

		// no caption needed (the wrapping component has the caption)
		comboBox.setLabel(null);

		bar.add(comboBox);
		bar.setFlexGrow(2, comboBox);
		if (quickAddAllowed) {
			Button addButton = constructAddButton();
			bar.add(addButton);
			bar.setFlexGrow(1, addButton);
		}
		if (directNavigationAllowed) {
			Button directNavigationButton = constructDirectNavigationButton();
			bar.add(directNavigationButton);
			bar.setFlexGrow(1, directNavigationButton);
		}

		add(bar);
	}

	@Override
	public void refresh() {
		if (comboBox != null) {
			comboBox.refresh(getFilter());
		}
	}

	@Override
	public void refresh(SerializablePredicate<T> filter) {
		setFilter(filter);
		if (comboBox != null) {
			comboBox.refresh(filter);
		}
	}

	@Override
	public void setAdditionalFilter(SerializablePredicate<T> additionalFilter) {
		super.setAdditionalFilter(additionalFilter);
		if (comboBox != null) {
			comboBox.refresh(
					getFilter() == null ? additionalFilter : new AndPredicate<T>(getFilter(), additionalFilter));
		}
	}

	@Override
	public void setInvalid(boolean invalid) {
		super.setInvalid(invalid);
		if (comboBox != null) {
			comboBox.setInvalid(invalid);
		}
	}

	@Override
	public void setPlaceholder(String placeholder) {
		if (comboBox != null) {
			comboBox.setPlaceholder(placeholder);
		}
	}

	@Override
	protected void setPresentationValue(T value) {
		if (comboBox != null) {
			comboBox.setValue(value);
		}
	}

	@Override
	public void setValue(T newFieldValue) {
		if (comboBox != null) {
			comboBox.setValue(newFieldValue);
		}
	}

}
