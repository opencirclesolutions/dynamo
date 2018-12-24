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
	 * Whether to display a button for opening a search dialog
	 */
	private boolean detailsGridSearchMode;

	/**
	 * Whether you can navigate to a detail screen by double clicking on a row in a
	 * results table
	 */
	private boolean doubleClickSelectAllowed = true;

	/**
	 * Whether to show an edit button when the screen is opened in view mode
	 */
	private boolean editAllowed;

	/**
	 * Indicates whether table export is allowed (read from system property)
	 */
	private boolean exportAllowed = SystemPropertyUtils.allowListExport();

	/**
	 * The data export mode - by default, export all attributes that are visible
	 */
	private ExportMode exportMode = ExportMode.ONLY_VISIBLE_IN_TABLE;

	/**
	 * Whether this form is nested in another form. Used by the framework, usually
	 * no need to set this yourself
	 */
	private boolean formNested;

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
	 * the split layout. In the HORIZONTAL view the table and form are displayed
	 * next to each other, in the VERTICAL view they are below each other
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
		return fo;
	}

	public AttributeGroupMode getAttributeGroupMode() {
		return attributeGroupMode;
	}

	public ExportMode getExportMode() {
		return exportMode;
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

	public boolean isDoubleClickSelectAllowed() {
		return doubleClickSelectAllowed;
	}

	public boolean isEditAllowed() {
		return editAllowed;
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

	public FormOptions setAttributeGroupMode(AttributeGroupMode attributeGroupMode) {
		this.attributeGroupMode = attributeGroupMode;
		return this;
	}

	public FormOptions setComplexDetailsMode(boolean complexDetailsMode) {
		this.complexDetailsMode = complexDetailsMode;
		return this;
	}

	public FormOptions setConfirmClear(boolean confirmClear) {
		this.confirmClear = confirmClear;
		return this;
	}

	public FormOptions setConfirmSave(boolean confirmSave) {
		this.confirmSave = confirmSave;
		return this;
	}

	public FormOptions setDetailsGridSearchMode(boolean detailsGridSearchMode) {
		this.detailsGridSearchMode = detailsGridSearchMode;
		return this;
	}

	public FormOptions setDoubleClickSelectAllowed(boolean doubleClickSelectAllowed) {
		this.doubleClickSelectAllowed = doubleClickSelectAllowed;
		return this;
	}

	public FormOptions setEditAllowed(boolean editAllowed) {
		this.editAllowed = editAllowed;
		return this;
	}

	public FormOptions setExportAllowed(boolean exportAllowed) {
		this.exportAllowed = exportAllowed;
		return this;
	}

	public FormOptions setExportMode(ExportMode exportMode) {
		this.exportMode = exportMode;
		return this;
	}

	public FormOptions setFormNested(boolean formNested) {
		this.formNested = formNested;
		return this;
	}

	public FormOptions setHideAddButton(boolean hideAddButton) {
		this.hideAddButton = hideAddButton;
		return this;
	}

	public FormOptions setHideCancelButton(boolean hideCancelButton) {
		this.hideCancelButton = hideCancelButton;
		return this;
	}

	public FormOptions setHideClearButton(boolean hideClearButton) {
		this.hideClearButton = hideClearButton;
		return this;
	}

	public FormOptions setOpenInViewMode(boolean openInViewMode) {
		this.openInViewMode = openInViewMode;
		return this;
	}

	public FormOptions setPlaceButtonBarAtTop(boolean placeButtonBarAtTop) {
		this.placeButtonBarAtTop = placeButtonBarAtTop;
		return this;
	}

	public FormOptions setPopup(boolean popup) {
		this.popup = popup;
		return this;
	}

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

	public FormOptions setScreenMode(ScreenMode screenMode) {
		this.screenMode = screenMode;
		return this;
	}

	public FormOptions setSearchImmediately(boolean searchImmediately) {
		this.searchImmediately = searchImmediately;
		return this;
	}

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

}
