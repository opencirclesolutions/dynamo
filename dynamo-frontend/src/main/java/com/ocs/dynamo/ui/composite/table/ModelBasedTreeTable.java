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
package com.ocs.dynamo.ui.composite.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.composite.table.export.TableExportActionHandler;
import com.ocs.dynamo.ui.composite.table.export.TableExportMode;
import com.ocs.dynamo.ui.container.hierarchical.HierarchicalContainer.HierarchicalDefinition;
import com.ocs.dynamo.ui.container.hierarchical.ModelBasedHierarchicalContainer;
import com.ocs.dynamo.ui.container.hierarchical.ModelBasedHierarchicalContainer.ModelBasedHierarchicalDefinition;
import com.ocs.dynamo.ui.utils.FormatUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
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
 */
public class ModelBasedTreeTable<ID extends Serializable, T extends AbstractEntity<ID>> extends TreeTable
		implements Action.Handler {

	private static final long serialVersionUID = -2011675569709594136L;

	/**
	 * The action that is used to expand all nodes
	 */
	private Action actionExpandAll;

	/**
	 * The action that is used to collapse all nodes
	 */
	private Action actionCollapseAll;

	/**
	 * The entity model factory
	 */
	private EntityModelFactory entityModelFactory;

	/**
	 * The message service
	 */
	private MessageService messageService;

	/**
	 * Currency symbol to be used for displaying amounts in the table
	 */
	private String currencySymbol;

	/**
	 * Indicates whether it is allowed to expand/hide the
	 */
	private boolean expandAndHideAllowed = true;

	/**
	 * Whether exporting is allowed
	 */
	private boolean exportAllowed;

	/**
	 * Constructor
	 *
	 * @param container
	 *            the container that contains the data for the table
	 * @param entityModelFactory
	 *            the entity model factory
	 * @param messageService
	 *            the message service
	 */
	@SuppressWarnings("unchecked")
	public ModelBasedTreeTable(ModelBasedHierarchicalContainer<T> container, EntityModelFactory entityModelFactory,
			boolean exportAllowed) {
		super("", container);
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		this.entityModelFactory = entityModelFactory;
		this.exportAllowed = exportAllowed;

		EntityModel<T> rootEntityModel = (EntityModel<T>) container.getHierarchicalDefinition(0).getEntityModel();
		TableUtils.defaultInitialization(this);

		setCaption(rootEntityModel.getDisplayName());

		// add a custom field factory that takes care of special cases and
		// validation
		this.setTableFieldFactory(container.new HierarchicalFieldFactory(container, messageService));

		generateColumns(container, rootEntityModel);

		actionExpandAll = new Action(messageService.getMessage("ocs.expandAll", VaadinUtils.getLocale()));
		actionCollapseAll = new Action(messageService.getMessage("ocs.hideAll", VaadinUtils.getLocale()));
		addActionHandler(this);
		if (exportAllowed) {
			addActionHandler(new TableExportActionHandler(UI.getCurrent(), getEntityModels(),
					rootEntityModel.getDisplayNamePlural(), null, true, TableExportMode.EXCEL, null));
			addActionHandler(new TableExportActionHandler(UI.getCurrent(), getEntityModels(),
					rootEntityModel.getDisplayNamePlural(), null, true, TableExportMode.EXCEL_SIMPLIFIED, null));
			addActionHandler(new TableExportActionHandler(UI.getCurrent(), getEntityModels(),
					rootEntityModel.getDisplayNamePlural(), null, true, TableExportMode.CSV, null));
		}
	}

	/**
	 * Adds a container property
	 *
	 * @param scpId
	 *            the property
	 * @param def
	 *            the hierarchical definition
	 * @param lazyContainer
	 *            the container feeding this table
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	protected AttributeModel addContainerProperty(Object scpId, ModelBasedHierarchicalDefinition def,
			LazyQueryContainer lazyContainer) {
		AttributeModel aModel = null;
		if (scpId != null) {
			AttributeModel attributeModel = def.getEntityModel().getAttributeModel(scpId.toString());
			if (attributeModel != null) {
				aModel = attributeModel;
				if (!lazyContainer.getContainerPropertyIds().contains(attributeModel.getName())) {
					lazyContainer.addContainerProperty(attributeModel.getName(), attributeModel.getType(),
							attributeModel.getDefaultValue(),
							EditableType.READ_ONLY.equals(attributeModel.getEditableType()),
							attributeModel.isSortable());
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
		String result = FormatUtils.formatPropertyValue(this, entityModelFactory, null, rowId, colId, property, ",");
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

			if (rootAttributeModel != null) {
				propertyNames.add(propId);
				headerNames.add(rootAttributeModel.getDisplayName());
			}
			propIndex++;
		}

		setCaption(model.getDisplayNamePlural());
		setDescription(model.getDescription());
		setVisibleColumns(propertyNames.toArray());
		setColumnHeaders(headerNames.toArray(new String[0]));
	}

	public Action getActionExpandAll() {
		return actionExpandAll;
	}

	public Action getActionHideAll() {
		return actionCollapseAll;
	}

	@Override
	public Action[] getActions(Object target, Object sender) {
		if (isExpandAndHideAllowed()) {
			return new Action[] { actionExpandAll, actionCollapseAll };
		}
		return new Action[0];
	}

	public String getCurrencySymbol() {
		return currencySymbol;
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
	 *            the action
	 * @param sender
	 *            the sender of the action
	 * @param target
	 *            the target of the action
	 */
	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if (action == actionExpandAll || action == actionCollapseAll) {
			boolean expand = action == actionExpandAll;

			// When there are rows selected collapse them
			Object s = getValue();
			if (s != null) {
				if (s instanceof Collection<?>) {
					Collection<?> col = (Collection<?>) s;
					if (col.size() == 1) {
						for (Object id : (Collection<?>) s) {
							setCollapseAll(id, !expand);
						}
					} else {
						Notification.show(messageService.getMessage("ocs.select.single.row", VaadinUtils.getLocale()),
								Notification.Type.ERROR_MESSAGE);
					}
				} else {
					setCollapseAll(s, !expand);
				}
			} else if (target != null) {
				Notification.show(messageService.getMessage("ocs.select.row", VaadinUtils.getLocale()),
						Notification.Type.ERROR_MESSAGE);
			}
		}
	}

	public boolean isExpandAndHideAllowed() {
		return expandAndHideAllowed;
	}

	/**
	 * Recursively sets the collapsed state of the item identified by the provided
	 * ID and all of its children
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

	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}

	public void setExpandAndHideAllowed(boolean expandAndHideAllowed) {
		this.expandAndHideAllowed = expandAndHideAllowed;
	}

	public boolean isExportAllowed() {
		return exportAllowed;
	}

}
