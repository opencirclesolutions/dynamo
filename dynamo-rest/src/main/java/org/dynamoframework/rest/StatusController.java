package org.dynamoframework.rest;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dynamoframework.rest.model.StatusResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A simple controller for returning an "OK" when the application is running
 */
@RestController
@RequestMapping(value = "#{@'dynamoframework-org.dynamoframework.configuration.DynamoConfigurationProperties'.defaults.endpoints.status}")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "Status", description = "Dynamo status controller")
public class StatusController {

	@GetMapping
	@Operation(summary = "Get the status")
	StatusResponse getStatus() {
		return StatusResponse.builder()
			.status("OK")
			.build();
	}
}
