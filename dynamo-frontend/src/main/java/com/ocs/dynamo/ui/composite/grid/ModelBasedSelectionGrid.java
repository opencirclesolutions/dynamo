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
import java.util.List;
import java.util.Map;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.UIHelper;
import com.ocs.dynamo.ui.composite.type.GridEditMode;
import com.vaadin.componentfactory.selectiongrid.SelectionGrid;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

public class ModelBasedSelectionGrid<ID extends Serializable, T extends AbstractEntity<ID>> extends SelectionGrid<T> {

	private static final long serialVersionUID = 6946260934644731038L;

	/**
	 * Custom currency symbol to be used for this grid
	 */
	private String currencySymbol;

	/**
	 * The entity model of the entities to display in the grid
	 */
	private EntityModel<T> entityModel;

	/**
	 * The edit mode (row by row or all rows at once)
	 */
	private GridEditMode gridEditMode;

	/**
	 * The message service
	 */
	private MessageService messageService;

	/**
	 * Whether to store sort orders
	 */
	private boolean storeSortOrders;

	/**
	 * Constructor
	 * 
	 * @param dataProvider the data provider
	 * @param model        the entity model of the entities to display
	 * @param fieldFilters the field filters
	 * @param editable     whether the grid is editable
	 * @param gridEditMode
	 */
	public ModelBasedSelectionGrid(DataProvider<T, SerializablePredicate<T>> dataProvider, EntityModel<T> model,
			Map<String, SerializablePredicate<?>> fieldFilters, boolean editable, boolean storeSortOrders,
			GridEditMode gridEditMode) {
		setDataProvider(dataProvider);
		this.gridEditMode = gridEditMode;
		this.entityModel = model;
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		this.storeSortOrders = storeSortOrders;
		addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);

		setSizeFull();
		setColumnReorderingAllowed(true);
		setSelectionMode(SelectionMode.SINGLE);

		// in Vaadin 14, we explicitly need to set the binder
		Binder<T> binder = new BeanValidationBinder<>(entityModel.getEntityClass());
		getEditor().setBinder(binder);
		getEditor().setBuffered(false);

		ModelBasedGridBuilder<ID, T> gridBuilder = new ModelBasedGridBuilder<ID, T>(this, entityModel, fieldFilters,
				editable, gridEditMode) {

			@Override
			protected <U, V> Converter<U, V> constructCustomConverter(AttributeModel am) {
				return ModelBasedSelectionGrid.this.constructCustomConverter(am);
			}

			@Override
			protected <V> Validator<V> constructCustomValidator(AttributeModel am) {
				return ModelBasedSelectionGrid.this.constructCustomValidator(am);
			}

			@Override
			protected void postProcessComponent(ID id, AttributeModel am, Component comp) {
				ModelBasedSelectionGrid.this.postProcessComponent(id, am, comp);
			}

			@Override
			protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel) {
				return ModelBasedSelectionGrid.this.constructCustomField(entityModel, attributeModel);
			}

			@Override
			protected BindingBuilder<T, ?> doBind(T t, Component field, String attributeName) {
				return ModelBasedSelectionGrid.this.doBind(t, field, attributeName);
			}

		};
		gridBuilder.generateColumnsRecursive(model.getAttributeModelsSortedForGrid());

		addSortListener(event -> {
			UIHelper helper = ServiceLocatorFactory.getServiceLocator().getService(UIHelper.class);
			if (helper != null && storeSortOrders) {
				List<SortOrder<?>> collect = SortOrderUtil.restoreSortOrder(entityModel, event.getSortOrder());
				helper.storeSortOrders(collect);
			}
		});
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
	protected void postProcessComponent(ID id, AttributeModel am, Component comp) {
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

	public boolean isStoreSortOrders() {
		return storeSortOrders;
	}

}