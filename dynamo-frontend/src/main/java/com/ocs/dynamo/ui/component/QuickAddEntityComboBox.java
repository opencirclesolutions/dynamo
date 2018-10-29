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
import com.ocs.dynamo.ui.component.EntityComboBox.SelectMode;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

/**
 * A component that contains a combo box for selecting an entity, plus the
 * option to add new values on the fly
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key of the entity
 * @param <T> the type of the entity
 */
public class QuickAddEntityComboBox<ID extends Serializable, T extends AbstractEntity<ID>>
		extends QuickAddEntityField<ID, T, T> implements Refreshable {

	private static final long serialVersionUID = 4246187881499965296L;

	private static final float BUTTON_EXPAND_RATIO = 0.25f;

	private final boolean quickAddAllowed;

	private final boolean directNavigationAllowed;

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
			SelectMode mode, SerializablePredicate<T> filter, boolean search, List<T> items,
			SortOrder<?>... sortOrder) {
		super(service, entityModel, attributeModel, filter);
		comboBox = new EntityComboBox<>(entityModel, attributeModel, service, mode, filter, items, sortOrder);
		quickAddAllowed = attributeModel != null && attributeModel.isQuickAddAllowed() && !search;
		directNavigationAllowed = attributeModel != null && attributeModel.isDirectNavigation() && !search;
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
	protected void doSetValue(T value) {
		if (comboBox != null) {
			comboBox.setValue(value);
		}
	}

	public EntityComboBox<ID, T> getComboBox() {
		return comboBox;
	}

	@Override
	public T getValue() {
		return comboBox.getValue();
	}

	@Override
	protected Component initContent() {
		HorizontalLayout bar = new DefaultHorizontalLayout(false, true, true);
		bar.setSizeFull();

		if (this.getAttributeModel() != null) {
			this.setCaption(getAttributeModel().getDisplayName());
		}

		// no caption needed (the wrapping component has the caption)
		comboBox.setCaption(null);
		comboBox.setSizeFull();

		bar.addComponent(comboBox);
		float comboBoxExpandRatio = 1f;
		if (quickAddAllowed) {
			comboBoxExpandRatio -= BUTTON_EXPAND_RATIO;
		}
		if (directNavigationAllowed) {
			comboBoxExpandRatio -= 0.10f;
		}

		bar.setExpandRatio(comboBox, comboBoxExpandRatio);

		if (quickAddAllowed) {
			Button addButton = constructAddButton();
			addButton.setSizeFull();
			bar.addComponent(addButton);
			bar.setExpandRatio(addButton, BUTTON_EXPAND_RATIO);
		}
		if (directNavigationAllowed) {
			Button directNavigationButton = constructDirectNavigationButton();
			bar.addComponent(directNavigationButton);
			bar.setExpandRatio(directNavigationButton, 0.10f);
		}
		return bar;
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

	@Override
	public void setAdditionalFilter(SerializablePredicate<T> additionalFilter) {
		super.setAdditionalFilter(additionalFilter);
		if (comboBox != null) {
			comboBox.refresh(
					getFilter() == null ? additionalFilter : new AndPredicate<T>(getFilter(), additionalFilter));
		}
	}

	@Override
	public void setComponentError(ErrorMessage componentError) {
		if (comboBox != null) {
			comboBox.setComponentError(componentError);
		}
	}

	@Override
	public void setValue(T newFieldValue) {
		if (comboBox != null) {
			comboBox.setValue(newFieldValue);
		}
	}

	@Override
	public Registration addValueChangeListener(ValueChangeListener<T> listener) {
		if (comboBox != null) {
			return comboBox.addValueChangeListener(listener);
		}
		return null;
	}
}
