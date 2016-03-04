package com.ocs.dynamo.ui.composite.layout;

import java.util.ArrayList;
import java.util.List;

import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;

/**
 * A base class for a composite layout that displays a collection of data
 * (rather than an single object)
 * 
 * @author bas.rutten
 * @param <ID>
 *            the type of the primary key
 * @param <T>
 *            the type of the entity
 */
public abstract class BaseCollectionLayout<ID, T extends AbstractEntity<ID>>
        extends BaseServiceCustomComponent<ID, T> {

	private static final long serialVersionUID = -2864711994829582000L;

	private HorizontalLayout buttonBar;

	// the joins to use when fetching data
	private FetchJoinInformation[] joins;

	// the currently selected item
	private T selectedItem;

	// whether the table can manually be sorted
	private boolean sortEnabled = true;

	// the sort order
	private SortOrder sortOrder;

	// list of buttons to update after the user selects an item in the tabular
	// view
	private List<Button> toUpdate = new ArrayList<>();

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
	public BaseCollectionLayout(BaseService<ID, T> service, EntityModel<T> entityModel,
	        FormOptions formOptions, SortOrder sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, formOptions);
		this.joins = joins;
		this.sortOrder = sortOrder;
	}

	/**
	 * Checks which buttons in the button bar must be enabled
	 * 
	 * @param selectedItem
	 */
	protected void checkButtonState(T selectedItem) {
		for (Button b : toUpdate) {
			b.setEnabled(selectedItem != null && mustEnableButton(b, selectedItem));
		}
	}

	/**
	 * Creates a new entity - override in subclass if needed
	 * 
	 * @return
	 */
	protected T createEntity() {
		return getService().createNewEntity();
	}

	public HorizontalLayout getButtonBar() {
		return buttonBar;
	}

	public FetchJoinInformation[] getJoins() {
		return joins;
	}

	public T getSelectedItem() {
		return selectedItem;
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}

	/**
	 * Indicates whether editing is allowed
	 * 
	 * @return
	 */
	protected boolean isEditAllowed() {
		return true;
	}

	public boolean isSortEnabled() {
		return sortEnabled;
	}

	/**
	 * Callback method that is called in order to enable/disable a button after
	 * selecting an item in the table
	 * 
	 * @param button
	 * @return
	 */
	protected boolean mustEnableButton(Button button, T selectedItem) {
		// overwrite in subclasses if needed
		return true;
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
	 * Adds additional layout components to the layout
	 * 
	 * @param main
	 *            the main layout
	 */
	protected void postProcessLayout(Layout main) {
		// overwrite in subclass
	}

	/**
	 * Registers a button that must be enabled/disabled after an item is
	 * selected. use the "mustEnableButton" callback method to impose additional
	 * constraints on when the button must be enabled
	 * 
	 * @param button
	 *            the button to register
	 */
	protected void registerDetailButton(Button button) {
		if (button != null) {
			button.setEnabled(false);
			toUpdate.add(button);
		}
	}

	protected void setButtonBar(HorizontalLayout buttonBar) {
		this.buttonBar = buttonBar;
	}

	public void setSelectedItem(T selectedItem) {
		this.selectedItem = selectedItem;
	}

	public void setSortEnabled(boolean sortEnabled) {
		this.sortEnabled = sortEnabled;
	}

	public void setSortOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}
}
