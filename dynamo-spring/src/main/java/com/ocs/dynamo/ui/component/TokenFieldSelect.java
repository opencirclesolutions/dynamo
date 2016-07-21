package com.ocs.dynamo.ui.component;

import com.explicatis.ext_token_field.ExtTokenField;
import com.explicatis.ext_token_field.SimpleTokenizable;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

import java.io.Serializable;

public class TokenFieldSelect<ID extends Serializable, T extends AbstractEntity<ID>> extends
		CustomEntityField<ID, T, Object> {
	private final ExtTokenField extTokenField;
	private final EntityComboBox comboBox;

	public TokenFieldSelect(EntityModel<T> em, AttributeModel attributeModel, BaseService<ID, T> service,
							Container.Filter filter, SortOrder... sortOrders) {
		super(service, em, attributeModel);
		extTokenField = new ExtTokenField();

		comboBox = new EntityComboBox<ID, T>(em ,attributeModel, service, sortOrders);
	}


	@Override
	protected Component initContent() {
		VerticalLayout layout = new DefaultVerticalLayout(false, false);

		comboBox.setItemCaptionPropertyId("name");
		comboBox.setInputPrompt(getMessageService().getMessage("ocs.type.to.add.new.value"));
		extTokenField.setInputField(comboBox);
		extTokenField.setEnableDefaultDeleteTokenAction(true);
		comboBox.addValueChangeListener(getComboBoxValueChangeListener(extTokenField, comboBox));

		layout.addComponent(extTokenField);
		return layout;
	}

	@Override
	public Class<? extends Object> getType() {
		return Object.class;
	}

	private ValueChangeListener getComboBoxValueChangeListener(final ExtTokenField extTokenField, final ComboBox comboBox) {
		return new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {

				Object countryObject = event.getProperty().getValue();
				if (countryObject != null) {
					SimpleTokenizable t = new SimpleTokenizable((Integer) ClassUtils.getFieldValue(countryObject, "id"), ClassUtils.getFieldValueAsString(countryObject, "name"));
					extTokenField.addTokenizable(t);

					// if you would use a real container, you would filter the selected tokens out

					// reset combobox
					comboBox.setValue(null);
				}
			}
		};
	}
}
