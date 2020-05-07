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

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.functional.domain.AbstractEntityTranslated;
import com.ocs.dynamo.functional.domain.Translation;
import com.ocs.dynamo.ui.CanAssignEntity;
import com.ocs.dynamo.ui.component.DetailsEditGrid;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.TextArea;

import java.util.Collection;

/**
 * Grid component for managing the translations for a single field
 * 
 * @author Bas Rutten
 *
 * @param <ID> the ID of the translation entity
 * @param <E> the type of the translation entity
 */
public class TranslationGrid<ID, E extends AbstractEntityTranslated<ID, Translation<E>>>
		extends DetailsEditGrid<Integer, Translation<E>> implements CanAssignEntity<ID, E> {

	private static final int TEXT_AREA_ROWS = 4;

	private static final long serialVersionUID = 4974840467576193534L;

	/**
	 * The parent entity
	 */
	private E entity;

	/**
	 * The field name for which the translations are added
	 */
	private String fieldName;

	/**
	 * Whether the addition of new values is disabled
	 */
	private boolean localesRestricted;

	/**
	 * Constructor
	 * 
	 * @param entity
	 * @param fieldName
	 * @param entityModel
	 * @param attributeModel
	 * @param viewMode
	 * @param localesRestricted
	 */
	public TranslationGrid(E entity, String fieldName, EntityModel<Translation<E>> entityModel,
			AttributeModel attributeModel, boolean viewMode, boolean localesRestricted) {
		super(entityModel, attributeModel, viewMode,
				new FormOptions().setHideAddButton(localesRestricted).setShowRemoveButton(!localesRestricted));
		this.entity = entity;
		this.fieldName = fieldName;
		this.localesRestricted = localesRestricted;
	}

	@Override
	protected Component initContent() {
		Component result = super.initContent();
//		getGrid().setUpdateCaption(false);
//		getGrid().setCaption(null);

		// manually change row height if there are any text areas to be displayed
		if (entity.getTextAreaFields().contains(fieldName)) {
			getGrid().setHeight("100"); // Correct?
		}
		return result;
	}

	@Override
	public void assignEntity(E entity) {
		this.entity = entity;
		setCreateEntitySupplier(() -> {
			Translation<E> translation;
			try {
				translation = getEntityModel().getEntityClass().newInstance();
				translation.setField(fieldName);
				this.entity.addTranslation(translation);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new OCSRuntimeException("Could not create translation", e);
			}
			return translation;
		});

		setRemoveEntityConsumer(t -> {
			this.entity.removeTranslation(t);
		});
	}

	@Override
	protected void postProcessComponent(AttributeModel am, Component comp) {
		if (am.getPath().equals("locale") && localesRestricted) {
			comp.onEnabledStateChanged(false);
		}
	}

	@Override
	protected Component constructCustomField(EntityModel<Translation<E>> entityModel,
			AttributeModel attributeModel, boolean viewMode) {
		final Collection<String> textAreaFields = entity.getTextAreaFields();
		if (textAreaFields.contains(fieldName) && attributeModel.getName().equals("translation")) {
			TextArea ta = new TextArea();
//			ta.setRows(TEXT_AREA_ROWS);
			ta.setHeight(TEXT_AREA_ROWS * 14 + ""); // correct?
			return ta;

		}
		return null;
	}

}
