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
package com.ocs.dynamo.ui.composite.form;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.*;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.filter.InPredicate;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.CanAssignEntity;
import com.ocs.dynamo.ui.NestedComponent;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.UseInViewMode;
import com.ocs.dynamo.ui.component.*;
import com.ocs.dynamo.ui.composite.autofill.FormFillDialog;
import com.ocs.dynamo.ui.composite.layout.DetailsEditLayout;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.layout.HasSelectedItem;
import com.ocs.dynamo.ui.composite.layout.TabWrapper;
import com.ocs.dynamo.ui.composite.type.AttributeGroupMode;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.util.TriConsumer;
import com.ocs.dynamo.utils.*;
import com.vaadin.componentfactory.EnhancedFormLayout;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.Binding;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.server.StreamResource;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;

/**
 * An edit form that is constructed based on an entity model
 *
 * @param <ID> the type of the primary key
 * @param <T>  the type of the entity
 * @author bas.rutten
 */
public class ModelBasedEditForm<ID extends Serializable, T extends AbstractEntity<ID>>
        extends AbstractModelBasedForm<ID, T> implements NestedComponent, HasSelectedItem<T> {

    private static final String COLSPAN = "colspan";

    private static final String BACK_BUTTON_DATA = "backButton";

    private static final String CANCEL_BUTTON_DATA = "cancelButton";

    private static final String EDIT_BUTTON_DATA = "editButton";

    private static final String NEXT_BUTTON_DATA = "nextButton";

    private static final String PREV_BUTTON_DATA = "prevButton";

    private static final String REFRESH_BUTTON_DATA = "refreshButton";

    private static final String SAVE_BUTTON_DATA = "saveButton";

    private static final long serialVersionUID = 2201140375797069148L;

    /**
     * Callback method that is executed after the user completes are cancels an edit
     * action
     */
    @Getter
    @Setter
    private TriConsumer<Boolean, Boolean, T> afterEditDone;

    /**
     * Map for keeping track of which attributes have already been bound to a
     * component
     */
    private final Map<Boolean, Set<String>> alreadyBound = new HashMap<>();

    /**
     * The fields to which to assign the currently selected entity after the
     * selected entity changes
     */
    private final List<CanAssignEntity<ID, T>> assignEntityToFields = new ArrayList<>();

    /**
     * For keeping track of attribute groups per view mode
     */
    private final Map<Boolean, Map<String, Object>> attributeGroups = new HashMap<>();

    /**
     * Buttons per view mode and functions (save, edit, etc)
     */
    private final Map<Boolean, Map<String, List<Button>>> buttons = new HashMap<>();

    /**
     * The relations to fetch when selecting a single detail relation
     */
    private FetchJoinInformation[] detailJoins;

    /**
     * Callback method that is executed to check whether editing is allowed
     */
    @Getter
    @Setter
    private BooleanSupplier editAllowed = () -> true;

    /**
     * The selected entity
     */
    @Getter
    private T entity;

    /**
     * The field factory
     */
    private final FieldFactory fieldFactory = FieldFactory.getInstance();

    /**
     * Indicates whether the fields have been post processed
     */
    private boolean fieldsProcessed;

    /**
     * Method that is called to determine the parent group that an attribute group
     * belongs to
     */
    @Getter
    @Setter
    private Function<String, String> findParentGroup;

    /**
     * Map from tab index to the first field on each tab
     */
    private final Map<Integer, Focusable<?>> firstFields = new HashMap<>();

    /**
     * Map for keeping track of which attributes have been added to the form by
     * means of a form item (typically, labels)
     */
    private final Map<Boolean, Map<AttributeModel, FormItem>> formItems = new HashMap<>();

    /**
     * Groups for data binding (one for each view mode)
     */
    private final Map<Boolean, Binder<T>> groups = new HashMap<>();

    @Getter
    @Setter
    private Supplier<Boolean> hasNextEntity;

    @Getter
    @Setter
    private Supplier<Boolean> hasPreviousEntity;

    /**
     * A map containing all the labels that were added - used to replace the label
     * values as the selected entity changes
     */
    private final Map<Boolean, Map<AttributeModel, Component>> labels = new HashMap<>();

    private VerticalLayout mainEditLayout;

    private VerticalLayout mainViewLayout;

    @Getter
    @Setter
    private boolean nestedMode;

    @Getter
    @Setter
    private Supplier<T> nextEntity;

    /**
     * Callback method that is executed when the "back" button is clicked
     */
    @Getter
    @Setter
    private Runnable onBackButtonClicked = () -> {
    };

    @Getter
    @Setter
    private String[] parentGroupHeaders;

    @Getter
    @Setter
    private BiConsumer<FlexLayout, Boolean> postProcessButtonBar;

    private final Map<Boolean, Map<AttributeModel, Component>> previews = new HashMap<>();

    @Getter
    @Setter
    private Supplier<T> previousEntity;

    private final BaseService<ID, T> service;

    /**
     * Whether the component supports iteration over the records
     */
    @Getter
    @Setter
    private boolean supportsIteration;

    private final Map<Boolean, TabWrapper> tabs = new HashMap<>();

    private final Map<Boolean, HorizontalLayout> titleBars = new HashMap<>();

    private final Map<Boolean, Span> titleLabels = new HashMap<>();

    private boolean viewMode;

    /**
     * Constructor
     *
     * @param entity       the entity
     * @param service      the service
     * @param entityModel  the entity model
     * @param formOptions  the form options
     * @param fieldFilters the field filters
     */
    public ModelBasedEditForm(T entity, BaseService<ID, T> service, EntityModel<T> entityModel, FormOptions formOptions,
                              Map<String, SerializablePredicate<?>> fieldFilters) {
        super(formOptions, fieldFilters, entityModel);
        addClassName(DynamoConstants.CSS_MODEL_BASED_EDIT_FORM);

        this.service = service;
        this.entity = entity;

        Class<T> clazz = service.getEntityClass();

        // set up a bean field group for automatic binding and validation
        Binder<T> binder = new BeanValidationBinder<>(clazz);
        binder.setBean(entity);
        groups.put(Boolean.FALSE, binder);

        binder = new BeanValidationBinder<>(clazz);
        binder.setBean(entity);
        groups.put(Boolean.TRUE, binder);

        // init panel maps
        attributeGroups.put(Boolean.TRUE, new HashMap<>());
        attributeGroups.put(Boolean.FALSE, new HashMap<>());

        alreadyBound.put(Boolean.TRUE, new HashSet<>());
        alreadyBound.put(Boolean.FALSE, new HashSet<>());

        buttons.put(Boolean.TRUE, new HashMap<>());
        buttons.put(Boolean.FALSE, new HashMap<>());
    }

    /**
     * Adds a cascade listener to the input field for an attribute
     *
     * @param am    the attribute
     * @param event the value change event for the input field
     */
    @SuppressWarnings("unchecked")
    private <S> void addCascadeListener(AttributeModel am, ValueChangeEvent<S> event) {
        for (String cascadePath : am.getCascadeAttributes()) {
            CascadeMode cm = am.getCascadeMode(cascadePath);
            if (CascadeMode.BOTH.equals(cm) || CascadeMode.EDIT.equals(cm)) {
                Component cascadeField = getField(isViewMode(), cascadePath);
                if (cascadeField instanceof Cascadable) {
                    Cascadable<S> ca = (Cascadable<S>) cascadeField;
                    if (event.getValue() == null) {
                        ca.clearAdditionalFilter();
                    } else {
                        if (event.getValue() instanceof Collection) {
                            ca.setAdditionalFilter(new InPredicate<>(am.getCascadeFilterPath(cascadePath),
                                    (Collection<S>) event.getValue()));
                        } else {
                            ca.setAdditionalFilter(
                                    new EqualsPredicate<>(am.getCascadeFilterPath(cascadePath), event.getValue()));
                        }
                    }
                } else {
                    // field not found or does not support cascading
                    throw new OCSRuntimeException("Cannot setup cascading from " + am.getPath() + " to " + cascadePath);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addConvertersAndValidators(AttributeModel attributeModel, boolean viewMode, Component field,
                                            FieldCreationContext context) {
        if (!(field instanceof ServiceBasedDetailsEditGrid)) {
            BindingBuilder<T, ?> builder = groups.get(viewMode).forField((HasValue<?, ?>) field);

            fieldFactory.addConvertersAndValidators(this, builder, attributeModel, context,
                    getComponentContext().findCustomConverter(attributeModel),
                    getComponentContext().findCustomValidator(attributeModel),
                    getComponentContext().findCustomRequiredValidator(attributeModel));
            builder.bind(attributeModel.getPath());
        }
    }

    /**
     * Adds a field for a certain attribute
     *
     * @param parent         the layout to which to add the field
     * @param entityModel    the entity model
     * @param attributeModel the attribute model
     * @param tabIndex       the index of the tab (when adding the field to a tab
     *                       sheet)
     */
    private void addField(HasComponents parent, EntityModel<T> entityModel, AttributeModel attributeModel,
                          int tabIndex) {
        AttributeType type = attributeModel.getAttributeType();
        if (!alreadyBound.get(isViewMode()).contains(attributeModel.getPath()) && attributeModel.isVisibleInForm()) {
            if (EditableType.READ_ONLY.equals(attributeModel.getEditableType()) || isViewMode()) {
                if (AttributeType.LOB.equals(type)) {
                    // image preview (or label if no preview is available)
                    Component c = constructImagePreview(attributeModel);
                    EnhancedFormLayout container = new EnhancedFormLayout();
                    FormItem fi = container.addFormItem(c, attributeModel.getDisplayName(VaadinUtils.getLocale()));
                    formItems.get(isViewMode()).put(attributeModel, fi);
                    parent.add(container);
                    previews.get(isViewMode()).put(attributeModel, c);
                } else {
                    Component comp = findCustomComponent(entityModel, attributeModel, viewMode);
                    if (comp instanceof UseInViewMode) {
                        constructField(parent, entityModel, attributeModel, true, tabIndex);
                    } else { // otherwise, display a label
                        constructLabel(parent, entityModel, attributeModel, tabIndex);
                    }
                }
            } else {
                if (AttributeType.BASIC.equals(type) || AttributeType.MASTER.equals(type)
                        || AttributeType.DETAIL.equals(type) || AttributeType.ELEMENT_COLLECTION.equals(type)
                        || AttributeType.LOB.equals(type)) {
                    constructField(parent, entityModel, attributeModel, false, tabIndex);
                }
            }
            alreadyBound.get(isViewMode()).add(attributeModel.getPath());
        }
    }

    /**
     * Adds multiple grouped together attributes
     *
     * @param parent         the parent component
     * @param entityModel    the entity model
     * @param attributeModel the attribute model
     * @param tabIndex       the tab index
     * @param field          the component
     */
    private void addGroupedAttributes(HasComponents parent, EntityModel<T> entityModel, AttributeModel attributeModel,
                                      int tabIndex, Component field) {
        // construct a separate layout for holding the fields that will be displayed
        // together
        EnhancedFormLayout rowLayout = new EnhancedFormLayout();
        rowLayout.setWidth("100%");
        rowLayout.addClassName(DynamoConstants.CSS_GROUP_TOGETHER_LAYOUT);

        List<ResponsiveStep> steps = new ArrayList<>();

        GroupTogetherMode groupTogetherMode = getComponentContext().getGroupTogetherMode();
        Integer groupTogetherWidth = getComponentContext().getGroupTogetherWidth();

        GroupTogetherMode gtm = groupTogetherMode != null ? groupTogetherMode
                : SystemPropertyUtils.getDefaultGroupTogetherMode();
        Integer gtw = groupTogetherWidth != null ? groupTogetherWidth
                : SystemPropertyUtils.getDefaultGroupTogetherWidth();

        if (GroupTogetherMode.PERCENTAGE.equals(gtm)) {
            int percentage = 100 / attributeModel.getGroupTogetherWith().size();
            for (int i = 0; i < attributeModel.getGroupTogetherWith().size() + 1; i++) {
                steps.add(new ResponsiveStep(percentage + "%", i + 1));
            }
        } else {
            for (int i = 0; i < attributeModel.getGroupTogetherWith().size() + 1; i++) {
                steps.add(new ResponsiveStep((i * gtw) + "px", i + 1));
            }
        }
        rowLayout.setResponsiveSteps(steps);

        // when in nested mode (e.g. DetailsEditLayout) stretch over entire width
        if (nestedMode) {
            rowLayout.getElement().setAttribute(COLSPAN, "2");
        }

        parent.add(rowLayout);
        rowLayout.add(field);

        for (String path : attributeModel.getGroupTogetherWith()) {
            AttributeModel nestedAm = getEntityModel().getAttributeModel(path);
            if (nestedAm != null) {
                addField(rowLayout, entityModel, nestedAm, tabIndex);
            }
        }
    }

    /**
     * Adds the component for a single attribute
     *
     * @param parent         the parent layout to which to add the component
     * @param attributeModel the attribute model for the component
     * @param field          the component
     */
    @SuppressWarnings("rawtypes")
    private void addSingleAttribute(HasComponents parent, AttributeModel attributeModel, Component field) {
        boolean colspan = field instanceof BaseDetailsEditGrid || field instanceof ElementCollectionGrid
                || field instanceof DetailsEditLayout;
        if (parent instanceof EnhancedFormLayout form && colspan) {
            int colSpan = form.getResponsiveSteps().size();
            if (SystemPropertyUtils.mustIndentGrids()) {
                // indent grids (default)
                FormItem fi = form.addFormItem(field,
                        new Span(attributeModel.getDisplayName(VaadinUtils.getLocale())));
                fi.getElement().setAttribute(COLSPAN, Integer.toString(colSpan));
                formItems.get(isViewMode()).put(attributeModel, fi);
            } else {
                // do not indent grids
                if (field instanceof BaseDetailsEditGrid baseDetailsEditGrid) {
                    baseDetailsEditGrid.setLabel(attributeModel.getDisplayName(VaadinUtils.getLocale()));
                } else if (field instanceof DetailsEditLayout detailsEditLayout) {
                    detailsEditLayout.setLabel(attributeModel.getDisplayName(VaadinUtils.getLocale()));
                } else {
                    ElementCollectionGrid collectionGrid = (ElementCollectionGrid) field;
                    collectionGrid.setLabel(attributeModel.getDisplayName(VaadinUtils.getLocale()));
                }

                field.getElement().setAttribute(COLSPAN, Integer.toString(colSpan));
                form.add(field);
            }
        } else {
            parent.add(field);
        }
    }

    /**
     * Add a listener to respond to a tab change and focus the first available field
     *
     * @param wrapper the tab wrapper to add the listener to
     */
    private void addTabChangeListener(TabWrapper wrapper) {
        wrapper.addSelectedChangeListener(event -> {
            int index = event.getSource().getSelectedIndex();

            if (getComponentContext().getAfterTabSelected() != null) {
                getComponentContext().getAfterTabSelected().accept(index);
            }

            if (firstFields.get(index) != null) {
                firstFields.get(index).focus();
            }
        });
    }

    /**
     * Adds listeners for responding to a file upload
     *
     * @param attributeModel the attribute model for which the upload components is
     *                       meant
     * @param field          the upload component
     */
    private void addUploadFieldListeners(AttributeModel attributeModel, Component field) {
        if (field instanceof UploadComponent upload) {
            if (attributeModel.getFileNameProperty() != null) {
                upload.setFileNameConsumer(fileName -> {
                    ClassUtils.setFieldValue(getEntity(), attributeModel.getFileNameProperty(), fileName);
                    setLabelValue(attributeModel.getFileNameProperty(), fileName);
                });
            }

            if (getComponentContext().getAfterUploadCompleted() != null) {
                upload.setAfterUploadCompleted(getComponentContext().getAfterUploadCompleted());
            }
        }
    }

    /**
     * Main build method - lazily constructs the layout for either edit or view mode
     */
    @Override
    public void build() {

        // the first time the component is opened, determine the view mode
        if (mainViewLayout == null && mainEditLayout == null) {
            this.viewMode = !checkEditAllowed() || (getFormOptions().isOpenInViewMode() && entity.getId() != null);
        }

        if (entity != null && getComponentContext().getAfterEntitySet() != null) {
            getComponentContext().getAfterEntitySet().accept(entity);
        }

        if (isViewMode()) {
            if (mainViewLayout == null) {
                Map<AttributeModel, Component> map = new HashMap<>();
                labels.put(Boolean.TRUE, map);

                Map<AttributeModel, FormItem> formItemMap = new HashMap<>();
                formItems.put(Boolean.TRUE, formItemMap);

                Map<AttributeModel, Component> previewMap = new HashMap<>();
                previews.put(Boolean.TRUE, previewMap);

                mainViewLayout = buildMainLayout(getEntityModel());
            }
            removeAll();
            add(mainViewLayout);
        } else {
            if (mainEditLayout == null) {
                Map<AttributeModel, Component> map = new HashMap<>();
                labels.put(Boolean.FALSE, map);

                Map<AttributeModel, FormItem> formItemMap = new HashMap<>();
                formItems.put(Boolean.FALSE, formItemMap);

                Map<AttributeModel, Component> previewMap = new HashMap<>();
                previews.put(Boolean.FALSE, previewMap);

                mainEditLayout = buildMainLayout(getEntityModel());
                for (CanAssignEntity<ID, T> field : assignEntityToFields) {
                    field.assignEntity(entity);
                }

                if (!fieldsProcessed) {
                    Consumer<ModelBasedEditForm<ID, T>> postProcessEditFields = getComponentContext()
                            .getPostProcessEditFields();
                    if (postProcessEditFields != null) {
                        postProcessEditFields.accept(this);
                    }

                    fieldsProcessed = true;
                }
            }
            removeAll();
            add(mainEditLayout);
        }
    }

    /**
     * Constructs the main layout of the screen
     *
     * @param entityModel the entity model to base the layout on
     * @return the constructed layout
     */
    protected VerticalLayout buildMainLayout(EntityModel<T> entityModel) {
        VerticalLayout layout = new DefaultVerticalLayout(false, false);
        layout.addClassName(DynamoConstants.CSS_MODEL_BASED_EDIT_FORM_MAIN);

        titleLabels.put(isViewMode(), constructTitleLabel());
        titleBars.put(isViewMode(), new DefaultHorizontalLayout(true, true));

        if (getFormOptions().isShowEditFormCaption()) {
            titleBars.get(isViewMode()).add(titleLabels.get(isViewMode()));
        }

        FlexLayout buttonBar;
        if (!nestedMode) {
            // no button bar in "nested mode", i.e. when part of a DetailsEditLayout
            buttonBar = constructButtonBar(false);
            buttonBar.setSizeUndefined();
            if (getFormOptions().isPlaceButtonBarAtTop()) {
                layout.add(buttonBar);
            } else {
                titleBars.get(isViewMode()).add(buttonBar);
            }
        }
        layout.add(titleBars.get(isViewMode()));
        titleBars.get(isViewMode()).setVisible(!nestedMode);

        HasComponents form;
        if (entityModel.usesDefaultGroupOnly()) {
            form = new EnhancedFormLayout();
            setResponsiveSteps(form);
        } else {
            form = new DefaultVerticalLayout(false, false);
        }

        if (!entityModel.usesDefaultGroupOnly()) {
            // display the attributes in groups
            boolean useTabs = AttributeGroupMode.TABSHEET.equals(getFormOptions().getAttributeGroupMode());
            if (useTabs) {
                TabWrapper tabSheet = new TabWrapper();
                tabs.put(isViewMode(), tabSheet);
                form.add(tabSheet);

                // focus first field after tab change
                addTabChangeListener(tabSheet);
            }

            if (getParentGroupHeaders() != null && getParentGroupHeaders().length > 0) {
                // extra layer of grouping (always tabs)
                processParentHeaderGroups(form, useTabs);
            } else {
                // just one layer of attribute groups
                processAttributeGroups(entityModel, form, useTabs);
            }
        } else {
            // iterate over the attributes and add them to the form (without any
            // grouping)
            for (AttributeModel attributeModel : entityModel.getAttributeModels()) {
                addField(form, entityModel, attributeModel, 0);
            }
        }

        constructCascadeListeners();
        layout.add((Component) form);

        if (firstFields.get(0) != null) {
            firstFields.get(0).focus();
        }

        if (!nestedMode) {
            buttonBar = constructButtonBar(true);
            buttonBar.setSizeUndefined();
            layout.add(buttonBar);
        }

        setDefaultValues();
        disableCreateOnlyFields();

        if (getComponentContext().getAfterEditFormBuilt() != null) {
            getComponentContext().getAfterEditFormBuilt().accept(form, isViewMode());
        }

        return layout;
    }

    private boolean checkEditAllowed() {
        return editAllowed == null || editAllowed.getAsBoolean();
    }

    /**
     * Checks the state of the iteration (prev/next) buttons. These will be shown in
     * view mode or if the form only supports an edit mode
     *
     * @param checkEnabled whether to check if the buttons should be enabled
     */
    private void checkIterationButtonState(boolean checkEnabled) {
        for (Button button : filterButtons(NEXT_BUTTON_DATA)) {
            button.setVisible(isSupportsIteration() && getFormOptions().isShowNextButton() && entity.getId() != null);
            button.setEnabled(
                    checkEnabled && button.isVisible() && (isViewMode() || !getFormOptions().isOpenInViewMode()));
        }
        for (Button button : filterButtons(PREV_BUTTON_DATA)) {
            button.setVisible(isSupportsIteration() && getFormOptions().isShowPrevButton() && entity.getId() != null);
            button.setEnabled(
                    checkEnabled && button.isVisible() && (isViewMode() || !getFormOptions().isOpenInViewMode()));
        }
    }

    /**
     * Constructs a container for grouping multiple attributes
     *
     * @param parent      the parent of the layout
     * @param useTabs     whether to use tabs
     * @param tabs        tab sheet component to add the new layout to
     * @param messageKey  message key of the caption
     * @param lowestLevel whether we are at the lowest level
     * @return the constructed component
     */
    private HasComponents constructAttributeGroupLayout(HasComponents parent, boolean useTabs, TabWrapper tabs,
                                                        String messageKey, boolean lowestLevel) {
        Component innerLayout;
        if (lowestLevel) {
            innerLayout = new EnhancedFormLayout();
            setResponsiveSteps((HasComponents) innerLayout);
        } else {
            innerLayout = new DefaultVerticalLayout(false, false);
        }

        if (useTabs) {
            // create a new tab and add it to the tab sheet
            Tab innerTab = tabs.addTab(message(messageKey), null, innerLayout, null);
            attributeGroups.get(isViewMode()).put(messageKey, innerTab);
        } else {
            CollapsiblePanel panel = new CollapsiblePanel(message(messageKey), innerLayout);
            parent.add(panel);
            attributeGroups.get(isViewMode()).put(messageKey, panel);
        }
        return (HasComponents) innerLayout;
    }

    /**
     * Constructs the button for navigating back
     *
     * @param buttonBar the button bar to which to add the button
     */
    private void constructBackButton(FlexLayout buttonBar) {
        Button backButton = new Button(message("ocs.back"));
        backButton.setIcon(VaadinIcon.BACKWARDS.create());

        if (getOnBackButtonClicked() != null) {
            backButton.addClickListener(event -> getOnBackButtonClicked().run());
        }
        backButton.setVisible(isViewMode() && getFormOptions().isShowBackButton());
        buttonBar.add(backButton);
        storeButton(BACK_BUTTON_DATA, backButton);
    }

    /**
     * Constructs the button bar
     *
     * @param bottom whether this is the bottom button bar
     * @return the button bar layout
     */
    private FlexLayout constructButtonBar(boolean bottom) {
        FlexLayout buttonBar = new DefaultFlexLayout();

        constructBackButton(buttonBar);

        // in edit mode, display a cancel button
        Button cancelButton = new Button(message("ocs.cancel"));
        cancelButton.addClickListener(event -> {
            if (entity.getId() != null) {
                entity = service.fetchById(entity.getId(), getDetailJoins());
            }

            if (afterEditDone != null) {
                afterEditDone.accept(true, entity.getId() == null, entity);
            }
        });

        // display button when in edit mode and explicitly specified, or when creating a
        // new entity
        // in nested mode
        cancelButton.setVisible((!isViewMode() && getFormOptions().isShowCancelButton()));
        cancelButton.setIcon(VaadinIcon.BAN.create());
        buttonBar.add(cancelButton);
        storeButton(CANCEL_BUTTON_DATA, cancelButton);

        if (!isViewMode()) {
            Button saveButton = constructSaveButton(bottom);
            buttonBar.add(saveButton);
            storeButton(SAVE_BUTTON_DATA, saveButton);
        }

        constructEditButton(buttonBar);

        if (isViewMode() && getFormOptions().isShowRefreshButton()) {
            Button refreshButton = constructRefreshButton();
            buttonBar.add(refreshButton);
        }

        Button prevButton = constructPrevButton(buttonBar);
        Button nextButton = constructNextButton(buttonBar);
        prevButton.setVisible(isSupportsIteration() && getFormOptions().isShowPrevButton() && entity.getId() != null);
        nextButton.setVisible(isSupportsIteration() && getFormOptions().isShowNextButton() && entity.getId() != null);

        if (getFormOptions().isShowFormFillButton()) {
            if (!SystemPropertyUtils.isFormAutofillEnabled()) {
                throw new OCSRuntimeException("Form fill button visible, but auto-fill functionality has not been enabled");
            }

            Button formFillButton = new Button(message("ocs.fill.form"));
            formFillButton.setIcon(VaadinIcon.MAGIC.create());
            formFillButton.addClickListener(event -> fillForm());
            buttonBar.add(formFillButton);
        }

        if (postProcessButtonBar != null) {
            postProcessButtonBar.accept(buttonBar, isViewMode());
        }

        return buttonBar;
    }

    /**
     * Adds any value change listeners for handling cascaded search
     */
    @SuppressWarnings("unchecked")
    private <S> void constructCascadeListeners() {
        for (AttributeModel am : getEntityModel().getCascadeAttributeModels()) {
            HasValue<?, S> field = (HasValue<?, S>) getField(isViewMode(), am.getPath());
            if (field != null) {
                ValueChangeListener<ValueChangeEvent<?>> cascadeListener = event -> addCascadeListener(am, event);
                field.addValueChangeListener(cascadeListener);
            }
        }
    }

    /**
     * Constructs the button for opening the edit mode
     *
     * @param buttonBar the button bar to which to add the button
     */
    private void constructEditButton(FlexLayout buttonBar) {
        Button editButton = new Button(message("ocs.edit"));
        editButton.setIcon(VaadinIcon.PENCIL.create());
        editButton.addClickListener(event -> setViewMode(false));
        buttonBar.add(editButton);
        storeButton(EDIT_BUTTON_DATA, editButton);
        editButton.setVisible(isViewMode() && getFormOptions().isShowEditButton() && checkEditAllowed());
    }

    /**
     * Constructs a field or label for a certain attribute
     *
     * @param parent         the parent layout to which to add the field
     * @param entityModel    the entity model
     * @param attributeModel the attribute model
     * @param viewMode       whether the screen is in view mode
     */
    @SuppressWarnings({"unchecked"})
    private void constructField(HasComponents parent, EntityModel<T> entityModel, AttributeModel attributeModel,
                                boolean viewMode, int tabIndex) {

        EntityModel<?> fieldEntityModel = getFieldEntityModel(attributeModel);
        // allow the user to override the construction of a field
        Component field = findCustomComponent(entityModel, attributeModel, viewMode);
        FieldCreationContext context = FieldCreationContext.create().attributeModel(attributeModel)
                .fieldEntityModel(fieldEntityModel).fieldFilters(getFieldFilters()).viewMode(viewMode)
                .parentEntity(entity).build();
        if (field == null) {
            field = fieldFactory.constructField(context);
        }

        if (field instanceof URLField) {
            ((URLField) field)
                    .setEditable(!isViewMode() && !EditableType.CREATE_ONLY.equals(attributeModel.getEditableType()));
        }

        if (field != null) {
            field.setId(attributeModel.getName());
            if (field instanceof HasSize && !(field instanceof TextArea)) {
                ((HasSize) field).setSizeFull();
            }

            addConvertersAndValidators(attributeModel, viewMode, field, context);
            addUploadFieldListeners(attributeModel, field);

            if (field instanceof DetailsEditLayout) {
                DetailsEditLayout<?, ?, ID, T> del = (DetailsEditLayout<?, ?, ID, T>) field;
                del.setEnclosingForm(this);
            }

            if (!attributeModel.getGroupTogetherWith().isEmpty()) {
                addGroupedAttributes(parent, entityModel, attributeModel, tabIndex, field);
            } else {
                addSingleAttribute(parent, attributeModel, field);
            }

            // store a reference to the first field, so we can give it focus
            if (!isViewMode() && firstFields.get(tabIndex) == null && ((HasEnabled) field).isEnabled()
                    && !(field instanceof Checkbox) && (field instanceof Focusable)) {
                firstFields.put(tabIndex, (Focusable<?>) field);
            }

            // keep track of which child fields have to be updated
            if (field instanceof CanAssignEntity) {
                ((CanAssignEntity<ID, T>) field).assignEntity(entity);
                assignEntityToFields.add((CanAssignEntity<ID, T>) field);
            }
        }
    }

    /**
     * Constructs a preview component for displaying an image
     *
     * @param attributeModel the attribute model for the image property
     * @return the constructed component
     */
    private Component constructImagePreview(AttributeModel attributeModel) {
        if (attributeModel.isImage()) {
            byte[] bytes = ClassUtils.getBytes(getEntity(), attributeModel.getName());
            if (bytes == null || bytes.length == 0) {
                return new Span(message("ocs.no.preview.available"));
            } else {
                StreamResource sr = new StreamResource(attributeModel.getDisplayName(VaadinUtils.getLocale()),
                        () -> new ByteArrayInputStream(new byte[0]));
                Image image = new Image(sr, attributeModel.getDisplayName(VaadinUtils.getLocale()));
                image.setClassName(DynamoConstants.CSS_IMAGE_PREVIEW);
                return image;
            }
        } else {
            return new Span(message("ocs.no.preview.available"));
        }
    }

    /**
     * Constructs an internal link button. This is used for displaying a clickable
     * link in view mode
     *
     * @param attributeModel the attribute model
     * @return the constructed button
     */
    @SuppressWarnings("unchecked")
    private <ID2 extends Serializable, S extends AbstractEntity<ID2>> InternalLinkButton<ID2, S> constructInternalLinkButton(
            AttributeModel attributeModel) {
        S value = (S) ClassUtils.getFieldValue(entity, attributeModel.getName());
        return new InternalLinkButton<>(value, (EntityModel<S>) getFieldEntityModel(attributeModel), attributeModel);
    }

    /**
     * Constructs a label (or other read-only component) and adds it to the form
     *
     * @param parent         the parent component to which the label must be added
     * @param entityModel    the entity model
     * @param attributeModel the attribute model for the attribute for which to
     *                       create a* label
     * @param tabIndex       the number of components added so far
     */
    private void constructLabel(HasComponents parent, EntityModel<T> entityModel, AttributeModel attributeModel,
                                int tabIndex) {
        Component comp;
        if (attributeModel.isUrl()) {
            // read-only URL field
            String value = (String) ClassUtils.getFieldValue(entity, attributeModel.getName());
            value = StringUtils.prependProtocol(value);
            Anchor anchor = new Anchor(value == null ? "" : value, value);
            anchor.setTarget("_blank");
            labels.get(isViewMode()).put(attributeModel, anchor);
            comp = anchor;
        } else if (attributeModel.isNavigable()) {
            // read only internal link
            InternalLinkButton<?, ?> linkButton = constructInternalLinkButton(attributeModel);
            labels.get(isViewMode()).put(attributeModel, linkButton);
            comp = linkButton;
        } else {
            Span label = constructLabel(entity, attributeModel);
            labels.get(isViewMode()).put(attributeModel, label);
            comp = label;
        }

        if (!attributeModel.getGroupTogetherWith().isEmpty()) {
            // group multiple labels on the same line
            EnhancedFormLayout rowLayout = new EnhancedFormLayout();

            List<ResponsiveStep> steps = new ArrayList<>();
            for (int i = 0; i < attributeModel.getGroupTogetherWith().size() + 1; i++) {
                steps.add(new ResponsiveStep(i * 200 + "px", i + 1));
            }
            rowLayout.setResponsiveSteps(steps);

            parent.add(rowLayout);
            FormItem formItem = rowLayout.addFormItem(comp, attributeModel.getDisplayName(VaadinUtils.getLocale()));
            formItems.get(isViewMode()).put(attributeModel, formItem);

            for (String path : attributeModel.getGroupTogetherWith()) {
                AttributeModel am = entityModel.getAttributeModel(path);
                if (am != null) {
                    addField(rowLayout, getEntityModel(), am, tabIndex);
                }
            }
        } else {
            // attributes not grouped on the same row
            if (parent instanceof FormLayout) {
                FormItem formItem = ((FormLayout) parent).addFormItem(comp,
                        attributeModel.getDisplayName(VaadinUtils.getLocale()));
                formItems.get(isViewMode()).put(attributeModel, formItem);
            } else {
                parent.add(comp);
            }
        }
    }

    /**
     * Constructs the button for navigating to the next entity
     *
     * @param buttonBar the button bar to which to add the button
     * @return the constructed button
     */
    private Button constructNextButton(FlexLayout buttonBar) {
        Button nextButton = new Button(message("ocs.next"));
        nextButton.setIcon(VaadinIcon.ARROW_RIGHT.create());
        nextButton.addClickListener(e -> {
            T next = nextEntity == null ? null : nextEntity.get();
            if (next != null) {
                setEntity(next, true);
            }
            getNextButtons().forEach(button -> button.setEnabled(existsNextEntity()));

        });
        nextButton.setEnabled(existsNextEntity());
        storeButton(NEXT_BUTTON_DATA, nextButton);

        buttonBar.add(nextButton);
        return nextButton;
    }

    /**
     * Constructs the button for navigating to the previous entity
     *
     * @param buttonBar the button bar to which to add the button
     * @return the constructed button
     */
    private Button constructPrevButton(FlexLayout buttonBar) {
        Button prevButton = new Button(message("ocs.previous"));
        prevButton.setIcon(VaadinIcon.ARROW_LEFT.create());
        prevButton.addClickListener(e -> {
            T prev = previousEntity == null ? null : previousEntity.get();
            if (prev != null) {
                setEntity(prev, true);
            }
            getPreviousButtons().forEach(button -> button.setEnabled(existsPreviousEntity()));
        });
        storeButton(PREV_BUTTON_DATA, prevButton);
        buttonBar.add(prevButton);
        prevButton.setEnabled(existsPreviousEntity());
        return prevButton;
    }

    /**
     * Constructs a button that can be used to refresh the screen
     *
     * @return the button
     */
    private Button constructRefreshButton() {
        Button refreshButton = new Button(message("ocs.refresh"));
        refreshButton.setIcon(VaadinIcon.RECYCLE.create());
        storeButton(REFRESH_BUTTON_DATA, refreshButton);
        refreshButton.addClickListener(event -> setEntity(service.fetchById(entity.getId(), getDetailJoins())));
        return refreshButton;
    }

    /**
     * Constructs the save button
     *
     * @param bottom indicates whether this is the button at the bottom of the
     *               screen
     */
    private Button constructSaveButton(boolean bottom) {
        Button saveButton = new Button(
                (entity != null && entity.getId() != null) ? message("ocs.save.existing") : message("ocs.save.new"));
        saveButton.setIcon(VaadinIcon.SAFE.create());
        saveButton.addClickListener(event -> {
            try {
                // validate all fields
                boolean error = validateAllFields();
                if (!error) {
                    if (getFormOptions().isConfirmSave()) {
                        // ask for confirmation before saving
                        service.validate(entity);
                        showSaveConfirmDialog();
                    } else {
                        BiConsumer<ModelBasedEditForm<ID, T>, T> customSaveAction = getComponentContext()
                                .getCustomSaveAction();
                        if (customSaveAction != null) {
                            customSaveAction.accept(this, entity);
                        } else {
                            doSave();
                        }
                    }
                }
            } catch (RuntimeException ex) {
                Predicate<RuntimeException> customSaveExceptionHandler = getComponentContext()
                        .getCustomSaveExceptionHandler();
                if (customSaveExceptionHandler == null || !customSaveExceptionHandler.test(ex)) {
                    handleSaveException(ex);
                }
            }
        });

        // enable/disable save button based on form validity
        if (bottom) {
            groups.get(isViewMode()).getFields().forEach(f -> {
                if (f instanceof HasValidation hv) {
                    ValueChangeListener<ValueChangeEvent<?>> listener = event -> hv.setErrorMessage(null);
                    f.addValueChangeListener(listener);
                }
            });
        }
        return saveButton;
    }

    private Span constructTitleLabel() {
        String value = getTitleLabelValue();
        return new Span(value);
    }

    /**
     * Disables any fields that are only editable when creating a new entity
     */
    private void disableCreateOnlyFields() {
        if (!isViewMode()) {
            for (AttributeModel am : getEntityModel().getAttributeModels()) {
                Component field = getField(isViewMode(), am.getPath());
                if (field != null && EditableType.CREATE_ONLY.equals(am.getEditableType())) {
                    ((HasEnabled) field).setEnabled(entity.getId() == null);
                }
            }
        }
    }

    /**
     * Perform the actual save action
     */
    public void doSave() {
        boolean isNew = entity.getId() == null;

        entity = service.save(entity);
        setEntity(service.fetchById(entity.getId(), getDetailJoins()));
        showTrayNotification(message("ocs.changes.saved"));

        // set to view mode, load the view mode screen, and fill the
        // details
        if (getFormOptions().isOpenInViewMode()) {
            viewMode = true;
            build();
        }

        if (afterEditDone != null) {
            afterEditDone.accept(false, isNew, getEntity());
        }

    }

    private boolean existsNextEntity() {
        return hasNextEntity != null && hasNextEntity.get();
    }

    private boolean existsPreviousEntity() {
        return hasPreviousEntity != null && hasPreviousEntity.get();
    }

    /**
     * Filters the buttons and returns those that match the provided data string
     *
     * @param data the String to filter on
     * @return the filtered list of buttons
     */
    private List<Button> filterButtons(String data) {
        List<Button> list = buttons.get(isViewMode()).get(data);
        return Collections.unmodifiableList(list == null ? new ArrayList<>() : list);
    }

    /**
     * Finds a custom component creator for an attribute model defined in the
     * component context
     *
     * @param entityModel    the entity model
     * @param attributeModel the attribute model
     * @param viewMode       whether the form is in view mode
     * @return the component
     */
    private Component findCustomComponent(EntityModel<?> entityModel, AttributeModel attributeModel, boolean viewMode) {
        Function<CustomFieldContext, Component> customFieldCreator = getComponentContext()
                .getCustomFieldCreator(attributeModel.getPath());
        if (customFieldCreator != null) {
            return customFieldCreator.apply(CustomFieldContext.builder().entityModel(entityModel)
                    .attributeModel(attributeModel).viewMode(viewMode).build());
        }
        return null;
    }

    public CollapsiblePanel getAttributeGroupPanel(String key) {
        Object c = attributeGroups.get(isViewMode()).get(key);
        if (c instanceof CollapsiblePanel) {
            return (CollapsiblePanel) c;
        }
        return null;
    }

    public List<Button> getBackButtons() {
        return filterButtons(BACK_BUTTON_DATA);
    }

    /**
     * Returns the binding for a field
     *
     * @param path the path of the property
     * @return the binding for the field that is used for editing the property
     */
    public Binding<T, ?> getBinding(String path) {
        Optional<Binding<T, ?>> binding = groups.get(viewMode).getBinding(path);
        return binding.orElse(null);
    }

    public List<Button> getCancelButtons() {
        return filterButtons(CANCEL_BUTTON_DATA);
    }

    public FetchJoinInformation[] getDetailJoins() {
        return detailJoins;
    }

    public List<Button> getEditButtons() {
        return filterButtons(EDIT_BUTTON_DATA);
    }

    /**
     * Retrieves a field for a certain property
     *
     * @param viewMode  whether the screen is in view mode
     * @param fieldName the name of the field/property
     * @return the field
     */
    private Component getField(boolean viewMode, String fieldName) {
        Optional<Binding<T, ?>> binding = groups.get(viewMode).getBinding(fieldName);
        return binding.map(tBinding -> (Component) tBinding.getField()).orElse(null);
    }

    /**
     * Returns the field for the given property
     *
     * @param path the path to the property
     * @return the component
     */
    public Component getField(String path) {
        return getField(isViewMode(), path);
    }

    /**
     * Returns the field for the given path and casts it to the provided type
     *
     * @param <C>   the type to cast to
     * @param path  the path to the attribute
     * @param clazz the class to cast to
     * @return the component, cast to the requested type
     */
    @SuppressWarnings("unchecked")
    public <C extends Component> C getField(String path, Class<C> clazz) {
        return (C) getField(isViewMode(), path);
    }

    /**
     * Returns the field with the given name, if it exists, cast to a HasValue
     *
     * @param fieldName the name of the field
     * @return the field
     */
    @SuppressWarnings("unchecked")
    public <U> HasValue<?, U> getFieldAsHasValue(String fieldName) {
        return (HasValue<?, U>) getField(isViewMode(), fieldName);
    }

    /**
     * Returns an Optional that contains the field with the given name, if it exists
     *
     * @param fieldName the name of the field
     * @return the field
     */
    public Optional<Component> getFieldOptional(String fieldName) {
        return Optional.ofNullable(getField(fieldName));
    }

    public Optional<HasValue<?, ?>> getFieldOptionalAsHasValue(String fieldName) {
        return Optional.ofNullable(getFieldAsHasValue(fieldName));
    }

    /**
     * Returns the collection of all input fields for the specified mode
     *
     * @param viewMode whether to return the components for the view mode
     * @return the fields
     */
    public Collection<HasValue<?, ?>> getFields(boolean viewMode) {
        return groups.get(viewMode).getFields().toList();
    }

    /**
     * Returns a label for a property
     *
     * @param path the path to the property
     * @return the label component
     */
    public Component getLabel(String path) {
        AttributeModel am = getEntityModel().getAttributeModel(path);
        if (am != null) {
            return labels.get(isViewMode()).get(am);
        }
        return null;
    }

    public List<Button> getNextButtons() {
        return filterButtons(NEXT_BUTTON_DATA);
    }

    /**
     * Calculates the default value for a numeric field
     *
     * @param am           the attribute model for the field
     * @param defaultValue the default value
     * @return the default value
     */
    private Object getNumericDefaultValue(AttributeModel am, Object defaultValue) {
        DecimalFormat nf = (DecimalFormat) DecimalFormat.getInstance(VaadinUtils.getLocale());
        char sep = nf.getDecimalFormatSymbols().getDecimalSeparator();

        // set correct precision for non-integers
        if (NumberUtils.isDouble(am.getType()) || NumberUtils.isFloat(am.getType())
                || BigDecimal.class.equals(am.getType())) {
            nf.setMaximumFractionDigits(am.getPrecision());
            nf.setMinimumFractionDigits(am.getPrecision());
            nf.setGroupingUsed(false);
            defaultValue = nf.format(defaultValue);
        }
        defaultValue = defaultValue.toString().replace('.', sep);
        if (am.isPercentage()) {
            defaultValue = defaultValue + "%";
        }
        return defaultValue;
    }

    private String getParentGroup(String attributeGroup) {
        if (findParentGroup == null) {
            return null;
        }
        return findParentGroup.apply(attributeGroup);
    }

    public List<Button> getPreviousButtons() {
        return filterButtons(PREV_BUTTON_DATA);
    }

    public List<Button> getRefreshButtons() {
        return filterButtons(REFRESH_BUTTON_DATA);
    }

    public List<Button> getSaveButtons() {
        return filterButtons(SAVE_BUTTON_DATA);
    }

    public int getSelectedTabIndex() {
        return tabs.get(isViewMode()).getSelectedIndex();
    }

    private String getTitleLabelValue() {
        String mainValue = EntityModelUtils.getDisplayPropertyValue(entity, getEntityModel());
        if (isViewMode()) {
            return message("ocs.modelbasededitform.title.view",
                    getEntityModel().getDisplayName(VaadinUtils.getLocale()), mainValue);
        } else {
            if (entity.getId() == null) {
                // create a new entity
                return message("ocs.modelbasededitform.title.create",
                        getEntityModel().getDisplayName(VaadinUtils.getLocale()));
            } else {
                // update an existing entity
                return message("ocs.modelbasededitform.title.update",
                        getEntityModel().getDisplayName(VaadinUtils.getLocale()), mainValue);
            }
        }
    }

    /**
     * Check whether a certain attribute group is visible
     *
     * @param key the message key by which the group is identifier
     * @return true whether the group is visible, false otherwise
     */
    public boolean isAttributeGroupVisible(String key) {
        Object c = attributeGroups.get(false).get(key);
        return isGroupVisible(c);
    }

    /**
     * Check if a certain attribute group is visible
     *
     * @param group the component representing the attribute group
     * @return true whether the group is visible, false otherwise
     */
    private boolean isGroupVisible(Object group) {
        if (group != null) {
            if (group instanceof Component comp) {
                return comp.isVisible();
            } else if (group instanceof Tab tab) {
                return tab.isVisible();
            }
        }
        return false;
    }

    /**
     * Check if the form is valid
     *
     * @return true if this is the case, false otherwise
     */
    public boolean isValid() {
        return groups.get(isViewMode()).isValid();
    }

    public boolean isViewMode() {
        return viewMode;
    }

    /**
     * Processes the various attribute groups
     *
     * @param entityModel the entity model
     * @param form        the form to add the attribute group container components
     *                    to
     * @param useTabs     whether to use tabs (as opposed to panels)
     */
    private void processAttributeGroups(EntityModel<T> entityModel, HasComponents form, boolean useTabs) {
        int tabIndex = 0;
        for (String attributeGroup : entityModel.getAttributeGroups()) {

            if (entityModel.isAttributeGroupVisible(attributeGroup, isViewMode())) {
                HasComponents innerForm = constructAttributeGroupLayout(form, useTabs, tabs.get(isViewMode()),
                        attributeGroup, true);
                for (AttributeModel attributeModel : entityModel.getAttributeModelsForGroup(attributeGroup)) {
                    addField(innerForm, entityModel, attributeModel, tabIndex);
                }
                if (AttributeGroupMode.TABSHEET.equals(getFormOptions().getAttributeGroupMode())) {
                    tabIndex++;
                }
            }
        }
    }

    /**
     * Processes all fields that are part of a property group
     *
     * @param parentGroupHeader the group header
     * @param innerForm         the form layout to which to add the fields
     * @param useInnerTabs      the tab sheet to which to add the fields
     * @param innerTabs         whether we are displaying tabs
     * @param tabIndex          the index of the tab (used when the groups are
     *                          displayed in a tab sheet)
     */
    private void processParentHeaderGroup(String parentGroupHeader, HasComponents innerForm, boolean useInnerTabs,
                                          TabWrapper innerTabs, int tabIndex) {
        // display a group if it is not the default group
        for (String attributeGroup : getEntityModel().getAttributeGroups()) {
            if ((!EntityModel.DEFAULT_GROUP.equals(attributeGroup)
                    || getEntityModel().isAttributeGroupVisible(attributeGroup, viewMode))
                    && Objects.equals(getParentGroup(attributeGroup), parentGroupHeader)) {
                HasComponents innerLayout2 = constructAttributeGroupLayout(innerForm, useInnerTabs, innerTabs,
                        attributeGroup, true);
                for (AttributeModel attributeModel : getEntityModel().getAttributeModelsForGroup(attributeGroup)) {
                    addField(innerLayout2, getEntityModel(), attributeModel, tabIndex);
                }
            }
        }
    }

    /**
     * Process the parent group headers
     *
     * @param form    the form to which to add the containers for the parent groups
     * @param useTabs whether to use tabs (as opposed to panels)
     */
    private void processParentHeaderGroups(HasComponents form, boolean useTabs) {
        int tabIndex = 0;
        for (String parentGroupHeader : getParentGroupHeaders()) {
            HasComponents innerForm = constructAttributeGroupLayout(form, useTabs, tabs.get(isViewMode()),
                    parentGroupHeader, false);

            // add a tab sheet on the inner level if needed
            TabWrapper innerTabs = null;
            boolean useInnerTabs = !useTabs;
            if (useInnerTabs) {
                innerTabs = new TabWrapper();
                innerForm.add(innerTabs);
            }

            // add all appropriate inner groups
            processParentHeaderGroup(parentGroupHeader, innerForm, useInnerTabs, innerTabs, tabIndex);
            tabIndex++;
        }
    }

    public void putAttributeGroupPanel(String key, Component c) {
        attributeGroups.get(isViewMode()).put(key, c);
    }

    /**
     * Refreshes the binding for the currently selected entity. This can be used to
     * force a fresh after you make changes to e.g. the converters after the entity
     * has already been set
     */
    public void refreshBinding() {
        groups.get(isViewMode()).setBean(entity);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void refreshFieldFilters() {

        // if there is a field filter, make sure it is used
        for (String propertyName : getFieldFilters().keySet()) {
            Optional<Binding<T, ?>> binding = groups.get(isViewMode()).getBinding(propertyName);
            if (binding.isPresent()) {
                HasValue<?, ?> field = binding.get().getField();
                if (field instanceof CustomEntityField) {
                    SerializablePredicate<?> fieldFilter = getFieldFilters().get(propertyName);
                    ((CustomEntityField) field).refresh(fieldFilter);
                }
            }
        }

        // otherwise refresh
        groups.get(isViewMode()).getFields().forEach(field -> {
            if (field instanceof Refreshable && !(field instanceof CustomEntityField)) {
                ((Refreshable) field).refresh();
            }
        });
    }

    /**
     * Refreshes the label for the specified property
     *
     * @param propertyName the name of the property
     */
    public void refreshLabel(String propertyName) {
        AttributeModel am = getEntityModel().getAttributeModel(propertyName);
        if (am != null && labels.get(isViewMode()) != null) {
            Component comp = labels.get(isViewMode()).get(am);
            Object value = ClassUtils.getFieldValue(entity, propertyName);
            String formatted = FormatUtils.formatPropertyValue(getEntityModelFactory(), getMessageService(), am, value,
                    ", ", VaadinUtils.getLocale(), VaadinUtils.getTimeZoneId(), am.getCurrencySymbol());
            if (comp instanceof Span) {
                ((Span) comp).setText(formatted == null ? "" : formatted);
            } else if (comp instanceof Anchor anchor) {
                formatted = StringUtils.prependProtocol(formatted);
                anchor.setHref(formatted == null ? "" : formatted);
                anchor.setText(formatted);
            } else if (comp instanceof InternalLinkButton<?, ?> linkButton) {
                linkButton.setText(formatted);
                linkButton.setValue(value);
            }
        }
    }

    /**
     * Refreshes the contents of all labels and URL fields
     */
    private void refreshLabelsAndUrls() {
        if (labels.get(isViewMode()) != null) {
            for (Entry<AttributeModel, Component> e : labels.get(isViewMode()).entrySet()) {
                refreshLabel(e.getKey().getPath());
            }
        }

        // also replace the title labels
        Span titleSpan = titleLabels.get(isViewMode());
        if (titleSpan != null) {
            titleSpan.setText(getTitleLabelValue());
        }
    }

    /**
     * Removes any error messages from the individual form components
     */
    private void resetComponentErrors() {
        groups.get(isViewMode()).getFields().forEach(f -> {
            if (f instanceof HasValidation) {
                ((HasValidation) f).setErrorMessage(null);
            }
        });
    }

    /**
     * Resets the tab sheet so that the first tabs is selected if needed
     */
    public void resetTabsheetIfNeeded() {
        if (tabs.get(isViewMode()) != null && !getFormOptions().isPreserveSelectedTab()) {
            tabs.get(isViewMode()).setSelectedIndex(0);
        }
    }

    /**
     * Selects the tab specified by the provided index
     *
     * @param index the index of the tab to select
     */
    public void selectTab(int index) {
        if (tabs.get(isViewMode()) != null) {
            tabs.get(isViewMode()).setSelectedIndex(index);
        }
    }

    /**
     * Shows/hides an attribute group
     *
     * @param key     the message key by which the group is identified
     * @param visible the desired visibility of the group
     */
    public void setAttributeGroupVisible(String key, boolean visible) {
        Object object = attributeGroups.get(false).get(key);
        setGroupVisible(object, visible);
        object = attributeGroups.get(true).get(key);
        setGroupVisible(object, visible);
    }

    /**
     * Shows or hides the component for a certain property - this will work*
     * regardless of the view
     *
     * @param propertyName the name of the property for which to show/hide the
     *                     property
     * @param visible      whether to make the component visible
     */
    public void setComponentVisible(String propertyName, boolean visible) {
        setLabelVisible(propertyName, visible);
        Component field = getField(isViewMode(), propertyName);
        if (field != null) {
            field.setVisible(visible);
        }
    }

    /**
     * Sets the default value for a field
     *
     * @param field the field
     * @param value the default value
     */
    private <R> void setDefaultValue(HasValue<?, R> field, R value) {
        field.setValue(value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setDefaultValues() {
        if (!isViewMode() && entity.getId() == null) {
            for (AttributeModel am : getEntityModel().getAttributeModels()) {
                Component field = getField(isViewMode(), am.getPath());
                if (field != null && am.getDefaultValue() != null) {
                    Object defaultValue = am.getDefaultValue();
                    if (NumberUtils.isNumeric(am.getType())) {
                        defaultValue = getNumericDefaultValue(am, defaultValue);
                    } else if (Boolean.class.equals(am.getType()) || boolean.class.equals(am.getType())) {
                        defaultValue = Boolean.valueOf(defaultValue.toString());
                    } else if (am.getType().isEnum()) {
                        defaultValue = Enum.valueOf((Class<Enum>) am.getType(), defaultValue.toString());
                    }
                    setDefaultValue((HasValue<?, Object>) field, defaultValue);
                }
            }
        }
    }

    public void setDetailJoins(FetchJoinInformation[] detailJoins) {
        this.detailJoins = detailJoins;
    }

    public void setEntity(T entity) {
        setEntity(entity, entity.getId() != null);
    }

    private void setEntity(T entity, boolean checkIterationButtons) {
        this.entity = entity;

        refreshFieldFilters();

        // inform all children
        for (CanAssignEntity<ID, T> field : assignEntityToFields) {
            field.assignEntity(this.entity);
        }

        if (getComponentContext().getAfterEntitySet() != null) {
            getComponentContext().getAfterEntitySet().accept(entity);
        }

        setViewMode(getFormOptions().isOpenInViewMode() && entity.getId() != null, checkIterationButtons);

        // recreate the group
        groups.get(isViewMode()).setBean(this.entity);

        // "rebuild" so that the correct layout is displayed
        build();
        refreshLabelsAndUrls();
        resetTabsheetIfNeeded();

        // enable/disable fields for create only mode
        disableCreateOnlyFields();
        setDefaultValues();

        // change caption depending on entity state
        updateSaveButtonCaptions();

        for (Button cancelButton : getCancelButtons()) {
            cancelButton.setVisible((!isViewMode() && getFormOptions().isShowCancelButton()));
        }

        triggerCascadeListeners();

        if (getComponentContext().getAfterEntitySelected() != null) {
            getComponentContext().getAfterEntitySelected().accept(this, entity);
        }

    }

    /**
     * Hides/shows a group of components
     *
     * @param component the parent component of the group
     * @param visible   whether to set the component to visible
     */
    private void setGroupVisible(Object component, boolean visible) {
        if (component != null) {
            if (component instanceof Component) {
                ((Component) component).setVisible(visible);
            } else if (component instanceof Tab) {
                ((Tab) component).setVisible(visible);
            }
        }
    }

    /**
     * Set a label value
     *
     * @param propertyName the name of the property for which to set the label
     * @param value        the value
     */
    public void setLabelValue(String propertyName, String value) {
        AttributeModel am = getEntityModel().getAttributeModel(propertyName);
        if (am != null) {
            Component comp = labels.get(isViewMode()).get(am);
            if (comp instanceof Span) {
                ((Span) comp).setText(value == null ? "" : value);
            }
        }
    }

    /**
     * Shows or hides a label
     *
     * @param propertyName the name of the property for which to show/hide the label
     * @param visible      whether to show the label
     */
    public void setLabelVisible(String propertyName, boolean visible) {
        AttributeModel am = getEntityModel().getAttributeModel(propertyName);
        if (am != null) {
            FormItem item = formItems.get(isViewMode()).get(am);
            if (item != null) {
                item.setVisible(visible);
            }
        }
    }

    /**
     * Sets the responsive steps on the form layout. This determines when a second
     * column will be shown
     *
     * @param comp the component
     */
    private void setResponsiveSteps(HasComponents comp) {
        if (comp instanceof EnhancedFormLayout form) {
            if (getComponentContext().getMaxEditFormWidth() != null) {
                form.setMaxWidth(getComponentContext().getMaxEditFormWidth());
            }

            List<String> columnThresholds = getComponentContext().getEditColumnThresholds();

            if (columnThresholds == null || columnThresholds.isEmpty()) {
                columnThresholds = SystemPropertyUtils.getDefaultEditColumnThresholds();
            }

            if (columnThresholds != null && !columnThresholds.isEmpty()) {
                // custom responsive steps
                List<ResponsiveStep> list = new ArrayList<>();
                int i = 0;
                for (String t : columnThresholds) {
                    list.add(new ResponsiveStep(t, i + 1));
                    i++;
                }
                form.setResponsiveSteps(list);
            }
        }
    }

    /**
     * Overwrite the value of the title label
     *
     * @param value the desired value
     */
    public void setTitleLabel(String value) {
        titleLabels.get(isViewMode()).setText(value);
    }

    /**
     * Sets the view mode of the component
     *
     * @param viewMode the desired view mode
     */
    public void setViewMode(boolean viewMode) {
        setViewMode(viewMode, true);
    }

    /**
     * Switches the form from or to view mode
     *
     * @param viewMode the new view mode
     */
    private void setViewMode(boolean viewMode, boolean checkIterationButtons) {
        boolean oldMode = this.viewMode;

        // check what the new view mode must become and adapt the screen
        this.viewMode = !checkEditAllowed() || viewMode;

        groups.get(isViewMode()).setBean(entity);

        refreshLabelsAndUrls();
        build();

        checkIterationButtonState(checkIterationButtons);

        // if this is the first time in edit mode, post process the editable
        // fields
        if (!isViewMode() && !fieldsProcessed) {
            Consumer<ModelBasedEditForm<ID, T>> postProcessEditFields = getComponentContext()
                    .getPostProcessEditFields();
            if (postProcessEditFields != null) {
                postProcessEditFields.accept(this);
            }

            fieldsProcessed = true;
        }

        // update button captions
        updateSaveButtonCaptions();
        disableCreateOnlyFields();

        // preserve tab index when switching between view modes
        if (tabs.get(oldMode) != null) {
            int selectedIndex = tabs.get(oldMode).getSelectedIndex();
            tabs.get(isViewMode()).setSelectedIndex(selectedIndex);
            if (!isViewMode() && firstFields.get(selectedIndex) != null) {
                firstFields.get(selectedIndex).focus();
            }
        } else if (firstFields.get(0) != null) {
            firstFields.get(0).focus();
        }

        resetComponentErrors();
        if (oldMode != this.viewMode) {
            if (getComponentContext().getAfterModeChanged() != null) {
                getComponentContext().getAfterModeChanged().accept(this, isViewMode());
            }
        }
    }

    /**
     * Shows a confirm dialog that asks the user to confirm before saving changes
     */
    private void showSaveConfirmDialog() {
        VaadinUtils.showConfirmDialog(getMessageService().getMessage("ocs.confirm.save", VaadinUtils.getLocale(),
                getEntityModel().getDisplayName(VaadinUtils.getLocale())), () -> {
            try {
                BiConsumer<ModelBasedEditForm<ID, T>, T> customSaveAction = getComponentContext()
                        .getCustomSaveAction();
                if (customSaveAction != null) {
                    customSaveAction.accept(this, entity);
                } else {
                    doSave();
                }
            } catch (RuntimeException ex) {
                Predicate<RuntimeException> customSaveExceptionHandler = getComponentContext()
                        .getCustomSaveExceptionHandler();
                if (customSaveExceptionHandler == null || !customSaveExceptionHandler.test(ex)) {
                    handleSaveException(ex);
                }
            }
        });
    }

    /**
     * Stores a button in the data to button map
     *
     * @param data   the data key
     * @param button the button
     */
    private void storeButton(String data, Button button) {
        Map<String, List<Button>> map = buttons.get(isViewMode());
        map.putIfAbsent(data, new ArrayList<>());
        map.get(data).add(button);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void triggerCascadeListeners() {
        for (AttributeModel am : getEntityModel().getCascadeAttributeModels()) {
            HasValue field = (HasValue) getField(isViewMode(), am.getPath());
            if (field != null) {
                if (field.getValue() != null) {
                    Object value = field.getValue();
                    field.clear();
                    field.setValue(value);
                }
            }
        }
    }

    /**
     * Sets the caption of the save button depending on whether we are creating or
     * updating an entity
     */
    private void updateSaveButtonCaptions() {
        for (Button b : getSaveButtons()) {
            if (entity.getId() != null) {
                b.setText(message("ocs.save.existing"));
            } else {
                b.setText(message("ocs.save.new"));
            }
        }
    }

    /**
     * Validates all fields and returns true if an error occurs
     *
     * @return true when a validation error occurs
     */
    @Override
    public boolean validateAllFields() {

        BinderValidationStatus<T> status = groups.get(isViewMode()).validate();
        boolean error = !status.isOk();

        // validate nested form and components
        error |= groups.get(isViewMode()).getFields().anyMatch(field -> {
            if (field instanceof NestedComponent) {
                return ((NestedComponent) field).validateAllFields();
            }
            return false;
        });
        return error;
    }

    private void fillForm() {
        FormFillDialog dialog = new FormFillDialog(this.mainEditLayout,
                    this.getEntityModel(), this);
        dialog.buildAndOpen();
    }

    @Override
    public T getSelectedItem() {
        return getEntity();
    }

}
