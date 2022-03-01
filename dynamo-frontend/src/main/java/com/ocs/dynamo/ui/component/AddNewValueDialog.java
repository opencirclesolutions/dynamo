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
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSNonUniqueException;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.dialog.SimpleModalDialog;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

/**
 * A popup dialog for quickly adding new values to a domain
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the primary key of the entity being added
 * @param <T>  the type of the entity being added
 */
public class AddNewValueDialog<ID extends Serializable, T extends AbstractEntity<ID>> extends SimpleModalDialog {

	private static final long serialVersionUID = 6208738706327329145L;

	private Consumer<T> afterNewEntityAdded;

	private AttributeModel attributeModel;

	private EntityModel<T> entityModel;

	private BaseService<ID, T> service;

	private TextField valueField;

	public AddNewValueDialog(EntityModel<T> entityModel, AttributeModel attributeModel, BaseService<ID, T> service) {
		super(true);
		this.entityModel = entityModel;
		this.attributeModel = attributeModel;
		this.service = service;

		setTitle(message("ocs.enter.new.value", entityModel.getDisplayName(VaadinUtils.getLocale()),
				VaadinUtils.getLocale()));
		setBuildMainLayout(parent -> {
			VerticalLayout container = new DefaultVerticalLayout(true, true);
			parent.add(container);

			valueField = new TextField(message("ocs.enter.new.value", VaadinUtils.getLocale()));
			valueField.setSizeFull();
			valueField.focus();
			container.add(valueField);
		});

		constructCloseListener();
	}

	private void constructCloseListener() {
		setOnClose(() -> {
			String value = valueField.getValue();
			if (!StringUtils.isEmpty(value)) {
				T t = service.createNewEntity();

				// disallow values that are too long
				String propName = attributeModel.getQuickAddPropertyName();
				Integer maxLength = entityModel.getAttributeModel(propName).getMaxLength();

				if (maxLength != null && value.length() > maxLength) {
					showNotification(message("ocs.value.too.long", VaadinUtils.getLocale()));
					return false;
				}
				ClassUtils.setFieldValue(t, propName, value);

				try {
					t = service.save(t);
					afterNewEntityAdded.accept(t);
					return true;
				} catch (OCSNonUniqueException ex) {
					showNotification(ex.getMessage());
				}
			} else {
				showNotification(message("ocs.value.required", VaadinUtils.getLocale()));
			}
			return false;
		});
	}

	public Consumer<T> getAfterNewEntityAdded() {
		return afterNewEntityAdded;
	}

//	@Override
//	protected String getTitle() {
//		return message("ocs.enter.new.value", entityModel.getDisplayName(VaadinUtils.getLocale()),
//				VaadinUtils.getLocale());
//	}

	public TextField getValueField() {
		return valueField;
	}

	public void setAfterNewEntityAdded(Consumer<T> afterNewEntityAdded) {
		this.afterNewEntityAdded = afterNewEntityAdded;
	}

}
