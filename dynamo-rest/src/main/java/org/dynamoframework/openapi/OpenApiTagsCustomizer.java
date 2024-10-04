package org.dynamoframework.openapi;

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

import io.swagger.v3.core.filter.SpecFilter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dynamoframework.constants.DynamoConstants;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Customizer for modifying the OpenAPI generation
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpenApiTagsCustomizer extends SpecFilter implements OpenApiCustomizer {

	@Override
	public void customise(OpenAPI openApi) {
		modifySchemas(openApi);
		modifyContentTypes(openApi);
	}

	/**
	 * Modifies the content types that are specified as any type (which causes
	 * requests to be returned as BLOB rather than JSON) by changing them to
	 * "application/json"
	 *
	 * @param openApi the OpenAPI object
	 */
	private void modifyContentTypes(OpenAPI openApi) {
		openApi.getPaths().forEach((key, value) -> {
			if (value != null && value.getGet() != null) {
				ApiResponses responses = value.getGet().getResponses();

				// add JSON mapping and throw away catch-all mapping
				responses.forEach((nestedKey, nestedValue) -> {
					Content content = nestedValue.getContent();
					if (content != null) {
						MediaType mediaType = content.get("*/*");
						content.put("application/json", mediaType);
						content.remove("*/*");
					}
				});
			}
		});
	}

	/**
	 * Modifies the "Schemas" (basically the request and response bodies)
	 *
	 * @param openApi the open API definitions
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private void modifySchemas(OpenAPI openApi) {
		openApi.getComponents().getSchemas()
			.forEach((schemaName, schema) -> {
				log.info("Modifying schema: {}", schemaName);

				Map<String, Schema> properties = schema.getProperties();
				if (properties != null) {
					// never include version properties
					properties.remove(DynamoConstants.VERSION);
				}
			});
	}
}
