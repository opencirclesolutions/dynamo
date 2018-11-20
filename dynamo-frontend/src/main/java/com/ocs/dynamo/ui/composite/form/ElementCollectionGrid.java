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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.utils.ConvertUtil;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.Binder;
import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ListDataProvider;
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
 * A component for editing a property that is annotated as an @ElementCollection
 *
 * @param <T> the type of the elements in the table
 * @author bas.rutten @ElementCollection.
 */
public class ElementCollectionGrid<T extends Serializable> extends CustomField<Collection<T>> implements SignalsParent {

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
	 * The parent form in which this component is embedded
	 */
	private ReceivesSignal receiver;

	/**
	 * Whether to propagate change events (disabled during construction)
	 */
	private boolean propagateChanges = true;

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

	private Map<ValueHolder<T>, Binder<ValueHolder<T>>> binders = new HashMap<>();

	/**
	 * Constructor
	 * 
	 * @param attributeModel the attribute model
	 * @param viewMode       whether the component is in view mode
	 * @param formOptions
	 */
	public ElementCollectionGrid(final AttributeModel attributeModel, final boolean viewMode,
			final FormOptions formOptions) {
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		this.viewMode = viewMode;
		this.formOptions = formOptions;
		this.attributeModel = attributeModel;
		this.provider = new ListDataProvider<>(new ArrayList<>());

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

			Binder<ValueHolder<T>> binder = new BeanValidationBinder(ValueHolder.class);
			binder.setBean(vh);
			binders.put(vh, binder);
			provider.refreshAll();

			if (receiver != null) {
				receiver.signalDetailsComponentValid(ElementCollectionGrid.this,
						VaadinUtils.allFixedTableFieldsValid(grid));
			}

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
			final String removeMsg = messageService.getMessage("ocs.detail.remove", VaadinUtils.getLocale());
			grid.addComponentColumn((ValueProvider<ValueHolder<T>, Component>) t -> {
				Button remove = new Button(removeMsg);
				remove.setIcon(VaadinIcons.TRASH);
				remove.addClickListener(event -> {
					provider.getItems().remove(t);
					provider.refreshAll();
					// callback method so the entity can be removed from its
					// parent
					// removeEntity((T) t);
					if (receiver != null) {
						receiver.signalDetailsComponentValid(ElementCollectionGrid.this,
								VaadinUtils.allFixedTableFieldsValid(grid));
					}
				});
				return remove;
			});
		}
	}

	public Button getAddButton() {
		return addButton;
	}

	public FormOptions getFormOptions() {
		return formOptions;
	}

	public void setFormOptions(final FormOptions formOptions) {
		this.formOptions = formOptions;
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

	public void setPageLength(final int pageLength) {
		this.pageLength = pageLength;
	}

	public ReceivesSignal getReceiver() {
		return receiver;
	}

	public void setReceiver(ReceivesSignal receiver) {
		this.receiver = receiver;
		if (receiver != null) {
			receiver.signalDetailsComponentValid(this, VaadinUtils.allFixedTableFieldsValid(grid));
		}
	}

	public Object getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(final String selectedItem) {
		this.selectedItem = selectedItem;
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
			binder.forField(tf).asRequired().bind("value");
			return tf;
		}, new ComponentRenderer());

		column.setCaption(messageService.getMessage("ocs.value", VaadinUtils.getLocale()));

		grid.setHeightByRows(pageLength);
		grid.setSelectionMode(SelectionMode.SINGLE);

//		table.setTableFieldFactory(new DefaultFieldFactory() {
//
//			@Override
//			public Field<?> createField(final Container container, final Object itemId, final Object propertyId,
//					final Component uiContext) {
//
//				final Field<?> f = super.createField(container, itemId, propertyId, uiContext);
//				if (f instanceof TextField) {
//					final TextField tf = (TextField) f;
//					tf.setNullRepresentation("");
//					tf.setSizeFull();
//					tf.setConverter(ConverterFactory.createConverterFor(attributeModel.getNormalizedType(),
//							attributeModel, SystemPropertyUtils.useThousandsGroupingInEditMode()));
//					// there is only one property per record so it has to be
//					// required
//					tf.setRequired(true);
//					tf.setRequiredError("may not be null");
//				}
//
//				// add a validator that checks for the maximum length
//				if (attributeModel.getMaxLength() != null) {
//					f.addValidator(new StringLengthValidator(messageService.getMessage("ocs.value.too.long",
//							VaadinUtils.getLocale(), attributeModel.getMaxLength()), 0, attributeModel.getMaxLength(),
//							true));
//				}
//
//				// add a validator that checks for the minimum length
//				if (attributeModel.getMinLength() != null) {
//					f.addValidator(new StringLengthValidator(
//							messageService.getMessage("ocs.value.too.short", VaadinUtils.getLocale(),
//									attributeModel.getMinLength()),
//							attributeModel.getMinLength(), Integer.MAX_VALUE, true));
//				}
//
//				if (attributeModel.getMinValue() != null) {
//					if (NumberUtils.isInteger(attributeModel.getNormalizedType())) {
//						f.addValidator(
//								new IntegerRangeValidator(
//										messageService.getMessage("ocs.value.too.low", VaadinUtils.getLocale(),
//												attributeModel.getMinValue()),
//										attributeModel.getMinValue().intValue(), null));
//					} else if (NumberUtils.isLong(attributeModel.getNormalizedType())) {
//						f.addValidator(new LongRangeValidator(messageService.getMessage("ocs.value.too.low",
//								VaadinUtils.getLocale(), attributeModel.getMinValue()), attributeModel.getMinValue(),
//								null));
//					}
//				}
//
//				if (attributeModel.getMaxValue() != null) {
//					if (NumberUtils.isInteger(attributeModel.getNormalizedType())) {
//						f.addValidator(
//								new IntegerRangeValidator(
//										messageService.getMessage("ocs.value.too.high", VaadinUtils.getLocale(),
//												attributeModel.getMaxValue()),
//										null, attributeModel.getMaxValue().intValue()));
//					} else if (NumberUtils.isLong(attributeModel.getNormalizedType())) {
//						f.addValidator(new LongRangeValidator(messageService.getMessage("ocs.value.too.high",
//								VaadinUtils.getLocale(), attributeModel.getMaxValue()), null,
//								attributeModel.getMaxValue()));
//					}
//				}
//
//				// value change listener that makes sure the validity of the
//				// parent form is correctly set
//				f.addValueChangeListener(event -> {
//					if (propagateChanges) {
//						propagateChanges = false;
//						setValue(extractValues());
//						parentForm.signalDetailsComponentValid(CollectionTable.this,
//								VaadinUtils.allFixedTableFieldsValid(table));
//						propagateChanges = true;
//
//					}
//				});
//				return f;
//			}
//		});

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

		// set the reference to the parent so the status of the save button can
		// be set correctly
		final ModelBasedEditForm<?, ?> receiver = VaadinUtils.getParentOfClass(this, ModelBasedEditForm.class);
		setReceiver(receiver);

		return layout;
	}

	public boolean isViewMode() {
		return viewMode;
	}

	public void setViewMode(final boolean viewMode) {
		this.viewMode = viewMode;
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

	@Override
	protected void doSetValue(Collection<T> value) {
		provider.getItems().clear();
		provider.refreshAll();
		binders.clear();

		for (T t : value) {
			ValueHolder<T> vh = new ValueHolder<T>(t);
			provider.getItems().add(vh);
			Binder<ValueHolder<T>> binder = new BeanValidationBinder(ValueHolder.class);
			binder.setBean(vh);
			binders.put(vh, binder);
		}
	}

	@Override
	public boolean validateAllFields() {
		boolean error = false;
		for (Binder<ValueHolder<T>> binder : binders.values()) {
			error |= !binder.validate().isOk();
		}
		return error;
	}

	@Override
	public Collection<T> getValue() {
		Collection<T> col = provider.getItems().stream().map(vh -> vh.getValue()).collect(Collectors.toList());
		Collection<T> col2 = ConvertUtil.convertCollection(col, attributeModel);
		System.out.println("Selected: " + col2);
		return col2;
	}

}
