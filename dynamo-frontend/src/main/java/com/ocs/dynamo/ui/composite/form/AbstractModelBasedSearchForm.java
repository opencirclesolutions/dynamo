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
import java.util.function.Function;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.exception.OCSValidationException;
import com.ocs.dynamo.filter.AndPredicate;
import com.ocs.dynamo.filter.OrPredicate;
import com.ocs.dynamo.filter.PredicateUtils;
import com.ocs.dynamo.filter.PropertyPredicate;
import com.ocs.dynamo.filter.listener.FilterChangeEvent;
import com.ocs.dynamo.filter.listener.FilterListener;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.Searchable;
import com.ocs.dynamo.ui.UIHelper;
import com.ocs.dynamo.ui.component.CustomFieldContext;
import com.ocs.dynamo.ui.component.DefaultFlexLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.Getter;
import lombok.Setter;

/**
 * An abstract model search form that servers as the basis for other model based
 * search forms
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the ID of the entity
 * @param <T>  the type of the entity
 */
public abstract class AbstractModelBasedSearchForm<ID extends Serializable, T extends AbstractEntity<ID>>
		extends AbstractModelBasedForm<ID, T>
		implements FilterListener<T>, Refreshable, ComponentEventListener<ClickEvent<Button>> {

	private static final long serialVersionUID = 2146875385041665280L;

	@Getter
	@Setter
	private boolean advancedSearchMode = false;

	/**
	 * Callback method that is executed after advanced mode is toggle on or off
	 */
	@Getter
	@Setter
	private Runnable afterAdvancedModeToggled;

	/**
	 * Callback method that is executed after the clear button is clicked
	 */
	@Getter
	@Setter
	public Runnable afterClear;

	/**
	 * Additional code to execute after the clear button is pressed
	 */
	@Getter
	@Setter
	private Consumer<ClickEvent<Button>> afterClearConsumer;

	/**
	 * Callback method that is executed after the search form is toggled (between
	 * shown and hidden)
	 */
	@Getter
	@Setter
	public Consumer<Boolean> afterSearchFormToggled;

	/**
	 * Callback method that is executed after a search has been performed
	 */
	@Getter
	@Setter
	public Runnable afterSearchPerformed;

	@Getter
	private FlexLayout buttonBar;

	@Getter
	private Button clearButton;

	/**
	 * The list of currently active search filters
	 */
	@Getter
	private List<SerializablePredicate<T>> currentFilters = new ArrayList<>();

	/**
	 * Any filters that will always be applied to any search query (use these to
	 * restrict the result set beforehand)
	 */
	@Getter
	@Setter
	private List<SerializablePredicate<T>> defaultFilters = new ArrayList<>();

	/**
	 * Field factory singleton for constructing fields
	 */
	@Getter
	private FieldFactory fieldFactory = FieldFactory.getInstance();

	/**
	 * The layout that holds the various filters
	 */
	@Getter
	private HasComponents filterLayout;

	/**
	 * The main layout (constructed only once)
	 */
	private VerticalLayout main;

	/**
	 * Callback method that is executed after the button bar has been created
	 */
	@Getter
	@Setter
	private Consumer<FlexLayout> postProcessButtonBar;

	/**
	 * Callback method that is executed after the entire layout has been created
	 */
	@Getter
	@Setter
	private Consumer<VerticalLayout> afterLayoutBuilt;

//	/**
//	 * Callback method that is executed before the search fields are added to the
//	 * search layout
//	 */
//	@Getter
//	@Setter
//	private Consumer<VerticalLayout> preProcessLayout;

	/**
	 * The component on which the search will be carried out after the user clicks
	 * the "search" button
	 */
	@Getter
	@Setter
	private Searchable<T> searchable;

	@Getter
	private Button searchAnyButton;

	@Getter
	private Button searchButton;

	@Getter
	private Button toggleAdvancedModeButton;

	private Button toggleButton;

	/**
	 * Callback method that is executed to validate the search form before searching
	 */
	@Getter
	@Setter
	private Runnable validateBeforeSearch;

	private VerticalLayout wrapperPanel;

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
		this.advancedSearchMode = formOptions.isStartInAdvancedMode();
		this.defaultFilters = defaultFilters == null ? new ArrayList<>() : defaultFilters;
		this.currentFilters.addAll(this.defaultFilters);
		this.searchable = searchable;
	}

	@Override
	public void build() {
		if (main == null) {
			main = new DefaultVerticalLayout(false, true);
			main.addClassName(DynamoConstants.CSS_SEARCH_FORM_LAYOUT);
//			if (preProcessLayout != null) {
//				preProcessLayout.accept(main);
//			}

			filterLayout = constructFilterLayout();
			if (((Component) filterLayout).isVisible()) {

				// add a wrapper for adding an action handlers
				wrapperPanel = new DefaultVerticalLayout(false, false);
				wrapperPanel.addClassName(DynamoConstants.CSS_SEARCH_FORM_WRAPPER);
				main.add(wrapperPanel);

				wrapperPanel.add((Component) filterLayout);

				// create the button bar
				buttonBar = new DefaultFlexLayout();
				main.add(buttonBar);
				constructButtonBar(buttonBar);

				if (postProcessButtonBar != null) {
					postProcessButtonBar.accept(buttonBar);
				}

				searchButton.setEnabled(isSearchAllowed());
			}

			if (afterLayoutBuilt != null) {
				afterLayoutBuilt.accept(main);
			}
			add(main);

			if (getFormOptions().isPreserveSearchTerms() && !getComponentContext().isPopup()) {
				restoreSearchValues();
			}
		}
	}

	/**
	 * Clears any search filters (and re-applies the default filters afterwards)
	 */
	public void clear() {
		UIHelper helper = ServiceLocatorFactory.getServiceLocator().getService(UIHelper.class);
		if (helper != null) {
			helper.clearSearchTerms();
			helper.clearSortOrders();
		}
		currentFilters.clear();
		currentFilters.addAll(getDefaultFilters());
	}

	/**
	 * Constructs the "toggle advanced search mode" button
	 * 
	 * @return
	 */
	protected final Button constructAdvancedSearchModeButton() {
		toggleAdvancedModeButton = new Button(
				advancedSearchMode ? message("ocs.to.simple.search.mode") : message("ocs.to.advanced.search.mode"));
		toggleAdvancedModeButton.setIcon(VaadinIcon.CLIPBOARD.create());
		toggleAdvancedModeButton.addClickListener(this);
		toggleAdvancedModeButton
				.setVisible(getFormOptions().isEnableAdvancedSearchMode() && supportsAdvancedSearchMode());
		return toggleAdvancedModeButton;
	}

	/**
	 * Creates buttons and adds them to the button bar
	 *
	 * @param buttonBar the button bar
	 */
	protected abstract void constructButtonBar(FlexLayout buttonBar);

	/**
	 * Constructs the "clear" button
	 * 
	 * @return
	 */
	protected Button constructClearButton() {
		clearButton = new Button(message("ocs.clear"));
		clearButton.setIcon(VaadinIcon.ERASER.create());
		clearButton.addClickListener(this);
		clearButton.setVisible(getFormOptions().isShowClearButton());
		return clearButton;
	}

	/**
	 * Constructs the layout that holds all the filter components
	 *
	 * @return
	 */
	protected abstract HasComponents constructFilterLayout();

	/**
	 * Construct the "search any" button which allows the user to search on entities
	 * that match any (rather than all) of the search criteria
	 * 
	 * @return
	 */
	protected final Button constructSearchAnyButton() {
		searchAnyButton = new Button(message("ocs.search.any"));
		searchAnyButton.setIcon(VaadinIcon.SEARCH.create());
		searchAnyButton.setVisible(getFormOptions().isShowSearchAnyButton());
		searchAnyButton.addClickListener(this);
		return searchAnyButton;
	}

	/**
	 * Constructs the Search button
	 * 
	 * @return
	 */
	protected final Button constructSearchButton() {
		searchButton = new Button(message("ocs.search"));
		searchButton.setIcon(VaadinIcon.SEARCH.create());
		searchButton.addClickListener(this);
		searchButton.addClickShortcut(Key.ENTER);
		return searchButton;
	}

	/**
	 * Constructs the "toggle" button which shows/hides the search form
	 * 
	 * @return
	 */
	protected final Button constructToggleButton() {
		toggleButton = new Button(message("ocs.hide"));
		toggleButton.setIcon(VaadinIcon.ARROWS.create());
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
			SerializablePredicate<T> currentFilter = matchAny
					? new OrPredicate<>(currentFilters.toArray(new SerializablePredicate[0]))
					: new AndPredicate<>(currentFilters.toArray(new SerializablePredicate[0]));

			if (defaultFilter != null) {
				return new AndPredicate<>(defaultFilter, currentFilter);
			}
			return currentFilter;
		}
		return null;
	}

	/**
	 * Looks up and creates a custom field for the provided attribute model
	 * 
	 * @param entityModel    the entity model of the entity
	 * @param attributeModel the attribute model of the attribute
	 * @return
	 */
	protected Component findCustomComponent(EntityModel<?> entityModel, AttributeModel attributeModel) {
		Function<CustomFieldContext, Component> customFieldCreator = getComponentContext()
				.getCustomFieldCreator(attributeModel.getPath());
		if (customFieldCreator != null) {
			return customFieldCreator.apply(CustomFieldContext.builder().entityModel(entityModel)
					.attributeModel(attributeModel).searchMode(true).build());
		}
		return null;
	}

	/**
	 *
	 * @return the number of currently active search filters
	 */
	public int getFilterCount() {
		return currentFilters.size();
	}

	/**
	 * Checks whether a filter has been set for a certain attribute
	 *
	 * @param path the path of the attribute
	 * @return
	 */
	public boolean isFilterSet(String path) {
		return currentFilters.stream().filter(p -> p instanceof PropertyPredicate).map(p -> (PropertyPredicate<T>) p)
				.anyMatch(p -> p.appliesToProperty(path));
	}

	/**
	 * Searching is allowed when there are no required attributes or all required
	 * attributes are in the composite filter.
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean isSearchAllowed() {

		// Get the required attributes.
		List<AttributeModel> requiredAttributes = getEntityModel().getRequiredForSearchingAttributeModels();
		if (requiredAttributes.isEmpty()) {
			return true;
		}

		if (currentFilters.isEmpty()) {
			return false;
		}

		// check if there is at least one predicate for every required attribute
		for (AttributeModel ram : requiredAttributes) {
			SerializablePredicate<T> predicate = PredicateUtils.extractPredicate(
					new AndPredicate<>(currentFilters.toArray(new SerializablePredicate[0])), ram.getPath());
			if (predicate == null) {
				return false;
			}
		}

		return true;
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		build();
	}

	/**
	 * Respond to button clicks
	 */
	@Override
	public void onComponentEvent(ClickEvent<Button> event) {
		if (event.getSource() == searchButton) {
			search();
		} else if (event.getSource() == searchAnyButton) {
			searchAny();
		} else if (event.getSource() == clearButton) {
			if (getFormOptions().isConfirmClear()) {
				VaadinUtils.showConfirmDialog(message("ocs.confirm.clear"), () -> {
					clear();
					handleAfterClear(event);
				});
			} else {
				clear();
				if (getFormOptions().isSearchImmediately()) {
					search(true);
				}
			}
		} else if (event.getSource() == toggleButton) {
			toggle(!wrapperPanel.isVisible());
		} else if (event.getSource() == toggleAdvancedModeButton) {
			toggleAdvancedMode();
			if (afterAdvancedModeToggled != null) {
				afterAdvancedModeToggled.run();
			}
		}
	}

	/**
	 * Execute the required logic after a form clear
	 * 
	 * @param event
	 */
	private void handleAfterClear(ClickEvent<Button> event) {
		if (afterClearConsumer != null) {
			afterClearConsumer.accept(event);
		}

		if (getFormOptions().isSearchImmediately()) {
			search(true);
		}
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

//	/**
//	 * Pre-process the layout - this method is called directly after the main layout
//	 * has been created
//	 *
//	 * @param main the layout
//	 */
//	protected void preProcessLayout(VerticalLayout layout) {
//		// override in subclass
//	}

	/**
	 * Restores any previously cached search values
	 */
	protected abstract void restoreSearchValues();

	/**
	 * Performs a search that matches all of the search criteria
	 * 
	 * @return whether the search was successful
	 */
	public boolean search() {
		return search(false, false);
	}

	/**
	 * Carries out a search using default search AND behaviour.
	 *
	 * @param skipValidation whether to skip validation before searching
	 *
	 * @return whether the search was successful
	 */
	public boolean search(boolean skipValidation) {
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
		if (!skipValidation && !isSearchAllowed()) {
			return false;
		}

		if (searchable != null) {
			if (!skipValidation) {
				try {
					if (validateBeforeSearch != null) {
						validateBeforeSearch.run();
					}
				} catch (OCSValidationException ex) {
					showErrorNotification(ex.getErrors().get(0));
					return false;
				}
			}

			searchable.search(extractFilter(matchAny));

			// store search filters for later user
			storeSearchFilters();

			if (!skipValidation) {
				if (afterSearchPerformed != null) {
					afterSearchPerformed.run();
				}
			}

			return true;
		}
		return false;
	}

	/**
	 * Performs a search that matches any of the search criteria
	 * 
	 * @return whether the search was successful
	 */
	public boolean searchAny() {
		return search(false, true);
	}

	/**
	 * Stores any selected search filters for later use when the screen is reopened
	 */
	protected abstract void storeSearchFilters();

	/**
	 * @return whether advanced search mode is enabled
	 */
	protected abstract boolean supportsAdvancedSearchMode();

	/**
	 * Toggles the visibility of the search form
	 *
	 * @param show whether to show or hide the form
	 */
	protected void toggle(boolean show) {
		if (!show) {
			toggleButton.setText(message("ocs.show.search.fields"));
		} else {
			toggleButton.setText(message("ocs.hide.search.fields"));
		}
		wrapperPanel.setVisible(show);

		if (afterSearchFormToggled != null) {
			afterSearchFormToggled.accept(wrapperPanel.isVisible());
		}
	}

	/**
	 * Toggles the advanced search mode
	 */
	public abstract void toggleAdvancedMode();

}
