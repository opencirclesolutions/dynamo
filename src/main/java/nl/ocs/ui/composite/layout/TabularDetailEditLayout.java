package nl.ocs.ui.composite.layout;

import java.io.Serializable;

import nl.ocs.dao.query.FetchJoinInformation;
import nl.ocs.domain.AbstractEntity;
import nl.ocs.domain.model.EntityModel;
import nl.ocs.service.BaseService;
import nl.ocs.ui.composite.form.FormOptions;

import com.vaadin.data.sort.SortOrder;

/**
 * A tabular edit layout that keeps a reference to a parent object of the
 * collection being edited
 * 
 * @author bas.rutten
 *
 * @param <ID>
 * @param <T>
 * @param <ID2>
 * @param <Q>
 */
public abstract class TabularDetailEditLayout<ID extends Serializable, T extends AbstractEntity<ID>, ID2 extends Serializable, Q extends AbstractEntity<ID2>>
		extends TabularEditLayout<ID, T> {

	private static final long serialVersionUID = -3432301286152665223L;

	private final BaseService<ID2, Q> parentService;

	private Q parentEntity;

	/**
	 * Constructor
	 * 
	 * @param service
	 * @param parentEntity
	 * @param parentService
	 * @param entityModel
	 * @param formOptions
	 * @param sortOrder
	 * @param joins
	 */
	public TabularDetailEditLayout(BaseService<ID, T> service, Q parentEntity,
			BaseService<ID2, Q> parentService, EntityModel<T> entityModel, FormOptions formOptions,
			SortOrder sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, formOptions, sortOrder, joins);
		this.parentService = parentService;
		this.parentEntity = parentEntity;
	}

	public void setParentEntity(Q parentEntity) {
		this.parentEntity = parentEntity;
	}

	public Q getParentEntity() {
		return parentEntity;
	}

	public BaseService<ID2, Q> getParentService() {
		return parentService;
	}

}
