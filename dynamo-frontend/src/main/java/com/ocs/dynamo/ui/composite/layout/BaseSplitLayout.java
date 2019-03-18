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

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.type.ScreenMode;
import com.ocs.dynamo.ui.utils.FormatUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.Converter;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * Base class for a layout that contains both a results grid and a details view.
 * Based on the screen mode these can be displayed either next to each other or
 * below each other
 * 
 * @author bas.rutten
 * @param <ID> type of the primary key
 * @param <T> type of the entity
 */
public abstract class BaseSplitLayout<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseCollectionLayout<ID, T> {

	private static final long serialVersionUID = 4606800218149558500L;

	// the add button
	private Button addButton;

	// default split position (width of first component in percent)
	private Integer defaultSplitPosition;

	// the form layout that is nested inside the detail view
	private Layout detailFormLayout;

	// the layout that contains the form/detail view
	private Layout detailLayout;

	// the edit form
	private ModelBasedEditForm<ID, T> editForm;

	// layout that is placed above the grid view
	private Component headerLayout;

	// the main layout
	private VerticalLayout mainLayout;

	// quick search filed for filtering the grid
	private TextField quickSearchField;

	// the remove button
	private Button removeButton;

	// the currently selected detail layout (can be either edit mode or
	// read-only mode)
	private Component selectedDetailLayout;

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
	}

	/**
	 * Perform any actions after the screen reloads after an entity was saved.
	 * Override in subclasses if needed
	 *
	 * @param entity
	 */
	protected void afterReload(T entity) {
		// override in subclass
	}

	@Override
	public void attach() {
		super.attach();
		build();
	}

	/**
	 * Builds the component
	 */
	@Override
	public void build() {
		buildFilter();
		if (mainLayout == null) {
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
			if (!isHorizontalMode() && quickSearchField != null) {
				mainLayout.addComponent(quickSearchField);
			}

			// initialize the grid
			getGridWrapper().getGrid().setHeightByRows(getPageLength());
			disableGridSorting();
			constructGridDividers();

			// extra splitter (for horizontal mode)
			if (isHorizontalMode()) {
				splitter = new HorizontalSplitPanel();
				mainLayout.addComponent(splitter);

				splitterLayout = new DefaultVerticalLayout(false, true);
				if (quickSearchField != null) {
					splitterLayout.addComponent(quickSearchField);
				}

				splitterLayout.addComponent(getGridWrapper());
				splitter.setFirstComponent(splitterLayout);

				if (defaultSplitPosition != null) {
					splitter.setSplitPosition(defaultSplitPosition);
				}
			} else {
				mainLayout.addComponent(getGridWrapper());
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
			getButtonBar().addComponent(addButton);

			removeButton = constructRemoveButton();
			registerComponent(removeButton);
			getButtonBar().addComponent(removeButton);

			// allow the user to define extra buttons
			postProcessButtonBar(getButtonBar());

			postProcessLayout(mainLayout);

			checkButtonState(null);
			setCompositionRoot(mainLayout);
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
			getAddButton().setVisible(!getFormOptions().isHideAddButton() && isEditAllowed());
		}
		if (getRemoveButton() != null) {
			getRemoveButton().setVisible(getFormOptions().isShowRemoveButton() && isEditAllowed());
		}
	}

	/**
	 * Constructs a header layout (displayed above the actual tabular content). By
	 * default this is empty, overwrite in subclasses if you want to modify this
	 *
	 * @return
	 */
	protected Component constructHeaderLayout() {
		return null;
	}

	/**
	 * Constructs the remove button
	 */
	protected final Button constructRemoveButton() {
		Button rb = new RemoveButton(message("ocs.remove"), null) {

			private static final long serialVersionUID = 6489940330122182935L;

			@Override
			protected void doDelete() {
				remove();
			}

			@Override
			protected String getItemToDelete() {
				T t = getSelectedItem();
				return FormatUtils.formatEntity(getEntityModel(), t);
			}
		};
		rb.setIcon(VaadinIcons.TRASH);
		rb.setVisible(getFormOptions().isShowRemoveButton() && isEditAllowed());
		return rb;
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

	/**
	 * Fills the detail part of the screen with a custom component
	 *
	 * @param component the custom component
	 */
	protected void customDetailView(Component component) {
		detailLayout.replaceComponent(selectedDetailLayout, component);
		selectedDetailLayout = component;
	}

	/**
	 * Shows the details of a selected entity
	 *
	 * @param parent the parent of the entity
	 * @param entity the entity
	 */
	@Override
	public void detailsMode(T entity) {
		if (detailFormLayout == null) {
			detailFormLayout = new DefaultVerticalLayout(false, false);

			// canceling is not needed in the in-line view
			getFormOptions().setHideCancelButton(true).setPreserveSelectedTab(true);
			editForm = new ModelBasedEditForm<ID, T>(entity, getService(), getEntityModel(), getFormOptions(),
					getFieldFilters()) {

				private static final long serialVersionUID = 6642035999999009278L;

				@Override
				protected void afterEditDone(boolean cancel, boolean newObject, T entity) {
					if (!cancel) {
						// update the selected item so master and detail are in sync
						// again
						reload();
						detailsMode(entity);
						afterReload(entity);
					} else {
						reload();
					}
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
				protected void afterTabSelected(int tabIndex) {
					BaseSplitLayout.this.afterTabSelected(tabIndex);
				}

				@Override
				protected Converter<String, ?> constructCustomConverter(AttributeModel am) {
					return BaseSplitLayout.this.constructCustomConverter(am);
				}

				@Override
				protected AbstractComponent constructCustomField(EntityModel<T> entityModel,
						AttributeModel attributeModel, boolean viewMode) {
					return BaseSplitLayout.this.constructCustomField(entityModel, attributeModel, viewMode, false);
				}

				@Override
				protected String getParentGroup(String childGroup) {
					return BaseSplitLayout.this.getParentGroup(childGroup);
				}

				@Override
				protected String[] getParentGroupHeaders() {
					return BaseSplitLayout.this.getParentGroupHeaders();
				}

				@Override
				protected boolean handleCustomException(RuntimeException ex) {
					return BaseSplitLayout.this.handleCustomException(ex);
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

			editForm.setCustomSaveConsumer(getCustomSaveConsumer());
			editForm.setFormTitleWidth(getFormTitleWidth());
			editForm.setDetailJoins(getDetailJoins());
			editForm.setFieldEntityModels(getFieldEntityModels());
			editForm.build();
			detailFormLayout.addComponent(editForm);
		} else {
			// reset the form's view mode if needed
			editForm.setEntity(entity);
			editForm.resetTab();
		}

		setSelectedItem(entity);
		checkButtonState(getSelectedItem());
		afterEntitySelected(editForm, entity);

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
		vLayout.addComponent(
				new Label(message("ocs.select.item", getEntityModel().getDisplayName(VaadinUtils.getLocale()))));
		detailLayout.replaceComponent(selectedDetailLayout, vLayout);
		selectedDetailLayout = vLayout;
	}

	public Button getAddButton() {
		return addButton;
	}

	public Integer getDefaultSplitPosition() {
		return defaultSplitPosition;
	}

	public Layout getDetailLayout() {
		return detailLayout;
	}

	public ModelBasedEditForm<ID, T> getEditForm() {
		return editForm;
	}

	public TextField getQuickSearchField() {
		return quickSearchField;
	}

	public Button getRemoveButton() {
		return removeButton;
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
	public void refresh() {
		// override in subclasses
	}

	/**
	 * Replaces the contents of a label by its current value. Use in response to an
	 * automatic update if a field
	 *
	 * @param propertyName the name of the property for which to replace the label
	 */
	public void refreshLabel(String propertyName) {
		if (editForm != null) {
			editForm.refreshLabel(propertyName);
		}
	}

	/**
	 * Reloads the component
	 */
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
	 * Remove the item and clean up the screen afterwards. Do not override. Use
	 * "doRemove" if you need to do some custom functionality
	 */
	protected final void remove() {
		doRemove();
		setSelectedItem(null);
		emptyDetailView();
		reload();
	}

	/**
	 * Reselects the entity
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

	public void setDefaultSplitPosition(Integer defaultSplitPosition) {
		this.defaultSplitPosition = defaultSplitPosition;
	}

	/**
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
