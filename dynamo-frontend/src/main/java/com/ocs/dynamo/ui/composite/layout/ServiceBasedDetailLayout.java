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
import java.util.function.Function;
import java.util.function.Supplier;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.CanAssignEntity;
import com.ocs.dynamo.ui.provider.QueryType;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.Getter;
import lombok.Setter;

/**
 * A split layout that contains a reference to the parent object
 * 
 * @author bas.rutten
 * @param <ID>  type of the primary key
 * @param <T>   type of the entity
 * @param <ID2> type of the primary key of the parent
 * @param <Q>   type of the parent entity
 */
public class ServiceBasedDetailLayout<ID extends Serializable, T extends AbstractEntity<ID>, ID2 extends Serializable, Q extends AbstractEntity<ID2>>
		extends ServiceBasedSplitLayout<ID, T> implements CanAssignEntity<ID2, Q> {

	private static final long serialVersionUID = 1068860513192819804L;

	@Getter
	private final BaseService<ID2, Q> parentService;

	@Getter
	@Setter
	private Q parentEntity;

	/**
	 * The joins to use when refreshing the parent entity
	 */
	@Getter
	@Setter
	private FetchJoinInformation[] parentJoins;

	@Getter
	@Setter
	private Function<Q, SerializablePredicate<T>> parentFilterCreator;

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
			EntityModel<T> entityModel, QueryType queryType, FormOptions formOptions, SortOrder<?> sortOrder,
			FetchJoinInformation... joins) {
		super(service, entityModel, queryType, formOptions, sortOrder, joins);
		this.parentEntity = parentEntity;
		this.parentService = parentService;
	}

	@Override
	public void assignEntity(Q parentEntity) {
		setParentEntity(parentEntity);
	}

	@Override
	protected void buildFilter() {
		filter = parentFilterCreator == null ? null : parentFilterCreator.apply(getParentEntity());
	}

	@Override
	public void reload() {
		setParentEntity(getParentService().fetchById(getParentEntity().getId(), getParentJoins()));
		super.reload();
	}

	@Override
	public void setFilterCreator(Supplier<SerializablePredicate<T>> filterCreator) {
		throw new UnsupportedOperationException("Use the setParentFilterCreator method instead");
	}

}
