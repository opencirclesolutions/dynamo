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
package com.ocs.dynamo.ui.composite.layout;

import java.io.Serializable;

import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.CanAssignEntity;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.vaadin.data.sort.SortOrder;

/**
 * A split layout that contains a reference to the parent object
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 * @param <ID2>
 *            type of the primary key of the parent
 * @param <Q>
 *            type of the parent entity
 */
public class ServiceBasedDetailLayout<ID extends Serializable, T extends AbstractEntity<ID>, ID2 extends Serializable, Q extends AbstractEntity<ID2>>
        extends ServiceBasedSplitLayout<ID, T> implements CanAssignEntity<ID2, Q> {

	private static final long serialVersionUID = 1068860513192819804L;

	private final BaseService<ID2, Q> parentService;

	private Q parentEntity;

	/**
	 * Constructor
	 * 
	 * @param service
	 * @param parentEntity
	 * @param parentService
	 * @param formOptions
	 * @param sortOrder
	 * @param joins
	 */
	public ServiceBasedDetailLayout(BaseService<ID, T> service, Q parentEntity, BaseService<ID2, Q> parentService,
	        EntityModel<T> entityModel, FormOptions formOptions, SortOrder sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, formOptions, sortOrder, joins);
		this.parentEntity = parentEntity;
		this.parentService = parentService;
	}

	@Override
	public void assignEntity(Q parentEntity) {
		setParentEntity(parentEntity);
	}

	public Q getParentEntity() {
		return parentEntity;
	}

	public BaseService<ID2, Q> getParentService() {
		return parentService;
	}

	@Override
	public void reload() {
		setParentEntity(getParentService().fetchById(getParentEntity().getId()));
		super.reload();
	}

	public void setParentEntity(Q parentEntity) {
		this.parentEntity = parentEntity;
	}
}
