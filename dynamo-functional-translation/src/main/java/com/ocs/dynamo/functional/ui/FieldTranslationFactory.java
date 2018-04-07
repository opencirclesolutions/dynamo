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
package com.ocs.dynamo.functional.ui;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.functional.domain.AbstractEntityTranslated;
import com.ocs.dynamo.functional.domain.Translation;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.ui.Field;

/**
 * This factory can be used to generate TranslationTable objects for attributes in fields which have to be dynamically
 * localized (translated). It expects a generic database table based on entity Translation and attributes to be
 * translated mapped to the translation collection in this entity.
 * 
 * @author patrick.deenen@opencircle.solutions
 *
 * @param <ID>
 *            The type for the id of the entity which has translated attributes
 * @param <T>
 *            The type which implements the translation for the entity
 */
// FIXME This factory should be improved to implement an interface that can register this interface to a field
// factory repository which can be used by a generic ModelFieldFactory.
public class FieldTranslationFactory<ID extends Serializable, T extends AbstractEntityTranslated<ID, Translation<T>>> {
	
	private EntityModel<T> em;
	
	private T entity;
	
	private HashMap<String, Field<?>> fields = new HashMap<>();
	
	private boolean viewMode;

	/**
	 * Create the factory using the entity model of the parent form
	 * 
	 * @param em
	 *            The entity model of the parent form
	 */
	public FieldTranslationFactory(EntityModel<T> em) {
		super();
		this.em = em;
	}

	public T getEntity() {
		return entity;
	}

	public void setEntity(T entity) {
		if (this.entity == null || !this.entity.equals(entity)) {
			clearFields();
			this.entity = entity;
		}
	}

	public boolean isViewMode() {
		return viewMode;
	}

	public void setViewMode(boolean viewMode) {
		if (this.viewMode != viewMode) {
			clearFields();
			this.viewMode = viewMode;
		}
	}

	public void clearFields() {
		fields.clear();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void createFields() {
		if (em != null && entity != null) {
			for (AttributeModel am : em.getAttributeModels()) {
				if (am.isVisible() && am.getNestedEntityModel() != null
						&& Translation.class.isAssignableFrom(am.getNestedEntityModel().getEntityClass())) {
					Collection<Translation<T>> items = (Collection<Translation<T>>) ClassUtils.getFieldValue(entity,
							am.getName());
					final EntityModel<Translation<T>> nem = (EntityModel<Translation<T>>) ServiceLocatorFactory
							.getServiceLocator().getEntityModelFactory()
							.getModel(am.getNestedEntityModel().getEntityClass());
					TranslationTable<ID, T> tt = new TranslationTable(entity, am.getName(), items, nem, viewMode);
					tt.setRequired(am.isRequired());
					fields.put(am.getName(), tt);
				}
			}
		}
	}

	public Field<?> getField(String fieldName) {
		if (fields.isEmpty()) {
			createFields();
		}
		return fields.get(fieldName);
	}

	public void setFieldCaptions(ModelBasedEditForm<ID, T> form) {
		if (!fields.isEmpty()) {
			for (AttributeModel am : em.getAttributeModels()) {
				if (fields.containsKey(am.getName())) {
					form.getField(am.getName()).setCaption(am.getDisplayName());
				}
			}
		}
	}
}
