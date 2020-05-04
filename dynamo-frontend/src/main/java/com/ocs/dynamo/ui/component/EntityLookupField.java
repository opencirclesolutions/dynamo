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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.dialog.ModelBasedSearchDialog;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.EntityModelUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * A composite component that displays a selected entity and offers a search
 * dialog to search for another one
 * 
 * @author bas.rutten
 * @param <ID> the type of the primary key
 * @param <T> the type of the entity
 */
public class EntityLookupField<ID extends Serializable, T extends AbstractEntity<ID>> extends QuickAddEntityField<ID, T, Object> {

    private static final long serialVersionUID = 5377765863515463622L;

    /**
     * Whether the component is inside a grid
     */
    private boolean grid;

    /**
     * Whether direct navigation via internal link is allowed
     */
    private boolean directNavigationAllowed;

    /**
     * Indicates whether it is allowed to clear the selection
     */
    private boolean clearAllowed;

    /**
     * Indicates whether it is allowed to add items
     */
    private boolean addAllowed;

    /**
     * The button used to clear the current selection
     */
    private Button clearButton;

    /**
     * The joins to apply to the search in the search dialog
     */
    private FetchJoinInformation[] joins;

    /**
     * The label that displays the currently selected item
     */
    private Span label;

    /**
     * Whether the component allows multiple select
     */
    private boolean multiSelect;

    /**
     * The button that brings up the search dialog
     */
    private Button selectButton;

    /**
     * The sort order to apply to the search dialog
     */
    private List<SortOrder<?>> sortOrders = new ArrayList<>();

    /**
     * The current value of the component. This can either be a single item or a set
     */
    private Object value;

    /**
     * Constructor
     *
     * @param service        the service used to query the database
     * @param entityModel    the entity model
     * @param attributeModel the attribute mode
     * @param filter         the filter to apply when searching
     * @param search         whether the component is used in a search screen
     * @param sortOrders     the sort order
     * @param joins          the joins to use when fetching data when filling the
     *                       pop-up dialog
     */
    public EntityLookupField(BaseService<ID, T> service, EntityModel<T> entityModel, AttributeModel attributeModel,
            SerializablePredicate<T> filter, boolean search, boolean multiSelect, boolean grid, List<SortOrder<?>> sortOrders,
            FetchJoinInformation... joins) {
        super(service, entityModel, attributeModel, filter);
        this.sortOrders = sortOrders != null ? sortOrders : new ArrayList<>();
        this.joins = joins;
        this.multiSelect = multiSelect;
        this.clearAllowed = true;
        this.addAllowed = !search && (attributeModel != null && attributeModel.isQuickAddAllowed());
        this.directNavigationAllowed = !search && (attributeModel != null && attributeModel.isNavigable());
        this.grid = grid;
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
     * @param sortOrder the sort order to add
     */
    public void addSortOrder(SortOrder<T> sortOrder) {
        this.sortOrders.add(sortOrder);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void afterNewEntityAdded(T entity) {
        if (multiSelect) {
            if (getValue() == null) {
                // create new collection
                setValue(Lists.newArrayList(entity));
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

    public Button getClearButton() {
        return clearButton;
    }

    protected FetchJoinInformation[] getJoins() {
        return joins;
    }

    /**
     * Gets the value that must be displayed on the label that shows which items are
     * currently selected
     * 
     * @param newValue the new value
     * @return
     */
    @SuppressWarnings("unchecked")
    protected String constructLabelValue(Object newValue) {
        String caption = getMessageService().getMessage("ocs.no.item.selected", VaadinUtils.getLocale());
        if (newValue instanceof Collection<?>) {
            Collection<T> col = (Collection<T>) newValue;
            if (!col.isEmpty()) {
                caption = EntityModelUtils.getDisplayPropertyValue(col, getEntityModel(), SystemPropertyUtils.getLookupFieldMaxItems(),
                        getMessageService(), VaadinUtils.getLocale());
            }
        } else {
            // just a single value
            T t = (T) newValue;
            if (newValue != null) {
                caption = EntityModelUtils.getDisplayPropertyValue(t, getEntityModel());
            }
        }
        return caption;
    }

    public Button getSelectButton() {
        return selectButton;
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
        if (this.getAttributeModel() != null) {
            this.setLabel(getAttributeModel().getDisplayName(VaadinUtils.getLocale()));
        }

        // label for displaying selected values
        label = new Span("");
        updateLabel(getValue());
        bar.add(label);

        // button for selecting an entity - brings up the search dialog
        selectButton = new Button(grid ? "" : getMessageService().getMessage("ocs.select", VaadinUtils.getLocale()));
        selectButton.setIcon(VaadinIcon.SEARCH.create());
        selectButton.addClickListener(event -> {
            List<SerializablePredicate<T>> filterList = new ArrayList<>();
            if (getFilter() != null) {
                filterList.add(getFilter());
            }
            if (getAdditionalFilter() != null) {
                filterList.add(getAdditionalFilter());
            }

            ModelBasedSearchDialog<ID, T> dialog = new ModelBasedSearchDialog<ID, T>(getService(), getEntityModel(), filterList, sortOrders,
                    multiSelect, true, getJoins()) {

                private static final long serialVersionUID = -3432107069929941520L;

                @Override
                protected boolean doClose() {
                    if (multiSelect) {
                        if (EntityLookupField.this.getValue() == null) {
                            EntityLookupField.this.setValue(getSelectedItems());
                        } else {
                            // add selected items to already selected items
                            @SuppressWarnings("unchecked")
                            Collection<T> cumulative = (Collection<T>) EntityLookupField.this.getValue();
                            cumulative.addAll(getSelectedItems());
                            EntityLookupField.this.setValue(cumulative);
                        }
                    } else {
                        // single value select
                        EntityLookupField.this.setValue(getSelectedItem());
                    }
                    return true;
                }
            };
            dialog.build();
            selectValuesInDialog(dialog);
            dialog.open();
        });
        bar.add(selectButton);

        // button for clearing the current selection
        if (clearAllowed) {
            clearButton = new Button(grid ? "" : getMessageService().getMessage("ocs.clear", VaadinUtils.getLocale()));
            clearButton.setIcon(VaadinIcon.ERASER.create());
            clearButton.addClickListener(event -> clearValue());
            bar.add(clearButton);
        }

        // quick add button
        if (addAllowed) {
            Button addButton = constructAddButton();
            bar.add(addButton);
        }

        // direct navigation link
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
    @SuppressWarnings("unchecked")
    public void selectValuesInDialog(ModelBasedSearchDialog<ID, T> dialog) {
        // select any previously selected values in the dialog
        if (multiSelect && getValue() != null && getValue() instanceof Collection) {
            Collection<T> col = (Collection<T>) getValue();
            dialog.select(col);
        }
    }

    protected void setAddAllowed(boolean addAllowed) {
        this.addAllowed = addAllowed;
    }

    @Override
    public void setAdditionalFilter(SerializablePredicate<T> additionalFilter) {
        setValue(null);
        super.setAdditionalFilter(additionalFilter);
    }

    protected void setClearAllowed(boolean clearAllowed) {
        this.clearAllowed = clearAllowed;
    }

    protected void setDirectNavigationAllowed(boolean directNavigationAllowed) {
        this.directNavigationAllowed = directNavigationAllowed;
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

    public void clearValue() {
        if (Set.class.isAssignableFrom(getAttributeModel().getType())) {
            setValue(Sets.newHashSet());
        } else if (List.class.isAssignableFrom(getAttributeModel().getType())) {
            setValue(Lists.newArrayList());
        } else {
            setValue(null);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setValue(Object value) {
        if (value == null) {
            super.setValue(null);
        } else if (Set.class.isAssignableFrom(getAttributeModel().getType())) {
            Collection<T> col = (Collection<T>) value;
            super.setValue(Sets.newHashSet(col));
        } else if (List.class.isAssignableFrom(getAttributeModel().getType())) {
            Collection<T> col = (Collection<T>) value;
            super.setValue(Lists.newArrayList(col));
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
    public void setPlaceholder(String placeholder) {
        // do nothing
    }

    @Override
    protected Object generateModelValue() {
        return convertToCorrectCollection(value);
    }

    @Override
    protected void setPresentationValue(Object value) {
        this.value = value;
        updateLabel(value);
    }

}
