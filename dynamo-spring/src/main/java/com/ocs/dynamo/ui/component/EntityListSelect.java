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
import com.ocs.dynamo.filter.FilterConverter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.utils.SortUtil;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.ListSelect;

/**
 * Custom ListSelect component for displaying a collection of entities.
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key of the entity
 * @param <T>
 *            type of the entity
 */
public class EntityListSelect<ID extends Serializable, T extends AbstractEntity<ID>> extends ListSelect implements
        Refreshable {

	private static final long serialVersionUID = 3041574615271340579L;

	/**
	 * The attribute model that governs how to build the component
	 */
	private final AttributeModel attributeModel;

	/**
	 * The select mode (filtered, all, or fixed)
	 */
	private SelectMode selectMode = SelectMode.FILTERED;

	/**
	 * The sort orders
	 */
	private final SortOrder[] sortOrders;

	private BaseService<ID, T> service;

	/**
	 * The searc filter to use in filtered mode
	 */
	private Filter filter;

	public enum SelectMode {
		ALL, FILTERED, FIXED;
	}

	/**
	 * Constructor - for the "ALL" mode
	 * 
	 * @param targetEntityModel
	 *            the entity model of the entities that are to be displayed
	 * @param attributeModel
	 *            the attribute model for the property that is bound to this component
	 * @param service
	 *            the service used to retrieve entities
	 */
	public EntityListSelect(EntityModel<T> targetEntityModel, AttributeModel attributeModel,
	        BaseService<ID, T> service, SortOrder... sortOrder) {
		this(targetEntityModel, attributeModel, service, SelectMode.ALL, null, null, sortOrder);
	}

	/**
	 * Constructor - for the "FIXED" mode
	 * 
	 * @param targetEntityModel
	 *            the entity model of the entities that are to be displayed
	 * @param attributeModel
	 *            the attribute model for the property that is bound to this component
	 * @param items
	 *            the list of entities to display
	 */
	public EntityListSelect(EntityModel<T> targetEntityModel, AttributeModel attributeModel, List<T> items) {
		this(targetEntityModel, attributeModel, null, SelectMode.FIXED, null, items);
	}

	/**
	 * Constructor - for the "FILTERED" mode
	 * 
	 * @param targetEntityModel
	 *            the entity model of the entities that are to be displayed
	 * @param attributeModel
	 *            the attribute model for the property that is bound to this component
	 * @param service
	 *            the service used to retrieve the entities
	 * @param filter
	 *            the filter used to filter the entities
	 * @param sortOrder
	 *            the sort order used to sort the entities
	 */
	public EntityListSelect(EntityModel<T> targetEntityModel, AttributeModel attributeModel,
	        BaseService<ID, T> service, Filter filter, SortOrder... sortOrder) {
		this(targetEntityModel, attributeModel, service, SelectMode.FILTERED, filter, null, sortOrder);
	}

	/**
	 * Constructor
	 * 
	 * @param targetEntityModel
	 * @param attributeModel
	 * @param service
	 * @param mode
	 * @param filter
	 * @param items
	 * @param itemCaptionPropertyId
	 * @param sortOrder
	 */
	public EntityListSelect(EntityModel<T> targetEntityModel, AttributeModel attributeModel,
	        BaseService<ID, T> service, SelectMode mode, Filter filter, List<T> items, SortOrder... sortOrders) {

		this.service = service;
		this.selectMode = mode;
		this.sortOrders = sortOrders;
		this.attributeModel = attributeModel;
		this.filter = filter;

		if (attributeModel != null) {
			this.setCaption(attributeModel.getDisplayName());
		}

		BeanItemContainer<T> container = new BeanItemContainer<T>(targetEntityModel.getEntityClass());
		this.setContainerDataSource(container);

		if (SelectMode.ALL.equals(mode)) {
			// add all items (but sorted)
			container.addAll(service.findAll(SortUtil.translate(sortOrders)));
		} else if (SelectMode.FILTERED.equals(mode)) {
			// add a filtered selection of items
			items = service.find(new FilterConverter(null).convert(filter), SortUtil.translate(sortOrders));
			container.addAll(items);
		} else if (SelectMode.FIXED.equals(mode)) {
			container.addAll(items);
		}

		setItemCaptionMode(ItemCaptionMode.PROPERTY);
		setItemCaptionPropertyId(targetEntityModel.getDisplayProperty());
		setSizeFull();
	}

	public SelectMode getSelectMode() {
		return selectMode;
	}

	public SortOrder[] getSortOrders() {
		return sortOrders;
	}

	public AttributeModel getAttributeModel() {
		return attributeModel;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void refresh() {
		if (SelectMode.ALL.equals(selectMode)) {
			// add all items (but sorted)
			getContainerDataSource().removeAllItems();
			((BeanItemContainer<T>) getContainerDataSource()).addAll(service.findAll(SortUtil.translate(sortOrders)));
		} else if (SelectMode.FILTERED.equals(selectMode)) {
			// add a filtered selection of items
			getContainerDataSource().removeAllItems();
			List<T> list = service.find(new FilterConverter(null).convert(filter), SortUtil.translate(sortOrders));
			((BeanItemContainer<T>) getContainerDataSource()).addAll(list);
		}
	}
}
