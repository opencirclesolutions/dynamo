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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.filter.AndPredicate;
import com.ocs.dynamo.filter.BetweenPredicate;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.filter.GreaterOrEqualPredicate;
import com.ocs.dynamo.filter.LessOrEqualPredicate;
import com.ocs.dynamo.filter.SimpleStringPredicate;
import com.ocs.dynamo.filter.listener.FilterChangeEvent;
import com.ocs.dynamo.filter.listener.FilterListener;
import com.ocs.dynamo.ui.composite.form.ModelBasedSearchForm.FilterType;
import com.ocs.dynamo.ui.utils.ConvertUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * Represents one or more search fields used to filter on a single property
 * 
 * @author bas.rutten
 */
public class FilterGroup<T> {

    /**
     * The attribute model that this group is based on
     */
    private final AttributeModel attributeModel;

    /**
     * The auxiliary UI component
     */
    private final Component auxField;

    /**
     * The search filter for the auxiliary field
     */
    private SerializablePredicate<T> auxFieldFilter;

    /**
     * The main UI component
     */
    private final Component field;

    /**
     * The actual search filter
     */
    private SerializablePredicate<T> fieldFilter;

    /**
     * The top level component that contains the filter UI components
     */
    private final Component filterComponent;

    /**
     * The type of the filter
     */
    private final FilterType filterType;

    /**
     * Filter listeners to fire when the input changes
     */
    private List<FilterListener<T>> listeners = new ArrayList<>();

    /**
     * The main filter component
     */
    private SerializablePredicate<T> mainFilter;

    /**
     * The name of the property to filter on
     */
    private final String propertyId;

    /**
     * Constructor
     * 
     * @param attributeModel  the attribute model
     * @param propertyId      the property to bind
     * @param filterType      the type of the filter
     * @param filterComponent the layout component that contains the filter
     *                        components
     * @param field           the main filter field
     * @param auxField        the auxiliary filter field
     */
    public FilterGroup(AttributeModel attributeModel, FilterType filterType, Component filterComponent, Component field,
            Component auxField) {
        this.attributeModel = attributeModel;
        this.propertyId = attributeModel.getPath();
        this.filterType = filterType;
        this.filterComponent = filterComponent;
        this.field = field;
        this.auxField = auxField;

        // respond to a change of the main field
        if (field instanceof HasValue) {
            ValueChangeListener<ValueChangeEvent<?>> listener = event -> {
                Result<?> result = ConvertUtils.convertToModelValue(FilterGroup.this.attributeModel, event.getValue());
                result.ifError(r -> ((HasValidation) field).setErrorMessage(r));
                result.ifOk(r -> FilterGroup.this.valueChange(FilterGroup.this.field, r));
            };
            ((HasValue<?, ?>) field).addValueChangeListener(listener);
        }

        // respond to a change of the auxiliary field
        if (auxField instanceof HasValue) {
            ValueChangeListener<ValueChangeEvent<?>> auxListener = event -> {
                Result<?> result = ConvertUtils.convertToModelValue(FilterGroup.this.attributeModel, event.getValue());
                result.ifError(r -> ((HasValidation) auxField).setErrorMessage(r));
                result.ifOk(r -> FilterGroup.this.valueChange(FilterGroup.this.auxField, r));
            };
            ((HasValue<?, ?>) auxField).addValueChangeListener(auxListener);
        }
    }

    /**
     * Adds a listener that responds to a filter change
     * 
     * @param listener
     */
    public void addListener(FilterListener<T> listener) {
        this.listeners.add(listener);
    }

    /**
     * Broadcast a change to all listeners
     * 
     * @param event the change event
     */
    protected void broadcast(FilterChangeEvent<T> event) {
        for (FilterListener<T> listener : listeners) {
            listener.onFilterChange(event);
        }
    }

    /**
     * 
     * @param value
     * @return
     */
    private SerializablePredicate<T> createSearchDateOnlyFilter(Object value) {
        SerializablePredicate<T> filter = null;
        if (attributeModel.isSearchDateOnly() && value != null) {
            LocalDate ldt = (LocalDate) value;
            if (LocalDateTime.class.equals(attributeModel.getType())) {
                filter = new BetweenPredicate<>(propertyId, ldt.atStartOfDay(), ldt.atStartOfDay().plusDays(1).minusSeconds(1));
            } else {
                // zoned date time
                filter = new BetweenPredicate<>(propertyId, ldt.atStartOfDay(ZoneId.systemDefault()),
                        ldt.atStartOfDay(ZoneId.systemDefault()).plusDays(1).minusSeconds(1));
            }
        } else if (value != null && (!(value instanceof Collection) || !((Collection<?>) value).isEmpty())) {
            filter = new EqualsPredicate<>(propertyId, value);
        }
        return filter;
    }

    public Component getAuxField() {
        return auxField;
    }

    public Component getField() {
        return field;
    }

    public Component getFilterComponent() {
        return filterComponent;
    }

    public List<FilterListener<T>> getListeners() {
        return listeners;
    }

    public String getPropertyId() {
        return propertyId;
    }

    /**
     * Resets the search filters for both fields
     */
    public void reset() {
        if (field instanceof HasValue) {
            ((HasValue<?, ?>) field).clear();
            if (field instanceof HasValidation) {
                ((HasValidation) field).setErrorMessage(null);
            }
        }

        if (auxField != null) {
            if (auxField instanceof HasValue) {
                ((HasValue<?, ?>) auxField).clear();
                if (auxField instanceof HasValidation) {
                    ((HasValidation) auxField).setErrorMessage(null);
                }
            }
        }
    }

    /**
     * Sets the input component(s) as enabled or disabled
     * 
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        HasEnabled he = (HasEnabled) field;
        he.setEnabled(enabled);
        if (auxField instanceof HasEnabled) {
            ((HasEnabled) auxField).setEnabled(enabled);
        }
    }

    public void setListeners(List<FilterListener<T>> listeners) {
        this.listeners = listeners;
    }

    /**
     * Respond to a value change
     * 
     * @param field the changed field (can either be the main field or the auxiliary
     *              field)
     * @param value the new field value
     */
    public void valueChange(Component field, Object value) {
        // store the current filter
        SerializablePredicate<T> oldFilter = fieldFilter;
        SerializablePredicate<T> filter = null;

        switch (filterType) {
        case BETWEEN:
            // construct new filter for the selected field (or clear it)
            if (field == this.auxField) {
                // filter for the auxiliary field
                if (value != null) {
                    auxFieldFilter = new LessOrEqualPredicate<>(propertyId, value);
                } else {
                    auxFieldFilter = null;
                }
            } else {
                // filter for the main field
                if (value != null) {
                    mainFilter = new GreaterOrEqualPredicate<>(propertyId, value);
                } else {
                    mainFilter = null;
                }
            }

            // construct the aggregate filter
            if (auxFieldFilter != null && mainFilter != null) {
                filter = new AndPredicate<>(mainFilter, auxFieldFilter);
            } else if (auxFieldFilter != null) {
                filter = auxFieldFilter;
            } else {
                filter = mainFilter;
            }

            break;
        case LIKE:
            // like filter for comparing string fields
            if (value != null) {
                if (value instanceof Collection<?>) {
                    filter = new EqualsPredicate<>(propertyId, value);
                } else {
                    String valueStr = value.toString();
                    if (StringUtils.isNotEmpty(valueStr)) {
                        filter = new SimpleStringPredicate<>(propertyId, valueStr, attributeModel.isSearchPrefixOnly(),
                                attributeModel.isSearchCaseSensitive());
                    }
                }
            }
            break;
        default:
            // special handling for "search for date only"
            filter = createSearchDateOnlyFilter(value);
            break;
        }

        // store the current filter
        this.fieldFilter = filter;

        // propagate the change (this will trigger the actual search action)
        if (!listeners.isEmpty()) {
            broadcast(new FilterChangeEvent<T>(propertyId, oldFilter, filter));
        }
    }

}
