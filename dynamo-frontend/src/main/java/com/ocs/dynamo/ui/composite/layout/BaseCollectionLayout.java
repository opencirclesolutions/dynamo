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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.Reloadable;
import com.ocs.dynamo.ui.component.DefaultFlexLayout;
import com.ocs.dynamo.ui.composite.grid.GridWrapper;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.Getter;
import lombok.Setter;

/**
 * A base class for a composite layout that displays a collection of data
 * (rather than a single object)
 * 
 * @author bas.rutten
 * @param <ID> the type of the primary key
 * @param <T>  the type of the entity
 */
public abstract class BaseCollectionLayout<ID extends Serializable, T extends AbstractEntity<ID>, U>
		extends BaseServiceCustomComponent<ID, T> implements Reloadable, Refreshable {

	private static final long serialVersionUID = -2864711994829582000L;

	/**
	 * The main button bar that appears below the search results grid
	 */
	@Getter
	private final FlexLayout buttonBar = new DefaultFlexLayout();

	/**
	 * The relations to fetch when retrieving a single entity
	 */
	@Getter
	private FetchJoinInformation[] detailJoins;

	/**
	 * The entity model to use when exporting to CSV or Excel. Defaults to the
	 * regular model if not set
	 */
	@Getter
	@Setter
	private EntityModel<T> exportEntityModel;

	/**
	 * The relations to fetch when doing an export with export mode FULL
	 */
	@Getter
	private FetchJoinInformation[] exportJoins;

	/**
	 * The search filters to apply to the individual fields
	 */
	@Getter
	private final Map<String, SerializablePredicate<?>> fieldFilters = new HashMap<>();

	/**
	 * The default height of the grid
	 */
	@Getter
	@Setter
	private String gridHeight = SystemPropertyUtils.getDefaultGridHeight();

	private GridWrapper<ID, T, U> gridWrapper;

	/**
	 * The joins to use when retrieving data for the grid
	 */
	@Getter
	private final FetchJoinInformation[] joins;

	/**
	 * The maximum number of search results
	 */
	@Getter
	@Setter
	private Integer maxResults;

	/**
	 * The code that is carried out to add additional buttons to the button bar of
	 * the edit form
	 */
	@Getter
	@Setter
	private BiConsumer<FlexLayout, Boolean> postProcessDetailButtonBar;

	/**
	 * The code that is carried out to post process the grid wrapper
	 */
	@Getter
	@Setter
	private Consumer<GridWrapper<ID, T, U>> postProcessGridWrapper;

	/**
	 * The code that is carried out after the entire layout has been built
	 */
	@Getter
	@Setter
	private Consumer<VerticalLayout> afterLayoutBuilt;

	/**
	 * The code that is carried out to add additional buttons to the main button bar
	 * (below the search results)
	 */
	@Getter
	@Setter
	private Consumer<FlexLayout> postProcessMainButtonBar;

	/**
	 * The currently selected item (in the grid)
	 */
	@Getter
	private T selectedItem;

	/**
	 * Whether the results grid can be sorted by clicking on the column headers
	 */
	@Getter
	@Setter
	private boolean sortEnabled = true;

	/**
	 * The list of sort orders that are applied by default
	 */
	@Setter
	private List<SortOrder<?>> sortOrders = new ArrayList<>();

	@Getter
	@Setter
	private Runnable onAdd = () -> {
		setSelectedItem(doCreateEntity());
		detailsMode(getSelectedItem());
	};

	@Getter
	@Setter
	private Runnable onRemove = () -> getService().delete(getSelectedItem());

	/**
	 * Constructor
	 * 
	 * @param service     the service
	 * @param entityModel the entity model
	 * @param formOptions the form options
	 * @param sortOrder   the sort order
	 * @param joins       the joins to use when fetching data
	 */
	protected BaseCollectionLayout(BaseService<ID, T> service, EntityModel<T> entityModel, FormOptions formOptions,
			SortOrder<?> sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, formOptions);
		this.joins = joins;
		if (sortOrder != null) {
			sortOrders.add(sortOrder);
		}
	}

	/**
	 * Adds a field filter
	 * 
	 * @param property the property for which to add a field filter
	 * @param filter   the field filter
	 */
	public void addFieldFilter(String property, SerializablePredicate<?> filter) {
		this.fieldFilters.put(property, filter);
	}

	/**
	 * Adds a sort order
	 * 
	 * @param sortOrder the sort order that must be added
	 */
	public final void addSortOrder(SortOrder<?> sortOrder) {
		this.sortOrders.add(sortOrder);
	}

	/**
	 * Throws away the grid wrapper, making sure it is reconstructed the next time
	 * the layout is displayed
	 */
	public void clearGridWrapper() {
		this.gridWrapper = null;
	}

	/**
	 * Constructs the Add button that is used to open the form in "new entity" mode
	 * 
	 * @return the constructed button
	 */
	protected final Button constructAddButton() {
		Button ab = new Button(message("ocs.add"));
		ab.setIcon(VaadinIcon.PLUS.create());
		ab.addClickListener(e -> {
			checkComponentState(null);
			onAdd.run();
		});
		ab.setVisible(getFormOptions().isShowAddButton() && checkEditAllowed());
		return ab;
	}

	/**
	 * Lazily constructs the grid wrapper - used by the framework in order to
	 * construct the appropriate grid wrapper
	 * 
	 * @return the grid wrapper
	 */
	protected abstract GridWrapper<ID, T, U> constructGridWrapper();

	/**
	 * Creates a new entity - override in subclass if needed
	 * 
	 * @return the created entity
	 */
	protected final T doCreateEntity() {
		return getCreateEntity() != null ? getCreateEntity().get() : getService().createNewEntity();
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
	protected final void disableGridSorting() {
		if (!isSortEnabled()) {
			for (Column<?> c : getGridWrapper().getGrid().getColumns()) {
				c.setSortable(false);
			}
		}
	}

	public GridWrapper<ID, T, U> getGridWrapper() {
		if (gridWrapper == null) {
			gridWrapper = constructGridWrapper();

			if (postProcessGridWrapper != null) {
				postProcessGridWrapper.accept(gridWrapper);
			}
		}
		return gridWrapper;
	}

	/**
	 * @return the currently configured sort orders
	 */
	public final List<SortOrder<?>> getSortOrders() {
		return Collections.unmodifiableList(sortOrders);
	}

	/**
	 * Shared additional configuration after the grid wrapper has been created.
	 * 
	 * @param wrapper the wrapper
	 */
	protected final void postConfigureGridWrapper(GridWrapper<ID, T, U> wrapper) {
		wrapper.setExportEntityModel(getExportEntityModel());
		wrapper.setExportJoins(getExportJoins());
	}

	/**
	 * Callback method that fires after the provider has been created. Override this
	 * to modify data provider construction. This should rarely be needed.
	 * 
	 * @param provider the provider
	 */
	protected void postProcessDataProvider(DataProvider<U, SerializablePredicate<U>> provider) {
		// override in subclasses
	}

	/**
	 * Sets the provided item as the currently selected item in the grid
	 * 
	 * @param selectedItem the item that you want to become the selected item
	 */
	public void setSelectedItem(T selectedItem) {
		this.selectedItem = selectedItem;
		checkComponentState(selectedItem);
	}

	public void setDetailJoins(FetchJoinInformation... detailJoins) {
		this.detailJoins = detailJoins;
	}

	public void setExportJoins(FetchJoinInformation... exportJoins) {
		this.exportJoins = exportJoins;
	}

}
