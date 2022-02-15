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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.CanAssignEntity;
import com.ocs.dynamo.ui.composite.dialog.EntityPopupDialog;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.provider.IdBasedDataProvider;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.Getter;
import lombok.Setter;

/**
 * A grid component that is meant for use inside an edit form. It manages a
 * lazily loaded 1-to-N or N-to-N detail collection
 * 
 * @author Bas Rutten
 *
 * @param <ID>  the type of the primary key of the entities
 * @param <T>   the type of the entities
 * @param <ID2> the type of the ID of the parent attribute
 * @param <U>   the type of the parent attribute
 */
public class ServiceBasedDetailsEditGrid<ID extends Serializable, T extends AbstractEntity<ID>, ID2 extends Serializable, U extends AbstractEntity<ID2>>
		extends BaseDetailsEditGrid<U, ID, T> implements CanAssignEntity<ID2, U> {

	private static final long serialVersionUID = -1203245694503350276L;

	/**
	 * The joins to use when exporting (needed when using export mode FULL)
	 */
	@Getter
	@Setter
	private FetchJoinInformation[] exportJoins;

	/**
	 * The search filter to apply
	 */
	private SerializablePredicate<T> filter;

	/**
	 * The supplier for constructing the search filter used to restrict the
	 * displayed items
	 */
	@Getter
	@Setter
	private Function<U, SerializablePredicate<T>> filterSupplier;

	/**
	 * The currently selected entity in the edit form that this component is part of
	 */
	private U parent;

	private IdBasedDataProvider<ID, T> provider;

	/**
	 * Constructor
	 * 
	 * @param service        the service for retrieving data from the database
	 * @param entityModel    the entity model
	 * @param attributeModel the attribute model
	 * @param viewMode       whether the component is in view mode
	 * @param formOptions    the form options that govern how the component behaves
	 * @param joins          the joins to apply when fetching data
	 */
	public ServiceBasedDetailsEditGrid(BaseService<ID, T> service, EntityModel<T> entityModel,
			AttributeModel attributeModel, boolean viewMode, FormOptions formOptions, FetchJoinInformation... joins) {
		super(service, entityModel, attributeModel, viewMode, true, formOptions);
		this.provider = new IdBasedDataProvider<>(service, entityModel, joins);
		provider.setAfterCountCompleted(count -> updateCaption(count));
		build();
	}

	protected void addDownloadMenu() {
		if (getFormOptions().isExportAllowed() && getExportDelegate() != null) {
			GridContextMenu<T> contextMenu = getGrid().addContextMenu();
			Button downloadButton = new Button(getMessageService().getMessage("ocs.download", VaadinUtils.getLocale()));
			downloadButton.addClickListener(event -> {
				List<SortOrder<?>> orders = new ArrayList<>();
				List<GridSortOrder<T>> so = getGrid().getSortOrder();
				for (GridSortOrder<T> gso : so) {
					orders.add(new SortOrder<String>(gso.getSorted().getKey(), gso.getDirection()));
				}
				applyFilter();
				EntityModel<T> em = getExportEntityModel() != null ? getExportEntityModel() : getEntityModel();
				getExportDelegate().export(em, getFormOptions().getExportMode(), filter, orders,
						getExportJoins() != null ? getExportJoins() : null);
			});
			contextMenu.add(downloadButton);
		}
	}

	/**
	 * Applies the search filter to the data provider
	 */
	@Override
	protected void applyFilter() {
		filter = (filterSupplier == null || parent == null) ? null : filterSupplier.apply(parent);

		// for a new entity without ID you can't filter yet
		if (parent != null && parent.getId() == null) {
			filter = new EqualsPredicate<>(DynamoConstants.ID, -1);
		}
		getGrid().getDataCommunicator().setDataProvider(provider, filter);
	}

	@Override
	public void assignEntity(U u) {
		this.parent = u;
		build();

		if (getGrid() != null) {
			getGrid().deselectAll();
			applyFilter();
		}

		// hide add button for new entity
		getAddButton().setVisible(!isViewMode() && !getFormOptions().isHideAddButton()
				&& !getFormOptions().isDetailsGridSearchMode() && this.parent.getId() != null);
	}

	@Override
	protected void doAdd() {
		if (this.parent.getId() != null) {
			showPopup(null);
		} else {
			// cannot add a new entity if the parent entity has not been saved yet
			VaadinUtils.showErrorNotification(
					getMessageService().getMessage("ocs.save.entity.first", VaadinUtils.getLocale()));
		}
	}

	@Override
	protected void doEdit(T entity) {
		showPopup(entity);
	}

	@Override
	protected U generateModelValue() {
		return parent;
	}

	@Override
	protected DataProvider<T, SerializablePredicate<T>> getDataProvider() {
		return provider;
	}

	public int getItemCount() {
		return provider.getSize();
	}

	@Override
	public U getValue() {
		return parent;
	}

	@Override
	protected void handleDialogSelection(Collection<T> selected) {
		if (getLinkEntityConsumer() == null) {
			throw new OCSRuntimeException("No linkEntityConsumer specified!");
		}
		selected.forEach(t -> getLinkEntityConsumer().accept(t));
	}

	@Override
	protected void setPresentationValue(U value) {
		this.parent = value;
		if (getGrid() != null) {
			getGrid().deselectAll();
			applyFilter();
			getGrid().deselectAll();
		}
	}

	/**
	 * Shows a pop-up for editing/creating the specified entity
	 * 
	 * @param entity the entity - this is empty in case a new entity is being
	 *               created
	 */
	private void showPopup(T entity) {
		EntityPopupDialog<ID, T> dialog = new EntityPopupDialog<ID, T>(getService(), entity, getEntityModel(),
				getFieldFilters(), new FormOptions(), getComponentContext(), getDetailJoins());
		dialog.setAfterEditDone((cancel, newEntity, ent) -> provider.refreshAll());
		dialog.setCreateEntitySupplier(getCreateEntitySupplier());
		dialog.buildAndOpen();
	}

	@Override
	protected boolean showDetailsPanelInEditMode() {
		return true;
	}

}
