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
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.dialog.EntityPopupDialog;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.provider.IdBasedDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.UI;

/**
 * A grid component that is meant for use inside an edit form. It manages a
 * lazily loaded 1-to-N or N-to-N collectionF
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the primary key of the entities
 * @param <T> the type of the entities
 * @param <ID2> the type of the bound attribute
 */
public abstract class ServiceBasedDetailsEditGrid<ID extends Serializable, T extends AbstractEntity<ID>, ID2 extends Serializable>
		extends BaseDetailsEditGrid<ID2, ID, T> {

	private static final long serialVersionUID = -1203245694503350276L;

	/**
	 * The data provider
	 */
	private IdBasedDataProvider<ID, T> provider;

	/**
	 * The parent ID
	 */
	private ID2 selectedId;

	/**
	 * The supplier for constructing the search filter used to restrict the
	 * displayed items
	 */
	private Function<ID2, SerializablePredicate<T>> filterSupplier;

	/**
	 * Code to execute after
	 */
	private Consumer<T> linkEntityConsumer;

	/**
	 * Constructor
	 *
	 * @param entityModel    the entity model of the entities to display
	 * @param attributeModel the attribute model of the attribute to display
	 * @param viewMode       the view mode
	 * @param formOptions    the form options that determine how the grid behaves
	 */
	public ServiceBasedDetailsEditGrid(BaseService<ID, T> service, EntityModel<T> entityModel,
			AttributeModel attributeModel, boolean viewMode, FormOptions formOptions, FetchJoinInformation... joins) {
		super(service, entityModel, attributeModel, viewMode, true, formOptions, joins);
		this.provider = new IdBasedDataProvider<>(service, entityModel, joins);
		provider.setAfterCountCompleted(x -> getGrid().updateCaption(x));
	}

	@Override
	protected void applyFilter() {
		SerializablePredicate<T> filter = filterSupplier == null ? null : filterSupplier.apply(selectedId);
		getGrid().getDataCommunicator().setDataProvider(provider, filter);
	}

	@Override
	protected void doAdd() {
		showPopup(null);
	}

	@Override
	protected void doEdit(T entity) {
		showPopup(entity);
	}

	@Override
	protected void doSetValue(ID2 value) {
		this.selectedId = value;
		if (getGrid() != null) {
			applyFilter();
		}
	}

	@Override
	protected DataProvider<T, SerializablePredicate<T>> getDataProvider() {
		return provider;
	}

	public Function<ID2, SerializablePredicate<T>> getFilterSupplier() {
		return filterSupplier;
	}

	public int getItemCount() {
		return provider.getSize();
	}

	public Consumer<T> getLinkEntityConsumer() {
		return linkEntityConsumer;
	}

	@Override
	public ID2 getValue() {
		return selectedId;
	}

	@Override
	protected void handleDialogSelection(Collection<T> selected) {
		for (T t : selected) {
			linkEntityConsumer.accept(t);
		}
	}

	public void setFilterSupplier(Function<ID2, SerializablePredicate<T>> filterSupplier) {
		this.filterSupplier = filterSupplier;
	}

	public void setLinkEntityConsumer(Consumer<T> linkEntityConsumer) {
		this.linkEntityConsumer = linkEntityConsumer;
	}

	/**
	 * Shows a pop-up for editing/creating the specified entity
	 * 
	 * @param entity the entity - this is empty in case a new entity is being
	 *               created
	 */
	private void showPopup(T entity) {
		EntityPopupDialog<ID, T> dialog = new EntityPopupDialog<ID, T>(getService(), entity, getEntityModel(),
				getFieldFilters(), new FormOptions()) {

			private static final long serialVersionUID = 3660359220933653009L;

			@Override
			public void afterEditDone(boolean cancel, boolean newEntity, T entity) {
				// reload so that the newly created entity shows up
				provider.refreshAll();
			}

			@Override
			protected T createEntity() {
				return getCreateEntitySupplier().get();
			}

		};
		dialog.build();
		UI.getCurrent().addWindow(dialog);
	}

}
