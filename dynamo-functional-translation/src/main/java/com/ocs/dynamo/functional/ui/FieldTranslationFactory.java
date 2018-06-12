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
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.domain.model.impl.FieldFactoryContextImpl;
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
 * This class can be used in 2 ways: [1] by hand [2] as a factory delegate as part of the editform.
 *
 * @author patrick.deenen@opencircle.solutions
 *
 * @param <ID>
 *            The type for the id of the entity which has translated attributes
 * @param <T>
 *            The type which implements the translation for the entity
 */
public class FieldTranslationFactory<ID extends Serializable, T extends AbstractEntityTranslated<ID, Translation<T>>>
		implements FieldFactory {
	private EntityModel<T> em;
	private T entity;
	private HashMap<String, Field<?>> fields = new HashMap<>();
	private boolean viewMode;

	/**
	 * Default constructor
	 */
	public FieldTranslationFactory() {
		super();
	}

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

	protected void createFields() {
		if (em != null && entity != null) {
			FieldFactoryContextImpl context = new FieldFactoryContextImpl();
			context.setParentEntity(entity);
			context.setViewMode(viewMode);
			for (AttributeModel am : em.getAttributeModels()) {
				context.setAttributeModel(am);
				Field<?> tt = constructField(context);
				if (tt != null) {
					fields.put(am.getName(), tt);
				}
			}
		}
	}

	protected static Field<?> createField(AttributeModel attributeModel) {
		Field<?> field = null;
		return field;
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

	@SuppressWarnings("unchecked")
	@Override
	public Field<?> constructField(Context context) {
		AttributeModel am = context.getAttributeModel();
		if (am.isVisible() && am.getNestedEntityModel() != null
				&& Translation.class
						.isAssignableFrom(am.getNestedEntityModel().getEntityClass())
				&& context.getParentEntity() != null) {
			Collection<Translation<T>> items = (Collection<Translation<T>>) ClassUtils
					.getFieldValue(context.getParentEntity(), am.getName());
			final EntityModel<Translation<T>> nem = (EntityModel<Translation<T>>) ServiceLocatorFactory
					.getServiceLocator().getEntityModelFactory()
					.getModel(am.getNestedEntityModel().getEntityClass());
			TranslationTable<ID, T> tt = new TranslationTable<>(context.getParentEntity(),
					am.getName(), items, nem, context.getViewMode());
			tt.setRequired(am.isRequired());
			tt.setCaption(am.getDisplayName());
			return tt;
		}
		return null;
	}
}
