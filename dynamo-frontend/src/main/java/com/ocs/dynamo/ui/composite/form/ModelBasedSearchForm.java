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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.CascadeMode;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldFactoryContext;
import com.ocs.dynamo.domain.model.annotation.SearchMode;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.filter.InPredicate;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.Searchable;
import com.ocs.dynamo.ui.component.Cascadable;
import com.ocs.dynamo.ui.component.CustomEntityField;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * A search form that is constructed based on the metadata model
 * 
 * @author bas.rutten
 * @param <ID> The type of the primary key of the entity
 * @param <T>  The type of the entity
 */
public class ModelBasedSearchForm<ID extends Serializable, T extends AbstractEntity<ID>>
		extends AbstractModelBasedSearchForm<ID, T> {

	// the types of search field
	protected enum FilterType {
		BETWEEN, BOOLEAN, ENTITY, ENUM, EQUAL, LIKE
	}

	private static final long serialVersionUID = -7226808613882934559L;

	/**
	 * The main form layout
	 */
	private FormLayout form;

	/**
	 * The various filter groups
	 */
	private Map<String, FilterGroup<T>> groups = new HashMap<>();

	/**
	 * Column width thresholds
	 */
	private List<String> columnThresholds = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param searchable  the component on which to carry out the search
	 * @param entityModel the entity model
	 * @param formOptions the form options
	 */
	public ModelBasedSearchForm(Searchable<T> searchable, EntityModel<T> entityModel, FormOptions formOptions) {
		this(searchable, entityModel, formOptions, null, null);
	}

	/**
	 * Constructor
	 * 
	 * @param searchable     the component on which to carry out the search
	 * @param entityModel    the entity model
	 * @param formOptions    the form options
	 * @param defaultFilters the additional filters to apply to every search action
	 * @param fieldFilters   the filters to apply to the individual search fields
	 */
	public ModelBasedSearchForm(Searchable<T> searchable, EntityModel<T> entityModel, FormOptions formOptions,
			List<SerializablePredicate<T>> defaultFilters, Map<String, SerializablePredicate<?>> fieldFilters) {
		super(searchable, entityModel, formOptions, defaultFilters, fieldFilters);
		setAdvancedSearchMode(formOptions.isStartInAdvancedMode());
	}

	/**
	 * Clears all filters then performs a fresh search
	 */
	@Override
	public void clear() {
		groups.values().forEach(g -> g.reset());
		super.clear();
	}

	/**
	 * Constructs the button bar for the search form
	 */
	@Override
	protected void constructButtonBar(FlexLayout buttonBar) {
		buttonBar.add(constructSearchButton());
		buttonBar.add(constructSearchAnyButton());
		buttonBar.add(constructClearButton());
		buttonBar.add(constructToggleButton());
		buttonBar.add(constructAdvancedSearchModeButton());
	}

	/**
	 * Adds any value change listeners for taking care of cascading search
	 */
	protected void constructCascadeListeners() {
		for (final AttributeModel am : getEntityModel().getCascadeAttributeModels()) {
			if (am.isSearchable()) {
				Component field = groups.get(am.getPath()).getField();
				if (field instanceof HasValue) {
					ValueChangeListener<ValueChangeEvent<?>> cascadeListener = event -> {
						for (String cascadePath : am.getCascadeAttributes()) {
							handleCascade(event, am, cascadePath);
						}
					};
					((HasValue<?, ?>) field).addValueChangeListener(cascadeListener);
				}
			}
		}
	}

	/**
	 * Creates a search field based on an attribute model
	 * 
	 * @param entityModel    the entity model of the entity to search for
	 * @param attributeModel the attribute model the attribute model of the property
	 *                       that is bound to the field
	 * @return
	 */
	protected Component constructField(EntityModel<T> entityModel, AttributeModel attributeModel) {
		Component field = constructCustomField(entityModel, attributeModel);
		if (field == null) {
			EntityModel<?> em = getFieldEntityModel(attributeModel);
			FieldFactoryContext ctx = FieldFactoryContext.create().setAttributeModel(attributeModel)
					.setFieldEntityModel(em).setFieldFilters(getFieldFilters()).setViewMode(false).setSearch(true);
			field = getFieldFactory().constructField(ctx);
		}

		if (field == null) {
			throw new OCSRuntimeException("No field could be constructed for " + attributeModel.getPath());
		}

		return field;

	}

	/**
	 * Constructs a filter group for searching on a single attribute
	 * 
	 * @param entityModel    the entity model
	 * @param attributeModel the attribute model
	 * @return
	 */
	protected FilterGroup<T> constructFilterGroup(EntityModel<T> entityModel, AttributeModel attributeModel) {
		Component field = this.constructField(entityModel, attributeModel);
		if (field != null) {
			FilterType filterType = FilterType.BETWEEN;
			if (String.class.isAssignableFrom(attributeModel.getType())) {
				filterType = FilterType.LIKE;
			} else if (Boolean.class.isAssignableFrom(attributeModel.getType())
					|| Boolean.TYPE.isAssignableFrom(attributeModel.getType())) {
				filterType = FilterType.BOOLEAN;
			} else if (attributeModel.getType().isEnum()) {
				filterType = FilterType.ENUM;
			} else if (AttributeType.ELEMENT_COLLECTION.equals(attributeModel.getAttributeType())) {
				filterType = FilterType.EQUAL;
			} else if (AbstractEntity.class.isAssignableFrom(attributeModel.getType())
					|| AttributeType.DETAIL.equals(attributeModel.getAttributeType())) {
				// search for an entity
				filterType = FilterType.ENTITY;
			} else if (attributeModel.isSearchForExactValue() || attributeModel.isSearchDateOnly()) {
				filterType = FilterType.EQUAL;
			}

			Component comp = field;
			Component auxField = null;
			if (FilterType.BETWEEN.equals(filterType)) {
				// in case of a between value, construct two fields for the
				// lower
				// and upper bounds
				String from = message("ocs.from");
				VaadinUtils.setLabel(field, attributeModel.getDisplayName(VaadinUtils.getLocale()) + " " + from);

				auxField = constructField(entityModel, attributeModel);
				String to = message("ocs.to");
				VaadinUtils.setLabel(auxField, attributeModel.getDisplayName(VaadinUtils.getLocale()) + " " + to);
				auxField.setVisible(true);
				FlexLayout layout = new FlexLayout();
				layout.addClassName(DynamoConstants.CSS_DYNAMO_FLEX_ROW);
				layout.add(field);
				layout.add(auxField);
				comp = layout;
			}
			return new FilterGroup<>(attributeModel, filterType, comp, field, auxField);
		}
		return null;
	}

	/**
	 * Builds the layout that contains the various search filters
	 * 
	 * @param entityModel the entity model
	 * @return
	 */
	@Override
	protected HasComponents constructFilterLayout() {
		form = new FormLayout();

		if (!columnThresholds.isEmpty()) {
			// custom responsive steps
			List<ResponsiveStep> list = new ArrayList<>();
			int i = 0;
			for (String t : columnThresholds) {
				list.add(new ResponsiveStep(t, i + 1));
				i++;
			}
			form.setResponsiveSteps(list);
		} else {
			form.setResponsiveSteps(Lists.newArrayList(new ResponsiveStep("0px", 1), new ResponsiveStep("650px", 2),
					new ResponsiveStep("1300px", 3)));
		}

		// iterate over the searchable attributes and add a field for each
		iterate(getEntityModel().getAttributeModels());
		constructCascadeListeners();

		// hide the search form if there are no search criteria (and no extra search
		// fields)
		if (groups.isEmpty()) {
			form.setVisible(false);
		}
		return form;
	}

	/**
	 * Programmatically force a search
	 * 
	 * @param propertyId the property ID to search on
	 * @param value      the value of the property
	 */
	public <R> void forceSearch(String propertyId, R value) {
		setSearchValue(propertyId, value);
		search();
	}

	/**
	 * Sets a search filter then forces a search
	 * 
	 * @param propertyId the property to search on
	 * @param lower      the lower bound
	 * @param upper      the upper bound
	 */
	public <R> void forceSearch(String propertyId, R lower, R upper) {
		setSearchValue(propertyId, lower, upper);
		search();
	}

	public List<String> getColumnThresholds() {
		return columnThresholds;
	}

	/**
	 * Returns all filter groups
	 * 
	 * @return
	 */
	public Map<String, FilterGroup<T>> getGroups() {
		return groups;
	}

	/**
	 * Handles a cascade event
	 * 
	 * @param event       the event that triggered the cascade
	 * @param am          the attribute model of the property that triggered the
	 *                    cascade
	 * @param cascadePath the path to the property that is the target of the cascade
	 */
	@SuppressWarnings("unchecked")
	private <S> void handleCascade(ValueChangeEvent<?> event, AttributeModel am, String cascadePath) {
		CascadeMode cm = am.getCascadeMode(cascadePath);
		if (CascadeMode.BOTH.equals(cm) || CascadeMode.SEARCH.equals(cm)) {
			HasValue<?, ?> cascadeField = (HasValue<?, ?>) groups.get(cascadePath).getField();
			if (cascadeField instanceof Cascadable) {
				Cascadable<S> ca = (Cascadable<S>) cascadeField;
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
				throw new OCSRuntimeException("Cannot setup cascading from " + am.getPath() + " to " + cascadePath);
			}
		}
	}

	/**
	 * Recursively iterate over the attribute models (including nested models) and
	 * add search fields if the fields are searchable
	 * 
	 * @param attributeModels the attribute models to iterate over
	 */
	private void iterate(List<AttributeModel> attributeModels) {
		for (AttributeModel attributeModel : attributeModels) {
			if (mustShowSearchField(attributeModel)) {
				FilterGroup<T> group = constructFilterGroup(getEntityModel(), attributeModel);
				form.add(group.getFilterComponent());

				// register with the form and set the listener
				group.addListener(this);
				groups.put(group.getPropertyId(), group);
			}

			// also support search on nested attributes
			if (attributeModel.getNestedEntityModel() != null) {
				EntityModel<?> nested = attributeModel.getNestedEntityModel();
				iterate(nested.getAttributeModels());
			}
		}
	}

	/**
	 * Indicates whether a search field for the attribute model must be shown
	 * 
	 * @param attributeModel the attribute model
	 * @return
	 */
	private boolean mustShowSearchField(AttributeModel attributeModel) {
		boolean mustShow = false;
		if (isAdvancedSearchMode()) {
			// in advanced search mode, show all searchable fields
			mustShow = attributeModel.isSearchable();
		} else {
			// in basic mode, only show fields that are always searchable
			mustShow = SearchMode.ALWAYS.equals(attributeModel.getSearchMode());
		}
		return mustShow;
	}

	/**
	 * Callback method that allows the developer to overwrite the filter groups
	 * after they have been created
	 * 
	 * @param groups the filter groups
	 */
	protected void postProcessFilterGroups(Map<String, FilterGroup<T>> groups) {
		// overwrite in subclasses
	}

	/**
	 * Callback method that is called once the processing of the layout is complete.
	 * Allows the developer to modify the layout or add extra components at the end
	 * 
	 * @param layout the main layout
	 */
	@Override
	protected void postProcessLayout(VerticalLayout layout) {
		postProcessFilterGroups(groups);
	}

	/**
	 * Refreshes any fields that are susceptible to this
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void refresh() {
		for (FilterGroup<?> group : getGroups().values()) {
			if (group.getField() instanceof Refreshable) {
				if (getFieldFilters().containsKey(group.getPropertyId())
						&& group.getField() instanceof CustomEntityField) {
					SerializablePredicate<?> ff = getFieldFilters().get(group.getPropertyId());
					((CustomEntityField) group.getField()).refresh(ff);
				} else {
					((Refreshable) group.getField()).refresh();
				}
			}
		}
	}

	public void setColumnThresholds(List<String> columnThresholds) {
		this.columnThresholds = columnThresholds;
	}

	/**
	 * Manually set the value for a certain search field (and clear the value of the
	 * auxiliary search field if present)
	 * 
	 * @param propertyId the ID of the property
	 * @param value      the desired value
	 */
	public <R> void setSearchValue(String propertyId, R value) {
		setSearchValue(propertyId, value, null);
	}

	/**
	 * Manually set the value for a certain search field
	 * 
	 * @param propertyId the ID of the property
	 * @param value      the desired value for the main field
	 * @param auxValue   the desired value for the auxiliary field
	 */
	@SuppressWarnings("unchecked")
	public <R> void setSearchValue(String propertyId, R value, R auxValue) {
		FilterGroup<T> group = groups.get(propertyId);

		if (value != null) {
			((HasValue<?, R>) group.getField()).setValue(value);
		} else {
			((HasValue<?, R>) group.getField()).clear();
		}

		if (group.getAuxField() != null) {
			if (auxValue != null) {
				((HasValue<?, R>) group.getAuxField()).setValue(auxValue);
			} else {
				((HasValue<?, R>) group.getAuxField()).clear();
			}
		}
	}

	@Override
	protected boolean supportsAdvancedSearchMode() {
		return true;
	}

	@Override
	public void toggleAdvancedMode() {

		Map<String, Object> oldValues = new HashMap<>();

		// store groups
		for (Entry<String, FilterGroup<T>> fg : groups.entrySet()) {
			HasValue<?, ?> hv = (HasValue<?, ?>) fg.getValue().getField();
			if (hv.getValue() != null) {
				boolean emptyCollection = hv.getValue() instanceof Collection
						&& ((Collection<?>) hv.getValue()).isEmpty();
				if (!emptyCollection) {
					oldValues.put(fg.getKey(), hv.getValue());
				}
			}
		}

		clear();
		setAdvancedSearchMode(!isAdvancedSearchMode());

		if (isAdvancedSearchMode()) {
			getToggleAdvancedModeButton().setText(message("ocs.to.simple.search.mode"));
		} else {
			getToggleAdvancedModeButton().setText(message("ocs.to.advanced.search.mode"));
		}

		// empty the search form and rebuild it
		form.removeAll();
		groups.clear();
		iterate(getEntityModel().getAttributeModels());

		// restore search values (note that maybe not all fields are there)
		for (Entry<String, Object> entry : oldValues.entrySet()) {
			FilterGroup<T> filterGroup = groups.get(entry.getKey());
			if (filterGroup != null) {
				setSearchValue(entry.getKey(), entry.getValue());
			}
		}
	}

}
