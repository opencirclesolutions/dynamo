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

import com.google.cloud.vertexai.VertexAI;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dynamoframework.configuration.DynamoProperties;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatClient;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "dynamoframework.vertexai.enabled", havingValue = "true")
public class VertexAIService implements AIService {


	@Autowired
	private DynamoProperties dynamoProperties;

	private VertexAiGeminiChatClient client;

	@PostConstruct
	public void init() {
		VertexAI vertexApi = new VertexAI(dynamoProperties.getVertexai().getProjectId(), dynamoProperties.getVertexai().getProjectRegion());
		client = new VertexAiGeminiChatClient(vertexApi,
			VertexAiGeminiChatOptions.builder()
				.withTemperature(0.4f)
				.withModel(dynamoProperties.getVertexai().getModel())
				.build());
	}

	@Override
	public boolean supports(AIServiceType type) {
		return AIServiceType.VERTEX_AI.equals(type);
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
