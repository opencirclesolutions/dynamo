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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.converter.ConverterFactory;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.data.Container;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.data.validator.LongRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.UserError;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * A component for editing a property that is annotated as an @ElementCollection
 *
 * @param <T>
 *            the type of the elements in the table
 * @author bas.rutten @ElementCollection.
 */
@SuppressWarnings("serial")
public class CollectionTable<T extends Serializable> extends CustomField<Collection<T>> implements SignalsParent {

	private static final long serialVersionUID = -1203245694503350276L;

	/**
	 * The property of the "value" column
	 */
	private static final String VALUE = "value";
	/**
	 * The attribute model
	 */
	private final AttributeModel attributeModel;
	/**
	 * The message service
	 */
	private final MessageService messageService;
	/**
	 * The table for displaying the actual items
	 */
	private final Table table;
	/**
	 * Button for adding new items to the table
	 */
	private Button addButton;
	/**
	 * Form options that determine which buttons and functionalities are available
	 */
	private FormOptions formOptions;
	/**
	 * The number of rows to display
	 */
	private int pageLength = SystemPropertyUtils.getDefaultListSelectRows();
	/**
	 * The parent form in which this component is embedded
	 */
	private ModelBasedEditForm<?, ?> parentForm;
	/**
	 * Whether to propagate change events (disabled during construction)
	 */
	private boolean propagateChanges = true;
	/**
	 * the currently selected item in the table
	 */
	private Object selectedItem;
	/**
	 * Whether the table is in view mode. If this is the case, editing is not
	 * allowed
	 */
	private boolean viewMode;

	/**
	 * Constructor
	 *
	 * @param viewMode
	 *            whether to display the component in view (read-only) mode
	 * @param formOptions
	 *            FormOptions parameter object that can be used to govern how the
	 *            component behaves
	 */
	public CollectionTable(final AttributeModel attributeModel, final boolean viewMode, final FormOptions formOptions) {
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		this.viewMode = viewMode;
		this.formOptions = formOptions;
		this.attributeModel = attributeModel;
		table = new Table("");
	}

	/**
	 * Constructs the button that is used for adding new items
	 *
	 * @param buttonBar
	 */
	protected void constructAddButton(final Layout buttonBar) {
		addButton = new Button(messageService.getMessage("ocs.add", VaadinUtils.getLocale()));
		addButton.setIcon(FontAwesome.PLUS);
		addButton.addClickListener(event -> {
			// add a new item then set the validity to false (since an empty
			// item is never allowed)
			table.addItem();
			if (parentForm != null) {
				parentForm.signalDetailsComponentValid(CollectionTable.this, false);
			}
		});
		buttonBar.addComponent(addButton);
	}

	/**
	 * Constructs the button bar
	 *
	 * @param parent
	 *            the parent layout
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
		if (!isViewMode() && formOptions.isShowRemoveButton()) {
			final String removeHeader = messageService.getMessage("ocs.remove", VaadinUtils.getLocale());
			final String removeMsg = messageService.getMessage("ocs.detail.remove", VaadinUtils.getLocale());
			table.addGeneratedColumn(removeHeader, (source, itemId, columnId) -> {
				final Button remove = new Button(removeMsg);
				remove.setIcon(FontAwesome.TRASH);
				remove.addClickListener(event -> {
					table.removeItem(itemId);
					setValue(extractValues());
					setSelectedItem(null);
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
		table.getItemIds().stream().map(o -> table.getItem(o).getItemProperty(VALUE).getValue())
				.filter(Objects::nonNull).forEach(t -> set.add((T) t));
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

	public ModelBasedEditForm<?, ?> getParentForm() {
		return parentForm;
	}

	/**
	 * This method is called to store a reference to the parent form
	 *
	 * @param parentForm
	 */
	private void setParentForm(final ModelBasedEditForm<?, ?> parentForm) {
		this.parentForm = parentForm;
		if (parentForm != null) {
			parentForm.signalDetailsComponentValid(this, VaadinUtils.allFixedTableFieldsValid(table));
		}
	}

	public Object getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(final String selectedItem) {
		this.selectedItem = selectedItem;
	}

	public Table getTable() {
		return table;
	}

	/**
	 * Returns the type of the field (inherited form CustomField)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends Collection<T>> getType() {
		return (Class<Collection<T>>) (Class<?>) Collection.class;
	}

	/**
	 * Constructs the actual component
	 */
	@Override
	protected Component initContent() {
		// set up a very basic table with one column
		table.addContainerProperty(VALUE, attributeModel.getNormalizedType(), null);

		table.setColumnHeader(VALUE, messageService.getMessage("ocs.value", VaadinUtils.getLocale()));

		table.setEditable(!isViewMode());
		table.setMultiSelect(false);

		table.setPageLength(pageLength);
		table.setColumnCollapsingAllowed(false);
		table.setSizeFull();
		table.setTableFieldFactory(new DefaultFieldFactory() {

			@Override
			public Field<?> createField(final Container container, final Object itemId, final Object propertyId,
					final Component uiContext) {

				final Field<?> f = super.createField(container, itemId, propertyId, uiContext);
				if (f instanceof TextField) {
					final TextField tf = (TextField) f;
					tf.setNullRepresentation("");
					tf.setSizeFull();
					tf.setConverter(ConverterFactory.createConverterFor(attributeModel.getNormalizedType(),
							attributeModel, SystemPropertyUtils.useThousandsGroupingInEditMode()));
					// there is only one property per record so it has to be
					// required
					tf.setRequired(true);
					tf.setRequiredError("may not be null");
				}

				// add a validator that checks for the maximum length
				if (attributeModel.getMaxLength() != null) {
					f.addValidator(new StringLengthValidator(messageService.getMessage("ocs.value.too.long",
							VaadinUtils.getLocale(), attributeModel.getMaxLength()), 0, attributeModel.getMaxLength(),
							true));
				}

				// add a validator that checks for the minimum length
				if (attributeModel.getMinLength() != null) {
					f.addValidator(new StringLengthValidator(
							messageService.getMessage("ocs.value.too.short", VaadinUtils.getLocale(),
									attributeModel.getMinLength()),
							attributeModel.getMinLength(), Integer.MAX_VALUE, true));
				}

				if (attributeModel.getMinValue() != null) {
					if (NumberUtils.isInteger(attributeModel.getNormalizedType())) {
						f.addValidator(
								new IntegerRangeValidator(
										messageService.getMessage("ocs.value.too.low", VaadinUtils.getLocale(),
												attributeModel.getMinValue()),
										attributeModel.getMinValue().intValue(), null));
					} else if (NumberUtils.isLong(attributeModel.getNormalizedType())) {
						f.addValidator(new LongRangeValidator(messageService.getMessage("ocs.value.too.low",
								VaadinUtils.getLocale(), attributeModel.getMinValue()), attributeModel.getMinValue(),
								null));
					}
				}

				if (attributeModel.getMaxValue() != null) {
					if (NumberUtils.isInteger(attributeModel.getNormalizedType())) {
						f.addValidator(
								new IntegerRangeValidator(
										messageService.getMessage("ocs.value.too.high", VaadinUtils.getLocale(),
												attributeModel.getMaxValue()),
										null, attributeModel.getMaxValue().intValue()));
					} else if (NumberUtils.isLong(attributeModel.getNormalizedType())) {
						f.addValidator(new LongRangeValidator(messageService.getMessage("ocs.value.too.high",
								VaadinUtils.getLocale(), attributeModel.getMaxValue()), null,
								attributeModel.getMaxValue()));
					}
				}

				// value change listener that makes sure the validity of the
				// parent form is correctly set
				f.addValueChangeListener(event -> {
					if (propagateChanges) {
						propagateChanges = false;
						setValue(extractValues());
						parentForm.signalDetailsComponentValid(CollectionTable.this,
								VaadinUtils.allFixedTableFieldsValid(table));
						propagateChanges = true;
					}
				});
				return f;
			}
		});

		// add a change listener (to make sure the buttons are correctly
		// enabled/disabled)
		table.addValueChangeListener(event -> {
			selectedItem = table.getValue();
			onSelect(table.getValue());
		});

		// add a remove button directly in the table
		constructRemoveColumn();

		final VerticalLayout layout = new DefaultVerticalLayout(false, true);
		layout.addComponent(table);

		// add the buttons
		constructButtonBar(layout);

		// set the reference to the parent so the status of the save button can
		// be set correctly
		final ModelBasedEditForm<?, ?> parent = VaadinUtils.getParentOfClass(this, ModelBasedEditForm.class);
		setParentForm(parent);

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
	@SuppressWarnings("unchecked")
	protected void setInternalValue(final Collection<T> newValue) {
		if (propagateChanges && table != null) {
			table.addContainerProperty(VALUE, attributeModel.getNormalizedType(), null);

			// simply cleaning the container does not work since Vaadin keeps a
			// reference to a selected item
			// that cannot be removed - so instead unfortunately we have to
			// recreate the container
			final IndexedContainer containerDataSource = new IndexedContainer();
			containerDataSource.addContainerProperty(VALUE, attributeModel.getNormalizedType(), null);

			if (newValue != null) {
				for (final T t : newValue) {
					final Object item = containerDataSource.addItem();
					containerDataSource.getItem(item).getItemProperty(VALUE).setValue(t);
				}
			}
			table.setContainerDataSource(containerDataSource);

			if (table.removeGeneratedColumn(messageService.getMessage("ocs.remove", VaadinUtils.getLocale()))) {
				constructRemoveColumn();
			}
		}
		super.setInternalValue(newValue);
	}

	@Override
	public boolean validateAllFields() {
		boolean error = false;
		Iterator<Component> component = table.iterator();
		while (component.hasNext()) {
			Component next = component.next();
			if (next instanceof AbstractField) {
				try {
					((AbstractField<?>) next).validate();
					((AbstractField<?>) next).setComponentError(null);
				} catch (InvalidValueException ex) {
					error = true;
					((AbstractField<?>) next).setComponentError(new UserError(ex.getLocalizedMessage()));
				}
			}
		}
		return error;
	}

}
