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
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.Getter;

/**
 * A custom field that can be used to edit an AbstractEntity or collection
 * of AbstractEntities
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key of the entity
 * @param <T>  the type of the entity
 * @param <U>  the type of the value of the component (can typically be an
 *             entity or a collection of entities)
 */
public abstract class CustomEntityField<ID extends Serializable, T extends AbstractEntity<ID>, U> extends CustomField<U>
		implements Cascadable<T> {

	private static final long serialVersionUID = 8898382056620026384L;

	@Getter
	private SerializablePredicate<T> filter;

	@Getter
	private final BaseService<ID, T> service;

	@Getter
	private final MessageService messageService;

	@Getter
	private final EntityModel<T> entityModel;

	@Getter
	private final AttributeModel attributeModel;

	protected CustomEntityField(BaseService<ID, T> service, EntityModel<T> entityModel, AttributeModel attributeModel,
			SerializablePredicate<T> filter) {
		this.service = service;
		this.entityModel = entityModel;
		this.attributeModel = attributeModel;
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		setFilter(filter);
	}

	/**
	 * Sets the search filter to the provided filter then refreshes the data
	 * provider
	 * 
	 * @param filter the new filter
	 */
	public abstract void refresh(SerializablePredicate<T> filter);

	/**
	 * Sets the filter to apply to the component
	 * @param filter the filter to set
	 */
	public void setFilter(SerializablePredicate<T> filter) {
		this.filter = filter;
	}

	public void setPlaceholder(String placeholder) {
		// override in subclass
	}

}
