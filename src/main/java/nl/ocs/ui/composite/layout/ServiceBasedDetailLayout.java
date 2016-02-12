package nl.ocs.ui.composite.layout;

import java.io.Serializable;

import nl.ocs.dao.query.FetchJoinInformation;
import nl.ocs.domain.AbstractEntity;
import nl.ocs.domain.model.EntityModel;
import nl.ocs.service.BaseService;
import nl.ocs.ui.composite.form.FormOptions;

import com.vaadin.data.sort.SortOrder;

/**
 * A split layout that contains a reference to the parent object
 * 
 * @author bas.rutten
 * 
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 * @param <ID2>
 *            type of the primary key of the parent
 * @param <Q>
 *            type of the parent entity
 */
public abstract class ServiceBasedDetailLayout<ID extends Serializable, T extends AbstractEntity<ID>, ID2 extends Serializable, Q extends AbstractEntity<ID2>>
		extends ServiceBasedSplitLayout<ID, T> {

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
	public ServiceBasedDetailLayout(BaseService<ID, T> service, Q parentEntity,
			BaseService<ID2, Q> parentService, EntityModel<T> entityModel, FormOptions formOptions,
			SortOrder sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, formOptions, sortOrder, joins);
		this.parentEntity = parentEntity;
		this.parentService = parentService;
	}

	@Override
	public void reload() {
		super.reload();
		setParentEntity(getParentService().fetchById(getParentEntity().getId()));
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
