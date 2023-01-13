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

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.*;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.filter.*;
import com.ocs.dynamo.filter.listener.FilterChangeEvent;
import com.ocs.dynamo.filter.listener.FilterListener;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.Searchable;
import com.ocs.dynamo.ui.UIHelper;
import com.ocs.dynamo.ui.component.Cascadable;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.utils.ConvertUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.utils.DateUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.function.SerializablePredicate;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

/**
 *
 * A search form for creating flexible search queries. The form contains
 * functionality for adding and removing filter that can be clicked together
 * from an attribute, an operator and a value
 *
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key
 * @param <T>  the type of the entity
 */
public class ModelBasedFlexibleSearchForm<ID extends Serializable, T extends AbstractEntity<ID>>
		extends AbstractModelBasedSearchForm<ID, T> {

	/**
	 * A region that contains the fields for managing a single filter
	 *
	 * @author bas.rutten
	 *
	 */
	private class FilterRegion {

		/**
		 * The attribute model
		 */
		private AttributeModel am;

		/**
		 * The combo box that contains the attributes to filter on
		 */
		private ComboBox<AttributeModel> attributeFilterComboBox;

		/**
		 * The filter for the auxiliary field
		 */
		private SerializablePredicate<T> auxFilter;

		/**
		 * The component that holds the auxiliary search value
		 */
		private HasValue<?, Object> auxValueComponent;

		/**
		 * The currently active filter for the entire region (calculated by composing
		 * the mainFilter and the auxFieldFilter)
		 */
		private SerializablePredicate<T> fieldFilter;

		/**
		 * The filter type
		 */
		private FlexibleFilterType filterType;

		/**
		 * The main layout
		 */
		@Getter
		private FlexLayout layout;

		/**
		 * The FilterListener that listens for filter changes
		 */
		private FilterListener<T> listener;

		/**
		 * The filter for the main field
		 */
		private SerializablePredicate<T> mainFilter;

		/**
		 * The component that holds the main search value
		 */
		private HasValue<?, Object> mainValueComponent;

		/**
		 * Label to display when no filter has been selected
		 */
		private Span noFilterLabel;

		/**
		 * The button used to remove the filter
		 */
		private Button removeButton;

		/**
		 * Indicates whether we are restoring an existing definition - if this is the
		 * case the we do not need to set a default filter value
		 */
		private boolean restoring;

		/**
		 * The combo box that contains the available filter types
		 */
		private ComboBox<FlexibleFilterType> typeFilterCombo;

		/**
		 * Constructor
		 *
		 * @param listener the filter listener
		 */
		FilterRegion(FilterListener<T> listener) {
			this.listener = listener;
			layout = new FlexLayout();
			layout.setSizeFull();
			layout.setFlexWrap(FlexWrap.WRAP);
			layout.addClassName(DynamoConstants.CSS_DYNAMO_FLEX_ROW);

			createRemoveButton();
			createAttributeFilterComboBox();

			noFilterLabel = new Span(message("ocs.select.filter"));
			noFilterLabel.setText("");
			layout.add(noFilterLabel);
		}

		/**
		 * Adds cascase listeners to a component
		 * 
		 * @param newComponent the component
		 */
		private void addCascadeListeners(Component newComponent) {
			if (am.getCascadeAttributes() != null && !am.getCascadeAttributes().isEmpty()) {
				for (String cascadePath : am.getCascadeAttributes()) {
					ValueChangeListener<ValueChangeEvent<?>> listener = event -> handleCascade(event, am, cascadePath);
					((HasValue<?, ?>) newComponent).addValueChangeListener(listener);
				}
			}
		}

		/**
		 * Adds a value change listener to a component
		 * 
		 * @param newComponent the component to add the listener to
		 */
		private void addValueChangeListener(Component newComponent) {
			if (newComponent instanceof HasValue) {
				ValueChangeListener<ValueChangeEvent<?>> listener = event -> handleValueChange(
						(HasValue<?, ?>) newComponent, event.getValue());
				((HasValue<?, ?>) newComponent).addValueChangeListener(listener);
			}
		}

		private SerializablePredicate<T> constructBetweenFilter(HasValue<?, ?> field, Object value) {
			SerializablePredicate<T> filter;
			// construct new filter for the selected field (or clear it)
			if (field == this.auxValueComponent) {
				// filter for the auxiliary field
				if (value != null) {
					auxFilter = new LessOrEqualPredicate<>(am.getPath(), value);
				} else {
					auxFilter = null;
				}
			} else {
				// filter for the main field
				if (value != null) {
					mainFilter = new GreaterOrEqualPredicate<>(am.getPath(), value);
				} else {
					mainFilter = null;
				}
			}

			// construct the aggregate filter
			if (auxFilter != null && mainFilter != null) {
				filter = new BetweenPredicate<>(am.getPath(),
						(Comparable<?>) ((GreaterOrEqualPredicate<?>) mainFilter).getValue(),
						(Comparable<?>) ((LessOrEqualPredicate<?>) auxFilter).getValue());
			} else if (auxFilter != null) {
				filter = auxFilter;
			} else {
				filter = mainFilter;
			}
			return filter;
		}

		/**
		 * Constructs the search predicate based on the field value
		 * 
		 * @param field the input field
		 * @param value the field value
		 * @return
		 */
		private SerializablePredicate<T> constructFilter(HasValue<?, ?> field, Object value) {
			SerializablePredicate<T> filter = null;

			value = convertDateOnlyFilter(field, value);

			switch (this.filterType) {
			case BETWEEN:
				filter = constructBetweenFilter(field, value);
				break;
			case LESS_OR_EQUAL:
				filter = new LessOrEqualPredicate<>(am.getPath(), value);
				break;
			case LESS_THAN:
				filter = new LessThanPredicate<>(am.getPath(), value);
				break;
			case GREATER_OR_EQUAL:
				filter = new GreaterOrEqualPredicate<>(am.getPath(), value);
				break;
			case GREATER_THAN:
				filter = new GreaterThanPredicate<>(am.getPath(), value);
				break;
			case CONTAINS:
				// like filter for comparing string fields
				filter = createStringFilter(value, false);
				break;
			case NOT_EQUAL:
				filter = new NotPredicate<>(new EqualsPredicate<>(am.getPath(), value));
				break;
			case STARTS_WITH:
				// like filter for comparing string fields
				filter = createStringFilter(value, true);
				break;
			case NOT_CONTAINS:
				filter = new NotPredicate<>(createStringFilter(value, false));
				break;
			case NOT_STARTS_WITH:
				filter = new NotPredicate<>(createStringFilter(value, true));
				break;
			default:
				filter = convertFilterDefault(value, filter);
				break;
			}
			return filter;
		}

		/**
		 * default conversion of search filter value.
		 * 
		 * @param value
		 * @param filter
		 * @return
		 */
		private SerializablePredicate<T> convertFilterDefault(Object value, SerializablePredicate<T> filter) {
			if (value != null && am.isSearchDateOnly() && am.isSearchForExactValue()) {
				filter = convertDateOnlyExactMatchFilter(value);
			} else if (value != null && !(value instanceof Collection && ((Collection<?>) value).isEmpty())) {
				// default case (simple comparison or non-empty collection)
				filter = new EqualsPredicate<>(am.getPath(), value);
			}
			return filter;
		}

		private SerializablePredicate<T> convertDateOnlyExactMatchFilter(Object value) {
			SerializablePredicate<T> filter;
			LocalDate ldt = (LocalDate) value;
			if (LocalDateTime.class.equals(am.getType())) {
				filter = new BetweenPredicate<>(am.getPath(), ldt.atStartOfDay(),
						ldt.atStartOfDay().plusDays(1).minusNanos(1));
			} else {
				// zoned date time
				filter = new BetweenPredicate<>(am.getPath(), ldt.atStartOfDay(VaadinUtils.getTimeZoneId()),
						ldt.atStartOfDay(VaadinUtils.getTimeZoneId()).plusDays(1).minusNanos(1));
			}
			return filter;
		}

		/**
		 * Converts a "date only" filter value by adding the time stamp.
		 * 
		 * @param field
		 * @param value
		 * @return
		 */
		private Object convertDateOnlyFilter(HasValue<?, ?> field, Object value) {
			if (am.isSearchDateOnly() && !am.isSearchForExactValue()) {
				if (value != null) {
					if (LocalDateTime.class.equals(am.getType())) {
						if (field == this.auxValueComponent) {
							LocalDate ldt = (LocalDate) value;
							value = ldt.atStartOfDay().plusDays(1).minusNanos(1);
						} else {
							LocalDate ldt = (LocalDate) value;
							value = ldt.atStartOfDay();
						}
					} else if (ZonedDateTime.class.equals(am.getType())) {
						if (field == this.auxValueComponent) {
							LocalDate ldt = (LocalDate) value;
							value = ldt.atStartOfDay(VaadinUtils.getTimeZoneId()).plusDays(1).minusNanos(1);
						} else {
							LocalDate ldt = (LocalDate) value;
							value = ldt.atStartOfDay(VaadinUtils.getTimeZoneId());
						}
					}
				}
			}
			return value;
		}

		/**
		 * Creates a combo box for selecting the filter type
		 */
		private void createAttributeFilterComboBox() {
			attributeFilterComboBox = new ComboBox<>(message("ocs.filter"));
			attributeFilterComboBox.setWidth("250px");

			// find out which attributes can be searched on and sort them in
			// alphabetical order
			List<AttributeModel> filteredModels = iterate(getEntityModel().getAttributeModels());
			filteredModels.sort((o1, o2) -> o1.getDisplayName(VaadinUtils.getLocale())
					.compareToIgnoreCase(o2.getDisplayName(VaadinUtils.getLocale())));
			// add any attribute models that are not required
			attributeFilterComboBox.setItems(filteredModels.stream()
					.filter(a -> !a.isRequiredForSearching() || !hasFilter(a)).toList());
			attributeFilterComboBox.setItemLabelGenerator(item -> item.getDisplayName(VaadinUtils.getLocale()));

			// add a value change listener that fills the filter type combo box
			// after a change
			ValueChangeListener<ValueChangeEvent<?>> vcl = e -> handleFilterAttributeChange(e, restoring);
			attributeFilterComboBox.addValueChangeListener(vcl);
			layout.add(attributeFilterComboBox);
		}

		/**
		 * Creates an auxiliary component
		 * 
		 * @param context      the component creation context
		 * @param newComponent the component for which to add an auxiliary component
		 */
		@SuppressWarnings("unchecked")
		private void createAuxComponent(FieldCreationContext context, Component newComponent) {
			Component newAuxComponent = factory.constructField(context);
			ValueChangeListener<ValueChangeEvent<?>> auxListener = event -> handleValueChange(
					(HasValue<?, ?>) newAuxComponent, event.getValue());
			((HasValue<?, ?>) newAuxComponent).addValueChangeListener(auxListener);

			VaadinUtils.setLabel(newComponent, am.getDisplayName(VaadinUtils.getLocale()) + " " + message("ocs.from"));
			VaadinUtils.setLabel(newAuxComponent, am.getDisplayName(VaadinUtils.getLocale()) + " " + message("ocs.to"));

			if (auxValueComponent == null) {
				layout.add(newAuxComponent);
			} else {
				layout.replace((Component) auxValueComponent, newAuxComponent);
			}
			auxValueComponent = (HasValue<?, Object>) newAuxComponent;
		}

		/**
		 * Creates the button for removing a filter row
		 */
		private void createRemoveButton() {
			removeButton = new Button("");
			removeButton.setIcon(VaadinIcon.TRASH.create());
			removeButton.addClickListener(e -> {
				VerticalLayout parent = (VerticalLayout) layout.getParent().orElse(null);
				parent.remove(layout);

				regions.remove(FilterRegion.this);
				if (am != null) {
					FilterRegion.this.listener.onFilterChange(FilterChangeEvent.<T>builder().propertyId(am.getPath())
							.oldFilter(fieldFilter).newFilter(null).build());
				}
			});
			layout.add(removeButton);
			removeButton.setClassName("flexRemoveButton");
		}

		/**
		 * Creates a SimpleStringFilter based on the provided parameters
		 *
		 * @param value      the value to search on
		 * @param prefixOnly whether to match by prefix only
		 * @return
		 */
		private SimpleStringPredicate<T> createStringFilter(Object value, boolean prefixOnly) {
			String valueStr = value == null ? "" : value.toString();
			if (StringUtils.isNotEmpty(valueStr)) {
				return new SimpleStringPredicate<>(am.getPath(), valueStr, prefixOnly, am.isSearchCaseSensitive());
			}
			return null;
		}

		/**
		 * Respond to a change in the selected attribute by updating the filter
		 * components
		 * 
		 * @param attributeModel the selected attribute model
		 * @param restoring      whether we are restoring an existing filter during
		 *                       screen construction
		 */
		private void filterAttributeChange(AttributeModel attributeModel, boolean restoring) {
			this.am = attributeModel;
			if (am != null) {
				replaceFilters(restoring);
			} else {
				removeFilters();
			}
			noFilterLabel.setVisible(this.am == null);
		}

		/**
		 * Returns the available filter types for a certain property
		 *
		 * @param am the attribute model of the property
		 * @return
		 */
		private List<FlexibleFilterType> getFilterTypes(AttributeModel am) {
			List<FlexibleFilterType> result = new ArrayList<>();
			result.add(FlexibleFilterType.EQUALS);
			if (!am.isSearchForExactValue()) {
				switch (am.getAttributeType()) {
				case BASIC:
					if (String.class.equals(am.getType())) {
						if (basicStringFilterProperties.contains(am.getPath())) {
							result.add(FlexibleFilterType.CONTAINS);
							result.add(FlexibleFilterType.STARTS_WITH);
						} else {
							result.add(FlexibleFilterType.NOT_EQUAL);
							result.add(FlexibleFilterType.CONTAINS);
							result.add(FlexibleFilterType.STARTS_WITH);
							result.add(FlexibleFilterType.NOT_CONTAINS);
							result.add(FlexibleFilterType.NOT_STARTS_WITH);
						}
					} else if (Enum.class.isAssignableFrom(am.getType())) {
						result.add(FlexibleFilterType.NOT_EQUAL);
					} else if (Number.class.isAssignableFrom(am.getType())
							|| (!am.isSearchDateOnly() && DateUtils.isSupportedDateType(am.getType()))) {
						result.add(FlexibleFilterType.BETWEEN);
						result.add(FlexibleFilterType.LESS_THAN);
						result.add(FlexibleFilterType.LESS_OR_EQUAL);
						result.add(FlexibleFilterType.GREATER_OR_EQUAL);
						result.add(FlexibleFilterType.GREATER_THAN);
					} else if (am.isSearchDateOnly() && DateUtils.isSupportedDateType(am.getType())) {
						// for a date only comparison, an exact match does not make sense
						result.remove(FlexibleFilterType.EQUALS);
						result.add(FlexibleFilterType.BETWEEN);
						result.add(FlexibleFilterType.LESS_THAN);
						result.add(FlexibleFilterType.LESS_OR_EQUAL);
						result.add(FlexibleFilterType.GREATER_OR_EQUAL);
						result.add(FlexibleFilterType.GREATER_THAN);
					}
					break;
				case MASTER:
				case DETAIL:
					// also support "not equal" for entities
					result.add(FlexibleFilterType.NOT_EQUAL);
					break;
				default:
					break;
				}
			}
			return result;
		}

		/**
		 * Handles a cascading search (responds to a value change by propagating the
		 * value change to all cascaded fields)
		 * 
		 * @param event       the search event
		 * @param am          the attribute model
		 * @param cascadePath the cascade path
		 */
		@SuppressWarnings("unchecked")
		private <S> void handleCascade(ValueChangeEvent<?> event, AttributeModel am, String cascadePath) {
			CascadeMode cm = am.getCascadeMode(cascadePath);
			if (CascadeMode.BOTH.equals(cm) || CascadeMode.SEARCH.equals(cm)) {
				FilterRegion region = getFilterRegion(cascadePath);
				if (region != null && region.mainValueComponent != null) {
					if (region.mainValueComponent instanceof Cascadable) {
						Cascadable<S> ca = (Cascadable<S>) region.mainValueComponent;
						if (event.getValue() == null) {
							ca.clearAdditionalFilter();
						} else {
							if (event.getValue() instanceof Collection) {
								ca.setAdditionalFilter(new InPredicate<S>(am.getCascadeFilterPath(cascadePath),
										(Collection<S>) event.getValue()));
							} else {
								ca.setAdditionalFilter(
										new EqualsPredicate<S>(am.getCascadeFilterPath(cascadePath), event.getValue()));
							}
						}
					} else {
						// field not found or does not support cascading
						throw new OCSRuntimeException(
								"Cannot setup cascading from " + am.getPath() + " to " + cascadePath);
					}
				}
			}
		}

		/**
		 * Handle a change of the attribute to filter on
		 *
		 * @param event the event
		 */
		private void handleFilterAttributeChange(HasValue.ValueChangeEvent<?> event, boolean restoring) {
			AttributeModel temp = (AttributeModel) event.getValue();

			if (mainValueComponent != null) {
				mainValueComponent.clear();
			}
			if (auxValueComponent != null) {
				auxValueComponent.clear();
			}

			filterAttributeChange(temp, restoring);
		}

		/**
		 * Handle a change of the filter by updating/creating the UI components that are
		 * used for actually entering the value to search on
		 *
		 * @param type the selected filter type
		 */
		@SuppressWarnings("unchecked")
		private void handleFilterTypeChange(FlexibleFilterType type) {
			filterType = type;

			Object oldValue = mainValueComponent == null ? null : mainValueComponent.getValue();
			Object oldAuxValue = auxValueComponent == null ? null : auxValueComponent.getValue();

			Component custom = findCustomComponent(getEntityModel(), am);
			FieldCreationContext context = FieldCreationContext.create().attributeModel(am)
					.fieldEntityModel(getFieldEntityModel(am)).fieldFilters(getFieldFilters()).viewMode(false)
					.search(true).build();
			Component newComponent = custom != null ? custom : factory.constructField(context);

			addValueChangeListener(newComponent);
			addCascadeListeners(newComponent);

			if (mainValueComponent == null) {
				layout.add(newComponent);
			} else {
				layout.replace((Component) mainValueComponent, newComponent);
			}

			mainValueComponent = (HasValue<?, Object>) newComponent;

			if (FlexibleFilterType.BETWEEN.equals(filterType)) {
				createAuxComponent(context, newComponent);
			} else {
				// no need for the auxiliary field
				if (auxValueComponent != null) {
					layout.remove((Component) auxValueComponent);
					auxValueComponent = null;
				}
			}

			restoreOldValues(oldValue, oldAuxValue);
		}

		/**
		 * Respond to a change of the value in one of the search fields
		 *
		 * @param field the changed field (can either be the main field or the auxiliary
		 *              field)
		 * @param value the new field value
		 */
		private void handleValueChange(HasValue<?, ?> field, Object value) {
			// store the current filter
			SerializablePredicate<T> oldFilter = fieldFilter;
			// convert the value to its actual representation
			Result<?> result = ConvertUtils.convertToModelValue(am, value);
			result.ifOk(r -> {
				((HasValidation) field).setErrorMessage(null);
				SerializablePredicate<T> filter = constructFilter(field, r);
				// store the current filter
				this.fieldFilter = filter;
				// propagate the change (this will trigger the actual search action)
				listener.onFilterChange(FilterChangeEvent.<T>builder().propertyId(am.getPath()).oldFilter(oldFilter)
						.newFilter(filter).build());
			});
			result.ifError(r -> ((HasValidation) field).setErrorMessage(r));
		}

		private void removeFilters() {
			// no filter selected, remove everything
			if (typeFilterCombo != null) {
				layout.remove(typeFilterCombo);
			}
			if (mainValueComponent != null) {
				layout.remove((Component) mainValueComponent);
			}
			if (auxValueComponent != null) {
				layout.remove((Component) auxValueComponent);
			}
		}

		/**
		 * Rebuild any currently selected filters
		 * 
		 * @param restoring whether we are restoring an existing set of filters
		 */
		private void replaceFilters(boolean restoring) {
			ComboBox<FlexibleFilterType> newTypeFilterCombo = new ComboBox<>(message("ocs.type"));
			newTypeFilterCombo.setWidth("250px");
			ValueChangeListener<ValueChangeEvent<FlexibleFilterType>> ccl = event -> handleFilterTypeChange(
					event.getValue());
			newTypeFilterCombo.addValueChangeListener(ccl);
			newTypeFilterCombo.setItems(getFilterTypes(am));
			newTypeFilterCombo.setItemLabelGenerator(item -> getMessageService()
					.getEnumMessage(FlexibleFilterType.class, item, VaadinUtils.getLocale()));

			// cannot remove mandatory filters
			removeButton.setEnabled(!am.isRequiredForSearching());
			attributeFilterComboBox.setEnabled(!am.isRequiredForSearching());

			if (typeFilterCombo != null) {
				layout.replace(typeFilterCombo, newTypeFilterCombo);
			} else {
				layout.add(newTypeFilterCombo);
			}

			// hide the value component(s) after a filter change
			if (mainValueComponent != null) {
				layout.remove((Component) mainValueComponent);
			}
			if (auxValueComponent != null) {
				layout.remove((Component) auxValueComponent);
			}

			typeFilterCombo = newTypeFilterCombo;

			// select the first value and disable the component if there
			// is just one component
			if (!restoring) {
				typeFilterCombo.setValue(getDefaultFilterType(am));
			}

			// disable if there is only one option
			if (getFilterTypes(am).size() == 1) {
				typeFilterCombo.setEnabled(false);
			}
		}

		private void restoreOldValues(Object oldValue, Object oldAuxValue) {
			try {
				if (oldValue != null) {
					mainValueComponent.setValue(oldValue);
				}
				if (oldAuxValue != null && auxValueComponent != null) {
					auxValueComponent.setValue(oldAuxValue);
				}
			} catch (Exception ex) {
				// do nothing
			}
		}

		/**
		 * Extracts the filter definition from the region
		 *
		 * @return
		 */
		public FlexibleFilterDefinition toDefinition() {
			if (mainValueComponent != null) {

				FlexibleFilterDefinition.FlexibleFilterDefinitionBuilder builder = FlexibleFilterDefinition.builder();

				builder.flexibleFilterType(filterType);
				builder.attributeModel(am);
				Result<?> result = ConvertUtils.convertToModelValue(am, mainValueComponent.getValue());
				result.ifOk(v -> builder.value(v));
				if (auxValueComponent != null) {
					result = ConvertUtils.convertToModelValue(am, auxValueComponent.getValue());
					result.ifOk(v -> builder.valueTo(v));
				}
				return builder.build();
			}
			return null;
		}
	}

	private static final long serialVersionUID = -6668770373597055403L;

	/**
	 * The button that is used to add a new filter
	 */
	@Getter
	private Button addFilterButton;

	/**
	 * Path of the properties for which to offer only basic String search
	 * capabilities
	 */
	@Getter
	@Setter
	private Set<String> basicStringFilterProperties = new HashSet<>();

	/**
	 * The field factory
	 */
	private FieldFactory factory = FieldFactory.getInstance();

	/**
	 * The filter regions
	 */
	private List<FilterRegion> regions = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param searchable  the component on which to carry out the search
	 * @param entityModel the entity model
	 * @param formOptions the form options
	 */
	public ModelBasedFlexibleSearchForm(Searchable<T> searchable, EntityModel<T> entityModel, FormOptions formOptions) {
		this(searchable, entityModel, formOptions, null, null);
	}

	/**
	 * Constructor
	 *
	 * @param searchable     the component on which to carry out the search
	 * @param entityModel    the entity model
	 * @param formOptions    the form options
	 * @param defaultFilters the additional filters to apply to every search action
	 * @param fieldFilters   a map of filters to apply to the individual fields
	 */
	public ModelBasedFlexibleSearchForm(Searchable<T> searchable, EntityModel<T> entityModel, FormOptions formOptions,
			List<SerializablePredicate<T>> defaultFilters, Map<String, SerializablePredicate<?>> fieldFilters) {
		super(searchable, entityModel, formOptions, defaultFilters, fieldFilters);
	}

	/**
	 * Programmatically add a search filter for a property if there is no filter
	 * present yet. Otherwise, set the value(s) of the already present filter to the
	 * provided values
	 *
	 * @param am the attribute model to base the filter on
	 * @param filterType     the type of the filter
	 * @param value          the value of the filter
	 * @param auxValue       the auxiliary value
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addFilter(AttributeModel am, FlexibleFilterType filterType, Object value, Object auxValue) {

		// check if multiple values are allowed
		boolean multi = Collection.class.isAssignableFrom(am.getType()) || am.isMultipleSearch();
		FilterRegion match = regions.stream()
				.filter(region -> region.am != null && region.am.getPath().equals(am.getPath())).findFirst()
				.orElse(null);

		// no matching filter found, create a new one
		if (match == null) {
			addFilterRegion();
			match = regions.get(regions.size() - 1);
			match.attributeFilterComboBox.setValue(am);
			match.filterAttributeChange(am, true);
			match.typeFilterCombo.setValue(filterType);
		}

		if (multi) {
			List list = new ArrayList();
			// preserve existing values
			if (match.mainValueComponent.getValue() != null) {
				Collection col = (Collection) match.mainValueComponent.getValue();
				list.addAll(col);
			}

			// add new value
			if (value != null) {
				list.add(value);
			}
			match.mainValueComponent.setValue(list);
		} else {
			// singular value, simply set the value
			match.typeFilterCombo.setValue(filterType);
			if (value != null) {
				match.mainValueComponent.setValue(value);
			} else {
				match.mainValueComponent.clear();
			}
			if (match.auxValueComponent != null) {
				if (auxValue != null) {
					match.auxValueComponent.setValue(auxValue);
				} else {
					match.auxValueComponent.clear();
				}
			}
		}
	}

	/**
	 * Adds a new (empty) filter in response to a button click
	 */
	private void addFilterRegion() {
		FilterRegion region = new FilterRegion(this);
		regions.add(region);
		getFilterLayout().add(region.getLayout());
		toggle(true);
	}

	/**
	 * Clears all search filters
	 */
	@Override
	public void clear() {
		// remove any non-required filter regions and clear the rest
		Iterator<FilterRegion> it = regions.iterator();
		while (it.hasNext()) {
			FilterRegion fr = it.next();
			if (fr.am == null || !fr.am.isRequiredForSearching()) {
				getFilterLayout().remove(fr.getLayout());
				it.remove();
			} else {
				fr.mainValueComponent.clear();
				if (fr.auxValueComponent != null) {
					fr.auxValueComponent.clear();
				}
			}
		}
		super.clear();
	}

	/**
	 * Constructs the main button bar
	 */
	@Override
	protected void constructButtonBar(FlexLayout buttonBar) {

		// construct button for adding a new filter
		addFilterButton = new Button(message("ocs.add.filter"));
		addFilterButton.setIcon(VaadinIcon.PLUS.create());
		addFilterButton.addClickListener(evenbt -> addFilterRegion());

		buttonBar.add(addFilterButton);
		buttonBar.add(constructSearchButton());
		buttonBar.add(constructSearchAnyButton());
		buttonBar.add(constructClearButton());
		buttonBar.add(constructToggleButton());
	}

	@Override
	protected VerticalLayout constructFilterLayout() {
		// just an empty layout - filters will be added on the fly
		DefaultVerticalLayout layout = new DefaultVerticalLayout();
		layout.addClassName("dynamoFlexFilterLayout");
		return layout;
	}

	/**
	 * Extracts a list of FlexibleFilterDefinitions form the currently active search
	 * filters
	 */
	public List<FlexibleFilterDefinition> extractFilterDefinitions() {
		return regions.stream().map(FilterRegion::toDefinition).filter(Objects::nonNull).toList();
	}

	/**
	 * Returns the default filter type for a certain property
	 *
	 * @param am the attribute model the attribute model of the property
	 */
	public FlexibleFilterType getDefaultFilterType(AttributeModel am) {
		if (am.isSearchForExactValue()) {
			return FlexibleFilterType.EQUALS;
		}

		if (AttributeType.BASIC.equals(am.getAttributeType())) {
			if (String.class.equals(am.getType())) {
				return FlexibleFilterType.CONTAINS;
			} else if (Enum.class.isAssignableFrom(am.getType())) {
				return FlexibleFilterType.EQUALS;
			} else if (Number.class.isAssignableFrom(am.getType()) || DateUtils.isJava8DateType(am.getType())) {
				return (am.isSearchForExactValue() && am.isSearchDateOnly()) ? FlexibleFilterType.EQUALS
						: FlexibleFilterType.BETWEEN;
			}
		}
		return FlexibleFilterType.EQUALS;
	}

	/**
	 * Returns a filter region for the specified attribute model, or
	 * <code>null</code> if such a region does not exist
	 *
	 * @param attributeModel the attribute model
	 * @return
	 */
	public FilterRegion getFilterRegion(AttributeModel attributeModel) {
		return regions.stream().filter(r -> Objects.equals(attributeModel, r.am)).findFirst().orElse(null);
	}

	/**
	 * Finds a filter region for the specified attribute path
	 * 
	 * @param path the attribute path
	 * @return
	 */
	public FilterRegion getFilterRegion(String path) {
		return regions.stream().filter(r -> Objects.equals(path, r.am.getPath())).findFirst().orElse(null);
	}

	/**
	 * Indicates whether the search form already contains a filter for the provided
	 * attribute model
	 *
	 * @param attributeModel the attribute model
	 * @return
	 */
	public boolean hasFilter(AttributeModel attributeModel) {
		return regions.stream().anyMatch(r -> Objects.equals(attributeModel, r.am));
	}

	/**
	 * Iterates over the available attribute models and constructs a list of the
	 * attributes that can be searched
	 *
	 * @param attributeModels the attribute models
	 * @return
	 */
	private List<AttributeModel> iterate(List<AttributeModel> attributeModels) {
		List<AttributeModel> result = new ArrayList<>();

		for (AttributeModel am : attributeModels) {
			if (am.isSearchable()) {
				result.add(am);
			}

			// also support search on nested attributes
			if (am.getNestedEntityModel() != null) {
				EntityModel<?> nested = am.getNestedEntityModel();
				result.addAll(iterate(nested.getAttributeModels()));
			}
		}
		return result;
	}

	/**
	 * Refreshes the lookup components
	 */
	@Override
	public void refresh() {
		for (FilterRegion r : regions) {
			if (r.mainValueComponent instanceof Refreshable) {
				((Refreshable) r.mainValueComponent).refresh();
			}
			// not needed to check aux component since only lookup fields
			// can be refreshable
		}
	}

	/**
	 * Restores any previously stored filters
	 *
	 * @param definitions the filter definitions to restore
	 */
	public void restoreFilterDefinitions(List<FlexibleFilterDefinition> definitions) {
		for (FlexibleFilterDefinition def : definitions) {
			FilterRegion region = new FilterRegion(this);
			region.restoring = true;
			region.attributeFilterComboBox.setValue(def.getAttributeModel());
			region.filterAttributeChange(def.getAttributeModel(), true);
			region.typeFilterCombo.setValue(def.getFlexibleFilterType());

			Object value = ConvertUtils.convertToPresentationValue(def.getAttributeModel(), def.getValue());
			if (value != null) {
				region.mainValueComponent.setValue(value);
			} else {
				region.mainValueComponent.clear();
			}

			if (region.auxValueComponent != null) {
				Object auxValue = ConvertUtils.convertToPresentationValue(def.getAttributeModel(), def.getValueTo());
				if (auxValue != null) {
					region.auxValueComponent.setValue(auxValue);
				} else {
					region.auxValueComponent.clear();
				}
			}
			region.restoring = false;
			regions.add(region);

			getFilterLayout().add(region.getLayout());
		}
	}

	/**
	 * Restores previously cached search filters
	 */
	@Override
	protected void restoreSearchValues() {
		UIHelper helper = ServiceLocatorFactory.getServiceLocator().getService(UIHelper.class);
		if (helper != null) {
			List<FlexibleFilterDefinition> filters = helper.retrieveSearchFilterDefinitions();
			restoreFilterDefinitions(filters);
		}
	}

	/**
	 * Stores search filters
	 */
	@Override
	protected void storeSearchFilters() {
		UIHelper helper = ServiceLocatorFactory.getServiceLocator().getService(UIHelper.class);
		if (helper != null) {
			helper.storeSearchFilterDefinitions(extractFilterDefinitions());
		}
	}

	@Override
	protected boolean supportsAdvancedSearchMode() {
		return false;
	}

	@Override
	public void toggleAdvancedMode() {
		// not needed
	}
}
