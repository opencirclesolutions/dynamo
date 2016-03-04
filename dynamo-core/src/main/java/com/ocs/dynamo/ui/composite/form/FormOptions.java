package com.ocs.dynamo.ui.composite.form;

import java.io.Serializable;

import com.ocs.dynamo.ui.composite.type.AttributeGroupMode;
import com.ocs.dynamo.ui.composite.type.ScreenMode;

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
     * How to display the various attribute groups. The default is PANEL
     * (related fields are shown in a panel, the panels are placed below each
     * other), but it can be changed to TABSHEET (related attributes are placed
     * on separate tabs)
     */
    private AttributeGroupMode attributeGroupMode = AttributeGroupMode.PANEL;

    /**
     * Whether to hide the add button
     */
    private boolean hideAddButton;

    /**
     * Whether to hide the cancel button (in an explicit detail view)
     */
    private boolean hideCancelButton;

    /**
     * The orientation of the screen (horizontal or vertical). This is relevant
     * for the split layout. In the HORIZONTAL view the table and form are
     * displayed next to each other, in the VERTICAL view they are below each
     * other
     */
    private ScreenMode screenMode = ScreenMode.HORIZONTAL;

    /**
     * Whether to open the form in view (read-only) mode
     */
    private boolean openInViewMode;

    /**
     * Whether to show an edit button when the screen is opened in view mode
     */
    private boolean showEditButton;

    /**
     * Whether to include an extra search field
     */
    private boolean showExtraSearchField;

    /**
     * Whether or not to add a "remove" button - by default this is disabled
     */
    private boolean showRemoveButton;

    /**
     * Whether to hide the clear button in the search form
     */
    private boolean hideClearButton;

    /**
     * Whether to display a button for toggling search fields
     */
    private boolean showToggleButton;

    /**
     * Whether to display a button for opening a search dialog
     */
    private boolean showSearchDialogButton;

    /**
     * Whether to display a back button
     */
    private boolean showBackButton;

    /**
     * Whether to display the screen in complete read-only mode. Settings
     * readOnly to true will automatically adapt all other necessray settings
     * (e.g. it will set "openInViewMode" to true)
     */
    private boolean readOnly;

    private boolean popup;

    public AttributeGroupMode getAttributeGroupMode() {
        return attributeGroupMode;
    }

    public ScreenMode getScreenMode() {
        return screenMode;
    }

    public boolean isHideAddButton() {
        return hideAddButton;
    }

    public boolean isHideCancelButton() {
        return hideCancelButton;
    }

    public boolean isOpenInViewMode() {
        return openInViewMode;
    }

    public boolean isShowEditButton() {
        return showEditButton;
    }

    public boolean isShowExtraSearchField() {
        return showExtraSearchField;
    }

    public boolean isShowRemoveButton() {
        return showRemoveButton;
    }

    public boolean isShowToggleButton() {
        return showToggleButton;
    }

    public void setShowToggleButton(boolean showToggleButton) {
        this.showToggleButton = showToggleButton;
    }

    public void setAttributeGroupMode(AttributeGroupMode attributeGroupMode) {
        this.attributeGroupMode = attributeGroupMode;
    }

    public void setHideAddButton(boolean hideAddButton) {
        this.hideAddButton = hideAddButton;
    }

    public void setHideCancelButton(boolean hideCancelButton) {
        this.hideCancelButton = hideCancelButton;
    }

    public void setScreenMode(ScreenMode screenMode) {
        this.screenMode = screenMode;
    }

    public void setOpenInViewMode(boolean openInViewMode) {
        this.openInViewMode = openInViewMode;
    }

    public void setShowEditButton(boolean showEditButton) {
        this.showEditButton = showEditButton;
    }

    public void setShowExtraSearchField(boolean showExtraSearchField) {
        this.showExtraSearchField = showExtraSearchField;
    }

    public void setShowRemoveButton(boolean showRemoveButton) {
        this.showRemoveButton = showRemoveButton;
    }

    public boolean isHideClearButton() {
        return hideClearButton;
    }

    public void setHideClearButton(boolean hideClearButton) {
        this.hideClearButton = hideClearButton;
    }

    public boolean isShowSearchDialogButton() {
        return showSearchDialogButton;
    }

    public void setShowSearchDialogButton(boolean showSearchDialogButton) {
        this.showSearchDialogButton = showSearchDialogButton;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Sets the screen to strict read-only modus. Will hide any add buttons and
     * set the screen to read only
     * 
     * @param readOnly
     */
    public void setReadOnly(boolean readOnly) {
        if (readOnly) {
            this.setOpenInViewMode(true);
            this.setShowEditButton(false);
            this.setHideAddButton(true);
            this.setShowRemoveButton(false);
        }
        this.readOnly = readOnly;
    }

    public boolean isShowBackButton() {
        return showBackButton;
    }

    public void setShowBackButton(boolean showBackButton) {
        this.showBackButton = showBackButton;
    }

    public boolean isPopup() {
        return popup;
    }

    public void setPopup(boolean popup) {
        this.popup = popup;
    }

}
