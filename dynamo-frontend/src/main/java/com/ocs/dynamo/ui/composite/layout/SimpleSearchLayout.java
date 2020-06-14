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
import java.util.ArrayList;
import java.util.List;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.form.ModelBasedSearchForm;
import com.ocs.dynamo.ui.provider.QueryType;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.data.provider.SortOrder;

/**
 * A composite component that contains a search form and a results grid, along
 * with the option to navigate to a detail screen for adding/editing
 * 
 * @author bas.rutten
 * @param <ID> type of the primary key of the entity to search for
 * @param <T>  type of the entity to search for
 */
public class SimpleSearchLayout<ID extends Serializable, T extends AbstractEntity<ID>>
		extends AbstractModelSearchLayout<ID, T> {

	private static final long serialVersionUID = 4606800218149558500L;

	/**
	 * Column width thresholds
	 */
	private List<String> columnThresholds = new ArrayList<>();

	/**
	 * Constructor - only the most important attributes
	 * 
	 * @param service     the service that is used to query the database
	 * @param entityModel the entity model of the entities to search for
	 * @param queryType   the type of the query
	 * @param formOptions form options that governs which buttons and options to
	 *                    show
	 * @param sortOrder   the default sort order
	 * @param joins       the joins to include in the query
	 */
	public SimpleSearchLayout(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
			FormOptions formOptions, SortOrder<?> sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, queryType, formOptions, sortOrder, joins);
	}

	/**
	 * Method that is used to construct any extra search fields. These will be added
	 * at the front of the search form
	 */
	protected List<Component> constructExtraSearchFields() {
		// overwrite in subclasses
		return new ArrayList<>();
	}

	/**
	 * Constructs the search form
	 * 
	 * @return the search form
	 */
	@Override
	protected ModelBasedSearchForm<ID, T> constructSearchForm() {
		// by default, do not pass a searchable object, in order to prevent an
		// unnecessary and
		// potentially unfiltered search
		ModelBasedSearchForm<ID, T> result = new ModelBasedSearchForm<ID, T>(null, getEntityModel(), getFormOptions(),
				this.getDefaultFilters(), this.getFieldFilters()) {

			private static final long serialVersionUID = 8929442625027442714L;

			@Override
			protected void afterSearchFieldToggle(boolean visible) {
				SimpleSearchLayout.this.afterSearchFieldToggle(visible);
			}

			@Override
			protected void afterSearchPerformed() {
				SimpleSearchLayout.this.afterSearchPerformed();
			}

			@Override
			protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel) {
				return SimpleSearchLayout.this.constructCustomField(entityModel, attributeModel, false, true);
			}

			@Override
			protected void postProcessButtonBar(FlexLayout buttonBar) {
				SimpleSearchLayout.this.postProcessSearchButtonBar(buttonBar);
			}

			@Override
			protected void validateBeforeSearch() {
				SimpleSearchLayout.this.validateBeforeSearch();
			}
		};
		result.setFieldEntityModels(getFieldEntityModels());
		result.setColumnThresholds(getColumnThresholds());
		result.build();
		return result;
	}

	public List<String> getColumnThresholds() {
		return this.columnThresholds;
	}

	@Override
	public ModelBasedSearchForm<ID, T> getSearchForm() {
		return (ModelBasedSearchForm<ID, T>) super.getSearchForm();
	}

	public void setColumnThresholds(List<String> columnThresholds) {
		this.columnThresholds = columnThresholds;
	}

	/**
	 * Sets a certain search value (for a property with a single value to search on)
	 * 
	 * @param propertyId the property to search for
	 * @param value      the desired value
	 */
	@Override
	public void setSearchValue(String propertyId, Object value) {
		getSearchForm().setSearchValue(propertyId, value);
	}

	/**
	 * Sets a certain search value (for a property with an upper and a lower bound)
	 * 
	 * @param propertyId the property to search for
	 * @param value      the value of the lower bound
	 * @param auxValue   the value of the upper bound
	 */
	@Override
	public void setSearchValue(String propertyId, Object value, Object auxValue) {
		getSearchForm().setSearchValue(propertyId, value, auxValue);
	}

}
