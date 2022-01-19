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
import java.util.Collections;

import com.google.common.collect.Sets;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.SelectMode;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.SharedProvider;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

/**
 * 
 * A ListSelect field that allows the quick addition of simple entities.
 * Supports multiple select use cases
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key of the entity that is being displayed
 * @param <T>  the type of the entity that is being displayed
 */
public class QuickAddTokenSelect<ID extends Serializable, T extends AbstractEntity<ID>>
		extends QuickAddEntityField<ID, T, Collection<T>> implements Refreshable, SharedProvider<T> {

	private static final long serialVersionUID = 4246187881499965296L;

	/**
	 * Whether direct navigation is allowed
	 */
	private final boolean directNavigationAllowed;

	/**
	 * Whether quick adding is allowed
	 */
	private final boolean quickAddAllowed;

	/**
	 * The list select component
	 */
	private final EntityTokenSelect<ID, T> tokenSelect;

	/**
	 * Constructor
	 * 
	 * @param entityModel    the entity model of the entity that is being edited
	 * @param attributeModel the attribute model the component is based on
	 * @param service        service for database communication
	 * @param filter         search filter to apply
	 * @param sharedProvider shared data provider
	 * @param search         whether the component is part of a search form
	 * @param sortOrder      sort orders
	 */
	@SafeVarargs
	public QuickAddTokenSelect(EntityModel<T> entityModel, AttributeModel attributeModel, BaseService<ID, T> service,
			SelectMode selectMode, SerializablePredicate<T> filter,
			DataProvider<T, SerializablePredicate<T>> sharedProvider, boolean search, SortOrder<?>... sortOrder) {
		super(service, entityModel, attributeModel, filter);
		tokenSelect = new EntityTokenSelect<>(entityModel, attributeModel, service, selectMode, filter, null,
				sharedProvider, sortOrder);
		this.quickAddAllowed = !search && attributeModel != null && attributeModel.isQuickAddAllowed();
		this.directNavigationAllowed = !search && attributeModel != null && attributeModel.isNavigable();
		initContent();
	}

	@Override
	public Registration addValueChangeListener(
			ValueChangeListener<? super ComponentValueChangeEvent<CustomField<Collection<T>>, Collection<T>>> listener) {
		if (tokenSelect != null) {
			return tokenSelect
					.addValueChangeListener(event -> listener.valueChanged(new ComponentValueChangeEvent<>(this, this,
							event.getOldValue() == null ? null : Sets.newHashSet(event.getOldValue()),
							event.isFromClient())));
		}
		return null;
	}

	@Override
	protected void afterNewEntityAdded(T entity) {
		tokenSelect.afterNewEntityAdded(entity);
	}

	@Override
	public void clear() {
		if (tokenSelect != null) {
			tokenSelect.clear();
		}
	}

	@Override
	public void clearAdditionalFilter() {
		super.clearAdditionalFilter();
		if (tokenSelect != null) {
			tokenSelect.refresh(getFilter());
		}
	}

	@Override
	protected Collection<T> generateModelValue() {
		return retrieveValue();
	}

	@Override
	@SuppressWarnings("unchecked")
	public DataProvider<T, SerializablePredicate<T>> getSharedProvider() {
		return (DataProvider<T, SerializablePredicate<T>>) tokenSelect.getDataProvider();
	}

	public EntityTokenSelect<ID, T> getTokenSelect() {
		return tokenSelect;
	}

	@Override
	public Collection<T> getValue() {
		return retrieveValue();
	}

	protected void initContent() {
		HorizontalLayout bar = new HorizontalLayout();
		bar.setSizeFull();

		if (this.getAttributeModel() != null) {
			this.setLabel(getAttributeModel().getDisplayName(VaadinUtils.getLocale()));
		}

		// no caption needed (the wrapping component has the caption)
		tokenSelect.setLabel(null);
		tokenSelect.setSizeFull();
		bar.add(tokenSelect);

		if (quickAddAllowed) {
			Button addButton = constructAddButton();
			bar.add(addButton);
		}

		if (directNavigationAllowed) {
			Button directNavigationButton = constructDirectNavigationButton();
			bar.add(directNavigationButton);
		}

		add(bar);
	}

	/**
	 * Refreshes the data in the list
	 */
	@Override
	public void refresh() {
		if (tokenSelect != null) {
			tokenSelect.refresh(getFilter());
		}
	}

	@Override
	public void refresh(SerializablePredicate<T> filter) {
		setFilter(filter);
		if (tokenSelect != null) {
			tokenSelect.refresh(filter);
		}
	}

	@SuppressWarnings("unchecked")
	private Collection<T> retrieveValue() {
		if (tokenSelect != null) {
			return (Collection<T>) convertToCorrectCollection(tokenSelect.getValue());
		}
		return null;
	}

	@Override
	public void setAdditionalFilter(SerializablePredicate<T> additionalFilter) {
		super.setAdditionalFilter(additionalFilter);
		if (tokenSelect != null) {
			tokenSelect.refresh(getFilter() == null ? additionalFilter : getFilter().and(additionalFilter));
			tokenSelect.clear();
		}
	}

	@Override
	public void setErrorMessage(String errorMessage) {
		if (tokenSelect != null) {
			tokenSelect.setErrorMessage(errorMessage);
		}
	}

	@Override
	public void setInvalid(boolean invalid) {
		super.setInvalid(invalid);
		if (tokenSelect != null) {
			tokenSelect.setInvalid(invalid);
		}
	}

	@Override
	public void setPlaceholder(String placeholder) {
		if (tokenSelect != null) {
			tokenSelect.setPlaceholder(placeholder);
		}
	}

	@Override
	protected void setPresentationValue(Collection<T> value) {
		if (tokenSelect != null) {
			if (value == null) {
				value = Collections.emptyList();
			}
			tokenSelect.setValue(Sets.newHashSet(value));
		}
	}

	@Override
	public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
		super.setRequiredIndicatorVisible(requiredIndicatorVisible);
		tokenSelect.setRequiredIndicatorVisible(requiredIndicatorVisible);
	}

	@Override
	public void setValue(Collection<T> value) {
		if (tokenSelect != null) {
			if (value == null) {
				value = Collections.emptyList();
			}
			tokenSelect.setValue(Sets.newHashSet(value));
		}
		super.setValue(value);
	}
}
