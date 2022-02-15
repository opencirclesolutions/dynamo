package com.ocs.dynamo.ui.component;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomFieldContext {

	private EntityModel<?> entityModel;

	private AttributeModel attributeModel;

	private boolean viewMode;

	private boolean searchMode;
}
