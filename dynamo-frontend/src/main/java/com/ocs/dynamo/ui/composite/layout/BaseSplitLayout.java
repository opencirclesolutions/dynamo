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

import com.ocs.dynamo.constants.DynamoConstants;
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
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.provider.SortOrder;

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
		extends BaseCollectionLayout<ID, T, T> {

	private static final long serialVersionUID = 4606800218149558500L;

	private Button addButton;

	private VerticalLayout detailFormLayout;

	/**
	 * The standard detail layout
	 */
	private VerticalLayout detailLayout;

	private VerticalLayout splitterLayout;

	private ModelBasedEditForm<ID, T> editForm;

	/**
	 * Additional layout that is placed above the grid
	 */
	private Component headerLayout;

	private VerticalLayout mainLayout;

	private TextField quickSearchField;

	private Button removeButton;

	/**
	 * The currently active detail layout. Can be the default edit form or a custom
	 * component
	 */
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
		setMargin(false);
		setClassName(DynamoConstants.CSS_SPLIT_LAYOUT);
	}

	/**
	 * Callback method that fires after the detail screen has been reloaded
	 *
	 * @param entity
	 */
	protected void afterReload(T entity) {
		// override in subclass
	}

	/**
	 * Builds the component
	 */
	@Override
	public void build() {
		buildFilter();
		if (mainLayout == null) {
			mainLayout = new DefaultVerticalLayout(false, true);
			mainLayout.setSizeFull();

			SplitLayout splitter = null;
			splitterLayout = null;

			detailLayout = new DefaultVerticalLayout(false, true);
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
				splitter = new SplitLayout();

				splitter.setSizeFull();
				mainLayout.add(splitter);

				splitterLayout = new DefaultVerticalLayout(false, true);

				// optional header layout
				headerLayout = constructHeaderLayout();
				if (headerLayout != null) {
					splitterLayout.add(headerLayout);
				}

				if (quickSearchField != null) {
					splitterLayout.add(quickSearchField);
				}

				splitterLayout.add(getGridWrapper());
				splitter.addToPrimary(splitterLayout);

			} else {
				// vertical mode, just add component at bottom
				mainLayout.add(getGridWrapper());
			}

			if (isHorizontalMode()) {
				splitterLayout.add(getButtonBar());
			} else {
				mainLayout.add(getButtonBar());
			}

			// create a panel to hold the edit form
			VerticalLayout editPanel = new DefaultVerticalLayout(true, false);
			editPanel.add(detailLayout);

			if (isHorizontalMode()) {
				// create the layout that is the right part of the splitter
				VerticalLayout extra = new DefaultVerticalLayout(false, true);
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
			postProcessButtonBar(getButtonBar());
			postProcessLayout(mainLayout);

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
				removeEntity();
			}

			@Override
			protected String getItemToDelete() {
				T t = getSelectedItem();
				return FormatUtils.formatEntity(getEntityModel(), t);
			}
		};
		rb.setIcon(VaadinIcon.TRASH.create());
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
						reloadDetails();
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
				protected <U, V> Converter<U, V> constructCustomConverter(AttributeModel am) {
					return BaseSplitLayout.this.constructCustomConverter(am);
				}

				@Override
				protected <V> Validator<V> constructCustomValidator(AttributeModel am) {
					return BaseSplitLayout.this.constructCustomValidator(am);
				}

				@Override
				protected <V> Validator<V> constructCustomRequiredValidator(AttributeModel am) {
					return BaseSplitLayout.this.constructCustomRequiredValidator(am);
				}

				@Override
				protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
						boolean viewMode) {
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
				protected void postProcessButtonBar(FlexLayout buttonBar, boolean viewMode) {
					BaseSplitLayout.this.postProcessDetailButtonBar(buttonBar, viewMode);
				}

				@Override
				protected void postProcessEditFields() {
					BaseSplitLayout.this.postProcessEditFields(editForm);
				}

			};

			editForm.setCustomSaveConsumer(getCustomSaveConsumer());
			editForm.setDetailJoins(getDetailJoins());
			editForm.setFieldEntityModels(getFieldEntityModels());
			editForm.build();
			detailFormLayout.add(editForm);
		} else {
			// reset the form's view mode if needed
			editForm.setEntity(entity);
			editForm.resetTabsheetIfNeeded();
		}

		setSelectedItem(entity);
		checkComponentState(getSelectedItem());
		afterEntitySelected(editForm, entity);

		detailLayout.replace(selectedDetailLayout, detailFormLayout);
		selectedDetailLayout = detailFormLayout;
	}

	/**
	 * Performs the actual remove functionality - overwrite in subclass if needed
	 */
	protected void doRemove() {
		getService().delete(getSelectedItem());
	}

	public void doSave() {
		editForm.doSave();
	}

	/**
	 * Clears the detail view
	 */
	public void emptyDetailView() {
		VerticalLayout vLayout = new VerticalLayout();
		vLayout.add(new Span(message("ocs.select.item", getEntityModel().getDisplayName(VaadinUtils.getLocale()))));
		detailLayout.replace(selectedDetailLayout, vLayout);
		selectedDetailLayout = vLayout;
	}

	public Button getAddButton() {
		return addButton;
	}

	public VerticalLayout getDetailLayout() {
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
		Component component = constructHeaderLayout();
		if (component != null) {
			if (headerLayout != null) {
				splitterLayout.replace(headerLayout, component);
			} else {
				splitterLayout.add(component);
			}
		} else if (headerLayout != null) {
			splitterLayout.remove(headerLayout);
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
		doRemove();
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
