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
package com.ocs.dynamo.ui.composite.grid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.ComponentContext;
import com.ocs.dynamo.ui.composite.export.PivotParameters;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.provider.BaseDataProvider;
import com.ocs.dynamo.ui.provider.IdBasedDataProvider;
import com.ocs.dynamo.ui.provider.PagingDataProvider;
import com.ocs.dynamo.ui.provider.PivotAggregationType;
import com.ocs.dynamo.ui.provider.PivotDataProvider;
import com.ocs.dynamo.ui.provider.PivotedItem;
import com.ocs.dynamo.ui.provider.QueryType;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper around a pivot grid
 *
 * @param <ID>
 * @param <T>
 * @author Bas Rutten
 */
public class PivotGridWrapper<ID extends Serializable, T extends AbstractEntity<ID>>
        extends GridWrapper<ID, T, PivotedItem> {

    private static final long serialVersionUID = -4691108261565306844L;

    @Getter
    @Setter
    private Map<String, Class<?>> aggregationClassMap = new HashMap<>();

    @Getter
    @Setter
    private Map<String, PivotAggregationType> aggregationMap = new HashMap<>();

    /**
     * The label that displays the table caption
     */
    private final Span caption = new Span("");

    /**
     * The name of the property that contains the values that lead to the pivoted
     * columns
     */
    @Getter
    @Setter
    private String columnKeyProperty;

    @Getter
    @Setter
    private BiFunction<String, Object, String> customFormatter = null;

    /**
     * The data provider
     */
    private PivotDataProvider<ID, T> dataProvider;

    /**
     * Bifunction used to map pivot column headers for export only
     */
    @Getter
    @Setter
    private BiFunction<Object, Object, String> exportHeaderMapper;

    /**
     * Bifunction used to map pivot column subheaders for export only
     */
    @Getter
    @Setter
    private BiFunction<Object, Object, String> exportSubHeaderMapper;

    /**
     * The names of the fixed/frozen columns
     */
    @Getter
    @Setter
    private List<String> fixedColumnKeys;

    /**
     * Function for mapping for fixed property name to grid header
     */
    @Getter
    @Setter
    private Function<String, String> fixedHeaderMapper = Function.identity();

    /**
     * The wrapped grid component
     */
    private PivotGrid<ID, T> grid;

    /**
     * Bifunction used to map pivot column headers
     */
    @Getter
    @Setter
    private BiFunction<Object, Object, String> headerMapper = (a, b) -> a.toString();

    /**
     * The properties to display in the pivoted columns
     */
    @Getter
    @Setter
    private List<String> hiddenPivotedProperties;

    /**
     * Whether to include an aggregate row at the bottom
     */
    @Getter
    @Setter
    private boolean includeAggregateRow;

    /**
     * The layout that contains the grid
     */
    private VerticalLayout layout;

    /**
     * The properties to display in the pivoted columns
     */
    @Getter
    @Setter
    private List<String> pivotedProperties;

    /**
     * The possible values of the columnPropertyKey property.
     */
    @Getter
    @Setter
    private List<Object> possibleColumnKeys;

    /**
     * The property that is checked to determine whether a new row is reached
     */
    @Getter
    @Setter
    private String rowKeyProperty;

    /**
     * Supplier that is used to determine the number of rows in the pivot table
     */
    @Getter
    @Setter
    private Supplier<Integer> sizeSupplier;

    /**
     * Mapping function for determining the sub header from the column key value and
     * pivot property
     */
    @Getter
    @Setter
    private BiFunction<Object, Object, String> subHeaderMapper = (a, b) -> b.toString();

    /**
     * The wrapped data provider
     */
    private BaseDataProvider<ID, T> wrappedProvider;

    /**
     * @param service          the service that is used for retrieving data
     * @param entityModel      the entity model
     * @param queryType        the query type to use
     * @param formOptions      the form options to use
     * @param componentContext the component context
     * @param filter           the search filter to apply
     * @param sortOrders       the sort orders
     * @param joins            options list of fetch joins to include in the query
     */
    public PivotGridWrapper(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
                            FormOptions formOptions, ComponentContext<ID, T> componentContext, SerializablePredicate<T> filter,
                            List<SortOrder<?>> sortOrders, FetchJoinInformation... joins) {
        super(service, entityModel, queryType, formOptions, componentContext, filter, sortOrders, joins);
    }

    /**
     * Builds the component.
     */
    @Override
    public void build() {
        layout = new VerticalLayout();
        layout.add(caption);

        this.dataProvider = constructDataProvider();
        grid = getGrid();
        layout.add(grid);
        initSortingAndFiltering();

        grid.setSelectionMode(SelectionMode.SINGLE);
        add(layout);
    }

    protected PivotDataProvider<ID, T> constructDataProvider() {

        if (QueryType.PAGING.equals(getQueryType())) {
            wrappedProvider = new PagingDataProvider<>(getService(), getEntityModel(),
                    getFormOptions().isShowNextButton() || getFormOptions().isShowPrevButton(), getJoins());
        } else {
            wrappedProvider = new IdBasedDataProvider<>(getService(), getEntityModel(), getJoins());
        }

        PivotDataProvider<ID, T> pivotDataProvider = new PivotDataProvider<>(wrappedProvider, rowKeyProperty,
                columnKeyProperty, fixedColumnKeys, pivotedProperties, hiddenPivotedProperties, sizeSupplier);
        pivotDataProvider.setAggregationMap(aggregationMap);
        pivotDataProvider.setAggregationClassMap(aggregationClassMap);
        pivotDataProvider.setAfterCountCompleted(count -> updateCaption(count));
        postProcessDataProvider(pivotDataProvider);
        return pivotDataProvider;
    }

    /**
     * Constructs the grid - override in subclasses if you need a different grid
     * implementation
     *
     * @return the constructed grid
     */
    protected PivotGrid<ID, T> constructGrid() {
        return new PivotGrid<>(dataProvider, possibleColumnKeys, fixedHeaderMapper, headerMapper, subHeaderMapper,
                customFormatter);
    }

    public DataProvider<PivotedItem, SerializablePredicate<PivotedItem>> getDataProvider() {
        return dataProvider;
    }

    @Override
    public int getDataProviderSize() {
        return dataProvider.getSize();
    }

    /**
     * Lazily construct and return the grid
     *
     * @return the grid
     */
    public PivotGrid<ID, T> getGrid() {
        if (grid == null) {
            grid = constructGrid();
        }
        return grid;
    }

    /**
     * Extracts the sort directions from the sort orders
     */
    protected boolean[] getSortDirections() {
        boolean[] result = new boolean[getSortOrders().size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = SortDirection.ASCENDING == getSortOrders().get(i).getDirection();
        }
        return result;
    }

    /**
     * Initializes the sorting and filtering for the grid
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected List<SortOrder<?>> initSortingAndFiltering() {

        // pass along initial filter
        getGrid().getDataCommunicator().setDataProvider(getDataProvider(), (SerializablePredicate) getFilter());

        List<SortOrder<?>> fallbackOrders = super.initSortingAndFiltering();

        // set fall back sort orders
        if (wrappedProvider != null) {
            wrappedProvider.setFallbackSortOrders(fallbackOrders);
        }

        if (getFormOptions().isExportAllowed() && getExportDelegate() != null) {

            GridContextMenu<PivotedItem> contextMenu = getGrid().addContextMenu();
            Button downloadButton = new Button(message("ocs.download"));
            downloadButton.addClickListener(event -> {
                List<SortOrder<?>> orders = new ArrayList<>();
                List<GridSortOrder<PivotedItem>> so = getGrid().getSortOrder();
                for (GridSortOrder<PivotedItem> gso : so) {
                    orders.add(new SortOrder<String>(gso.getSorted().getKey(), gso.getDirection()));
                }

                PivotParameters pars = PivotParameters.builder() //
                        .columnKeyProperty(columnKeyProperty) //
                        .fixedColumnKeys(fixedColumnKeys) //
                        .fixedHeaderMapper(fixedHeaderMapper) //
                        .pivotedProperties(pivotedProperties) //
                        .possibleColumnKeys(possibleColumnKeys) //
                        .rowKeyProperty(rowKeyProperty) //
                        .hiddenPivotedProperties(hiddenPivotedProperties) //
                        .aggregationMap(aggregationMap) //
                        .aggregationClassMap(aggregationClassMap) //
                        .includeAggregateRow(includeAggregateRow) //
                        .subHeaderMapper(subHeaderMapper) //
                        .headerMapper(exportHeaderMapper != null ? exportHeaderMapper : headerMapper) //
                        .subHeaderMapper(exportSubHeaderMapper != null ? exportSubHeaderMapper : subHeaderMapper)
                        .build();

                // use the fallback sort orders here
                getExportDelegate().exportPivoted(
                        getExportEntityModel() != null ? getExportEntityModel() : getEntityModel(), getFilter(),
                        fallbackOrders, pars, getExportJoins() != null ? getExportJoins() : getJoins());

            });
            contextMenu.add(downloadButton);
        }

        return fallbackOrders;
    }

    /**
     * Respond to a selection of an item in the grid
     */
    protected void onSelect(Object selected) {
        // overwrite in subclasses
    }

    /**
     * Callback method used to modify data provider creation
     *
     * @param provider
     */
    protected void postProcessDataProvider(PivotDataProvider<ID, T> provider) {
        // overwrite in subclasses
    }

    @Override
    public void reloadDataProvider() {
        // not needed
    }

    /**
     * Updates the caption above the grid that shows the number of items
     *
     * @param size the number of items
     */
    protected void updateCaption(int size) {
        caption.setText(getEntityModel().getDisplayNamePlural(VaadinUtils.getLocale()) + " "
                + getMessageService().getMessage("ocs.showing.results", VaadinUtils.getLocale(), size));
    }

}
