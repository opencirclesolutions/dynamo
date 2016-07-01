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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.impl.ModelBasedFieldFactory;
import com.ocs.dynamo.filter.listener.FilterChangeEvent;
import com.ocs.dynamo.filter.listener.FilterListener;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.Searchable;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.And;
import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.Action.Handler;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * A search form that is constructed based on the metadata model
 * 
 * @author bas.rutten
 * @param <ID>
 *            The type of the primary key of the entity
 * @param <T>
 *            The type of the entity
 */
public class ModelBasedSearchForm<ID extends Serializable, T extends AbstractEntity<ID>> extends
        AbstractModelBasedForm<ID, T> implements FilterListener, Button.ClickListener {

    // the types of search field
    protected enum FilterType {
        BETWEEN, BOOLEAN, ENTITY, ENUM, LIKE, EQUAL
    }

    private static final long serialVersionUID = -7226808613882934559L;

    // list of additional filters (these will be included in every query, even
    // if the search form is empty)
    private List<Filter> additionalFilters = new ArrayList<>();

    // button to clear the search form
    private Button clearButton;

    // the currently active search filters
    private List<Filter> currentFilters = new ArrayList<Filter>();

    /**
     * Custom field factory
     */
    private ModelBasedFieldFactory<T> fieldFactory;

    // the number of fields added
    private int fieldsAdded = 0;

    // the layout that contains all the filter fields
    private Component filterLayout;

    // the main layout
    private Layout form;

    // the list of filter groups
    private Map<String, FilterGroup> groups = new HashMap<>();

    // the number of columns
    private int nrOfColumns = 1;

    // the object to search when the button is pressed
    private Searchable searchable;

    // the search button
    private Button searchButton;

    // sub form
    private List<FormLayout> subForms = new ArrayList<>();

    // toggle button (hides/shows the search form)
    private Button toggleButton;

    /**
     * Constructor
     * 
     * @param searchable
     *            the component on which to carry out the search
     * @param entityModel
     *            the entity model
     * @param formOptions
     *            the form options
     */
    public ModelBasedSearchForm(Searchable searchable, EntityModel<T> entityModel,
            FormOptions formOptions) {
        this(searchable, entityModel, formOptions, null, null);
    }

    /**
     * Constructor
     * 
     * @param searchable
     *            the component on which to carry out the search
     * @param entityModel
     *            the entity model
     * @param formOptions
     *            the form options
     * @param additionalFilters
     *            the additional filters to apply to every search action
     * @param fieldFilters
     *            a map of filters to apply to the individual fields
     */
    public ModelBasedSearchForm(Searchable searchable, EntityModel<T> entityModel,
            FormOptions formOptions, List<Filter> additionalFilters,
            Map<String, Filter> fieldFilters) {
        super(formOptions, fieldFilters, entityModel);
        this.fieldFactory = ModelBasedFieldFactory.getSearchInstance(entityModel,
                getMessageService());
        this.additionalFilters = additionalFilters == null ? new ArrayList<Filter>()
                : additionalFilters;
        this.currentFilters.addAll(this.additionalFilters);
        this.searchable = searchable;
    }

    /**
     * Callback method that is called when the user toggles the visibility of the search form
     * 
     * @param visible
     *            indicates the search fields are visible now
     */
    protected void afterSearchFieldToggle(boolean visible) {
        // override in subclasses
    }

    @Override
    public void build() {
        VerticalLayout main = new DefaultVerticalLayout(false, true);

        // add a wrapper for adding an action handler
        Panel formWrapper = new Panel();
        main.addComponent(formWrapper);

        // create the search form
        filterLayout = constructFilterLayout(getEntityModel());
        formWrapper.setContent(filterLayout);

        // action handler for carrying out a search after an Enter press
        formWrapper.addActionHandler(new Handler() {

            private static final long serialVersionUID = -2136828212405809213L;

            private Action enter = new ShortcutAction(null, ShortcutAction.KeyCode.ENTER, null);

            @Override
            public Action[] getActions(Object target, Object sender) {
                return new Action[] { enter };
            }

            @Override
            public void handleAction(Action action, Object sender, Object target) {
                if (action == enter) {
                    search();
                }
            }
        });

        // create the button bar
        HorizontalLayout buttonBar = new DefaultHorizontalLayout();
        main.addComponent(buttonBar);

        // create the search button
        searchButton = new Button(message("ocs.search"));
        searchButton.setImmediate(true);
        searchButton.addClickListener(this);
        buttonBar.addComponent(searchButton);

        // create the clear button
        clearButton = new Button(message("ocs.clear"));
        clearButton.setImmediate(true);
        clearButton.addClickListener(this);
        clearButton.setVisible(!getFormOptions().isHideClearButton());
        buttonBar.addComponent(clearButton);

        // create the toggle button
        toggleButton = new Button(message("ocs.hide"));
        toggleButton.addClickListener(this);
        toggleButton.setVisible(getFormOptions().isShowToggleButton());
        buttonBar.addComponent(toggleButton);

        // add custom buttons
        postProcessButtonBar(buttonBar);

        // initial search
        if (!currentFilters.isEmpty()) {
            Filter composite = new And(currentFilters.toArray(new Filter[0]));
            searchable.search(composite);
        }
        postProcessFilterGroups(groups);

        setCompositionRoot(main);
    }

    @Override
    public void buttonClick(ClickEvent event) {
        if (event.getButton() == searchButton) {
            search();
        } else if (event.getButton() == clearButton) {
            clear();
            search();
        } else if (event.getButton() == toggleButton) {
            boolean visible = filterLayout.isVisible();
            if (visible) {
                toggleButton.setCaption(message("ocs.show.search.fields"));
            } else {
                toggleButton.setCaption(message("ocs.hide.search.fields"));
            }
            filterLayout.setVisible(!visible);
            afterSearchFieldToggle(filterLayout.isVisible());
        }
    }

    /**
     * Clears all filters then performs a fresh search
     */
    protected void clear() {
        // Clear all filter groups
        for (FilterGroup group : groups.values()) {
            group.reset();
        }
        currentFilters.clear();
        currentFilters.addAll(additionalFilters);
    }

    /**
     * Creates a custom field - override in subclasses if needed
     * 
     * @param entityModel
     *            the entity model of the entity to search for
     * @param attributeModel
     *            the attribute model the attribute model of the property that is bound to the field
     * @return
     */
    protected Field<?> constructCustomField(EntityModel<T> entityModel,
            AttributeModel attributeModel) {
        return null;
    }

    /**
     * Callback method that can be used to create any additional search fields (that are not
     * governed by the entity model)
     * 
     * @return
     */
    protected List<Component> constructExtraSearchFields() {
        return new ArrayList<>();
    }

    /**
     * Creates a search field based on an attribute model
     * 
     * @param entityModel
     *            the entity model of the entity to search for
     * @param attributeModel
     *            the attribute model the attribute model of the property that is bound to the field
     * @return
     */
    protected Field<?> constructField(EntityModel<T> entityModel, AttributeModel attributeModel) {

        Field<?> field = constructCustomField(entityModel, attributeModel);
        if (field == null) {
            EntityModel<?> em = getFieldEntityModel(attributeModel);
            field = fieldFactory.constructField(attributeModel, getFieldFilters(), em);
        }

        if (field != null) {
            field.setSizeFull();
        }
        return field;
    }

    /**
     * Constructs a filter group
     * 
     * @param entityModel
     *            the entity model
     * @param attributeModel
     *            the attribute model
     * @return
     */
    protected FilterGroup constructFilterGroup(EntityModel<T> entityModel,
            AttributeModel attributeModel) {
        Field<?> field = this.constructField(entityModel, attributeModel);
        if (field != null) {
            FilterType filterType = FilterType.BETWEEN;
            if (String.class.isAssignableFrom(attributeModel.getType())) {
                filterType = FilterType.LIKE;
            } else if (Boolean.class.isAssignableFrom(attributeModel.getType())
                    || Boolean.TYPE.isAssignableFrom(attributeModel.getType())) {
                filterType = FilterType.BOOLEAN;
            } else if (attributeModel.getType().isEnum()) {
                filterType = FilterType.ENUM;
            } else if (AbstractEntity.class.isAssignableFrom(attributeModel.getType())
                    || AttributeType.DETAIL.equals(attributeModel.getAttributeType())) {
                // search for an entity
                filterType = FilterType.ENTITY;
            } else if (attributeModel.isSearchForExactValue()) {
                filterType = FilterType.EQUAL;
            }

            Component comp = field;
            Field<?> auxField = null;
            if (FilterType.BETWEEN.equals(filterType)) {
                // in case of a between value, construct two fields for the
                // lower
                // and upper bounds
                String from = message("ocs.from");
                field.setCaption(attributeModel.getDisplayName() + " " + from);
                auxField = constructField(entityModel, attributeModel);
                String to = message("ocs.to");
                auxField.setCaption(attributeModel.getDisplayName() + " " + to);
                auxField.setVisible(true);
                HorizontalLayout layout = new DefaultHorizontalLayout();
                layout.setSizeFull();
                layout.addComponent(field);
                layout.addComponent(auxField);
                comp = layout;
            }
            return new FilterGroup(attributeModel, attributeModel.getPath(), filterType, comp,
                    field, auxField);
        }
        return null;
    }

    /**
     * Builds the layout that contains the various search filters
     * 
     * @param entityModel
     *            the entity model
     * @return
     */
    protected Component constructFilterLayout(EntityModel<T> entityModel) {
        if (nrOfColumns == 1) {
            form = new FormLayout();
            // don't use all the space unless it's a popup window
            if (!getFormOptions().isPopup()) {
                form.setStyleName(DynamoConstants.CSS_CLASS_HALFSCREEN);
            }
        } else {
            // create a number of form layouts next to each others
            form = new GridLayout(nrOfColumns, 1);
            form.setSizeFull();

            for (int i = 0; i < nrOfColumns; i++) {
                FormLayout column = new FormLayout();
                column.setMargin(true);
                subForms.add(column);
                form.addComponent(column);
            }
        }

        // add any extra fields
        List<Component> extra = constructExtraSearchFields();
        for (Component c : extra) {
            if (nrOfColumns == 1) {
                form.addComponent(c);
            } else {
                int index = fieldsAdded % nrOfColumns;
                subForms.get(index).addComponent(c);
            }
            fieldsAdded++;
        }

        // iterate over the searchable attributes and add a field for each
        iterate(entityModel.getAttributeModels());
        return form;
    }

    /**
     * Programmatically force a search
     * 
     * @param propertyId
     *            the property ID to search on
     * @param value
     *            the value of the property
     */
    public void forceSearch(String propertyId, Object value) {
        setSearchValue(propertyId, value);
        search();
    }

    public Map<String, FilterGroup> getGroups() {
        return groups;
    }

    public int getNrOfColumns() {
        return nrOfColumns;
    }

    /**
     * Recursively iterate over the attribute models (including nested models) and add search fields
     * if the fields are searchable
     * 
     * @param form
     *            the form to add the search fields to
     * @param attributeModels
     *            the attribute models to iterate over
     */
    private void iterate(List<AttributeModel> attributeModels) {
        for (AttributeModel attributeModel : attributeModels) {
            if (attributeModel.isSearchable()) {

                FilterGroup group = constructFilterGroup(getEntityModel(), attributeModel);
                group.getFilterComponent().setSizeFull();

                if (nrOfColumns == 1) {
                    form.addComponent(group.getFilterComponent());
                } else {
                    int index = fieldsAdded % nrOfColumns;
                    subForms.get(index).addComponent(group.getFilterComponent());
                }

                // register with the form and set the listener
                group.addListener(this);
                groups.put(group.getPropertyId(), group);

                fieldsAdded++;
            }

            // also support search on nested attributes
            if (attributeModel.getNestedEntityModel() != null) {
                EntityModel<?> nested = attributeModel.getNestedEntityModel();
                iterate(nested.getAttributeModels());
            }
        }
    }

    @Override
    public void onFilterChange(FilterChangeEvent event) {
        currentFilters.remove(event.getOldFilter());
        if (event.getNewFilter() != null) {
            currentFilters.add(event.getNewFilter());
        }
    }

    /**
     * Callback method that allows the user to modify the button bar
     * 
     * @param groups
     */
    protected void postProcessButtonBar(Layout buttonBar) {
        // Use in subclass to add additional buttons
    }

    /**
     * Callback method that allows the user to modify the various filter groups
     * 
     * @param groups
     *            the filter groups
     */
    protected void postProcessFilterGroups(Map<String, FilterGroup> groups) {
        // overwrite in subclasses
    }

    /**
     * Trigger the actual search
     */
    public void search() {
        if (!currentFilters.isEmpty()) {
            Filter composite = new And(currentFilters.toArray(new Filter[0]));
            searchable.search(composite);
        } else {
            // search without any filters
            searchable.search(null);
        }
    }

    public void setNrOfColumns(int nrOfColumns) {
        this.nrOfColumns = nrOfColumns;
    }

    /**
     * Manually set the value for a certain search field
     * 
     * @param propertyId
     *            the ID of the property
     * @param value
     *            the desired value
     */
    public void setSearchValue(String propertyId, Object value) {
        setSearchValue(propertyId, value, null);
    }

    /**
     * Manually set the value for a certain search field
     * 
     * @param propertyId
     *            the ID of the property
     * @param value
     *            the desired value for the main field
     * @param auxValue
     *            the desired value for the auxiliary field
     */
    public void setSearchValue(String propertyId, Object value, Object auxValue) {
        FilterGroup group = groups.get(propertyId);
        group.getField().setValue((Object) value);
        if (group.getAuxField() != null) {
            group.getAuxField().setValue(auxValue);
        }
    }

    /**
     * Refreshes the
     */
    public void refresh() {
        for (FilterGroup group : getGroups().values()) {
            if (group.getField() instanceof Refreshable) {
                ((Refreshable) group.getField()).refresh();
            }
        }
    }
}
