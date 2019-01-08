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
package com.ocs.dynamo.domain.model;

import java.util.Map;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.SerializablePredicate;

/**
 * Default implementation for the field construction context.
 * 
 * @author patrickdeenen
 *
 */
public class FieldFactoryContext {

	/**
	 * Creates a blank context
	 * 
	 * @return
	 */
	public static FieldFactoryContext create() {
		return new FieldFactoryContext();
	}

	/**
	 * Creates a default context based on only the attribute model
	 * 
	 * @param am the attribute model
	 * @return
	 */
	public static FieldFactoryContext createDefault(AttributeModel am) {
		return new FieldFactoryContext().setAttributeModel(am);
	}

	private AttributeModel attributeModel;

	private Map<String, SerializablePredicate<?>> fieldFilters;

	private EntityModel<?> fieldEntityModel;

	private boolean viewMode;

	private boolean search;

	private Map<String, ListDataProvider<?>> sharedProviders;

	private FieldFactoryContext() {
		// hidden
	}

	public AttributeModel getAttributeModel() {
		return attributeModel;
	}

	public EntityModel<?> getFieldEntityModel() {
		return fieldEntityModel;
	}

	public Map<String, SerializablePredicate<?>> getFieldFilters() {
		return fieldFilters;
	}

	public boolean getViewMode() {
		return viewMode;
	}

	/**
	 * @return the search
	 */
	public boolean isSearch() {
		return search;
	}

	/**
	 * @param attributeModel the attributeModel to set
	 */
	public FieldFactoryContext setAttributeModel(AttributeModel attributeModel) {
		this.attributeModel = attributeModel;
		return this;
	}

	/**
	 * @param fieldEntityModel the fieldEntityModel to set
	 */
	public FieldFactoryContext setFieldEntityModel(EntityModel<?> fieldEntityModel) {
		this.fieldEntityModel = fieldEntityModel;
		return this;
	}

	/**
	 * @param fieldFilters the fieldFilters to set
	 */
	public FieldFactoryContext setFieldFilters(Map<String, SerializablePredicate<?>> fieldFilters) {
		this.fieldFilters = fieldFilters;
		return this;
	}

	/**
	 * @param search the search to set
	 */
	public FieldFactoryContext setSearch(boolean search) {
		this.search = search;
		return this;
	}

	/**
	 *
	 * @param viewMode
	 * @return the view mode to set
	 */
	public FieldFactoryContext setViewMode(boolean viewMode) {
		this.viewMode = viewMode;
		return this;
	}

	public Map<String, ListDataProvider<?>> getSharedProviders() {
		return sharedProviders;
	}

	public FieldFactoryContext setSharedProviders(Map<String, ListDataProvider<?>> sharedProviders) {
		this.sharedProviders = sharedProviders;
		return this;
	}

	public void addSharedProvider(String attribute, ListDataProvider<?> sharedProvider) {
		sharedProviders.put(attribute, sharedProvider);
	}

	public ListDataProvider<?> getSharedProvider(String attribute) {
		return sharedProviders == null ? null : sharedProviders.get(attribute);
	}

}
