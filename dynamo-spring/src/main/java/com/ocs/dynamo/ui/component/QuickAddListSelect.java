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

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Refreshable;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

/**
 * 
 * A ListSelect field that allows the quick addition of simple entities. Supports both multiple
 * select and single select use cases
 * 
 * @author bas.rutten
 *
 * @param <ID>
 *            the type of the primary key of the entity that is being displayed
 * @param <T>
 *            the type of the entity that is being displayed
 */
public class QuickAddListSelect<ID extends Serializable, T extends AbstractEntity<ID>> extends
        QuickAddEntityField<ID, T, Object> implements Refreshable {

    private static final long serialVersionUID = 4246187881499965296L;

    /**
     * The list select component
     */
    private EntityListSelect<ID, T> listSelect;

    /**
     * The button for adding new entries
     */
    private Button addButton;

    /**
     * Whether the component is in view mode
     */
    private boolean viewMode;

    /**
     * 
     * @param entityModel
     * @param attributeModel
     * @param service
     * @param filter
     * @param multiSelect
     * @param rows
     * @param sortOrder
     */
    public QuickAddListSelect(EntityModel<T> entityModel, AttributeModel attributeModel,
            BaseService<ID, T> service, Filter filter, boolean multiSelect, int rows,
            SortOrder... sortOrder) {
        super(service, entityModel, attributeModel);
        listSelect = new EntityListSelect<ID, T>(entityModel, attributeModel, service, filter,
                sortOrder);
        listSelect.setMultiSelect(multiSelect);
        listSelect.setRows(rows);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void afterNewEntityAdded(T entity) {
        // add to the container
        BeanItemContainer<T> container = (BeanItemContainer<T>) listSelect.getContainerDataSource();
        container.addBean(entity);
        listSelect.select(entity);
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

        listSelect.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 5114731461745867455L;

            @Override
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                setValue(event.getProperty().getValue());
            }
        });

        bar.addComponent(listSelect);

        if (!viewMode) {
            addButton = constructAddButton();
            bar.addComponent(addButton);
        }

        return bar;
    }

    @Override
    protected void setInternalValue(Object newValue) {
        super.setInternalValue(newValue);
        if (listSelect != null) {
            listSelect.setValue(newValue);
        }
    }

    @Override
    public void setValue(Object newFieldValue) throws com.vaadin.data.Property.ReadOnlyException,
            ConversionException {
        super.setValue(newFieldValue);
        if (listSelect != null) {
            listSelect.setValue(newFieldValue);
        }
    }

    @Override
    public Class<? extends Object> getType() {
        return Object.class;
    }

    public void setViewMode(boolean viewMode) {
        this.viewMode = viewMode;
    }

    /**
     * Refreshes the data in the list
     */
    public void refresh() {
        if (listSelect != null) {
            listSelect.refresh();
        }
    }
}
