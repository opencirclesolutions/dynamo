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
import java.util.function.Consumer;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.exception.OCSValidationException;
import com.ocs.dynamo.filter.AndPredicate;
import com.ocs.dynamo.filter.OrPredicate;
import com.ocs.dynamo.filter.PropertyPredicate;
import com.ocs.dynamo.filter.listener.FilterChangeEvent;
import com.ocs.dynamo.filter.listener.FilterListener;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.Searchable;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ShortcutAction;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * An abstract model search form that servers as the basis for other model based
 * search forms
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the ID of the entity
 * @param <T> the type of the entity
 */
public abstract class AbstractModelBasedSearchForm<ID extends Serializable, T extends AbstractEntity<ID>>
        extends AbstractModelBasedForm<ID, T> implements FilterListener<T>, Button.ClickListener, Refreshable {

    private static final long serialVersionUID = 2146875385041665280L;

    /**
     * Any filters that will always be applied to any search query (use these to
     * restrict the result set beforehand)
     */
    private List<SerializablePredicate<T>> defaultFilters = new ArrayList<>();

    /**
     * Button to clear the search form
     */
    private Button clearButton;

    /**
     * The list of currently active search filters
     */
    private List<SerializablePredicate<T>> currentFilters = new ArrayList<>();

    /**
     * The layout that holds the various filters
     */
    private Layout filterLayout;

    /**
     * The component on which the search will be carried out after the user presses
     * the "search" button
     */
    private Searchable<T> searchable;

    /**
     * The search button
     */
    private Button searchButton;

    /**
     * The toggle button (hides/shows the search form)
     */
    private Button toggleButton;

    /**
     * Search any button
     */
    private Button searchAnyButton;

    /**
     * The panel that wraps around the filter form
     */
    private Panel wrapperPanel;

    /**
     * The button bar
     */
    private HorizontalLayout buttonBar;

    /**
     * The main layout (constructed only once)
     */
    private VerticalLayout main;

    /**
     * Additional code to execute after the clear button is pressed
     */
    private Consumer<ClickEvent> afterClearConsumer;

    /**
     * Field factory singleton for constructing fields
     */
    private FieldFactory fieldFactory = FieldFactory.getInstance();

    /**
     * Constructor
     *
     * @param searchable
     * @param entityModel
     * @param formOptions
     * @param defaultFilters
     * @param fieldFilters
     */
    public AbstractModelBasedSearchForm(Searchable<T> searchable, EntityModel<T> entityModel, FormOptions formOptions,
            List<SerializablePredicate<T>> defaultFilters, Map<String, SerializablePredicate<?>> fieldFilters) {
        super(formOptions, fieldFilters, entityModel);
        this.defaultFilters = defaultFilters == null ? new ArrayList<>() : defaultFilters;
        this.currentFilters.addAll(this.defaultFilters);
        this.searchable = searchable;
    }

    /**
     * Callback method that is called when the user toggles the visibility of the
     * search form
     *
     * @param visible indicates if the search fields are visible now
     */
    protected void afterSearchFieldToggle(boolean visible) {
        // override in subclasses
    }

    /**
     * Callback method that is called after a successful search has been performed
     */
    protected void afterSearchPerformed() {
        // override in subclasses
    }

    @Override
    public void attach() {
        super.attach();
        build();
    }

    @Override
    public void build() {
        if (main == null) {
            main = new DefaultVerticalLayout(false, true);
            preProcessLayout(main);

            // create the search form
            filterLayout = constructFilterLayout();
            if (filterLayout.isVisible()) {

                // add a wrapper for adding an action handlers
                wrapperPanel = new Panel();
                main.addComponent(wrapperPanel);

                wrapperPanel.setContent(filterLayout);

                // action handlers for carrying out a search after an Enter press
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
                buttonBar = new DefaultHorizontalLayout();
                main.addComponent(buttonBar);
                constructButtonBar(buttonBar);
                // add custom buttons
                postProcessButtonBar(buttonBar);

                searchButton.setEnabled(isSearchAllowed());
            }

            // add any custom functionality
            postProcessLayout(main);
            setCompositionRoot(main);
        }
    }

    /**
     * Responds to a click on any of the standard buttons in the search bar
     * 
     * @param event the event to respond to
     */
    @Override
    public void buttonClick(ClickEvent event) {
        if (event.getButton() == searchButton) {
            search();
        } else if (event.getButton() == searchAnyButton) {
            searchAny();
        } else if (event.getButton() == clearButton) {
            if (getFormOptions().isConfirmClear()) {
                VaadinUtils.showConfirmDialog(getMessageService(), message("ocs.confirm.clear"), () -> {
                    clear();

                    if (afterClearConsumer != null) {
                        afterClearConsumer.accept(event);
                    }

                    if (getFormOptions().isSearchImmediately()) {
                        search(true);
                    }
                });
            } else {
                clear();
                if (getFormOptions().isSearchImmediately()) {
                    search(true);
                }
            }
        } else if (event.getButton() == toggleButton) {
            toggle(!wrapperPanel.isVisible());
        }
    }

    /**
     * Clears any search filters (and re-applies the default filters afterwards)
     */
    public void clear() {
        currentFilters.clear();
        currentFilters.addAll(getDefaultFilters());
    }

    /**
     * Creates buttons and adds them to the button bar
     *
     * @param buttonBar the button bar
     */
    protected abstract void constructButtonBar(Layout buttonBar);

    /**
     * Constructs the "clear" button
     * 
     * @return
     */
    protected Button constructClearButton() {
        clearButton = new Button(message("ocs.clear"));
        clearButton.setIcon(VaadinIcons.ERASER);
        clearButton.addClickListener(this);
        clearButton.setVisible(!getFormOptions().isHideClearButton());
        return clearButton;
    }

    /**
     * Creates a custom field - override in subclasses if needed
     *
     * @param entityModel    the entity model of the entity to search for
     * @param attributeModel the attribute model the attribute model of the property
     *                       that is bound to the field
     * @return
     */
    protected AbstractComponent constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel) {
        return null;
    }

    /**
     * Constructs the layout that holds all the filter components
     *
     * @return
     */
    protected abstract Layout constructFilterLayout();

    /**
     * Construct the "search any" button which allows the user to search on entities
     * that match any (rather than all) of the search criteria
     * 
     * @return
     */
    protected final Button constructSearchAnyButton() {
        searchAnyButton = new Button(message("ocs.search.any"));
        searchAnyButton.setIcon(VaadinIcons.SEARCH);
        searchAnyButton.setVisible(getFormOptions().isShowSearchAnyButton());
        searchAnyButton.addClickListener(this);
        return searchAnyButton;
    }

    /**
     * Constructs the "search" button
     * 
     * @return
     */
    protected final Button constructSearchButton() {
        searchButton = new Button(message("ocs.search"));
        searchButton.setIcon(VaadinIcons.SEARCH);
        searchButton.addClickListener(this);
        return searchButton;
    }

    /**
     * Constructs the "toggle" button which shows/hides the search form
     * 
     * @return
     */
    protected final Button constructToggleButton() {
        toggleButton = new Button(message("ocs.hide"));
        toggleButton.setIcon(VaadinIcons.ARROWS);
        toggleButton.addClickListener(this);
        toggleButton.setVisible(getFormOptions().isShowToggleButton());
        return toggleButton;
    }

    public SerializablePredicate<T> extractFilter() {
        return extractFilter(false);
    }

    @SuppressWarnings("unchecked")
    private SerializablePredicate<T> extractFilter(boolean matchAny) {
        if (!currentFilters.isEmpty()) {
            SerializablePredicate<T> defaultFilter = null;
            if (!defaultFilters.isEmpty()) {
                defaultFilter = new AndPredicate<>(defaultFilters.toArray(new SerializablePredicate[0]));
            }
            List<SerializablePredicate<T>> customFilters = new ArrayList<>(currentFilters);
            customFilters.removeAll(defaultFilters);
            if (currentFilters.isEmpty()) {
                return defaultFilter;
            }
            SerializablePredicate<T> currentFilter = matchAny ? new OrPredicate<>(currentFilters.toArray(new SerializablePredicate[0]))
                    : new AndPredicate<>(currentFilters.toArray(new SerializablePredicate[0]));
            if (defaultFilter != null) {
                return new AndPredicate<>(defaultFilter, currentFilter);
            }
            return currentFilter;
        }
        return null;
    }

    public Consumer<ClickEvent> getAfterClearConsumer() {
        return afterClearConsumer;
    }

    public HorizontalLayout getButtonBar() {
        return buttonBar;
    }

    public Button getClearButton() {
        return clearButton;
    }

    public List<SerializablePredicate<T>> getCurrentFilters() {
        return currentFilters;
    }

    public List<SerializablePredicate<T>> getDefaultFilters() {
        return defaultFilters;
    }

    public FieldFactory getFieldFactory() {
        return fieldFactory;
    }

    /**
     *
     * @return the number of filters
     */
    public int getFilterCount() {
        return currentFilters.size();
    }

    public Layout getFilterLayout() {
        return filterLayout;
    }

    public Searchable<T> getSearchable() {
        return searchable;
    }

    public Button getSearchAnyButton() {
        return searchAnyButton;
    }

    public Button getSearchButton() {
        return searchButton;
    }

    /**
     * Checks whether a filter has been set for a certain attribute
     *
     * @param path the path of the attribute
     * @return
     */
    public boolean isFilterSet(String path) {
        for (SerializablePredicate<T> filter : currentFilters) {
            if (filter instanceof PropertyPredicate && ((PropertyPredicate<T>) filter).appliesToProperty(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Searching is allowed when there are no required attributes or all required
     * attributes are in the composite filter.
     *
     * @return
     */
    public boolean isSearchAllowed() {

        // Get the required attributes.
        List<AttributeModel> requiredAttributes = getEntityModel().getRequiredForSearchingAttributeModels();
        if (requiredAttributes.isEmpty()) {
            return true;
        }

        if (currentFilters.isEmpty()) {
            return false;
        }

        // check if required properties have been set
        int matches = (int) requiredAttributes.stream().filter(am -> currentFilters.stream().filter(f -> f instanceof PropertyPredicate)
                .map(f -> (PropertyPredicate<T>) f).anyMatch(f -> f.appliesToProperty(am.getPath()))).count();
        return matches == requiredAttributes.size();
    }

    /**
     * Responds to a filter change
     */
    @Override
    public void onFilterChange(FilterChangeEvent<T> event) {
        if (event.getOldFilter() != null) {
            currentFilters.remove(event.getOldFilter());
        }

        // exclude filter for attributes that are only used for cascading search
        AttributeModel am = getEntityModel().getAttributeModel(event.getPropertyId());
        if (event.getNewFilter() != null && !am.isIgnoreInSearchFilter()) {
            currentFilters.add(event.getNewFilter());
        }
        searchButton.setEnabled(isSearchAllowed());
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
     *
     * @param main the layout
     */
    protected void postProcessLayout(VerticalLayout layout) {
        // override in subclass
    }

    /**
     * Pre-process the layout - this method is called directly after the main layout
     * has been created
     *
     * @param main the layout
     */
    protected void preProcessLayout(VerticalLayout layout) {
        // override in subclass
    }

    /**
     * Performs a search that matches all of the search criteria
     * 
     * @return
     */
    public boolean search() {
        return search(false, false);
    }

    /**
     * Carries out a search using default search AND behaviour.
     *
     * @param skipValidation whether to skip validation before searching
     *
     * @return
     */
    private boolean search(boolean skipValidation) {
        return search(skipValidation, false);
    }

    /**
     * Carries out a search
     *
     * @param skipValidation whether to skip validation before searching
     * @param matchAny       whether the search is an 'Or' search or an 'And'
     *                       search. Where in the former all results matching any
     *                       predicate are returned and in the latter case all
     *                       results matching all predicates are returned.
     *
     * @return
     */
    private boolean search(boolean skipValidation, boolean matchAny) {
        if (!isSearchAllowed()) {
            return false;
        }

        if (searchable != null) {
            if (!skipValidation) {
                try {
                    validateBeforeSearch();
                } catch (OCSValidationException ex) {
                    showNotifification(ex.getErrors().get(0), Notification.Type.ERROR_MESSAGE);
                    return false;
                }
            }

            searchable.search(extractFilter(matchAny));
            if (!skipValidation) {
                afterSearchPerformed();
            }

            return true;
        }
        return false;
    }

    /**
     * Performs a search that matches any of the search criteria
     * 
     * @return
     */
    public boolean searchAny() {
        return search(false, true);
    }

    /**
     * Sets the consumer that will be carried out after the Clear button is clicked
     * 
     * @param afterClearConsumer
     */
    public void setAfterClearConsumer(Consumer<ClickEvent> afterClearConsumer) {
        this.afterClearConsumer = afterClearConsumer;
    }

    /**
     * Sets the default filters that will be added to every quer
     * 
     * @param defaultFilters the default filters
     */
    public void setDefaultFilters(List<SerializablePredicate<T>> defaultFilters) {
        this.defaultFilters = defaultFilters;
    }

    /**
     * Sets the UI component on which the search will be carried out
     *
     * @param searchable the UI component on which the search will be carried out
     */
    public void setSearchable(Searchable<T> searchable) {
        this.searchable = searchable;
    }

    /**
     * Toggles the visibility of the search form
     *
     * @param show whether to show or hide the form
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

    /**
     * Callback method that is called before a search is carried out. The developer
     * can use this to carry out additional validation before searching. When this
     * method returns an OCSValidationException the search process will be aborted
     * and a message will be shown
     */
    protected void validateBeforeSearch() {
        // overwrite in subclass
    }

}
