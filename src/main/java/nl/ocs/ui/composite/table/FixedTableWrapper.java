package nl.ocs.ui.composite.table;

import java.io.Serializable;
import java.util.Collection;

import nl.ocs.domain.AbstractEntity;
import nl.ocs.domain.model.EntityModel;
import nl.ocs.service.BaseService;
import nl.ocs.ui.container.QueryType;

import com.vaadin.data.Container;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.BeanItemContainer;

/**
 * A wrapper for a table that displays a fixed number of items
 * 
 * @author bas.rutten
 * 
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
public class FixedTableWrapper<ID extends Serializable, T extends AbstractEntity<ID>> extends
		BaseTableWrapper<ID, T> {

	private static final long serialVersionUID = -6711832174203817230L;

	// the collection of items to display
	private Collection<T> items;

	/**
	 * Constructor
	 * 
	 * @param service
	 * @param entityModel
	 * @param items
	 * @param sortOrder
	 */
	public FixedTableWrapper(BaseService<ID, T> service, EntityModel<T> entityModel,
			Collection<T> items, SortOrder sortOrder) {
		super(service, entityModel, QueryType.NONE, sortOrder, null);
		this.items = items;
	}

	@Override
	protected Container constructContainer() {
		BeanItemContainer<T> container = new BeanItemContainer<T>(getService().getEntityClass());
		container.addAll(items);
		return container;
	}

	@Override
	public void reloadContainer() {
		// not needed
	}
}
