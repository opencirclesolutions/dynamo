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
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.FieldCreationContext;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.SharedProvider;
import com.ocs.dynamo.ui.component.InternalLinkField;
import com.ocs.dynamo.ui.component.URLField;
import com.ocs.dynamo.ui.composite.ComponentContext;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
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
import com.vaadin.flow.data.binder.Binder.BindingBuilder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * A class tasked with adding all relevant columns to a grid. This was
 * introduced to reduce code duplication when creating ModelBasedGrid and
 * ModelBasedSelectionGrid
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the primary key of the entity
 * @param <T>  the type of the entity
 */
public abstract class ModelBasedGridBuilder<ID extends Serializable, T extends AbstractEntity<ID>> {

	private ComponentContext<ID, T> componentContext;

	private EntityModel<T> entityModel;

	/**
	 * The field factory
	 */
	private FieldFactory fieldFactory;

	/**
	 * The field filter mapping
	 */
	private Map<String, SerializablePredicate<?>> fieldFilters;

	private FormOptions formOptions;

	private Grid<T> grid;

	/**
	 * Map from attribute path to "shared provider" that can be shared by all
	 * components in the same column in a grid
	 */
	private Map<String, DataProvider<?, SerializablePredicate<?>>> sharedProviders = new HashMap<>();

	/**
	 * Constructor
	 * 
	 * @param grid             the grid
	 * @param entityModel      the entity model
	 * @param fieldFilters     the field filters to apply to the separate fields
	 * @param formOptions      the form options
	 * @param componentContext the component context
	 */
	protected ModelBasedGridBuilder(Grid<T> grid, EntityModel<T> entityModel,
			Map<String, SerializablePredicate<?>> fieldFilters, FormOptions formOptions,
			ComponentContext<ID, T> componentContext) {
		this.grid = grid;
		this.entityModel = entityModel;
		this.formOptions = formOptions;
		this.componentContext = componentContext;
		this.fieldFilters = fieldFilters;
		this.fieldFactory = FieldFactory.getInstance();
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
				column = addUrlColumn(am);
			} else if (!componentContext.isPopup() && am.isNavigable()
					&& AttributeType.MASTER.equals(am.getAttributeType())) {
				column = grid.addComponentColumn(
						t -> generateInternalLinkField(am, ClassUtils.getFieldValue(t, am.getPath())));
			} else {
				if (isSimultaneousEditMode(am)) {
					// edit all columns at once
					column = addEditableColumn(am);
				} else {
					column = grid.addColumn(t -> GridFormatUtils.extractAndFormat(am, t, VaadinUtils.getLocale(),
							VaadinUtils.getTimeZoneId(), VaadinUtils.getCurrencySymbol()));
				}
			}

			// edit row-by-row, create in-line edit component using binder
			if (isSingleRowEditMode(am)) {
				setEditorComponent(am, column);
			}

			column.setHeader(am.getDisplayName(VaadinUtils.getLocale())).setSortProperty(am.getActualSortPath())
					.setSortable(am.isSortable()).setClassNameGenerator(item -> am.isNumerical() ? "v-align-right" : "")
					.setKey(am.getPath()).setAutoWidth(true).setResizable(true).setId(am.getPath());
		}

	}

	/**
	 * Indicates whether the attribute is editable using a single row at a time
	 * editor
	 * 
	 * @param am
	 * @return
	 */
	private boolean isSingleRowEditMode(AttributeModel am) {
		return componentContext.isEditable() && GridEditMode.SINGLE_ROW.equals(formOptions.getGridEditMode())
				&& EditableType.EDITABLE.equals(am.getEditableType());
	}

	/**
	 * Creates the editor component for the provided attribute model and column
	 * 
	 * @param am
	 * @param column
	 */
	@SuppressWarnings("unchecked")
	private void setEditorComponent(AttributeModel am, Column<T> column) {
		Binder<T> binder = grid.getEditor().getBinder();
		FieldCreationContext context = FieldCreationContext.create().attributeModel(am).viewMode(false)
				.fieldFilters(fieldFilters).editableGrid(true).build();
		Component comp = fieldFactory.constructField(context);
		if (comp != null) {
			Binder.BindingBuilder<T, ?> builder = binder.forField((HasValue<?, ?>) comp);
			if (am.isRequired()) {
				builder.asRequired();
			}
			fieldFactory.addConvertersAndValidators(builder, am, context, componentContext.findCustomConverter(am),
					componentContext.findCustomValidator(am), null);
			builder.bind(am.getPath());
		}
		column.setEditorComponent(comp);
	}

	private boolean isSimultaneousEditMode(AttributeModel am) {
		return componentContext.isEditable() && GridEditMode.SIMULTANEOUS.equals(formOptions.getGridEditMode())
				&& EditableType.EDITABLE.equals(am.getEditableType());
	}

	@SuppressWarnings("unchecked")
	private Column<T> addEditableColumn(AttributeModel am) {
		return grid.addComponentColumn(t -> {
			Component comp = constructCustomField(entityModel, am);
			FieldCreationContext context = FieldCreationContext.create().attributeModel(am)
					.fieldEntityModel(getFieldEntityModel(am)).fieldFilters(fieldFilters).viewMode(false)
					.sharedProviders(sharedProviders).editableGrid(true).build();

			if (comp == null) {
				comp = fieldFactory.constructField(context);
			}

			// always hide label inside grid
			VaadinUtils.setLabel(comp, "");

			// store shared date provider so it can be used by multiple components
			storeSharedProvider(am, comp);

			if (comp instanceof HasSize) {
				((HasSize) comp).setSizeFull();
			}

			// delegate the binding to the enveloping component
			BindingBuilder<T, ?> builder = doBind(t, comp, am.getPath());
			if (builder != null) {
				fieldFactory.addConvertersAndValidators(builder, am, context, componentContext.findCustomConverter(am),
						componentContext.findCustomValidator(am), null);
				builder.bind(am.getPath());
			}
			postProcessComponent(t.getId(), am, comp);

			return comp;
		});
	}

	@SuppressWarnings("unchecked")
	private void storeSharedProvider(AttributeModel am, Component comp) {
		if (comp instanceof SharedProvider) {
			DataProvider<?, ?> sharedProvider = ((SharedProvider<?>) comp).getSharedProvider();
			if (sharedProvider instanceof ListDataProvider) {
				sharedProviders.put(am.getPath(), (DataProvider<?, SerializablePredicate<?>>) sharedProvider);
			}
		}
	}

	private Column<T> addUrlColumn(AttributeModel am) {
		return grid.addComponentColumn(t -> {
			URLField urlField = new URLField(new TextField("", ClassUtils.getFieldValueAsString(t, am.getPath(), "")),
					am,
					componentContext.isEditable() && GridEditMode.SIMULTANEOUS.equals(formOptions.getGridEditMode()));
			urlField.setValue(ClassUtils.getFieldValueAsString(t, am.getPath(), ""));
			return urlField;
		});
	}

	/**
	 * Adds the columns to the grid based on the attribute models
	 * 
	 * @param attributeModels the attribute models
	 */
	public void addColumns(List<AttributeModel> attributeModels) {
		for (AttributeModel am : attributeModels) {
			addColumn(am);
			if (am.getNestedEntityModel() != null) {
				addColumns(am.getNestedEntityModel().getAttributeModelsSortedForGrid());
			}
		}
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
	 * @param entity the entity
	 * @param field  the field to bind
	 * @return
	 */
	protected BindingBuilder<T, ?> doBind(T entity, Component field, String attributeName) {
		return null;
	}

	@SuppressWarnings("unchecked")
	private <ID2 extends Serializable, S extends AbstractEntity<ID2>> InternalLinkField<ID2, S> generateInternalLinkField(
			AttributeModel attributeModel, Object value) {
		return new InternalLinkField<>(attributeModel, null, (S) value);
	}

	protected EntityModelFactory getEntityModelFactory() {
		return ServiceLocatorFactory.getServiceLocator().getEntityModelFactory();
	}

	protected final EntityModel<?> getFieldEntityModel(AttributeModel attributeModel) {
		String reference = componentContext.getFieldEntityModels().get(attributeModel.getPath());
		return reference == null ? null
				: getEntityModelFactory().getModel(reference, attributeModel.getNormalizedType());
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
}
