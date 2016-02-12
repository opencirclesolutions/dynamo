package nl.ocs.ui.composite.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import nl.ocs.domain.AbstractEntity;
import nl.ocs.domain.model.AttributeModel;
import nl.ocs.domain.model.EntityModel;
import nl.ocs.domain.model.EntityModelFactory;
import nl.ocs.domain.model.impl.ModelBasedFieldFactory;
import nl.ocs.service.MessageService;
import nl.ocs.ui.composite.table.export.TableExportActionHandler;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;

/**
 * A Table that bases its columns on the meta model of an entity
 * 
 * @author bas.rutten
 * 
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
public class ModelBasedTable<ID extends Serializable, T extends AbstractEntity<ID>> extends Table {

	private static final long serialVersionUID = 6946260934644731038L;

	private Container container;

	private EntityModel<T> entityModel;

	private EntityModelFactory entityModelFactory;

	private MessageService messageService;

	/**
	 * Constructor
	 * 
	 * @param container
	 * @param model
	 * @param entityModelFactory
	 * @param messageService
	 */
	public ModelBasedTable(Container container, EntityModel<T> model,
			EntityModelFactory entityModelFactory, MessageService messageService) {
		super("", container);
		this.container = container;
		this.entityModel = model;
		this.messageService = messageService;
		this.entityModelFactory = entityModelFactory;

		TableUtils.defaultInitialization(this);

		// add a custom field factory that takes care of special cases and
		// validation
		this.setTableFieldFactory(ModelBasedFieldFactory.getInstance(entityModel, messageService));

		generateColumns(this, container, model);

		// add export functionality
		List<EntityModel<?>> list = new ArrayList<>();
		list.add(model);
		addActionHandler(new TableExportActionHandler(UI.getCurrent(), entityModelFactory, list,
				messageService, model.getDisplayNamePlural(), null, false, null));

		addItemSetChangeListener(new ItemSetChangeListener() {

			private static final long serialVersionUID = 3035240490920769456L;

			@Override
			public void containerItemSetChange(ItemSetChangeEvent event) {
				updateTableCaption();
			}
		});
	}

	/**
	 * Overridden to deal with custom formatting
	 */
	@Override
	protected String formatPropertyValue(Object rowId, Object colId, Property<?> property) {
		String result = TableUtils.formatPropertyValue(this, entityModelFactory, entityModel,
				messageService, rowId, colId, property);
		if (result != null) {
			return result;
		}
		return super.formatPropertyValue(rowId, colId, property);
	}

	/**
	 * Generates the columns of the table based on the entity model
	 * 
	 * @param container
	 *            the container
	 * @param model
	 *            the entity model
	 */
	protected void generateColumns(Table table, Container container, EntityModel<T> model) {
		generateColumns(table, container, model.getAttributeModels());
		table.setCaption(model.getDisplayNamePlural());
		table.setDescription(model.getDescription());
	}

	/**
	 * Generates the columns of the table based on a select number of attribute
	 * models
	 * 
	 * @param table
	 * @param container
	 * @param attributeModels
	 */
	protected void generateColumns(Table table, Container container,
			List<AttributeModel> attributeModels) {
		List<Object> propertyNames = new ArrayList<>();
		List<String> headerNames = new ArrayList<>();

		for (AttributeModel attributeModel : attributeModels) {
			if (attributeModel.isVisibleInTable()) {
				propertyNames.add(attributeModel.getName());
				headerNames.add(attributeModel.getDisplayName());

				// for the lazy query container we explicitly have to add the
				// properties - for the standard Bean container this is not
				// needed
				if (container instanceof LazyQueryContainer) {
					LazyQueryContainer lazyContainer = (LazyQueryContainer) container;
					if (!lazyContainer.getContainerPropertyIds().contains(attributeModel.getName())) {
						lazyContainer.addContainerProperty(attributeModel.getName(),
								attributeModel.getType(), attributeModel.getDefaultValue(),
								attributeModel.isReadOnly(), attributeModel.isSortable());
					}
				}

				if (attributeModel.isNumerical()) {
					table.setColumnAlignment(attributeModel.getName(), Table.Align.RIGHT);
				}
			}
		}

		table.setVisibleColumns(propertyNames.toArray());
		table.setColumnHeaders(headerNames.toArray(new String[0]));
	}

	public Container getContainer() {
		return container;
	}

	public void updateTableCaption() {
		setCaption(entityModel.getDisplayNamePlural() + " "
				+ messageService.getMessage("ocs.showing.results", getContainerDataSource().size()));
	}

}
