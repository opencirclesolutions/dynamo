package org.dynamoframework.filter;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.dynamoframework.domain.model.AttributeModel;

/**
 * Flexible filter definitions for use with a flexible search layout
 *
 * @author bas.rutten
 */
@Getter
@Builder(toBuilder = true)
@ToString
public class FlexibleFilterDefinition {

	private AttributeModel attributeModel;

	private FlexibleFilterType flexibleFilterType;

	private Object value;

	private Object valueTo;


}
