package org.dynamoframework.rest.model;

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

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;
import org.dynamoframework.domain.model.EntityModelActionType;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@Jacksonized
public class EntityModelActionResponse {

	@NotNull
	private String id;

	@NotNull
	private Map<String, String> displayNames;

	@NotNull
	private List<AttributeModelResponse> attributeModels;

	@NotNull
	private EntityModelActionType type;

	private String icon;

	private List<String> roles;
}
