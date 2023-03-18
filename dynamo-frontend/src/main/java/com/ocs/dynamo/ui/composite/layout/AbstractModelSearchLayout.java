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
import java.util.List;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.UIHelper;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
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

import lombok.Getter;
import lombok.Setter;

public abstract class AbstractModelSearchLayout<ID extends Serializable, T extends AbstractEntity<ID>>
		extends AbstractSearchLayout<ID, T, T> {

	private static final long serialVersionUID = -2767171519143049029L;

	@Getter
	private Button addButton;

	/**
	 * The back button that is displayed above the tab sheet in "complex details"
	 * mode
	 */
	@Getter
	private Button complexDetailModeBackButton;

	/**
	 * The title to display above the tab sheet when in "complex details" mode
	 */
	@Getter
	@Setter
	private String detailsModeTabTitle;

	@Getter
	private Button editButton;

	@Getter
	private ModelBasedEditForm<ID, T> editForm;

	private ServiceBasedGridWrapper<ID, T> gridWrapper;

	/**
	 * The main layout while the component is in details mode
	 */
	private VerticalLayout mainEditLayout;

	@Getter
	private Button nextButton;

	@Getter
	private Button prevButton;

	@Getter
	private Button removeButton;

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

	protected AbstractModelSearchLayout(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
			FormOptions formOptions, SortOrder<?> sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, queryType, formOptions, sortOrder, joins);
	}

	private void addAfterEditDone() {
		editForm.setAfterEditDone((cancel, isNew, entity) -> {
			if (getFormOptions().isOpenInViewMode()) {
				if (isNew) {
					searchMode();
				} else {
					// if details screen opens in view mode, simply switch
					// to view mode
					detailsMode(entity);
				}
			} else {
				// otherwise, go back to the main screen
				if (cancel || isNew || (!getFormOptions().isShowNextButton() && !getFormOptions().isShowPrevButton())) {
					searchMode();
				}
			}
		});
	}

	@Override
	public void addManageDetailButtons() {
		addButton = constructAddButton();
		getButtonBar().add(addButton);

		editButton = constructEditButton();
		registerComponent(editButton);
		getButtonBar().add(editButton);

		removeButton = constructRemoveButton();
		registerComponent(removeButton);
		getButtonBar().add(removeButton);
	}

	/**
	 * Builds a tab layout for the complex details mode view.
	 * 
	 * @param entity      the currently selected entity
	 * @param formOptions the form options
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
			constructPrevButton(buttonBar);
		}

		if (getFormOptions().isShowNextButton()) {
			constructNextButton(buttonBar);
		}

		tabLayout = new TabLayout<>(entity);
		tabLayout.setTitleCreator(this::getDetailsModeTabTitle);
		tabLayout.setCaptions(getDetailsModeTabCaptions());
		tabLayout.setTabCreator(index -> getDetailTabCreators().get(index).apply(formOptions, entity.getId() == null));
		tabLayout.setIconCreator(getTabIconCreator());
		tabLayout.build();
		tabContainerLayout.add(tabLayout);
	}

	/**
	 * Builds the edit form
	 * 
	 * @param entity  the currently selected entity
	 * @param options the form options
	 */
	private void buildEditForm(T entity, FormOptions options) {
		editForm = new ModelBasedEditForm<>(entity, getService(), getEntityModel(), options, getFieldFilters());

		initEditForm(editForm);
		editForm.setNextEntity(this::getNextEntity);
		editForm.setPreviousEntity(this::getPreviousEntity);
		editForm.setHasNextEntity(this::hasNextEntity);
		editForm.setHasPreviousEntity(this::hasPrevEntity);
		editForm.setOnBackButtonClicked(this::searchMode);
		addAfterEditDone();

		editForm.setSupportsIteration(true);
		editForm.setDetailJoins(getDetailJoins());
		editForm.setPostProcessButtonBar(getPostProcessDetailButtonBar());

		editForm.build();

		// TODO: is this still needed or useful?
//		if (getAfterEntitySelected() != null) {
//			editForm.setAfterEntitySelected(getAfterEntitySelected());
//		}

		editForm.setEntity(entity);
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

		ServiceBasedGridWrapper<ID, T> wrapper = new ServiceBasedGridWrapper<>(this.getService(), getEntityModel(),
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
	 * Constructs the button used to navigate to the next entity
	 * 
	 * @param buttonBar the button bar to which to add the button
	 */
	private void constructNextButton(HorizontalLayout buttonBar) {
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

	/**
	 * Constructs the button used to navigate to the previous entity
	 * 
	 * @param buttonBar the button bar to which to add the button
	 */
	private void constructPrevButton(HorizontalLayout buttonBar) {
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

	/**
	 * Open the screen in details mode
	 * 
	 * @param entity the entity to display
	 */
	@Override
	public void detailsMode(T entity) {

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
		copy.setConfirmSave(getFormOptions().isConfirmSave());

		// set the form options for the detail form
		if (getFormOptions().isShowEditButton()) {
			// editing in form must be possible
			copy.setShowEditButton(true);
		} else {
			// read-only mode
			copy.setOpenInViewMode(true).setShowEditButton(false);
		}

		if (copy.isOpenInViewMode() || !checkEditAllowed()) {
			copy.setShowBackButton(true);
		}

		if (getFormOptions().isComplexDetailsMode() && entity != null && entity.getId() != null) {
			// complex tab layout, back button is placed separately above the tab layout
			copy.setShowBackButton(false);
			copy.setShowCancelButton(false);

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
				buildEditForm(entity, copy);
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
			Component comp = getDetailTabCreators().get(0).apply(getFormOptions(), entity.getId() == null);

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
	 * @param entity           the entity to display
	 * @param selectedTabIndex the index of currently selected tab
	 */
	public void detailsMode(T entity, int selectedTabIndex) {
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
		getOnEdit().run();
		if (editForm != null) {
			editForm.selectTab(initialTab);
		} else {
			tabLayout.selectTab(initialTab);
		}
	}

	@Override
	public ServiceBasedGridWrapper<ID, T> getGridWrapper() {
		if (gridWrapper == null) {
			gridWrapper = constructGridWrapper();
		}
		return gridWrapper;
	}

	/**
	 * @return the next item that is available in the data provider
	 */
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
	 * @return the previous entity that is available in the data provider
	 */
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

	/**
	 * @return whether the data provider contains a next item
	 */
	protected boolean hasNextEntity() {
		BaseDataProvider<ID, T> provider = (BaseDataProvider<ID, T>) getGridWrapper().getDataProvider();
		return provider.hasNextItemId();
	}

	/**
	 * @return whether the data provider contains a previous item
	 */
	protected boolean hasPrevEntity() {
		BaseDataProvider<ID, T> provider = (BaseDataProvider<ID, T>) getGridWrapper().getDataProvider();
		return provider.hasPreviousItemId();
	}

	/**
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
