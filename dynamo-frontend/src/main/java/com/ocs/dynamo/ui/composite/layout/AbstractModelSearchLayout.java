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
import java.util.List;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.UIHelper;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.grid.ComponentContext;
import com.ocs.dynamo.ui.composite.grid.ServiceBasedGridWrapper;
import com.ocs.dynamo.ui.composite.type.ScreenMode;
import com.ocs.dynamo.ui.provider.BaseDataProvider;
import com.ocs.dynamo.ui.provider.QueryType;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

public abstract class AbstractModelSearchLayout<ID extends Serializable, T extends AbstractEntity<ID>>
		extends AbstractSearchLayout<ID, T, T> {

	private static final long serialVersionUID = -2767171519143049029L;

	/**
	 * Button for adding new items. Displayed by default
	 */
	private Button addButton;

	/**
	 * The back button that is displayed above the tab sheet in "complex details"
	 * mode
	 */
	private Button complexDetailModeBackButton;

	/**
	 * The edit button
	 */
	private Button editButton;

//	/**
//	 * Edit column width thresholds
//	 */
//	private List<String> editColumnThresholds = new ArrayList<>();

	/**
	 * The edit form for editing a single object
	 */
	private ModelBasedEditForm<ID, T> editForm;

	/**
	 * The grid wrapper
	 */
	private ServiceBasedGridWrapper<ID, T> gridWrapper;

	/**
	 * The main layout (in edit mode)
	 */
	private VerticalLayout mainEditLayout;

	/**
	 * Button for selecting the next item
	 */
	private Button nextButton;

	/**
	 * Button for selecting the previous item
	 */
	private Button prevButton;

	/**
	 * The remove button
	 */
	private Button removeButton;

	/**
	 * The selected detail layout
	 */
	private Component selectedDetailLayout;

	/**
	 * The layout that holds the tab sheet when the component is in complex details
	 * mode
	 */
	private VerticalLayout tabContainerLayout;

	/**
	 * Tab layout for complex detail mode
	 */
	private TabLayout<ID, T> tabLayout;

	public AbstractModelSearchLayout(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
			FormOptions formOptions, SortOrder<?> sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, queryType, formOptions, sortOrder, joins);
	}

	@Override
	public void addManageDetailButtons() {
		// add button
		addButton = constructAddButton();
		getButtonBar().add(addButton);

		// edit/view button
		editButton = constructEditButton();
		registerComponent(editButton);
		getButtonBar().add(editButton);

		// remove button
		removeButton = constructRemoveButton();
		registerComponent(removeButton);
		getButtonBar().add(removeButton);
	}

	/**
	 * Builds a tab layout for the display view. The definition of the tabs has to
	 * be done in the subclasses
	 * 
	 * @param entity the currently selected entity
	 */
	private void buildDetailsTabLayout(T entity, FormOptions formOptions) {
		tabContainerLayout = new DefaultVerticalLayout(false, false);

		HorizontalLayout buttonBar = new DefaultHorizontalLayout(true, true);
		tabContainerLayout.add(buttonBar);

		complexDetailModeBackButton = new Button(message("ocs.back"));
		complexDetailModeBackButton.setIcon(VaadinIcon.BACKWARDS.create());
		complexDetailModeBackButton.addClickListener(e -> searchMode());
		buttonBar.add(complexDetailModeBackButton);

		if (getFormOptions().isShowPrevButton()) {
			prevButton = new Button(message("ocs.previous"));
			prevButton.setIcon(VaadinIcon.ARROW_LEFT.create());
			prevButton.addClickListener(e -> {
				T prev = getPreviousEntity();
				if (prev != null) {
					tabLayout.setEntity(prev, getFormOptions().isPreserveSelectedTab());
					// tabLayout.reload();
				} else {
					prevButton.setEnabled(false);
				}
				if (nextButton != null) {
					nextButton.setEnabled(true);
				}
			});
			prevButton.setEnabled(hasPrevEntity());
			buttonBar.add(prevButton);
		}

		if (getFormOptions().isShowNextButton()) {
			nextButton = new Button(message("ocs.next"));
			nextButton.setIcon(VaadinIcon.ARROW_RIGHT.create());
			nextButton.addClickListener(e -> {
				T next = getNextEntity();
				if (next != null) {
					tabLayout.setEntity(next, getFormOptions().isPreserveSelectedTab());
				} else {
					nextButton.setEnabled(false);
				}
				if (prevButton != null) {
					prevButton.setEnabled(true);
				}
			});
			nextButton.setEnabled(hasNextEntity());
			buttonBar.add(nextButton);
		}

		tabLayout = new TabLayout<ID, T>(entity);

		tabLayout.setTitleSupplier(() -> getDetailModeTabTitle());
		tabLayout.setCaptions(getDetailModeTabCaptions());
		tabLayout.setTabSupplier(index -> constructComplexDetailModeTab(index, formOptions, false));
		tabLayout.setIconSupplier(index -> getIconForTab(index));
		tabLayout.build();
		tabContainerLayout.add(tabLayout);
	}

	/**
	 * Builds the edit form
	 * 
	 * @param entity  the currently selected entity
	 * @param options the form options
	 */
	private void buildEditForm(T entity, FormOptions options, ComponentContext<ID,T> context) {
		editForm = new ModelBasedEditForm<ID, T>(entity, getService(), getEntityModel(), options, getFieldFilters()) {

			private static final long serialVersionUID = 6485097089659928131L;

			@Override
			protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
					boolean viewMode) {
				return AbstractModelSearchLayout.this.constructCustomField(entityModel, attributeModel, viewMode,
						false);
			}

			@Override
			protected T getNextEntity() {
				return AbstractModelSearchLayout.this.getNextEntity();
			}

			@Override
			protected T getPreviousEntity() {
				return AbstractModelSearchLayout.this.getPreviousEntity();
			}

			@Override
			protected boolean hasNextEntity() {
				return AbstractModelSearchLayout.this.hasNextEntity();
			}

			@Override
			protected boolean hasPrevEntity() {
				return AbstractModelSearchLayout.this.hasPrevEntity();
			}

//			@Override
//			protected boolean isEditAllowed() {
//				return AbstractModelSearchLayout.this.isEditAllowed();
//			}

		};

		initEditForm(editForm);

		editForm.setOnBackButtonClicked(() -> searchMode());
		addAfterEditDone();

		editForm.setSupportsIteration(true);
		editForm.setDetailJoins(getDetailJoins());
		editForm.setPostProcessButtonBar(getPostProcessDetailButtonBar());

		editForm.build();

//		if (getAfterEntitySelected() != null) {
//			editForm.setAfterEntitySelected(getAfterEntitySelected());
//		}

		editForm.setEntity(entity);
	}

	private void addAfterEditDone() {
		editForm.setAfterEditDone((cancel, isNew, ent) -> {
			if (getFormOptions().isOpenInViewMode()) {
				if (isNew) {
					searchMode();
				} else {
					// if details screen opens in view mode, simply switch
					// to view mode
					// editForm.setViewMode(true);
					detailsMode(ent);
				}
			} else {
				// otherwise go back to the main screen
				if (cancel || isNew || (!getFormOptions().isShowNextButton() && !getFormOptions().isShowPrevButton())) {
					searchMode();
				}
			}
		});
	}

	/**
	 * Lazily constructs the grid wrapper
	 */
	@Override
	public ServiceBasedGridWrapper<ID, T> constructGridWrapper() {

		// restore stored sort orders
		UIHelper helper = ServiceLocatorFactory.getServiceLocator().getService(UIHelper.class);
		if (helper != null) {
			List<SortOrder<?>> retrievedOrders = helper.retrieveSortOrders();
			if (!getComponentContext().isPopup() && getFormOptions().isPreserveSortOrders() && retrievedOrders != null
					&& !retrievedOrders.isEmpty()) {
				setSortOrders(retrievedOrders);
			}
		}

		ServiceBasedGridWrapper<ID, T> wrapper = new ServiceBasedGridWrapper<ID, T>(this.getService(), getEntityModel(),
				getQueryType(), getFormOptions(), getComponentContext(), getSearchForm().extractFilter(),
				getFieldFilters(), getSortOrders(), false, getJoins()) {

			private static final long serialVersionUID = 6343267378913526151L;

			@Override
			protected SerializablePredicate<T> beforeSearchPerformed(SerializablePredicate<T> filter) {
				return AbstractModelSearchLayout.this.beforeSearchPerformed(filter);
			}

			@Override
			protected void postProcessDataProvider(DataProvider<T, SerializablePredicate<T>> provider) {
				AbstractModelSearchLayout.this.postProcessDataProvider(provider);
			}

		};
		postConfigureGridWrapper(wrapper);

		wrapper.setMaxResults(getMaxResults());
		wrapper.build();

		if (getFormOptions().isSearchImmediately()) {
			getSearchForm().setSearchable(wrapper);
		}

		return wrapper;
	}

	/**
	 * Open the screen in details mode
	 * 
	 * @param entity the entity to display
	 */
	@Override
	protected void detailsMode(T entity) {

		if (mainEditLayout == null) {
			mainEditLayout = new DefaultVerticalLayout(true, false);
			mainEditLayout.addClassName(DynamoConstants.CSS_MAIN_EDIT_LAYOUT);
		}

		FormOptions copy = new FormOptions();
		copy.setOpenInViewMode(getFormOptions().isOpenInViewMode());
		copy.setScreenMode(ScreenMode.VERTICAL);
		copy.setAttributeGroupMode(getFormOptions().getAttributeGroupMode());
		copy.setPreserveSelectedTab(getFormOptions().isPreserveSelectedTab());
		copy.setShowNextButton(getFormOptions().isShowNextButton());
		copy.setShowPrevButton(getFormOptions().isShowPrevButton());
		copy.setPlaceButtonBarAtTop(getFormOptions().isPlaceButtonBarAtTop());
		// copy.setFormNested(true);
		copy.setConfirmSave(getFormOptions().isConfirmSave());

		// set the form options for the detail form
		if (getFormOptions().isEditAllowed()) {
			// editing in form must be possible
			copy.setEditAllowed(true);
		} else {
			// read-only mode
			copy.setOpenInViewMode(true).setEditAllowed(false);
		}

		if (copy.isOpenInViewMode() || !checkEditAllowed()) {
			copy.setShowBackButton(true);
		}

		if (getFormOptions().isComplexDetailsMode() && entity != null && entity.getId() != null) {
			// complex tab layout, back button is placed separately
			copy.setShowBackButton(false);
			copy.setHideCancelButton(true);

			if (tabContainerLayout == null) {
				buildDetailsTabLayout(entity, copy);
			} else {
				tabLayout.setEntity(entity, getFormOptions().isPreserveSelectedTab());
			}
			if (selectedDetailLayout == null) {
				mainEditLayout.add(tabContainerLayout);
			} else {
				mainEditLayout.replace(selectedDetailLayout, tabContainerLayout);
			}
			selectedDetailLayout = tabContainerLayout;
		} else if (!getFormOptions().isComplexDetailsMode()) {
			// simple edit form
			if (editForm == null) {
				buildEditForm(entity, copy, getComponentContext().toBuilder().formNested(true).build());
			} else {
				editForm.setViewMode(copy.isOpenInViewMode());
				editForm.setEntity(entity);
				editForm.resetTabsheetIfNeeded();
			}
			if (selectedDetailLayout == null) {
				mainEditLayout.add(editForm);
			} else {
				mainEditLayout.replace(selectedDetailLayout, editForm);
			}
			selectedDetailLayout = editForm;
		} else {
			// complex details mode for creating a new entity, re-use the first tab
			Component comp = constructComplexDetailModeTab(0, getFormOptions(), true);

			if (selectedDetailLayout == null) {
				mainEditLayout.add(comp);
			} else {
				mainEditLayout.replace(selectedDetailLayout, comp);
			}
			selectedDetailLayout = comp;
		}

		checkComponentState(getSelectedItem());
		removeAll();
		add(mainEditLayout);
	}

	/**
	 * Opens the screen in details mode and selects a certain tab
	 *
	 * @param entity      the entity to display
	 * @param selectedTab the currently selected tab
	 */
	protected void detailsMode(T entity, int selectedTabIndex) {
		detailsMode(entity);
		if (editForm != null) {
			editForm.selectTab(selectedTabIndex);
		} else if (getFormOptions().isComplexDetailsMode()) {
			tabLayout.selectTab(selectedTabIndex);
		}
	}

	/**
	 * Open in edit mode and select the tab with the provided index
	 *
	 * @param entity     the entity to select
	 * @param initialTab the index of the tab to display
	 */
	public final void edit(T entity, int initialTab) {
		setSelectedItem(entity);
		doEdit();
		if (editForm != null) {
			editForm.selectTab(initialTab);
		} else {
			tabLayout.selectTab(initialTab);
		}
	}

	public Button getAddButton() {
		return addButton;
	}

	public Button getComplexDetailModeBackButton() {
		return complexDetailModeBackButton;
	}

	public Button getEditButton() {
		return editButton;
	}

//	public List<String> getEditColumnThresholds() {
//		return editColumnThresholds;
//	}

	public ModelBasedEditForm<ID, T> getEditForm() {
		return editForm;
	}

	@Override
	public ServiceBasedGridWrapper<ID, T> getGridWrapper() {
		if (gridWrapper == null) {
			gridWrapper = constructGridWrapper();
		}
		return gridWrapper;
	}

	/**
	 * Returns the next item that is available in the data provider
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected final T getNextEntity() {
		BaseDataProvider<ID, T> provider = (BaseDataProvider<ID, T>) getGridWrapper().getDataProvider();
		ID nextId = provider.getNextItemId();
		T next = null;
		if (nextId != null) {
			next = getService().fetchById(nextId, getDetailJoins());
			getGridWrapper().getGrid().select(next);
		}
		return next;
	}

	/**
	 * Returns the previous entity that is available in the data provider
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected final T getPreviousEntity() {
		BaseDataProvider<ID, T> provider = (BaseDataProvider<ID, T>) getGridWrapper().getDataProvider();
		ID prevId = provider.getPreviousItemId();
		T prev = null;
		if (prevId != null) {
			prev = getService().fetchById(prevId, getDetailJoins());
			getGridWrapper().getGrid().select(prev);
		}
		return prev;
	}

	public Button getRemoveButton() {
		return removeButton;
	}

	/**
	 * Check whether the data provider contains a next item
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected boolean hasNextEntity() {
		BaseDataProvider<ID, T> provider = (BaseDataProvider<ID, T>) getGridWrapper().getDataProvider();
		return provider.hasNextItemId();
	}

	/**
	 * Check whether the data provider contains a previous item
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected boolean hasPrevEntity() {
		BaseDataProvider<ID, T> provider = (BaseDataProvider<ID, T>) getGridWrapper().getDataProvider();
		return provider.hasPreviousItemId();
	}

	/**
	 * 
	 * @return whether the user is currently editing/adding an item
	 */
	public boolean isEditing() {
		if (isInSearchMode()) {
			return false;
		}
		if (!getFormOptions().isComplexDetailsMode()) {
			return getEditForm() != null && !getEditForm().isViewMode();
		}
		return false;
	}

	/**
	 * Sets the tab specified by the provided index to the provided visibility (for
	 * use in complex details mode)
	 *
	 * @param index   the index
	 * @param visible whether the tabs must be visible
	 */
	public void setDetailsTabVisible(int index, boolean visible) {
		if (tabLayout != null) {
			tabLayout.setTabVisible(index, visible);
		}
	}

//	public void setEditColumnThresholds(List<String> editColumnThresholds) {
//		this.editColumnThresholds = editColumnThresholds;
//	}

	/**
	 * Refreshes the contents of a label inside the edit form
	 * 
	 * @param propertyName the name of the property for which to refresh the label
	 */
	public void setLabelValue(String propertyName, String value) {
		if (editForm != null) {
			editForm.setLabelValue(propertyName, value);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setSelectedItem(T selectedItem) {
		super.setSelectedItem(selectedItem);
		// communicate selected item ID to provider
		BaseDataProvider<ID, T> provider = (BaseDataProvider<ID, T>) getGridWrapper().getDataProvider();
		provider.setCurrentlySelectedId(selectedItem == null ? null : selectedItem.getId());
		if (prevButton != null) {
			prevButton.setEnabled(hasPrevEntity());
		}
		if (nextButton != null) {
			nextButton.setEnabled(hasNextEntity());
		}
	}

}
