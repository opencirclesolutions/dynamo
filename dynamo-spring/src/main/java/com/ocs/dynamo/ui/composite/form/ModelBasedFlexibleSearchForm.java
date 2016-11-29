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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.FlexibleFilterDefinition;
import com.ocs.dynamo.filter.listener.FilterChangeEvent;
import com.ocs.dynamo.filter.listener.FilterListener;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.Searchable;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.component.FancyListSelect;
import com.ocs.dynamo.utils.ConvertUtil;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.filter.Between;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.Not;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;

/**
 * 
 * A search form for creating flexible search queries. The form contains functionality for adding
 * and removing filter that can be clicked together from an attribute, an operator and a value
 * 
 * @author bas.rutten
 *
 * @param <ID>
 *            the type of the primary key
 * @param <T>
 *            the type of the entity
 */
public class ModelBasedFlexibleSearchForm<ID extends Serializable, T extends AbstractEntity<ID>> extends
        AbstractModelBasedSearchForm<ID, T> {

	/**
	 * A region that contains the fields for managing a single filter
	 * 
	 * @author bas.rutten
	 *
	 */
	private class FilterRegion {

		/**
		 * Indicates whether we are restoring an existing definition - if this is the case the we do
		 * not need to set a default filter value
		 */
		private boolean restoring;

		/**
		 * The attribute model
		 */
		private AttributeModel am;

		/**
		 * The filter for the auxiliary field
		 */
		private Filter auxFilter;

		/**
		 * The component that holds the auxiliary search value
		 */
		private Field<Object> auxValueComponent;

		/**
		 * The currently active filter for the entire region (calculated by composing the mainFilter
		 * and the auxFieldFilter)
		 */
		private Filter fieldFilter;

		/**
		 * The combo box that contains the attributes to filter on
		 */
		private ComboBox attributeFilterComboBox;

		/**
		 * Label to display when no filter has been selected
		 */
		private Label noFilterLabel;

		/**
		 * The filter type
		 */
		private FlexibleFilterType filterType;

		/**
		 * The main layout
		 */
		private Layout layout;

		/**
		 * The FilterListener that listens for filter changes
		 */
		private FilterListener listener;

		/**
		 * The filter for the main field
		 */
		private Filter mainFilter;

		/**
		 * The component that holds the main search value
		 */
		private Field<Object> mainValueComponent;

		/**
		 * The button used to remove the filter
		 */
		private Button removeButton;

		/**
		 * The combo box that contains the available filter types
		 */
		private ComboBox typeFilterCombo;

		FilterRegion(FilterListener listener) {
			this.listener = listener;
			layout = new DefaultHorizontalLayout();

			removeButton = new Button(message("ocs.remove"));
			removeButton.addClickListener(new Button.ClickListener() {

				private static final long serialVersionUID = -3195227654172834655L;

				@Override
				public void buttonClick(ClickEvent event) {
					Layout parent = (Layout) layout.getParent();
					parent.removeComponent(layout);

					// remove from list
					regions.remove(FilterRegion.this);

					// remove the filter
					if (am != null) {
						FilterRegion.this.listener.onFilterChange(new FilterChangeEvent(am.getPath(), fieldFilter,
						        null, null));
					}
				}
			});
			layout.addComponent(removeButton);

			attributeFilterComboBox = new ComboBox(message("ocs.filter"));
			attributeFilterComboBox.setStyleName(DynamoConstants.CSS_NESTED);
			attributeFilterComboBox.setFilteringMode(FilteringMode.CONTAINS);

			// find out which attributes can be search on and sort them in alphabetical name
			List<AttributeModel> filteredModels = iterate(getEntityModel().getAttributeModels());
			Collections.sort(filteredModels, new Comparator<AttributeModel>() {

				@Override
				public int compare(AttributeModel o1, AttributeModel o2) {
					return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
				}
			});

			for (AttributeModel a : filteredModels) {
				boolean mayAdd = !a.isRequiredForSearching() || !hasFilter(a);
				if (mayAdd) {
					attributeFilterComboBox.addItem(a);
					attributeFilterComboBox.setItemCaption(a, a.getDisplayName());
				}
			}

			// add a value change listener that fills the filter type combo box after a change
			attributeFilterComboBox.addValueChangeListener(new ValueChangeListener() {

				private static final long serialVersionUID = -2902597100183015869L;

				@Override
				public void valueChange(ValueChangeEvent event) {
					handleFilterAttributeChange(event, restoring);
				}
			});
			layout.addComponent(attributeFilterComboBox);

			noFilterLabel = new Label(message("ocs.select.filter"));
			noFilterLabel.setCaption("");
			layout.addComponent(noFilterLabel);
		}

		/**
		 * Creates a SimpleStringFilter with certain characteristics
		 * 
		 * @param value
		 *            the value to search on
		 * @param prefixOnly
		 *            whether to search by prefix only
		 * @return
		 */
		private SimpleStringFilter createStringFilter(Object value, boolean prefixOnly) {
			String valueStr = value == null ? "" : value.toString();
			if (StringUtils.isNotEmpty(valueStr)) {
				return new SimpleStringFilter(am.getPath(), valueStr, !am.isSearchCaseSensitive(), prefixOnly);
			}
			return null;
		}

		/**
		 * Change the filter attribute
		 * 
		 * @param attributeModel
		 *            the selected attribute model
		 * @param restoring
		 *            whether we are restoring an existing filter
		 */
		private void filterAttributeChange(AttributeModel attributeModel, boolean restoring) {
			this.am = attributeModel;
			if (am != null) {
				ComboBox newTypeFilterCombo = new ComboBox(message("ocs.type"));
				newTypeFilterCombo.setNullSelectionAllowed(false);
				newTypeFilterCombo.addValueChangeListener(new ValueChangeListener() {

					private static final long serialVersionUID = -98045001905415268L;

					@Override
					public void valueChange(ValueChangeEvent event) {
						handleFilterTypeChange((FlexibleFilterType) event.getProperty().getValue());
					}
				});
				newTypeFilterCombo.setStyleName(DynamoConstants.CSS_NESTED);

				List<FlexibleFilterType> filterTypes = getFilterTypes(am);
				for (FlexibleFilterType ft : filterTypes) {
					newTypeFilterCombo.addItem(ft);
				}

				// cannot remove mandatory filters
				removeButton.setEnabled(!am.isRequiredForSearching());
				attributeFilterComboBox.setEnabled(!am.isRequiredForSearching());

				if (typeFilterCombo != null) {
					layout.replaceComponent(typeFilterCombo, newTypeFilterCombo);
				} else {
					layout.addComponent(newTypeFilterCombo);
				}

				// hide the value component after a filter change
				if (mainValueComponent != null) {
					layout.removeComponent(mainValueComponent);
				}
				if (auxValueComponent != null) {
					layout.removeComponent(auxValueComponent);
				}

				typeFilterCombo = newTypeFilterCombo;

				// pre-select the first value and disable the component if there is just one
				// component
				if (!restoring) {
					typeFilterCombo.setValue(getDefaultFilterType(am));
				}
				if (filterTypes.size() == 1) {
					typeFilterCombo.setEnabled(false);
				}
			} else {
				// no filter selected, remove everything
				if (typeFilterCombo != null) {
					layout.removeComponent(typeFilterCombo);
				}
				if (mainValueComponent != null) {
					layout.removeComponent(mainValueComponent);
				}
				if (auxValueComponent != null) {
					layout.removeComponent(auxValueComponent);
				}
			}
			noFilterLabel.setVisible(this.am == null);

		}

		/**
		 * Returns the available filter types for a certain attribute model
		 * 
		 * @param am
		 *            the attribute model
		 * @return
		 */
		private List<FlexibleFilterType> getFilterTypes(AttributeModel am) {
			List<FlexibleFilterType> result = new ArrayList<>();
			result.add(FlexibleFilterType.EQUALS);

			switch (am.getAttributeType()) {
			case BASIC:
				if (String.class.equals(am.getType())) {
					result.add(FlexibleFilterType.NOT_EQUAL);
					result.add(FlexibleFilterType.CONTAINS);
					result.add(FlexibleFilterType.STARTS_WITH);
					result.add(FlexibleFilterType.NOT_CONTAINS);
					result.add(FlexibleFilterType.NOT_STARTS_WITH);
				} else if (Enum.class.isAssignableFrom(am.getType())) {
					result.add(FlexibleFilterType.NOT_EQUAL);
				} else if (Number.class.isAssignableFrom(am.getType())) {
					result.add(FlexibleFilterType.BETWEEN);
					result.add(FlexibleFilterType.LESS_THAN);
					result.add(FlexibleFilterType.LESS_OR_EQUAL);
					result.add(FlexibleFilterType.GREATER_OR_EQUAL);
					result.add(FlexibleFilterType.GREATER_THAN);
				} else if (Date.class.isAssignableFrom(am.getType())) {
					result.add(FlexibleFilterType.BETWEEN);
					result.add(FlexibleFilterType.LESS_THAN);
					result.add(FlexibleFilterType.LESS_OR_EQUAL);
					result.add(FlexibleFilterType.GREATER_OR_EQUAL);
					result.add(FlexibleFilterType.GREATER_THAN);
				}
				break;
			default:
				break;
			}

			return result;
		}

		public Layout getLayout() {
			return layout;
		}

		/**
		 * Handle a change of the attribute to filter on
		 * 
		 * @param event
		 *            the event
		 */
		private void handleFilterAttributeChange(ValueChangeEvent event, boolean restoring) {
			AttributeModel temp = (AttributeModel) event.getProperty().getValue();
			filterAttributeChange(temp, restoring);
		}

		/**
		 * Handle a change of the filter
		 * 
		 * @param type
		 *            the selected filter type
		 */
		@SuppressWarnings("unchecked")
		private void handleFilterTypeChange(FlexibleFilterType type) {
			filterType = type;

			// construct the field
			Field<Object> custom = (Field<Object>) constructCustomField(getEntityModel(), am);
			final Field<Object> newComponent = custom != null ? custom : (Field<Object>) getFieldFactory()
			        .constructField(am, getFieldFilters(), getFieldEntityModel(am));

			if (newComponent instanceof FancyListSelect) {
				FancyListSelect<?, ?> fls = (FancyListSelect<?, ?>) newComponent;
				fls.getListSelect().setStyleName(DynamoConstants.CSS_NESTED);
				fls.getComboBox().setStyleName(DynamoConstants.CSS_NESTED);
			}

			newComponent.addValueChangeListener(new ValueChangeListener() {

				private static final long serialVersionUID = 8238796619466110500L;

				@Override
				public void valueChange(ValueChangeEvent event) {
					handleValueChange(newComponent, event.getProperty().getValue());
				}
			});

			if (mainValueComponent == null) {
				layout.addComponent(newComponent);
			} else {
				layout.replaceComponent(mainValueComponent, newComponent);
			}

			mainValueComponent = newComponent;

			if (FlexibleFilterType.BETWEEN.equals(filterType)) {
				newComponent.setCaption(am.getDisplayName() + " " + message("ocs.from"));

				final Field<Object> newAuxComponent = (Field<Object>) getFieldFactory().createField(am.getPath());
				newAuxComponent.addValueChangeListener(new ValueChangeListener() {

					private static final long serialVersionUID = 8238796619466110500L;

					@Override
					public void valueChange(ValueChangeEvent event) {
						handleValueChange(newAuxComponent, event.getProperty().getValue());
					}
				});
				newAuxComponent.setCaption(am.getDisplayName() + " " + message("ocs.to"));

				if (auxValueComponent == null) {
					layout.addComponent(newAuxComponent);
				} else {
					layout.replaceComponent(auxValueComponent, newAuxComponent);
				}
				auxValueComponent = newAuxComponent;
			} else {
				// no need for the auxiliary field
				if (auxValueComponent != null) {
					layout.removeComponent(auxValueComponent);
					auxValueComponent = null;
				}
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
		private void handleValueChange(Field<?> field, Object value) {
			// store the current filter
			Filter oldFilter = fieldFilter;
			Filter filter = null;

			// convert the value to its actual representation
			value = ConvertUtil.convertSearchValue(am, value);

			switch (this.filterType) {
			case BETWEEN:

				// construct new filter for the selected field (or clear it)
				if (field == this.auxValueComponent) {
					// filter for the auxiliary field
					if (value != null) {
						auxFilter = new Compare.LessOrEqual(am.getPath(), value);
					} else {
						auxFilter = null;
					}
				} else {
					// filter for the main field
					if (value != null) {
						mainFilter = new Compare.GreaterOrEqual(am.getPath(), value);
					} else {
						mainFilter = null;
					}
				}

				// construct the aggregate filter
				if (auxFilter != null && mainFilter != null) {
					filter = new Between(am.getPath(),
					        (Comparable<?>) ((Compare.GreaterOrEqual) mainFilter).getValue(),
					        (Comparable<?>) ((Compare.LessOrEqual) auxFilter).getValue());
				} else if (auxFilter != null) {
					filter = auxFilter;
				} else {
					filter = mainFilter;
				}

				break;
			case LESS_OR_EQUAL:
				filter = new Compare.LessOrEqual(am.getPath(), value);
				break;
			case LESS_THAN:
				filter = new Compare.Less(am.getPath(), value);
				break;
			case GREATER_OR_EQUAL:
				filter = new Compare.GreaterOrEqual(am.getPath(), value);
				break;
			case GREATER_THAN:
				filter = new Compare.Greater(am.getPath(), value);
				break;
			case CONTAINS:
				// like filter for comparing string fields
				filter = createStringFilter(value, false);
				break;
			case NOT_EQUAL:
				filter = new Not(new Compare.Equal(am.getPath(), value));
				break;
			case STARTS_WITH:
				// like filter for comparing string fields
				filter = createStringFilter(value, true);
				break;
			case NOT_CONTAINS:
				filter = new Not(createStringFilter(value, false));
				break;
			case NOT_STARTS_WITH:
				filter = new Not(createStringFilter(value, true));
				break;
			default:
				// by default, simply use an "equals" filter
				if (value != null) {
					filter = new Compare.Equal(am.getPath(), value);
				}
				break;
			}

			// store the current filter
			this.fieldFilter = filter;

			// propagate the change (this will trigger the actual search action)
			listener.onFilterChange(new FilterChangeEvent(am.getPath(), oldFilter, filter, value));
		}

		/**
		 * Extracts the filter definition from the region
		 * 
		 * @return
		 */
		public FlexibleFilterDefinition toDefinition() {
			if (mainValueComponent != null) {

				FlexibleFilterDefinition definition = new FlexibleFilterDefinition();
				definition.setFlexibleFilterType(filterType);
				definition.setAttributeModel(am);
				definition.setValue(ConvertUtil.convertSearchValue(am, mainValueComponent.getValue()));
				if (auxValueComponent != null) {
					definition.setValueTo(ConvertUtil.convertSearchValue(am, auxValueComponent.getValue()));
				}
				return definition;
			}
			return null;
		}
	}

	private static final long serialVersionUID = -6668770373597055403L;

	/**
	 * The button that is used to add a new filter
	 */
	private Button addFilterButton;

	/**
	 * The filter regions
	 */
	private List<FilterRegion> regions = new ArrayList<>();

	/**
	 * Constructor
	 */
	public ModelBasedFlexibleSearchForm(Searchable searchable, EntityModel<T> entityModel, FormOptions formOptions) {
		this(searchable, entityModel, formOptions, null, null);
	}

	/**
	 * Constructor
	 * 
	 * @param searchable
	 *            the component on which to carry out the search
	 * @param entityModel
	 *            the entity model
	 * @param formOptions
	 *            the form options
	 * @param defaultFilters
	 *            the additional filters to apply to every search action
	 * @param fieldFilters
	 *            a map of filters to apply to the individual fields
	 */
	public ModelBasedFlexibleSearchForm(Searchable searchable, EntityModel<T> entityModel, FormOptions formOptions,
	        List<Filter> defaultFilters, Map<String, Filter> fieldFilters) {
		super(searchable, entityModel, formOptions, defaultFilters, fieldFilters);
	}

	/**
	 * Adds a filter in response to a button click
	 */
	private void addFilter() {
		FilterRegion region = new FilterRegion(this);
		regions.add(region);

		getFilterLayout().addComponent(region.getLayout());
		toggle(true);
	}

	/**
	 * Programmatically add
	 * 
	 * @param attributeModel
	 *            the attribute model to base the filter on
	 * @param filterType
	 *            the type of the filter
	 * @param value
	 *            the value of the filter
	 * @param auxValue
	 *            the auxiliary value
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addFilter(AttributeModel attributeModel, FlexibleFilterType filterType, Object value, Object auxValue) {

		boolean multi = Collection.class.isAssignableFrom(attributeModel.getType())
		        || attributeModel.isMultipleSearch();

		FilterRegion match = null;
		for (FilterRegion region : regions) {
			if (region.am != null && region.am.getPath().equals(attributeModel.getPath())) {
				match = region;
				break;
			}
		}

		// no matching filter found, create a new one
		if (match == null) {
			addFilter();
			match = regions.get(regions.size() - 1);
			match.attributeFilterComboBox.setValue(attributeModel);
			match.filterAttributeChange(attributeModel, true);
			match.typeFilterCombo.setValue(filterType);
		}

		if (multi) {
			List list = new ArrayList();

			// preserve existing values
			if (match.mainValueComponent.getValue() != null) {
				Collection col = (Collection) match.mainValueComponent.getValue();
				list.addAll(col);
			}

			// then add the new one
			list.add(value);
			match.mainValueComponent.setValue(list);
		} else {
			// singular value, simply set the value
			match.typeFilterCombo.setValue(filterType);
			match.mainValueComponent.setValue(value);
			if (match.auxValueComponent != null) {
				match.auxValueComponent.setValue(auxValue);
			}
		}

	}

	@Override
	public void clear() {
		// remove any non-required filter regions and clear the rest
		Iterator<FilterRegion> it = regions.iterator();
		for (; it.hasNext();) {
			FilterRegion fr = it.next();
			if (fr.am == null || !fr.am.isRequiredForSearching()) {
				getFilterLayout().removeComponent(fr.getLayout());
				it.remove();
			} else {
				fr.mainValueComponent.setValue(null);
				if (fr.auxValueComponent != null) {
					fr.auxValueComponent.setValue(null);
				}
			}
		}
		super.clear();
	}

	@Override
	protected void constructButtonBar(Layout buttonBar) {

		// construct button for adding a new filter
		addFilterButton = new Button(message("ocs.add.filter"));
		addFilterButton.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 3509270848120570068L;

			@Override
			public void buttonClick(ClickEvent event) {
				addFilter();
			}
		});

		buttonBar.addComponent(addFilterButton);
		buttonBar.addComponent(constructSearchButton());
		buttonBar.addComponent(constructClearButton());
		buttonBar.addComponent(constructToggleButton());
	}

	@Override
	protected Layout constructFilterLayout() {
		// just an enmpty layout - filterw sill be added to it on the fly
		return new DefaultVerticalLayout();
	}

	/**
	 * Extracts a list of FlexibleFilterDefinitions form the currently active search filters
	 */
	public List<FlexibleFilterDefinition> extractFilterDefinitions() {
		List<FlexibleFilterDefinition> definitions = new ArrayList<>();

		for (FilterRegion region : regions) {
			FlexibleFilterDefinition def = region.toDefinition();
			if (def != null) {
				definitions.add(def);
			}
		}
		return definitions;
	}

	public Button getAddFilterButton() {
		return addFilterButton;
	}

	/**
	 * Returns the default filter type for a certain attribute model
	 * 
	 * @param am
	 *            the attribute model
	 */
	public FlexibleFilterType getDefaultFilterType(AttributeModel am) {
		switch (am.getAttributeType()) {
		case BASIC:
			if (String.class.equals(am.getType())) {
				return FlexibleFilterType.CONTAINS;
			} else if (Enum.class.isAssignableFrom(am.getType())) {
				return FlexibleFilterType.EQUALS;
			} else if (Number.class.isAssignableFrom(am.getType())) {
				return FlexibleFilterType.BETWEEN;
			} else if (Date.class.isAssignableFrom(am.getType())) {
				return FlexibleFilterType.BETWEEN;
			}
		default:
			return FlexibleFilterType.EQUALS;
		}
	}

	/**
	 * Indicates whether the form contains a filter for the provided attribute model
	 * 
	 * @param attributeModel
	 *            the attribute model
	 * @return
	 */
	public boolean hasFilter(AttributeModel attributeModel) {
		for (FilterRegion r : regions) {
			if (ObjectUtils.equals(attributeModel, r.am)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Iterates over the available attribute models and construct a list of the attributes that can
	 * be searched
	 * 
	 * @param attributeModels
	 *            the attribute modesl
	 * @return
	 */
	private List<AttributeModel> iterate(List<AttributeModel> attributeModels) {
		List<AttributeModel> result = new ArrayList<>();

		for (AttributeModel attributeModel : attributeModels) {
			if (attributeModel.isSearchable()) {
				result.add(attributeModel);
			}

			// also support search on nested attributes
			if (attributeModel.getNestedEntityModel() != null) {
				EntityModel<?> nested = attributeModel.getNestedEntityModel();
				result.addAll(iterate(nested.getAttributeModels()));
			}
		}
		return result;
	}

	@Override
	public void refresh() {
		for (FilterRegion r : regions) {
			if (r.mainValueComponent != null && r.mainValueComponent instanceof Refreshable) {
				((Refreshable) r.mainValueComponent).refresh();
			}
		}
	}

	/**
	 * Restores any previously stored filters
	 * 
	 * @param definitions
	 *            the filter definitions to restore
	 */
	public void restoreFilterDefinitions(List<FlexibleFilterDefinition> definitions) {
		for (FlexibleFilterDefinition def : definitions) {
			FilterRegion region = new FilterRegion(this);
			region.restoring = true;
			region.attributeFilterComboBox.setValue(def.getAttributeModel());
			region.typeFilterCombo.setValue(def.getFlexibleFilterType());

			region.mainValueComponent.setValue(ConvertUtil.convertToPresentationValue(def.getAttributeModel(),
			        def.getValue()));
			if (region.auxValueComponent != null) {
				region.auxValueComponent.setValue(ConvertUtil.convertToPresentationValue(def.getAttributeModel(),
				        def.getValueTo()));
			}
			region.restoring = false;
			regions.add(region);
			getFilterLayout().addComponent(region.getLayout());

		}
	}
}
