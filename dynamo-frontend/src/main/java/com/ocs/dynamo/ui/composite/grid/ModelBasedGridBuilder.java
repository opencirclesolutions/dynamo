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
import com.ocs.dynamo.ui.SharedProvider;
import com.ocs.dynamo.ui.component.InternalLinkField;
import com.ocs.dynamo.ui.component.URLField;
import com.ocs.dynamo.ui.composite.type.GridEditMode;
import com.ocs.dynamo.ui.utils.GridFormatUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * A class tasked with adding all relevant columns to a grid. This was
 * introduced to reduce code duplication when creating ModelBasedGrid and
 * ModelBasedSelectionGrid
 * 
 * @author Bas Rutten
 *
 * @param <ID>
 * @param <T>
 */
public abstract class ModelBasedGridBuilder<ID extends Serializable, T extends AbstractEntity<ID>> {

	private Grid<T> grid;

	private EntityModel<T> entityModel;

	private boolean editable;

	private GridEditMode gridEditMode;

	/**
	 * The field factory
	 */
	private FieldFactory fieldFactory;

	/**
	 * The field filter mapping
	 */
	private Map<String, SerializablePredicate<?>> fieldFilters;

	/**
	 * Map from attribute path to "shared provider" that can be shared by all
	 * components in the same column in a grid
	 */
	private Map<String, ListDataProvider<?>> sharedProviders = new HashMap<>();

	/**
	 * Constructor
	 * 
	 * @param grid
	 * @param entityModel
	 * @param fieldFilters
	 * @param editable
	 * @param gridEditMode
	 */
	public ModelBasedGridBuilder(Grid<T> grid, EntityModel<T> entityModel,
			Map<String, SerializablePredicate<?>> fieldFilters, boolean editable, GridEditMode gridEditMode) {
		this.grid = grid;
		this.entityModel = entityModel;
		this.editable = editable;
		this.gridEditMode = gridEditMode;
		this.fieldFilters = fieldFilters;
		this.fieldFactory = FieldFactory.getInstance();
	}

	/**
	 * 
	 * @param ams
	 */
	public void generateColumnsRecursive(List<AttributeModel> ams) {
		for (AttributeModel am : ams) {
			addColumn(am);
			if (am.getNestedEntityModel() != null) {
				generateColumnsRecursive(am.getNestedEntityModel().getAttributeModelsSortedForGrid());
			}
		}
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
				column = grid.addComponentColumn(t -> {
					URLField urlField = new URLField(
							new TextField("", ClassUtils.getFieldValueAsString(t, am.getPath(), "")), am,
							editable && GridEditMode.SIMULTANEOUS.equals(gridEditMode));
					urlField.setValue(ClassUtils.getFieldValueAsString(t, am.getPath(), ""));
					return urlField;
				});
			} else if (am.isNavigable() && AttributeType.MASTER.equals(am.getAttributeType())) {
				// generate internal link field
				column = grid.addComponentColumn(
						t -> generateInternalLinkField(am, ClassUtils.getFieldValue(t, am.getPath())));
			} else {
				if (editable && GridEditMode.SIMULTANEOUS.equals(gridEditMode)
						&& EditableType.EDITABLE.equals(am.getEditableType())) {
					// edit all columns at once
					column = grid.addComponentColumn(t -> {
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
						if (builder != null) {
							fieldFactory.addConvertersAndValidators(builder, am, constructCustomConverter(am),
									constructCustomValidator(am), null);
							builder.bind(am.getPath());
						}
						postProcessComponent(t.getId(), am, comp);

						return comp;
					});
				} else {
					column = grid.addColumn(t -> GridFormatUtils.extractAndFormat(am, t, VaadinUtils.getLocale(),
							VaadinUtils.getTimeZoneId(), VaadinUtils.getCurrencySymbol()));
				}
			}

			// edit row-by-row, create in-line edit component using binder
			if (editable && GridEditMode.SINGLE_ROW.equals(gridEditMode)
					&& EditableType.EDITABLE.equals(am.getEditableType())) {
				Binder<T> binder = grid.getEditor().getBinder();
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
					.setKey(am.getPath()).setAutoWidth(true).setResizable(true).setId(am.getPath());
		}

	}

	@SuppressWarnings("unchecked")
	private <ID2 extends Serializable, S extends AbstractEntity<ID2>> InternalLinkField<ID2, S> generateInternalLinkField(
			AttributeModel attributeModel, Object value) {
		return new InternalLinkField<>(attributeModel, null, (S) value);
	}

	/**
	 * Callback method. override in subclasses
	 * 
	 * @param entityModel
	 * @param attributeModel
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
	 * Post process the component. Callback method that can be used from a component
	 * that includes the grid
	 * 
	 * @param am
	 * @param comp
	 */
	protected void postProcessComponent(ID id, AttributeModel am, Component comp) {
		// override in subclass
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

	/**
	 * Callback method for inserting a custom validator
	 * 
	 * @param <V>
	 * @param am
	 * @return
	 */
	protected <V> Validator<V> constructCustomValidator(AttributeModel am) {
		return null;
	}
}
