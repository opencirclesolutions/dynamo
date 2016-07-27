package com.ocs.dynamo.ui.component;

import com.explicatis.ext_token_field.ExtTokenField;
import com.explicatis.ext_token_field.SimpleTokenizable;
import com.explicatis.ext_token_field.Tokenizable;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@StyleSheet("tokenField.css")
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
		HorizontalLayout layout = new DefaultHorizontalLayout(false, false, false);

		comboBox.setInputPrompt(getMessageService().getMessage("ocs.type.to.add.new.value"));
		extTokenField.setInputField(comboBox);
		extTokenField.setEnableDefaultDeleteTokenAction(true);
		comboBox.addValueChangeListener(getComboBoxValueChangeListener(extTokenField, comboBox, getEntityModel(), getAttributeModel()));

		extTokenField.setSizeFull();
		layout.addComponent(extTokenField);
		return layout;
	}

	private ValueChangeListener getComboBoxValueChangeListener(final ExtTokenField extTokenField, final EntityComboBox comboBox, EntityModel<T> entityModel, AttributeModel attributeModel) {
		return new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				Object selectedObject = event.getProperty().getValue();
				if (selectedObject != null) {
					T abstractEntity = (T) selectedObject;
					container.addBean(abstractEntity);

					Tokenizable token = new SimpleTokenizable(getTokenIdentifier(abstractEntity), getTokenDisplayName(abstractEntity, (String) comboBox.getItemCaptionPropertyId()));
					extTokenField.addTokenizable(token);

					for (ValueChangeListener valueChangeListener : TokenFieldSelect.this.valueChangeListeners) {
						valueChangeListener.valueChange(new ValueChangeEvent(TokenFieldSelect.this));
					}

					// reset combobox
					comboBox.setValue(null);
				}
			}
		};
	}

	@Override
	public List<T> getValue() {
		return container.getItemIds();
	}

	@Override
	protected List<T> getInternalValue() {
		return container.getItemIds();
	}

	@Override
	public Class<? extends Collection> getType() {
		return List.class;
	}

	private String getTokenDisplayName(AbstractEntity entity, String captionPropertyId) {
		return ClassUtils.getFieldValueAsString(entity, captionPropertyId);
	}

	private long getTokenIdentifier(AbstractEntity entity) {
		return Long.parseLong(ClassUtils.getFieldValueAsString(entity, "id"));
	}

	@Override
	public void addValueChangeListener(final ValueChangeListener listener) {
		valueChangeListeners.add(listener);
	}
}
