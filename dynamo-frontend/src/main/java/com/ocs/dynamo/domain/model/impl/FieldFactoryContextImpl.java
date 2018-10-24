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
package com.ocs.dynamo.domain.model.impl;

import java.util.Map;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldFactory.Context;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.server.SerializablePredicate;

/**
 * Default implementation for the field construction context.
 * 
 * @author patrickdeenen
 *
 */
public class FieldFactoryContextImpl<T> implements Context<T> {

	private DataProvider<T, SerializablePredicate<T>> dataProvider;

	private AttributeModel attributeModel;

	private Map<String, SerializablePredicate<T>> fieldFilters;

	private EntityModel<?> fieldEntityModel;

	private Object parentEntity;

	private Boolean viewMode;

	private Boolean search;

	public FieldFactoryContextImpl() {
	}

	public DataProvider<T, SerializablePredicate<T>> getDataProvider() {
		return dataProvider;
	}

	@Override
	public AttributeModel getAttributeModel() {
		return attributeModel;
	}

	@Override
	public Map<String, SerializablePredicate<T>> getFieldFilters() {
		return fieldFilters;
	}

	@SuppressWarnings({ "unchecked", "hiding" })
	@Override
	public <U> EntityModel<U> getFieldEntityModel() {
		return (EntityModel<U>) fieldEntityModel;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P> P getParentEntity() {
		return (P) parentEntity;
	}

	@Override
	public Boolean getViewMode() {
		return viewMode;
	}

	/**
	 * @param container
	 *            the container to set
	 */
	public FieldFactoryContextImpl<T> setContainer(DataProvider<T, SerializablePredicate<T>> dataProvider) {
		this.dataProvider = dataProvider;
		return this;
	}

	/**
	 * @param attributeModel
	 *            the attributeModel to set
	 */
	public FieldFactoryContextImpl<T> setAttributeModel(AttributeModel attributeModel) {
		this.attributeModel = attributeModel;
		return this;
	}

	/**
	 * @param fieldFilters
	 *            the fieldFilters to set
	 */
	public FieldFactoryContextImpl<T> setFieldFilters(Map<String, SerializablePredicate<T>> fieldFilters) {
		this.fieldFilters = fieldFilters;
		return this;
	}

	/**
	 * @param fieldEntityModel
	 *            the fieldEntityModel to set
	 */
	public FieldFactoryContextImpl<T> setFieldEntityModel(EntityModel<T> fieldEntityModel) {
		this.fieldEntityModel = fieldEntityModel;
		return this;
	}

	/**
	 *
	 * @param parentEntity
	 * @return the parent entity to set
	 */
	public <P> FieldFactoryContextImpl<T> setParentEntity(P parentEntity) {
		this.parentEntity = parentEntity;
		return this;
	}

	/**
	 *
	 * @param viewMode
	 * @return the view mode to set
	 */
	public FieldFactoryContextImpl<T> setViewMode(boolean viewMode) {
		this.viewMode = viewMode;
		return this;
	}

	/**
	 * @return the search
	 */
	@Override
	public Boolean isSearch() {
		return search;
	}

	/**
	 * @param search
	 *            the search to set
	 */
	public FieldFactoryContextImpl<T> setSearch(boolean search) {
		this.search = search;
		return this;
	}

}
