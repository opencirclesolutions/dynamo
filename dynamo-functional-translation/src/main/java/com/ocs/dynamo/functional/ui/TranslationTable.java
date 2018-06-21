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

import java.util.Collection;

import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.functional.domain.AbstractEntityTranslated;
import com.ocs.dynamo.functional.domain.Translation;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.CanAssignEntity;
import com.ocs.dynamo.ui.composite.form.DetailsEditTable;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.vaadin.ui.Component;;

/**
 * @author Patrick.Deenen@opencircle.solutions
 * 
 *         Class which provides a generic implementation of a table to translate attributes of a entity. Should be used
 *         in combination with the FieldTranslationFactory.
 *
 */
public class TranslationTable<ID, E extends AbstractEntityTranslated<ID, Translation<E>>>
		extends DetailsEditTable<Integer, Translation<E>> implements CanAssignEntity<ID, E> {

	private static final long serialVersionUID = 4974840467576193534L;

	private E entity;
	private String fieldName;
	@SuppressWarnings("unchecked")
	private BaseService<Integer, Translation<E>> translationService = (BaseService<Integer, Translation<E>>) ServiceLocatorFactory
			.getServiceLocator()
			.getServiceForEntity(Translation.class);

	public TranslationTable(E entity, String fieldName,
			Collection<Translation<E>> items,
			EntityModel<Translation<E>> entityModel, boolean viewMode) {
		super(items, entityModel, viewMode, new FormOptions().setShowRemoveButton(true));
		this.entity = entity;
		this.fieldName = fieldName;
	}

	@Override
	protected Component initContent() {
		Component result = super.initContent();
		getTable().setUpdateTableCaption(false);
		return result;
	}

	@Override
	protected Translation<E> createEntity() {
		Translation<E> translation = null;
		try {
			translation = getEntityModel().getEntityClass().newInstance();
			translation.setField(fieldName);
			entity.addTranslation(translation);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new OCSRuntimeException("Could not create translation", e);
		}
		return translation;
	}

	@Override
	protected void removeEntity(Translation<E> toRemove) {
		// No need to remove the entity from the database if it has not been saved yet (no id)
		if(toRemove.getId() != null){
			translationService.delete(toRemove);
		}
		entity.removeTranslation(toRemove);
	}

	@Override
	public void assignEntity(E translation) {
		entity = translation;
	}

}
