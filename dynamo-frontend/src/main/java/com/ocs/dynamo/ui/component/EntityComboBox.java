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

import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.SelectMode;
import com.ocs.dynamo.filter.AndPredicate;
import com.ocs.dynamo.filter.FilterConverter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.utils.SortUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.utils.EntityModelUtils;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.Getter;

/**
 * Combo box for displaying a list of entities.
 */
public class EntityComboBox<ID extends Serializable, T extends AbstractEntity<ID>> extends ComboBox<T>
		implements Refreshable, Cascadable<T> {

	private static final long serialVersionUID = 3041574615271340579L;

	@Getter
	private SerializablePredicate<T> additionalFilter;

	@Getter
	private final AttributeModel attributeModel;

	private final EntityModel<T> entityModel;

	@Getter
	private SerializablePredicate<T> predicate;

	private SerializablePredicate<T> originalFilter;

	@Getter
	private final SelectMode selectMode;

	@Getter
	private final BaseService<ID, T> service;

	@Getter
	private final SortOrder<?>[] sortOrders;

	private int count;

	/**
	 * Constructor
	 *
	 * @param entityModel    the entity model for the entities that are displayed in
	 *                       the combo box
	 * @param attributeModel the attribute model for the attribute to which the
	 *                       selected value will be assigned
	 * @param service        the service used to retrieve entities
	 * @param mode           the selection mode
	 * @param filter         optional filters to apply to the search
	 * @param sharedProvider shared data provider when using the component inside a
	 *                       grid
	 * @param items          the items to display (in fixed mode)
	 * @param sortOrders     the sort order(s) to apply
	 * 
	 */
	public EntityComboBox(EntityModel<T> entityModel, AttributeModel attributeModel, BaseService<ID, T> service,
			SelectMode mode, SerializablePredicate<T> filter, List<T> items,
			DataProvider<T, SerializablePredicate<T>> sharedProvider, SortOrder<?>... sortOrders) {
		this.service = service;
		this.selectMode = mode;
		this.sortOrders = sortOrders;
		this.attributeModel = attributeModel;
		this.predicate = filter;
		this.entityModel = entityModel;
		if (attributeModel != null) {
			this.setLabel(attributeModel.getDisplayName(VaadinUtils.getLocale()));
		}

		initProvider(sharedProvider, items, mode);
		setItemLabelGenerator(t -> EntityModelUtils.getDisplayPropertyValue(t, entityModel));
		setSizeFull();
	}

	/**
	 * Constructor (for a filtering combo box)
	 *
	 * @param entityModel    the entity model for the entities that are displayed in
	 *                       the box
	 * @param attributeModel the attribute model
	 * @param service        the service for querying the database
	 * @param filter         the filter to apply when searching
	 * @param sortOrders     the sort orders that will be used
	 */
	public EntityComboBox(EntityModel<T> entityModel, AttributeModel attributeModel, BaseService<ID, T> service,
			SerializablePredicate<T> filter, SortOrder<?>... sortOrders) {
		this(entityModel, attributeModel, service, SelectMode.FILTERED_PAGED, filter, null, null, sortOrders);
	}

	/**
	 * Constructor - for the "ALL"mode
	 *
	 * @param entityModel    the entity model for the entities that are displayed
	 * @param attributeModel the attribute model of the property that is bound to
	 *                       this component
	 * @param service        the service used to retrieve the entities
	 */
	public EntityComboBox(EntityModel<T> entityModel, AttributeModel attributeModel, BaseService<ID, T> service,
			SortOrder<?>... sortOrders) {
		this(entityModel, attributeModel, service, SelectMode.ALL, null, null, null, sortOrders);
	}

	/**
	 * Constructor - for the "FIXED" mode
	 *
	 * @param entityModel    the entity model for the entities that are displayed
	 * @param attributeModel the attribute model of the property that is bound to
	 *                       this component
	 * @param items          the list of entities to display
	 */
	public EntityComboBox(EntityModel<T> entityModel, AttributeModel attributeModel, List<T> items) {
		this(entityModel, attributeModel, null, SelectMode.FIXED, null, items, null);
	}

	@SuppressWarnings("unchecked")
	protected void afterNewEntityAdded(T entity) {
		if (getDataProvider() instanceof ListDataProvider) {
			ListDataProvider<T> provider = (ListDataProvider<T>) getDataProvider();
			provider.getItems().add(entity);
		} else {
			updateProvider((DataProvider<T, SerializablePredicate<T>>) getDataProvider());
		}
		setValue(entity);
	}

	@SuppressWarnings({ "unchecked" })
	private void castAndSetDataProvider(DataProvider<T, SerializablePredicate<T>> provider) {
		if (provider instanceof CallbackDataProvider callbackDataProvider) {
			setItems(callbackDataProvider);
		} else if (provider instanceof ListDataProvider listDataProvider) {
			setItems(new IgnoreDiacriticsCaptionFilter<>(entityModel, true, false), listDataProvider);
		}
	}

	@Override
	public void clearAdditionalFilter() {
		this.additionalFilter = null;
		this.predicate = originalFilter;
		refresh();
	}

	private CallbackDataProvider<T, String> createCallbackProvider() {
		return CallbackProviderHelper.createCallbackProvider(service, entityModel, predicate,
				new SortOrders(SortUtils.translateSortOrders(sortOrders)), c -> this.count = c);
	}

	public int getDataProviderSize() {
		if (getDataProvider() instanceof ListDataProvider) {
			return ((ListDataProvider<?>) getDataProvider()).getItems().size();
		} else if (getDataProvider() instanceof CallbackDataProvider) {
			return count;
		}
		return 0;
	}

	/**
	 * Initializes the data provider
	 * 
	 * @param provider already existing provider (in case of shared provider)
	 * @param items    fixed list of items to display
	 * @param mode     the desired mode
	 */
	private void initProvider(DataProvider<T, SerializablePredicate<T>> provider, List<T> items, SelectMode mode) {
		if (provider == null) {
			if (SelectMode.ALL.equals(mode)) {
				ListDataProvider<T> listProvider = new ListDataProvider<>(
						service.findAll(SortUtils.translateSortOrders(sortOrders)));
				setItems(new IgnoreDiacriticsCaptionFilter<>(entityModel, true, false), listProvider);
			} else if (SelectMode.FILTERED_PAGED.equals(mode)) {
				CallbackDataProvider<T, String> callbackProvider = createCallbackProvider();
				setItems(callbackProvider);
			} else if (SelectMode.FILTERED_ALL.equals(mode)) {
				items = service.find(new FilterConverter<>(entityModel).convert(predicate),
						SortUtils.translateSortOrders(sortOrders));
				setItems(new IgnoreDiacriticsCaptionFilter<>(entityModel, true, false), new ListDataProvider<>(items));
			} else if (SelectMode.FIXED.equals(mode)) {
				setItems(new IgnoreDiacriticsCaptionFilter<>(entityModel, true, false), new ListDataProvider<>(items));
			}
		} else {
			castAndSetDataProvider(provider);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void refresh() {
		T stored = this.getValue();
		clear();
		DataProvider<T, ?> provider = getDataProvider();
		updateProvider((DataProvider<T, SerializablePredicate<T>>) provider);
		setValue(stored);
	}

	public void refresh(SerializablePredicate<T> filter) {
		this.originalFilter = filter;
		this.predicate = filter;
		refresh();
	}

	private void reloadDataProvider(ListDataProvider<T> listProvider, List<T> items) {
		listProvider.getItems().clear();
		listProvider.getItems().addAll(items);
		listProvider.refreshAll();
	}

	@Override
	public void setAdditionalFilter(SerializablePredicate<T> additionalFilter) {
		clear();
		this.additionalFilter = additionalFilter;
		this.predicate = originalFilter == null ? additionalFilter
				: new AndPredicate<>(originalFilter, additionalFilter);
		refresh();
	}

	/**
	 * Updates the data provider after a refresh
	 * 
	 * @param provider the provider to update
	 */
	private void updateProvider(DataProvider<T, SerializablePredicate<T>> provider) {
		if (SelectMode.ALL.equals(selectMode)) {
			ListDataProvider<T> listProvider = (ListDataProvider<T>) provider;
			reloadDataProvider(listProvider, service.findAll(SortUtils.translateSortOrders(sortOrders)));
		} else if (SelectMode.FILTERED_PAGED.equals(selectMode)) {
			setItems(createCallbackProvider());
		} else if (SelectMode.FILTERED_ALL.equals(selectMode)) {
			ListDataProvider<T> listProvider = (ListDataProvider<T>) provider;
			List<T> items = service.find(new FilterConverter<>(entityModel).convert(predicate),
					SortUtils.translateSortOrders(sortOrders));
			reloadDataProvider(listProvider, items);
		}
	}
}
