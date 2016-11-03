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
import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.table.BaseTableWrapper;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;

/**
 * A base class for a composite layout that displays a collection of data (rather than an single
 * object)
 * 
 * @author bas.rutten
 * @param <ID>
 *            the type of the primary key
 * @param <T>
 *            the type of the entity
 */
public abstract class BaseCollectionLayout<ID extends Serializable, T extends AbstractEntity<ID>> extends
        BaseServiceCustomComponent<ID, T> {

	private static final long serialVersionUID = -2864711994829582000L;

	// the default page length
	private static final int PAGE_LENGTH = 20;

	// the button bar
	private HorizontalLayout buttonBar = new DefaultHorizontalLayout();

	//
	private FetchJoinInformation[] detailJoins;

	// the property used to determine when to draw a divider row
	private String dividerProperty;

	// filters to apply to individual search fields
	private Map<String, Filter> fieldFilters = new HashMap<>();

	// the joins to use when fetching data
	private FetchJoinInformation[] joins;

	// the value from the previous row used when drawing divider rows
	private Object previousDividerValue;

	// the page length (number of rows that is displayed in the table)
	private int pageLength = PAGE_LENGTH;

	// the currently selected item
	private T selectedItem;

	// whether the table can manually be sorted
	private boolean sortEnabled = true;

	// the sort orders
	private List<SortOrder> sortOrders = new ArrayList<>();

	// the table wrapper
	private BaseTableWrapper<ID, T> tableWrapper;

	// whether the selection of multiple values is allowed
	private boolean multiSelect = false;

	/**
	 * Constructor
	 * 
	 * @param service
	 *            the service
	 * @param entityModel
	 *            the entity model
	 * @param formOptions
	 *            the form options
	 * @param sortOrder
	 *            the sort order
	 * @param joins
	 *            the joins to use when fetching data
	 */
	public BaseCollectionLayout(BaseService<ID, T> service, EntityModel<T> entityModel, FormOptions formOptions,
	        SortOrder sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, formOptions);
		this.joins = joins;
		if (sortOrder != null) {
			sortOrders.add(sortOrder);
		}
	}

	/**
	 * Adds an additional sort order
	 * 
	 * @param sortOrder
	 */
	public void addSortOrder(SortOrder sortOrder) {
		this.sortOrders.add(sortOrder);
	}

	/**
	 * Method that is called after the user selects an entity to view in Details mode
	 * 
	 * @param editForm
	 *            the edit form which displays the entity
	 * @param entity
	 *            the selected entity
	 */
	protected void afterDetailSelected(ModelBasedEditForm<ID, T> editForm, T entity) {
		// override in subclass
	}

	/**
	 * Method that is called after the setEntity method is called. Can be used to fetch additional
	 * data if required. This method is called before the "afterDetailSelected" method is called
	 * 
	 * @param entity
	 *            the entity
	 */
	protected void afterEntitySet(T entity) {
		// override in subclass
	}

	/**
	 * Removes all sort orders
	 */
	public void clearSortOrders() {
		this.sortOrders.clear();
	}

	/**
	 * Constructs the "Add"-button
	 * 
	 * @return
	 */
	protected Button constructAddButton() {
		Button ab = new Button(message("ocs.add"));
		ab.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = -5005648144833272606L;

			@Override
			public void buttonClick(ClickEvent event) {
				doAdd();
			}
		});
		ab.setVisible(!getFormOptions().isHideAddButton() && isEditAllowed());
		return ab;
	}

	/**
	 * Set up the code for adding table dividers
	 */
	protected void constructTableDividers() {
		if (dividerProperty != null) {
			getTableWrapper().getTable().setStyleName(DynamoConstants.CSS_DIVIDER);
			getTableWrapper().getTable().setCellStyleGenerator(new CellStyleGenerator() {

				private static final long serialVersionUID = -943390318671601151L;

				@Override
				public String getStyle(Table source, Object itemId, Object propertyId) {
					String result = null;
					if (itemId != null) {
						Property<?> prop = source.getItem(itemId).getItemProperty(dividerProperty);
						if (prop != null) {
							Object obj = prop.getValue();
							if (!ObjectUtils.equals(obj, previousDividerValue)) {
								result = DynamoConstants.CSS_DIVIDER;
							}
							previousDividerValue = obj;
						}
					}
					return result;
				}
			});
		}
	}

	/**
	 * Lazily constructs the table wrapper - implement in subclasses in order to create the right
	 * type of wrapper
	 * 
	 * @return
	 */
	protected abstract BaseTableWrapper<ID, T> constructTableWrapper();

	/**
	 * Creates a new entity - override in subclass if needed
	 * 
	 * @return
	 */
	protected T createEntity() {
		return getService().createNewEntity();
	}

	/**
	 * Switches to the detail mode (which displays the attributes of a single entity)
	 * 
	 * @param entity
	 *            the entity to display
	 */
	protected abstract void detailsMode(T entity);

	/**
	 * The code that is carried out once the add button is clicked
	 */
	protected void doAdd() {
		setSelectedItem(createEntity());
		detailsMode(getSelectedItem());
	}

	/**
	 * Method that is called after the search results container has been constructed. Use this to
	 * modify the container if needed
	 * 
	 * @param container
	 */
	protected void doConstructContainer(Container container) {
		// overwrite in subclasses
	}

	public HorizontalLayout getButtonBar() {
		return buttonBar;
	}

	public String getDividerProperty() {
		return dividerProperty;
	}

	public FetchJoinInformation[] getJoins() {
		return joins;
	}

	public int getPageLength() {
		return pageLength;
	}

	public T getSelectedItem() {
		return selectedItem;
	}

	/**
	 * 
	 * @return the currently configured sort orders
	 */
	public List<SortOrder> getSortOrders() {
		return Collections.unmodifiableList(sortOrders);
	}

	/**
	 * 
	 * @return the table wrapper
	 */
	public BaseTableWrapper<ID, T> getTableWrapper() {
		if (tableWrapper == null) {
			tableWrapper = constructTableWrapper();
			postProcessTableWrapper(tableWrapper);
		}
		return tableWrapper;
	}

	/**
	 * Indicates whether editing is allowed. This defaults to TRUE but you can overwrite it in
	 * subclasses if needed
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
	 * Adds additional buttons to the button bar
	 * 
	 * @param buttonBar
	 *            the button bar
	 */
	protected void postProcessButtonBar(Layout buttonBar) {
		// overwrite in subclass if needed
	}

	/**
	 * Adds additional buttons to the button bar above/below the detail screen
	 * 
	 * @param buttonBar
	 *            the button bar
	 * @param viewMode
	 *            indicates whether the form is in view mode
	 */
	protected void postProcessDetailButtonBar(Layout buttonBar, boolean viewMode) {
		// overwrite in subclass if needed
	}

	/**
	 * Post processes the edit fields. This method is called once, just before the screen is
	 * displayed in edit mode for the first time. Use this method to e.g. set up change listeners
	 * 
	 * @param editForm
	 */
	protected void postProcessEditFields(ModelBasedEditForm<ID, T> editForm) {
		// do nothing by default - override in subclasses
	}

	/**
	 * Method that is called after the entire layout has been constructed. Use this to e.g. add
	 * additional components to the bottom of the layout or to modify the table
	 * 
	 * @param main
	 *            the main layout
	 */
	protected void postProcessLayout(Layout main) {
		// overwrite in subclass
	}

	/**
	 * Method that is called after the table wrapper has been constructed
	 * 
	 * @param wrapper
	 */
	protected void postProcessTableWrapper(BaseTableWrapper<ID, T> wrapper) {
		// overwrite in subclasses when needed
	}

	public void setDividerProperty(String dividerProperty) {
		this.dividerProperty = dividerProperty;
	}

	public void setMultiSelect(boolean multiSelect) {
		this.multiSelect = multiSelect;
	}

	public void setPageLength(int pageLength) {
		this.pageLength = pageLength;
	}

	public void setSelectedItem(T selectedItem) {
		this.selectedItem = selectedItem;
		checkButtonState(selectedItem);
	}

	public void setSortEnabled(boolean sortEnabled) {
		this.sortEnabled = sortEnabled;
	}

	public FetchJoinInformation[] getDetailJoins() {
		return detailJoins;
	}

	public void setDetailJoins(FetchJoinInformation[] detailJoins) {
		this.detailJoins = detailJoins;
	}

	public Map<String, Filter> getFieldFilters() {
		return fieldFilters;
	}

	public void setFieldFilters(Map<String, Filter> fieldFilters) {
		this.fieldFilters = fieldFilters;
	}
}
