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
import java.util.List;

import com.explicatis.ext_token_field.ExtTokenField;
import com.explicatis.ext_token_field.Tokenizable;
import com.ocs.dynamo.service.MessageService;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
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
	static <T> void initializeOrdering(SortOrder<T>[] sortOrders, List<Object> sortProperties,
			List<Boolean> sortOrdering) {
		if (sortOrders != null) {
			for (SortOrder<T> sortOrder : sortOrders) {
				sortProperties.add(sortOrder.getSorted().toString());
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
	private static <T> void addTokens(ExtTokenField extTokenField, ListDataProvider<T> provider,
			Collection<ValueChangeListener<?>> valueChangeListeners, AbstractField<?> field,
			TokenizableFactory<T> tokenizableFactory) {
		extTokenField.clear();
		if (!provider.getItems().isEmpty()) {
			for (T item : provider.getItems()) {
				Tokenizable token = tokenizableFactory.createToken(item);
				extTokenField.addTokenizable(token);
			}
		}
		for (ValueChangeListener<?> valueChangeListener : valueChangeListeners) {
			// valueChangeListener.valueChange(new ValueChangeEvent<>(field));
		}
	}

	/**
	 * Set up a listener to respond to a combo box selection change
	 */
	@SuppressWarnings("unchecked")
	private static <T> void attachComboBoxValueChange(final ComboBox<T> comboBox, final ExtTokenField extTokenField,
			final ListDataProvider<T> container, final Collection<ValueChangeListener<?>> valueChangeListeners,
			final AbstractField<?> field, final TokenizableFactory<T> tokenizableFactory) {
		comboBox.addValueChangeListener(event -> {
			Object selectedObject = event.getValue();
			if (selectedObject != null) {
				T value = (T) selectedObject;
				addValueToProvider(value, container);

				addTokens(extTokenField, container, valueChangeListeners, field, tokenizableFactory);

				// reset the combo box
				comboBox.setValue(null);
				ListDataProvider<T> provider = (ListDataProvider<T>) comboBox.getDataProvider();
				provider.getItems().remove(value);

				// copyValueFromContainer(container, field);
			}
		});
	}

	/**
	 * Respond to a token removal by also removing the corresponding value from the
	 * container
	 */
	private static <T> void attachTokenFieldValueChange(ExtTokenField extTokenField, ListDataProvider<T> provider,
			ComboBox<T> comboBox, List<Object> sortProperties, List<Boolean> sortOrdering, final AbstractField<?> field,
			TokenizableFactory<T> tokenizableFactory) {
		extTokenField.addTokenRemovedListener(event -> {
			final Tokenizable tokenizable = event.getTokenizable();
			tokenizableFactory.removeTokenFromContainer(tokenizable, provider);

			comboBox.setValue(null);
			tokenizableFactory.addTokenToComboBox(tokenizable, comboBox);
			//sortComboBox(comboBox, sortProperties, sortOrdering);
			// copyValueFromContainer(provider, field);
		});
	}

	/**
	 * 
	 * @param comboBox
	 * @param sortProperties
	 * @param sortOrdering
	 */
//	static <T> void sortComboBox(ComboBox<T> comboBox, List<Object> sortProperties, List<Boolean> sortOrdering) {
//		// re-order the list to the original order
//		if (sortProperties != null && sortOrdering != null) {
//			Object[] sortPropertiesArray = sortProperties.toArray(new Object[sortProperties.size()]);
//			boolean[] sortOrderingArray = new boolean[sortOrdering.size()];
//			for (int i = 0; i < sortOrdering.size(); i++) {
//				// auto boxing is not working with direct to List.toArray call...
//				sortOrderingArray[i] = sortOrdering.get(i);
//			}
//			// TODO: sorting
//			// ((Container.Sortable) comboBox.getDataProvider()).sort(sortPropertiesArray,
//			// sortOrderingArray);
//		}
//	}

	// /**
	// * Copies the values from the container to the component
	// */
	// @SuppressWarnings({ "unchecked", "rawtypes" })
	// static <T> void copyValueFromContainer(ListDataProvider<T> container,
	// AbstractField<?> field) {
	// Collection<T> values = container.getItemIds();
	// field.setValue(new HashSet<>(values));
	// }

	/**
	 * 
	 * @param value
	 * @param container
	 */
	static <T> void addValueToProvider(T value, ListDataProvider<T> provider) {
		provider.getItems().add(value);
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
	 *            value change listeners that must be notified when a value is
	 *            selected in the combo box
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
	static <T> Component initContent(final ComboBox<T> comboBox, final MessageService messageService,
			ExtTokenField extTokenField, ListDataProvider<T> provider,
			Collection<ValueChangeListener<?>> valueChangeListeners, AbstractField<?> field,
			List<Object> sortProperties, List<Boolean> sortOrdering, PostProcessLayout processLayout,
			TokenizableFactory<T> tokenizableFactory) {
		HorizontalLayout layout = new DefaultHorizontalLayout(false, true, false);

		// comboBox.setInputPrompt(messageService.getMessage("ocs.type.to.add",
		// VaadinUtils.getLocale()));
		// comboBox.setFilteringMode(FilteringMode.CONTAINS);
		// comboBox.setNullSelectionAllowed(false);
		comboBox.setWidth(25, Sizeable.Unit.PERCENTAGE);
		comboBox.setHeightUndefined();

		extTokenField.setInputField(comboBox);
		extTokenField.setEnableDefaultDeleteTokenAction(true);

		attachComboBoxValueChange(comboBox, extTokenField, provider, valueChangeListeners, field, tokenizableFactory);
		attachTokenFieldValueChange(extTokenField, provider, comboBox, sortProperties, sortOrdering, field,
				tokenizableFactory);

		layout.addComponent(extTokenField);

		// initial filling of the field
		addTokens(extTokenField, provider, valueChangeListeners, field, tokenizableFactory);

		// provider.addItemSetChangeListener(
		// event -> addTokens(extTokenField, container, valueChangeListeners, field,
		// tokenizableFactory));
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
		 * Method that is called when a value is removed from the available values and
		 * added as a token
		 * 
		 * @param tokenizable
		 * @param container
		 */
		void removeTokenFromContainer(Tokenizable tokenizable, ListDataProvider<T> provider);

		/**
		 * Method that is called when a value is removed as a token and added to the
		 * list of available values
		 * 
		 * @param tokenizable
		 * @param comboBox
		 */
		void addTokenToComboBox(Tokenizable tokenizable, ComboBox<T> comboBox);
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
