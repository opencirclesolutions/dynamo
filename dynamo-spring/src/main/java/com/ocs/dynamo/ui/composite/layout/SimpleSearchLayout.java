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
package com.ocs.dynamo.ui.composite.layout;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.form.ModelBasedSearchForm;
import com.ocs.dynamo.ui.container.QueryType;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;

/**
 * A composite component that contains a search form and a results table, along with the option to
 * navigate to a detail screen for adding/editing
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key of the entity to search for
 * @param <T>
 *            type of the entity to search for
 */
public class SimpleSearchLayout<ID extends Serializable, T extends AbstractEntity<ID>> extends
        AbstractSearchLayout<ID, T> {

	private static final long serialVersionUID = 4606800218149558500L;

	/**
	 * The number of columns in the search form
	 */
	private int nrOfColumns = 1;

	/**
	 * Constructor - all fields
	 * 
	 * @param service
	 *            the service that is used to query the database
	 * @param entityModel
	 *            the entity model of the entities to search for
	 * @param queryType
	 *            the type of the query
	 * @param formOptions
	 *            form options that governs which buttons and options to show
	 * @param fieldFilters
	 *            filters that are applied to individual search fields
	 * @param defaultFilters
	 *            search filters that are added to every query
	 * @param sortOrder
	 *            the default sort order
	 * @param joins
	 *            the joins to include in the query
	 */
	public SimpleSearchLayout(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
	        FormOptions formOptions, Map<String, Filter> fieldFilters, List<Filter> defaultFilters,
	        SortOrder sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, queryType, formOptions, fieldFilters, defaultFilters, sortOrder, joins);
	}

	/**
	 * Constructor - only the most important attributes
	 * 
	 * @param service
	 *            the service that is used to query the database
	 * @param entityModel
	 *            the entity model of the entities to search for
	 * @param queryType
	 *            the type of the query
	 * @param formOptions
	 *            form options that governs which buttons and options to show
	 * @param sortOrder
	 *            the default sort order
	 * @param joins
	 *            the joins to include in the query
	 */
	public SimpleSearchLayout(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
	        FormOptions formOptions, SortOrder sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, queryType, formOptions, sortOrder, joins);
	}

	/**
	 * Constructs the search form
	 * 
	 * @return the search form
	 */
	@Override
	protected ModelBasedSearchForm<ID, T> constructSearchForm() {
		// by default, do not pass a searchable object, in order to prevent an unnecessary and
		// potentially unfiltered search
		ModelBasedSearchForm<ID, T> result = new ModelBasedSearchForm<ID, T>(null, getEntityModel(), getFormOptions(),
		        this.getDefaultFilters(), this.getFieldFilters()) {

			private static final long serialVersionUID = 8929442625027442714L;

			@Override
			protected void afterSearchFieldToggle(boolean visible) {
				SimpleSearchLayout.this.afterSearchFieldToggle(visible);
			}

			@Override
			protected Field<?> constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel) {
				return SimpleSearchLayout.this.constructCustomField(entityModel, attributeModel, false, true);
			}

			@Override
			protected List<Component> constructExtraSearchFields() {
				return SimpleSearchLayout.this.constructExtraSearchFields();
			}
		};
		result.setNrOfColumns(getNrOfColumns());
		result.setFieldEntityModels(getFieldEntityModels());
		result.build();
		return result;
	}

	public int getNrOfColumns() {
		return nrOfColumns;
	}

	@Override
	public ModelBasedSearchForm<ID, T> getSearchForm() {
		return (ModelBasedSearchForm<ID, T>) super.getSearchForm();
	}

	/**
	 * Sets the desired number of columns in the search form
	 * 
	 * @param nrOfColumns
	 */
	public void setNrOfColumns(int nrOfColumns) {
		this.nrOfColumns = nrOfColumns;
	}

	/**
	 * Sets a certain search value
	 * 
	 * @param propertyId
	 *            the property to search for
	 * @param value
	 *            the desired value
	 */
	@Override
	public void setSearchValue(String propertyId, Object value) {
		getSearchForm().setSearchValue(propertyId, value);
	}

	/**
	 * Sets a certain search value (for a property with an upper and a lower bound)
	 * 
	 * @param propertyId
	 *            the property to search for
	 * @param value
	 *            the value of the lower bound
	 * @param auxValue
	 *            the value of the upper bound
	 */
	@Override
	public void setSearchValue(String propertyId, Object value, Object auxValue) {
		getSearchForm().setSearchValue(propertyId, value, auxValue);
	}
}
