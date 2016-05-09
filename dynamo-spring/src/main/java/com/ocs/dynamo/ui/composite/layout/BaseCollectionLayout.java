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
import java.util.List;

import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.table.BaseTableWrapper;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Button.ClickEvent;

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
public abstract class BaseCollectionLayout<ID extends Serializable, T extends AbstractEntity<ID>>
        extends BaseServiceCustomComponent<ID, T> {

    // the default page length
    private static final int PAGE_LENGTH = 20;

    private static final long serialVersionUID = -2864711994829582000L;

    // the button bar
    private HorizontalLayout buttonBar = new DefaultHorizontalLayout();

    // the joins to use when fetching data
    private FetchJoinInformation[] joins;

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
     * Removes all sort orders
     */
    public void clearSortOrders() {
        this.sortOrders.clear();
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
     * Displays the details mode
     * 
     * @param entity
     */
    protected abstract void detailsMode(T entity);

    /**
     * Constructs the add button
     * 
     * @return
     */
    protected Button constructAddButton() {
        Button ab = new Button(message("ocs.add"));
        ab.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = -5005648144833272606L;

            @Override
            public void buttonClick(ClickEvent event) {
                setSelectedItem(createEntity());
                detailsMode(getSelectedItem());
            }
        });
        ab.setVisible(!getFormOptions().isHideAddButton() && isEditAllowed());
        return ab;
    };
    
    public HorizontalLayout getButtonBar() {
        return buttonBar;
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
        }
        return tableWrapper;
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
     * Method that is called in order to enable/disable a button after selecting an item in the
     * table
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
     * Registers a button that must be enabled/disabled after an item is selected. use the
     * "mustEnableButton" callback method to impose additional constraints on when the button must
     * be enabled
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

    public void setPageLength(int pageLength) {
        this.pageLength = pageLength;
    }

    public void setSelectedItem(T selectedItem) {
        this.selectedItem = selectedItem;
    }

    public void setSortEnabled(boolean sortEnabled) {
        this.sortEnabled = sortEnabled;
    }
}
