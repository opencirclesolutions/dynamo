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

import com.google.gwt.thirdparty.guava.common.collect.Sets;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Refreshable;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * 
 * A ListSelect field that allows the quick addition of simple entities.
 * Supports both multiple select and single select use cases
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key of the entity that is being displayed
 * @param <T> the type of the entity that is being displayed
 */
public class QuickAddListSelect<ID extends Serializable, T extends AbstractEntity<ID>>
		extends QuickAddEntityField<ID, T, Collection<T>> implements Refreshable {

	private static final long serialVersionUID = 4246187881499965296L;

	/**
	 * The list select component
	 */
	private EntityListSelect<ID, T> listSelect;

	/**
	 * Whether the component is in view mode
	 */
	private boolean viewMode;

	/**
	 * Whether quick adding is allowed
	 */
	private boolean quickAddAllowed;

	/**
	 * Whether direct navigation is allowed
	 */
	private boolean directNavigationAllowed;

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
	public QuickAddListSelect(EntityModel<T> entityModel, AttributeModel attributeModel, BaseService<ID, T> service,
			SerializablePredicate<T> filter, boolean multiSelect, int rows, SortOrder<?>... sortOrder) {
		super(service, entityModel, attributeModel, filter);
		listSelect = new EntityListSelect<>(entityModel, attributeModel, service, filter, sortOrder);
		listSelect.setRows(rows);
		this.quickAddAllowed = attributeModel != null && attributeModel.isQuickAddAllowed();
		this.directNavigationAllowed = attributeModel != null && attributeModel.isDirectNavigation();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void afterNewEntityAdded(T entity) {
		// add to the container
		ListDataProvider<T> provider = (ListDataProvider<T>) listSelect.getDataProvider();
		provider.getItems().add(entity);
		listSelect.select(entity);
	}

	@Override
	public void clearAdditionalFilter() {
		super.clearAdditionalFilter();
		if (listSelect != null) {
			listSelect.refresh(getFilter());
		}
	}

	public EntityListSelect<ID, T> getListSelect() {
		return listSelect;
	}

	@Override
	protected Component initContent() {
		HorizontalLayout bar = new DefaultHorizontalLayout(false, true, true);
		bar.setSizeFull();

		if (this.getAttributeModel() != null) {
			this.setCaption(getAttributeModel().getDisplayName());
		}

		// no caption needed (the wrapping component has the caption)
		listSelect.setCaption(null);
		listSelect.setSizeFull();
		listSelect.addValueChangeListener(event -> setValue(event.getValue()));

		bar.addComponent(listSelect);

		float listExpandRatio = 1f;
		if (quickAddAllowed && !viewMode) {
			listExpandRatio -= 0.10f;
		}
		if (directNavigationAllowed) {
			listExpandRatio -= 0.05f;
		}

		bar.setExpandRatio(listSelect, listExpandRatio);

		if (!viewMode && quickAddAllowed) {
			Button addButton = constructAddButton();
			bar.addComponent(addButton);
			bar.setExpandRatio(addButton, 0.10f);
		}
		if (directNavigationAllowed) {
			Button directNavigationButton = constructDirectNavigationButton();
			bar.addComponent(directNavigationButton);
			bar.setExpandRatio(directNavigationButton, 0.05f);
		}
		return bar;
	}

	/**
	 * Refreshes the data in the list
	 */
	@Override
	public void refresh() {
		if (listSelect != null) {
			listSelect.refresh();
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
	public void setValue(Collection<T> value) {
		super.setValue(value);
		if (listSelect != null) {
			if (value == null){
				value = Collections.emptyList();
			}
			listSelect.setValue(Sets.newHashSet(value));
		}
	}

	public void setViewMode(boolean viewMode) {
		this.viewMode = viewMode;
	}

	@Override
	public Collection<T> getValue() {
		if (listSelect != null) {
			return listSelect.getValue();
		}
		return null;
	}

	@Override
	protected void doSetValue(Collection<T> value) {
		if (listSelect != null) {
			if (value == null){
				value = Collections.emptyList();
			}
			listSelect.setValue(Sets.newHashSet(value));
		}
	}

	@Override
	public Registration addValueChangeListener(final ValueChangeListener<Collection<T>> listener) {

		return listSelect.addValueChangeListener(event -> {
			listener.valueChange(new ValueChangeEvent<>(this, this, null, false ));
		});
	}
}
