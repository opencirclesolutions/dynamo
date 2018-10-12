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

import org.apache.commons.lang.StringUtils;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.FilterConverter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocator;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.utils.SortUtil;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.Property;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.converter.ConverterUtil;
import com.vaadin.data.util.filter.And;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.ComboBox;

/**
 * Combo box for displaying a list of entities. This component supports
 * filtering
 */
public class EntityComboBox<ID extends Serializable, T extends AbstractEntity<ID>> extends ComboBox
		implements Refreshable, Cascadable {

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
	private final SortOrder[] sortOrder;

	/**
	 * The current search filter
	 */
	private Filter filter;

	/**
	 * The original search filter (without cascades)
	 */
	private Filter originalFilter;

	private Filter additionalFilter;

	private ServiceLocator serviceLocator = ServiceLocatorFactory.getServiceLocator();

	private EntityModel<T> targetEntityModel;

	/**
	 * Constructor (for a filtering combo box)
	 *
	 * @param targetEntityModel
	 * @param attributeModel
	 * @param service
	 * @param filter
	 * @param sortOrder
	 */
	public EntityComboBox(EntityModel<T> targetEntityModel, AttributeModel attributeModel, BaseService<ID, T> service,
			Filter filter, SortOrder... sortOrder) {
		this(targetEntityModel, attributeModel, service, SelectMode.FILTERED, filter, null, sortOrder);
	}

	/**
	 * Constructor
	 *
	 * @param targetEntityModel
	 *            the entity model for the entities that are displayed in the
	 *            combo box
	 * @param attributeModel
	 *            the attribute model for the attribute to which the selected
	 *            value will be assigned
	 * @param service
	 *            the service used to retrieve entities
	 * @param mode
	 *            the select mode
	 * @param filter
	 *            optional filters to apply to the search
	 * @param items
	 *            the items to display (in fixed mode)
	 * @param sortOrder
	 *            the sort order(s) to apply
	 */
	public EntityComboBox(EntityModel<T> targetEntityModel, AttributeModel attributeModel, BaseService<ID, T> service,
			SelectMode mode, Filter filter, List<T> items, SortOrder... sortOrder) {

		this.service = service;
		this.selectMode = mode;
		this.sortOrder = sortOrder;
		this.attributeModel = attributeModel;
		this.filter = filter;
		this.targetEntityModel = targetEntityModel;
		if (attributeModel != null) {
			this.setCaption(attributeModel.getDisplayName());
		}
		this.setRequiredError(
				serviceLocator.getMessageService().getMessage("ocs.may.not.be.null", VaadinUtils.getLocale()));

		setFilteringMode(FilteringMode.CONTAINS);

		BeanItemContainer<T> container = new BeanItemContainer<>(targetEntityModel.getEntityClass());
		this.setContainerDataSource(container);

		if (SelectMode.ALL.equals(mode)) {
			// add all items (but sorted)
			if (sortOrder != null) {
				container
						.addAll(service
								.findAll(SortUtil.translateAndFilterOnTransient(false, targetEntityModel, sortOrder)));
			}
		} else if (SelectMode.FILTERED.equals(mode)) {
			// add a filtered selection of items
			items = service.find(new FilterConverter(targetEntityModel).convert(filter),
					SortUtil.translateAndFilterOnTransient(false, targetEntityModel, sortOrder));
			container.addAll(items);
		} else if (SelectMode.FIXED.equals(mode)) {
			container.addAll(items);
		}
		// Apply sortorder on container when transient attributes are applied
		SortUtil.applyContainerSortOrder(container, true, targetEntityModel, sortOrder);

		setItemCaptionMode(ItemCaptionMode.PROPERTY);
		setItemCaptionPropertyId(targetEntityModel.getDisplayProperty());
		setSizeFull();
	}

	/**
	 * Constructor - for the "ALL"mode
	 *
	 * @param targetEntityModel
	 *            the entity model for the entities that are displayed
	 * @param attributeModel
	 *            the attribute model of the property that is bound to this
	 *            component
	 * @param service
	 *            the service used to retrieve the entities
	 */
	public EntityComboBox(EntityModel<T> targetEntityModel, AttributeModel attributeModel, BaseService<ID, T> service,
			SortOrder... sortOrder) {
		this(targetEntityModel, attributeModel, service, SelectMode.ALL, null, null, sortOrder);
	}

	/**
	 * Constructor - for the "FIXED" mode
	 *
	 * @param targetEntityModel
	 *            the entity model for the entities that are displayed
	 * @param attributeModel
	 *            the attribute model of the property that is bound to this
	 *            component
	 * @param items
	 *            the list of entities to display
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
		BeanItemContainer<T> bic = (BeanItemContainer<T>) this.getContainerDataSource();
		bic.addBean(entity);
	}

	/**
	 * Overwritten so that diacritics are ignored when comparing
	 */
	@Override
	protected Filter buildFilter(String filterString, FilteringMode filteringMode) {
		Filter ft = null;

		if (!StringUtils.isEmpty(filterString)) {
			switch (filteringMode) {
			case STARTSWITH:
				ft = new IgnoreDiacriticsStringFilter(getItemCaptionPropertyId(), filterString, true, true);
				break;
			case CONTAINS:
				ft = new IgnoreDiacriticsStringFilter(getItemCaptionPropertyId(), filterString, true, false);
				break;
			default:
				break;
			}
		}
		return ft;
	}

	@Override
	public void clearAdditionalFilter() {
		this.additionalFilter = null;
		this.filter = originalFilter;
		refresh();
	}

	public AttributeModel getAttributeModel() {
		return attributeModel;
	}

	public Filter getFilter() {
		return filter;
	}

	@SuppressWarnings("unchecked")
	public T getFirstItem() {
		BeanItemContainer<T> bc = (BeanItemContainer<T>) getContainerDataSource();
		return bc.firstItemId();
	}

	public SelectMode getSelectMode() {
		return selectMode;
	}

	public SortOrder[] getSortOrder() {
		return sortOrder;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void refresh() {
		if (SelectMode.ALL.equals(selectMode)) {
			// add all items (but sorted)
			getContainerDataSource().removeAllItems();
			((BeanItemContainer<T>) getContainerDataSource()).addAll(
					service.findAll(SortUtil.translateAndFilterOnTransient(false, targetEntityModel, sortOrder)));
		} else if (SelectMode.FILTERED.equals(selectMode)) {
			// add a filtered selection of items
			getContainerDataSource().removeAllItems();
			List<T> list = service.find(new FilterConverter(targetEntityModel).convert(filter),
					SortUtil.translateAndFilterOnTransient(false, targetEntityModel, sortOrder));
			((BeanItemContainer<T>) getContainerDataSource()).addAll(list);
		}
		// Apply sortorder on container when transiant attributes are applied
		SortUtil.applyContainerSortOrder((BeanItemContainer<T>) getContainerDataSource(), true, targetEntityModel,
				sortOrder);
	}

	public void refresh(Filter filter) {
		this.originalFilter = filter;
		this.filter = filter;
		refresh();
	}

	@Override
	public void setAdditionalFilter(Filter additionalFilter) {
		setValue(null);
		this.additionalFilter = additionalFilter;
		this.filter = originalFilter == null ? new And(additionalFilter) : new And(originalFilter, additionalFilter);
		refresh();
	}

	public void setSelectMode(SelectMode selectMode) {
		this.selectMode = selectMode;
	}

	@Override
	public Filter getAdditionalFilter() {
		return additionalFilter;
	}

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.ui.AbstractSelect#getItemCaption(java.lang.Object)
	 */
	@Override
	public String getItemCaption(Object itemId) {
		if (getItemCaptionMode() == ItemCaptionMode.PROPERTY) {
			final Property<?> p = getContainerProperty(itemId, getItemCaptionPropertyId());
			if (p != null) {
				Object value = p.getValue();
				if (value != null) {
					try {
						value = ConverterUtil.convertFromModel(value, Object.class, getConverter(),
								getLocale());
					} catch (Exception e) {
					}
					return value.toString();
				}
			}
			return null;
		}
		return super.getItemCaption(itemId);
	}

}
