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
import java.util.HashMap;
import java.util.Map;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.ui.composite.grid.ComponentContext;
import com.ocs.dynamo.ui.composite.layout.BaseCustomComponent;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.Getter;
import lombok.Setter;

/**
 * Abstract base class for model-based forms - this includes both search forms
 * and edit forms
 * 
 * @author bas.rutten
 * @param <ID> the type of the primary key
 * @param <T>  the type of the entity
 */
public abstract class AbstractModelBasedForm<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseCustomComponent {

	private static final long serialVersionUID = -1163137979989646987L;

	/**
	 * The entity model on which to base the form
	 */
	private EntityModel<T> entityModel;

//	/**
//	 * Map for keeping track of custom entity models for certain fields
//	 */
//	private Map<String, String> fieldEntityModels = new HashMap<>();

	/**
	 * Field filters for easily building filtered combo boxes
	 */
	private Map<String, SerializablePredicate<?>> fieldFilters = new HashMap<>();

	@Getter
	private final FormOptions formOptions;

	@Getter
	@Setter
	private ComponentContext<ID, T> componentContext = ComponentContext.<ID, T>builder().build();

	/**
	 * Constructor
	 * 
	 * @param formOptions  the form options parameter object
	 * @param fieldFilters the field filter map
	 * @param entityModel  the entity model
	 */
	public AbstractModelBasedForm(FormOptions formOptions, Map<String, SerializablePredicate<?>> fieldFilters,
			EntityModel<T> entityModel) {
		this.formOptions = formOptions;
		this.fieldFilters = fieldFilters == null ? new HashMap<>() : fieldFilters;
		this.entityModel = entityModel;
	}

	public EntityModel<T> getEntityModel() {
		return entityModel;
	}

	/**
	 * Returns the overruled entity model to be used for the rendering of a certain
	 * field
	 * 
	 * @param attributeModel the attribute model
	 * @return
	 */
	protected final EntityModel<?> getFieldEntityModel(AttributeModel attributeModel) {
		String reference = getFieldEntityModels().get(attributeModel.getPath());
		return reference == null ? null
				: getEntityModelFactory().getModel(reference, attributeModel.getNormalizedType());
	}

	public Map<String, String> getFieldEntityModels() {
		return getComponentContext().getFieldEntityModels();
	}

	public Map<String, SerializablePredicate<?>> getFieldFilters() {
		return fieldFilters;
	}

	public void setEntityModel(EntityModel<T> entityModel) {
		this.entityModel = entityModel;
	}

//	public void setFieldEntityModels(Map<String, String> fieldEntityModels) {
//		this.fieldEntityModels = fieldEntityModels;
//	}

	public void setFieldFilters(Map<String, SerializablePredicate<?>> fieldFilters) {
		this.fieldFilters = fieldFilters;
	}

}
