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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.filter.listener.FilterChangeEvent;
import com.ocs.dynamo.filter.listener.FilterListener;
import com.ocs.dynamo.ui.composite.form.ModelBasedSearchForm.FilterType;
import com.ocs.dynamo.utils.ConvertUtil;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;

/**
 * Represents one or more search fields used to filter on a single property
 * 
 * @author bas.rutten
 */
public class FilterGroup {

	// the attribute model that this region is based on
	private final AttributeModel attributeModel;

	// the name of the property to filter on
	private final String propertyId;

	// the type of the filter
	private final FilterType filterType;

	// the component that contains the field(s) to filter on
	private final Component filterComponent;

	// the main search field
	private final Field<Object> field;

	// the currently active filter - constructing by composing the main filter
	// and the aux filter
	private Filter fieldFilter;

	// the main filter - only used in case of a main/aux combination
	private Filter mainFilter;

	// auxiliary field (in case of range searches)
	private final Field<Object> auxField;

	// filter on the auxiliary field
	private Filter auxFieldFilter;

	// listener that responds to any changes
	private List<FilterListener> listeners = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param attributeModel
	 *            the attribute model
	 * @param propertyId
	 *            the property to bind
	 * @param filterType
	 *            the type of the filter
	 * @param filterComponent
	 *            the layout component that contains the filter components
	 * @param field
	 *            the main filter field
	 * @param auxField
	 *            the auxiliary filter field
	 */
	@SuppressWarnings("unchecked")
	public FilterGroup(AttributeModel attributeModel, String propertyId, FilterType filterType,
	        Component filterComponent, Field<?> field, Field<?> auxField) {
		this.attributeModel = attributeModel;
		this.propertyId = propertyId;
		this.filterType = filterType;
		this.filterComponent = filterComponent;
		this.field = (Field<Object>) field;
		this.auxField = (Field<Object>) auxField;

		// respond to a change of the main field
		field.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = -7262800348377772750L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				try {

					FilterGroup.this.valueChange(FilterGroup.this.field, ConvertUtil.convertSearchValue(
					        FilterGroup.this.attributeModel, event.getProperty().getValue()));
				} catch (ConversionException ex) {
					// do nothing (this results in a nicer exception being displayed)
				}
			}
		});

		// respond to a change of the auxiliary field
		if (auxField != null) {
			auxField.addValueChangeListener(new ValueChangeListener() {

				private static final long serialVersionUID = 2849016722614508332L;

				@Override
				public void valueChange(ValueChangeEvent event) {
					FilterGroup.this.valueChange(FilterGroup.this.auxField, ConvertUtil.convertSearchValue(
					        FilterGroup.this.attributeModel, event.getProperty().getValue()));
				}
			});
		}
	}

	/**
	 * Respond to a value change
	 * 
	 * @param field
	 *            the changed field (can either be the main field or the auxiliary field)
	 * @param value
	 *            the new field value
	 */
	public void valueChange(Field<?> field, Object value) {
		// store the current filter
		Filter oldFilter = fieldFilter;
		Filter filter = null;

		switch (filterType) {
		case BETWEEN:

			// construct new filter for the selected field (or clear it)
			if (field == this.auxField) {
				// filter for the auxiliary field
				if (value != null) {
					auxFieldFilter = new Compare.LessOrEqual(propertyId, value);
				} else {
					auxFieldFilter = null;
				}
			} else {
				// filter for the main field
				if (value != null) {
					mainFilter = new Compare.GreaterOrEqual(propertyId, value);
				} else {
					mainFilter = null;
				}
			}

			// construct the aggregate filter
			if (auxFieldFilter != null && mainFilter != null) {
				filter = new And(mainFilter, auxFieldFilter);
			} else if (auxFieldFilter != null) {
				filter = auxFieldFilter;
			} else {
				filter = mainFilter;
			}

			break;
		case LIKE:
			// like filter for comparing string fields
			if (value != null) {
				String valueStr = value.toString();
				if (StringUtils.isNotEmpty(valueStr)) {
					filter = new SimpleStringFilter(propertyId, valueStr, !attributeModel.isSearchCaseSensitive(),
					        attributeModel.isSearchPrefixOnly());
				}
			}
			break;
		default:
			// by default, simply use and "equals" filter
			if (value != null) {
				filter = new Compare.Equal(propertyId, value);
			}
			break;
		}

		// store the current filter
		this.fieldFilter = filter;

		// propagate the change (this will trigger the actual search action)
		if (!listeners.isEmpty()) {
			broadcast(new FilterChangeEvent(propertyId, oldFilter, filter, value));
		}
	}

	public void setEnabled(boolean enabled) {
		field.setEnabled(enabled);
		if (auxField != null) {
			auxField.setEnabled(enabled);
		}
	}

	/**
	 * Resets both filters
	 */
	public void reset() {
		field.setValue(null);
		if (auxField != null) {
			auxField.setValue(null);
		}
	}

	public List<FilterListener> getListeners() {
		return listeners;
	}

	public void setListeners(List<FilterListener> listeners) {
		this.listeners = listeners;
	}

	/**
	 * Adds a listener that responds to a filter change
	 * 
	 * @param listener
	 */
	public void addListener(FilterListener listener) {
		this.listeners.add(listener);
	}

	/**
	 * Broadcast a change to all listeners
	 * 
	 * @param event
	 *            the change event
	 */
	protected void broadcast(FilterChangeEvent event) {
		for (FilterListener listener : listeners) {
			listener.onFilterChange(event);
		}
	}

	public String getPropertyId() {
		return propertyId;
	}

	public Component getFilterComponent() {
		return filterComponent;
	}

	public Field<Object> getField() {
		return field;
	}

	public Field<Object> getAuxField() {
		return auxField;
	}

}
