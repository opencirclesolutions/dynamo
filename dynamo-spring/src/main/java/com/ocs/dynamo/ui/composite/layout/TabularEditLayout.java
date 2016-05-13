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
package com.ocs.dynamo.ui.composite.layout;

import java.io.Serializable;
import java.util.Collection;

import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.impl.ModelBasedFieldFactory;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Reloadable;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.component.URLField;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.table.BaseTableWrapper;
import com.ocs.dynamo.ui.composite.table.ServiceResultsTableWrapper;
import com.ocs.dynamo.ui.container.QueryType;
import com.ocs.dynamo.ui.container.ServiceContainer;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * A page for editing items directly in a table - this is built around the lazy query container
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
@SuppressWarnings("serial")
public abstract class TabularEditLayout<ID extends Serializable, T extends AbstractEntity<ID>>
        extends BaseCollectionLayout<ID, T> implements Reloadable {

    // the default page length
    private static final int PAGE_LENGTH = 15;

    private static final long serialVersionUID = 4606800218149558500L;

    private Button addButton;

    private Button cancelButton;

    private Button editButton;

    private Filter filter;

    // the main layout
    private VerticalLayout mainLayout;

    private int pageLength = PAGE_LENGTH;

    private Button removeButton;

    private Button saveButton;

    private boolean viewmode;

    /**
     * Constructor
     * 
     * @param service
     *            the service used to query the database
     * @param entityModel
     *            the entity model the entity model used to build the table
     * @param formOptions
     *            the form options
     * @param sortOrder
     *            the first sort order
     * @param joins
     *            the desired joins
     */
    public TabularEditLayout(BaseService<ID, T> service, EntityModel<T> entityModel,
            FormOptions formOptions, SortOrder sortOrder, FetchJoinInformation... joins) {
        super(service, entityModel, formOptions, sortOrder, joins);
    }

    /**
     * Method that is called after a remove operation has been carried out
     */
    protected void afterRemove() {
        // do nothing
    }

    /**
     * Callback method that is called after a save operation has been carried out
     */
    protected void afterSave() {
        // do nothing
    }

    @Override
    public void attach() {
        super.attach();
        this.filter = constructFilter();
        build();
    }

    /**
     * Method that is called before a remove operation is carried out
     * 
     * @param entity
     *            the entity to remove
     */
    protected void beforeRemove(T entity) {
        // do nothing
    }

    /**
     * Method that is called before a save operation is carried out
     */
    protected void beforeSave() {
        // do nothing
    }

    /**
     * Lazily builds the actual layout
     */
    @Override
    public void build() {
        if (mainLayout == null) {

            setViewmode(!isEditAllowed() || getFormOptions().isOpenInViewMode());
            mainLayout = new DefaultVerticalLayout(true, true);

            initTable();

            mainLayout.addComponent(getButtonBar());

            // add button
            if (!getFormOptions().isHideAddButton() && isEditAllowed()) {
                addButton = new Button(message("ocs.add"));
                addButton.addClickListener(new Button.ClickListener() {

                    @Override
                    @SuppressWarnings("unchecked")
                    public void buttonClick(ClickEvent event) {
                        // delegate the construction of a new item to the lazy
                        // query container
                        ID id = (ID) getContainer().addItem();
                        constructEntity(getEntityFromTable(id));
                        getTableWrapper().getTable().setCurrentPageFirstItemId(id);
                    }
                });
                getButtonBar().addComponent(addButton);
                addButton.setVisible(!isViewmode());
            }

            // remove button
            if (getFormOptions().isShowRemoveButton()) {
                removeButton = new RemoveButton() {

                    @Override
                    protected void doDelete() {
                        if (getSelectedItem() != null) {
                            beforeRemove(getSelectedItem());
                            getTableWrapper().getTable().removeItem(getSelectedItem().getId());
                            getContainer().commit();
                            setSelectedItem(null);
                            afterRemove();
                        }
                    }
                };
                getButtonBar().addComponent(removeButton);
                removeButton.setVisible(!isViewmode());
                registerDetailButton(removeButton);
            }

            // save button
            saveButton = new Button(message("ocs.save"));
            saveButton.setEnabled(false);
            saveButton.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    try {
                        beforeSave();
                        getContainer().commit();
                        afterSave();
                    } catch (RuntimeException ex) {
                        handleSaveException(ex);
                    }
                }
            });
            getButtonBar().addComponent(saveButton);
            saveButton.setVisible(!isViewmode());

            if (getFormOptions().isShowEditButton()) {
                editButton = new Button(message("ocs.edit"));
                editButton.addClickListener(new Button.ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        toggleViewMode(false);
                    }

                });
                editButton.setVisible(isViewmode());
                getButtonBar().addComponent(editButton);

                cancelButton = new Button(message("ocs.cancel"));
                cancelButton.addClickListener(new Button.ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        reload();
                        toggleViewMode(true);
                    }

                });
                cancelButton.setVisible(!isViewmode() && getFormOptions().isOpenInViewMode());
                getButtonBar().addComponent(cancelButton);
            }

            postProcessButtonBar(getButtonBar());
            postProcessLayout(mainLayout);
        }
        setCompositionRoot(mainLayout);
    }

    /**
     * Method that is called after a new row with a fresh entity is added to the table. Use this
     * method to perform initialization
     * 
     * @param entity
     *            the newly created entity that has to be initialized
     * @return
     */
    protected T constructEntity(T entity) {
        return entity;
    }

    /**
     * Creates the filter used for searching
     * 
     * @return
     */
    protected Filter constructFilter() {
        return null;
    }

    @Override
    protected BaseTableWrapper<ID, T> constructTableWrapper() {
        ServiceResultsTableWrapper<ID, T> tableWrapper = new ServiceResultsTableWrapper<ID, T>(
                getService(), getEntityModel(), QueryType.ID_BASED, filter, getSortOrders(),
                getJoins()) {

            @Override
            protected void onSelect(Object selected) {
                setSelectedItems(selected);
                checkButtonState(getSelectedItem());
            }
        };
        tableWrapper.build();
        return tableWrapper;
    }

    @SuppressWarnings("unchecked")
    protected ServiceContainer<ID, T> getContainer() {
        return (ServiceContainer<ID, T>) getTableWrapper().getContainer();
    }

    /**
     * Retrieves an entity with a certain ID from the lazy query container
     * 
     * @param id
     *            the ID of the entity
     * @return
     */
    protected T getEntityFromTable(ID id) {
        return VaadinUtils.getEntityFromContainer(getContainer(), id);
    }

    public int getPageLength() {
        return pageLength;
    }

    /**
     * Initializes the table
     */
    protected void initTable() {

        final Table table = getTableWrapper().getTable();

        // make sure the table can be edited
        table.setEditable(!isViewmode());
        // make sure changes are not persisted right away
        table.setBuffered(true);
        table.setMultiSelect(false);
        table.setColumnCollapsingAllowed(false);
        // set a higher cache rate to allow for smoother scrolling
        table.setCacheRate(2.0);
        table.setSortEnabled(isSortEnabled());
        table.setPageLength(getPageLength());

        // default sorting
        // default sorting
        if (getSortOrders() != null && !getSortOrders().isEmpty()) {
            ServiceContainer<ID, T> sc = getContainer();
            sc.sort(getSortOrders().toArray(new SortOrder[0]));
        }

        // overwrite the field factory to handle validation
        table.setTableFieldFactory(new ModelBasedFieldFactory<T>(getEntityModel(),
                getMessageService(), true, false) {

            @Override
            public Field<?> createField(String propertyId) {

                // first try to create a custom field
                Field<?> custom = constructCustomField(getEntityModel(), getEntityModel()
                        .getAttributeModel(propertyId), isViewmode(), false);

                final Field<?> field = custom != null ? custom : super.createField(propertyId);

                if (field instanceof URLField) {
                    ((URLField) field).setEditable(!isViewmode());
                }

                if (field != null && field.isEnabled()) {
                    field.addValueChangeListener(new Property.ValueChangeListener() {

                        @Override
                        public void valueChange(Property.ValueChangeEvent event) {
                            if (saveButton != null) {
                                saveButton.setEnabled(VaadinUtils
                                        .allFixedTableFieldsValid(getTableWrapper().getTable()));
                            }
                        }

                    });
                    field.setSizeFull();
                    postProcessField(propertyId, field);
                }
                return field;
            }
        });
        mainLayout.addComponent(getTableWrapper());
    }

    public boolean isViewmode() {
        return viewmode;
    }

    /**
     * Post processes a field
     * 
     * @param propertyId
     *            the property ID
     * @param field
     *            the generated field
     */
    protected void postProcessField(Object propertyId, Field<?> field) {
        // overwrite in subclass
    }

    @Override
    public void reload() {
        getContainer().search(filter);
    }

    public void setPageLength(int pageLength) {
        this.pageLength = pageLength;
    }

    @SuppressWarnings("unchecked")
    public void setSelectedItems(Object selectedItems) {
        if (selectedItems != null) {
            if (selectedItems instanceof Collection<?>) {
                // the lazy query container returns an array of IDs of the
                // selected items
                Collection<?> col = (Collection<?>) selectedItems;
                ID id = (ID) col.iterator().next();
                setSelectedItem(getEntityFromTable(id));
            } else {
                ID id = (ID) selectedItems;
                setSelectedItem(getEntityFromTable(id));
            }
        } else {
            setSelectedItem(null);
        }
    }

    protected void setViewmode(boolean viewmode) {
        this.viewmode = viewmode;
    }

    /**
     * Sets the view mode of the screen, and adapts the table and all buttons accordingly
     * 
     * @param viewMode
     */
    protected void toggleViewMode(boolean viewMode) {
        setViewmode(viewMode);
        getTableWrapper().getTable().setEditable(!isViewmode());

        if (saveButton != null) {
            saveButton.setVisible(!isViewmode());
        }
        if (addButton != null) {
            addButton.setVisible(!isViewmode());
        }
        if (removeButton != null) {
            removeButton.setVisible(!isViewmode());
        }
        if (editButton != null) {
            editButton.setVisible(isViewmode());
        }
        if (cancelButton != null) {
            cancelButton.setVisible(!isViewmode());
        }
    }
}
