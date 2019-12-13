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
     * Whether to display a details grid in search mode
     */
    private boolean detailsGridSearchMode;

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
    private boolean editAllowed = true;

    /**
     * Whether to enabled advanced search mode
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
     * Whether this form is nested in another form. Used by the framework, there is
     * usually no need to set this yourself
     */
    private boolean formNested;

    /**
     * The grid edit mode (either all rows at once or one row at a time) inside an
     * EditableGridLayout
     */
    private GridEditMode gridEditMode = GridEditMode.SINGLE_ROW;

    /**
     * Whether to hide the add button
     */
    private boolean hideAddButton;

    /**
     * Whether to hide the cancel button (in an explicit detail view)
     */
    private boolean hideCancelButton;

    /**
     * Whether to hide the clear button in the search form
     */
    private boolean hideClearButton;

    /**
     * 
     */
    private String minimumTwoColumnWidth = SystemPropertyUtils.getMinimumTwoColumnWidth();

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
     * Whether the form is shown as part of a popup - this is normally set by the
     * framework when appropriate
     */
    private boolean popup;

    /**
     * Whether to preserve the last selected tab when reopening a screen
     */
    private boolean preserveSelectedTab;

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
     * Whether or not to add a "remove" button - by default this is disabled
     */
    private boolean showRemoveButton;

    /**
     * Whether to show an or button to enable match any search
     */
    private boolean showSearchAnyButton;

    /**
     * Whether to display a button for toggling search fields
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
        fo.setEditAllowed(isEditAllowed());
        fo.setHideAddButton(isHideAddButton());
        fo.setHideCancelButton(isHideCancelButton());
        fo.setHideClearButton(isHideClearButton());
        fo.setMinimumTwoColumnWidth(getMinimumTwoColumnWidth());
        fo.setOpenInViewMode(isOpenInViewMode());
        fo.setPlaceButtonBarAtTop(isPlaceButtonBarAtTop());
        fo.setPopup(isPopup());
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
        fo.setFormNested(isFormNested());
        fo.setConfirmSave(isConfirmSave());
        fo.setExportMode(getExportMode());
        fo.setGridEditMode(getGridEditMode());
        fo.setDetailsModeEnabled(isDetailsModeEnabled());
        fo.setEnableAdvancedSearchMode(isEnableAdvancedSearchMode());
        fo.setStartInAdvancedMode(isStartInAdvancedMode());
        return fo;
    }

    public AttributeGroupMode getAttributeGroupMode() {
        return attributeGroupMode;
    }

    public ExportMode getExportMode() {
        return exportMode;
    }

    public GridEditMode getGridEditMode() {
        return gridEditMode;
    }

    public String getMinimumTwoColumnWidth() {
        return minimumTwoColumnWidth;
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

    public boolean isDetailsGridSearchMode() {
        return detailsGridSearchMode;
    }

    public boolean isDetailsModeEnabled() {
        return detailsModeEnabled;
    }

    public boolean isDoubleClickSelectAllowed() {
        return doubleClickSelectAllowed;
    }

    public boolean isEditAllowed() {
        return editAllowed;
    }

    public boolean isEnableAdvancedSearchMode() {
        return enableAdvancedSearchMode;
    }

    public boolean isExportAllowed() {
        return exportAllowed;
    }

    public boolean isFormNested() {
        return formNested;
    }

    public boolean isHideAddButton() {
        return hideAddButton;
    }

    public boolean isHideCancelButton() {
        return hideCancelButton;
    }

    public boolean isHideClearButton() {
        return hideClearButton;
    }

    public boolean isOpenInViewMode() {
        return openInViewMode;
    }

    public boolean isPlaceButtonBarAtTop() {
        return placeButtonBarAtTop;
    }

    public boolean isPopup() {
        return popup;
    }

    public boolean isPreserveSelectedTab() {
        return preserveSelectedTab;
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

    public boolean isShowNextButton() {
        return showNextButton;
    }

    public boolean isShowPrevButton() {
        return showPrevButton;
    }

    public boolean isShowQuickSearchField() {
        return showQuickSearchField;
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
     * instead of a single detail screen, the application will render a tab layout.
     * You can use the <code>getDetailModeTabCaptions</code> and
     * <code>initTab</code> methods to initialize these tabs
     * 
     * @param complexDetailsMode
     * @return
     */
    public FormOptions setComplexDetailsMode(boolean complexDetailsMode) {
        this.complexDetailsMode = complexDetailsMode;
        return this;
    }

    /**
     * Specify whether to ask for confirmation before clearing the search form
     * 
     * @param confirmClear
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
     * @param confirmSave
     * @return
     */
    public FormOptions setConfirmSave(boolean confirmSave) {
        this.confirmSave = confirmSave;
        return this;
    }

    /**
     * Specify whether a DetailsEditGrid behaves in read-only mode and includes a
     * search form for selecting values.
     * 
     * @param detailsGridSearchMode
     * @return
     */
    public FormOptions setDetailsGridSearchMode(boolean detailsGridSearchMode) {
        this.detailsGridSearchMode = detailsGridSearchMode;
        return this;
    }

    /**
     * Specify whether the details screen is enabled for a search layout
     * 
     * @param detailsModeEnabled
     * @return
     */
    public FormOptions setDetailsModeEnabled(boolean detailsModeEnabled) {
        this.detailsModeEnabled = detailsModeEnabled;
        return this;
    }

    /**
     * Specify whether you can navigate to a detail screen by double clicking on a
     * row in the search results grid
     * 
     * @param doubleClickSelectAllowed
     * @return
     */
    public FormOptions setDoubleClickSelectAllowed(boolean doubleClickSelectAllowed) {
        this.doubleClickSelectAllowed = doubleClickSelectAllowed;
        return this;
    }

    /**
     * Specify whether editing existing entities is allowed
     * 
     * @param editAllowed
     * @return
     */
    public FormOptions setEditAllowed(boolean editAllowed) {
        this.editAllowed = editAllowed;
        return this;
    }

    /**
     * Whether to enable advanced search mode
     * 
     * @param enableAdvancedSearchMode
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
     * @param exportAllowed
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
     * Specify whether the details edit form is nested. This is used internally by
     * the framework and you do not normally have to edit it
     * 
     * @param formNested
     * @return
     */
    public FormOptions setFormNested(boolean formNested) {
        this.formNested = formNested;
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
     * Specify whether to hide the Add buttonF
     * 
     * @param hideAddButton
     * @return
     */
    public FormOptions setHideAddButton(boolean hideAddButton) {
        this.hideAddButton = hideAddButton;
        return this;
    }

    /**
     * Specify whether to hide the Cancel button in places in which it would
     * normally appear
     * 
     * @param hideCancelButton
     * @return
     */
    public FormOptions setHideCancelButton(boolean hideCancelButton) {
        this.hideCancelButton = hideCancelButton;
        return this;
    }

    /**
     * Specify whether to hide Clear button in search forms. This defaults to
     * <code>false</code>
     * 
     * @param hideClearButton
     * @return
     */
    public FormOptions setHideClearButton(boolean hideClearButton) {
        this.hideClearButton = hideClearButton;
        return this;
    }

    public FormOptions setMinimumTwoColumnWidth(String minimumTwoColumnWidth) {
        this.minimumTwoColumnWidth = minimumTwoColumnWidth;
        return this;
    }

    /**
     * Specify whether to open details screens in view mode. This defaults to
     * <code>false</code>
     * 
     * @param openInViewMode
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
     * Specify whether the component is part of a popupu window. This is used
     * internally by the framework and you do not normally need to modify it
     * 
     * @param popup
     * @return
     */
    public FormOptions setPopup(boolean popup) {
        this.popup = popup;
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
     * Sets the screen to strict read-only modus. Will hide any add buttons and set
     * the screen to read only
     *
     * @param readOnly
     */
    public FormOptions setReadOnly(boolean readOnly) {
        if (readOnly) {
            this.setOpenInViewMode(true);
            this.setEditAllowed(false);
            this.setHideAddButton(true);
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
     * Shorthand method for showing/hiding the previous and next buttons
     *
     * @param show
     * @return
     */
    public FormOptions setShowIterationButtons(boolean show) {
        this.setShowPrevButton(show);
        this.setShowNextButton(show);
        return this;
    }

    public FormOptions setShowNextButton(boolean showNextButton) {
        this.showNextButton = showNextButton;
        return this;
    }

    public FormOptions setShowPrevButton(boolean showPrevButton) {
        this.showPrevButton = showPrevButton;
        return this;
    }

    public FormOptions setShowQuickSearchField(boolean showQuickSearchField) {
        this.showQuickSearchField = showQuickSearchField;
        return this;
    }

    public FormOptions setShowRemoveButton(boolean showRemoveButton) {
        this.showRemoveButton = showRemoveButton;
        return this;
    }

    public FormOptions setShowSearchAnyButton(boolean showSearchAnyButton) {
        this.showSearchAnyButton = showSearchAnyButton;
        return this;
    }

    public FormOptions setShowToggleButton(boolean showToggleButton) {
        this.showToggleButton = showToggleButton;
        return this;
    }

    public FormOptions setStartInAdvancedMode(boolean startInAdvancedMode) {
        this.startInAdvancedMode = startInAdvancedMode;
        return this;
    }

}
