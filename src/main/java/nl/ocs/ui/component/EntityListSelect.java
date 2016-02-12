package nl.ocs.ui.component;

import java.io.Serializable;
import java.util.List;

import nl.ocs.domain.AbstractEntity;
import nl.ocs.domain.model.AttributeModel;
import nl.ocs.domain.model.EntityModel;
import nl.ocs.filter.FilterConverter;
import nl.ocs.service.BaseService;
import nl.ocs.utils.SortUtil;

import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.ListSelect;

/**
 * Custom ListSelect component for displaying a collection of entities
 * 
 * @author bas.rutten
 * 
 * @param <ID>
 *            type of the primary key of the entity
 * @param <T>
 *            type of the entity
 */
public class EntityListSelect<ID extends Serializable, T extends AbstractEntity<ID>> extends
		ListSelect {

	private static final long serialVersionUID = 3041574615271340579L;

	private final AttributeModel attributeModel;

	private SelectMode selectMode = SelectMode.FILTERED;

	private final SortOrder[] sortOrder;

	public enum SelectMode {
		ALL, FILTERED, FIXED;
	}

	/**
	 * Constructor - for the "ALL" mode
	 * 
	 * @param targetEntityModel
	 *            the entity model of the entities that are to be displayed
	 * @param attributeModel
	 *            the attribute model for the property that is bound to this
	 *            component
	 * @param service
	 *            the service used to retrieve entities
	 */
	public EntityListSelect(EntityModel<T> targetEntityModel, AttributeModel attributeModel,
			BaseService<ID, T> service, SortOrder... sortOrder) {
		this(targetEntityModel, attributeModel, service, SelectMode.ALL, null, null, sortOrder);
	}

	/**
	 * Constructor - for the "FIXED" mode
	 * 
	 * @param targetEntityModel
	 *            the entity model of the entities that are to be displayed
	 * @param attributeModel
	 *            the attribute model for the property that is bound to this
	 *            component
	 * @param items
	 *            the list of entities to display
	 */
	public EntityListSelect(EntityModel<T> targetEntityModel, AttributeModel attributeModel,
			List<T> items) {
		this(targetEntityModel, attributeModel, null, SelectMode.FIXED, null, items);
	}

	/**
	 * Constructor - for the "FILTERED" mode
	 * 
	 * @param targetEntityModel
	 *            the entity model of the entities that are to be displayed
	 * @param attributeModel
	 *            the attribute model for the property that is bound to this
	 *            component
	 * @param service
	 *            the service used to retrieve the entities
	 * @param filter
	 *            the filter used to filter the entities
	 * @param sortOrder
	 *            the sort order used to sort the entities
	 */
	public EntityListSelect(EntityModel<T> targetEntityModel, AttributeModel attributeModel,
			BaseService<ID, T> service, Filter filter, SortOrder... sortOrder) {
		this(targetEntityModel, attributeModel, service, SelectMode.FILTERED, filter, null,
				sortOrder);
	}

	/**
	 * Constructor
	 * 
	 * @param targetEntityModel
	 * @param attributeModel
	 * @param service
	 * @param mode
	 * @param filter
	 * @param items
	 * @param itemCaptionPropertyId
	 * @param sortOrder
	 */
	public EntityListSelect(EntityModel<T> targetEntityModel, AttributeModel attributeModel,
			BaseService<ID, T> service, SelectMode mode, Filter filter, List<T> items,
			SortOrder... sortOrder) {

		this.selectMode = mode;
		this.sortOrder = sortOrder;
		this.attributeModel = attributeModel;

		if (attributeModel != null) {
			this.setCaption(attributeModel.getDisplayName());
		}

		BeanItemContainer<T> container = new BeanItemContainer<T>(
				targetEntityModel.getEntityClass());
		this.setContainerDataSource(container);

		if (SelectMode.ALL.equals(mode)) {
			// add all items (but sorted)
			container.addAll(service.findAll(SortUtil.translate(sortOrder)));
		} else if (SelectMode.FILTERED.equals(mode)) {
			// add a filtered selection of items
			items = service.find(new FilterConverter().convert(filter),
					SortUtil.translate(sortOrder));
			container.addAll(items);
		} else if (SelectMode.FIXED.equals(mode)) {
			container.addAll(items);
		}

		setItemCaptionMode(ItemCaptionMode.PROPERTY);
		setItemCaptionPropertyId(targetEntityModel.getDisplayProperty());
		setSizeFull();
	}

	public SelectMode getSelectMode() {
		return selectMode;
	}

	public SortOrder[] getSortOrder() {
		return sortOrder;
	}

	public AttributeModel getAttributeModel() {
		return attributeModel;
	}

}
