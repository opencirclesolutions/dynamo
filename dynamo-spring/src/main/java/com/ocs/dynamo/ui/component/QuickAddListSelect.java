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

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Refreshable;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.And;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

import java.io.Serializable;

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
public class QuickAddListSelect<ID extends Serializable, T extends AbstractEntity<ID>>
        extends QuickAddEntityField<ID, T, Object> implements Refreshable {

    private static final long serialVersionUID = 4246187881499965296L;

    /**
     * The list select component
     */
    private EntityListSelect<ID, T> listSelect;

    /**
     * Whether the component is in view mode
     */
    private boolean viewMode;

    private boolean quickAddAllowed;

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
    public QuickAddListSelect(EntityModel<T> entityModel, AttributeModel attributeModel, BaseService<ID, T> service,
            Filter filter, boolean multiSelect, int rows, SortOrder... sortOrder) {
        super(service, entityModel, attributeModel, filter);
        listSelect = new EntityListSelect<>(entityModel, attributeModel, service, filter, sortOrder);
        listSelect.setMultiSelect(multiSelect);
        listSelect.setRows(rows);
        this.quickAddAllowed = attributeModel != null && attributeModel.isQuickAddAllowed();
        this.directNavigationAllowed = (attributeModel != null && attributeModel.isDirectNavigation());
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
    public Class<?> getType() {
        return Object.class;
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

        float listExpandRatio = 1f;
        if (quickAddAllowed && !viewMode){
            listExpandRatio -= 0.10f;
        }
        if (directNavigationAllowed){
            listExpandRatio -= 0.05f;
        }

        bar.setExpandRatio(listSelect, listExpandRatio);

        if (!viewMode && quickAddAllowed) {
            Button addButton = constructAddButton();
            bar.addComponent(addButton);
            bar.setExpandRatio(addButton, 0.10f);
        }
        if (directNavigationAllowed){
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
    public void refresh(Filter filter) {
        setFilter(filter);
        if (listSelect != null) {
            listSelect.refresh(filter);
        }
    }

    @Override
    public void setAdditionalFilter(Filter additionalFilter) {
        super.setAdditionalFilter(additionalFilter);
        if (listSelect != null) {
            listSelect.refresh(getFilter() == null ? additionalFilter : new And(getFilter(), additionalFilter));
        }
    }

    @Override
    protected void setInternalValue(Object newValue) {
        super.setInternalValue(newValue);
        if (listSelect != null) {
            listSelect.setValue(newValue);
        }
    }

    @Override
    public void setValue(Object newFieldValue) {
        super.setValue(newFieldValue);
        if (listSelect != null) {
            listSelect.setValue(newFieldValue);
        }
    }

    public void setViewMode(boolean viewMode) {
        this.viewMode = viewMode;
    }

}
