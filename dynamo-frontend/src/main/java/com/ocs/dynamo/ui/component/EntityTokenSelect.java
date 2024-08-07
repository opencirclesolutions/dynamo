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
package com.ocs.dynamo.ui.component;

import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.SelectMode;
import com.ocs.dynamo.filter.AndPredicate;
import com.ocs.dynamo.filter.FilterConverter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.utils.SortUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.utils.EntityModelUtils;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Custom ListSelect component for displaying a collection of entities from
 * which multiple items can be selected
 *
 * @param <ID> type of the primary key of the entity
 * @param <T>  type of the entity
 * @author bas.rutten
 */
public class EntityTokenSelect<ID extends Serializable, T extends AbstractEntity<ID>> extends MultiSelectComboBox<T>
        implements Refreshable, Cascadable<T> {

    private static final long serialVersionUID = 3041574615271340579L;

    /**
     * The addition search filter for cascading
     */
    @Getter
    private SerializablePredicate<T> additionalFilter;

    /**
     * The attribute model that governs how to build the component
     */
    @Getter
    private final AttributeModel attributeModel;

    private final EntityModel<T> entityModel;

    /**
     * The search filter to use in filtered mode
     */
    @Getter
    private SerializablePredicate<T> predicate;

    /**
     * The original search filter
     */
    private SerializablePredicate<T> originalFilter;

    /**
     * The select mode (filtered, all, or fixed)
     */
    @Getter
    private final SelectMode selectMode;

    private final BaseService<ID, T> service;

    /**
     * The sort orders
     */
    @Getter
    private final SortOrder<?>[] sortOrders;

    /**
     * The number of items managed by the data provider
     */
    private int count;

    /**
     * Constructor
     *
     * @param entityModel    the entity model
     * @param attributeModel the attribute model
     * @param service        the service that is used to query the database
     * @param selectMode     the select mode
     * @param filter         the field filter
     * @param items          the fixed collection on entities to display
     * @param sharedProvider the shared data provider
     * @param sortOrders     the sort orders
     */
    public EntityTokenSelect(EntityModel<T> entityModel, AttributeModel attributeModel, BaseService<ID, T> service,
                             SelectMode selectMode, SerializablePredicate<T> filter, List<T> items,
                             DataProvider<T, SerializablePredicate<T>> sharedProvider, SortOrder<?>... sortOrders) {
        this.entityModel = entityModel;
        this.service = service;
        this.selectMode = selectMode;
        this.sortOrders = sortOrders;
        this.attributeModel = attributeModel;
        this.predicate = filter;

        if (attributeModel != null) {
            this.setLabel(attributeModel.getDisplayName(VaadinUtils.getLocale()));
        }

        initProvider(sharedProvider, items, selectMode);

        setItemLabelGenerator(item -> {
            String value = EntityModelUtils.getDisplayPropertyValue(item, entityModel);
            return value == null ? "" : value;
        });
    }

    /**
     * Constructor - for the "FILTERED" mode
     *
     * @param targetEntityModel the entity model of the entities that are to be
     *                          displayed
     * @param attributeModel    the attribute model for the property that is bound
     *                          to this component
     * @param service           the service used to retrieve the entities
     * @param filter            the filter used to filter the entities
     * @param sortOrders        the sort orders used to sort the entities
     */
    public EntityTokenSelect(EntityModel<T> targetEntityModel, AttributeModel attributeModel,
                             BaseService<ID, T> service, SerializablePredicate<T> filter, SortOrder<?>... sortOrders) {
        this(targetEntityModel, attributeModel, service, SelectMode.FILTERED_PAGED, filter, null, null, sortOrders);
    }

    /**
     * Constructor - for the "ALL" mode
     *
     * @param targetEntityModel the entity model of the entities that are to be
     *                          displayed
     * @param attributeModel    the attribute model for the property that is bound
     *                          to this component
     * @param service           the service used to retrieve entities
     */
    public EntityTokenSelect(EntityModel<T> targetEntityModel, AttributeModel attributeModel,
                             BaseService<ID, T> service, SortOrder<?>... sortOrder) {
        this(targetEntityModel, attributeModel, service, SelectMode.ALL, null, null, null, sortOrder);
    }

    /**
     * Constructor - for the "FIXED" mode
     *
     * @param targetEntityModel the entity model of the entities that are to be
     *                          displayed
     * @param attributeModel    the attribute model for the property that is bound
     *                          to this component
     * @param items             the list of entities to display
     */
    public EntityTokenSelect(EntityModel<T> targetEntityModel, AttributeModel attributeModel, List<T> items) {
        this(targetEntityModel, attributeModel, null, SelectMode.FIXED, null, items, null);
    }

    @SuppressWarnings("unchecked")
    public void afterNewEntityAdded(T entity) {
        if (getDataProvider() instanceof ListDataProvider) {
            ListDataProvider<T> provider = (ListDataProvider<T>) getDataProvider();
            provider.getItems().add(entity);
        } else {
            updateProvider((DataProvider<T, SerializablePredicate<T>>) getDataProvider());
        }
        select(entity);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void castAndSetDataProvider(DataProvider<T, SerializablePredicate<T>> provider) {
        if (provider instanceof CallbackDataProvider) {
            setItems((CallbackDataProvider) provider);
        } else if (provider instanceof ListDataProvider) {
            setItems(new MultiSelectIgnoreDiacriticsCaptionFilter<>(entityModel, true, false), ((ListDataProvider<T>) provider).getItems());
        }
    }

    @Override
    public void clearAdditionalFilter() {
        this.additionalFilter = predicate;
        this.predicate = originalFilter;
        refresh();
    }

    private CallbackDataProvider<T, String> createCallbackProvider() {
        return CallbackProviderHelper.createCallbackProvider(service, entityModel, predicate,
                new SortOrders(SortUtils.translateSortOrders(sortOrders)), c -> this.count = c);
    }

    /**
     * Initializes the data provider
     *
     * @param provider already existing provider (in case of shared provider)
     * @param items    fixed list of items to display
     * @param mode     the desired mode
     */
    private void initProvider(DataProvider<T, SerializablePredicate<T>> provider, List<T> items, SelectMode mode) {
        if (provider == null) {
            if (SelectMode.ALL.equals(mode)) {
                ListDataProvider<T> listProvider = new ListDataProvider<>(
                        service.findAll(SortUtils.translateSortOrders(sortOrders)));
                setItems(new MultiSelectIgnoreDiacriticsCaptionFilter<>(entityModel, true, false), listProvider);
            } else if (SelectMode.FILTERED_PAGED.equals(mode)) {
                CallbackDataProvider<T, String> callbackProvider = createCallbackProvider();
                setItems(callbackProvider);
            } else if (SelectMode.FILTERED_ALL.equals(mode)) {
                items = service.find(new FilterConverter<>(entityModel).convert(predicate),
                        SortUtils.translateSortOrders(sortOrders));
                setItems(new MultiSelectIgnoreDiacriticsCaptionFilter<>(entityModel, true, false),
                        new ListDataProvider<>(items));
            } else if (SelectMode.FIXED.equals(mode)) {
                setItems(new MultiSelectIgnoreDiacriticsCaptionFilter<>(entityModel, true, false),
                        new ListDataProvider<>(items));
            }
        } else {
            castAndSetDataProvider(provider);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void refresh() {
        Set<T> stored = this.getValue();
        clear();
        DataProvider<T, ?> provider = getDataProvider();
        updateProvider((DataProvider<T, SerializablePredicate<T>>) provider);
        setValue(stored);
    }

    public int getDataProviderSize() {
        if (getDataProvider() instanceof ListDataProvider listDataProvider) {
            return listDataProvider.getItems().size();
        } else if (getDataProvider() instanceof CallbackDataProvider) {
            return count;
        }
        return 0;
    }

    public void refresh(SerializablePredicate<T> filter) {
        this.originalFilter = filter;
        this.predicate = filter;
        refresh();
    }

    private void reloadDataProvider(ListDataProvider<T> listProvider, List<T> items) {
        listProvider.getItems().clear();
        listProvider.getItems().addAll(items);
        listProvider.refreshAll();
    }

    @Override
    public void setAdditionalFilter(SerializablePredicate<T> additionalFilter) {
        clear();
        this.additionalFilter = additionalFilter;
        this.predicate = originalFilter == null ? additionalFilter : new AndPredicate<>(originalFilter, additionalFilter);
        refresh();
    }

    /**
     * Updates the data provider after a refresh
     *
     * @param provider the data provider to update
     */
    private void updateProvider(DataProvider<T, SerializablePredicate<T>> provider) {
        if (SelectMode.ALL.equals(selectMode)) {
            ListDataProvider<T> listProvider = (ListDataProvider<T>) provider;
            // add all items (but sorted)
            listProvider.getItems().clear();
            listProvider.getItems().addAll(service.findAll(SortUtils.translateSortOrders(sortOrders)));
        } else if (SelectMode.FILTERED_PAGED.equals(selectMode)) {
            // add a filtered selection of items
            setItems(createCallbackProvider());
        } else if (SelectMode.FILTERED_ALL.equals(selectMode)) {
            ListDataProvider<T> listProvider = (ListDataProvider<T>) provider;
            List<T> items = service.find(new FilterConverter<>(entityModel).convert(predicate),
                    SortUtils.translateSortOrders(sortOrders));
            reloadDataProvider(listProvider, items);
        }
    }
}
