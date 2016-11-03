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

import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Reloadable;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.type.ScreenMode;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * Base class for a layout that contains both a results table and a details view. Based on the
 * screen mode these can be displayed either next to each other or below each other
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
@SuppressWarnings("serial")
public abstract class BaseSplitLayout<ID extends Serializable, T extends AbstractEntity<ID>> extends
        BaseCollectionLayout<ID, T> implements Reloadable {

	private static final long serialVersionUID = 4606800218149558500L;

	// the add button
	private Button addButton;

	// the form layout that is nested inside the detail view
	private Layout detailFormLayout;

	// the layout that contains the form/detail view
	private Layout detailLayout;

	// the edit form
	private ModelBasedEditForm<ID, T> editForm;

	// layout that is placed above the table view
	private Component headerLayout;

	// the main layout
	private VerticalLayout mainLayout;

	// quick search filed for filtering the table
	private TextField quickSearchField;

	// the remove button
	private Button removeButton;

	// the currently selected detail layout (can be either edit mode or read-only mode)
	private Component selectedDetailLayout;

	/**
	 * Constructor
	 * 
	 * @param service
	 *            the service used to query the database
	 * @param entityModel
	 *            the entity model
	 * @param formOptions
	 *            the form options
	 * @param sortOrder
	 *            the sort order
	 * @param joins
	 *            the joins used to query the database
	 */
	public BaseSplitLayout(BaseService<ID, T> service, EntityModel<T> entityModel, FormOptions formOptions,
	        SortOrder sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, formOptions, sortOrder, joins);
	}

	/**
	 * Perform any actions after the screen reloads after a save. This is usually used to reselect
	 * the item that was selected before
	 * 
	 * @param entity
	 */
	protected abstract void afterReload(T entity);

	@Override
	public void attach() {
		super.attach();
		init();
		build();
	}

	/**
	 * Builds the component
	 */
	@Override
	public void build() {
		mainLayout = new DefaultVerticalLayout(true, true);

		HorizontalSplitPanel splitter = null;
		VerticalLayout splitterLayout = null;

		detailLayout = new DefaultVerticalLayout();
		emptyDetailView();

		// optional header
		headerLayout = constructHeaderLayout();
		if (headerLayout != null) {
			mainLayout.addComponent(headerLayout);
		}

		// construct option quick search field
		quickSearchField = constructSearchField();

		// additional quick search field
		if (!isHorizontalMode()) {
			if (quickSearchField != null) {
				mainLayout.addComponent(quickSearchField);
			}
		}

		// table init
		getTableWrapper().getTable().setPageLength(getPageLength());
		getTableWrapper().getTable().setSortEnabled(isSortEnabled());
		constructTableDividers();

		// extra splitter (for horizontal mode)
		if (isHorizontalMode()) {
			splitter = new HorizontalSplitPanel();
			mainLayout.addComponent(splitter);

			splitterLayout = new DefaultVerticalLayout(false, true);
			if (quickSearchField != null) {
				splitterLayout.addComponent(quickSearchField);
			}

			splitterLayout.addComponent(getTableWrapper());
			splitter.setFirstComponent(splitterLayout);
		} else {
			mainLayout.addComponent(getTableWrapper());
		}

		if (isHorizontalMode()) {
			splitterLayout.addComponent(getButtonBar());
		} else {
			mainLayout.addComponent(getButtonBar());
		}

		// create a panel to hold the edit form
		Panel editPanel = new Panel();
		editPanel.setContent(detailLayout);

		if (isHorizontalMode()) {
			// create the layout that is the right part of the splitter
			VerticalLayout extra = new DefaultVerticalLayout(true, false);
			extra.addComponent(editPanel);
			splitter.setSecondComponent(extra);
		} else {
			mainLayout.addComponent(editPanel);
		}

		addButton = constructAddButton();
		if (addButton != null) {
			getButtonBar().addComponent(addButton);
		}

		removeButton = constructRemoveButton();
		if (removeButton != null) {
			registerButton(removeButton);
			getButtonBar().addComponent(removeButton);
		}

		// allow the user to define extra buttons
		postProcessButtonBar(getButtonBar());

		postProcessLayout(mainLayout);

		checkButtonState(null);
		setCompositionRoot(mainLayout);
	}

	public abstract void setSelectedItems(Object selectedItems);

	/**
	 * Constructs a header layout (displayed above the actual tabular content)
	 * 
	 * @return
	 */
	protected Component constructHeaderLayout() {
		return null;
	}

	/**
	 * Constructs the remove button
	 */
	protected Button constructRemoveButton() {
		Button rb = new RemoveButton() {

			@Override
			protected void doDelete() {
				remove();
			}
		};
		rb.setVisible(getFormOptions().isShowRemoveButton() && isEditAllowed());
		return rb;
	}

	/**
	 * Constructs an extra quick search field - delegate to subclasses for implementation
	 * 
	 * @param parent
	 */
	protected abstract TextField constructSearchField();

	/**
	 * Fills the detail part of the screen with a custom component
	 * 
	 * @param component
	 */
	protected void customDetailView(Component component) {
		detailLayout.replaceComponent(selectedDetailLayout, component);
		selectedDetailLayout = component;
	}

	/**
	 * Shows the details of a selected entity
	 * 
	 * @param parent
	 *            the parent of the entity
	 * @param entity
	 *            the entity
	 */
	@Override
	protected void detailsMode(T entity) {
		if (detailFormLayout == null) {
			detailFormLayout = new DefaultVerticalLayout(false, false);

			// canceling is not needed in the inline view
			getFormOptions().setHideCancelButton(true);

			editForm = new ModelBasedEditForm<ID, T>(entity, getService(), getEntityModel(), getFormOptions(),
			        getFieldFilters()) {

				@Override
				protected void afterEditDone(boolean cancel, boolean newObject, T entity) {
					// update the selected item so master and detail are in sync
					// again
					setSelectedItem(entity);
					reload();
					afterReload(entity);
				}

				@Override
				protected void afterEntitySet(T entity) {
					BaseSplitLayout.this.afterEntitySet(entity);
				}

				@Override
				protected void afterModeChanged(boolean viewMode) {
					BaseSplitLayout.this.afterModeChanged(viewMode, editForm);
				}

				@Override
				protected Field<?> constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
				        boolean viewMode) {
					return BaseSplitLayout.this.constructCustomField(entityModel, attributeModel, viewMode, false);
				}

				@Override
				protected boolean isEditAllowed() {
					return BaseSplitLayout.this.isEditAllowed();
				}

				@Override
				protected void postProcessButtonBar(HorizontalLayout buttonBar, boolean viewMode) {
					BaseSplitLayout.this.postProcessDetailButtonBar(buttonBar, viewMode);
				}

				@Override
				protected void postProcessEditFields() {
					BaseSplitLayout.this.postProcessEditFields(editForm);
				}

			};

			editForm.setDetailJoins(getDetailJoins());
			editForm.setFieldEntityModels(getFieldEntityModels());
			editForm.build();
			detailFormLayout.addComponent(editForm);
		} else {
			// reset the form's view mode if needed
			editForm.setViewMode(getFormOptions().isOpenInViewMode());
			editForm.setEntity(entity);
		}

		checkButtonState(getSelectedItem());
		afterDetailSelected(editForm, entity);

		detailLayout.replaceComponent(selectedDetailLayout, detailFormLayout);
		selectedDetailLayout = detailFormLayout;
	}

	/**
	 * Performs the actual remove functionality - overwrite in subclass if needed
	 */
	protected void doRemove() {
		getService().delete(getSelectedItem());
	}

	/**
	 * Clears the detail view
	 */
	public void emptyDetailView() {
		VerticalLayout vLayout = new VerticalLayout();
		vLayout.addComponent(new Label(message("ocs.inline.select.item")));

		detailLayout.replaceComponent(selectedDetailLayout, vLayout);
		selectedDetailLayout = vLayout;
	}

	public Button getAddButton() {
		return addButton;
	}

	public Layout getDetailLayout() {
		return detailLayout;
	}

	public ModelBasedEditForm<ID, T> getEditForm() {
		return editForm;
	}

	public Button getRemoveButton() {
		return removeButton;
	}

	/**
	 * Perform any required initialization (e.g. load the required items) before attaching the
	 * screen
	 */
	protected abstract void init();

	/**
	 * Indicates whether the panel is in horizontal mode
	 * 
	 * @return
	 */
	protected boolean isHorizontalMode() {
		return ScreenMode.HORIZONTAL.equals(getFormOptions().getScreenMode());
	}

	@Override
	public void reload() {
		// replace the header layout (if there is one)
		Component component = constructHeaderLayout();
		if (component != null) {
			if (headerLayout != null) {
				mainLayout.replaceComponent(headerLayout, component);
			} else {
				mainLayout.addComponent(component, 0);
			}
		} else if (headerLayout != null) {
			mainLayout.removeComponent(headerLayout);
		}
		headerLayout = component;

		if (quickSearchField != null) {
			quickSearchField.setValue("");
		}

		// refresh the details
		if (getSelectedItem() != null) {
			detailsMode(getSelectedItem());
		}
	}

	/**
	 * Reloads the details view only
	 */
	public void reloadDetails() {
		this.setSelectedItem(getService().fetchById(this.getSelectedItem().getId(), getDetailJoins()));
		detailsMode(getSelectedItem());
		getTableWrapper().reloadContainer();
	}

	/**
	 * Remove the item and clean up the screen afterwards. Do not override. Use "doRemove" if you
	 * need to do some custom functionality
	 */
	protected final void remove() {
		doRemove();
		setSelectedItem(null);
		emptyDetailView();
		reload();
	}

	/**
	 * Replaces the contents of a label by its current value. Use in response to an automatic update
	 * if a field
	 * 
	 * @param propertyName
	 *            the name of the property for which to replace the label
	 */
	public void replaceLabel(String propertyName) {
		if (editForm != null) {
			editForm.replaceLabel(propertyName);
		}
	}

	/**
	 * Sets the mode of the screen (either view mode or edit mode)
	 * 
	 * @param viewMode
	 *            the desired view mode
	 */
	public void setViewMode(boolean viewMode) {
		if (getSelectedItem() != null) {
			editForm.setViewMode(viewMode);
		}
	}

	public TextField getQuickSearchField() {
		return quickSearchField;
	}
}
