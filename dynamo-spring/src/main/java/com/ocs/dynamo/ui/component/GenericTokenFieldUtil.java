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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.explicatis.ext_token_field.ExtTokenField;
import com.explicatis.ext_token_field.Tokenizable;
import com.ocs.dynamo.service.MessageService;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;

public final class GenericTokenFieldUtil {

    private GenericTokenFieldUtil() {
    }

    /**
     * Copy sort orders to property and boolean lists
     * 
     * @param sortOrders
     * @param sortProperties
     * @param sortOrdering
     */
    static void initializeOrdering(SortOrder[] sortOrders, List<Object> sortProperties, List<Boolean> sortOrdering) {
        if (sortOrders != null) {
            for (SortOrder sortOrder : sortOrders) {
                sortProperties.add(sortOrder.getPropertyId());
                sortOrdering.add(sortOrder.getDirection().equals(SortDirection.ASCENDING));
            }
        }
    }

    /**
     * Adds a token for every selected item
     * 
     * @param extTokenField
     *            the token field
     * @param container
     *            the container
     * @param valueChangeListeners
     *            list of value change listeners that respond to the addition
     * @param field
     * @param tokenizableFactory
     */
    private static <T> void addTokens(ExtTokenField extTokenField, BeanItemContainer<T> container,
            Collection<Property.ValueChangeListener> valueChangeListeners, Field<?> field,
            TokenizableFactory<T> tokenizableFactory) {
        extTokenField.clear();
        if (container.size() > 0) {
            for (T item : container.getItemIds()) {
                Tokenizable token = tokenizableFactory.createToken(item);
                extTokenField.addTokenizable(token);
            }
        }
        for (Property.ValueChangeListener valueChangeListener : valueChangeListeners) {
            valueChangeListener.valueChange(new Field.ValueChangeEvent(field));
        }
    }

    /**
     * Set up a listener to respond to a combo box selection change
     */
    @SuppressWarnings("unchecked")
    private static <T> void attachComboBoxValueChange(final ComboBox comboBox, final ExtTokenField extTokenField,
            final BeanItemContainer<T> container, final Collection<Property.ValueChangeListener> valueChangeListeners,
            final Field<?> field, final TokenizableFactory<T> tokenizableFactory) {
        comboBox.addValueChangeListener(new Property.ValueChangeListener() {

            private static final long serialVersionUID = -1734818761735064248L;

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Object selectedObject = event.getProperty().getValue();
                if (selectedObject != null) {
                    T value = (T) selectedObject;
                    addValueToContainer(value, container);

                    addTokens(extTokenField, container, valueChangeListeners, field, tokenizableFactory);

                    // reset the combo box
                    comboBox.setValue(null);
                    comboBox.getContainerDataSource().removeItem(value);

                    copyValueFromContainer(container, field);
                }
            }
        });
    }

    /**
     * Respond to a token removal by also removing the corresponding value from the container
     */
    private static <T> void attachTokenFieldValueChange(ExtTokenField extTokenField,
            final BeanItemContainer<T> container, final ComboBox comboBox, final List<Object> sortProperties,
            final List<Boolean> sortOrdering, final Field<?> field, final TokenizableFactory<T> tokenizableFactory) {
        extTokenField.addTokenRemovedListener(event -> {
            final Tokenizable tokenizable = event.getTokenizable();
            tokenizableFactory.removeTokenFromContainer(tokenizable, container);

            comboBox.setValue(null);
            tokenizableFactory.addTokenToComboBox(tokenizable, comboBox);
            sortComboBox(comboBox, sortProperties, sortOrdering);

            copyValueFromContainer(container, field);
        });
    }

    /**
     * 
     * @param comboBox
     * @param sortProperties
     * @param sortOrdering
     */
    static void sortComboBox(ComboBox comboBox, List<Object> sortProperties, List<Boolean> sortOrdering) {
        // re-order the list to the original order
        if (sortProperties != null && sortOrdering != null) {
            Object[] sortPropertiesArray = sortProperties.toArray(new Object[sortProperties.size()]);
            boolean[] sortOrderingArray = new boolean[sortOrdering.size()];
            for (int i = 0; i < sortOrdering.size(); i++) {
                // auto boxing is not working with direct to List.toArray call...
                sortOrderingArray[i] = sortOrdering.get(i);
            }

            ((Container.Sortable) comboBox.getContainerDataSource()).sort(sortPropertiesArray, sortOrderingArray);
        }
    }

    /**
     * Copies the values from the container to the component
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    static <T> void copyValueFromContainer(BeanItemContainer<T> container, Field field) {
        Collection<T> values = container.getItemIds();
        field.setValue(new HashSet<>(values));
    }

    /**
     * 
     * @param value
     * @param container
     */
    static <T> void addValueToContainer(T value, BeanItemContainer<T> container) {
        container.addBean(value);
    }

    /**
     * Initializes the select component
     * 
     * @param comboBox
     *            the combo box
     * @param messageService
     *            the message service
     * @param extTokenField
     *            the extended token field
     * @param container
     *            the container that holds the available items
     * @param valueChangeListeners
     *            value change listeners that must be notified when a value is selected in the combo
     *            box
     * @param field
     *            ???
     * @param sortProperties
     *            the properties to sort on
     * @param sortOrdering
     *            the sort direction
     * @param processLayout
     * @param tokenizableFactory
     * @return
     */
    static <T> Component initContent(final ComboBox comboBox, final MessageService messageService,
            final ExtTokenField extTokenField, final BeanItemContainer<T> container,
            final Collection<Property.ValueChangeListener> valueChangeListeners, final Field<?> field,
            final List<Object> sortProperties, final List<Boolean> sortOrdering, final PostProcessLayout processLayout,
            final TokenizableFactory<T> tokenizableFactory) {
        HorizontalLayout layout = new DefaultHorizontalLayout(false, true, false);

        comboBox.setInputPrompt(messageService.getMessage("ocs.type.to.add"));
        comboBox.setFilteringMode(FilteringMode.CONTAINS);
        comboBox.setNullSelectionAllowed(false);
        comboBox.setWidth(25, Sizeable.Unit.PERCENTAGE);
        comboBox.setHeightUndefined();

        extTokenField.setInputField(comboBox);
        extTokenField.setEnableDefaultDeleteTokenAction(true);

        attachComboBoxValueChange(comboBox, extTokenField, container, valueChangeListeners, field, tokenizableFactory);
        attachTokenFieldValueChange(extTokenField, container, comboBox, sortProperties, sortOrdering, field,
                tokenizableFactory);

        layout.addComponent(extTokenField);

        // initial filling of the field
        addTokens(extTokenField, container, valueChangeListeners, field, tokenizableFactory);

        container.addItemSetChangeListener(new Container.ItemSetChangeListener() {

            private static final long serialVersionUID = -2171389796068112560L;

            @Override
            public void containerItemSetChange(Container.ItemSetChangeEvent event) {
                addTokens(extTokenField, container, valueChangeListeners, field, tokenizableFactory);
            }
        });

        processLayout.postProcessLayout(layout);

        layout.setSizeFull();

        return layout;
    }

    /**
     * Interface for a factory that removes/adds tokens
     * 
     * @author bas.rutten
     *
     * @param <T>
     */
    interface TokenizableFactory<T> {
        Tokenizable createToken(T item);

        /**
         * Method that is called when a value is removed from the available values and added as a
         * token
         * 
         * @param tokenizable
         * @param container
         */
        void removeTokenFromContainer(Tokenizable tokenizable, BeanItemContainer<T> container);

        /**
         * Method that is called when a value is removed as a token and added to the list of
         * available values
         * 
         * @param tokenizable
         * @param comboBox
         */
        void addTokenToComboBox(Tokenizable tokenizable, ComboBox comboBox);
    }

    /**
     * Functional interface for performing post-processing
     * 
     * @author bas.rutten
     *
     */
    @FunctionalInterface
    interface PostProcessLayout {

        void postProcessLayout(AbstractOrderedLayout layout);
    }

}
