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

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.ComponentContext;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.provider.BaseDataProvider;
import com.ocs.dynamo.ui.provider.QueryType;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
	 * The caption that displays the table caption
	 */
	private final Span caption = new Span("");

	/**
	 * The data provider
	 */
	@Setter
	private DataProvider<T, SerializablePredicate<T>> dataProvider;

	/**
	 * Field filter map
	 */
	private final Map<String, SerializablePredicate<?>> fieldFilters;

	/**
	 * The wrapped grid component
	 */
	@Setter
	private Grid<T> grid;

	/**
	 * The layout that contains the grid
	 */
	@Getter
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
	protected BaseGridWrapper(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
			FormOptions formOptions, ComponentContext<ID, T> context, SerializablePredicate<T> filter,
			Map<String, SerializablePredicate<?>> fieldFilters, List<SortOrder<?>> sortOrders,
			FetchJoinInformation... joins) {
		super(service, entityModel, queryType, formOptions, context, filter, sortOrders, joins);
		this.fieldFilters = fieldFilters;
		setSpacing(false);
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
	 * Callback method for constructing a custom field
	 * 
	 * @param entityModel    the entity model of the main entity
	 * @param attributeModel the attribute model to base the field on
	 * @return the constructed component
	 */
	protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel) {
		return null;
	}

	/**
	 * Creates the container that holds the data
	 * 
	 * @return the container
	 */
	protected abstract DataProvider<T, SerializablePredicate<T>> constructDataProvider();

	protected Grid<T> constructGrid() {
		if (getComponentContext().isUseCheckboxesForMultiSelect()) {
			ModelBasedGrid<ID, T> newGrid = new ModelBasedGrid<>(dataProvider, getEntityModel(), fieldFilters,
					getFormOptions(), getComponentContext()) {

				private static final long serialVersionUID = -4559181057050230055L;

				@Override
				protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel) {
					return BaseGridWrapper.this.constructCustomField(entityModel, attributeModel);
				}

				@Override
				protected BindingBuilder<T, ?> doBind(T t, Component field, String attributeName) {
					return BaseGridWrapper.this.doBind(t, field, attributeName);
				}

				@Override
				protected void postProcessComponent(ID id, AttributeModel am, Component comp) {
					BaseGridWrapper.this.postProcessComponent(id, am, comp);
				}

			};

			newGrid.build();
			return newGrid;
		} else {
			ModelBasedSelectionGrid<ID, T> newGrid = new ModelBasedSelectionGrid<>(dataProvider, getEntityModel(),
					fieldFilters, getFormOptions(), getComponentContext()) {

				private static final long serialVersionUID = -4559181057050230055L;

				@Override
				protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel) {
					return BaseGridWrapper.this.constructCustomField(entityModel, attributeModel);
				}

				@Override
				protected BindingBuilder<T, ?> doBind(T t, Component field, String attributeName) {
					return BaseGridWrapper.this.doBind(t, field, attributeName);
				}

				@Override
				protected void postProcessComponent(ID id, AttributeModel am, Component comp) {
					BaseGridWrapper.this.postProcessComponent(id, am, comp);
				}

			};
			newGrid.build();
			return newGrid;
		}
	}

	protected BindingBuilder<T, ?> doBind(T entity, Component field, String attributeName) {
		return null;
	}

	/**
	 * Forces a search
	 */
	public abstract void forceSearch();

	public DataProvider<T, SerializablePredicate<T>> getDataProvider() {
		return dataProvider;
	}

	public Grid<T> getGrid() {
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
	protected List<SortOrder<?>> initSortingAndFiltering() {

		List<SortOrder<?>> fallbackOrders = super.initSortingAndFiltering();

		// set fall back sort orders
		if (dataProvider instanceof BaseDataProvider) {
			((BaseDataProvider<ID, T>) dataProvider).setFallbackSortOrders(fallbackOrders);
		}
		return fallbackOrders;
	}

	/**
	 * Responds to a selection of an item in the grid
	 */
	protected void onSelect(Object selected) {
		// overwrite in subclasses
	}

	/**
	 * Callback method used to post process any component
	 * @param id the ID of the entity
	 * @param attributeModel the attribute model
	 * @param component the component
	 */
	protected void postProcessComponent(ID id, AttributeModel attributeModel, Component component) {
		// overwrite in subclasses
	}

	/**
	 * Callback method used to modify data provider creation
	 * 
	 * @param provider the data provider
	 */
	protected void postProcessDataProvider(DataProvider<T, SerializablePredicate<T>> provider) {
		// overwrite in subclasses
	}

	/**
	 * Reloads the data in the container
	 */
	public abstract void reloadDataProvider();

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
