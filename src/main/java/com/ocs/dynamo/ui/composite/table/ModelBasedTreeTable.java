package com.ocs.dynamo.ui.composite.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.ServiceLocator;
import com.ocs.dynamo.ui.composite.table.export.TableExportActionHandler;
import com.ocs.dynamo.ui.container.hierarchical.ModelBasedHierarchicalContainer;
import com.ocs.dynamo.ui.container.hierarchical.HierarchicalContainer.HierarchicalDefinition;
import com.ocs.dynamo.ui.container.hierarchical.ModelBasedHierarchicalContainer.ModelBasedHierarchicalDefinition;
import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.UI;

/**
 * Model aware tree table.
 * 
 * @author Patrick Deenen (patrick.deenen@opencirclesolutions.nl)
 *
 */
public class ModelBasedTreeTable<ID extends Serializable, T extends AbstractEntity<ID>> extends
		TreeTable implements Action.Handler {

	private static final long serialVersionUID = -2011675569709594136L;

	private Action actionExpandAll;

	private Action actionHideAll;

	private EntityModelFactory entityModelFactory;

	private MessageService messageService;

	/**
	 * 
	 * @param container
	 * @param entityModelFactory
	 * @param messageService
	 */
	@SuppressWarnings("unchecked")
	public ModelBasedTreeTable(ModelBasedHierarchicalContainer<T> container,
			EntityModelFactory entityModelFactory) {
		super("", container);
		this.messageService = ServiceLocator.getMessageService();
		this.entityModelFactory = entityModelFactory;
		EntityModel<T> rootEntityModel = (EntityModel<T>) container.getHierarchicalDefinition(0)
				.getEntityModel();
		TableUtils.defaultInitialization(this);

		setCaption(rootEntityModel.getDisplayName());

		// add a custom field factory that takes care of special cases and
		// validation
		this.setTableFieldFactory(container.new HierarchicalFieldFactory(container, messageService));

		generateColumns(container, rootEntityModel);

		actionExpandAll = new Action(messageService.getMessage("ocs.expandAll"));
		actionHideAll = new Action(messageService.getMessage("ocs.hideAll"));
		addActionHandler(this);
		addActionHandler(new TableExportActionHandler(UI.getCurrent(), entityModelFactory,
				getEntityModels(), messageService, rootEntityModel.getDisplayName(), null, true,
				null));
	}

	/**
	 * Adds a container property
	 * 
	 * @param scpId
	 * @param def
	 * @param lazyContainer
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	protected AttributeModel addContainerProperty(Object scpId,
			ModelBasedHierarchicalDefinition def, LazyQueryContainer lazyContainer) {
		AttributeModel aModel = null;
		if (scpId != null) {
			AttributeModel attributeModel = def.getEntityModel()
					.getAttributeModel(scpId.toString());
			if (attributeModel != null) {
				if (aModel == null) {
					aModel = attributeModel;
				}
				if (!lazyContainer.getContainerPropertyIds().contains(attributeModel.getName())) {
					lazyContainer.addContainerProperty(attributeModel.getName(),
							attributeModel.getType(), attributeModel.getDefaultValue(),
							attributeModel.isReadOnly(), attributeModel.isSortable());
				}
			}
		}
		return aModel;
	}

	/**
	 * Overridden to deal with custom formatting
	 */
	@Override
	protected String formatPropertyValue(Object rowId, Object colId, Property<?> property) {
		String result = TableUtils.formatPropertyValue(this, entityModelFactory, null,
				messageService, rowId, colId, property);
		if (result != null) {
			return result;
		}
		return super.formatPropertyValue(rowId, colId, property);
	}

	/**
	 * Generates the columns of the table based on the metadata model
	 * 
	 * @param container
	 *            the container
	 * @param model
	 *            the entity model
	 */
	@SuppressWarnings("rawtypes")
	public void generateColumns(ModelBasedHierarchicalContainer<T> container, EntityModel<T> model) {
		List<Object> propertyNames = new ArrayList<>();
		List<String> headerNames = new ArrayList<>();

		int propIndex = 0;
		for (Object propId : container.getContainerPropertyIds()) {
			AttributeModel aModel = null;
			// Define columns on every level when needed
			for (int level = 0; level < container.getHierarchy().size(); level++) {
				// Get sub container
				ModelBasedHierarchicalDefinition def = container.getHierarchicalDefinition(level);
				Indexed c = def.getContainer();

				// for the lazy query container we explicitly have to add the
				// properties - for the standard Bean container this is not
				// needed
				if (c instanceof LazyQueryContainer && propIndex < def.getPropertyIds().size()) {
					LazyQueryContainer lazyContainer = (LazyQueryContainer) c;
					Object scpId = def.getPropertyIds().get(propIndex);
					aModel = addContainerProperty(scpId, def, lazyContainer);
				}

				if (aModel != null && aModel.isNumerical()) {
					this.setColumnAlignment(aModel.getName(), Table.Align.RIGHT);
				}
			}
			// When the property is not available in the top level, use a lower
			// level definition
			AttributeModel rootAttributeModel = model.getAttributeModel(propId.toString());
			if (rootAttributeModel == null) {
				rootAttributeModel = aModel;
			}
			propertyNames.add(propId);
			headerNames.add(rootAttributeModel.getDisplayName());
			propIndex++;
		}

		setCaption(model.getDisplayNamePlural());
		setDescription(model.getDescription());
		setVisibleColumns(propertyNames.toArray());
		setColumnHeaders(headerNames.toArray(new String[0]));
	}

	@Override
	public Action[] getActions(Object target, Object sender) {
		return new Action[] { actionExpandAll, actionHideAll };
	}

	@SuppressWarnings("rawtypes")
	private List<EntityModel<?>> getEntityModels() {
		List<EntityModel<?>> models = new ArrayList<>();
		ModelBasedHierarchicalContainer<?> modelContainer = (ModelBasedHierarchicalContainer<?>) getContainerDataSource();

		for (Entry<Integer, HierarchicalDefinition> e : modelContainer.getHierarchy().entrySet()) {
			if (e.getValue() instanceof ModelBasedHierarchicalDefinition) {
				models.add(((ModelBasedHierarchicalDefinition) e.getValue()).getEntityModel());
			}
		}
		return models;
	}

	/**
	 * Handles an action
	 * 
	 * @param action
	 * @param sender
	 * @param target
	 */
	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if (action == actionExpandAll || action == actionHideAll) {
			boolean expand = action == actionExpandAll;

			// When there are rows selected collapse them
			Object s = getValue();
			if (s != null && !((Collection<?>) s).isEmpty()) {
				if (s instanceof Collection<?>) {
					Collection<?> col = (Collection<?>) s;
					if (col.size() == 1) {
						for (Object id : (Collection<?>) s) {
							setCollapseAll(id, !expand);
						}
					} else {
						Notification.show(messageService.getMessage("ocs.select.single.row"),
								Notification.Type.ERROR_MESSAGE);
					}
				} else {
					setCollapseAll(s, !expand);
				}
			} else if (target != null) {
				Notification.show(messageService.getMessage("ocs.select.row"),
						Notification.Type.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Recursively sets the collapsed state of the item identified by the
	 * provided ID
	 * 
	 * @param itemId
	 *            the ID of the item
	 * @param collapse
	 *            whether to collapse the items (as opposed to expanding them)
	 */
	public void setCollapseAll(Object itemId, boolean collapse) {
		if (itemId != null) {
			setCollapsed(itemId, collapse);
			Collection<?> ch = getChildren(itemId);
			if (ch != null && !ch.isEmpty()) {
				for (Object cid : ch) {
					setCollapseAll(cid, collapse);
				}
			}
		}
	}

	public Action getActionExpandAll() {
		return actionExpandAll;
	}

	public Action getActionHideAll() {
		return actionHideAll;
	}
}
