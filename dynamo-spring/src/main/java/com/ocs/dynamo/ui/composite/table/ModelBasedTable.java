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
package com.ocs.dynamo.ui.composite.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.ModelBasedFieldFactory;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.component.URLField;
import com.ocs.dynamo.ui.composite.table.export.TableExportActionHandler;
import com.ocs.dynamo.utils.SystemPropertyUtils;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;

/**
 * A Table that bases its columns on the meta model of an entity
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
public class ModelBasedTable<ID extends Serializable, T extends AbstractEntity<ID>> extends Table {

    private static final long serialVersionUID = 6946260934644731038L;

    private Container container;

    private EntityModel<T> entityModel;

    private EntityModelFactory entityModelFactory;

    private MessageService messageService;

    /**
     * Constructor
     * 
     * @param container
     * @param model
     * @param entityModelFactory
     * @param messageService
     */
    public ModelBasedTable(Container container, EntityModel<T> model,
            EntityModelFactory entityModelFactory, MessageService messageService) {
        super("", container);
        this.container = container;
        this.entityModel = model;
        this.messageService = messageService;
        this.entityModelFactory = entityModelFactory;

        TableUtils.defaultInitialization(this);

        // add a custom field factory that takes care of special cases and
        // validation
        this.setTableFieldFactory(ModelBasedFieldFactory.getInstance(entityModel, messageService));

        generateColumns(this, container, model);

        if (SystemPropertyUtils.allowTableExport()) {
            // add export functionality
            List<EntityModel<?>> list = new ArrayList<>();
            list.add(model);
            addActionHandler(new TableExportActionHandler(UI.getCurrent(), entityModelFactory,
                    list, messageService, model.getDisplayNamePlural(), null, false, null));
        }

        addItemSetChangeListener(new ItemSetChangeListener() {

            private static final long serialVersionUID = 3035240490920769456L;

            @Override
            public void containerItemSetChange(ItemSetChangeEvent event) {
                updateTableCaption();
            }
        });
    }

    /**
     * Adds a column to the table
     * 
     * @param table
     *            the table
     * @param attributeModel
     *            the (possibly nested) attribute model
     * @param propertyNames
     *            the properties to be added
     * @param headerNames
     *            the headers to be added
     */
    private void addColumn(Table table, final AttributeModel attributeModel,
            List<Object> propertyNames, List<String> headerNames) {
        if (attributeModel.isVisibleInTable()) {
            propertyNames.add(attributeModel.getName());
            headerNames.add(attributeModel.getDisplayName());

            // for the lazy query container we explicitly have to add the
            // properties - for the standard Bean container this is not
            // needed
            if (container instanceof LazyQueryContainer) {
                LazyQueryContainer lazyContainer = (LazyQueryContainer) container;
                if (!lazyContainer.getContainerPropertyIds().contains(attributeModel.getName())) {
                    lazyContainer.addContainerProperty(attributeModel.getName(),
                            attributeModel.getType(), attributeModel.getDefaultValue(),
                            attributeModel.isReadOnly(), attributeModel.isSortable());
                }
            }

            // generated column with clickable URL
            if (attributeModel.isUrl()) {
                table.addGeneratedColumn(attributeModel.getName(), new ColumnGenerator() {

                    private static final long serialVersionUID = -3191235289754428914L;

                    @Override
                    public Object generateCell(Table source, Object itemId, Object columnId) {
                        URLField field = (URLField) ((ModelBasedFieldFactory<?>) getTableFieldFactory())
                                .createField(attributeModel.getPath(), null);
                        if (field != null) {
                            String val = (String) getItem(itemId).getItemProperty(columnId)
                                    .getValue();
                            field.setValue(val);
                            return field;
                        }
                        return null;
                    }
                });
            }

            if (attributeModel.isNumerical()) {
                table.setColumnAlignment(attributeModel.getName(), Table.Align.RIGHT);
            }
        }
    }

    /**
     * Overridden to deal with custom formatting
     */
    @Override
    protected String formatPropertyValue(Object rowId, Object colId, Property<?> property) {
        String result = TableUtils.formatPropertyValue(this, entityModelFactory, entityModel,
                messageService, rowId, colId, property);
        if (result != null) {
            return result;
        }
        return super.formatPropertyValue(rowId, colId, property);
    }

    /**
     * Generates the columns of the table based on the entity model
     * 
     * @param container
     *            the container
     * @param model
     *            the entity model
     */
    protected void generateColumns(Table table, Container container, EntityModel<T> model) {
        generateColumns(table, model.getAttributeModels());
        table.setCaption(model.getDisplayNamePlural());
        table.setDescription(model.getDescription());
    }

    /**
     * Generates the columns of the table based on a select number of attribute models
     * 
     * @param table
     * @param attributeModels
     */
    protected void generateColumns(Table table, List<AttributeModel> attributeModels) {
        List<Object> propertyNames = new ArrayList<>();
        List<String> headerNames = new ArrayList<>();

        for (AttributeModel attributeModel : attributeModels) {
            addColumn(table, attributeModel, propertyNames, headerNames);
            if (attributeModel.getNestedEntityModel() != null) {
                for (AttributeModel nestedAttributeModel : attributeModel.getNestedEntityModel()
                        .getAttributeModels()) {
                    addColumn(table, nestedAttributeModel, propertyNames, headerNames);
                }
            }
        }

        table.setVisibleColumns(propertyNames.toArray());
        table.setColumnHeaders(headerNames.toArray(new String[0]));

    }

    public Container getContainer() {
        return container;
    }

    public void updateTableCaption() {
        setCaption(entityModel.getDisplayNamePlural() + " "
                + messageService.getMessage("ocs.showing.results", getContainerDataSource().size()));
    }

}
