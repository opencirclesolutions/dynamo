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
import com.ocs.dynamo.ui.composite.form.AbstractModelBasedSearchForm;
import com.ocs.dynamo.ui.composite.form.FlexibleFilterType;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.form.ModelBasedFlexibleSearchForm;
import com.ocs.dynamo.ui.container.QueryType;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.ui.Field;

/**
 * A search layout that allows a user to click together the search criteria
 * 
 * @author bas.rutten
 *
 * @param <ID>
 *            type of the primary key of the entities to search for
 * @param <T>
 *            the entity to search for
 */
public class FlexibleSearchLayout<ID extends Serializable, T extends AbstractEntity<ID>> extends
        AbstractSearchLayout<ID, T> {

	private static final long serialVersionUID = -6179979286298244161L;

	/**
	 * Constructor
	 * 
	 * @param service
	 *            the service used to retrieve the entities
	 * @param entityModel
	 *            the entity model
	 * @param queryType
	 *            the query type
	 * @param formOptions
	 *            the form options
	 * @param fieldFilters
	 *            field filters for filtering lookup components
	 * @param defaultFilters
	 *            additional/default filters
	 * @param sortOrder
	 *            sort order
	 * @param joins
	 *            relations to fetch
	 */
	public FlexibleSearchLayout(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
	        FormOptions formOptions, Map<String, Filter> fieldFilters, List<Filter> defaultFilters,
	        SortOrder sortOrder, FetchJoinInformation[] joins) {
		super(service, entityModel, queryType, formOptions, fieldFilters, defaultFilters, sortOrder, joins);
	}

	/**
	 * Constructor (basic parameters only)
	 * 
	 * @param service
	 *            the service used to retrieve the entities
	 * @param entityModel
	 *            the entity model
	 * @param queryType
	 *            the query type
	 * @param formOptions
	 *            the form options
	 * @param sortOrder
	 *            sort order
	 * @param joins
	 *            relations to fetch
	 */
	public FlexibleSearchLayout(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
	        FormOptions formOptions, SortOrder sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, queryType, formOptions, sortOrder, joins);
	}

	@Override
	protected AbstractModelBasedSearchForm<ID, T> constructSearchForm() {
		ModelBasedFlexibleSearchForm<ID, T> result = new ModelBasedFlexibleSearchForm<ID, T>(null, getEntityModel(),
		        getFormOptions(), this.getDefaultFilters(), this.getFieldFilters()) {

			private static final long serialVersionUID = 8929442625027442714L;

			@Override
			protected void afterSearchFieldToggle(boolean visible) {
				FlexibleSearchLayout.this.afterSearchFieldToggle(visible);
			}

			@Override
			protected Field<?> constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel) {
				return FlexibleSearchLayout.this.constructCustomField(entityModel, attributeModel, false, true);
			}

		};
		result.setFieldEntityModels(getFieldEntityModels());
		result.build();

		for (AttributeModel am : getEntityModel().getRequiredForSearchingAttributeModels()) {
			result.addFilter(am, result.getDefaultFilterType(am), null, null);
		}

		return result;
	}

	@Override
	public ModelBasedFlexibleSearchForm<ID, T> getSearchForm() {
		return (ModelBasedFlexibleSearchForm<ID, T>) super.getSearchForm();
	}

	/**
	 * Sets a predefined search value
	 * 
	 * @param propertyId
	 *            the property to search for
	 * @param value
	 *            the desired value
	 */
	@Override
	public void setSearchValue(String propertyId, Object value) {
		getSearchForm().addFilter(getEntityModel().getAttributeModel(propertyId), FlexibleFilterType.EQUALS, value,
		        null);
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
		getSearchForm().addFilter(getEntityModel().getAttributeModel(propertyId), FlexibleFilterType.BETWEEN, value,
		        auxValue);
	}
}
