package com.ocs.dynamo.ui.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.explicatis.ext_token_field.ExtTokenField;
import com.explicatis.ext_token_field.SimpleTokenizable;
import com.explicatis.ext_token_field.Tokenizable;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.ServiceLocator;
import com.vaadin.data.Container;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ItemSorter;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;

public class SimpleTokenFieldSelect<T extends Comparable> extends CustomField<Collection<T>> {

	private static final long serialVersionUID = -1490179285573442827L;
	private static final String VALUE = "value";
	private final ExtTokenField extTokenField;
	private final ComboBox comboBox;
	private final BeanItemContainer<T> container;
	private final Collection<ValueChangeListener> valueChangeListeners;
	private List<Object> sortProperties;
	private List<Boolean> sortOrdering;
	private MessageService messageService;
	private GenericTokenFieldUtil.TokenizableFactory tokenizableFactory;

	/**
	 * Constructor
	 *
	 * @param attributeModel
	 * @param items
	 * @param elementType
	 * @param sortOrders
	 */
	public SimpleTokenFieldSelect(AttributeModel attributeModel, List<T> items, Class<T> elementType,
			SortOrder... sortOrders) {
		messageService = ServiceLocator.getMessageService();

		extTokenField = new ExtTokenField();

		comboBox = new ComboBox();
		comboBox.addItems(items);
		comboBox.setFilteringMode(FilteringMode.CONTAINS);

		sortProperties = new ArrayList<>();
		sortOrdering = new ArrayList<>();
		GenericTokenFieldUtil.initializeOrdering(sortOrders, sortProperties, sortOrdering);

		((IndexedContainer) comboBox.getContainerDataSource()).setItemSorter(new SimpleItemSorter());

		container = new BeanItemContainer<T>(elementType);
		valueChangeListeners = new ArrayList<>();

		tokenizableFactory = new GenericTokenFieldUtil.TokenizableFactory() {
			@Override
			public Tokenizable createToken(Object item) {
				return new SimpleTokenizable(System.nanoTime(), item.toString());
			}

			@Override
			public void removeTokenFromContainer(Tokenizable tokenizable, BeanItemContainer container) {
				container.removeItem(tokenizable.getStringValue());
			}

			@Override
			public void addTokenToComboBox(Tokenizable tokenizable, ComboBox comboBox) {
				comboBox.addItem(tokenizable.getStringValue());
			}
		};
	}

	@Override
	public void addValueChangeListener(final ValueChangeListener listener) {
		valueChangeListeners.add(listener);
	}

	@Override
	protected List<T> getInternalValue() {
		if (container.size() == 0) {
			return null;
		}

		return container.getItemIds();
	}

	@Override
	protected void setInternalValue(Collection<T> values) {
		super.setInternalValue(values);

		if (values == null && !container.getItemIds().isEmpty()) {
			// restore all item in the comboBox
			for (T item : container.getItemIds()) {
				comboBox.getContainerDataSource().addItem(item);
			}
			GenericTokenFieldUtil.sortComboBox(comboBox, sortProperties, sortOrdering);
		}

		container.removeAllItems();

		if (values != null) {
			container.addAll(values);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<Collection<T>> getType() {
		return (Class<Collection<T>>) (Object) Collection.class;
	}

	@Override
	public List<T> getValue() {
		return getInternalValue();
	}

	@Override
	public void setValue(Collection<T> values) throws ReadOnlyException, Converter.ConversionException {
		super.setValue(values);
		setInternalValue(values);
	}

	@Override
	protected Component initContent() {
		return GenericTokenFieldUtil
				.initContent(comboBox, messageService, extTokenField, container, valueChangeListeners, this,
						sortProperties, sortOrdering, new GenericTokenFieldUtil.PostProcessLayout() {
							@Override
							public void postProcessLayout(AbstractOrderedLayout layout) {
								// nothing to do
							}
						}, tokenizableFactory);
	}

	private class SimpleItemSorter implements ItemSorter {

		private boolean sortOrderAscending;

		@Override
		@SuppressWarnings("unchecked")
		public int compare(Object itemId1, Object itemId2) {
			T item1 = (T) itemId1;
			T item2 = (T) itemId2;

			/*
			 * Items can be null if the container is filtered. Null is considered
			 * "less" than not-null.
			 */
			if (item1 == null) {
				if (item2 == null) {
					return 0;
				} else {
					return 1;
				}
			} else if (item2 == null) {
				return -1;
			}

			return this.sortOrderAscending ? item1.compareTo(item2) : item2.compareTo(item1);
		}

		@Override
		public void setSortProperties(Container.Sortable container, Object[] propertyId, boolean[] ascending) {
			sortOrderAscending = true;
			if (ascending != null) {
				sortOrderAscending = ascending[0];
			}
		}
	}
}
