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
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.vaadin.data.Container.Filter;
import com.vaadin.ui.CustomField;

/**
 * A custom field that can be used to edit an AbstractEntity or collection thereof
 * 
 * @author bas.rutten
 *
 * @param <ID>
 *            the type of the primary key of the entity
 * @param <T>
 *            the type of the entity
 * @param <U>
 *            the type of the value of the component (can typically be an entity or a collection of
 *            entities)
 */
public abstract class CustomEntityField<ID extends Serializable, T extends AbstractEntity<ID>, U> extends
        CustomField<U> implements Cascadable {

	private static final long serialVersionUID = 8898382056620026384L;

	/**
	 * The filter used to limit the entities to choose from
	 */
	private Filter filter;

	/**
	 * The service
	 */
	private final BaseService<ID, T> service;

	/**
	 * The message service
	 */
	private final MessageService messageService;

	/**
	 * The entity model of the entities that are displayed in the component
	 */
	private final EntityModel<T> entityModel;

	/**
	 * The attribute model used to define the behaviour of the component
	 */
	private final AttributeModel attributeModel;

	/**
	 * Constructor
	 * 
	 * @param service
	 * @param entityModel
	 * @param attributeModel
	 */
	public CustomEntityField(BaseService<ID, T> service, EntityModel<T> entityModel, AttributeModel attributeModel, 
			Filter filter) {
		this.service = service;
		this.entityModel = entityModel;
		this.attributeModel = attributeModel;
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		setFilter(filter);
	}

	public AttributeModel getAttributeModel() {
		return attributeModel;
	}

	public EntityModel<T> getEntityModel() {
		return entityModel;
	}

	public Filter getFilter() {
		return filter;
	}

	public MessageService getMessageService() {
		return messageService;
	}

	public BaseService<ID, T> getService() {
		return service;
	}

	/**
	 * Sets the search filter to the provided filter then updates the lookup list
	 * 
	 * @param filter
	 *            the new filter
	 */
	public abstract void refresh(Filter filter);

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

}
