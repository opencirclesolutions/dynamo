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
import java.util.Collection;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.grid.BaseGridWrapper;
import com.ocs.dynamo.ui.composite.grid.FixedGridWrapper;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * A layout for displaying a fixed collection of items, that contains both a
 * grid view and a details view
 * 
 * @author bas.rutten
 * @param <ID> the type of the primary key
 * @param <T> the type of the entity
 */
@SuppressWarnings("serial")
public abstract class FixedSplitLayout<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseSplitLayout<ID, T> {

    private static final long serialVersionUID = 4606800218149558500L;

    /**
     * The fixed collection of items that is displayed in the table
     */
    private Collection<T> items;

    /**
     * Constructor
     * 
     * @param service      the service
     * @param entityModel  the entity model that is used to construct the layout
     * @param formOptions  the form options that govern how the screen behaves
     * @param fieldFilters field filters applied to fields in the detail view
     * @param sortOrder    the sort order
     */
    public FixedSplitLayout(BaseService<ID, T> service, EntityModel<T> entityModel, FormOptions formOptions, SortOrder<?> sortOrder) {
        super(service, entityModel, formOptions, sortOrder);
    }

    /**
     * Callback method that is executed after reload
     */
    @Override
    protected void afterReload(T t) {
        if (t != null) {
            getGridWrapper().getGrid().select(t);
        } else {
            getGridWrapper().getGrid().deselectAll();
        }
    }

    /**
     * The initialization consists of retrieving the required items
     */
    @Override
    public void buildFilter() {
        this.items = loadItems();
    }

    @Override
    protected final BaseGridWrapper<ID, T> constructGridWrapper() {
        FixedGridWrapper<ID, T> wrapper = new FixedGridWrapper<ID, T>(getService(), getEntityModel(), getFormOptions(), getFieldFilters(),
                getItems(), getSortOrders()) {

            @Override
            protected void onSelect(Object selected) {
                setSelectedItems(selected);
                checkComponentState(getSelectedItem());
                if (getSelectedItem() != null) {
                    detailsMode(getSelectedItem());
                }
            }

            @Override
            protected void postProcessDataProvider(DataProvider<T, SerializablePredicate<T>> provider) {
                FixedSplitLayout.this.postProcessDataProvider(provider);
            }
        };
        postConfigureGridWrapper(wrapper);
        wrapper.build();
        return wrapper;
    }

    @Override
    protected final TextField constructSearchField() {
        // do nothing - not supported for this component
        return null;
    }

    public Collection<T> getItems() {
        return items;
    }

    /**
     * Loads the items that are to be displayed
     */
    protected abstract Collection<T> loadItems();

    /**
     * Reloads the data after an update
     */
    @Override
    public void reload() {
        buildFilter();
        super.reload();
        // remove all items from the container and add the new ones
        ListDataProvider<T> provider = (ListDataProvider<T>) getGridWrapper().getDataProvider();
        provider.getItems().clear();
        provider.getItems().addAll(items);
        provider.refreshAll();
        setSelectedItem(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setSelectedItems(Object selectedItems) {
        if (selectedItems != null) {
            if (selectedItems instanceof Collection<?>) {
                Collection<?> col = (Collection<?>) selectedItems;
                if (col.iterator().hasNext()) {
                    T t = (T) col.iterator().next();
                    // fetch the item again so that any details are loaded
                    setSelectedItem(t);
                } else {
                    setSelectedItem(null);
                    emptyDetailView();
                }
            }
        } else {
            setSelectedItem(null);
            emptyDetailView();
        }
    }
}
