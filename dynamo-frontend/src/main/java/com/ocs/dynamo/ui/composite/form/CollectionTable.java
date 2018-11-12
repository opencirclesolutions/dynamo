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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.google.gwt.thirdparty.guava.common.collect.Sets;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.vaadin.data.HasValue;
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
@SuppressWarnings("serial")
public class CollectionTable<T extends Serializable> extends CustomField<Collection<T>> implements SignalsParent {

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
	private ListDataProvider<T> provider;

	/**
	 * The grid for displaying the actual items
	 */
	private final Grid<T> grid;
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

	private Supplier<T> createEntitySupplier;

	/**
	 * Constructor
	 *
	 * @param viewMode    whether to display the component in view (read-only) mode
	 * @param formOptions FormOptions parameter object that can be used to govern
	 *                    how the component behaves
	 */
	public CollectionTable(final AttributeModel attributeModel, final boolean viewMode, final FormOptions formOptions) {
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		this.viewMode = viewMode;
		this.formOptions = formOptions;
		this.attributeModel = attributeModel;
		this.provider = new ListDataProvider<>(new ArrayList<>());
		grid = new Grid<T>("", provider);
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

			T t = createEntitySupplier.get();
			provider.getItems().add(t);
			grid.setDataProvider(provider);

			if (receiver != null) {
				receiver.signalDetailsComponentValid(CollectionTable.this, VaadinUtils.allFixedTableFieldsValid(grid));
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

	public void setItems(Collection<T> items) {

		List<T> list = new ArrayList<>();
		list.addAll(items);

		if (provider != null) {
			provider.getItems().clear();
			provider.getItems().addAll(list);
			provider.refreshAll();
		}
		// clear the selection
		setSelectedItem(null);
	}

	/**
	 * Constructs the column that holds the "remove" button
	 */
	private void constructRemoveColumn() {
		// add a remove button directly in the table
		if (!viewMode && formOptions.isShowRemoveButton()) {
			final String removeMsg = messageService.getMessage("ocs.detail.remove", VaadinUtils.getLocale());
			grid.addComponentColumn((ValueProvider<T, Component>) t -> {
				Button remove = new Button(removeMsg);
				remove.setIcon(VaadinIcons.TRASH);
				remove.addClickListener(event -> {
					provider.getItems().remove(t);
					provider.refreshAll();
					// callback method so the entity can be removed from its
					// parent
					// removeEntity((T) t);
					if (receiver != null) {
						receiver.signalDetailsComponentValid(CollectionTable.this,
								VaadinUtils.allFixedTableFieldsValid(grid));
					}
				});
				return remove;
			});
		}
	}

	/**
	 * Extracts the values from the table and returns them as a Set
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Set<T> extractValues() {
		final Set<T> set = new HashSet<>();
		// TODO:
		return set;
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

	public Grid<T> getGrid() {
		return grid;
	}

	/**
	 * Constructs the actual component
	 */
	@Override
	protected Component initContent() {
		// set up a very basic table with one column
		// Binder<T> binder = grid.getEditor().getBinder();

		Column<T, TextField> column = grid.addColumn(t -> {
			TextField tf = new TextField("", t.toString());
			tf.addValueChangeListener(e -> {
				provider.getItems().clear();
				Iterator<Component> it = grid.iterator();
				while (it.hasNext()) {
					Component next = it.next();
					if (next instanceof TextField) {
						HasValue<T> hasValue = (HasValue<T>) next;
						provider.getItems().add(hasValue.getValue());
					}
				}
				provider.refreshAll();
			});

			return tf;
		}, new ComponentRenderer());

		column.setCaption(messageService.getMessage("ocs.value", VaadinUtils.getLocale()));

		// table.setColumnHeader(VALUE, messageService.getMessage("ocs.value",
		// VaadinUtils.getLocale()));

		grid.getEditor().setEnabled(!isViewMode());
		grid.setSelectionMode(SelectionMode.SINGLE);

		// table.setPageLength(pageLength);
		grid.setSizeFull();
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
//		grid.addSelectionListener(event -> {
//			selectedItem = grid.getSelectedItems()
//			onSelect(grid.getValue());
//		});

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
		provider.getItems().addAll(value);
		grid.setDataProvider(provider);
	}

	@Override
	public boolean validateAllFields() {
//		boolean error = false;
//		Iterator<Component> component = grid.iterator();
//		while (component.hasNext()) {add
//			Component next = component.next();
//			if (next instanceof AbstractField) {
//				try {
//					((AbstractField<?>) next).validate();
//					((AbstractField<?>) next).setComponentError(null);
//				} catch (InvalidValueException ex) {
//					error = true;
//					((AbstractField<?>) next).setComponentError(new UserError(ex.getLocalizedMessage()));
//				}
//			}
//		}
//		return error;

		return false;
	}

	@Override
	public Collection<T> getValue() {
		return (Collection<T>) convertToCorrectCollection(provider.getItems());
	}

	public Supplier<T> getCreateEntitySupplier() {
		return createEntitySupplier;
	}

	public void setCreateEntitySupplier(Supplier<T> createEntitySupplier) {
		this.createEntitySupplier = createEntitySupplier;
	}

	@SuppressWarnings("unchecked")
	protected Object convertToCorrectCollection(Object value) {
		if (value == null) {
			return null;
		} else if (Set.class.isAssignableFrom(attributeModel.getType())) {
			Collection<T> col = (Collection<T>) value;
			return Sets.newHashSet(col);
		} else if (List.class.isAssignableFrom(attributeModel.getType())) {
			Collection<T> col = (Collection<T>) value;
			return Lists.newArrayList(col);
		} else {
			return value;
		}
	}

}
