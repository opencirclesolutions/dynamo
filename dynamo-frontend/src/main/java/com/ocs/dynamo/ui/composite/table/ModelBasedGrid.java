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
import java.util.List;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.impl.FieldFactoryImpl;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.component.InternalLinkField;
import com.ocs.dynamo.ui.component.URLField;
import com.ocs.dynamo.ui.utils.FormatUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.data.BeanPropertySet;
import com.vaadin.data.HasValue;
import com.vaadin.data.PropertyFilterDefinition;
import com.vaadin.data.PropertySet;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;
import com.vaadin.ui.renderers.ComponentRenderer;

/**
 * A Table that bases its columns on the meta model of an entity
 * 
 * @author bas.rutten
 * @param <ID> type of the primary key
 * @param <T> type of the entity
 */
public class ModelBasedGrid<ID extends Serializable, T extends AbstractEntity<ID>> extends Grid<T> {

	private static final long serialVersionUID = 6946260934644731038L;

	/**
	 * Custom currency symbol to be used for this table
	 */
	private String currencySymbol;

	/**
	 * The entity model of the entities to display in the table
	 */
	private EntityModel<T> entityModel;

	/**
	 * Indicates whether table export is allowed
	 */
	private boolean exportAllowed;

	/***
	 * Indicate whether to update the caption with the number of items in the table
	 */
	private boolean updateTableCaption = true;

	private boolean editable = false;

	/**
	 * The message service
	 */
	private MessageService messageService;

	private FieldFactoryImpl<T> factory;

	/**
	 * Constructor
	 *
	 * @param container     the data container
	 * @param model         the entity model that determines what to display
	 * @param exportAllowed whether export of the table is allowed
	 */
	public ModelBasedGrid(DataProvider<T, SerializablePredicate<T>> dataProvider, EntityModel<T> model,
			boolean exportAllowed, boolean editable) {
		setDataProvider(dataProvider);
		this.editable = editable;
		getEditor().setEnabled(editable);

		// we need to pre-populate the table with the available properties
		PropertySet<T> ps = BeanPropertySet.get(model.getEntityClass(), true,
				new PropertyFilterDefinition(3, new ArrayList<>()));
		setPropertySet(ps);

		this.entityModel = model;
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		this.factory = FieldFactoryImpl.getInstance(model, messageService);
		this.exportAllowed = exportAllowed;

		GridUtils.defaultInitialization(this);

		generateColumns(model);

		// add export functionality
		if (isExportAllowed()) {
			List<EntityModel<?>> list = new ArrayList<>();
			list.add(model);
			// addActionHandler(new TableExportActionHandler(UI.getCurrent(), list,
			// model.getDisplayNamePlural(), null,
			// false, TableExportMode.EXCEL, null));
			// addActionHandler(new TableExportActionHandler(UI.getCurrent(), list,
			// model.getDisplayNamePlural(), null,
			// false, TableExportMode.EXCEL_SIMPLIFIED, null));
			// addActionHandler(new TableExportActionHandler(UI.getCurrent(), list,
			// model.getDisplayNamePlural(), null,
			// false, TableExportMode.CSV, null));
		}

	}

	/**
	 * Adds a column to the table
	 *
	 * @param attributeModel the (possibly nested) attribute model for which to add
	 *                       a column
	 * @param propertyNames  the properties to be added
	 * @param headerNames    the headers to be added
	 */
	private void addColumn(final AttributeModel attributeModel) {
		if (attributeModel.isVisibleInTable()) {
			Column<T, ?> column;
			if (attributeModel.isUrl()) {
				// URL field
				column = addColumn(t -> new URLField(
						new TextField("", ClassUtils.getFieldValueAsString(t, attributeModel.getPath(), "")),
						attributeModel, false), new ComponentRenderer());
			} else if (attributeModel.isNavigable() && AttributeType.MASTER.equals(attributeModel.getAttributeType())) {
				column = addColumn(t -> generateInternalLinkField(attributeModel,
						ClassUtils.getFieldValue(t, attributeModel.getPath())), new ComponentRenderer());
			} else if (editable) {
				column = addColumn(t -> {
					// value change listener to copy value back to backing bean (Vaadin binding
					// doesn't really
					// seem to work
					HasValue<?> comp = (HasValue<?>) createField(t, attributeModel);
					comp.addValueChangeListener(event -> {
						ClassUtils.setFieldValue(t, attributeModel.getPath(), event.getValue());
					});
					return (AbstractComponent) comp;
				}, new ComponentRenderer());

			} else {
				column = addColumn(t -> FormatUtils.extractAndFormat(this, attributeModel, t));
			}

			column.setCaption(attributeModel.getDisplayName()).setSortable(attributeModel.isSortable())
					.setSortProperty(attributeModel.getPath())
					.setStyleGenerator(item -> attributeModel.isNumerical() ? "v-align-right" : "");
		}
	}

	/**
	 * Creates a field and fills it with the desireed value
	 * 
	 * @param t              the entity
	 * @param attributeModel the attribute model
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <S> AbstractComponent createField(T t, AttributeModel attributeModel) {
		AbstractComponent comp = factory.constructField(attributeModel, null, null, true);
		S value = (S) ClassUtils.getFieldValue(t, attributeModel.getPath());
		if (value != null) {
			((HasValue<S>) comp).setValue(value);
		}
		return comp;
	}

	/**
	 * Generates the columns of the table based on the entity model
	 *
	 * @param container the container
	 * @param model     the entity model
	 */
	protected void generateColumns(EntityModel<T> model) {
		generateColumns(model.getAttributeModels());
		this.setCaption(model.getDisplayNamePlural());
		this.setDescription(model.getDescription());
	}

	/**
	 * Generates the columns of the table based on a select number of attribute
	 * models
	 *
	 * @param attributeModels the attribute models for which to generate columns
	 */
	protected void generateColumns(List<AttributeModel> attributeModels) {
		generateColumnsRecursive(attributeModels);
	}

	/**
	 * Recursively generate columns for the provided attribute models
	 * 
	 * @param attributeModels
	 */
	private void generateColumnsRecursive(List<AttributeModel> attributeModels) {
		for (AttributeModel attributeModel : attributeModels) {
			addColumn(attributeModel);
			if (attributeModel.getNestedEntityModel() != null) {
				generateColumnsRecursive(attributeModel.getNestedEntityModel().getAttributeModels());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <ID2 extends Serializable, S extends AbstractEntity<ID2>> InternalLinkField<ID2, S> generateInternalLinkField(
			AttributeModel attributeModel, Object value) {
		return new InternalLinkField<ID2, S>(attributeModel, null, (S) value);
	}

	public String getCurrencySymbol() {
		return currencySymbol;
	}

	public boolean isExportAllowed() {
		return exportAllowed;
	}

	public boolean isUpdateTableCaption() {
		return updateTableCaption;
	}

	/**
	 * Removes a generated column
	 *
	 * @param attributeModel the attribute model for which to remove the column
	 */
	private void removeGeneratedColumn(final AttributeModel attributeModel) {
		if (attributeModel.isVisibleInTable() && attributeModel.isUrl()) {
			// removeGeneratedColumn(attributeModel.getPath());
			removeColumn(attributeModel.getPath());
		}
	}

	/**
	 * Remove any generated columns - this is used when switching between modes in
	 * order to remove any generated columns containing URL fields
	 */
	public void removeGeneratedColumns() {
		removeGeneratedColumnsRecursive(entityModel.getAttributeModels());
	}

	private void removeGeneratedColumnsRecursive(List<AttributeModel> attributeModels) {
		for (AttributeModel attributeModel : attributeModels) {
			removeGeneratedColumn(attributeModel);
			if (attributeModel.getNestedEntityModel() != null) {
				removeGeneratedColumnsRecursive(attributeModel.getNestedEntityModel().getAttributeModels());
			}
		}
	}

	/**
	 * Sets the visibility of a column. This can only be used to show/hide columns
	 * that would show up in the table based on the entity model
	 *
	 * @param propertyId the ID of the column.
	 * @param visible    whether the column must be visible
	 */
	public void setColumnVisible(Object propertyId, boolean visible) {
//		 Object[] visibleCols = getVisibleColumns();
//		 List<Object> temp = Arrays.stream(visibleCols).filter(c ->
//		 !c.equals(propertyId)).collect(Collectors.toList());
//		 boolean alreadyVisible = Arrays.stream(visibleCols).anyMatch(c ->
//		 c.equals(propertyId));
//		
//		 // add column if not already visible
//		 if (!alreadyVisible || visible) {
//		 temp.add(propertyId);
//		 }
//		 setVisibleColumns(temp.toArray(new Object[0]));

		getColumn((String) propertyId).setHidden(!visible);
	}

	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}

	public void setUpdateTableCaption(boolean updateTableCaption) {
		this.updateTableCaption = updateTableCaption;
	}

	/**
	 * Updates the table caption in response to a change of the data set
	 */
	@SuppressWarnings("unchecked")
	public void updateTableCaption() {
		if (updateTableCaption) {
			int size = 0;
			DataProvider<?, ?> dp = getDataCommunicator().getDataProvider();
			if (dp instanceof ListDataProvider) {
				size = ((ListDataProvider<T>) dp).getItems().size();
			} else {
				size = ((BaseDataProvider<ID, T>) dp).getSize();
			}
			setCaption(entityModel.getDisplayNamePlural() + " "
					+ messageService.getMessage("ocs.showing.results", VaadinUtils.getLocale(), size));
		}
	}

}
