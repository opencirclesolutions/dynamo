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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.explicatis.ext_token_field.ExtTokenField;
import com.explicatis.ext_token_field.SimpleTokenizable;
import com.explicatis.ext_token_field.Tokenizable;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.FilterConverter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.utils.SortUtil;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.Registration;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;

/**
 * A token field that displays the distinct values for a basic property of an
 * entity
 * 
 * @author bas.rutten
 *
 * @param <ID>
 *            the type of the primary key
 * @param <S>
 *            the type of the entity
 * @param <T>
 *            the type of the basic property
 */
public class SimpleTokenFieldSelect<ID extends Serializable, T extends AbstractEntity<ID>>
		extends CustomField<Collection<T>> implements Refreshable {

	// private class SimpleItemSorter implements ItemSorter {
	//
	// private static final long serialVersionUID = -2397932123434432733L;
	//
	// private boolean sortOrderAscending;
	//
	// @Override
	// @SuppressWarnings("unchecked")
	// public int compare(Object itemId1, Object itemId2) {
	// T item1 = (T) itemId1;
	// T item2 = (T) itemId2;
	//
	// /*
	// * Items can be null if the container is filtered. Null is
	// * considered "less" than not-null.
	// */
	// if (item1 == null) {
	// if (item2 == null) {
	// return 0;
	// } else {
	// return 1;
	// }
	// } else if (item2 == null) {
	// return -1;
	// }
	//
	// return this.sortOrderAscending ? item1.compareTo(item2) :
	// item2.compareTo(item1);
	// }
	//
	// @Override
	// public void setSortProperties(Container.Sortable container, Object[]
	// propertyId, boolean[] ascending) {
	// sortOrderAscending = true;
	// if (ascending != null) {
	// sortOrderAscending = ascending[0];
	// }
	// }
	// }

	private static final long serialVersionUID = -1490179285573442827L;

	private AttributeModel attributeModel;

	private final ExtTokenField extTokenField;

	private final boolean elementCollection;

	private final ComboBox<T> comboBox;

	private final ListDataProvider<T> dataProvider;

	private final Collection<ValueChangeListener<?>> valueChangeListeners;

	private List<Object> sortProperties;

	private List<Boolean> sortOrdering;

	private MessageService messageService;

	private GenericTokenFieldUtil.TokenizableFactory<T> tokenizableFactory;

	private BaseService<ID, T> service;

	private EntityModel<T> entityModel;

	private SerializablePredicate<T> fieldFilter;

	private String distinctField;

	private SortOrder<T>[] sortOrders;

	private Class<T> elementType;

	/**
	 * Constructor
	 *
	 * @param service
	 *
	 * @param entityModel
	 *
	 * @param attributeModel
	 *            the attribute model
	 * @param fieldFilter
	 *            the list of items to display
	 * @param distinctField
	 * @param elementType
	 *            the type of the items to display
	 * @param sortOrders
	 *            sort orders to apply
	 */
	@SafeVarargs
	public SimpleTokenFieldSelect(BaseService<ID, T> service, EntityModel<T> entityModel, AttributeModel attributeModel,
			SerializablePredicate<T> fieldFilter, String distinctField, Class<T> elementType, boolean elementCollection,
			SortOrder<T>... sortOrders) {
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		this.service = service;
		this.entityModel = entityModel;
		this.fieldFilter = fieldFilter;
		this.distinctField = distinctField;
		this.sortOrders = sortOrders;
		this.elementType = elementType;
		this.elementCollection = elementCollection;
		this.attributeModel = attributeModel;

		setCaption(attributeModel.getDisplayName());

		extTokenField = new ExtTokenField();

		comboBox = new ComboBox<T>();
		fillComboBox(this.elementCollection);

		sortProperties = new ArrayList<>();
		sortOrdering = new ArrayList<>();
		GenericTokenFieldUtil.initializeOrdering(sortOrders, sortProperties, sortOrdering);

		// ((IndexedContainer) comboBox.getContainerDataSource()).setItemSorter(new
		// SimpleItemSorter());

		dataProvider = new ListDataProvider<T>(new ArrayList<>());
		valueChangeListeners = new ArrayList<>();

		tokenizableFactory = new GenericTokenFieldUtil.TokenizableFactory<T>() {
			@Override
			public void addTokenToComboBox(Tokenizable tokenizable, ComboBox<T> comboBox) {
				// comboBox.getIte(tokenizable.getStringValue());
			}

			@Override
			public Tokenizable createToken(T item) {
				return new SimpleTokenizable(System.nanoTime(), item.toString());
			}

			@Override
			public void removeTokenFromContainer(Tokenizable tokenizable, ListDataProvider<T> container) {
				// container.removeItem(tokenizable.getStringValue());
			}
		};
	}

	@Override
	public Registration addValueChangeListener(final ValueChangeListener listener) {
		valueChangeListeners.add(listener);
		return null;
	}

	private void fillComboBox(boolean elementCollection) {
		List<T> items = null;
		if (elementCollection) {
			items = service.findDistinctInCollectionTable(attributeModel.getCollectionTableName(),
					attributeModel.getCollectionTableFieldName(), elementType);
		} else {
			items = service.findDistinct(new FilterConverter<T>(entityModel).convert(fieldFilter), distinctField,
					elementType, SortUtil.translate(sortOrders));
		}
		// comboBox.removeAllItems();
		// comboBox.addItems(items);
	}

	public ComboBox<T> getComboBox() {
		return comboBox;
	}

	// @Override
	// protected List<T> getInternalValue() {
	// if (container.size() == 0) {
	// return null;
	// }
	// return container.getItemIds();
	// }

	public ExtTokenField getTokenField() {
		return extTokenField;
	}

	// @Override
	// public List<T> getValue() {
	// return getInternalValue();
	// }

	@Override
	protected Component initContent() {
		return GenericTokenFieldUtil.initContent(comboBox, messageService, extTokenField, dataProvider,
				valueChangeListeners, this, sortProperties, sortOrdering, layout -> {
					// nothing to do
				}, tokenizableFactory);
	}

	@Override
	public void refresh() {
		if (comboBox != null) {
			fillComboBox(elementCollection);
		}
	}

	// @Override
	// protected void setInternalValue(Collection<T> values) {
	// super.setInternalValue(values);
	//
	// if (values == null && !container.getItemIds().isEmpty()) {
	// // restore all item in the comboBox
	// for (T item : container.getItemIds()) {
	// comboBox.getContainerDataSource().addItem(item);
	// }
	// GenericTokenFieldUtil.sortComboBox(comboBox, sortProperties, sortOrdering);
	// }
	// container.removeAllItems();
	// if (values != null) {
	// container.addAll(values);
	// }
	// }

	@Override
	public void setValue(Collection<T> values) {
		super.setValue(values);
		// setInternalValue(values);
	}

	@Override
	protected void doSetValue(Collection<T> value) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<T> getValue() {
		// TODO Auto-generated method stub
		return null;
	}

}
