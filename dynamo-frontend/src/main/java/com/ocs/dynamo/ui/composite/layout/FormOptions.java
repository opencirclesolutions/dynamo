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

import com.ocs.dynamo.ui.composite.type.AttributeGroupMode;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.ocs.dynamo.ui.composite.type.GridEditMode;
import com.ocs.dynamo.ui.composite.type.ScreenMode;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.vaadin.flow.component.grid.Grid.SelectionMode;

/**
 * Parameter object that can be passed along when creating a page - this object
 * uses smart defaults so that in most cases, you do not have to set the
 * properties individually
 * 
 * @author bas.rutten
 */
public class FormOptions implements Serializable {

	private static final long serialVersionUID = 7383335254540591298L;

	/**
	 * How to display the various attribute groups. The default is PANEL (related
	 * fields are shown in a panel, the panels are placed below each other), but it
	 * can be changed to TABSHEET (related attributes are placed on separate tabs)
	 */
	private AttributeGroupMode attributeGroupMode = AttributeGroupMode.PANEL;

	/**
	 * Whether to use a complex details mode that contains multiple tabs (only
	 * supported for AbstractSearchLayout and subclasses)
	 */
	private boolean complexDetailsMode = false;

	/**
	 * Whether the user has to confirm the clearing of the search form
	 */
	private boolean confirmClear;

	/**
	 * Whether to ask for confirmation before saving
	 */
	private boolean confirmSave;

	/**
	 * Whether to display the buttons in a DetailsEditLayout on the same row as the
	 * form
	 */
	private boolean detailsEditLayoutButtonsOnSameRow = true;

	/**
	 * Whether to display a details grid in search mode
	 */
	private boolean detailsGridSearchMode;

	/**
	 * The selection mode to use in the grid
	 */
	private SelectionMode detailsGridSelectionMode = SelectionMode.SINGLE;

	/**
	 * Whether the columns in a details edit grid are sortable
	 */
	private boolean detailsGridSortable = SystemPropertyUtils.getDefaultDetailsGridSortable();

	/**
	 * Whether details mode is enabled in a search layout
	 */
	private boolean detailsModeEnabled = true;

	/**
	 * Whether you can navigate to a detail screen by double clicking on a row in a
	 * results grid
	 */
	private boolean doubleClickSelectAllowed = true;

	/**
	 * Whether to show an edit button when the screen is opened in view mode
	 */
	private boolean showEditButton = true;

	/**
	 * Whether to enabled advanced search mode in a search layout
	 */
	private boolean enableAdvancedSearchMode;

	/**
	 * Indicates whether export is allowed (read from system property)
	 */
	private boolean exportAllowed = SystemPropertyUtils.allowListExport();

	/**
	 * The data export mode - by default, export only the attributes that are
	 * visible in the grid
	 */
	private ExportMode exportMode = ExportMode.ONLY_VISIBLE_IN_GRID;

	/**
	 * The grid edit mode (either all rows at once or one row at a time) inside an
	 * EditableGridLayout
	 */
	private GridEditMode gridEditMode = GridEditMode.SINGLE_ROW;

	/**
	 * Whether to show the add button
	 */
	private boolean showAddButton = true;

	/**
	 * Whether to show the cancel button (in an explicit detail view)
	 */
	private boolean showCancelButton = true;

	/**
	 * Whether to show the clear button in the search form
	 */
	private boolean showClearButton = true;;

	/**
	 * 
	 * Whether to open the form in view (read-only) mode
	 */
	private boolean openInViewMode;

	/**
	 * Whether to place the button bar at the top of the title label (rather than
	 * behind it)
	 */
	private boolean placeButtonBarAtTop = true;

	/**
	 * Whether to preserve advanced search mode when navigation away from the screen
	 */
	private boolean preserveAdvancedMode = true;

	/**
	 * Whether to preserve search terms when navigating away from the screen
	 */
	private boolean preserveSearchTerms = true;

	/**
	 * Whether to preserve the last selected tab when reopening a screen
	 */
	private boolean preserveSelectedTab;

	/**
	 * Whether to preserve sort orders when navigating away from the screen
	 */
	private boolean preserveSortOrders = true;

	/**
	 * Whether to display the screen in complete read-only mode. Settings readOnly
	 * to true will automatically adapt all other necessary settings (e.g. it will
	 * set "openInViewMode" to true)
	 */
	private boolean readOnly;

	/**
	 * The orientation of the screen (horizontal or vertical). This is relevant for
	 * the split layout. In the HORIZONTAL view the grid and form are displayed next
	 * to each other, in the VERTICAL view they are below each other
	 */
	private ScreenMode screenMode = ScreenMode.HORIZONTAL;

	/**
	 * Whether to search immediately when opening a search screen
	 */
	private boolean searchImmediately = true;

	/**
	 * Whether to display a back button inside an edit form. Usually managed by the
	 * framework.
	 */
	private boolean showBackButton;

	/**
	 * Whether to show a caption above an edit form
	 */
	private boolean showEditFormCaption = true;

	/**
	 * Whether to display a panel containing the item that is selected in a grid,
	 * below that grid
	 */
	private boolean showDetailsGridDetailsPanel;

	/**
	 * Whether to display a "next" button inside an edit form
	 */
	private boolean showNextButton;

	/**
	 * 
	 * Whether to display a "previous" button inside an edit form
	 */
	private boolean showPrevButton;

	/**
	 * Whether to include an quick search field
	 */
	private boolean showQuickSearchField;

	/**
	 * Whether to show a refresh button in an edit form
	 */
	private boolean showRefreshButton;

	/**
	 * Whether or not to add a "remove" button - by default this is disabled
	 */
	private boolean showRemoveButton;

	/**
	 * Whether to show an or button to enable match any search
	 */
	private boolean showSearchAnyButton;

	/**
	 * Whether to display a button for toggling
	 */
	private boolean showToggleButton;

	/**
	 * Whether to start the search screen in advanced mode
	 */
	private boolean startInAdvancedMode;

	public FormOptions createCopy() {
		FormOptions fo = new FormOptions();
		fo.setAttributeGroupMode(getAttributeGroupMode());
		fo.setComplexDetailsMode(isComplexDetailsMode());
		fo.setConfirmClear(isConfirmClear());
		fo.setDetailsGridSearchMode(isDetailsGridSearchMode());
		fo.setDoubleClickSelectAllowed(isDoubleClickSelectAllowed());
		fo.setShowEditButton(isShowEditButton());
		fo.setShowAddButton(isShowAddButton());
		fo.setShowCancelButton(isShowCancelButton());
		fo.setShowClearButton(isShowClearButton());
		fo.setOpenInViewMode(isOpenInViewMode());
		fo.setPlaceButtonBarAtTop(isPlaceButtonBarAtTop());
		fo.setPreserveSelectedTab(isPreserveSelectedTab());
		fo.setScreenMode(getScreenMode());
		fo.setSearchImmediately(isSearchImmediately());
		fo.setShowBackButton(isShowBackButton());
		fo.setShowNextButton(isShowNextButton());
		fo.setShowPrevButton(isShowPrevButton());
		fo.setShowQuickSearchField(isShowQuickSearchField());
		fo.setShowRemoveButton(isShowRemoveButton());
		fo.setShowSearchAnyButton(isShowSearchAnyButton());
		fo.setShowToggleButton(isShowToggleButton());
		fo.setExportAllowed(isExportAllowed());
		fo.setConfirmSave(isConfirmSave());
		fo.setExportMode(getExportMode());
		fo.setGridEditMode(getGridEditMode());
		fo.setDetailsModeEnabled(isDetailsModeEnabled());
		fo.setEnableAdvancedSearchMode(isEnableAdvancedSearchMode());
		fo.setStartInAdvancedMode(isStartInAdvancedMode());
		fo.setPreserveSearchTerms(isPreserveSearchTerms());
		fo.setPreserveSortOrders(isPreserveSortOrders());
		fo.setShowEditFormCaption(isShowEditFormCaption());
		fo.setDetailsEditLayoutButtonsOnSameRow(isDetailsEditLayoutButtonsOnSameRow());
		fo.setPreserveAdvancedMode(isPreserveAdvancedMode());
		fo.setShowDetailsGridDetailsPanel(isShowDetailsGridDetailsPanel());
		return fo;
	}

	public AttributeGroupMode getAttributeGroupMode() {
		return attributeGroupMode;
	}

	public SelectionMode getDetailsGridSelectionMode() {
		return detailsGridSelectionMode;
	}

	public ExportMode getExportMode() {
		return exportMode;
	}

	public GridEditMode getGridEditMode() {
		return gridEditMode;
	}

	public ScreenMode getScreenMode() {
		return screenMode;
	}

	public boolean isComplexDetailsMode() {
		return complexDetailsMode;
	}

	public boolean isConfirmClear() {
		return confirmClear;
	}

	public boolean isConfirmSave() {
		return confirmSave;
	}

	public boolean isDetailsEditLayoutButtonsOnSameRow() {
		return detailsEditLayoutButtonsOnSameRow;
	}

	public boolean isDetailsGridSearchMode() {
		return detailsGridSearchMode;
	}

	public boolean isDetailsGridSortable() {
		return detailsGridSortable;
	}

	public boolean isDetailsModeEnabled() {
		return detailsModeEnabled;
	}

	public boolean isDoubleClickSelectAllowed() {
		return doubleClickSelectAllowed;
	}

	public boolean isShowEditButton() {
		return showEditButton;
	}

	public boolean isEnableAdvancedSearchMode() {
		return enableAdvancedSearchMode;
	}

	public boolean isExportAllowed() {
		return exportAllowed;
	}

	public boolean isShowAddButton() {
		return showAddButton;
	}

	public boolean isShowCancelButton() {
		return showCancelButton;
	}

	public boolean isShowClearButton() {
		return showClearButton;
	}

	public boolean isOpenInViewMode() {
		return openInViewMode;
	}

	public boolean isPlaceButtonBarAtTop() {
		return placeButtonBarAtTop;
	}

	public boolean isPreserveAdvancedMode() {
		return preserveAdvancedMode;
	}

	public boolean isPreserveSearchTerms() {
		return preserveSearchTerms;
	}

	public boolean isPreserveSelectedTab() {
		return preserveSelectedTab;
	}

	public boolean isPreserveSortOrders() {
		return preserveSortOrders;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public boolean isSearchImmediately() {
		return searchImmediately;
	}

	public boolean isShowBackButton() {
		return showBackButton;
	}

	public boolean isShowEditFormCaption() {
		return showEditFormCaption;
	}

	public boolean isShowDetailsGridDetailsPanel() {
		return showDetailsGridDetailsPanel;
	}

	public boolean isShowNextButton() {
		return showNextButton;
	}

	public boolean isShowPrevButton() {
		return showPrevButton;
	}

	public boolean isShowQuickSearchField() {
		return showQuickSearchField;
	}

	public boolean isShowRefreshButton() {
		return showRefreshButton;
	}

	public boolean isShowRemoveButton() {
		return showRemoveButton;
	}

	public boolean isShowSearchAnyButton() {
		return showSearchAnyButton;
	}

	public boolean isShowToggleButton() {
		return showToggleButton;
	}

	public boolean isStartInAdvancedMode() {
		return startInAdvancedMode;
	}

	/**
	 * Sets the desired attribute group mode. This determines whether input
	 * components will be grouped in panels (default) or tabs
	 * 
	 * @param attributeGroupMode the desired attribute group mode
	 * @return
	 */
	public FormOptions setAttributeGroupMode(AttributeGroupMode attributeGroupMode) {
		this.attributeGroupMode = attributeGroupMode;
		return this;
	}

	/**
	 * Specify whether complex details mode is enabled. In complex details mode,
	 * 
	 * @param complexDetailsMode whether to enable complexDetailsMode
	 * @return
	 */
	public FormOptions setComplexDetailsMode(boolean complexDetailsMode) {
		this.complexDetailsMode = complexDetailsMode;
		return this;
	}

	/**
	 * Specify whether to ask for confirmation before clearing the search form
	 * 
	 * @param confirmClear whether to ask for confirmation
	 * @return
	 */
	public FormOptions setConfirmClear(boolean confirmClear) {
		this.confirmClear = confirmClear;
		return this;
	}

	/**
	 * Specify whether to ask for confirmation before saving changes after the Save
	 * button has been clicked
	 * 
	 * @param confirmSave whether to ask for confirmation
	 * @return
	 */
	public FormOptions setConfirmSave(boolean confirmSave) {
		this.confirmSave = confirmSave;
		return this;
	}

	/**
	 * Specify whether to display the button bar on the same row as the input
	 * components inside a DetailsEditLayout
	 * 
	 * @param detailsEditLayoutButtonsOnSameRow
	 * @return
	 */
	public FormOptions setDetailsEditLayoutButtonsOnSameRow(boolean detailsEditLayoutButtonsOnSameRow) {
		this.detailsEditLayoutButtonsOnSameRow = detailsEditLayoutButtonsOnSameRow;
		return this;
	}

	/**
	 * Specify whether a DetailsEditGrid behaves in read-only mode and includes a
	 * search form for selecting values.
	 * 
	 * @param detailsGridSearchMode whether to active search mode
	 * @return
	 */
	public FormOptions setDetailsGridSearchMode(boolean detailsGridSearchMode) {
		this.detailsGridSearchMode = detailsGridSearchMode;
		return this;
	}

	/**
	 * Specify the grid selection mode for a details grid
	 * 
	 * @param detailsGridSelectionMode the desired selection mode
	 * @return
	 */
	public FormOptions setDetailsGridSelectionMode(SelectionMode detailsGridSelectionMode) {
		this.detailsGridSelectionMode = detailsGridSelectionMode;
		return this;
	}

	/**
	 * Specify whether the rows in a DetailsGrid must be sortable
	 * 
	 * @param detailsGridSortable the desired sortable setting
	 * @return
	 */
	public FormOptions setDetailsGridSortable(boolean detailsGridSortable) {
		this.detailsGridSortable = detailsGridSortable;
		return this;
	}

	/**
	 * Specify whether the details screen is enabled for a search layout
	 * 
	 * @param detailsModeEnabled whether the details screen is enabled
	 * @return
	 */
	public FormOptions setDetailsModeEnabled(boolean detailsModeEnabled) {
		this.detailsModeEnabled = detailsModeEnabled;
		return this;
	}

	/**
	 * Specify whether you can navigate to a detail screen by double clicking on a
	 * row in the search results grid in a SearchLayout
	 * 
	 * @param doubleClickSelectAllowed whether double click selection is allowed
	 * @return
	 */
	public FormOptions setDoubleClickSelectAllowed(boolean doubleClickSelectAllowed) {
		this.doubleClickSelectAllowed = doubleClickSelectAllowed;
		return this;
	}

	/**
	 * Specify whether editing existing entities is allowed. When set to "true" an
	 * edit button will be rendered
	 * 
	 * @param showEditButton whether editing is allowed
	 * @return
	 */
	public FormOptions setShowEditButton(boolean showEditButton) {
		this.showEditButton = showEditButton;
		return this;
	}

	/**
	 * Whether to enable advanced search mode for search layouts
	 * 
	 * @param enableAdvancedSearchMode whether advanced search mode is enabled
	 * @return
	 */
	public FormOptions setEnableAdvancedSearchMode(boolean enableAdvancedSearchMode) {
		this.enableAdvancedSearchMode = enableAdvancedSearchMode;
		return this;
	}

	/**
	 * Specify whether exporting the data inside results grids to Excel or CSV is
	 * allowed
	 * 
	 * @param exportAllowed the desired export setting
	 * @return
	 */
	public FormOptions setExportAllowed(boolean exportAllowed) {
		this.exportAllowed = exportAllowed;
		return this;
	}

	/**
	 * Specify the desired export mode: FULL (export all visible properties) or
	 * VISIBLE_IN_GRID (only export the properties that are shown in the grid - this
	 * is the default)
	 * 
	 * @param exportMode
	 * @return
	 */
	public FormOptions setExportMode(ExportMode exportMode) {
		this.exportMode = exportMode;
		return this;
	}

	/**
	 * Set the desired edit mode for EditableGridLayout components. This can be
	 * either SINGLE_ROW (default) or SIMULTANEOUS (edit multiple rows at once)
	 * 
	 * @param gridEditMode
	 * @return
	 */
	public FormOptions setGridEditMode(GridEditMode gridEditMode) {
		this.gridEditMode = gridEditMode;
		return this;
	}

	/**
	 * Specify whether to show the Add button
	 * 
	 * @param showAddButton whether to show the add button
	 * @return
	 */
	public FormOptions setShowAddButton(boolean showAddButton) {
		this.showAddButton = showAddButton;
		return this;
	}

	/**
	 * Specify whether to show the Cancel button in places in which it would
	 * normally appear
	 * 
	 * @param showCancelButton whether to show the cancel button
	 * @return
	 */
	public FormOptions setShowCancelButton(boolean showCancelButton) {
		this.showCancelButton = showCancelButton;
		return this;
	}

	/**
	 * Specify whether to show Clear button in search forms
	 * 
	 * @param showClearButton whether to show the clear button
	 * @return
	 */
	public FormOptions setShowClearButton(boolean showClearButton) {
		this.showClearButton = showClearButton;
		return this;
	}

	/**
	 * Specify whether to open details screens in view mode. This defaults to
	 * <code>false</code>
	 * 
	 * @param openInViewMode whether to open the edit screen in view mode
	 * @return
	 */
	public FormOptions setOpenInViewMode(boolean openInViewMode) {
		this.openInViewMode = openInViewMode;
		return this;
	}

	/**
	 * Specify whether to place the button bar above the title of the edit screen.
	 * This defaults to <code>true</code>
	 * 
	 * @param placeButtonBarAtTop
	 * @return
	 */
	public FormOptions setPlaceButtonBarAtTop(boolean placeButtonBarAtTop) {
		this.placeButtonBarAtTop = placeButtonBarAtTop;
		return this;
	}

	/**
	 * Whether to preserve the "advanced search" mode when navigating away from a
	 * screen and then returning to it
	 * 
	 * @param preserveAdvancedMode
	 * @return
	 */
	public FormOptions setPreserveAdvancedMode(boolean preserveAdvancedMode) {
		this.preserveAdvancedMode = preserveAdvancedMode;
		return this;
	}

	/**
	 * Whether to preserve search terms when navigating away from a screen and then
	 * returning to it
	 * 
	 * @param preserveSearchTerms
	 * @return
	 */
	public FormOptions setPreserveSearchTerms(boolean preserveSearchTerms) {
		this.preserveSearchTerms = preserveSearchTerms;
		return this;
	}

	/**
	 * Specify whether to preserve the selected tab when switching between entities
	 * using the Previous and Next buttons
	 * 
	 * @param preserveSelectedTab
	 * @return
	 */
	public FormOptions setPreserveSelectedTab(boolean preserveSelectedTab) {
		this.preserveSelectedTab = preserveSelectedTab;
		return this;
	}

	/**
	 * Whether to preserve the sort order when navigating away from a screen and
	 * then returning to it
	 * 
	 * @param preserveSortOrders
	 * @return
	 */
	public FormOptions setPreserveSortOrders(boolean preserveSortOrders) {
		this.preserveSortOrders = preserveSortOrders;
		return this;
	}

	/**
	 * Sets the screen to strict read-only modus. Will show any add buttons and set
	 * the screen to read only
	 *
	 * @param readOnly
	 */
	public FormOptions setReadOnly(boolean readOnly) {
		if (readOnly) {
			this.setOpenInViewMode(true);
			this.setShowEditButton(false);
			this.setShowAddButton(false);
			this.setShowRemoveButton(false);
		}
		this.readOnly = readOnly;
		return this;
	}

	/**
	 * Specify the desired screen mode for SplitLayouts. Supported values are
	 * HORIZONTAL (the default) and VERTICAL
	 * 
	 * @param screenMode
	 * @return
	 */
	public FormOptions setScreenMode(ScreenMode screenMode) {
		this.screenMode = screenMode;
		return this;
	}

	/**
	 * Specify whether searching occurs immediately when the user opens a screen
	 * that contains a search form. This defaults to <code>true</code>
	 * 
	 * @param searchImmediately
	 * @return
	 */
	public FormOptions setSearchImmediately(boolean searchImmediately) {
		this.searchImmediately = searchImmediately;
		return this;
	}

	/**
	 * Specify whether a Back button occurs when it is appropriate, e.g. when in the
	 * details mode of a SearchLayout
	 * 
	 * @param showBackButton
	 * @return
	 */
	public FormOptions setShowBackButton(boolean showBackButton) {
		this.showBackButton = showBackButton;
		return this;
	}

	/**
	 * Specify whether to show a caption above an edit form
	 * 
	 * @param showEditFormCaption
	 * @return
	 */
	public FormOptions setShowEditFormCaption(boolean showEditFormCaption) {
		this.showEditFormCaption = showEditFormCaption;
		return this;
	}

	/**
	 * Specify whether to display a details panel below a DetailsGrid when a row is
	 * selected
	 * 
	 * @param showDetailsGridDetailPanel
	 * @return
	 */
	public FormOptions setShowDetailsGridDetailsPanel(boolean showDetailsGridDetailPanel) {
		this.showDetailsGridDetailsPanel = showDetailsGridDetailPanel;
		return this;
	}

	/**
	 * Shorthand method for showing/hiding the previous and next buttons
	 *
	 * @param show whether to show the iteration buttons
	 * @return
	 */
	public FormOptions setShowIterationButtons(boolean show) {
		this.setShowPrevButton(show);
		this.setShowNextButton(show);
		return this;
	}

	/**
	 * Specify whether to show the "next" button in the detail screen of a search
	 * layout
	 * 
	 * @param showNextButton
	 * @return
	 */
	public FormOptions setShowNextButton(boolean showNextButton) {
		this.showNextButton = showNextButton;
		return this;
	}

	/**
	 * Specify whether to show the "previous" button in the detail screen of a
	 * search layout
	 * 
	 * @param showNextButton
	 * @return
	 */
	public FormOptions setShowPrevButton(boolean showPrevButton) {
		this.showPrevButton = showPrevButton;
		return this;
	}

	/**
	 * Specify whether to show the "quick search" field in a split layout
	 * 
	 * @return
	 */
	public FormOptions setShowQuickSearchField(boolean showQuickSearchField) {
		this.showQuickSearchField = showQuickSearchField;
		return this;
	}

	/**
	 * Specify whether to display a "refresh" button in the details screen of a
	 * SearchLayout
	 * 
	 * @param showRefreshButton
	 * @return
	 */
	public FormOptions setShowRefreshButton(boolean showRefreshButton) {
		this.showRefreshButton = showRefreshButton;
		return this;
	}

	/**
	 * Specify whether to display a "remove" button
	 * 
	 * @param showRemoveButton
	 * @return
	 */
	public FormOptions setShowRemoveButton(boolean showRemoveButton) {
		this.showRemoveButton = showRemoveButton;
		return this;
	}

	/**
	 * Specify whether to display a "search any" button in a search form. This
	 * allows the user to search on any criterion, rather than on the combination of
	 * all criterions
	 * 
	 * @param showSearchAnyButton
	 * @return
	 */
	public FormOptions setShowSearchAnyButton(boolean showSearchAnyButton) {
		this.showSearchAnyButton = showSearchAnyButton;
		return this;
	}

	/**
	 * Specify whether to display a toggle button for showing/hiding the search form
	 * 
	 * @param showToggleButton
	 * @return
	 */
	public FormOptions setShowToggleButton(boolean showToggleButton) {
		this.showToggleButton = showToggleButton;
		return this;
	}

	/**
	 * 
	 * @param startInAdvancedMode
	 * @return
	 */
	public FormOptions setStartInAdvancedMode(boolean startInAdvancedMode) {
		this.startInAdvancedMode = startInAdvancedMode;
		return this;
	}

}
