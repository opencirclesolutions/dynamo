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
package com.ocs.dynamo.ui.composite.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.CanAssignEntity;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.converter.ConverterFactory;
import com.ocs.dynamo.ui.utils.ConvertUtil;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.Binder;
import com.vaadin.data.Binder.BindingBuilder;
import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ComponentRenderer;

/**
 * A grid for editing a collection of simple values stored in a collection table
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the key of the entity
 * @param <U> the type of the entity on which the collection is stored
 * @param <T> the type of the elements in the collection
 */
public class ElementCollectionGrid<ID extends Serializable, U extends AbstractEntity<ID>, T extends Serializable>
		extends CustomField<Collection<T>> implements SignalsParent, CanAssignEntity<ID, U> {

	private static final long serialVersionUID = -1203245694503350276L;

	/**
	 * The attribute model
	 */
	private final AttributeModel attributeModel;

	/**
	 * The message service
	 */
	private final MessageService messageService;

	/**
	 * The data provider
	 */
	private ListDataProvider<ValueHolder<T>> provider;

	/**
	 * The grid for displaying the actual items
	 */
	private Grid<ValueHolder<T>> grid;

	/**
	 * Button for adding new items to the table
	 */
	private Button addButton;

	/**
	 * 
	 * Form options that determine which buttons and functionalities are* available
	 */
	private FormOptions formOptions;

	/**
	 * 
	 * The number of rows to display
	 */
	private int pageLength = SystemPropertyUtils.getDefaultListSelectRows();

	/**
	 * 
	 * the currently selected item in the table
	 */
	private Object selectedItem;

	/**
	 * 
	 * Whether the table is in view mode. If this is the case, editing is not*
	 * allowed
	 */
	private boolean viewMode;

	/**
	 * The entity on to which to store the values. This should not normally be
	 * needed but for some reason the normal binding mechanisms don't work so we
	 * need to set the values ourselves
	 */
	private U entity;

	/**
	 * Map of bindings. Contains one binding for each row
	 */
	private Map<ValueHolder<T>, Binder<ValueHolder<T>>> binders = new HashMap<>();

	/**
	 * Constructor
	 * 
	 * @param attributeModel the attribute model
	 * @param formOptions    the form options that govern how the table behaves
	 */
	public ElementCollectionGrid(AttributeModel attributeModel, FormOptions formOptions) {
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		this.formOptions = formOptions;
		this.attributeModel = attributeModel;
		this.provider = new ListDataProvider<>(new ArrayList<>());
	}

	/**
	 * Assigns the parent entity
	 */
	@Override
	public void assignEntity(U t) {
		this.entity = t;
	}

	/**
	 * Constructs the button that is used for adding new items
	 *
	 * @param buttonBar
	 */
	protected void constructAddButton(final Layout buttonBar) {
		addButton = new Button(messageService.getMessage("ocs.add", VaadinUtils.getLocale()));
		addButton.setIcon(VaadinIcons.PLUS);
		addButton.addClickListener(event -> {

			ValueHolder<T> vh = new ValueHolder<T>(null);
			provider.getItems().add(vh);

			@SuppressWarnings({ "rawtypes", "unchecked" })
			Binder<ValueHolder<T>> binder = new BeanValidationBinder(ValueHolder.class);
			binder.setBean(vh);
			binders.put(vh, binder);
			provider.refreshAll();

		});
		buttonBar.addComponent(addButton);
	}

	/**
	 * Constructs the button bar
	 * 
	 * @param parent the parent layout
	 */
	protected void constructButtonBar(final Layout parent) {
		final Layout buttonBar = new DefaultHorizontalLayout();
		parent.addComponent(buttonBar);

		// button for adding a row
		if (!viewMode && !formOptions.isHideAddButton()) {
			constructAddButton(buttonBar);
		}

		postProcessButtonBar(buttonBar);
	}

	/**
	 * Constructs the column that holds the "remove" button
	 */
	private void constructRemoveColumn() {
		// add a remove button directly in the table
		if (!viewMode && formOptions.isShowRemoveButton()) {
			final String removeMsg = message("ocs.detail.remove");
			grid.addComponentColumn((ValueProvider<ValueHolder<T>, Component>) t -> {
				Button remove = new Button(removeMsg);
				remove.setIcon(VaadinIcons.TRASH);
				remove.addClickListener(event -> {
					provider.getItems().remove(t);
					provider.refreshAll();
				});
				return remove;
			});
		}
	}

	@Override
	protected void doSetValue(Collection<T> value) {
		provider.getItems().clear();
		provider.refreshAll();
		binders.clear();

		for (T t : value) {
			ValueHolder<T> vh = new ValueHolder<T>(t);
			provider.getItems().add(vh);
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Binder<ValueHolder<T>> binder = new BeanValidationBinder(ValueHolder.class);
			binder.setBean(vh);
			binders.put(vh, binder);
		}
	}

	public Button getAddButton() {
		return addButton;
	}

	public U getEntity() {
		return entity;
	}

	public FormOptions getFormOptions() {
		return formOptions;
	}

	public Integer getMaxLength() {
		return attributeModel.getMaxLength();
	}

	public Long getMaxValue() {
		return attributeModel.getMaxValue();
	}

	public Integer getMinLength() {
		return attributeModel.getMinLength();
	}

	public Long getMinValue() {
		return attributeModel.getMinValue();
	}

	public int getPageLength() {
		return pageLength;
	}

	public Object getSelectedItem() {
		return selectedItem;
	}

	@Override
	public Collection<T> getValue() {
		Collection<T> col = provider.getItems().stream().map(vh -> vh.getValue()).collect(Collectors.toList());
		Collection<T> converted = ConvertUtil.convertCollection(col, attributeModel);
		if (entity != null) {
			ClassUtils.setFieldValue(entity, attributeModel.getPath(), converted);
		}
		return converted;
	}

	/**
	 * Constructs the actual component
	 */
	@Override
	protected Component initContent() {

		grid = new Grid<ValueHolder<T>>("", provider) {

			private static final long serialVersionUID = -4045946516131771973L;

		};

		Column<ValueHolder<T>, TextField> column = grid.addColumn(vh -> {
			TextField tf = new TextField("");
			Binder<ValueHolder<T>> binder = binders.get(vh);

			BindingBuilder<ValueHolder<T>, String> builder = binder.forField(tf);
			builder.withNullRepresentation("");

			if (String.class.equals(attributeModel.getMemberType())) {
				// string length validation
				if (attributeModel.getMaxLength() != null) {
					builder.withValidator(
							new StringLengthValidator(message("ocs.value.too.long", attributeModel.getMaxLength()),
									null, attributeModel.getMaxLength()));
				}
				if (attributeModel.getMinLength() != null) {
					builder.withValidator(
							new StringLengthValidator(message("ocs.value.too.short", attributeModel.getMinLength()),
									attributeModel.getMinLength(), null));
				}
			} else if (NumberUtils.isInteger(attributeModel.getMemberType())) {
				builder.withConverter(ConverterFactory.createIntegerConverter(
						SystemPropertyUtils.useThousandsGroupingInEditMode(), attributeModel.isPercentage()));
			} else if (NumberUtils.isLong(attributeModel.getMemberType())) {
				builder.withConverter(ConverterFactory.createLongConverter(
						SystemPropertyUtils.useThousandsGroupingInEditMode(), attributeModel.isPercentage()));
			}
			builder.asRequired().bind("value");
			return tf;
		}, new ComponentRenderer());

		column.setCaption(messageService.getMessage("ocs.value", VaadinUtils.getLocale()));

		grid.setHeightByRows(pageLength);
		grid.setSelectionMode(SelectionMode.SINGLE);

		// add a change listener (to make sure the buttons are correctly
		// enabled/disabled)
		grid.addSelectionListener(event -> {
			ValueHolder<T> vh = grid.getSelectedItems().iterator().next();
			onSelect(vh.getValue());
		});

		// add a remove button directly in the table
		constructRemoveColumn();

		final VerticalLayout layout = new DefaultVerticalLayout(false, true);
		layout.addComponent(grid);

		// add the buttons
		constructButtonBar(layout);

		return layout;
	}

	public boolean isViewMode() {
		return viewMode;
	}

	private String message(String key, Object... values) {
		return messageService.getMessage(key, VaadinUtils.getLocale(), values);
	}

	/**
	 * Respond to a selection of an item in the table
	 */
	protected void onSelect(final Object selected) {
		// overwrite in subclass if needed
	}

	/**
	 * Add additional buttons to the button bar
	 *
	 * @param buttonBar
	 */
	protected void postProcessButtonBar(final Layout buttonBar) {
		// overwrite in subclass if needed
	}

	public void setEntity(U entity) {
		this.entity = entity;
	}

	public void setFormOptions(final FormOptions formOptions) {
		this.formOptions = formOptions;
	}

	public void setPageLength(final int pageLength) {
		this.pageLength = pageLength;
	}

	public void setSelectedItem(final String selectedItem) {
		this.selectedItem = selectedItem;
	}

	public void setViewMode(final boolean viewMode) {
		this.viewMode = viewMode;
	}

	@Override
	public boolean validateAllFields() {
		boolean error = false;
		for (Binder<ValueHolder<T>> binder : binders.values()) {
			error |= !binder.validate().isOk();
		}
		return error;
	}

	public Grid<ValueHolder<T>> getGrid() {
		return grid;
	}
}
