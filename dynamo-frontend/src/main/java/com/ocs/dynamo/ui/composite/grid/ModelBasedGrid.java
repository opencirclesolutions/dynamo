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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.domain.model.FieldFactoryContext;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.SharedProvider;
import com.ocs.dynamo.ui.component.InternalLinkField;
import com.ocs.dynamo.ui.component.URLField;
import com.ocs.dynamo.ui.composite.type.GridEditMode;
import com.ocs.dynamo.ui.utils.FormatUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * A Grid that bases its columns on the meta model of an entity
 * 
 * @author bas.rutten
 * @param <ID> type of the primary key
 * @param <T>  type of the entity
 */
public class ModelBasedGrid<ID extends Serializable, T extends AbstractEntity<ID>> extends Grid<T> {

	private static final long serialVersionUID = 6946260934644731038L;

	/**
	 * Custom currency symbol to be used for this grid
	 */
	private String currencySymbol;

	/**
	 * Whether the grid is editable
	 */
	private boolean editable;

	/**
	 * The entity model of the entities to display in the grid
	 */
	private EntityModel<T> entityModel;

	/**
	 * The field factory
	 */
	private FieldFactory fieldFactory;

	/**
	 * The field filter mapping
	 */
	private Map<String, SerializablePredicate<?>> fieldFilters;

	/**
	 * The edit mode (row by row or all rows at once)
	 */
	private GridEditMode gridEditMode;

	/**
	 * The message service
	 */
	private MessageService messageService;

	/**
	 * Map from attribute path to "shared provider" that can be shared by all
	 * components in the same column in a grid
	 */
	private Map<String, ListDataProvider<?>> sharedProviders = new HashMap<>();

	/**
	 * Constructor
	 * 
	 * @param dataProvider the data provider
	 * @param model        the entity model of the entities to display
	 * @param fieldFilters the field filters
	 * @param editable     whether the grid is editable
	 * @param gridEditMode
	 */
	public ModelBasedGrid(DataProvider<T, SerializablePredicate<T>> dataProvider, EntityModel<T> model,
			Map<String, SerializablePredicate<?>> fieldFilters, boolean editable, GridEditMode gridEditMode) {
		setDataProvider(dataProvider);
		this.editable = editable;
		this.gridEditMode = gridEditMode;
		this.entityModel = model;
		this.fieldFilters = fieldFilters;
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		this.fieldFactory = FieldFactory.getInstance();
		addThemeVariants(GridVariant.LUMO_COMPACT);

		setSizeFull();
		setColumnReorderingAllowed(true);
		setSelectionMode(SelectionMode.SINGLE);

		// in Vaadin 14, we explicitly need to set the binder
		Binder<T> binder = new BeanValidationBinder<>(entityModel.getEntityClass());
		getEditor().setBinder(binder);
		getEditor().setBuffered(false);

		generateColumns(model);
	}

	/**
	 * Adds a column to the grid
	 * 
	 * @param am the attribute model on which to base the column
	 */
	private void addColumn(AttributeModel am) {
		if (am.isVisibleInGrid()) {
			Column<T> column;
			if (am.isUrl()) {
				column = addComponentColumn(t -> {
					URLField urlField = new URLField(
							new TextField("", ClassUtils.getFieldValueAsString(t, am.getPath(), "")), am,
							editable && GridEditMode.SIMULTANEOUS.equals(gridEditMode));
					urlField.setValue(ClassUtils.getFieldValueAsString(t, am.getPath(), ""));
					return urlField;
				});
			} else if (am.isNavigable() && AttributeType.MASTER.equals(am.getAttributeType())) {
				// generate internal link field
				column = addComponentColumn(
						t -> generateInternalLinkField(am, ClassUtils.getFieldValue(t, am.getPath())));
			} else {
				if (editable && GridEditMode.SIMULTANEOUS.equals(gridEditMode)
						&& EditableType.EDITABLE.equals(am.getEditableType())) {
					// edit all columns at once
					column = addComponentColumn(t -> {
						Component comp = constructCustomField(entityModel, am);
						if (comp == null) {
							FieldFactoryContext ctx = FieldFactoryContext.create().setAttributeModel(am)
									.setFieldFilters(fieldFilters).setViewMode(false)
									.setSharedProviders(sharedProviders).setEditableGrid(true);
							comp = fieldFactory.constructField(ctx);
						}

						// store shared date provider so it can be used by multiple components
						if (comp instanceof SharedProvider) {
							sharedProviders.put(am.getPath(), ((SharedProvider<?>) comp).getSharedProvider());
						}

						if (comp instanceof HasSize) {
							((HasSize) comp).setSizeFull();
						}

						// delegate the binding to the enveloping component
						BindingBuilder<T, ?> builder = doBind(t, comp, am.getPath());
						fieldFactory.addConvertersAndValidators(builder, am, constructCustomConverter(am),
								constructCustomValidator(am), null);
						builder.bind(am.getPath());
						postProcessComponent(am, comp);
						return comp;
					});
				} else {
					column = addColumn(t -> FormatUtils.extractAndFormat(this, am, t));
				}
			}

			// edit row-by-row, create in-line edit component using binder
			if (editable && GridEditMode.SINGLE_ROW.equals(gridEditMode)
					&& EditableType.EDITABLE.equals(am.getEditableType())) {
				Binder<T> binder = getEditor().getBinder();
				FieldFactoryContext context = FieldFactoryContext.create().setAttributeModel(am).setViewMode(false)
						.setFieldFilters(fieldFilters).setEditableGrid(true);
				Component comp = fieldFactory.constructField(context);
				if (comp != null) {
					Binder.BindingBuilder<T, ?> builder = binder.forField((HasValue<?, ?>) comp);
					if (am.isRequired()) {
						builder.asRequired();
					}
					fieldFactory.addConvertersAndValidators(builder, am, null, null, null);
					builder.bind(am.getPath());
				}
				column.setEditorComponent(comp);
			}

			column.setHeader(am.getDisplayName(VaadinUtils.getLocale())).setSortProperty(am.getActualSortPath())
					.setSortable(am.isSortable()).setClassNameGenerator(item -> am.isNumerical() ? "v-align-right" : "")
					.setKey(am.getPath()).setAutoWidth(true).setId(am.getPath());
		}

	}

	/**
	 * Callback method for inserting custom converter
	 * 
	 * @param am the attribute model for the field for which to add a converter
	 * @return
	 */
	protected <U, V> Converter<U, V> constructCustomConverter(AttributeModel am) {
		return null;
	}

	protected <V> Validator<V> constructCustomValidator(AttributeModel am) {
		return null;
	}

	/**
	 * Callback method for constructing a custom field
	 * 
	 * @param entityModel    the entity model of the main entity
	 * @param attributeModel the attribute model to base the field on
	 * @return
	 */
	protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel) {
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
	protected BindingBuilder<T, ?> doBind(T t, Component field, String attributeName) {
		return null;
	}

	/**
	 * Generates the columns of the grid based on the entity model
	 *
	 * @param container the container
	 * @param model     the entity model
	 */
	protected void generateColumns(EntityModel<T> model) {
		generateColumnsRecursive(model.getAttributeModels());
	}

	/**
	 * Recursively generate columns for the provided attribute models
	 * 
	 * @param ams the attribute models
	 */
	private void generateColumnsRecursive(List<AttributeModel> ams) {
		for (AttributeModel am : ams) {
			addColumn(am);
			if (am.getNestedEntityModel() != null) {
				generateColumnsRecursive(am.getNestedEntityModel().getAttributeModels());
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

	public GridEditMode getGridEditMode() {
		return gridEditMode;
	}

	public MessageService getMessageService() {
		return messageService;
	}

	/**
	 * Post process the component. Callback method that can be used from a component
	 * that includes the grid
	 * 
	 * @param am
	 * @param comp
	 */
	protected void postProcessComponent(AttributeModel am, Component comp) {
		// override in subclass
	}

	/**
	 * Sets the visibility of a column. This can only be used to show/hide columns
	 * that would show up in the grid based on the entity model
	 *
	 * @param propertyId the ID of the column.
	 * @param visible    whether the column must be visible
	 */
	public void setColumnVisible(String propertyId, boolean visible) {
		getColumnByKey(propertyId).setVisible(visible);
	}

	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}

}
