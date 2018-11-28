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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.Reloadable;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.grid.BaseGridWrapper;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;

/**
 * A base class for a composite layout that displays a collection of data
 * (rather than an single object)
 * 
 * @author bas.rutten
 * @param <ID> the type of the primary key
 * @param <T> the type of the entity
 */
public abstract class BaseCollectionLayout<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseServiceCustomComponent<ID, T> implements Reloadable, Refreshable {

	private static final long serialVersionUID = -2864711994829582000L;

	/**
	 * The default page length
	 */
	private static final int PAGE_LENGTH = 15;

	/**
	 * The main button bar that appears below the search results grid
	 */
	private HorizontalLayout buttonBar = new DefaultHorizontalLayout();

	// the relations to fetch when displaying a single entity
	private FetchJoinInformation[] detailJoins;

	// the property used to determine when to draw a divider row
	private String dividerProperty;

	// filters to apply to individual search fields
	private Map<String, SerializablePredicate<?>> fieldFilters = new HashMap<>();

	// the joins to use when fetching data
	private FetchJoinInformation[] joins;

	// maximum number of results returned by search
	private Integer maxResults;

	// whether the selection of multiple values is allowed
	private boolean multiSelect = false;

	// the page length (number of rows that is displayed in the grid)
	private int pageLength = PAGE_LENGTH;

	// the value from the previous row used when drawing divider rows
	private Object previousDividerValue;

	// the currently selected item
	private T selectedItem;

	// whether the grid can manually be sorted
	private boolean sortEnabled = true;

	// the sort orders
	private List<SortOrder<?>> sortOrders = new ArrayList<>();

	/**
	 * The grid wrapper
	 */
	private BaseGridWrapper<ID, T> gridWrapper;

	/**
	 * Constructor
	 * 
	 * @param service     the service
	 * @param entityModel the entity model
	 * @param formOptions the form options
	 * @param sortOrder   the sort order
	 * @param joins       the joins to use when fetching data
	 */
	public BaseCollectionLayout(BaseService<ID, T> service, EntityModel<T> entityModel, FormOptions formOptions,
			SortOrder<?> sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, formOptions);
		this.joins = joins;
		if (sortOrder != null) {
			sortOrders.add(sortOrder);
		}
	}

	/**
	 * Adds an additional sort order
	 * 
	 * @param sortOrder the sort order to add
	 */
	public final void addSortOrder(SortOrder<?> sortOrder) {
		this.sortOrders.add(sortOrder);
	}

	/**
	 * Method that is called after the setEntity method is called. Can be used to
	 * fetch additional data if required. This method is called before the
	 * "afterDetailSelected" method is called
	 * 
	 * @param entity the entity
	 */
	protected void afterEntitySet(T entity) {
		// override in subclass
	}

	/**
	 * Callback method that is called after a tab has been selected in the tab sheet
	 * that is used in a detail view when the attribute group mode has been set to
	 * TABSHEET
	 * 
	 * @param tabIndex the zero-based index of the selected tab
	 */
	protected void afterTabSelected(int tabIndex) {
		// overwrite in subclasses
	}

	/**
	 * Removes all sort orders
	 */
	public void clearSortOrders() {
		this.sortOrders.clear();
	}

	/**
	 * Constructs the "Add"-button that is used to open the form in "new entity"
	 * mode
	 * 
	 * @return
	 */
	protected final Button constructAddButton() {
		Button ab = new Button(message("ocs.add"));
		ab.setIcon(VaadinIcons.PLUS);
		ab.addClickListener(e -> doAdd());
		ab.setVisible(!getFormOptions().isHideAddButton() && isEditAllowed());
		return ab;
	}

	/**
	 * Add divider rows to the grid based on the value of the "dividerProperty"
	 */
	protected final void constructGridDividers() {
		if (dividerProperty != null) {
			getGridWrapper().getGrid().setStyleName(DynamoConstants.CSS_DIVIDER);
			getGridWrapper().getGrid().setStyleGenerator(item -> {
				String result = null;
				if (item != null) {
					Object value = ClassUtils.getFieldValue(item, dividerProperty);
					if (value != null) {
						if (!ObjectUtils.equals(value, previousDividerValue)) {
							result = DynamoConstants.CSS_DIVIDER;
						}
						previousDividerValue = value;
					}
				}
				return result;
			});
		}
	}

	/**
	 * Lazily constructs the grid wrapper - implement in subclasses in order to
	 * create the right type of wrapper
	 * 
	 * @return
	 */
	protected abstract BaseGridWrapper<ID, T> constructGridWrapper();

	/**
	 * Creates a new entity - override in subclass if needed
	 * 
	 * @return
	 */
	protected T createEntity() {
		return getService().createNewEntity();
	}

	/**
	 * Switches to the detail mode (which displays the attributes of a single
	 * entity)
	 * 
	 * @param entity the entity to display
	 */
	protected abstract void detailsMode(T entity);

	/**
	 * Disables sorting for the grid if needed
	 */
	protected void disableGridSorting() {
		if (!isSortEnabled()) {
			for (Column<?, ?> c : getGridWrapper().getGrid().getColumns()) {
				c.setSortable(false);
			}
		}
	}

	/**
	 * Method that is called when the Add button is clicked. Can be overridden in
	 * order to perform your own custom logic
	 */
	public void doAdd() {
		setSelectedItem(createEntity());
		detailsMode(getSelectedItem());
	}

	/**
	 * Method that is called after the search results container has been
	 * constructed. Use this to modify the container if needed
	 * 
	 * @param container
	 */
	protected void doConstructDataProvider(DataProvider<T, SerializablePredicate<T>> provider) {
		// overwrite in subclasses
	}

	public HorizontalLayout getButtonBar() {
		return buttonBar;
	}

	public FetchJoinInformation[] getDetailJoins() {
		return detailJoins;
	}

	public FetchJoinInformation[] getDetailJoinsFallBack() {
		return (detailJoins == null || detailJoins.length == 0) ? getJoins() : detailJoins;
	}

	public String getDividerProperty() {
		return dividerProperty;
	}

	public Map<String, SerializablePredicate<?>> getFieldFilters() {
		return fieldFilters;
	}

	/**
	 * Lazily fetches the grip wrapper
	 * 
	 * @return
	 */
	public BaseGridWrapper<ID, T> getGridWrapper() {
		if (gridWrapper == null) {
			gridWrapper = constructGridWrapper();
			postProcessGridWrapper(gridWrapper);
		}
		return gridWrapper;
	}

	public FetchJoinInformation[] getJoins() {
		return joins;
	}

	public Integer getMaxResults() {
		return maxResults;
	}

	public int getPageLength() {
		return pageLength;
	}

	/**
	 * Returns the parent group (which must be returned by the getParentGroupHeaders
	 * method) to which a certain child group belongs
	 * 
	 * @param childGroup the name of the child group
	 * @return
	 */
	protected String getParentGroup(String childGroup) {
		// overwrite in subclasses if needed
		return null;
	}

	/**
	 * Returns a list of keys for the messages that can be used to add an extra
	 * attribute group layer to the layout. By default this method returns null
	 * which means that no extra layer will be used. If you return a non-empty
	 * String array, then the values in this array will be used as additional
	 * attribute group header. Use the "getParentGroup" method to determine which
	 * "regular" attribute group to place inside which parent group.
	 * 
	 * @return
	 */
	protected String[] getParentGroupHeaders() {
		// overwrite in subclasses if needed
		return null;
	}

	public T getSelectedItem() {
		return selectedItem;
	}

	/**
	 * 
	 * @return the currently configured sort orders
	 */
	public List<SortOrder<?>> getSortOrders() {
		return Collections.unmodifiableList(sortOrders);
	}

	/**
	 * Indicates whether editing is allowed. This defaults to TRUE but you can
	 * overwrite it in subclasses if needed
	 * 
	 * @return
	 */
	protected boolean isEditAllowed() {
		return true;
	}

	public boolean isMultiSelect() {
		return multiSelect;
	}

	public boolean isSortEnabled() {
		return sortEnabled;
	}

	/**
	 * Adds additional buttons to the main button bar (that appears below the
	 * results grid in a search layout, split layout, or tabular edit layout)
	 * 
	 * @param buttonBar the button bar
	 */
	protected void postProcessButtonBar(Layout buttonBar) {
		// overwrite in subclass if needed
	}

	/**
	 * Adds additional buttons to the button bar above/below the detail screen.
	 *
	 * 
	 * @param buttonBar the button bar
	 * @param viewMode  indicates whether the form is in view mode
	 */
	protected void postProcessDetailButtonBar(Layout buttonBar, boolean viewMode) {
		// overwrite in subclass if needed
	}

	/**
	 * Post processes the edit fields. This method is called once, just before the
	 * screen is displayed in edit mode for the first time. Use this method to e.g.
	 * set up change listeners
	 * 
	 * @param editForm
	 */
	protected void postProcessEditFields(ModelBasedEditForm<ID, T> editForm) {
		// override in subclasses
	}

	/**
	 * Callback method that is called after the entire layout has been constructed. Use this
	 * to e.g. add additional components to the bottom of the layout or to modify
	 * button captions
	 * 
	 * @param main the main layout
	 */
	protected void postProcessLayout(Layout main) {
		// override in subclasses
	}

	/**
	 * Method that is called after the grid wrapper has been constructed
	 * 
	 * @param wrapper
	 */
	protected void postProcessGridWrapper(BaseGridWrapper<ID, T> wrapper) {
		// overwrite in subclasses when needed
	}

	/**
	 * Sets the joins to use when retrieving a single object for use in a detail
	 * form. If not set then the application will use the same joins as for the
	 * complete result grid
	 * 
	 * @param detailJoins
	 */
	public void setDetailJoins(FetchJoinInformation[] detailJoins) {
		this.detailJoins = detailJoins;
	}

	public void setDividerProperty(String dividerProperty) {
		this.dividerProperty = dividerProperty;
	}

	/**
	 * Sets the field filters to use. Field filters are used to limit the search
	 * results when rendering lookup components for complex attributes (e.g. combo
	 * boxes, lookup fields)
	 * 
	 * @param fieldFilters
	 */
	public void setFieldFilters(Map<String, SerializablePredicate<?>> fieldFilters) {
		this.fieldFilters = fieldFilters;
	}

	/**
	 * Sets the maximum number of search results. If a search results in more hits,
	 * the result set will be truncated
	 * 
	 * @param maxResults
	 */
	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}

	public void setMultiSelect(boolean multiSelect) {
		this.multiSelect = multiSelect;
	}

	/**
	 * Sets the page length (number of rows to display in the search results grid)
	 * 
	 * @param pageLength the desired page length
	 */
	public void setPageLength(int pageLength) {
		this.pageLength = pageLength;
	}

	/**
	 * Sets the provided item as the currently selected item in the grid
	 * 
	 * @param selectedItem the item that you want to become the selected item
	 */
	public void setSelectedItem(T selectedItem) {
		this.selectedItem = selectedItem;
		checkButtonState(selectedItem);
	}

	/**
	 * Specify whether sorting is enabled for the results grid
	 * 
	 * @param sortEnabled whether sorting is enabled
	 */
	public void setSortEnabled(boolean sortEnabled) {
		this.sortEnabled = sortEnabled;
	}
}
