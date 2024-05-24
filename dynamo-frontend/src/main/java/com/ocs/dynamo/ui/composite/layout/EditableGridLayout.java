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

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.exception.OCSValidationException;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.UIHelper;
import com.ocs.dynamo.ui.component.CustomFieldContext;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.dialog.EntityPopupDialog;
import com.ocs.dynamo.ui.composite.grid.BaseGridWrapper;
import com.ocs.dynamo.ui.composite.grid.ServiceBasedGridWrapper;
import com.ocs.dynamo.ui.composite.type.GridEditMode;
import com.ocs.dynamo.ui.provider.QueryType;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.utils.FormatUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A layout for editing entities directly inside a grid. This layout supports
 * both a "row by row" and an "all rows at once" setting which can be specified
 * on the FormOptions by setting the GridEditMode.
 * <p>
 * For creating new entities a pop-up dialog is used
 *
 * @param <ID> the type of the primary key of the entity
 * @param <T>  the type of the entity
 * @author Bas Rutten
 */
public class EditableGridLayout<ID extends Serializable, T extends AbstractEntity<ID>>
        extends BaseCollectionLayout<ID, T, T> {

    @Getter
    private Button addButton;

    /**
     * Mapping from entity to associated binder
     */
    private final Map<T, Binder<T>> binders = new HashMap<>();

    /**
     * Button for canceling edit mode. Displayed below the grid when
     * "openInViewMode" is true
     */
    @Getter
    private Button cancelButton;

    /**
     * The IDS of the entities that were modified
     */
    private final Set<ID> changedEntityIds = new HashSet<>();

    /**
     * Map from (entity ID plus attribute) to component
     */
    private final Map<String, Component> componentMap = new HashMap<>();

    private BaseGridWrapper<ID, T> currentWrapper;

    @Getter
    private Button editButton;

    @Getter
    protected SerializablePredicate<T> filter;

    /**
     * The code that is carried out to create the filter for limiting the search
     * results
     */
    @Getter
    @Setter
    private Supplier<SerializablePredicate<T>> filterCreator;

    private VerticalLayout mainLayout;

    /**
     * Button for saving changes that appears below the grid (in "edit all at once"
     * mode)
     */
    @Getter
    private Button saveButton;

    /**
     * Whether the component is in view mode
     */
    @Getter
    private boolean viewMode;

    /**
     * Constructor
     *
     * @param service     the service that is used for querying the database
     * @param entityModel the entity model to base the grid on
     * @param formOptions the form options
     * @param sortOrder   the sort order that must be used
     * @param joins       the fetch joins to use when retrieving data
     */
    public EditableGridLayout(BaseService<ID, T> service, EntityModel<T> entityModel, FormOptions formOptions,
                              SortOrder<?> sortOrder, FetchJoinInformation... joins) {
        super(service, entityModel, formOptions, sortOrder, joins);
        setMargin(false);
        addClassName(DynamoConstants.CSS_EDITABLE_GRID_LAYOUT);
    }

    /**
     * Adds a column that contains a button that opens the editor for the
     * associated row
     *
     * @param editor the editor
     */
    private void addEditColumn(Editor<T> editor) {
        Column<T> editColumn = getGridWrapper().getGrid().addComponentColumn(entity -> {
            Button editButton = new Button("");
            editButton.setIcon(VaadinIcon.EDIT.create());
            editButton.addClickListener(event -> {
                if (!editor.isOpen()) {
                    // change to save button
                    editor.editItem(entity);
                    getGridWrapper().getGrid().getColumnByKey("edit").setVisible(false);
                    getGridWrapper().getGrid().getColumnByKey("save").setVisible(true);
                }
            });
            return editButton;
        });
        editColumn.setHeader(message("ocs.edit")).setAutoWidth(false).setWidth("100px").setKey("edit");
    }

    /**
     * Adds a column that contains a save button for the row that is currently being edited
     *
     * @param editor the editor
     */
    private void addSaveColumn(Editor<T> editor) {
        // button for saving currently edited row
        Column<T> saveColumn = getGridWrapper().getGrid().addComponentColumn(entity -> {
            if (Objects.equals(entity, editor.getItem())) {
                Button saveButton = new Button("");
                saveButton.setIcon(VaadinIcon.SAFE.create());
                saveButton.addClickListener(event -> {
                    if (editor.isOpen()) {
                        // save changes then rebuild grid
                        try {
                            BinderValidationStatus<T> validate = editor.getBinder().validate();
                            if (validate.isOk()) {

                                getService().save(editor.getItem());
                                VaadinUtils.showTrayNotification(message("ocs.changes.saved"));
                                // save and recreate grid to avoid optimistic locks
                                binders.clear();
                                clearGridWrapper();
                                constructGrid();
                            }
                        } catch (RuntimeException ex) {
                            handleSaveException(ex);
                        }
                    }
                });
                return saveButton;
            } else {
                // not the current row, no button
                return new Span("");
            }
        });

        saveColumn.setHeader(message("ocs.save")).setKey("save");
        getGridWrapper().getGrid().getColumnByKey("save").setVisible(false);
    }

    @Override
    public void build() {
        buildFilter();
        if (mainLayout == null) {
            setViewMode(!checkEditAllowed() || getFormOptions().isOpenInViewMode());
            mainLayout = new DefaultVerticalLayout(false, false);

            constructGrid();

            mainLayout.add(getButtonBar());

            createAddButton();
            createEditButton();
            createCancelButton();
            createSaveButton();

            if (getPostProcessMainButtonBar() != null) {
                getPostProcessMainButtonBar().accept(getButtonBar());
            }

            if (getAfterLayoutBuilt() != null) {
                getAfterLayoutBuilt().accept(mainLayout);
            }
        }
        add(mainLayout);
    }

    /**
     * Constructs the filter that limits which records show up
     */
    protected void buildFilter() {
        this.filter = filterCreator == null ? null : filterCreator.get();
    }

    /**
     * Clears the grid
     */
    private void clearAll() {
        changedEntityIds.clear();
        binders.clear();
        clearGridWrapper();
    }

    protected void constructGrid() {
        BaseGridWrapper<ID, T> wrapper = getGridWrapper();
        Editor<T> editor = wrapper.getGrid().getEditor();
        editor.addCloseListener(listener -> {
            binders.clear();
            clearGridWrapper();
            constructGrid();
        });

        editor.addSaveListener(event -> {
            try {
                T entity = getService().save(event.getItem());
                // reassign to avoid optimistic locking exception
                wrapper.getGrid().getEditor().getBinder().setBean(entity);
                wrapper.getGrid().getDataProvider().refreshAll();
            } catch (OCSValidationException ex) {
                Notification.show(ex.getMessage());
            }
        });

        // make sure changes are not persisted right away
        wrapper.getGrid().setSelectionMode(Grid.SelectionMode.SINGLE);
        wrapper.getGrid().getEditor().setBuffered(false);
        wrapper.getGrid().setHeight(getGridHeight());
        wrapper.getGrid().addSelectionListener(event -> setSelectedItems(event.getAllSelectedItems()));
        disableGridSorting();

        if (currentWrapper == null) {
            mainLayout.add(wrapper);
        } else {
            mainLayout.replace(currentWrapper, wrapper);
        }

        // add edit and save buttons when the grid is in "single row" mode
        if (checkEditAllowed() && !isViewMode() && GridEditMode.SINGLE_ROW.equals(getFormOptions().getGridEditMode())) {
            addEditColumn(editor);
            addSaveColumn(editor);
        }

        // remove button at the end of the row
        addRemoveColumn();
        currentWrapper = wrapper;
    }

    /**
     * Adds a column containing a remove button to the grid
     */
    private void addRemoveColumn() {
        if (getFormOptions().isShowRemoveButton() && checkEditAllowed() && !isViewMode()) {
            String defaultMsg = message("ocs.remove");
            Column<T> removeColumn = getGridWrapper().getGrid().addComponentColumn(entity -> {
                Button button = new Button("");
                button.setIcon(VaadinIcon.TRASH.create());
                button.addClickListener(event -> {
                    Runnable runnable = () -> {
                        try {
                            binders.remove(entity);
                            doRemove(entity);
                        } catch (OCSRuntimeException ex) {
                            showErrorNotification(ex.getMessage());
                        }
                    };
                    VaadinUtils.showConfirmDialog(
                            message("ocs.delete.confirm", FormatUtils.formatEntity(getEntityModel(), entity)),
                            runnable);
                });
                return button;
            });

            removeColumn.setHeader(defaultMsg).setAutoWidth(false).setWidth("100px").setId("remove");
        }
    }

    private Component findCustomComponent(EntityModel<?> entityModel, AttributeModel attributeModel, boolean viewMode) {
        Function<CustomFieldContext, Component> customFieldCreator = getComponentContext()
                .getCustomFieldCreator(attributeModel.getPath());
        if (customFieldCreator != null) {
            return customFieldCreator.apply(CustomFieldContext.builder().entityModel(entityModel)
                    .attributeModel(attributeModel).viewMode(viewMode).build());
        }
        return null;
    }

    @Override
    protected BaseGridWrapper<ID, T> constructGridWrapper() {

        UIHelper helper = ServiceLocatorFactory.getServiceLocator().getService(UIHelper.class);
        if (helper != null) {
            List<SortOrder<?>> retrievedOrders = helper.retrieveSortOrders();
            if (getFormOptions().isPreserveSortOrders() && retrievedOrders != null && !retrievedOrders.isEmpty()) {
                setSortOrders(retrievedOrders);
            }
        }

        ServiceBasedGridWrapper<ID, T> wrapper = new ServiceBasedGridWrapper<>(getService(), getEntityModel(),
                QueryType.ID_BASED, getFormOptions(), getComponentContext(), filter, getFieldFilters(), getSortOrders(),
                !isViewMode(), getJoins()) {

            @Override
            protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel) {
                return findCustomComponent(entityModel, attributeModel, viewMode);
            }

            @Override
            protected BindingBuilder<T, ?> doBind(T entity, Component field, String attributeName) {

                if (!binders.containsKey(entity)) {
                    binders.put(entity, new BeanValidationBinder<>(getEntityModel().getEntityClass()));
                    binders.get(entity).setBean(entity);
                }
                Binder<T> binder = binders.get(entity);
                return binder.forField((HasValue<?, ?>) field);
            }

            @Override
            protected void onSelect(Object selected) {
                setSelectedItems(selected);
                checkComponentState(getSelectedItem());
            }

            @Override
            protected void postProcessComponent(ID id, AttributeModel am, Component comp) {
                EditableGridLayout.this.postProcessComponent(id, am, comp);
            }

            @Override
            protected void postProcessDataProvider(final DataProvider<T, SerializablePredicate<T>> provider) {
                EditableGridLayout.this.postProcessDataProvider(provider);
            }

        };
        postConfigureGridWrapper(wrapper);
        wrapper.setMaxResults(getMaxResults());
        wrapper.build();
        return wrapper;
    }

    private void createAddButton() {
        addButton = new Button(message("ocs.add"));
        addButton.setIcon(VaadinIcon.PLUS.create());
        addButton.addClickListener(event -> {
            // create new entry by means of pop-up dialog
            EntityPopupDialog<ID, T> dialog = new EntityPopupDialog<>(getService(), null, getEntityModel(),
                    getFieldFilters(), new FormOptions(), getComponentContext());

            dialog.setAfterEditDone((cancel, newEntity, ent) -> reload());
            dialog.setCreateEntity(getCreateEntity());
            dialog.setPostProcessDetailButtonBar(getPostProcessDetailButtonBar());
            dialog.buildAndOpen();
        });
        getButtonBar().add(addButton);
        addButton.setVisible(getFormOptions().isShowAddButton() && checkEditAllowed() && !isViewMode());
    }

    private void createCancelButton() {
        // button for canceling edit mode
        cancelButton = new Button(message("ocs.cancel"));
        cancelButton.setIcon(VaadinIcon.BAN.create());
        cancelButton.addClickListener(event -> {

            Runnable afterConfirm = () -> {
                if (getGridWrapper().getGrid().getEditor().isOpen()) {
                    getGridWrapper().getGrid().getEditor().cancel();
                }
                toggleViewMode(true);
            };

            // check for pending changes before canceling
            if (!changedEntityIds.isEmpty()) {
                VaadinUtils.showConfirmDialog(message("ocs.pending.changes"), afterConfirm);
            } else {
                afterConfirm.run();
            }
        });
        cancelButton.setVisible(checkEditAllowed() && !isViewMode() && getFormOptions().isOpenInViewMode());
        getButtonBar().add(cancelButton);
    }

    /**
     * Creates a button for switching the grid to edit mode
     */
    private void createEditButton() {
        editButton = new Button(message("ocs.edit"));
        editButton.setIcon(VaadinIcon.EDIT.create());
        editButton.addClickListener(event -> toggleViewMode(false));
        editButton.setVisible(getFormOptions().isShowEditButton() && checkEditAllowed() && isViewMode());
        getButtonBar().add(editButton);
    }

    /**
     * Creates a button for saving multiple rows simultaneously
     */
    private void createSaveButton() {
        saveButton = new Button(message("ocs.save"));
        saveButton.addClickListener(event -> {

            List<T> toSave = new ArrayList<>(binders.keySet());
            boolean valid = binders.values().stream().map(binder -> binder.validate()).allMatch(s -> s.isOk());
            if (valid) {
                if (getFormOptions().isConfirmSave()) {
                    askConfirmationBeforeSave(toSave);
                } else {
                    // do not ask for confirmation before saving
                    try {
                        getService().save(toSave);
                        VaadinUtils.showTrayNotification(message("ocs.changes.saved"));
                        // save and reassign to avoid optimistic locks
                        clearAll();
                        constructGrid();
                    } catch (RuntimeException ex) {
                        handleSaveException(ex);
                    }
                }
            }
        });
        saveButton.setVisible(checkEditAllowed() && !isViewMode()
                && GridEditMode.SIMULTANEOUS.equals(getFormOptions().getGridEditMode()));
        getButtonBar().add(saveButton);
    }

    private void askConfirmationBeforeSave(List<T> toSave) {
        VaadinUtils.showConfirmDialog(getMessageService().getMessage("ocs.confirm.save.all",
                        VaadinUtils.getLocale(), getEntityModel().getDisplayNamePlural(VaadinUtils.getLocale())),
                () -> {
                    try {
                        getService().save(toSave);
                        VaadinUtils.showTrayNotification(message("ocs.changes.saved"));
                        // save and recreate grid to avoid optimistic locks
                        clearAll();
                        constructGrid();
                    } catch (RuntimeException ex) {
                        handleSaveException(ex);
                    }
                });
    }

    @Override
    protected void detailsMode(T entity) {
        // not needed
    }

    /**
     * Method that is called to remove an item
     */
    protected void doRemove(T t) {
        getService().delete(t);
        getGridWrapper().reloadDataProvider();
    }

    /**
     * Gets a component for the entity identified by ID and for the specified
     * attribute model
     *
     * @param id the ID of the entity
     * @param am the attribute model
     * @return the component
     */
    protected Component getComponent(ID id, AttributeModel am) {
        return componentMap.get(id + "_" + am.getPath());
    }

    protected DataProvider<T, SerializablePredicate<T>> getDataProvider() {
        return getGridWrapper().getDataProvider();
    }

    @Override
    public ServiceBasedGridWrapper<ID, T> getGridWrapper() {
        return (ServiceBasedGridWrapper<ID, T>) super.getGridWrapper();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        build();
    }

    /**
     * Callback method that is used to post-process an input component
     *
     * @param am   the attribute model for the component
     * @param comp the component
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void postProcessComponent(ID id, AttributeModel am, Component comp) {
        componentMap.put(id + "_" + am.getPath(), comp);
        if (comp instanceof HasValue<?, ?> hv) {
            ValueChangeListener listener = event -> changedEntityIds.add(id);
            hv.addValueChangeListener(listener);
        }
    }

    @Override
    public void refresh() {
        // override in subclasses
    }

    @Override
    public void reload() {
        buildFilter();
        getGridWrapper().setFilter(filter);
    }

    @SuppressWarnings("unchecked")
    public void setSelectedItems(Object selection) {
        if (selection != null) {
            if (selection instanceof Collection<?> col) {
                // the lazy query container returns an array of IDs of the
                // selected items
                if (!col.isEmpty()) {
                    T t = (T) col.iterator().next();
                    setSelectedItem(t);
                } else {
                    setSelectedItem(null);
                }
            } else {
                T t = (T) selection;
                setSelectedItem(t);
            }
        } else {
            setSelectedItem(null);
        }
    }

    protected void setViewMode(boolean viewMode) {
        this.viewMode = viewMode;
    }

    /**
     * Sets the view mode of the screen, and adapts the grid and all buttons
     * accordingly
     *
     * @param viewMode the new desired value for the view mode
     */
    protected void toggleViewMode(boolean viewMode) {
        setViewMode(viewMode);

        // replace the current grid
        clearGridWrapper();
        binders.clear();
        changedEntityIds.clear();
        constructGrid();

        // check the button statuses
        saveButton.setVisible(GridEditMode.SIMULTANEOUS.equals(getFormOptions().getGridEditMode()) && !isViewMode()
                && checkEditAllowed());
        editButton.setVisible(isViewMode() && getFormOptions().isShowEditButton() && checkEditAllowed());
        cancelButton.setVisible(!isViewMode() && checkEditAllowed() && getFormOptions().isOpenInViewMode());
        addButton.setVisible(!isViewMode() && getFormOptions().isShowAddButton() && checkEditAllowed());
    }

}
