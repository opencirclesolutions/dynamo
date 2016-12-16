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
package com.ocs.dynamo.ui.composite.form;

import java.io.Serializable;

import com.ocs.dynamo.ui.composite.type.AttributeGroupMode;
import com.ocs.dynamo.ui.composite.type.ScreenMode;

/**
 * Parameter object that can be passed along when creating a page - this object uses smart defaults
 * so that in most cases, you do not have to set the properties individually
 * 
 * @author bas.rutten
 */
public class FormOptions implements Serializable {

	private static final long serialVersionUID = 7383335254540591298L;

	/**
	 * How to display the various attribute groups. The default is PANEL (related fields are shown
	 * in a panel, the panels are placed below each other), but it can be changed to TABSHEET
	 * (related attributes are placed on separate tabs)
	 */
	private AttributeGroupMode attributeGroupMode = AttributeGroupMode.PANEL;

	/**
	 * Whether the user has to confirm the clearing of the search form
	 */
	private boolean confirmClear;

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
	 * Whether to open the form in view (read-only) mode
	 */
	private boolean openInViewMode;

	/**
	 * Whether the form is shown as part of a popup - this is normally set by the framework when
	 * appropriate
	 */
	private boolean popup;

	/**
	 * Whether to display the screen in complete read-only mode. Settings readOnly to true will
	 * automatically adapt all other necessary settings (e.g. it will set "openInViewMode" to true)
	 */
	private boolean readOnly;

	/**
	 * The orientation of the screen (horizontal or vertical). This is relevant for the split
	 * layout. In the HORIZONTAL view the table and form are displayed next to each other, in the
	 * VERTICAL view they are below each other
	 */
	private ScreenMode screenMode = ScreenMode.HORIZONTAL;

	/**
	 * Whether to search immediately when opening a search screen
	 */
	private boolean searchImmediately = true;

	/**
	 * Whether to display a back button
	 */
	private boolean showBackButton;

	/**
	 * Whether to show an edit button when the screen is opened in view mode
	 */
	private boolean showEditButton;

	/**
	 * Whether to include an quick search field
	 */
	private boolean showQuickSearchField;

	/**
	 * Whether or not to add a "remove" button - by default this is disabled
	 */
	private boolean showRemoveButton;

	/**
	 * Whether to display a button for opening a search dialog
	 */
	private boolean showSearchDialogButton;

	/**
	 * Whether to display a button for toggling search fields
	 */
	private boolean showToggleButton;

	/**
	 * Whether to preserve the last selected tab when reopening a screen
	 */
	private boolean preserveSelectedTab;

	/**
	 * Indicates whether table export is allowed (false by default)
	 */
	private boolean tableExportAllowed = false;

	private boolean doubleClickSelectAllowed = true;

	public AttributeGroupMode getAttributeGroupMode() {
		return attributeGroupMode;
	}

	public ScreenMode getScreenMode() {
		return screenMode;
	}

	public boolean isConfirmClear() {
		return confirmClear;
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

	public boolean isPopup() {
		return popup;
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

	public boolean isShowEditButton() {
		return showEditButton;
	}

	public boolean isShowQuickSearchField() {
		return showQuickSearchField;
	}

	public boolean isShowRemoveButton() {
		return showRemoveButton;
	}

	public boolean isShowSearchDialogButton() {
		return showSearchDialogButton;
	}

	public boolean isShowToggleButton() {
		return showToggleButton;
	}

	public boolean isTableExportAllowed() {
		return tableExportAllowed;
	}

	public FormOptions setAttributeGroupMode(AttributeGroupMode attributeGroupMode) {
		this.attributeGroupMode = attributeGroupMode;
		return this;
	}

	public FormOptions setConfirmClear(boolean confirmClear) {
		this.confirmClear = confirmClear;
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

	public FormOptions setPopup(boolean popup) {
		this.popup = popup;
		return this;
	}

	/**
	 * Sets the screen to strict read-only modus. Will hide any add buttons and set the screen to
	 * read only
	 *
	 * @param readOnly
	 */
	public FormOptions setReadOnly(boolean readOnly) {
		if (readOnly) {
			this.setOpenInViewMode(true);
			this.setShowEditButton(false);
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

	public FormOptions setShowEditButton(boolean showEditButton) {
		this.showEditButton = showEditButton;
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

	public FormOptions setShowSearchDialogButton(boolean showSearchDialogButton) {
		this.showSearchDialogButton = showSearchDialogButton;
		return this;
	}

	public FormOptions setShowToggleButton(boolean showToggleButton) {
		this.showToggleButton = showToggleButton;
		return this;
	}

	public FormOptions setTableExportAllowed(boolean tableExportAllowed) {
		this.tableExportAllowed = tableExportAllowed;
		return this;
	}

	public boolean isPreserveSelectedTab() {
		return preserveSelectedTab;
	}

	public FormOptions setPreserveSelectedTab(boolean preserveSelectedTab) {
		this.preserveSelectedTab = preserveSelectedTab;
		return this;
	}

	public boolean isDoubleClickSelectAllowed() {
		return doubleClickSelectAllowed;
	}

	public FormOptions setDoubleClickSelectAllowed(boolean doubleClickSelectAllowed) {
		this.doubleClickSelectAllowed = doubleClickSelectAllowed;
		return this;
	}

}
