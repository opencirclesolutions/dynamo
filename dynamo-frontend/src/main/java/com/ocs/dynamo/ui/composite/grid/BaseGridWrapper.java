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
import java.util.List;
import java.util.Map;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.provider.BaseDataProvider;
import com.ocs.dynamo.ui.provider.QueryType;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * A base class for objects that wrap around a ModelBasedTable
 * 
 * @author bas.rutten
 * @param <ID> type of the primary key
 * @param <T>  type of the entity
 */
public abstract class BaseGridWrapper<ID extends Serializable, T extends AbstractEntity<ID>>
		extends GridWrapper<ID, T, T> {

	private static final long serialVersionUID = -4691108261565306844L;

	/**
	 * The label that displays the table caption
	 */
	private Span caption = new Span("");

	/**
	 * The data provider
	 */
	private DataProvider<T, SerializablePredicate<T>> dataProvider;

	/**
	 * Whether the grid is editable using a row editor
	 */
	private boolean editable;

	/**
	 * Field filter map
	 */
	private Map<String, SerializablePredicate<?>> fieldFilters;

	/**
	 * The wrapped grid component
	 */
	private ModelBasedGrid<ID, T> grid;

	/**
	 * The layout that contains the grid
	 */
	private VerticalLayout layout;

	/**
	 * Constructor
	 * 
	 * @param service     the service used to query the repository
	 * @param entityModel the entity model for the items that are displayed in the
	 *                    grid
	 * @param queryType   the type of query
	 * @param sortOrders  the sort order
	 * @param joins       the fetch joins to use when executing the query
	 */
	public BaseGridWrapper(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
			FormOptions formOptions, SerializablePredicate<T> filter,
			Map<String, SerializablePredicate<?>> fieldFilters, List<SortOrder<?>> sortOrders, boolean editable,
			FetchJoinInformation... joins) {
		super(service, entityModel, queryType, formOptions, filter, sortOrders, joins);
		setSpacing(false);
		this.editable = editable;
	}

	/**
	 * Builds the component.
	 */
	@Override
	public void build() {
		layout = new DefaultVerticalLayout(false, true);
		layout.add(caption);

		this.dataProvider = constructDataProvider();
		grid = getGrid();
		layout.add(grid);
		initSortingAndFiltering();

		grid.setSelectionMode(SelectionMode.SINGLE);
		add(layout);
	}

	/**
	 * Creates the container that holds the data
	 * 
	 * @return the container
	 */
	protected abstract DataProvider<T, SerializablePredicate<T>> constructDataProvider();

	/**
	 * Constructs the grid - override in subclasses if you need a different grid
	 * implementation
	 * 
	 * @return
	 */
	protected ModelBasedGrid<ID, T> constructGrid() {
		return new ModelBasedGrid<ID, T>(dataProvider, getEntityModel(), fieldFilters, editable,
				getFormOptions().getGridEditMode()) {

			private static final long serialVersionUID = -4559181057050230055L;

			@Override
			protected BindingBuilder<T, ?> doBind(T t, Component field, String attributeName) {
				return BaseGridWrapper.this.doBind(t, field, attributeName);
			}
		};
	}

	protected BindingBuilder<T, ?> doBind(T t, Component field, String attributeName) {
		return null;
	}

	/**
	 * Forces a search
	 */
	public abstract void forceSearch();

	public DataProvider<T, SerializablePredicate<T>> getDataProvider() {
		return dataProvider;
	}

	/**
	 * Lazily construct and return the grid
	 * 
	 * @return
	 */
	public ModelBasedGrid<ID, T> getGrid() {
		if (grid == null) {
			grid = constructGrid();
		}
		return grid;
	}

	public VerticalLayout getLayout() {
		return layout;
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
	@SuppressWarnings("unchecked")
	@Override
	protected List<SortOrder<?>> initSortingAndFiltering() {

		List<SortOrder<?>> fallBackOrders = super.initSortingAndFiltering();

		// set fall back sort orders
		if (dataProvider instanceof BaseDataProvider) {
			((BaseDataProvider<ID, T>) dataProvider).setFallBackSortOrders(fallBackOrders);
		}
		return fallBackOrders;
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
	 * @param container
	 */
	protected void postProcessDataProvider(DataProvider<T, SerializablePredicate<T>> provider) {
		// overwrite in subclasses
	}

	/**
	 * Reloads the data in the container
	 */
	public abstract void reloadDataProvider();

	public void setDataProvider(DataProvider<T, SerializablePredicate<T>> dataProvider) {
		this.dataProvider = dataProvider;
	}

	protected void setGrid(ModelBasedGrid<ID, T> grid) {
		this.grid = grid;
	}

	/**
	 * Updates the caption above the grid that shows the number of items
	 * 
	 * @param size
	 */
	protected void updateCaption(int size) {
		caption.setText(getEntityModel().getDisplayNamePlural(VaadinUtils.getLocale()) + " "
				+ getMessageService().getMessage("ocs.showing.results", VaadinUtils.getLocale(), size));
	}

}
