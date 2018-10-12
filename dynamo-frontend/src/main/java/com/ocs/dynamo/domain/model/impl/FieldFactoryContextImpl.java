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

import org.apache.poi.ss.formula.functions.T;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldFactory.Context;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;

/**
 * Default implementation for the field construction context.
 * 
 * @author patrickdeenen
 *
 */
public class FieldFactoryContextImpl implements Context {

	private Container container;

	private AttributeModel attributeModel;

	private Map<String, Filter> fieldFilters;

	private EntityModel<?> fieldEntityModel;

	private Object parentEntity;

	private Boolean viewMode;

	private Boolean search;

	public FieldFactoryContextImpl() {
	}

	@Override
	public Container getContainer() {
		return container;
	}

	@Override
	public AttributeModel getAttributeModel() {
		return attributeModel;
	}

	@Override
	public Map<String, Filter> getFieldFilters() {
		return fieldFilters;
	}

	@SuppressWarnings({ "unchecked", "hiding" })
	@Override
	public <T> EntityModel<T> getFieldEntityModel() {
		return (EntityModel<T>) fieldEntityModel;
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
	public FieldFactoryContextImpl setContainer(Container container) {
		this.container = container;
		return this;
	}

	/**
	 * @param attributeModel
	 *            the attributeModel to set
	 */
	public FieldFactoryContextImpl setAttributeModel(AttributeModel attributeModel) {
		this.attributeModel = attributeModel;
		return this;
	}

	/**
	 * @param fieldFilters
	 *            the fieldFilters to set
	 */
	public FieldFactoryContextImpl setFieldFilters(Map<String, Filter> fieldFilters) {
		this.fieldFilters = fieldFilters;
		return this;
	}

	/**
	 * @param fieldEntityModel
	 *            the fieldEntityModel to set
	 */
	public FieldFactoryContextImpl setFieldEntityModel(EntityModel<T> fieldEntityModel) {
		this.fieldEntityModel = fieldEntityModel;
		return this;
	}

	/**
	 *
	 * @param parentEntity
	 * @return the parent entity to set
	 */
	public <P> FieldFactoryContextImpl setParentEntity(P parentEntity) {
		this.parentEntity = parentEntity;
		return this;
	}

	/**
	 *
	 * @param viewMode
	 * @return the view mode to set
	 */
	public FieldFactoryContextImpl setViewMode(boolean viewMode) {
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
	public FieldFactoryContextImpl setSearch(boolean search) {
		this.search = search;
		return this;
	}

}
