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
package com.ocs.dynamo.ui.composite.grid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.domain.model.FieldFactoryContext;
import com.ocs.dynamo.domain.model.impl.FieldFactoryImpl;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.component.InternalLinkField;
import com.ocs.dynamo.ui.component.URLField;
import com.ocs.dynamo.ui.composite.type.GridEditMode;
import com.ocs.dynamo.ui.provider.BaseDataProvider;
import com.ocs.dynamo.ui.utils.FormatUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.data.BeanPropertySet;
import com.vaadin.data.Binder;
import com.vaadin.data.Binder.BindingBuilder;
import com.vaadin.data.Converter;
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
 * A Grid that bases its columns on the meta model of an entity
 * 
 * @author bas.rutten
 * @param <ID> type of the primary key
 * @param <T> type of the entity
 */
public class ModelBasedGrid<ID extends Serializable, T extends AbstractEntity<ID>> extends Grid<T> {

	private static final long serialVersionUID = 6946260934644731038L;

	/**
	 * Custom currency symbol to be used for this grid
	 */
	private String currencySymbol;

	/**
	 * The entity model of the entities to display in the grid
	 */
	private EntityModel<T> entityModel;

	/***
	 * Indicate whether to update the caption with the number of items in the grid
	 */
	private boolean updateCaption = true;

	/**
	 * Whether the grid is editable
	 */
	private boolean editable;

	/**
	 * The edit mode (row by row or all rows at once)
	 */
	private GridEditMode gridEditMode;

	/**
	 * The message service
	 */
	private MessageService messageService;

	/**
	 * The field factory
	 */
	private FieldFactory fieldFactory;

	/**
	 * Constructor
	 * 
	 * @param dataProvider   data provider
	 * @param model          the entity model
	 * @param editable       whether a single row is editable
	 * @param fullGridEditor whether the full grid at once is editable
	 */
	public ModelBasedGrid(DataProvider<T, SerializablePredicate<T>> dataProvider, EntityModel<T> model,
			boolean editable, GridEditMode gridEditMode) {
		setDataProvider(dataProvider);
		this.editable = editable;
		this.gridEditMode = gridEditMode;
		this.entityModel = model;
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		this.fieldFactory = FieldFactory.getInstance();

		// we need to pre-populate the grid with the available properties
		PropertySet<T> ps = BeanPropertySet.get(model.getEntityClass(), true,
				new PropertyFilterDefinition(3, new ArrayList<>()));
		setPropertySet(ps);
		getEditor().setEnabled(editable && GridEditMode.SINGLE_ROW.equals(gridEditMode));

		setSizeFull();
		setColumnReorderingAllowed(true);
		setSelectionMode(SelectionMode.SINGLE);

		generateColumns(model);

	}

	/**
	 * Adds a column to the grid
	 * 
	 * @param attributeModel the attribute model on which to base the column
	 */
	private void addColumn(final AttributeModel am) {
		if (am.isVisibleInGrid()) {
			Column<T, ?> column;
			if (am.isUrl()) {
				// URL field
				column = addColumn(
						t -> new URLField(new TextField("", ClassUtils.getFieldValueAsString(t, am.getPath(), "")), am,
								editable && GridEditMode.SIMULTANEOUS.equals(gridEditMode)),
						new ComponentRenderer());
			} else if (am.isNavigable() && AttributeType.MASTER.equals(am.getAttributeType())) {
				// generate internal link field
				column = addColumn(t -> generateInternalLinkField(am, ClassUtils.getFieldValue(t, am.getPath())),
						new ComponentRenderer());
			} else {
				if (editable && GridEditMode.SIMULTANEOUS.equals(gridEditMode)
						&& EditableType.EDITABLE.equals(am.getEditableType())) {
					// edit all columns at once
					column = addColumn(t -> {
						AbstractComponent comp = constructCustomField(entityModel, am);
						if (comp == null) {
							FieldFactoryContext ctx = FieldFactoryContext.create().setAttributeModel(am)
									.setViewMode(true);
							comp = fieldFactory.constructField(ctx);
						}
						comp.setSizeFull();

						// delegate the binding to the enveloping component
						BindingBuilder<T, ?> builder = doBind(t, (AbstractComponent) comp);

						FieldFactoryImpl.addConvertersAndValidators(builder, am, constructCustomConverter(am));
						builder.bind(am.getPath());

						postProcessComponent(am, comp);

						return (AbstractComponent) comp;
					}, new ComponentRenderer());
				} else {
					column = addColumn(t -> FormatUtils.extractAndFormat(this, am, t));
				}
			}

			// edit row-by-row, create inline edit component
			if (editable && GridEditMode.SINGLE_ROW.equals(gridEditMode)) {
				if (EditableType.EDITABLE.equals(am.getEditableType())) {
					Binder<T> binder = getEditor().getBinder();
					FieldFactoryContext context = FieldFactoryContext.create().setAttributeModel(am).setViewMode(false);
					AbstractComponent comp = fieldFactory.constructField(context);
					if (comp != null) {
						comp.setSizeFull();
						Binder.BindingBuilder<T, ?> builder = binder.forField((HasValue<?>) comp);
						FieldFactoryImpl.addConvertersAndValidators(builder, am, null);
						column.setEditorBinding(builder.bind(am.getPath()));
					}
				}
			}
			column.setCaption(am.getDisplayName()).setSortable(am.isSortable()).setId(am.getPath())
					.setSortProperty(am.getActualSortPath())
					.setStyleGenerator(item -> am.isNumerical() ? "v-align-right" : "");
		}
	}

	/**
	 * Callback method for constructing a custom field
	 * 
	 * @param entityModel    the entity model of the main entity
	 * @param attributeModel the attribute model to base the field on
	 * @return
	 */
	protected AbstractComponent constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel) {
		return null;
	}

	/**
	 * Callback method for inserting custom converter
	 * 
	 * @param am
	 * @return
	 */
	protected Converter<String, ?> constructCustomConverter(AttributeModel am) {
		return null;
	}

	/**
	 * Callback method for components that incorporate this grid component but do
	 * the binding themselves
	 * 
	 * @param t     the entity
	 * @param field the field to bind
	 * @return
	 */
	protected BindingBuilder<T, ?> doBind(T t, AbstractComponent field) {
		return null;
	}

	/**
	 * Generates the columns of the grid based on the entity model
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
	 * Generates the columns of the grid based on a select number of attribute
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
		return new InternalLinkField<>(attributeModel, null, (S) value);
	}

	public String getCurrencySymbol() {
		return currencySymbol;
	}

	public MessageService getMessageService() {
		return messageService;
	}

	public boolean isUpdateCaption() {
		return updateCaption;
	}

	/**
	 * Sets the visibility of a column. This can only be used to show/hide columns
	 * that would show up in the grid based on the entity model
	 *
	 * @param propertyId the ID of the column.
	 * @param visible    whether the column must be visible
	 */
	public void setColumnVisible(Object propertyId, boolean visible) {
		getColumn((String) propertyId).setHidden(!visible);
	}

	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}

	public void setUpdateCaption(boolean updateCaption) {
		this.updateCaption = updateCaption;
	}

	/**
	 * Updates the grid caption in response to a change of the data set
	 */
	@SuppressWarnings("unchecked")
	public void updateCaption() {
		if (updateCaption) {
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

	public void updateCaption(int size) {
		if (updateCaption) {
			setCaption(entityModel.getDisplayNamePlural() + " "
					+ messageService.getMessage("ocs.showing.results", VaadinUtils.getLocale(), size));
		}
	}

	protected void postProcessComponent(AttributeModel am, AbstractComponent comp) {
		// override in subclass
	}

	public GridEditMode getGridEditMode() {
		return gridEditMode;
	}

}
