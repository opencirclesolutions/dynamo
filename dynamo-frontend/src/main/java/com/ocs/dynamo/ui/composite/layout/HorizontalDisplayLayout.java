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
package com.ocs.dynamo.ui.composite.layout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.componentfactory.EnhancedFormLayout;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Span;

import lombok.Getter;
import lombok.Setter;

/**
 * A simple horizontal layout for read-only display of attributes (i.e. for a
 * header bar)
 * 
 * @author bas.rutten
 * @param <ID> the type of the primary key of the entity
 * @param <T>  the type of the entity being displayed
 */
public class HorizontalDisplayLayout<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseServiceCustomComponent<ID, T> {

	private static final long serialVersionUID = -2610435729199505546L;

	private final T entity;

	@Getter
	@Setter
	private List<String> columnThresholds = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param service     the service used to query the database
	 * @param entityModel the entity model of the entity to display
	 * @param entity      the entity to display
	 */
	public HorizontalDisplayLayout(BaseService<ID, T> service, EntityModel<T> entityModel, T entity) {
		super(service, entityModel, new FormOptions());
		addClassName(DynamoConstants.CSS_HORIZONTAL_DISPLAY_LAYOUT);
		this.entity = entity;
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		build();
	}

	@Override
	public void build() {
		EnhancedFormLayout layout = new EnhancedFormLayout();

		if (columnThresholds == null || columnThresholds.isEmpty()) {
			layout.setResponsiveSteps(List.of(new ResponsiveStep("0px", 1), new ResponsiveStep("300px", 2),
					new ResponsiveStep("600px", 3)));
		} else {
			List<ResponsiveStep> steps = new ArrayList<>();
			for (int i = 0; i < columnThresholds.size(); i++) {
				steps.add(new ResponsiveStep(columnThresholds.get(i), i + 1));
			}
			layout.setResponsiveSteps(steps);
		}

		for (AttributeModel attributeModel : getEntityModel().getAttributeModels()) {
			if (attributeModel.isVisible() && AttributeType.BASIC.equals(attributeModel.getAttributeType())) {
				Span label = constructLabel(entity, attributeModel);
				layout.addFormItem(label, attributeModel.getDisplayName(VaadinUtils.getLocale()));
			}
		}
		add(layout);
	}

}
