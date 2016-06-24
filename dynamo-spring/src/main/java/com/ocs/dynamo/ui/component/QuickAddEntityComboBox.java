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
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.component.EntityComboBox.SelectMode;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

/**
 * A component that contains a combo box for selecting an entity, plus the option to add new values
 * on the fly
 * 
 * @author bas.rutten
 *
 * @param <ID>
 *            the type of the primary key of the entity
 * @param <T>
 *            the type of the entity
 */
public class QuickAddEntityComboBox<ID extends Serializable, T extends AbstractEntity<ID>> extends
        QuickAddEntityField<ID, T, T> implements Refreshable {

    private static final long serialVersionUID = 4246187881499965296L;

    /**
     * The combo box that we wrap this component around
     */
    private EntityComboBox<ID, T> comboBox;

    /**
     * Constructor
     * 
     * @param entityModel
     *            the entity model
     * @param attributeModel
     *            the attribute model
     * @param service
     *            the service
     * @param mode
     *            the mode
     * @param filter
     *            the filter that is used for filtering the data
     * @param items
     * @param sortOrder
     */
    public QuickAddEntityComboBox(EntityModel<T> entityModel, AttributeModel attributeModel,
            BaseService<ID, T> service, SelectMode mode, Filter filter, List<T> items,
            SortOrder... sortOrder) {
        super(service, entityModel, attributeModel);
        comboBox = new EntityComboBox<>(entityModel, attributeModel, service, mode, filter, items,
                sortOrder);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void afterNewEntityAdded(T entity) {
        // add to the container
        BeanItemContainer<T> container = (BeanItemContainer<T>) comboBox.getContainerDataSource();
        container.addBean(entity);
        comboBox.setValue(entity);
    }

    @Override
    protected Component initContent() {
        HorizontalLayout bar = new DefaultHorizontalLayout(false, true, true);
        bar.setSizeFull();

        if (this.getAttributeModel() != null) {
            this.setCaption(getAttributeModel().getDisplayName());
        }

        // no caption needed (the wrapping component has the caption)
        comboBox.setCaption(null);
        comboBox.setSizeFull();

        comboBox.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 5114731461745867455L;

            @Override
            @SuppressWarnings("unchecked")
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                setValue((T) event.getProperty().getValue());
            }
        });

        bar.addComponent(comboBox);

        Button addButton = constructAddButton();
        bar.addComponent(addButton);

        return bar;
    }

    @Override
    protected void setInternalValue(T newValue) {
        super.setInternalValue(newValue);
        if (comboBox != null) {
            comboBox.setValue(newValue);
        }
    }

    @Override
    public void setValue(T newFieldValue) throws com.vaadin.data.Property.ReadOnlyException,
            ConversionException {
        super.setValue(newFieldValue);
        if (comboBox != null) {
            comboBox.setValue(newFieldValue);
        }
    }

    @Override
    public Class<? extends T> getType() {
        return getEntityModel().getEntityClass();
    }

    @Override
    public void refresh() {
        if (comboBox != null) {
            comboBox.refresh();
        }
    }
}
