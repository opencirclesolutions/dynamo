package org.dynamoframework.autofill;

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

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dynamoframework.configuration.DynamoProperties;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@ConditionalOnProperty(value = "dynamoframework.ollama.enabled", havingValue = "true")
@RequiredArgsConstructor
public class OllamaService implements AIService {

	private OllamaChatClient client;

	@Autowired
	private DynamoProperties dynamoProperties;

	@PostConstruct
	public void init() {
		var ollamaApi = new OllamaApi(dynamoProperties.getOllama().getUrl());
		client = new OllamaChatClient(ollamaApi)
			.withDefaultOptions(OllamaOptions.create()
				.withModel(dynamoProperties.getOllama().getModel())
				.withTemperature(0.9f));
	}

	@Override
	public boolean supports(AIServiceType type) {
		return AIServiceType.OLLAMA.equals(type);
	}

	@Override
	public String execute(String input, Map<String, Object> objectMap, Map<String, String> typesMap, Map<String, String> componentInstructions, List<String> contextInstructions) {
		StringBuilder request = createRequest(input, objectMap);
		appendInstructions(request, typesMap, componentInstructions, contextInstructions);

		log.debug("AI request {}", request);

		ChatResponse call = client.call(new Prompt(request.toString()));
		return call.getResult().getOutput().getContent();
	}
}
