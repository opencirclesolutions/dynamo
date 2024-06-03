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
package com.ocs.dynamo.ui.composite.grid;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.FieldCreationContext;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.SharedProvider;
import com.ocs.dynamo.ui.component.InternalLinkField;
import com.ocs.dynamo.ui.component.URLField;
import com.ocs.dynamo.ui.composite.ComponentContext;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.layout.HasSelectedItem;
import com.ocs.dynamo.ui.composite.type.GridEditMode;
import com.ocs.dynamo.ui.utils.GridFormatUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * A class tasked with adding all relevant columns to a grid. This was
 * introduced to reduce code duplication when creating ModelBasedGrid and
 * ModelBasedSelectionGrid
 *
 * @param <ID> the type of the primary key of the entity
 * @param <T>  the type of the entity
 * @author Bas Rutten
 */
public abstract class ModelBasedGridBuilder<ID extends Serializable, T extends AbstractEntity<ID>> {

    private final ComponentContext<ID, T> componentContext;

    private final EntityModel<T> entityModel;

    private final FieldFactory fieldFactory;

    private final Map<String, SerializablePredicate<?>> fieldFilters;

    private final FormOptions formOptions;

    private final Grid<T> grid;

    /**
     * Map from attribute path to "shared provider" that can be shared by all
     * components in the same column in a grid
     */
    private final Map<String, DataProvider<?, SerializablePredicate<?>>> sharedProviders = new HashMap<>();

    /**
     * Constructor
     *
     * @param grid             the grid
     * @param entityModel      the entity model
     * @param fieldFilters     the field filters to apply to the separate fields
     * @param formOptions      the form options
     * @param componentContext the component context
     */
    protected ModelBasedGridBuilder(Grid<T> grid, EntityModel<T> entityModel,
                                    Map<String, SerializablePredicate<?>> fieldFilters, FormOptions formOptions,
                                    ComponentContext<ID, T> componentContext) {
        this.grid = grid;
        this.entityModel = entityModel;
        this.formOptions = formOptions;
        this.componentContext = componentContext;
        this.fieldFilters = fieldFilters;
        this.fieldFactory = FieldFactory.getInstance();
    }

    /**
     * Adds a column to the grid
     *
     * @param am the attribute model on which to base the column
     */
    private void addColumn(AttributeModel am) {
        if (am.isVisibleInGrid()) {
            Column<T> column;
            if (am.isUrl()) {
                column = addUrlColumn(am);
            } else if (!componentContext.isPopup() && am.isNavigable()
                    && AttributeType.MASTER.equals(am.getAttributeType())) {
                // internal link
                column = grid.addComponentColumn(
                        entity -> generateInternalLinkField(am, ClassUtils.getFieldValue(entity, am.getPath())));
            } else {
                if (isSimultaneousEditMode(am)) {
                    // edit all columns at once
                    column = addEditableColumn(am);
                } else {
                    column = grid.addColumn(entity -> GridFormatUtils.extractAndFormat(am,
                            entity, VaadinUtils.getLocale(),
                            VaadinUtils.getTimeZoneId(), am.getCurrencySymbol()));
                }
            }

            // edit row-by-row, create in-line edit component using binder
            if (isSingleRowEditMode(am)) {
                setEditorComponent(am, column);
            }

            column.setHeader(am.getDisplayName(VaadinUtils.getLocale())).setSortProperty(am.getActualSortPath())
                    .setSortable(am.isSortable()).setClassNameGenerator(item -> am.isNumerical() ? "v-align-right" : "")
                    .setKey(am.getPath()).setAutoWidth(true).setResizable(true).setId(am.getPath());
        }

    }

    /**
     * Indicates whether an attribute is editable using a single row at a time
     * editor
     *
     * @param am the attribute model
     * @return true if this is the case, false otherwise
     */
    private boolean isSingleRowEditMode(AttributeModel am) {
        return componentContext.isEditable() && GridEditMode.SINGLE_ROW.equals(formOptions.getGridEditMode())
                && EditableType.EDITABLE.equals(am.getEditableType());
    }

    /**
     * Creates the editor component for the provided attribute model and column
     *
     * @param am the attribute model
     * @param column the column to create the component for
     */
    @SuppressWarnings("unchecked")
    private void setEditorComponent(AttributeModel am, Column<T> column) {
        Binder<T> binder = grid.getEditor().getBinder();
        FieldCreationContext context = FieldCreationContext.create().attributeModel(am).viewMode(false)
                .fieldFilters(fieldFilters).editableGrid(true).build();
        Component comp = fieldFactory.constructField(context);
        if (comp != null) {
            Binder.BindingBuilder<T, ?> builder = binder.forField((HasValue<?, ?>) comp);
            if (am.isRequired()) {
                builder.asRequired();
            }

            GridSelectedEntityProviderWrapper<T> wrapper = new GridSelectedEntityProviderWrapper<>(grid);
            fieldFactory.addConvertersAndValidators(wrapper, builder, am, context,
                    componentContext.findCustomConverter(am), componentContext.findCustomValidator(am), null);
            builder.bind(am.getPath());
        }
        column.setEditorComponent(comp);
    }

    private boolean isSimultaneousEditMode(AttributeModel am) {
        return componentContext.isEditable() && GridEditMode.SIMULTANEOUS.equals(formOptions.getGridEditMode())
                && EditableType.EDITABLE.equals(am.getEditableType());
    }

    @SuppressWarnings("unchecked")
    private Column<T> addEditableColumn(AttributeModel am) {
        return grid.addComponentColumn(entity -> {
            Component comp = constructCustomField(entityModel, am);
            FieldCreationContext context = FieldCreationContext.create().attributeModel(am)
                    .fieldEntityModel(getFieldEntityModel(am)).fieldFilters(fieldFilters).viewMode(false)
                    .sharedProviders(sharedProviders).editableGrid(true).build();

            if (comp == null) {
                comp = fieldFactory.constructField(context);
            }

            // always hide label inside grid
            VaadinUtils.setLabel(comp, "");

            // store shared date provider, so it can be used by multiple components
            storeSharedProvider(am, comp);

            if (comp instanceof HasSize) {
                ((HasSize) comp).setSizeFull();
            }

            // delegate the binding to the enveloping component
            BindingBuilder<T, ?> builder = doBind(entity, comp, am.getPath());
            if (builder != null) {
                HasSelectedItem<T> selector = () -> entity;
                fieldFactory.addConvertersAndValidators(selector, builder, am, context,
                        componentContext.findCustomConverter(am), componentContext.findCustomValidator(am),
                        componentContext.findCustomRequiredValidator(am));
                builder.bind(am.getPath());
            }
            postProcessComponent(entity.getId(), am, comp);

            return comp;
        });
    }

    @SuppressWarnings("unchecked")
    private void storeSharedProvider(AttributeModel am, Component comp) {
        if (comp instanceof SharedProvider) {
            DataProvider<?, ?> sharedProvider = ((SharedProvider<?>) comp).getSharedProvider();
            if (sharedProvider instanceof ListDataProvider) {
                sharedProviders.put(am.getPath(), (DataProvider<?, SerializablePredicate<?>>) sharedProvider);
            }
        }
    }

    private Column<T> addUrlColumn(AttributeModel am) {
        return grid.addComponentColumn(t -> {
            URLField urlField = new URLField(new TextField("", ClassUtils.getFieldValueAsString(t, am.getPath(), "")),
                    am,
                    componentContext.isEditable() && GridEditMode.SIMULTANEOUS.equals(formOptions.getGridEditMode()));
            urlField.setValue(ClassUtils.getFieldValueAsString(t, am.getPath(), ""));
            return urlField;
        });
    }

    /**
     * Adds the columns to the grid based on the attribute models
     *
     * @param attributeModels the attribute models
     */
    public void addColumns(List<AttributeModel> attributeModels) {
        for (AttributeModel am : attributeModels) {
            addColumn(am);
            if (am.getNestedEntityModel() != null) {
                addColumns(am.getNestedEntityModel().getAttributeModelsSortedForGrid());
            }
        }
    }

    /**
     * Method that is used to create custom components
     *
     * @param entityModel the entity model
     * @param attributeModel the attribute model for which to create the component
     * @return the created component
     */
    protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel) {
        return null;
    }

    /**
     * Callback method for components that incorporate this grid component but do
     * the binding themselves
     *
     * @param entity the entity
     * @param field  the field to bind
     * @return the created binding builder
     */
    protected BindingBuilder<T, ?> doBind(T entity, Component field, String attributeName) {
        return null;
    }

    @SuppressWarnings("unchecked")
    private <ID2 extends Serializable, S extends AbstractEntity<ID2>> InternalLinkField<ID2, S> generateInternalLinkField(
            AttributeModel attributeModel, Object value) {
        return new InternalLinkField<>(attributeModel, null, (S) value);
    }

    protected EntityModelFactory getEntityModelFactory() {
        return ServiceLocatorFactory.getServiceLocator().getEntityModelFactory();
    }

    protected final EntityModel<?> getFieldEntityModel(AttributeModel attributeModel) {
        String reference = componentContext.getFieldEntityModels().get(attributeModel.getPath());
        return reference == null ? null
                : getEntityModelFactory().getModel(reference, attributeModel.getNormalizedType());
    }

    /**
     * Post-process the component. Callback method that can be used from a component
     * that includes the grid
     *
     * @param am the attribute model
     * @param comp the component to post process
     */
    protected void postProcessComponent(ID id, AttributeModel am, Component comp) {
        // override in subclass
    }
}
