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

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.ui.BaseUI;
import com.ocs.dynamo.ui.utils.FormatUtils;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 * A field for displaying an internal link
 * 
 * @author Bas Rutten
 *
 * @param <T>
 */
public class InternalLinkField<ID extends Serializable, T extends AbstractEntity<ID>> extends CustomField<T> {

	private static final long serialVersionUID = -4586184051577153289L;

	private Button linkButton;

	private EntityModel<T> entityModel;

	private AttributeModel attributeModel;

	/**
	 * 
	 * @param attributeModel
	 */
	public InternalLinkField(EntityModel<T> entityModel, AttributeModel attributeModel) {
		this.entityModel = entityModel;
		this.attributeModel = attributeModel;
	}

	@Override
	protected Component initContent() {

		T t = getValue();
		String str = FormatUtils.formatEntity(attributeModel.getNestedEntityModel(), t);
		linkButton = new Button(str);
		linkButton.setSizeFull();
		linkButton.setStyleName(ValoTheme.BUTTON_LINK);
		linkButton.addClickListener(event -> {
			BaseUI ui = (BaseUI) UI.getCurrent();
			ui.navigateToEntityScreenDirectly(getValue());
		});

		return linkButton;
	}

	@Override
	public Class<? extends T> getType() {
		return entityModel.getEntityClass();
	}

	@Override
	public void setValue(T newValue) {
		super.setValue(newValue);
		String str = FormatUtils.formatEntity(attributeModel.getNestedEntityModel(), newValue);
		linkButton.setCaption(str);
	}

	@Override
	protected void setInternalValue(T newValue) {
		super.setInternalValue(newValue);
		if (linkButton != null) {
			String str = FormatUtils.formatEntity(attributeModel.getNestedEntityModel(), newValue);
			linkButton.setCaption(str);
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		// field is always enabled
		super.setEnabled(true);
	}
}
