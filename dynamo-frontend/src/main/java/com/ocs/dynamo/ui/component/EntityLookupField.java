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
package com.ocs.dynamo.ui.component;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.VisibilityType;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.dialog.ModelBasedSearchDialog;
import com.ocs.dynamo.ui.composite.layout.SearchOptions;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.EntityModelUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.util.*;

/**
 * A composite component that displays a selected entity and offers a search
 * dialog to search for another one
 *
 * @param <ID> the type of the primary key
 * @param <T>  the type of the entity
 * @author bas.rutten
 */
public class EntityLookupField<ID extends Serializable, T extends AbstractEntity<ID>>
        extends QuickAddEntityField<ID, T, Object> {

    private static final long serialVersionUID = 5377765863515463622L;

    /**
     * Indicates whether it is allowed to add items
     */
    private final boolean addAllowed;

    /**
     * Indicates whether it is allowed to clear the selection
     */
    private final boolean clearAllowed;

    /**
     * The button used to clear the current selection
     */
    @Getter
    private Button clearButton;

    /**
     * Whether direct navigation via internal link is allowed
     */
    private final boolean directNavigationAllowed;

    /**
     * The joins to apply to the search in the search dialog
     */
    @Getter
    private FetchJoinInformation[] joins;

    /**
     * The label that displays the currently selected items
     */
    private Span label;

    /**
     * The button that brings up the search dialog
     */
    @Getter
    private Button selectButton;

    /**
     * The sort orders that are used in the popup dialog
     */
    private final List<SortOrder<?>> sortOrders;

    /**
     * The current value of the component. This can either be a single item or a set
     */
    private Object value;

    /**
     * The modal search dialog
     */
    private ModelBasedSearchDialog<ID, T> dialog;

    private final SearchOptions searchOptions;

    private final UI ui = UI.getCurrent();

    /**
     * Constructor
     *
     * @param service        the service used to query the database
     * @param entityModel    the entity model
     * @param attributeModel the attribute mode
     * @param filter         the filter to apply when searching
     * @param search         whether the component is used in a search screen
     * @param searchOptions  the search options
     * @param sortOrders     the sort order
     * @param joins          the joins to use when fetching data when filling the
     *                       pop-up dialog
     */
    public EntityLookupField(BaseService<ID, T> service, EntityModel<T> entityModel, AttributeModel attributeModel,
                             SerializablePredicate<T> filter, boolean search, SearchOptions searchOptions, List<SortOrder<?>> sortOrders,
                             FetchJoinInformation... joins) {
        super(service, entityModel, attributeModel, filter);
        this.sortOrders = sortOrders != null ? sortOrders : new ArrayList<>();
        this.joins = joins;
        this.clearAllowed = true;
        this.addAllowed = !search && (attributeModel != null && attributeModel.isQuickAddAllowed());
        this.directNavigationAllowed = !search && (attributeModel != null && attributeModel.isNavigable());
        this.searchOptions = searchOptions;
        initContent();
    }

    /**
     * Adds additional fetch joins
     *
     * @param fetchJoinInformation the joins to add
     */
    public void addFetchJoinInformation(FetchJoinInformation... fetchJoinInformation) {
        joins = ArrayUtils.addAll(joins, fetchJoinInformation);
    }

    /**
     * Adds a sort order
     *
     * @param sortOrder the sort order that must be added
     */
    public void addSortOrder(SortOrder<T> sortOrder) {
        this.sortOrders.add(sortOrder);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void afterNewEntityAdded(T entity) {
        if (searchOptions.isMultiSelect()) {
            if (getValue() == null) {
                // create new collection
                setValue(new ArrayList<>(List.of(entity)));
            } else {
                // add new entity to existing collection
                Collection<T> col = (Collection<T>) getValue();
                col.add(entity);
                setValue(col);
            }
        } else {
            setValue(entity);
        }
    }

    /**
     * Clears the current value of the component
     */
    public void clearValue() {
        if (Set.class.isAssignableFrom(getAttributeModel().getType())) {
            setValue(new HashSet<>());
        } else if (List.class.isAssignableFrom(getAttributeModel().getType())) {
            setValue(new ArrayList<>());
        } else {
            setValue(null);
        }
    }

    /**
     * Gets the value that must be displayed on the label that shows which items are
     * currently selected
     *
     * @param newValue the new value of the component
     * @return the contents of the label
     */
    @SuppressWarnings("unchecked")
    protected String constructLabelValue(Object newValue) {
        String caption = getMessageService().getMessage(
                searchOptions.isMultiSelect() ? "ocs.no.items.selected" : "ocs.no.item.selected",
                VaadinUtils.getLocale());
        if (newValue instanceof Collection<?>) {
            Collection<T> col = (Collection<T>) newValue;
            if (!col.isEmpty()) {
                caption = EntityModelUtils.getDisplayPropertyValue(col, getEntityModel(),
                        SystemPropertyUtils.getDefaultLookupFieldMaxItems(), getMessageService(),
                        VaadinUtils.getLocale());
            }
        } else {
            T entity = (T) newValue;
            if (newValue != null) {
                caption = EntityModelUtils.getDisplayPropertyValue(entity, getEntityModel());
            }
        }
        return caption;
    }

    private void constructSelectButton(HorizontalLayout bar, boolean showCaption) {
        selectButton = new Button(showCaption ? getMessageService().getMessage("ocs.select", VaadinUtils.getLocale()) : "");
        selectButton.setIcon(VaadinIcon.SEARCH.create());
        VaadinUtils.setTooltip(selectButton, getMessageService().getMessage("ocs.select", VaadinUtils.getLocale()));
        selectButton.addClickListener(event -> showSearchDialog());
        bar.add(selectButton);
    }

    private void showSearchDialog() {
        List<SerializablePredicate<T>> filterList = new ArrayList<>();
        if (getFilter() != null) {
            filterList.add(getFilter());
        }
        if (getAdditionalFilter() != null) {
            filterList.add(getAdditionalFilter());
        }

        dialog = new ModelBasedSearchDialog<>(getService(), getEntityModel(), filterList, sortOrders,
                searchOptions, getJoins());
        dialog.setOnClose(this::onDialogClose);
        dialog.setAfterOpen(this::afterOpen);
        dialog.buildAndOpen();
    }

    private Boolean onDialogClose() {
        if (searchOptions.isMultiSelect()) {
            if (EntityLookupField.this.getValue() == null) {
                EntityLookupField.this.setValue(dialog.getSelectedItems());
            } else {
                // add selected items to already selected items
                @SuppressWarnings("unchecked")
                Collection<T> cumulative = (Collection<T>) EntityLookupField.this.getValue();

                for (T selectedItem : dialog.getSelectedItems()) {
                    if (!cumulative.contains(selectedItem)) {
                        cumulative.add(selectedItem);
                    }
                }
                EntityLookupField.this.setValue(cumulative);
            }
        } else {
            // single value select
            EntityLookupField.this.setValue(dialog.getSelectedItem());
        }
        return true;
    }

    /**
     * Select the currently selected values in the pop-up search dialog
     * This uses a bit of a work-around of waiting until the dialog has
     * been opened first and then selecting the values because the checkbox dialog does not play nice
     */
    private void afterOpen() {
        if (searchOptions.isMultiSelect()) {
        	if (searchOptions.isUseCheckboxesForMultiSelect()) {
            Runnable run = () -> {
                try {
                    Thread.sleep(200);
                    ui.access(() -> selectValuesInDialog(dialog));
                } catch (Exception e) {
                    // do nothing
                }
            };
            new Thread(run).start();
        	}
        	else {
        	  selectValuesInDialog(dialog);
        	}
        }
    }

    @Override
    protected Object generateModelValue() {
        return convertToCorrectCollection(value);
    }

    public List<SortOrder<?>> getSortOrders() {
        return Collections.unmodifiableList(sortOrders);
    }

    @Override
    public Object getValue() {
        return convertToCorrectCollection(value);
    }

    protected void initContent() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.setSizeFull();
        if (this.getAttributeModel() != null) {
            this.setLabel(getAttributeModel().getDisplayName(VaadinUtils.getLocale()));
        }

        // label for displaying selected values
        label = new Span("");
        updateLabel(getValue());
        bar.add(label);
        bar.setFlexGrow(5, label);

        boolean showCaption = getAttributeModel() != null && VisibilityType.SHOW.equals(getAttributeModel().getLookupFieldCaptions());
        constructSelectButton(bar, showCaption);

        if (clearAllowed) {
            clearButton = new Button(showCaption ? getMessageService().getMessage("ocs.clear", VaadinUtils.getLocale()) : "");
            VaadinUtils.setTooltip(bar, getMessageService().getMessage("ocs.clear", VaadinUtils.getLocale()));
            clearButton.setIcon(VaadinIcon.ERASER.create());
            clearButton.addClickListener(event -> clearValue());
            bar.add(clearButton);
        }

        if (addAllowed) {
            Button addButton = constructAddButton();
            bar.add(addButton);
        }

        if (directNavigationAllowed) {
            Button directNavigationButton = constructDirectNavigationButton();
            bar.add(directNavigationButton);
        }

        bar.setSizeFull();
        add(bar);
    }

    protected boolean isAddAllowed() {
        return addAllowed;
    }

    protected boolean isClearAllowed() {
        return clearAllowed;
    }

    protected boolean isDirectNavigationAllowed() {
        return directNavigationAllowed;
    }

    @Override
    public void refresh(SerializablePredicate<T> filter) {
        setFilter(filter);
    }

    /**
     * Makes sure any currently selected values are highlighted in the search dialog
     *
     * @param dialog the dialog
     */
    public void selectValuesInDialog(ModelBasedSearchDialog<ID, T> dialog) {
        if (getValue() != null) {
            dialog.select(getValue());
        }
    }

    @Override
    public void setAdditionalFilter(SerializablePredicate<T> additionalFilter) {
        setValue(null);
        super.setAdditionalFilter(additionalFilter);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (selectButton != null) {
            selectButton.setEnabled(enabled);
            if (getClearButton() != null) {
                getClearButton().setEnabled(enabled);
            }
            if (getAddButton() != null) {
                getAddButton().setEnabled(enabled);
            }
        }
    }

    @Override
    public void setPlaceholder(String placeholder) {
        // do nothing
    }

    @Override
    protected void setPresentationValue(Object value) {
        this.value = value;
        updateLabel(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setValue(Object value) {
        if (value == null) {
            super.setValue(null);
        } else if (Set.class.isAssignableFrom(getAttributeModel().getType())) {
            Collection<T> col = (Collection<T>) value;
            super.setValue(new HashSet<>(col));
        } else if (List.class.isAssignableFrom(getAttributeModel().getType())) {
            Collection<T> col = (Collection<T>) value;
            super.setValue(new ArrayList<>(col));
        } else {
            super.setValue(value);
        }
        updateLabel(value);
    }

    /**
     * Updates the value that is displayed in the label
     *
     * @param newValue the new value
     */
    private void updateLabel(Object newValue) {
        if (label != null) {
            String caption = constructLabelValue(newValue);
            label.setText(caption);
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        setEnabled(!readOnly);
    }

    @Override
    public void setClearButtonVisible(boolean visible) {
        // do nothing
    }

}
