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
package com.ocs.dynamo.ui.component;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.CanAssignEntity;
import com.ocs.dynamo.ui.NestedComponent;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.converter.ConverterFactory;
import com.ocs.dynamo.ui.utils.ConvertUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.validator.BigDecimalRangeValidator;
import com.vaadin.flow.data.validator.IntegerRangeValidator;
import com.vaadin.flow.data.validator.LongRangeValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.function.ValueProvider;

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
        extends CustomField<Collection<T>> implements NestedComponent, CanAssignEntity<ID, U> {

    private static final long serialVersionUID = -1203245694503350276L;

    private static final String VALUE_TOO_LONG = "ocs.value.too.long";

    private static final String VALUE_TOO_SHORT = "ocs.value.too.short";

    private static final String VALUE_TOO_LOW = "ocs.value.too.low";

    private static final String VALUE_TOO_HIGH = "ocs.value.too.high";

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
     * Button for adding new items to the grid
     */
    private Button addButton;

    /**
     * 
     * Form options that determine which buttons and functionalities are* available
     */
    private FormOptions formOptions;

    /**
     * 
     * The currently selected item in the grid
     */
    private Object selectedItem;

    /**
     * 
     * Whether the grid is in view mode. If this is the case, editing is not*
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
     * @param formOptions    the form options that govern how the grid behaves
     */
    public ElementCollectionGrid(AttributeModel attributeModel, FormOptions formOptions) {
        this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
        this.formOptions = formOptions;
        this.attributeModel = attributeModel;
        this.provider = new ListDataProvider<>(new ArrayList<>());
        setSizeFull();
        initContent();
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
    protected void constructAddButton(HorizontalLayout buttonBar) {
        addButton = new Button(messageService.getMessage("ocs.add", VaadinUtils.getLocale()));
        addButton.setIcon(VaadinIcon.PLUS.create());
        addButton.addClickListener(event -> {

            ValueHolder<T> vh = new ValueHolder<>(null);
            provider.getItems().add(vh);

            @SuppressWarnings({ "rawtypes", "unchecked" })
            Binder<ValueHolder<T>> binder = new BeanValidationBinder(ValueHolder.class);
            binder.setBean(vh);
            binders.put(vh, binder);
            provider.refreshAll();

        });
        buttonBar.add(addButton);
    }

    /**
     * Constructs the button bar
     * 
     * @param parent the parent layout
     */
    protected void constructButtonBar(VerticalLayout parent) {
        HorizontalLayout buttonBar = new DefaultHorizontalLayout();
        parent.add(buttonBar);

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
        if (!viewMode && formOptions.isShowRemoveButton()) {
            final String removeMsg = message("ocs.detail.remove");
            grid.addComponentColumn((ValueProvider<ValueHolder<T>, Component>) t -> {
                Button remove = new Button();
                remove.setIcon(VaadinIcon.TRASH.create());
                remove.addClickListener(event -> {
                    binders.remove(t);
                    provider.getItems().remove(t);
                    provider.refreshAll();
                });
                return remove;
            }).setHeader(removeMsg).setKey(removeMsg);
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

    public Object getSelectedItem() {
        return selectedItem;
    }

    @Override
    public Collection<T> getValue() {
        Collection<T> col = provider.getItems().stream().map(vh -> vh.getValue()).collect(Collectors.toList());
        Collection<T> converted = ConvertUtils.convertCollection(col, attributeModel);
        if (entity != null) {
            ClassUtils.setFieldValue(entity, attributeModel.getPath(), converted);
        }
        return converted;
    }

    /**
     * Constructs the actual component
     */
    protected void initContent() {

        VerticalLayout main = new VerticalLayout();

        grid = new Grid<>();
        grid.setDataProvider(provider);
        main.add(grid);

        Column<ValueHolder<T>> column = grid.addComponentColumn(vh -> {
            TextField tf = new TextField("");
            Binder<ValueHolder<T>> binder = binders.get(vh);

            BindingBuilder<ValueHolder<T>, String> builder = binder.forField(tf);
            builder.withNullRepresentation("");

            // custom validator since the normal one apparently doesn't work properly here
            // (note: add this before the converter!)
            Validator<String> notEmpty = (String value, ValueContext v) -> {
                if (value == null || "".equals(value)) {
                    return ValidationResult.error(messageService.getMessage("ocs.may.not.be.null", VaadinUtils.getLocale()));
                }
                return ValidationResult.ok();
            };
            builder.asRequired(notEmpty);

            if (String.class.equals(attributeModel.getMemberType())) {
                // string length validation
                if (attributeModel.getMaxLength() != null) {
                    builder.withValidator(new StringLengthValidator(message(VALUE_TOO_LONG, attributeModel.getMaxLength()), null,
                            attributeModel.getMaxLength()));
                }
                if (attributeModel.getMinLength() != null) {
                    builder.withValidator(new StringLengthValidator(message(VALUE_TOO_SHORT, attributeModel.getMinLength()),
                            attributeModel.getMinLength(), null));
                }
            } else if (NumberUtils.isInteger(attributeModel.getMemberType())) {
                BindingBuilder<ValueHolder<T>, Integer> iBuilder = builder.withConverter(ConverterFactory
                        .createIntegerConverter(SystemPropertyUtils.useThousandsGroupingInEditMode(), attributeModel.isPercentage()));
                if (attributeModel.getMaxValue() != null) {
                    iBuilder.withValidator(new IntegerRangeValidator(message(VALUE_TOO_HIGH, attributeModel.getMaxValue()), null,
                            attributeModel.getMaxValue().intValue()));
                }
                if (attributeModel.getMinValue() != null) {
                    iBuilder.withValidator(new IntegerRangeValidator(message(VALUE_TOO_LOW, attributeModel.getMinValue()),
                            attributeModel.getMinValue().intValue(), null));
                }
            } else if (NumberUtils.isLong(attributeModel.getMemberType())) {
                BindingBuilder<ValueHolder<T>, Long> iBuilder = builder.withConverter(ConverterFactory
                        .createLongConverter(SystemPropertyUtils.useThousandsGroupingInEditMode(), attributeModel.isPercentage()));
                if (attributeModel.getMaxValue() != null) {
                    iBuilder.withValidator(new LongRangeValidator(message(VALUE_TOO_HIGH, attributeModel.getMaxValue()), null,
                            attributeModel.getMaxValue()));
                }
                if (attributeModel.getMinValue() != null) {
                    iBuilder.withValidator(new LongRangeValidator(message(VALUE_TOO_LOW, attributeModel.getMinValue()),
                            attributeModel.getMinValue(), null));
                }
            } else if (BigDecimal.class.equals(attributeModel.getMemberType())) {
                BindingBuilder<ValueHolder<T>, BigDecimal> iBuilder = builder.withConverter(ConverterFactory.createBigDecimalConverter(
                        attributeModel.isCurrency(), attributeModel.isPercentage(), SystemPropertyUtils.useThousandsGroupingInEditMode(),
                        attributeModel.getPrecision(), SystemPropertyUtils.getDefaultCurrencySymbol()));
                if (attributeModel.getMaxValue() != null) {
                    iBuilder.withValidator(
                            new BigDecimalRangeValidator(message(VALUE_TOO_HIGH), null, BigDecimal.valueOf(attributeModel.getMaxValue())));
                }
                if (attributeModel.getMinValue() != null) {
                    iBuilder.withValidator(
                            new BigDecimalRangeValidator(message(VALUE_TOO_LOW), BigDecimal.valueOf(attributeModel.getMinValue()), null));
                }
            }
            builder.bind("value");
            tf.setSizeFull();
            return tf;
        });

        column.setHeader(messageService.getMessage("ocs.value", VaadinUtils.getLocale()));

        grid.setHeight("200px");
        grid.setSelectionMode(SelectionMode.SINGLE);

        // add a change listener (to make sure the buttons are correctly
        // enabled/disabled)
        grid.addSelectionListener(event -> {
            if (grid.getSelectedItems().iterator().hasNext()) {
                ValueHolder<T> vh = grid.getSelectedItems().iterator().next();
                onSelect(vh.getValue());
            }
        });

        // add a remove button directly in the grid
        constructRemoveColumn();
        constructButtonBar(main);

        add(main);
    }

    public boolean isViewMode() {
        return viewMode;
    }

    private String message(String key, Object... values) {
        return messageService.getMessage(key, VaadinUtils.getLocale(), values);
    }

    /**
     * Respond to a selection of an item in the grid
     */
    protected void onSelect(final Object selected) {
        // overwrite in subclass if needed
    }

    /**
     * Add additional buttons to the button bar
     *
     * @param buttonBar
     */
    protected void postProcessButtonBar(HorizontalLayout buttonBar) {
        // overwrite in subclass if needed
    }

    public void setEntity(U entity) {
        this.entity = entity;
    }

    public void setFormOptions(final FormOptions formOptions) {
        this.formOptions = formOptions;
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
        // refresh the bindings and validate
        for (Entry<ValueHolder<T>, Binder<ValueHolder<T>>> entry : binders.entrySet()) {
            error |= !entry.getValue().validate().isOk();
        }
        return error;
    }

    public Grid<ValueHolder<T>> getGrid() {
        return grid;
    }

    @Override
    protected Collection<T> generateModelValue() {
        Collection<T> col = provider.getItems().stream().map(vh -> vh.getValue()).collect(Collectors.toList());
        Collection<T> converted = ConvertUtils.convertCollection(col, attributeModel);
        if (entity != null) {
            ClassUtils.setFieldValue(entity, attributeModel.getPath(), converted);
        }
        return converted;
    }

    @Override
    protected void setPresentationValue(Collection<T> value) {
        provider.getItems().clear();
        provider.refreshAll();
        binders.clear();
        for (T t : value) {
            ValueHolder<T> vh = new ValueHolder<>(t);
            provider.getItems().add(vh);
            @SuppressWarnings({ "rawtypes", "unchecked" })
            Binder<ValueHolder<T>> binder = new BeanValidationBinder(ValueHolder.class);
            binder.setBean(vh);
            binders.put(vh, binder);
        }
    }
}
