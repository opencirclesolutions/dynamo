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
import com.ocs.dynamo.filter.FilterConverter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.utils.EntityModelUtil;
import com.ocs.dynamo.ui.utils.SortUtil;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.ComboBox;

/**
 * Combo box for displaying a list of entities. This component supports
 * filtering
 */
public class EntityComboBox<ID extends Serializable, T extends AbstractEntity<ID>> extends ComboBox<T>
		implements Refreshable, Cascadable<T> {

	public enum SelectMode {
		ALL, FILTERED, FIXED;
	}

	private static final long serialVersionUID = 3041574615271340579L;

	/**
	 * The service
	 */
	private BaseService<ID, T> service;

	/**
	 * The attribute mode
	 */
	private AttributeModel attributeModel;

	/**
	 * The select mode
	 */
	private SelectMode selectMode = SelectMode.FILTERED;

	/**
	 * The sort orders
	 */
	private final SortOrder<?>[] sortOrders;

	/**
	 * The current search filter
	 */
	private SerializablePredicate<T> filter;

	/**
	 * The original search filter (without cascades)
	 */
	private SerializablePredicate<T> originalFilter;

	/**
	 * Additional filter for cascading
	 */
	private SerializablePredicate<T> additionalFilter;

	/**
	 * The entity model
	 */
	private EntityModel<T> targetEntityModel;

	/**
	 * Constructor
	 *
	 * @param targetEntityModel the entity model for the entities that are displayed
	 *                          in the combo box
	 * @param attributeModel    the attribute model for the attribute to which the
	 *                          selected value will be assigned
	 * @param service           the service used to retrieve entities
	 * @param mode              the select mode
	 * @param filter            optional filters to apply to the search
	 * @param items             the items to display (in fixed mode)
	 * @param sortOrder         the sort order(s) to apply
	 */
	public EntityComboBox(EntityModel<T> targetEntityModel, AttributeModel attributeModel, BaseService<ID, T> service,
			SelectMode mode, SerializablePredicate<T> filter, List<T> items, SortOrder<?>... sortOrders) {
		this.service = service;
		this.selectMode = mode;
		this.sortOrders = sortOrders;
		this.attributeModel = attributeModel;
		this.filter = filter;
		this.targetEntityModel = targetEntityModel;
		if (attributeModel != null) {
			this.setCaption(attributeModel.getDisplayName());
		}

		setFilter(filter);
		ListDataProvider<T> provider = null;

		if (SelectMode.ALL.equals(mode)) {
			// add all items (but sorted)
			provider = new ListDataProvider<>(
					service.findAll(SortUtil.translateAndFilterOnTransient(false, targetEntityModel, sortOrders)));
		} else if (SelectMode.FILTERED.equals(mode)) {
			// add a filtered selection of items
			items = service.find(new FilterConverter<T>(targetEntityModel).convert(filter),
					SortUtil.translateAndFilterOnTransient(false, targetEntityModel, sortOrders));
			provider = new ListDataProvider<>(items);
		} else if (SelectMode.FIXED.equals(mode)) {
			provider = new ListDataProvider<>(items);
		}
		setDataProvider(provider);

		// TODO: fix the sort order
		// SortUtil.applyContainerSortOrder(container, true, targetEntityModel,
		// sortOrders);

		setItemCaptionGenerator(t -> EntityModelUtil.getDisplayPropertyValue(t, targetEntityModel));
		setSizeFull();
	}

	/**
	 * Constructor (for a filtering combo box)
	 *
	 * @param targetEntityModel
	 * @param attributeModel
	 * @param service
	 * @param filter
	 * @param sortOrder
	 */
	@SafeVarargs
	public EntityComboBox(EntityModel<T> targetEntityModel, AttributeModel attributeModel, BaseService<ID, T> service,
			SerializablePredicate<T> filter, SortOrder<?>... sortOrders) {
		this(targetEntityModel, attributeModel, service, SelectMode.FILTERED, filter, null, sortOrders);
	}

	/**
	 * Constructor - for the "ALL"mode
	 *
	 * @param targetEntityModel the entity model for the entities that are displayed
	 * @param attributeModel    the attribute model of the property that is bound to
	 *                          this component
	 * @param service           the service used to retrieve the entities
	 */
	@SafeVarargs
	public EntityComboBox(EntityModel<T> targetEntityModel, AttributeModel attributeModel, BaseService<ID, T> service,
			SortOrder<T>... sortOrder) {
		this(targetEntityModel, attributeModel, service, SelectMode.ALL, null, null, sortOrder);
	}

	/**
	 * Constructor - for the "FIXED" mode
	 *
	 * @param targetEntityModel the entity model for the entities that are displayed
	 * @param attributeModel    the attribute model of the property that is bound to
	 *                          this component
	 * @param items             the list of entities to display
	 */
	public EntityComboBox(EntityModel<T> targetEntityModel, AttributeModel attributeModel, List<T> items) {
		this(targetEntityModel, attributeModel, null, SelectMode.FIXED, null, items);
	}

	/**
	 * Adds an entity to the container
	 *
	 * @param entity
	 */
	@SuppressWarnings("unchecked")
	public void addEntity(T entity) {
		ListDataProvider<T> bic = (ListDataProvider<T>) this.getDataProvider();
		bic.getItems().add(entity);
	}

	/**
	 * Overwritten so that diacritics are ignored when comparing
	 */
	// @Override
	// protected SerializablePredicate<T> buildFilter(String filterString,
	// FilteringMode filteringMode) {
	// SerializablePredicate<T> ft = null;
	//
	// if (!StringUtils.isEmpty(filterString)) {
	// switch (filteringMode) {
	// case STARTSWITH:
	// ft = new IgnoreDiacriticsStringFilter(getItemCaptionPropertyId(),
	// filterString, true, true);
	// break;
	// case CONTAINS:
	// ft = new IgnoreDiacriticsStringFilter(getItemCaptionPropertyId(),
	// filterString, true, false);
	// break;
	// default:
	// break;
	// }
	// }
	// return ft;
	// }

	@Override
	public void clearAdditionalFilter() {
		this.additionalFilter = null;
		this.filter = originalFilter;
		refresh();
	}

	@Override
	public SerializablePredicate<T> getAdditionalFilter() {
		return additionalFilter;
	}

	public AttributeModel getAttributeModel() {
		return attributeModel;
	}

	@SuppressWarnings("unchecked")
	public int getDataProviderSize() {
		ListDataProvider<T> bic = (ListDataProvider<T>) this.getDataProvider();
		return bic.getItems().size();
	}

	public SerializablePredicate<T> getFilter() {
		return filter;
	}

	@SuppressWarnings("unchecked")
	public T getFirstItem() {
		ListDataProvider<T> bic = (ListDataProvider<T>) this.getDataProvider();
		return bic.getItems().iterator().next();
	}

	public SelectMode getSelectMode() {
		return selectMode;
	}

	public SortOrder<?>[] getSortOrder() {
		return sortOrders;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void refresh() {
		ListDataProvider<T> provider = (ListDataProvider<T>) getDataProvider();
		if (SelectMode.ALL.equals(selectMode)) {
			// add all items (but sorted)
			provider.getItems().clear();
			provider.getItems().addAll(
					service.findAll(SortUtil.translateAndFilterOnTransient(false, targetEntityModel, sortOrders)));
		} else if (SelectMode.FILTERED.equals(selectMode)) {
			// add a filtered selection of items
			provider.getItems().clear();
			com.ocs.dynamo.dao.SortOrder[] orders = SortUtil.translateAndFilterOnTransient(false, targetEntityModel,
					sortOrders);
			if (orders != null) {
				List<T> list = service.find(new FilterConverter<T>(targetEntityModel).convert(filter), orders);
				provider.getItems().addAll(list);
			} else {
				List<T> list = service.find(new FilterConverter<T>(targetEntityModel).convert(filter));
				provider.getItems().addAll(list);
			}
		}
	}

	public void refresh(SerializablePredicate<T> filter) {
		this.originalFilter = filter;
		this.filter = filter;
		refresh();
	}

	@Override
	public void setAdditionalFilter(SerializablePredicate<T> additionalFilter) {
		setValue(null);
		this.additionalFilter = additionalFilter;
		this.filter = originalFilter == null ? additionalFilter : new AndPredicate<T>(originalFilter, additionalFilter);
		refresh();
	}

	public void setFilter(SerializablePredicate<T> filter) {
		this.filter = filter;
	}

	public void setSelectMode(SelectMode selectMode) {
		this.selectMode = selectMode;
	}
}
