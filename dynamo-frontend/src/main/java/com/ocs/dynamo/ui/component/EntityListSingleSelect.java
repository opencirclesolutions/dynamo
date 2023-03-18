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
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.EntityModelUtils;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.Getter;

/**
 * Custom ListSelect component for selecting a single item from a list
 *
 * @param <ID> type of the primary key of the entity
 * @param <T>  type of the entity
 */
public class EntityListSingleSelect<ID extends Serializable, T extends AbstractEntity<ID>> extends ListBox<T>
		implements Refreshable, Cascadable<T> {

	private static final long serialVersionUID = 3041574615271340579L;

	@Getter
	private SerializablePredicate<T> additionalFilter;

	@Getter
	private final AttributeModel attributeModel;

	private int count;

	private final EntityModel<T> entityModel;

	@Getter
	private SerializablePredicate<T> filter;

	@Getter
	private SerializablePredicate<T> originalFilter;

	@Getter
	private final SelectMode selectMode;

	private final BaseService<ID, T> service;

	@Getter
	private final SortOrder<?>[] sortOrders;

	@SafeVarargs
	public EntityListSingleSelect(EntityModel<T> entityModel, AttributeModel attributeModel, BaseService<ID, T> service,
			SelectMode mode, SerializablePredicate<T> filter, List<T> items,
			DataProvider<T, SerializablePredicate<T>> sharedProvider, SortOrder<?>... sortOrders) {
		this.entityModel = entityModel;
		this.service = service;
		this.selectMode = mode;
		this.sortOrders = sortOrders;
		this.attributeModel = attributeModel;
		this.filter = filter;
		setHeight(SystemPropertyUtils.getDefaultListSelectHeight());

		DataProvider<T, SerializablePredicate<T>> provider = sharedProvider;
		initProvider(provider, items, mode);

		// non-standard way of setting captions
		setRenderer(new ComponentRenderer<Text, T>(entity -> {
			String label = EntityModelUtils.getDisplayPropertyValue(entity, entityModel);
			return new Text(label == null ? "" : label);
		}));
	}

	/**
	 * Constructor - for the "FILTERED" mode
	 *
	 * @param entityModel    the entity model of the entities that are to be
	 *                       displayed
	 * @param attributeModel the attribute model for the property that is bound to
	 *                       this component
	 * @param service        the service used to retrieve the entities
	 * @param filter         the filter used to filter the entities
	 * @param sortOrder      the sort order used to sort the entities
	 */
	@SafeVarargs
	public EntityListSingleSelect(EntityModel<T> entityModel, AttributeModel attributeModel, BaseService<ID, T> service,
			SerializablePredicate<T> filter, SortOrder<?>... sortOrder) {
		this(entityModel, attributeModel, service, SelectMode.FILTERED_PAGED, filter, null, null, sortOrder);
	}

	/**
	 * Constructor - for the "ALL" mode
	 *
	 * @param entityModel    the entity model of the entities that are to be
	 *                       displayed
	 * @param attributeModel the attribute model for the property that is bound to
	 *                       this component
	 * @param service        the service used to retrieve entities
	 * @param sortOrder      the sort order
	 */
	@SafeVarargs
	public EntityListSingleSelect(EntityModel<T> entityModel, AttributeModel attributeModel, BaseService<ID, T> service,
			SortOrder<?>... sortOrder) {
		this(entityModel, attributeModel, service, SelectMode.ALL, null, null, null, sortOrder);
	}

	/**
	 * Constructor - for the "FIXED" mode
	 *
	 * @param entityModel    the entity model of the entities that are to be
	 *                       displayed
	 * @param attributeModel the attribute model for the property that is bound to
	 *                       this component
	 * @param items          the list of entities to display
	 */
	public EntityListSingleSelect(EntityModel<T> entityModel, AttributeModel attributeModel, List<T> items) {
		this(entityModel, attributeModel, null, SelectMode.FIXED, null, items, null);
	}

	public void afterNewEntityAdded(T entity) {
		if (usesFixedProvider()) {
			getListDataView().addItem(entity);
		} else {
			setItems(createCallbackProvider());
		}
		setValue(entity);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void castAndSetDataProvider(DataProvider<T, SerializablePredicate<T>> provider) {
		if (provider instanceof ListDataProvider ldp) {
			setItems(ldp.getItems());
		}
		else if (provider instanceof CallbackDataProvider cdp) {
			setItems(cdp);
		}
	}

	@Override
	public void clearAdditionalFilter() {
		this.additionalFilter = filter;
		this.filter = originalFilter;
		refresh();
	}

	private CallbackDataProvider<T, Void> createCallbackProvider() {
		return CallbackProviderHelper.createCallbackProviderNoFiltering(service, entityModel, filter,
				new SortOrders(SortUtils.translateSortOrders(sortOrders)), c -> this.count = c);
	}

	public int getDataProviderSize() {
		if (usesFixedProvider()) {
			return getListDataView().getItemCount();
		} else {
			return count;
		}
	}
	
	/**
	 * Initializes the data provider
	 *
	 * @param provider already existing provider (in case of shared provider)
	 * @param items    fixed list of items to display
	 * @param mode     the desired mode
	 * @return
	 */
	private void initProvider(DataProvider<T, SerializablePredicate<T>> provider, List<T> items, SelectMode mode) {
		if (provider == null) {
			if (SelectMode.ALL.equals(mode)) {
				ListDataProvider<T> listProvider = new ListDataProvider<>(
						service.findAll(SortUtils.translateSortOrders(sortOrders)));
				setItems(listProvider);
			} else if (SelectMode.FILTERED_PAGED.equals(mode)) {
				CallbackDataProvider<T, Void> callbackProvider = createCallbackProvider();
				setItems(callbackProvider);
			} else if (SelectMode.FILTERED_ALL.equals(mode)) {
				// add a filtered selection of items
				items = service.find(new FilterConverter<T>(entityModel).convert(filter),
						SortUtils.translateSortOrders(sortOrders));
				setItems(new ListDataProvider<>(items));
			} else if (SelectMode.FIXED.equals(mode)) {
				setItems(new ListDataProvider<>(items));
			}
		} else {
			castAndSetDataProvider(provider);
		}
	}
	
	@Override
	public void refresh() {
		T stored = this.getValue();
		clear();
		updateProvider();
		setValue(stored);
	}

	public void refresh(SerializablePredicate<T> filter) {
		this.originalFilter = filter;
		this.filter = filter;
		refresh();
	}

	@Override
	public void setAdditionalFilter(SerializablePredicate<T> additionalFilter) {
		clear();
		this.additionalFilter = additionalFilter;
		this.filter = originalFilter == null ? additionalFilter : new AndPredicate<>(originalFilter, additionalFilter);
		refresh();
	}
   
	/**
	 * Updates the data provider after a refresh
	 *
	 * @param provider
	 */
	private void updateProvider() {
		if (SelectMode.ALL.equals(selectMode)) {
			setItems(service.findAll(SortUtils.translateSortOrders(sortOrders)));
		} else if (SelectMode.FILTERED_PAGED.equals(selectMode)) {
			setItems(createCallbackProvider());
		} else if (SelectMode.FILTERED_ALL.equals(selectMode)) {
			List<T> items = service.find(new FilterConverter<T>(entityModel).convert(filter),
					SortUtils.translateSortOrders(sortOrders));
			setItems(items);
			//reloadDataProvider(listProvider, items);
		}
	}

	private boolean usesFixedProvider() {
		return SelectMode.ALL.equals(selectMode) || SelectMode.FILTERED_ALL.equals(selectMode) || 
				SelectMode.FIXED.equals(selectMode);
	}

}
