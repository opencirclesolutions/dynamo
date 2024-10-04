package org.dynamoframework.export.rest;

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

import org.dynamoframework.export.CustomXlsStyleGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * A service for providing custom Excel style generators
 */
@Service
public class CustomGeneratorService {

	@Autowired(required = false)
	private List<CustomGeneratorProvider> providers;

	public CustomXlsStyleGenerator getCustomGenerator(Class<?> entityClass, String reference) {
		if (providers == null || providers.isEmpty()) {
			return null;
		}

		Optional<CustomGeneratorProvider> first = providers.stream()
			.filter(provider -> provider.matches(entityClass, reference))
			.findFirst();
		return first.map(gen -> gen.getGenerator()).orElse(null);
	}
}
