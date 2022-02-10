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
import java.util.Comparator;
import java.util.List;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.utils.ConvertUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * A grid component for the in-line editing of a one-to-many relation. It can
 * also be used to manage a many-to-many relation but in this case the
 * "setDetailsGridSearchMode" on the FormOptions must be set to true. You can
 * then use the setSearchXXX methods to configure the behaviour of the search
 * dialog that can be used to modify the values If you need a component like
 * this, you should override the constructCustomField method and use it to
 * construct a subclass of this component
 * 
 * Note that a separate instance of this component is generated for the view
 * mode and the edit mode of the form it appears in, so this component does not
 * contain logic for switching between the modes
 * 
 * @author bas.rutten
 * @param <ID> the type of the primary key
 * @param <T>  the type of the entity
 */
public class DetailsEditGrid<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseDetailsEditGrid<Collection<T>, ID, T> {

	private static final long serialVersionUID = -1203245694503350276L;

	private Comparator<T> comparator;

	private ListDataProvider<T> provider;

	/**
	 * Constructor
	 *
	 * @param entityModel    the entity model of the entities to display
	 * @param attributeModel the attribute model of the attribute to display
	 * @param viewMode       the view mode
	 * @param formOptions    the form options that determine how the grid behaves
	 */
	public DetailsEditGrid(EntityModel<T> entityModel, AttributeModel attributeModel, boolean viewMode,
			FormOptions formOptions) {
		super(null, entityModel, attributeModel, viewMode, false, formOptions);
		this.provider = new ListDataProvider<>(new ArrayList<>());
		initContent();

	}

	/**
	 * Adds a context menu used to download the contents of the grid
	 */
	protected void addDownloadMenu() {
		if (getFormOptions().isExportAllowed() && getExportDelegate() != null) {
			GridContextMenu<T> contextMenu = getGrid().addContextMenu();
			Button downloadButton = new Button(getMessageService().getMessage("ocs.download", VaadinUtils.getLocale()));
			downloadButton.addClickListener(event -> {
				ListDataProvider<T> provider = (ListDataProvider<T>) getDataProvider();
				EntityModel<T> em = getExportEntityModel() != null ? getExportEntityModel() : getEntityModel();
				getExportDelegate().exportFixed(em, getFormOptions().getExportMode(), provider.getItems());
			});
			contextMenu.add(downloadButton);
		}
	}

	@Override
	protected void applyFilter() {
		// do nothing
	}

	@Override
	protected void doAdd() {
		T t = getCreateEntitySupplier().get();
		provider.getItems().add(t);
		provider.refreshAll();
	}

	@Override
	protected void doEdit(T t) {
		// do nothing
	}

	@Override
	protected Collection<T> generateModelValue() {
		return ConvertUtils.convertCollection(provider == null ? new ArrayList<>() : provider.getItems(),
				getAttributeModel());
	}

	public Comparator<T> getComparator() {
		return comparator;
	}

	@Override
	protected DataProvider<T, SerializablePredicate<T>> getDataProvider() {
		return provider;
	}

	public int getItemCount() {
		return provider.getItems().size();
	}

	@Override
	public Collection<T> getValue() {
		return ConvertUtils.convertCollection(provider == null ? new ArrayList<>() : provider.getItems(),
				getAttributeModel());
	}

	@Override
	protected void handleDialogSelection(Collection<T> selected) {
		if (getLinkEntityConsumer() == null) {
			throw new OCSRuntimeException("No linkEntityConsumer specified!");
		}

		// add to data provider
		for (T t : selected) {
			if (!provider.getItems().contains(t)) {
				provider.getItems().add(t);
			}
		}

		// link to parent entity
		for (T t : selected) {
			getLinkEntityConsumer().accept(t);
		}
	}

	public void setComparator(Comparator<T> comparator) {
		this.comparator = comparator;
	}

	@Override
	protected void setPresentationValue(Collection<T> value) {
		List<T> list = new ArrayList<>();
		list.addAll(value);
		if (comparator != null) {
			list.sort(comparator);
		}

		getBinders().clear();
		if (provider != null) {
			provider.getItems().clear();
			provider.getItems().addAll(list);
			provider.refreshAll();
		}

		updateCaption(value == null ? 0 : value.size());

		// clear the selection
		setSelectedItem(null);
	}

	@Override
	protected boolean showDetailsPanelInEditMode() {
		return false;
	}
}
