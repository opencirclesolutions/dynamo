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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.CascadeMode;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.filter.listener.FilterListener;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.Searchable;
import com.ocs.dynamo.ui.component.Cascadable;
import com.ocs.dynamo.ui.component.CustomEntityField;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

/**
 * A search form that is constructed based on the metadata model
 * 
 * @author bas.rutten
 * @param <ID>
 *            The type of the primary key of the entity
 * @param <T>
 *            The type of the entity
 */
public class ModelBasedSearchForm<ID extends Serializable, T extends AbstractEntity<ID>>
		extends AbstractModelBasedSearchForm<ID, T> implements FilterListener, Button.ClickListener {

	// the types of search field
	protected enum FilterType {
		BETWEEN, BOOLEAN, ENTITY, ENUM, EQUAL, LIKE
	}

	private static final long serialVersionUID = -7226808613882934559L;

	/**
	 * The number of search fields that was added so far
	 */
	private int fieldsAdded = 0;

	/**
	 * The main form layout
	 */
	private Layout form;

	/**
	 * The various filter groups
	 */
	private Map<String, FilterGroup> groups = new HashMap<>();

	/**
	 * The number of search columns
	 */
	private int nrOfColumns = 1;

	/**
	 * Sub form layouts that are used in case of multiple column layout
	 */
	private List<FormLayout> subForms = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param searchable
	 *            the component on which to carry out the search
	 * @param entityModel
	 *            the entity model
	 * @param formOptions
	 *            the form options
	 */
	public ModelBasedSearchForm(Searchable searchable, EntityModel<T> entityModel, FormOptions formOptions) {
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
	public ModelBasedSearchForm(Searchable searchable, EntityModel<T> entityModel, FormOptions formOptions,
			List<Filter> defaultFilters, Map<String, Filter> fieldFilters) {
		super(searchable, entityModel, formOptions, defaultFilters, fieldFilters);
	}

	/**
	 * Clears all filters then performs a fresh search
	 */
	@Override
	public void clear() {
		// Clear all filter groups
		for (FilterGroup group : groups.values()) {
			group.reset();
		}
		super.clear();
	}

	@Override
	protected void constructButtonBar(Layout buttonBar) {
		buttonBar.addComponent(constructSearchButton());
		buttonBar.addComponent(constructSearchAnyButton());
		buttonBar.addComponent(constructClearButton());
		buttonBar.addComponent(constructToggleButton());
	}

	/**
	 * Adds any value change listeners for taking care of cascading search
	 */
	protected void constructCascadeListeners() {
		for (final AttributeModel am : getEntityModel().getCascadeAttributeModels()) {
			if (am.isSearchable()) {
				Field<?> field = groups.get(am.getPath()).getField();
				field.addValueChangeListener(event -> {
					for (String cascadePath : am.getCascadeAttributes()) {
						handleCascade(event, am, cascadePath);
					}
				});
			}
		}
	}

	/**
	 * Callback method that can be used to create any additional search fields (that
	 * are not governed by the entity model)
	 * 
	 * @return
	 */
	protected List<Component> constructExtraSearchFields() {
		return new ArrayList<>();
	}

	/**
	 * Creates a search field based on an attribute model
	 * 
	 * @param entityModel
	 *            the entity model of the entity to search for
	 * @param attributeModel
	 *            the attribute model the attribute model of the property that is
	 *            bound to the field
	 * @return
	 */
	protected Field<?> constructField(EntityModel<T> entityModel, AttributeModel attributeModel) {
		Field<?> field = constructCustomField(entityModel, attributeModel);
		if (field == null) {
			EntityModel<?> em = getFieldEntityModel(attributeModel);
			field = getFieldFactory().constructField(attributeModel, getFieldFilters(), em);
		}

		if (field != null) {
			field.setSizeFull();
		}
		return field;
	}

	/**
	 * Constructs a filter group for searching on a single attribute
	 * 
	 * @param entityModel
	 *            the entity model
	 * @param attributeModel
	 *            the attribute model
	 * @return
	 */
	protected FilterGroup constructFilterGroup(EntityModel<T> entityModel, AttributeModel attributeModel) {
		Field<?> field = this.constructField(entityModel, attributeModel);
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
			Field<?> auxField = null;
			if (FilterType.BETWEEN.equals(filterType)) {
				// in case of a between value, construct two fields for the
				// lower
				// and upper bounds
				String from = message("ocs.from");
				field.setCaption(attributeModel.getDisplayName() + " " + from);
				auxField = constructField(entityModel, attributeModel);
				String to = message("ocs.to");
				auxField.setCaption(attributeModel.getDisplayName() + " " + to);
				auxField.setVisible(true);
				HorizontalLayout layout = new DefaultHorizontalLayout();
				layout.setSizeFull();
				layout.addComponent(field);
				layout.addComponent(auxField);
				comp = layout;
			}

			return new FilterGroup(attributeModel, filterType, comp, field, auxField);
		}
		return null;
	}

	/**
	 * Builds the layout that contains the various search filters
	 * 
	 * @param entityModel
	 *            the entity model
	 * @return
	 */
	@Override
	protected Layout constructFilterLayout() {
		if (nrOfColumns == 1) {
			form = new FormLayout();
			// don't use all the space unless it's a popup window
			if (!getFormOptions().isPopup()) {
				form.setStyleName(DynamoConstants.CSS_CLASS_HALFSCREEN);
			}
		} else {
			// create a number of form layouts next to each others
			form = new GridLayout(nrOfColumns, 1);
			form.setSizeFull();

			for (int i = 0; i < nrOfColumns; i++) {
				FormLayout column = new FormLayout();
				column.setMargin(true);
				subForms.add(column);
				form.addComponent(column);
			}
		}

		// add any extra fields
		List<Component> extra = constructExtraSearchFields();
		for (Component c : extra) {
			if (nrOfColumns == 1) {
				form.addComponent(c);
			} else {
				int index = fieldsAdded % nrOfColumns;
				subForms.get(index).addComponent(c);
			}
			fieldsAdded++;
		}

		// iterate over the searchable attributes and add a field for each
		iterate(getEntityModel().getAttributeModels());
		constructCascadeListeners();

		DefaultVerticalLayout margin = new DefaultVerticalLayout(true, false);
		margin.addComponent(form);

		// hide the search form if there are no search criteria (and no extra search
		// fields)
		if (groups.isEmpty() && extra.isEmpty()) {
			margin.setVisible(false);
		}
		return margin;
	}

	/**
	 * Programmatically force a search
	 * 
	 * @param propertyId
	 *            the property ID to search on
	 * @param value
	 *            the value of the property
	 */
	public void forceSearch(String propertyId, Object value) {
		setSearchValue(propertyId, value);
		search();
	}

	/**
	 * Sets a search filter then forces a search
	 * 
	 * @param propertyId
	 *            the property to search on
	 * @param lower
	 *            the lower bound
	 * @param upper
	 *            the upper bound
	 */
	public void forceSearch(String propertyId, Object lower, Object upper) {
		setSearchValue(propertyId, lower, upper);
		search();
	}

	/**
	 * Returns all filter groups
	 * 
	 * @return
	 */
	public Map<String, FilterGroup> getGroups() {
		return groups;
	}

	public int getNrOfColumns() {
		return nrOfColumns;
	}

	/**
	 * Handles a cascade event
	 * 
	 * @param event
	 *            the event that triggered the cascade
	 * @param am
	 *            the attribute model of the property that triggered the cascade
	 * @param cascadePath
	 *            the path to the property that is the target of the cascade
	 */
	private void handleCascade(ValueChangeEvent event, AttributeModel am, String cascadePath) {
		CascadeMode cm = am.getCascadeMode(cascadePath);
		if (CascadeMode.BOTH.equals(cm) || CascadeMode.SEARCH.equals(cm)) {
			Field<?> cascadeField = groups.get(cascadePath).getField();
			if (cascadeField instanceof Cascadable) {
				Cascadable ca = (Cascadable) cascadeField;
				if (event.getProperty().getValue() == null) {
					ca.clearAdditionalFilter();
				} else {
					ca.setAdditionalFilter(
							new Compare.Equal(am.getCascadeFilterPath(cascadePath), event.getProperty().getValue()));
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
	 * @param form
	 *            the form to add the search fields to
	 * @param attributeModels
	 *            the attribute models to iterate over
	 */
	private void iterate(List<AttributeModel> attributeModels) {
		for (AttributeModel attributeModel : attributeModels) {
			if (attributeModel.isSearchable()) {

				FilterGroup group = constructFilterGroup(getEntityModel(), attributeModel);
				// set field with
				Integer fieldWidth = SystemPropertyUtils.getDefaultFieldWidth();
				if (fieldWidth != null) {
					group.getFilterComponent().setWidth(fieldWidth + "px");
				} else {
					group.getFilterComponent().setSizeFull();
				}

				if (nrOfColumns == 1) {
					form.addComponent(group.getFilterComponent());
				} else {
					int index = fieldsAdded % nrOfColumns;
					subForms.get(index).addComponent(group.getFilterComponent());
				}

				// register with the form and set the listener
				group.addListener(this);
				groups.put(group.getPropertyId(), group);
				fieldsAdded++;
			}

			// also support search on nested attributes
			if (attributeModel.getNestedEntityModel() != null) {
				EntityModel<?> nested = attributeModel.getNestedEntityModel();
				iterate(nested.getAttributeModels());
			}
		}
	}

	/**
	 * Callback method that allows the you to modify the various filter groups
	 * 
	 * @param groups
	 *            the filter groups
	 */
	protected void postProcessFilterGroups(Map<String, FilterGroup> groups) {
		// overwrite in subclasses
	}

	/**
	 * Callback method that is called once the processing of the layout is complete.
	 * Allows you to modify the layout or add extra components at the end
	 * 
	 * @param layout
	 *            the main layout
	 */
	@Override
	protected void postProcessLayout(VerticalLayout layout) {
		postProcessFilterGroups(groups);
	}

	/**
	 * Refreshes any fields that are susceptible to this
	 */
	@Override
	public void refresh() {
		for (FilterGroup group : getGroups().values()) {
			if (group.getField() instanceof Refreshable) {
				if (getFieldFilters().containsKey(group.getPropertyId())
						&& group.getField() instanceof CustomEntityField) {
					Filter ff = getFieldFilters().get(group.getPropertyId());
					((CustomEntityField) group.getField()).refresh(ff);
				} else {
					((Refreshable) group.getField()).refresh();
				}
			}
		}
	}

	/**
	 * Sets the desired number of columns
	 * 
	 * @param nrOfColumns
	 *            the number of columns
	 */
	public void setNrOfColumns(int nrOfColumns) {
		this.nrOfColumns = nrOfColumns;
	}

	/**
	 * Manually set the value for a certain search field
	 * 
	 * @param propertyId
	 *            the ID of the property
	 * @param value
	 *            the desired value
	 */
	public void setSearchValue(String propertyId, Object value) {
		setSearchValue(propertyId, value, null);
	}

	/**
	 * Manually set the value for a certain search field
	 * 
	 * @param propertyId
	 *            the ID of the property
	 * @param value
	 *            the desired value for the main field
	 * @param auxValue
	 *            the desired value for the auxiliary field
	 */
	public void setSearchValue(String propertyId, Object value, Object auxValue) {
		FilterGroup group = groups.get(propertyId);
		group.getField().setValue(value);
		if (group.getAuxField() != null) {
			group.getAuxField().setValue(auxValue);
		}
	}

}
