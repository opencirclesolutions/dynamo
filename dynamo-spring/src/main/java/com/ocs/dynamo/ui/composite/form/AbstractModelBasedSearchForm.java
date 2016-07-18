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
import java.util.List;
import java.util.Map;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.impl.ModelBasedFieldFactory;
import com.ocs.dynamo.filter.listener.FilterChangeEvent;
import com.ocs.dynamo.filter.listener.FilterListener;
import com.ocs.dynamo.ui.Searchable;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.And;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * An abstract model search form that servers as the basis for other model based search forms
 * 
 * @author bas.rutten
 *
 * @param <ID>
 *            the type of the ID of the entity
 * @param <T>
 *            the type of the entity
 */
public abstract class AbstractModelBasedSearchForm<ID extends Serializable, T extends AbstractEntity<ID>>
        extends AbstractModelBasedForm<ID, T> implements FilterListener, Button.ClickListener {

    private static final long serialVersionUID = 2146875385041665280L;

    /**
     * Any filters that will always be applied to any search query (use these to restrict the result
     * set beforehand)
     */
    private List<Filter> additionalFilters = new ArrayList<>();

    /**
     * Button to clear the search form
     */
    private Button clearButton;

    /**
     * The filter that is created by taking all individual field filters together
     */
    private Filter compositeFilter;

    /**
     * The list of currently active search filters
     */
    private List<Filter> currentFilters = new ArrayList<Filter>();

    /**
     * Field factory used for constructing search fields
     */
    private ModelBasedFieldFactory<T> fieldFactory;

    /**
     * The layout that holds the various filters
     */
    private Layout filterLayout;

    /**
     * The object that will be searched when the user presses the "Search" button
     */
    private Searchable searchable;

    /**
     * The "search" button
     */
    private Button searchButton;

    /**
     * The toggle button (hides/shows the search form)
     */
    private Button toggleButton;

    /**
     * The panel that wraps around the filter form
     */
    private Panel wrapperPanel;

    /**
     * 
     * @param searchable
     * @param entityModel
     * @param formOptions
     * @param additionalFilters
     * @param fieldFilters
     */
    public AbstractModelBasedSearchForm(Searchable searchable, EntityModel<T> entityModel,
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
     *            indicates if the search fields are visible now
     */
    protected void afterSearchFieldToggle(boolean visible) {
        // override in subclasses
    }

    @Override
    public void build() {
        VerticalLayout main = new DefaultVerticalLayout(false, true);

        // add a wrapper for adding an action handler
        wrapperPanel = new Panel();
        main.addComponent(wrapperPanel);

        // create the search form
        filterLayout = constructFilterLayout();
        wrapperPanel.setContent(filterLayout);

        // action handler for carrying out a search after an Enter press
        wrapperPanel.addActionHandler(new Handler() {

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

        constructButtonBar(buttonBar);

        // add custom buttons
        postProcessButtonBar(buttonBar);

        // add any custom functionality
        postProcessLayout();

        // initial search
        search();

        setCompositionRoot(main);
    }

    @Override
    public void buttonClick(ClickEvent event) {
        if (event.getButton() == searchButton) {
            search();
        } else if (event.getButton() == clearButton) {
            if (getFormOptions().isConfirmClear()) {
                VaadinUtils.showConfirmDialog(getMessageService(), message("ocs.confirm.clear"),
                        new Runnable() {

                            @Override
                            public void run() {
                                clear();
                                search();
                            }
                        });
            } else {
                clear();
                search();
            }
        } else if (event.getButton() == toggleButton) {
            toggle(!wrapperPanel.isVisible());
        }
    }

    /**
     * Clears the search filters
     */
    protected void clear() {
        currentFilters.clear();
        currentFilters.addAll(getAdditionalFilters());
    }

    /**
     * Creates buttons and adds them to the button bar
     * 
     * @param buttonBar
     */
    protected abstract void constructButtonBar(Layout buttonBar);

    /**
     * Constructs the "clear" button
     * 
     * @return
     */
    protected Button constructClearButton() {
        clearButton = new Button(message("ocs.clear"));
        clearButton.setImmediate(true);
        clearButton.addClickListener(this);
        clearButton.setVisible(!getFormOptions().isHideClearButton());
        return clearButton;
    }

    /**
     * Constructs the layout that holds all the filter components
     * 
     * @return
     */
    protected abstract Layout constructFilterLayout();

    /**
     * Constructs the "search" button
     * 
     * @return
     */
    protected Button constructSearchButton() {
        searchButton = new Button(message("ocs.search"));
        searchButton.setImmediate(true);
        searchButton.addClickListener(this);
        return searchButton;
    }

    /**
     * Constructs the "toggle" button
     * 
     * @return
     */
    protected Button constructToggleButton() {
        toggleButton = new Button(message("ocs.hide"));
        toggleButton.addClickListener(this);
        toggleButton.setVisible(getFormOptions().isShowToggleButton());
        return toggleButton;
    }

    public List<Filter> getAdditionalFilters() {
        return additionalFilters;
    }

    public Button getClearButton() {
        return clearButton;
    }

    public Filter getCompositeFilter() {
        return compositeFilter;
    }

    public List<Filter> getCurrentFilters() {
        return currentFilters;
    }

    public ModelBasedFieldFactory<T> getFieldFactory() {
        return fieldFactory;
    }

    public Layout getFilterLayout() {
        return filterLayout;
    }

    public Searchable getSearchable() {
        return searchable;
    }

    public Button getSearchButton() {
        return searchButton;
    }

    /**
     * Responds to a filter change
     */
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
     * Perform any actions necessary after the layout has been build
     */
    protected void postProcessLayout() {

    }

    /**
     * Trigger the actual search
     */
    public void search() {
        if (!currentFilters.isEmpty()) {
            compositeFilter = new And(currentFilters.toArray(new Filter[0]));
        } else {
            // search without any filters
            compositeFilter = null;
        }
        searchable.search(compositeFilter);
    }

    public void setCompositeFilter(Filter compositeFilter) {
        this.compositeFilter = compositeFilter;
        searchable.search(compositeFilter);
    }

    /**
     * Toggles the visibility of the search form
     * 
     * @param show
     *            whether to show or hide the form
     */
    protected void toggle(boolean show) {
        if (!show) {
            toggleButton.setCaption(message("ocs.show.search.fields"));
        } else {
            toggleButton.setCaption(message("ocs.hide.search.fields"));
        }
        wrapperPanel.setVisible(show);
        afterSearchFieldToggle(wrapperPanel.isVisible());
    }
}
