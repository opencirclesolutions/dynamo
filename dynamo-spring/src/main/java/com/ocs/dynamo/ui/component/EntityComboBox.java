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
import com.ocs.dynamo.utils.SortUtil;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.combobox.FilteringMode;

/**
 * Combo box for displaying a list of entities
 */
public class EntityComboBox<ID extends Serializable, T extends AbstractEntity<ID>>
        extends ComboBoxFixed {

    private static final long serialVersionUID = 3041574615271340579L;

    private final AttributeModel attributeModel;

    private SelectMode selectMode = SelectMode.FILTERED;

    private final SortOrder[] sortOrder;

    public enum SelectMode {
        ALL, FILTERED, FIXED;
    }

    /**
     * Constructor - for the "ALL"mode
     * 
     * @param targetEntityModel
     *            the entity model for the entities that are displayed
     * @param attributeModel
     *            the attribute model of the property that is bound to this component
     * @param service
     *            the service used to retrieve the entities
     */
    public EntityComboBox(EntityModel<T> targetEntityModel, AttributeModel attributeModel,
            BaseService<ID, T> service, SortOrder... sortOrder) {
        this(targetEntityModel, attributeModel, service, SelectMode.ALL, null, null, sortOrder);
    }

    /**
     * Constructor - for the "FIXED" mode
     * 
     * @param targetEntityModel
     *            the entity model for the entities that are displayed
     * @param attributeModel
     *            the attribute model of the property that is bound to this component
     * @param items
     *            the list of entities to display
     */
    public EntityComboBox(EntityModel<T> targetEntityModel, AttributeModel attributeModel,
            List<T> items) {
        this(targetEntityModel, attributeModel, null, SelectMode.FIXED, null, items);
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
    public EntityComboBox(EntityModel<T> targetEntityModel, AttributeModel attributeModel,
            BaseService<ID, T> service, Filter filter, SortOrder... sortOrder) {
        this(targetEntityModel, attributeModel, service, SelectMode.FILTERED, filter, null,
                sortOrder);
    }

    /**
     * Constructor
     * 
     * @param targetEntityModel
     *            the entity model for the entities that are displayed in the combo box
     * @param attributeModel
     *            the attribute model for the attribute to which the selected value will be assigned
     * @param service
     *            the service used to retrieve entities
     * @param mode
     *            the select mode
     * @param filter
     *            optional filters to apply to the search
     * @param items
     *            the items to display (in fixed mode)
     * @param itemCaptionPropertyId
     *            the ID of the property to use as the caption
     * @param sortOrder
     *            the sort order(s) to apply
     */
    public EntityComboBox(EntityModel<T> targetEntityModel, AttributeModel attributeModel,
            BaseService<ID, T> service, SelectMode mode, Filter filter, List<T> items,
            SortOrder... sortOrder) {

        this.selectMode = mode;
        this.sortOrder = sortOrder;
        this.attributeModel = attributeModel;

        if (attributeModel != null) {
            this.setCaption(attributeModel.getDisplayName());
        }

        setFilteringMode(FilteringMode.CONTAINS);

        BeanItemContainer<T> container = new BeanItemContainer<T>(
                targetEntityModel.getEntityClass());
        this.setContainerDataSource(container);

        if (SelectMode.ALL.equals(mode)) {
            // add all items (but sorted)
            if (sortOrder != null) {
                container.addAll(service.findAll(SortUtil.translate(sortOrder)));
            }
        } else if (SelectMode.FILTERED.equals(mode)) {
            // add a filtered selection of items
            items = service.find(new FilterConverter().convert(filter),
                    SortUtil.translate(sortOrder));
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

    @SuppressWarnings("unchecked")
    public T getFirstItem() {
        BeanItemContainer<T> bc = (BeanItemContainer<T>) getContainerDataSource();
        return bc.firstItemId();
    }

    public SortOrder[] getSortOrder() {
        return sortOrder;
    }

    public AttributeModel getAttributeModel() {
        return attributeModel;
    }

}
