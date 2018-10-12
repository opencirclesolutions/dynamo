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

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;

import java.util.List;
import java.util.Map;

/**
 * A wrapper that adds lazy loading to an entity model definition
 * 
 * @author bas.rutten
 *
 * @param <T>
 */
public class LazyEntityModelWrapper<T> implements EntityModel<T> {

	private EntityModelFactory factory;

	/**
	 * The entity model to which to delegate the actual functionality
	 */
	private EntityModel<T> delegate;

	/**
	 * The unique reference for this entity model
	 */
	private String reference;

	/**
	 * The entity class
	 */
	private Class<T> entityClass;

	/**
	 * Constructor
	 *
	 * @param factory
	 *            the entity model factory
	 * @param reference
	 *            the reference (unique name of the model)
	 * @param entityClass
	 *            the entity class
	 */
	public LazyEntityModelWrapper(EntityModelFactory factory, String reference, Class<T> entityClass) {
		this.reference = reference;
		this.factory = factory;
		this.entityClass = entityClass;
	}

	@Override
	public void addAttributeModel(String attributeGroup, AttributeModel model, AttributeModel existingModel) {
		getDelegate().addAttributeModel(attributeGroup, model, existingModel);
	}

	@Override
	public List<String> getAttributeGroups() {
		return getDelegate().getAttributeGroups();
	}

	@Override
	public AttributeModel getAttributeModel(String attributeName) {
		return getDelegate().getAttributeModel(attributeName);
	}

	@Override
	public List<AttributeModel> getAttributeModels() {
		return getDelegate().getAttributeModels();
	}

	@Override
	public List<AttributeModel> getAttributeModelsForGroup(String group) {
		return getDelegate().getAttributeModelsForGroup(group);
	}

	@Override
	public List<AttributeModel> getAttributeModelsForType(AttributeType attributeType, Class<?> type) {
		return getDelegate().getAttributeModelsForType(attributeType, type);
	}

	@Override
	public List<AttributeModel> getCascadeAttributeModels() {
		return getDelegate().getCascadeAttributeModels();
	}

	protected EntityModel<T> getDelegate() {
		if (delegate == null) {
			init();
		}
		return delegate;
	}

	@Override
	public String getDescription() {
		return getDelegate().getDescription();
	}

	@Override
	public String getDisplayName() {
		return getDelegate().getDisplayName();
	}

	@Override
	public String getDisplayNamePlural() {
		return getDelegate().getDisplayNamePlural();
	}

	@Override
	public String getDisplayProperty() {
		return getDelegate().getDisplayProperty();
	}

	@Override
	public Class<T> getEntityClass() {
		return getDelegate().getEntityClass();
	}

	@Override
	public AttributeModel getIdAttributeModel() {
		return getDelegate().getIdAttributeModel();
	}

	@Override
	public AttributeModel getMainAttributeModel() {
		return getDelegate().getMainAttributeModel();
	}

	@Override
	public String getReference() {
		return getDelegate().getReference();
	}

	@Override
	public List<AttributeModel> getRequiredForSearchingAttributeModels() {
		return getDelegate().getRequiredForSearchingAttributeModels();
	}

	@Override
	public Map<AttributeModel, Boolean> getSortOrder() {
		return getDelegate().getSortOrder();
	}

	private synchronized void init() {
		if (delegate == null) {
			delegate = factory.getModel(reference, entityClass);
		}
	}

	@Override
	public boolean isAttributeGroupVisible(String group, boolean readOnly) {
		return getDelegate().isAttributeGroupVisible(group, readOnly);
	}

	@Override
	public boolean usesDefaultGroupOnly() {
		return getDelegate().usesDefaultGroupOnly();
	}

	@Override
	public void addAttributeGroup(String attributeGroup) {
		getDelegate().addAttributeGroup(attributeGroup);
	}

}
