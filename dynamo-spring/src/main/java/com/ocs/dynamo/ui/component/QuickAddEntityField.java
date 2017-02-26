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
package com.ocs.dynamo.ui.component;

import java.io.Serializable;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.vaadin.data.Container.Filter;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.UI;

/**
 * Base class for components that display an Entity or collection of entities and that allow the
 * user to easily add new values on the fly
 * 
 * @author bas.rutten
 *
 * @param <ID>
 *            the type of the primary key
 * @param <T>
 *            the type of the entity
 * @param <U>
 *            the type of the value of the component (can be an entity, or a collection of entities)
 */
public abstract class QuickAddEntityField<ID extends Serializable, T extends AbstractEntity<ID>, U> extends
        CustomEntityField<ID, T, U> {

	private UI ui = UI.getCurrent();

	private static final long serialVersionUID = 7118578276952170818L;

	/**
	 * The button that brings up the dialog for adding a new entity
	 */
	private Button addButton;

	/**
	 * Constructor
	 * 
	 * @param service
	 *            the service used to interact with the storage
	 * @param entityModel
	 *            the entity model of the entity that is being displayed
	 * @param attributeModel
	 *            the attribute model
	 * @param filter
	 *            the search filter
	 */
	public QuickAddEntityField(BaseService<ID, T> service, EntityModel<T> entityModel, AttributeModel attributeModel,
	        Filter filter) {
		super(service, entityModel, attributeModel, filter);
	}

	/**
	 * Method that is called after a new entity has been successfully created. Use this to add the
	 * new entity to the component and select it
	 * 
	 * @param entity
	 */
	protected abstract void afterNewEntityAdded(T entity);

	/**
	 * Constructs the button that brings up the dialog that allows the user to add a new item
	 * 
	 * @return
	 */
	protected Button constructAddButton() {
		addButton = new Button(getMessageService().getMessage("ocs.add"));
		addButton.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 4074804834729142520L;

			@Override
			public void buttonClick(ClickEvent event) {
				AddNewValueDialog<ID, T> dialog = new AddNewValueDialog<ID, T>(getEntityModel(), getAttributeModel(),
				        getService(), getMessageService()) {

					private static final long serialVersionUID = 2040216794358094524L;

					@Override
					protected void afterNewEntityAdded(T entity) {
						QuickAddEntityField.this.afterNewEntityAdded(entity);
					}

				};
				dialog.build();
				ui.addWindow(dialog);
			}
		});
		return addButton;
	}

	public Button getAddButton() {
		return addButton;
	}

	public UI getUi() {
		return ui;
	}

}
