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
import java.util.function.Supplier;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.type.ScreenMode;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.utils.FormatUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortOrder;

import lombok.Getter;
import lombok.Setter;

/**
 * Base class for a layout that contains both a results grid and a details view.
 * Based on the screen mode these can be displayed either next to each other or
 * below each other
 * 
 * @author bas.rutten
 * @param <ID> type of the primary key
 * @param <T>  type of the entity
 */
public abstract class BaseSplitLayout<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseCollectionLayout<ID, T, T> implements HasSelectedItem<T> {

	private static final long serialVersionUID = 4606800218149558500L;

	@Getter
	private Button addButton;

	private VerticalLayout detailFormLayout;

	@Getter
	private VerticalLayout detailLayout;

	@Getter
	private ModelBasedEditForm<ID, T> editForm;

	/**
	 * Additional layout that is placed above the grid
	 */
	private Component headerLayout;

	/**
	 * Supplier that is used to define a custom header layout
	 */
	@Getter
	@Setter
	private Supplier<Component> headerLayoutCreator;

	private VerticalLayout mainLayout;

	@Getter
	private TextField quickSearchField;

	@Getter
	private Button removeButton;

	/**
	 * The currently active detail layout. Can be the default edit form or a custom
	 * component
	 */
	private Component selectedDetailLayout;

	/**
	 * The grid layout that is used as the left part of the
	 */
	private VerticalLayout splitterGridLayout;

	/**
	 * Constructor
	 *
	 * @param service     the service used to query the database
	 * @param entityModel the entity model
	 * @param formOptions the form options
	 * @param sortOrder   the sort order
	 * @param joins       the joins used to query the database
	 */
	public BaseSplitLayout(BaseService<ID, T> service, EntityModel<T> entityModel, FormOptions formOptions,
			SortOrder<?> sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, formOptions, sortOrder, joins);
		setMargin(false);
		setClassName(DynamoConstants.CSS_SPLIT_LAYOUT);
	}

	/**
	 * Callback method that fires after the detail screen has been reloaded
	 *
	 * @param entity
	 */
	protected void afterReload(T entity) {
		// override in subclasses
	}

	@Override
	public void build() {
		buildFilter();
		if (mainLayout == null) {
			mainLayout = new DefaultVerticalLayout(false, true);
			mainLayout.setSizeFull();

			SplitLayout splitter = null;
			splitterGridLayout = null;

			detailLayout = new DefaultVerticalLayout(isHorizontalMode(), false);
			detailLayout.addClassName("splitLayoutDetailLayout");
			emptyDetailView();

			// construct option quick search field
			quickSearchField = constructSearchField();
			if (!isHorizontalMode() && quickSearchField != null) {
				mainLayout.add(quickSearchField);
			}

			getGridWrapper().getGrid().setHeight(getGridHeight());
			disableGridSorting();

			// extra splitter (for horizontal mode)
			if (isHorizontalMode()) {
				splitter = constructSplitterLayout();

			} else {
				// vertical mode, just add component at bottom
				mainLayout.add(getGridWrapper());
			}

			if (isHorizontalMode()) {
				splitterGridLayout.add(getButtonBar());
			} else {
				mainLayout.add(getButtonBar());
			}

			// create a panel to hold the edit form
			VerticalLayout editPanel = new DefaultVerticalLayout(false, false);
			editPanel.add(detailLayout);
			editPanel.addClassName("splitLayoutEditPanel");

			if (isHorizontalMode()) {
				// create the layout that is the right part of the splitter
				VerticalLayout extra = new DefaultVerticalLayout(false, true);
				extra.setClassName(DynamoConstants.CSS_SPLIT_LAYOUT_RIGHT);
				extra.add(editPanel);
				splitter.addToSecondary(extra);

			} else {
				mainLayout.add(editPanel);
			}

			addButton = constructAddButton();
			getButtonBar().add(addButton);

			removeButton = constructRemoveButton();
			registerComponent(removeButton);
			getButtonBar().add(removeButton);

			// allow the user to define extra buttons
			if (getPostProcessMainButtonBar() != null) {
				getPostProcessMainButtonBar().accept(getButtonBar());
			}

			if (getAfterLayoutBuilt() != null) {
				getAfterLayoutBuilt().accept(mainLayout);
			}

			checkComponentState(null);
			add(mainLayout);
		}
	}

	/**
	 * Perform any required initialization (e.g. load the required items) before
	 * attaching the screen
	 */
	protected abstract void buildFilter();

	/**
	 * Check the state of the "main" buttons (add and remove) that are not tied to
	 * the currently selected item
	 */
	protected void checkMainButtons() {
		if (getAddButton() != null) {
			getAddButton().setVisible(getFormOptions().isShowAddButton() && checkEditAllowed());
		}
		if (getRemoveButton() != null) {
			getRemoveButton().setVisible(getFormOptions().isShowRemoveButton() && checkEditAllowed());
		}
	}

	protected final Button constructRemoveButton() {
		Button removeButton = new RemoveButton(this, message("ocs.remove"), null, () -> removeEntity(),
				entity -> FormatUtils.formatEntity(getEntityModel(), entity));
		removeButton.setIcon(VaadinIcon.TRASH.create());
		removeButton.setVisible(getFormOptions().isShowRemoveButton() && checkEditAllowed());
		return removeButton;
	}

	/**
	 * Constructs the quick search field - overridden in subclasses.
	 *
	 * Do not override this method as an end user - implement the
	 * "setQuickSearchFilterSupplier" instead
	 *
	 * @return
	 */
	protected abstract TextField constructSearchField();

	private SplitLayout constructSplitterLayout() {
		SplitLayout splitter;
		splitter = new SplitLayout();

		splitter.setSizeFull();
		mainLayout.add(splitter);

		splitterGridLayout = new DefaultVerticalLayout(false, true);
		splitterGridLayout.setClassName(DynamoConstants.CSS_SPLIT_LAYOUT_LEFT);

		// optional header layout
		headerLayout = headerLayoutCreator == null ? null : headerLayoutCreator.get();
		if (headerLayout != null) {
			splitterGridLayout.add(headerLayout);
		}

		if (quickSearchField != null) {
			splitterGridLayout.add(quickSearchField);
		}

		splitterGridLayout.add(getGridWrapper());
		splitter.addToPrimary(splitterGridLayout);
		return splitter;
	}

	/**
	 * Fills the detail part of the screen with a custom component
	 *
	 * @param component the custom component that will serve as the detail view
	 */
	protected void customDetailView(Component component) {
		detailLayout.replace(selectedDetailLayout, component);
		selectedDetailLayout = component;
	}

	/**
	 * Shows the details of a selected entity
	 *
	 * @param entity the entity
	 */
	@Override
	public void detailsMode(T entity) {
		if (detailFormLayout == null) {
			detailFormLayout = new DefaultVerticalLayout(!isHorizontalMode(), false);
			detailFormLayout.addClassName("splitLayoutDetailForm");

			// canceling is not needed in the in-line view
			getFormOptions().setShowCancelButton(false).setPreserveSelectedTab(true);
			editForm = new ModelBasedEditForm<ID, T>(entity, getService(), getEntityModel(), getFormOptions(),
					getFieldFilters());

			initEditForm(editForm);
			editForm.setEditAllowed(getEditAllowed());

			editForm.setAfterEditDone((cancel, isNew, ent) -> {
				if (!cancel) {
					// update the selected item so master and detail are in sync
					// again
					reload();
					if (isNew) {
						getGridWrapper().getGrid().select(ent);
					} else {
						detailsMode(ent);
					}
				} else {
					reload();
					reloadDetails();
				}
			});

			editForm.setPostProcessButtonBar(getPostProcessDetailButtonBar());
			editForm.setDetailJoins(getDetailJoins());
			editForm.build();

			detailFormLayout.add(editForm);
		} else {
			// reset the form's view mode if needed
			editForm.setEntity(entity);
			editForm.resetTabsheetIfNeeded();
		}

		setSelectedItem(entity);
		checkComponentState(getSelectedItem());

		detailLayout.replace(selectedDetailLayout, detailFormLayout);
		selectedDetailLayout = detailFormLayout;

		if (getComponentContext().getAfterEntitySelected() != null) {
			getComponentContext().getAfterEntitySelected().accept(getEditForm(), entity);
		}
	}

	public void doSave() {
		editForm.doSave();
	}

	/**
	 * Clears the detail view
	 */
	public void emptyDetailView() {
		VerticalLayout vLayout = new DefaultVerticalLayout(true, false);
		vLayout.add(new Span(message("ocs.select.item", getEntityModel().getDisplayName(VaadinUtils.getLocale()))));
		detailLayout.replace(selectedDetailLayout, vLayout);
		selectedDetailLayout = vLayout;
	}

	/**
	 * 
	 * @return whether the screen is currently in edit mode
	 */
	public boolean isEditing() {
		return getEditForm() != null && !getEditForm().isViewMode() && getSelectedItem() != null;
	}

	/**
	 * Indicates whether the panel is in horizontal mode
	 *
	 * @return
	 */
	protected boolean isHorizontalMode() {
		return ScreenMode.HORIZONTAL.equals(getFormOptions().getScreenMode());
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		build();
	}

	@Override
	public void refresh() {
		// override in subclasses
	}

	/**
	 * Reloads the component
	 */
	@Override
	public void reload() {
		// replace the header layout (if there is one)
		Component component = headerLayoutCreator == null ? null : headerLayoutCreator.get();
		if (component != null) {
			if (headerLayout != null) {
				splitterGridLayout.replace(headerLayout, component);
			} else {
				splitterGridLayout.add(component);
			}
		} else if (headerLayout != null) {
			splitterGridLayout.remove(headerLayout);
		}
		headerLayout = component;

		if (quickSearchField != null) {
			quickSearchField.setValue("");
		}

		getGridWrapper().reloadDataProvider();

		// clear the details
		setSelectedItem(null);
		emptyDetailView();
		checkMainButtons();
	}

	/**
	 * Reloads the details view only
	 */
	public void reloadDetails() {
		this.setSelectedItem(getService().fetchById(this.getSelectedItem().getId(), getDetailJoins()));
		detailsMode(getSelectedItem());
		getGridWrapper().reloadDataProvider();
	}

	/**
	 * Remove the item and clean up the screen afterwards. Use "doRemove" if you
	 * need to do some custom functionality
	 */
	protected final void removeEntity() {
		if (getOnRemove() != null) {
			getOnRemove().run();
		}
		setSelectedItem(null);
		emptyDetailView();
		reload();
	}

	/**
	 * Reselects the specified entity
	 *
	 * @param t entity to reselect
	 */
	public void reselect(T t) {
		detailsMode(t);
		if (t == null) {
			getGridWrapper().getGrid().deselectAll();
		} else {
			getGridWrapper().getGrid().select(t);
		}
	}

	/**
	 * Replaces the contents of a label by the specified value
	 *
	 * @param path  the path of the property to update
	 * @param value the value the new value
	 */
	public void setLabelValue(String path, String value) {
		if (editForm != null) {
			editForm.setLabelValue(path, value);
		}
	}

	/**
	 * Sets the provided items as the currently selected itmes
	 * 
	 * @param selectedItems
	 */
	public abstract void setSelectedItems(Object selectedItems);

	/**
	 * Sets the mode of the screen (either view mode or edit mode)
	 * 
	 * @param viewMode the desired view mode
	 */
	public void setViewMode(boolean viewMode) {
		if (getSelectedItem() != null) {
			editForm.setViewMode(viewMode);
		}
	}

}
