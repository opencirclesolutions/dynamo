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

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.table.BaseGridWrapper;
import com.ocs.dynamo.ui.composite.table.ServiceBasedGridWrapper;
import com.ocs.dynamo.ui.container.QueryType;
import com.ocs.dynamo.ui.utils.FormatUtils;
import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Resource;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;

import java.io.Serializable;
import java.util.Collection;

/**
 * A page for editing items directly in a table - this is built around the lazy
 * query container
 *
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
@SuppressWarnings("serial")
public class TabularEditLayout<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseCollectionLayout<ID, T> {

	/**
	 * The default page length
	 */
	private static final int PAGE_LENGTH = 15;

	private static final long serialVersionUID = 4606800218149558500L;

	/**
	 * The add button
	 */
	private Button addButton;

	/**
	 * The filter that is applied to limit the search results
	 */
	private SerializablePredicate<T> filter;

	/**
	 * The main layout
	 */
	private VerticalLayout mainLayout;

	/**
	 * The page length (number of visible rows)
	 */
	private int pageLength = PAGE_LENGTH;

	/**
	 * The icon to use inside the remove button
	 */
	private Resource removeIcon;

	/**
	 * The message to display inside the "remove" button
	 */
	private String removeMessage;

	/**
	 * Whether the screen is in view mode
	 */
	private boolean viewmode;

	/**
	 * Constructor
	 *
	 * @param service
	 *            the service used to query the database
	 * @param entityModel
	 *            the entity model the entity model used to build the table
	 * @param formOptions
	 *            the form options
	 * @param sortOrder
	 *            the first sort order
	 * @param joins
	 *            the desired joins
	 */
	public TabularEditLayout(BaseService<ID, T> service, EntityModel<T> entityModel, FormOptions formOptions,
			SortOrder sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, formOptions, sortOrder, joins);
	}

	@Override
	public void attach() {
		super.attach();
		build();
	}

	/**
	 * Lazily builds the actual layout
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void build() {
		this.filter = constructFilter();
		if (mainLayout == null) {
			setViewmode(false);
			//setViewmode(!isEditAllowed() || getFormOptions().isOpenInViewMode());
			mainLayout = new DefaultVerticalLayout(true, true);

			constructTable();

			// remove button at the end of the row
			if (getFormOptions().isShowRemoveButton()) {

				final String defaultMsg = message("ocs.remove");
				getGridWrapper().getGrid().addComponentColumn(
						(ValueProvider<T, Component>) t -> isViewmode() ? null : new RemoveButton(removeMessage, removeIcon) {
							@Override
							protected void doDelete() {
								doRemove(t);
							}

							@Override
							protected String getItemToDelete() {
								return FormatUtils.formatEntity(getEntityModel(), t);
							}
						});
			}
			mainLayout.addComponent(getButtonBar());

			// add button
			addButton = new Button(message("ocs.add"));
			addButton.setIcon(VaadinIcons.PLUS);
			addButton.addClickListener(event -> {
				// delegate the construction of a new item to the lazy
				// query container
//				ID id = (ID) getDataProvider().
//						.addItem();
//				createEntity(getEntityFromTable(id));
//				getTableWrapper().getTable().setCurrentPageFirstItemId(id);
			});
			getButtonBar().addComponent(addButton);
			addButton.setVisible(!getFormOptions().isHideAddButton() && isEditAllowed() && !isViewmode());

			postProcessButtonBar(getButtonBar());
			constructTableDividers();
			postProcessLayout(mainLayout);
		}
		setCompositionRoot(mainLayout);
	}

	/**
	 * Creates the filter used for searching
	 *
	 * @return
	 */
	protected SerializablePredicate<T> constructFilter() {
		return null;
	}

	/**
	 * Initializes the table
	 */
	protected void constructTable() {

		final BaseGridWrapper grid = getGridWrapper();

		// make sure the table can be edited
		grid.getGrid().getEditor().setEnabled(!isViewmode());
		grid.getGrid().getEditor().addSaveListener(event ->
				getService().save((T)event.getBean()));
		// make sure changes are not persisted right away
		grid.getGrid().getEditor().setBuffered(true);
		grid.getGrid().setSelectionMode(Grid.SelectionMode.SINGLE);

		// default sorting
		// default sorting
//		if (getSortOrders() != null && !getSortOrders().isEmpty()) {
//			DataProvider<T, SerializablePredicate<T>> sc = getDataProvider();
//			sc.sort(getSortOrders().toArray(new SortOrder[0]));
//		}
		mainLayout.addComponent(getGridWrapper());
	}

	@Override
	protected BaseGridWrapper<ID, T> constructTableWrapper() {
		ServiceBasedGridWrapper<ID, T> tableWrapper = new ServiceBasedGridWrapper<ID,T>(getService(),
				getEntityModel(), QueryType.PAGING, filter, getSortOrders(), getFormOptions().isTableExportAllowed(), true,
				getJoins()){
			@Override
			protected void doConstructDataProvider(final DataProvider<T, SerializablePredicate<T>> provider) {
				TabularEditLayout.this.doConstructDataProvider(provider);
			}

		};
		tableWrapper.setMaxResults(getMaxResults());
		tableWrapper.build();
		return tableWrapper;
	}

	/**
	 * This method does not work for this component since the creation of a new
	 * instance is delegated to the container - use constructEntity instead
	 */
	@Override
	protected T createEntity() {
		throw new UnsupportedOperationException(
				"This method is not supported for this component - use the parameterized method instead");
	}

	/**
	 * Method that is called after a new row with a fresh entity is added to the
	 * table. Use this method to perform initialization
	 *
	 * @param entity
	 *            the newly created entity that has to be initialized
	 * @return the modified entity
	 */
	protected T createEntity(T entity) {
		return entity;
	}

	@Override
	protected void detailsMode(T entity) {
		// not needed
	}

	/**
	 * Method that is called to remove an item
	 */
	protected void doRemove(T t) {
		//getTableWrapper().getTable().removeItem(t.getId());
		//getTableWrapper().getTable().commit();
	}

	public Button getAddButton() {
		return addButton;
	}

	protected DataProvider<T, SerializablePredicate<T>> getDataProvider() {
		return getGridWrapper().getDataProvider();
	}

	/**
	 * Retrieves an entity with a certain ID from the lazy query container
	 *
	 * @param id
	 *            the ID of the entity
	 * @return
	 */
	protected T getEntityFromTable(ID id) {
		return null;// VaadinUtils.get(getContainer(), id);
	}

	@Override
	public int getPageLength() {
		return pageLength;
	}

	public Resource getRemoveIcon() {
		return removeIcon;
	}

	public String getRemoveMessage() {
		return removeMessage;
	}

	public boolean isViewmode() {
		return viewmode;
	}

//	/**
//	 * Post processes a field
//	 *
//	 * @param propertyId
//	 *            the property ID
//	 * @param field
//	 *            the generated field
//	 */
//	protected void postProcessField(Object propertyId, Field<?> field) {
//		// overwrite in subclass
//	}

	@Override
	public void refresh() {
		// override in subclasses
	}

	@Override
	public void reload() {
		getDataProvider().refreshAll();
	}

	@Override
	public void setPageLength(int pageLength) {
		this.pageLength = pageLength;
	}

	public void setRemoveIcon(Resource removeIcon) {
		this.removeIcon = removeIcon;
	}

	public void setRemoveMessage(String removeMessage) {
		this.removeMessage = removeMessage;
	}

	@SuppressWarnings("unchecked")
	public void setSelectedItems(Object selectedItems) {
		if (selectedItems != null) {
			if (selectedItems instanceof Collection<?>) {
				// the lazy query container returns an array of IDs of the
				// selected items
				Collection<?> col = (Collection<?>) selectedItems;
				ID id = (ID) col.iterator().next();
				setSelectedItem(getEntityFromTable(id));
			} else {
				ID id = (ID) selectedItems;
				setSelectedItem(getEntityFromTable(id));
			}
		} else {
			setSelectedItem(null);
		}
	}

	protected void setViewmode(boolean viewmode) {
		this.viewmode = viewmode;
	}

	/**
	 * Sets the view mode of the screen, and adapts the table and all buttons
	 * accordingly
	 *
	 * @param viewMode
	 */
	@SuppressWarnings("unchecked")
	protected void toggleViewMode(boolean viewMode) {
		setViewmode(viewMode);
		getGridWrapper().getGrid().getEditor().setEnabled(!isViewmode() && isEditAllowed());
		//saveButton.setVisible(!isViewmode());
		addButton.setVisible(!isViewmode() && !getFormOptions().isHideAddButton() && isEditAllowed());
	}

}
