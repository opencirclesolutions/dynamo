package com.ocs.dynamo.ui.composite.layout;

import java.io.Serializable;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.vaadin.data.sort.SortOrder;

/**
 * child edit panel that displays a fixed set of items (i.e. the table is filled
 * with an explicit set of items. No separate query is necessary to retrieve
 * those). Use this only when the data set is sufficiently small
 * 
 * @author bas.rutten
 * 
 * @param <ID>
 *            the type of the primary key
 * @param <T>
 *            the type of the entity
 * @param <ID2>
 *            the type of the primary key of the parent entity
 * @param <Q>
 *            the type of the parent entity
 */
public abstract class FixedDetailLayout<ID extends Serializable, T extends AbstractEntity<ID>, ID2 extends Serializable, Q extends AbstractEntity<ID2>>
		extends FixedSplitLayout<ID, T> {

	private static final long serialVersionUID = 4606800218149558500L;

	private final BaseService<ID2, Q> parentService;

	private Q parentEntity;

	/**
	 * Constructor
	 * 
	 * @param service
	 *            the service
	 * @param parentEntity
	 *            the parent entity
	 * @param parentService
	 *            the parent service
	 * @param entityModel
	 *            the entity model that is used to build the form
	 * @param formOptions
	 *            form options
	 * @param fieldFilters
	 *            filters that will be applied to the fields in the detail view
	 * @param sortOrder
	 */
	public FixedDetailLayout(BaseService<ID, T> service, Q parentEntity,
			BaseService<ID2, Q> parentService, EntityModel<T> entityModel, FormOptions formOptions,
			SortOrder sortOrder) {
		super(service, entityModel, formOptions, sortOrder);
		this.parentEntity = parentEntity;
		this.parentService = parentService;
	}

	public Q getParentEntity() {
		return parentEntity;
	}

	public void setParentEntity(Q parentEntity) {
		this.parentEntity = parentEntity;
	}

	public BaseService<ID2, Q> getParentService() {
		return parentService;
	}

}
