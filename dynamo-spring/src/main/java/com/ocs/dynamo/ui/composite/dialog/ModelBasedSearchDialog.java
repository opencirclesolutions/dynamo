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
package com.ocs.dynamo.ui.composite.dialog;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.ServiceLocator;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.layout.SimpleSearchLayout;
import com.ocs.dynamo.ui.container.QueryType;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.ui.Layout;

/**
 * A dialog that contains a search form based on the Entity Model
 * 
 * @author bas.rutten
 * @param <ID>
 *            the type of the primary key
 * @param <T>
 *            the type of the entity to search for
 */
public class ModelBasedSearchDialog<ID extends Serializable, T extends AbstractEntity<ID>> extends
        SimpleModalDialog {

    private static final long serialVersionUID = -7158664165266474097L;

    /**
     * List of filters to apply to the search dialog
     */
    private List<Filter> filters;

    /**
     * The entity model
     */
    private EntityModel<T> entityModel;

    /**
     * The optional joins that determine which related data to fetch
     */
    private FetchJoinInformation[] joins;

    /**
     * Indicates whether the dialog is in multi-select mode
     */
    private boolean multiSelect;

    /**
     * the (optional) page length. If set it will override the default page length
     */
    private Integer pageLength;

    /**
     * The actual search layout
     */
    private SimpleSearchLayout<ID, T> searchLayout;

    /**
     * The service used for querying the database
     */
    private BaseService<ID, T> service;

    /**
     * The sort order
     */
    private SortOrder sortOrder;

    /**
     * Constructor
     * 
     * @param service
     *            the service used to retrieve entities
     * @param entityModel
     *            the entity model of the displayed entities
     * @param additionalFilters
     * @param sortOrder
     * @param multiSelect
     * @param joins
     */
    public ModelBasedSearchDialog(BaseService<ID, T> service, EntityModel<T> entityModel,
            List<Filter> filters, SortOrder sortOrder, boolean multiSelect,
            FetchJoinInformation... joins) {
        super(true);
        this.service = service;
        this.entityModel = entityModel;
        this.sortOrder = sortOrder;
        this.filters = filters;
        this.multiSelect = multiSelect;
        this.joins = joins;
    }

    @Override
    protected void doBuild(Layout parent) {
        FormOptions formOptions = new FormOptions();
        formOptions.setHideAddButton(true);
        formOptions.setPopup(true);

        searchLayout = new SimpleSearchLayout<ID, T>(service, entityModel, QueryType.ID_BASED,
                formOptions, null, filters, sortOrder, joins);
        if (pageLength != null) {
            searchLayout.setPageLength(pageLength);
        }
        searchLayout.getTableWrapper().getTable().setMultiSelect(multiSelect);
        parent.addComponent(searchLayout);
    }

    public Integer getPageLength() {
        return pageLength;
    }

    protected T getSelectedItem() {
        return searchLayout.getSelectedItem();
    }

    protected Collection<T> getSelectedItems() {
        return searchLayout.getSelectedItems();
    }

    @Override
    protected String getTitle() {
        return ServiceLocator.getMessageService().getMessage("ocs.search.title",
                entityModel.getDisplayNamePlural());
    }

    public void setPageLength(Integer pageLength) {
        this.pageLength = pageLength;
    }

    public SimpleSearchLayout<ID, T> getSearchLayout() {
        return searchLayout;
    }

}
