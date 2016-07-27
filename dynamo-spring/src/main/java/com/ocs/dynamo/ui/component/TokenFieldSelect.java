package com.ocs.dynamo.ui.component;

import com.explicatis.ext_token_field.ExtTokenField;
import com.explicatis.ext_token_field.Tokenizable;
import com.explicatis.ext_token_field.events.TokenRemovedEvent;
import com.explicatis.ext_token_field.events.TokenRemovedListener;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TokenFieldSelect<ID extends Serializable, T extends AbstractEntity<ID>> extends
		CustomEntityField<ID, T, Collection> {
	private final ExtTokenField extTokenField;
	private final EntityComboBox comboBox;
	private final BeanItemContainer<T> container;
	private final Collection<ValueChangeListener> valueChangeListeners;

	public TokenFieldSelect(EntityModel<T> em, AttributeModel attributeModel, BaseService<ID, T> service,
							Container.Filter filter, SortOrder... sortOrders) {
		super(service, em, attributeModel);
		extTokenField = new ExtTokenField();
		comboBox = new EntityComboBox<>(em, attributeModel, service, filter, sortOrders);
		container = new BeanItemContainer<>(AbstractEntity.class);
		valueChangeListeners = new ArrayList<>();
	}

	@Override
	protected Component initContent() {
		HorizontalLayout layout = new DefaultHorizontalLayout(false, true, false);

		comboBox.setInputPrompt(getMessageService().getMessage("ocs.type.to.add"));
		comboBox.setFilteringMode(FilteringMode.CONTAINS);
		comboBox.setWidthUndefined();
		extTokenField.setInputField(comboBox);
		extTokenField.setEnableDefaultDeleteTokenAction(true);

		attachComboBoxValueChange();
		attachTokenFieldValueChange();
		setupContainerFieldSync();

		layout.addComponent(extTokenField);
		layout.setSizeFull();
		return layout;
	}

	private void setupContainerFieldSync() {
		container.addItemSetChangeListener(new Container.ItemSetChangeListener() {
			@Override
			public void containerItemSetChange(Container.ItemSetChangeEvent event) {
				extTokenField.clear();
				if (container.size() > 0) {
					for (T item : container.getItemIds()) {
						Tokenizable token = new BeanItemTokenizable(item, (String) comboBox.getItemCaptionPropertyId());
						extTokenField.addTokenizable(token);
					}
				}
				for (ValueChangeListener valueChangeListener : valueChangeListeners) {
					valueChangeListener.valueChange(new ValueChangeEvent(TokenFieldSelect.this));
				}
			}
		});
	}

	private void attachTokenFieldValueChange() {
		extTokenField.addTokenRemovedListener(new TokenRemovedListener() {
			@Override
			public void tokenRemovedEvent(TokenRemovedEvent event) {
				final BeanItemTokenizable tokenizable = (BeanItemTokenizable) event.getTokenizable();
				container.removeItem(tokenizable.getItem());
			}
		});
	}

	private void attachComboBoxValueChange() {
		comboBox.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				Object selectedObject = event.getProperty().getValue();
				if (selectedObject != null) {
					T abstractEntity = (T) selectedObject;
					container.addBean(abstractEntity);

					// reset combobox
					comboBox.setValue(null);
				}
			}
		});
	}

	@Override
	public List<T> getValue() {
		return getInternalValue();
	}

	@Override
	public void setValue(Collection values) throws ReadOnlyException, Converter.ConversionException {
		setInternalValue(values);
	}

	@Override
	protected void setInternalValue(Collection values) {
		container.removeAllItems();
		if (values != null) {
			container.addAll(values);
		}
	}

	@Override
	protected List<T> getInternalValue() {
		if (container.size() == 0) {
			return null;
		}
		return container.getItemIds();
	}

	@Override
	public Class<? extends Collection> getType() {
		return List.class;
	}

	@Override
	public void addValueChangeListener(final ValueChangeListener listener) {
		valueChangeListeners.add(listener);
	}

	private class BeanItemTokenizable implements Tokenizable {
		private final T item;
		private final String displayValue;
		private final Long id;

		private BeanItemTokenizable(T item, String captionPropertyId) {
			this.item = item;
			this.id = getTokenIdentifier(item);
			this.displayValue = getTokenDisplayName(item, captionPropertyId);
		}

		@Override
		public String getStringValue() {
			return displayValue;
		}

		@Override
		public long getIdentifier() {
			return id;
		}

		public T getItem() {
			return item;
		}

		private String getTokenDisplayName(T entity, String captionPropertyId) {
			return ClassUtils.getFieldValueAsString(entity, captionPropertyId);
		}

		private long getTokenIdentifier(T entity) {
			return Long.parseLong(ClassUtils.getFieldValueAsString(entity, "id"));
		}
	}
}
