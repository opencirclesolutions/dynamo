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
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSValidationException;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.form.AbstractModelBasedSearchForm;
import com.ocs.dynamo.ui.composite.grid.GridWrapper;
import com.ocs.dynamo.ui.provider.QueryType;
import com.ocs.dynamo.utils.FormatUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.Getter;
import lombok.Setter;

/**
 * Base class for layout that support a search form and result grid
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key of the entity
 * @param <T>  the type of the entity
 */
public abstract class AbstractSearchLayout<ID extends Serializable, T extends AbstractEntity<ID>, U>
		extends BaseCollectionLayout<ID, T, U> implements HasSelectedItem<T> {

	private static final long serialVersionUID = 366639924823921266L;

	/**
	 * Code that is executed after the advanced mode is switch on or off
	 */
	@Getter
	@Setter
	private Consumer<Boolean> afterAdvancedModeToggled = b -> {
	};

	/**
	 * Code that is executed after the clear button is pressed
	 */
	@Getter
	@Setter
	private Runnable afterClear = () -> {
	};

	/**
	 * Code that is executed after the search form is shown or hidden
	 */
	@Getter
	@Setter
	private Consumer<Boolean> afterSearchFormToggled = b -> {
	};

	/**
	 * Code that is executed after a search is performed
	 */
	@Getter
	@Setter
	private Runnable afterSearchPerformed = () -> {
	};

	@Getter
	private List<SerializablePredicate<T>> defaultFilters;

	/**
	 * The tab captions to use for the tab sheet when the component is in "advanced
	 * details" mode
	 */
	@Getter
	@Setter
	private String[] detailsModeTabCaptions;

	/**
	 * The code that is executed to create each of the tabs used in advanced details
	 * mode
	 */
	@Getter
	private Map<Integer, BiFunction<FormOptions, Boolean, Component>> detailTabCreators = new HashMap<>();

	@Getter
	private VerticalLayout mainSearchLayout;

	/**
	 * Code that is executed when the edit button is clicked
	 */
	@Getter
	@Setter
	private Runnable onEdit = () -> detailsMode(getSelectedItem());

	/**
	 * Code that is executed when the remove button is clicked
	 */
	@Getter
	@Setter
	private Runnable onRemove = () -> getService().delete(getSelectedItem());

	/**
	 * Code that is carried out after the search button bar has been constructed
	 */
	@Getter
	@Setter
	private Consumer<FlexLayout> postProcessSearchButtonBar;

	/**
	 * Code that is carried out after the search form has been built
	 */
	@Getter
	@Setter
	private Consumer<VerticalLayout> afterSearchFormBuilt;

	@Getter
	private QueryType queryType;

	private AbstractModelBasedSearchForm<ID, T> searchForm;

	private boolean searchLayoutConstructed;

	@Getter
	private VerticalLayout searchResultsLayout;

	@Getter
	private Collection<T> selectedItems;

	/**
	 * Code that is carried out to validate the search form before a search can be
	 * carried out
	 */
	@Getter
	@Setter
	private Runnable validateBeforeSearch;

	/**
	 * Code that is carried out to construct an icon for a tab that is displayed in
	 * the tab layout when in complex details mode
	 */
	@Getter
	@Setter
	private Function<Integer, Icon> tabIconCreator;

	/**
	 * Constructor
	 * 
	 * @param service     the service that is used to query the database
	 * @param entityModel the entity model of the entities to search for
	 * @param queryType   the type of the query
	 * @param formOptions form options that governs which buttons and options to
	 *                    show
	 * @param sortOrder   the default sort order
	 * @param joins       the joins to include in the query
	 */
	protected AbstractSearchLayout(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
			FormOptions formOptions, SortOrder<?> sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, formOptions, sortOrder, joins);
		this.queryType = queryType;
	}

	/**
	 * Registers a lambda functions for creating a details tab that is used when
	 * "complexDetailsMode" is enab
	 * 
	 * @param index
	 * @param creator
	 */
	public void addDetailTabCreator(int index, BiFunction<FormOptions, Boolean, Component> creator) {
		detailTabCreators.put(index, creator);
	}

	public void addManageDetailButtons() {
		// overwrite in subclasses
	}

	/**
	 * TODO: change to lambda?
	 * 
	 * Callback method that fires just before performing a search. Can be used to
	 * perform any actions that are necessary before carrying out a search.
	 * 
	 * @param filter the current search filter
	 * @return the modified search filter. If not null, then this filter will be
	 *         used for the search instead of the current filter
	 */
	protected SerializablePredicate<T> beforeSearchPerformed(SerializablePredicate<T> filter) {
		// overwrite in subclasses
		return null;
	}

	/**
	 * Lazily constructs the screen
	 */
	@Override
	public void build() {
		if (mainSearchLayout == null) {
			mainSearchLayout = new DefaultVerticalLayout(true, false);
			mainSearchLayout.addClassName(DynamoConstants.CSS_MAIN_SEARCH_LAYOUT);

			// if search immediately, construct the search results grid
			if (getFormOptions().isSearchImmediately()) {
				constructSearchLayout();
				searchLayoutConstructed = true;
			}

			mainSearchLayout.add(getSearchForm());
			if (getSearchForm().getClearButton() != null) {
				constructAfterClearListeners();
			}

			searchResultsLayout = new DefaultVerticalLayout(false, false);
			searchResultsLayout.setClassName(DynamoConstants.CSS_SEARCH_RESULTS_LAYOUT);
			mainSearchLayout.add(searchResultsLayout);

			if (getFormOptions().isSearchImmediately()) {
				// immediately construct the search results grid
				searchResultsLayout.add(getGridWrapper());
			} else {
				// do not construct the search results grid yet
				constructLazySearchFunctionality();
			}
			
			// clear currently selected item and update buttons
			if (getSearchForm().getSearchButton() != null) {
				getSearchForm().getSearchButton().addClickListener(e -> {
					setSelectedItem(null);
					checkComponentState(getSelectedItem());
				});
			}

			addManageDetailButtons();

			// callback for adding additional buttons
			if (getPostProcessMainButtonBar() != null) {
				getPostProcessMainButtonBar().accept(getButtonBar());
			}

			mainSearchLayout.add(getButtonBar());

			checkComponentState(null);

			if (getAfterLayoutBuilt() != null) {
				getAfterLayoutBuilt().accept(mainSearchLayout);
			}

			// there is a small chance that the user navigates directly
			// to the detail screen without the search layout having been
			// created before. This check is there to ensure that the
			// search layout is not appended below the detail layout
			if (getComponentCount() == 0) {
				add(mainSearchLayout);
			}
		}
	}

	/**
	 * Sets up the listeners for constructing the search results grid after the 
	 */
	private void constructLazySearchFunctionality() {
		Text noSearchYetLabel = new Text(message("ocs.no.search.yet"));
		searchResultsLayout.add(noSearchYetLabel);

		// click listener that will construct search results grid on demand
		if (getSearchForm().getSearchButton() != null) {
			getSearchForm().getSearchButton().addClickListener(e -> constructLayoutIfNeeded(noSearchYetLabel));
		}
		if (getSearchForm().getSearchAnyButton() != null) {
			getSearchForm().getSearchAnyButton()
					.addClickListener(e -> constructLayoutIfNeeded(noSearchYetLabel));
		}
	}

	private void constructAfterClearListeners() {
		if (!getFormOptions().isSearchImmediately()) {
			// use a consumer since the action might have to be deferred until after the
			// user confirms the clear
			if (getFormOptions().isConfirmClear()) {
				getSearchForm().setAfterClearConsumer(e -> clearIfNotSearchingImmediately());
			} else {
				// clear right away
				getSearchForm().getClearButton().addClickListener(e -> clearIfNotSearchingImmediately());
			}
		} else {
			// clear current selection and update buttons
			if (getFormOptions().isConfirmClear()) {
				getSearchForm().setAfterClearConsumer(e -> {
					setSelectedItem(null);
					checkComponentState(getSelectedItem());
					if (afterClear != null) {
						afterClear.run();
					}
				});
			} else {
				getSearchForm().getClearButton().addClickListener(e -> {
					setSelectedItem(null);
					checkComponentState(getSelectedItem());
					if (afterClear != null) {
						afterClear.run();
					}
				});
			}
		}
	}

	/**
	 * Respond to a click on the Clear button when not in "search immediately" mode
	 */
	private void clearIfNotSearchingImmediately() {
		Text noSearchYetLabel = new Text(message("ocs.no.search.yet"));
		searchResultsLayout.removeAll();
		searchResultsLayout.add(noSearchYetLabel);
		getSearchForm().setSearchable(null);
		searchLayoutConstructed = false;
		setSelectedItem(null);
		checkComponentState(getSelectedItem());
		if (afterClear != null) {
			afterClear.run();
		}
	}

	/**
	 * Constructs the edit button
	 * 
	 * @return
	 */
	protected final Button constructEditButton() {
		Button editButton = new Button(
				(!getFormOptions().isShowEditButton() || !checkEditAllowed()) ? message("ocs.view")
						: message("ocs.edit"));
		editButton.setIcon(VaadinIcon.PENCIL.create());
		editButton.addClickListener(e -> {
			if (getSelectedItem() != null) {
				onEdit.run();
			}
		});
		editButton.setVisible(getFormOptions().isDetailsModeEnabled());
		return editButton;
	}

	public abstract GridWrapper<ID, T, U> constructGridWrapper();

	/**
	 * Constructs a search layout in response to a click on any of the search
	 * buttons
	 * 
	 * @param noSearchYetLabel the label used to indicate that there are no search
	 *                         results yet
	 */
	private void constructLayoutIfNeeded(Text noSearchYetLabel) {
		if (!searchLayoutConstructed) {
			// construct search screen if it is not there yet
			try {
				if (validateBeforeSearch != null) {
					validateBeforeSearch.run();
				}

				searchResultsLayout.removeAll();
				clearGridWrapper();
				constructSearchLayout();
				searchResultsLayout.add(getGridWrapper());
				getSearchForm().setSearchable(getGridWrapper());
				searchResultsLayout.remove(noSearchYetLabel);
				searchLayoutConstructed = true;

				if (afterSearchPerformed != null) {
					afterSearchPerformed.run();
				}
			} catch (OCSValidationException ex) {
				showErrorNotification(ex.getErrors().get(0));
			}
		}
	}

	protected final Button constructRemoveButton() {
		Button removeButton = new RemoveButton(this, message("ocs.remove"), VaadinIcon.TRASH.create(),
				() -> removeEntity(), entity -> FormatUtils.formatEntity(getEntityModel(), entity));
		removeButton.setVisible(checkEditAllowed() && getFormOptions().isShowRemoveButton());
		return removeButton;
	}

	/**
	 * Constructs the search form - implement in subclasses
	 * 
	 * @return the constructed form
	 */
	protected abstract AbstractModelBasedSearchForm<ID, T> constructSearchForm();

	/**
	 * Constructs the search layout
	 */
	public final void constructSearchLayout() {
		// construct grid and set properties
		disableGridSorting();
		getGridWrapper().getGrid().setHeight(getGridHeight());
		getGridWrapper().getGrid()
				.setSelectionMode(getComponentContext().isMultiSelect() ? SelectionMode.MULTI : SelectionMode.SINGLE);

		// add a listener to respond to the selection of an item
		getGridWrapper().getGrid().addSelectionListener(e -> {
			select(getGridWrapper().getGrid().getSelectedItems());
			checkComponentState(getSelectedItem());
		});

		// select item by double clicking on row (disable this inside pop-up
		// windows)
		if (getFormOptions().isDetailsModeEnabled() && getFormOptions().isDoubleClickSelectAllowed()) {
			getGridWrapper().getGrid().addItemDoubleClickListener(event -> {
				select(event.getItem());
				onEdit.run();
			});
		}
	}

	/**
	 * Sets the provided component as the current detail view
	 * 
	 * @param root the root component of the custom detail view
	 */
	protected final void customDetailView(Component root) {
		removeAll();
		add(root);
	}

	/**
	 * Open the screen in edit mode for the provided entity
	 * 
	 * @param entity
	 */
	public final void edit(T entity) {
		setSelectedItem(entity);
		onEdit.run();
	}

	/**
	 * 
	 * @return the total number of configured filters
	 */
	public int getFilterCount() {
		return getSearchForm().getFilterCount();
	}

	/**
	 * Returns the search form (lazily constructing it when needed)
	 * 
	 * @return
	 */
	public AbstractModelBasedSearchForm<ID, T> getSearchForm() {
		if (searchForm == null) {
			searchForm = constructSearchForm();
		}
		return searchForm;
	}

	protected void initSearchForm(AbstractModelBasedSearchForm<ID, T> searchForm) {
		searchForm.setComponentContext(getComponentContext());
		searchForm.setAfterSearchPerformed(getAfterSearchPerformed());
		searchForm.setAfterSearchFormToggled(getAfterSearchFormToggled());
		searchForm.setValidateBeforeSearch(getValidateBeforeSearch());
		searchForm.setPostProcessButtonBar(getPostProcessSearchButtonBar());
		searchForm.setAfterLayoutBuilt(getAfterSearchFormBuilt());
		searchForm.setAfterAdvancedModeToggled(getAfterAdvancedModeToggled());
	}

	/**
	 * Checks if a filter is set for a certain attribute
	 * 
	 * @param path the path to the attribute
	 * @return <code>true</code> if a filter for the specified attribute has been
	 *         set and <code>false</code> otherwise
	 */
	public boolean isFilterSet(String path) {
		return getSearchForm().isFilterSet(path);
	}

	/**
	 * Checks whether the layout is currently in search mode
	 *
	 * @return
	 */
	public boolean isInSearchMode() {
		return Objects.equals(getComponentAt(0), mainSearchLayout);
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		build();
	}

	/**
	 * Refreshes all lookup components but otherwise does not update the state of
	 * the screen
	 */
	@Override
	public void refresh() {
		getSearchForm().refresh();
	}

	/**
	 * Reloads the entire component, reverting to search mode and clearing the
	 * search form
	 */
	@Override
	public void reload() {
		removeAll();
		add(mainSearchLayout);
		getSearchForm().clear();
		search();
	}

	/**
	 * Reloads the details view only
	 */
	public void reloadDetails() {
		this.setSelectedItem(getService().fetchById(this.getSelectedItem().getId(), getDetailJoins()));
		detailsMode(getSelectedItem());
	}

	/**
	 * Performs the actual delete action
	 */
	protected final void removeEntity() {
		onRemove.run();
		// refresh the results so that the deleted item is no longer
		// there
		searchForm.search(true);
		getGridWrapper().getGrid().deselectAll();
		setSelectedItem(null);
	}

	/**
	 * Perform the actual search
	 */
	public void search() {
		boolean searched = searchForm.search();
		if (searched) {
			getGridWrapper().getGrid().deselectAll();
			setSelectedItem(null);
		}
	}

	/**
	 * Puts the screen in search mode (does not reset the search form)
	 */
	public void searchMode() {
		removeAll();
		add(mainSearchLayout);

		getSearchForm().refresh();
		search();
	}

	/**
	 * Select one or more items
	 * 
	 * @param selectedItems the item or items to select
	 */
	@SuppressWarnings("unchecked")
	public void select(Object selectedItems) {
		if (selectedItems != null) {
			if (selectedItems instanceof Collection<?>) {
				// the lazy query container returns an array of IDs of the
				// selected items

				Collection<?> col = (Collection<?>) selectedItems;
				if (col.size() == 1) {
					T t = (T) col.iterator().next();
					setSelectedItem(getService().fetchById(t.getId(), getDetailJoins()));
					this.selectedItems = new ArrayList<>(List.of(getSelectedItem()));
				} else if (col.size() > 1) {
					// deal with the selection of multiple items
					List<ID> ids = new ArrayList<>();
					for (Object c : col) {
						ids.add(((T) c).getId());
					}
					this.selectedItems = getService().fetchByIds(ids, getDetailJoins());
				}
			} else {
				// single item has been selected
				T t = (T) selectedItems;
				setSelectedItem(getService().fetchById(t.getId(), getDetailJoins()));
			}
		} else {
			setSelectedItem(null);
		}
	}

	/**
	 * Sets the default filters that are always applied to a search query (even
	 * after all search fields have been cleared)
	 *
	 * @param defaultFilters the default filters
	 */
	public void setDefaultFilters(List<SerializablePredicate<T>> defaultFilters) {
		this.defaultFilters = defaultFilters;
		if (searchForm != null) {
			searchForm.setDefaultFilters(defaultFilters);
		}
	}

	/**
	 * Sets a predefined search value
	 * 
	 * @param propertyId the name of the property for which to set a value
	 * @param value      the value
	 */
	public abstract void setSearchValue(String propertyId, Object value);

	/**
	 * Sets a predefined search value (upper and lower bound)
	 * 
	 * @param propertyId the name of the property for which to set a value
	 * @param value      the value (lower bound)
	 * @param auxValue   the auxiliary value (upper bound)
	 */
	public abstract void setSearchValue(String propertyId, Object value, Object auxValue);
}
