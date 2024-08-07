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

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.UIHelper;
import com.ocs.dynamo.ui.composite.ComponentContext;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.vaadin.componentfactory.selectiongrid.SelectionGrid;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * A grid component that allows the user to select multiple items by
 * shift-clicking or ctrl-clicking
 *
 * @author BasRutten
 *
 * @param <ID>
 * @param <T>
 */
public class ModelBasedSelectionGrid<ID extends Serializable, T extends AbstractEntity<ID>> extends SelectionGrid<T> {

	private static final long serialVersionUID = 6946260934644731038L;

	private boolean built;

	@Getter
	private ComponentContext<ID, T> componentContext;

	@Getter
	private FormOptions formOptions;

	/**
	 * Custom currency symbol to be used for this grid
	 */
	@Getter
	private String currencySymbol;

	private EntityModel<T> entityModel;

	private Map<String, SerializablePredicate<?>> fieldFilters;

	@Getter
	private MessageService messageService;

	/**
	 *
	 * @param dataProvider
	 * @param model
	 * @param fieldFilters
	 * @param formOptions
	 * @param componentContext
	 */
	public ModelBasedSelectionGrid(DataProvider<T, SerializablePredicate<T>> dataProvider, EntityModel<T> model,
			Map<String, SerializablePredicate<?>> fieldFilters, FormOptions formOptions,
			ComponentContext<ID, T> componentContext) {
		setDataProvider(dataProvider);
		// https://vaadin.com/api/platform/23.0.10/deprecated-list.html
//		setItems((Stream<T>) dataProvider);

		this.entityModel = model;
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		this.formOptions = formOptions;
		this.fieldFilters = fieldFilters;
		this.componentContext = componentContext;

		addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);

		setSizeFull();
		setColumnReorderingAllowed(true);
		setSelectionMode(SelectionMode.SINGLE);

		Binder<T> binder = new BeanValidationBinder<>(entityModel.getEntityClass());
		getEditor().setBinder(binder);
		getEditor().setBuffered(false);

	}

	public void build() {
		if (!built) {
			ModelBasedGridBuilder<ID, T> gridBuilder = new ModelBasedGridBuilder<ID, T>(this, entityModel, fieldFilters,
					formOptions, componentContext) {

				@Override
				protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel) {
					return ModelBasedSelectionGrid.this.constructCustomField(entityModel, attributeModel);
				}

				@Override
				protected BindingBuilder<T, ?> doBind(T t, Component field, String attributeName) {
					return ModelBasedSelectionGrid.this.doBind(t, field, attributeName);
				}

				@Override
				protected void postProcessComponent(ID id, AttributeModel am, Component comp) {
					ModelBasedSelectionGrid.this.postProcessComponent(id, am, comp);
				}

			};

			gridBuilder.addColumns(entityModel.getAttributeModelsSortedForGrid());

			addSortListener(event -> {
				UIHelper helper = ServiceLocatorFactory.getServiceLocator().getService(UIHelper.class);
				if (helper != null && formOptions.isPreserveSortOrders()) {
					List<SortOrder<?>> collect = SortOrderUtil.restoreSortOrder(event.getSortOrder());
					helper.storeSortOrders(collect);
				}
			});
			built = true;
		}
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

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		build();
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

}
