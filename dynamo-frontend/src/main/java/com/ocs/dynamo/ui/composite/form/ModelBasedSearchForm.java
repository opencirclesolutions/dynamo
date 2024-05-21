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

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.CascadeMode;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldCreationContext;
import com.ocs.dynamo.domain.model.annotation.SearchMode;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.filter.BetweenPredicate;
import com.ocs.dynamo.filter.CompositePredicate;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.filter.InPredicate;
import com.ocs.dynamo.filter.LessOrEqualPredicate;
import com.ocs.dynamo.filter.PropertyPredicate;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.Searchable;
import com.ocs.dynamo.ui.UIHelper;
import com.ocs.dynamo.ui.component.Cascadable;
import com.ocs.dynamo.ui.component.CustomEntityField;
import com.ocs.dynamo.ui.composite.ComponentContext;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.utils.ConvertUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.vaadin.componentfactory.EnhancedFormLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.Getter;

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

	private EnhancedFormLayout form;

	@Getter
	private final Map<String, FilterGroup<T>> groups = new HashMap<>();

	/**
	 * Constructor
	 * 
	 * @param searchable     the component that will display the search results
	 * @param entityModel    the entity model
	 * @param formOptions    the form options
	 * @param defaultFilters the additional filters to apply to every search action
	 * @param fieldFilters   the filters to apply to the individual search fields
	 */
	public ModelBasedSearchForm(Searchable<T> searchable, EntityModel<T> entityModel, FormOptions formOptions,
			List<SerializablePredicate<T>> defaultFilters, Map<String, SerializablePredicate<?>> fieldFilters) {
		this(searchable, entityModel, formOptions, ComponentContext.<ID, T>builder().build(), defaultFilters,
				fieldFilters);
	}

	/**
	 * Constructor
	 * 
	 * @param searchable     the component that will display the search results
	 * @param entityModel    the entity model
	 * @param formOptions    the form options
	 * @param context        the component context
	 * @param defaultFilters the additional filters to apply to every search action
	 * @param fieldFilters   the filters to apply to the individual search fields
	 */
	public ModelBasedSearchForm(Searchable<T> searchable, EntityModel<T> entityModel, FormOptions formOptions,
			ComponentContext<ID, T> context, List<SerializablePredicate<T>> defaultFilters,
			Map<String, SerializablePredicate<?>> fieldFilters) {
		super(searchable, entityModel, formOptions, defaultFilters, fieldFilters);
		setComponentContext(context);

		boolean advancedModeSaved = false;
		UIHelper helper = ServiceLocatorFactory.getServiceLocator().getService(UIHelper.class);
		if (helper != null && formOptions.isPreserveAdvancedMode()) {
			advancedModeSaved = helper.retrieveAdvancedMode();
		}
		setAdvancedSearchMode(formOptions.isStartInAdvancedMode() || advancedModeSaved);
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
	 * Adds the value change listeners for taking care of cascading search
	 */
	protected void constructCascadeListeners() {
		for (AttributeModel am : getEntityModel().getCascadeAttributeModels()) {
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
	 * Creates a search component based on an attribute model
	 * 
	 * @param entityModel    the entity model of the entity to search for
	 * @param attributeModel the attribute model the attribute model of the property
	 *                       that is bound to the field
	 * @return the component that was created
	 */
	protected Component constructComponent(EntityModel<T> entityModel, AttributeModel attributeModel) {
		Component component = constructCustomComponent(entityModel, attributeModel)
				.orElseGet(() -> {
			EntityModel<?> em = getFieldEntityModel(attributeModel);
			FieldCreationContext ctx = FieldCreationContext.create().attributeModel(attributeModel).fieldEntityModel(em)
					.fieldFilters(getFieldFilters()).viewMode(false).search(true).build();
			return getFieldFactory().constructField(ctx);
		});

		if (component == null) {
			throw new OCSRuntimeException("No field could be constructed for %s".formatted(attributeModel.getPath()));
		}

		return component;
	}

	/**
	 * Constructs a filter group for searching on a single attribute
	 * 
	 * @param entityModel    the entity model
	 * @param attributeModel the attribute model
	 * @return the constructed filter group
	 */
	protected FilterGroup<T> constructFilterGroup(EntityModel<T> entityModel, AttributeModel attributeModel) {
		Component field = this.constructComponent(entityModel, attributeModel);
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
			} else if (attributeModel.isSearchForExactValue()) {
				filterType = FilterType.EQUAL;
			}

			Component comp = field;
			Component auxField = null;
			if (FilterType.BETWEEN.equals(filterType)) {
				// in case of a between value, construct two fields for the
				// lower
				// and upper bounds
				String from = message("ocs.from");
				setLabelAndPlaceHolder(attributeModel, field, from);

				auxField = constructComponent(entityModel, attributeModel);
				String to = message("ocs.to");
				setLabelAndPlaceHolder(attributeModel, auxField, to);
				auxField.setVisible(true);

				FlexLayout layout = new FlexLayout();
				layout.setFlexWrap(FlexWrap.WRAP);
				layout.setSizeFull();
				layout.addClassName(DynamoConstants.CSS_DYNAMO_FLEX_ROW);
				layout.add(field);
				layout.add(auxField);

				layout.setFlexGrow(1, field);
				layout.setFlexGrow(1, auxField);

				comp = layout;
			}
			return new FilterGroup<>(attributeModel, filterType, comp, field, auxField);
		}
		return null;
	}

	/**
	 * Sets modified label and placeholder in case there is an auxiliary search
	 * field
	 * 
	 * @param attributeModel the attribute model
	 * @param component      the component to modify
	 * @param postFix        the post fix to add to the label/placeholder
	 */
	private void setLabelAndPlaceHolder(AttributeModel attributeModel, Component component, String postFix) {
		String message = attributeModel.getDisplayName(VaadinUtils.getLocale()) + " " + postFix;
		VaadinUtils.setLabel(component, message);
		VaadinUtils.setPlaceHolder(component, message);
	}

	/**
	 * Builds the layout that contains the various search filters
	 * 
	 * @return the constructed filter layout
	 */
	@Override
	protected HasComponents constructFilterLayout() {
		form = new EnhancedFormLayout();
		form.setWidth(getComponentContext().getMaxSearchFormWidth());

		List<String> columnThresholds = getComponentContext().getSearchColumnThresholds();
		if (columnThresholds == null || columnThresholds.isEmpty()) {
			columnThresholds = SystemPropertyUtils.getDefaultSearchColumnThresholds();
		}

		if (!columnThresholds.isEmpty()) {
			// custom responsive steps
			List<ResponsiveStep> list = new ArrayList<>();
			int i = 0;
			for (String t : columnThresholds) {
				list.add(new ResponsiveStep(t, i + 1));
				i++;
			}
			form.setResponsiveSteps(list);
		}

		// iterate over the searchable attributes and add a field for each
		iterate(getEntityModel().getAttributeModelsSortedForSearch());
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
						ca.setAdditionalFilter(new InPredicate<>(am.getCascadeFilterPath(cascadePath),
								(Collection<S>) event.getValue()));
					} else {
						ca.setAdditionalFilter(
								new EqualsPredicate<>(am.getCascadeFilterPath(cascadePath), event.getValue()));
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
				iterate(nested.getAttributeModelsSortedForSearch());
			}
		}
	}

	/**
	 * Indicates whether a search field for the attribute model must be shown
	 * 
	 * @param attributeModel the attribute model
	 * @return whether the search field for the attribute model must be shown
	 */
	private boolean mustShowSearchField(AttributeModel attributeModel) {
		boolean mustShow;
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
	 * Refreshes any fields that are susceptible to this
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void refresh() {
		for (FilterGroup<?> group : getGroups().values()) {
			if (group.getField() instanceof Refreshable) {
				if (getFieldFilters().containsKey(group.getPropertyId())
						&& group.getField() instanceof CustomEntityField cef) {
					Object oldValue = cef.getValue();
					SerializablePredicate<?> ff = getFieldFilters().get(group.getPropertyId());
					cef.refresh(ff);
					if (oldValue != null) {
						cef.setValue(oldValue);
					}
				} else {
					Object value = ((HasValue) group.getField()).getValue();
					((Refreshable) group.getField()).refresh();
					if (value != null) {
						((HasValue) group.getField()).setValue(value);
					}
				}
			}
		}
	}

	/**
	 * Explicitly enable or disable a search field
	 * 
	 * @param property the property for which to enable or disable the field
	 * @param enabled  the desired enabled setting
	 */
	public void setSearchFieldEnabled(String property, boolean enabled) {
		FilterGroup<T> filterGroup = getGroups().get(property);
		if (filterGroup != null) {
			((HasEnabled) filterGroup.getField()).setEnabled(enabled);
			if (filterGroup.getAuxField() != null) {
				((HasEnabled) filterGroup.getAuxField()).setEnabled(enabled);
			}
		}
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
		if (group != null) {
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
	}

	@SuppressWarnings("unchecked")
	public <R> void setSearchAuxValue(String propertyId, R auxValue) {
		FilterGroup<T> group = groups.get(propertyId);
		if (group != null && group.getAuxField() != null) {
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
		Map<String, Object> oldAuxValues = new HashMap<>();
		storeSearchTerms(oldValues, oldAuxValues);

		clear();
		setAdvancedSearchMode(!isAdvancedSearchMode());

		if (isAdvancedSearchMode()) {
			getToggleAdvancedModeButton().setText(message("ocs.to.simple.search.mode"));
		} else {
			getToggleAdvancedModeButton().setText(message("ocs.to.advanced.search.mode"));
		}

		UIHelper helper = ServiceLocatorFactory.getServiceLocator().getService(UIHelper.class);
		if (helper != null && getFormOptions().isPreserveAdvancedMode()) {
			helper.storeAdvancedMode(isAdvancedSearchMode());
		}

		// empty the search form and rebuild it
		form.removeAll();
		groups.clear();
		iterate(getEntityModel().getAttributeModelsSortedForSearch());

		// restore search values (some fields might not be there anymore)
		for (Entry<String, Object> entry : oldValues.entrySet()) {
			FilterGroup<T> filterGroup = groups.get(entry.getKey());
			if (filterGroup != null) {
				setSearchValue(entry.getKey(), entry.getValue());

				if (oldAuxValues.containsKey(entry.getKey())) {
					setSearchAuxValue(entry.getKey(), oldAuxValues.get(entry.getKey()));
				}
			}
		}
	}

	private void storeSearchTerms(Map<String, Object> oldValues, Map<String, Object> oldAuxValues) {
		for (Entry<String, FilterGroup<T>> filterGroup : groups.entrySet()) {
			HasValue<?, ?> hv = (HasValue<?, ?>) filterGroup.getValue().getField();
			if (hv.getValue() != null) {
				storeSearchTerm(oldValues, filterGroup, hv);
			}
			if (filterGroup.getValue().getAuxField() != null) {
				HasValue<?, ?> hvAux = (HasValue<?, ?>) filterGroup.getValue().getAuxField();
				storeSearchTerm(oldAuxValues, filterGroup, hvAux);
			}
		}
	}

	private void storeSearchTerm(Map<String, Object> values, Entry<String, FilterGroup<T>> filterGroup,
			HasValue<?, ?> hv) {
		boolean emptyCollection = hv.getValue() instanceof Collection && ((Collection<?>) hv.getValue()).isEmpty();
		if (!emptyCollection) {
			values.put(filterGroup.getKey(), hv.getValue());
		}
	}

	/**
	 * Restores a single search value based on a predicate
	 * 
	 * @param predicate the predicate
	 */
	protected void restoreSearchValue(SerializablePredicate<T> predicate) {
		if (predicate instanceof BetweenPredicate bp) {
			AttributeModel am = getEntityModel().getAttributeModel(bp.getProperty());
			Object fromValue = ConvertUtils.convertToPresentationValue(am, bp.getFromValue());
			Object toValue = ConvertUtils.convertToPresentationValue(am, bp.getToValue());
			setSearchValue(bp.getProperty(), fromValue, toValue);
		} else if (predicate instanceof LessOrEqualPredicate loe) {
			AttributeModel am = getEntityModel().getAttributeModel(loe.getProperty());
			Object auxValue = ConvertUtils.convertToPresentationValue(am, loe.getValue());
			setSearchAuxValue(loe.getProperty(), auxValue);
		} else if (predicate instanceof PropertyPredicate pp) {
			AttributeModel am = getEntityModel().getAttributeModel(pp.getProperty());
			Object value = ConvertUtils.convertToPresentationValue(am, pp.getValue());
			setSearchValue(pp.getProperty(), value);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void restoreSearchValues() {
		UIHelper helper = ServiceLocatorFactory.getServiceLocator().getService(UIHelper.class);
		if (helper != null) {
			List<SerializablePredicate<?>> searchTerms = helper.retrieveSearchTerms();
			searchTerms.forEach(s -> {
				SerializablePredicate<T> predicate = (SerializablePredicate<T>) s;
				if (s instanceof CompositePredicate) {
					CompositePredicate<T> cp = (CompositePredicate<T>) s;
					cp.getOperands().forEach(ap -> restoreSearchValue(ap));
				} else {
					restoreSearchValue(predicate);
				}
			});
		}
	}

	@Override
	protected void storeSearchFilters() {
		UIHelper helper = ServiceLocatorFactory.getServiceLocator().getService(UIHelper.class);
		if (helper != null) {
			List<SerializablePredicate<?>> list = new ArrayList<>(getCurrentFilters());
			helper.storeSearchTerms(list);
		}
	}

}
