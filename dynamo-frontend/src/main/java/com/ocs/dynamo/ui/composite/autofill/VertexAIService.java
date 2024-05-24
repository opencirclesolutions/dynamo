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
package com.ocs.dynamo.ui.composite.autofill;

import com.google.cloud.vertexai.VertexAI;
import com.vaadin.flow.component.Component;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatClient;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "ocs.vertexai.enabled", havingValue = "true")
public class VertexAIService implements AIService {

    @Value("${ocs.vertexai.project.id}")
    private String projectId;

    @Value("${ocs.vertexai.project.region}")
    private String region;

    @Value("${ocs.vertexai.model}")
    private String model;

    private VertexAiGeminiChatClient client;

    @PostConstruct
    public void init() {
        VertexAI vertexApi = new VertexAI(projectId, region);
        client = new VertexAiGeminiChatClient(vertexApi,
                VertexAiGeminiChatOptions.builder()
                        .withTemperature(0.4f)
                        .withModel(model)
                        .build());
    }

    @Override
    public boolean supports(AIServiceType type) {
        return AIServiceType.VERTEX_AI.equals(type);
    }

    @Override
    public String execute(String input, Map<String, Object> objectMap, Map<String, String> typesMap, Map<Component, String> componentInstructions, List<String> contextInstructions) {
        StringBuilder request = new StringBuilder(String.format(
                "Based on the user input: \n \"%s\", " +
                        "generate a JSON object according to these instructions: " +
                        "Never include duplicate keys, in case of duplicate keys just keep the first occurrence in the response. " +
                        "Fill out \"N/A\" in the JSON value if the user did not specify a value. " +
                        "Return only a valid JSON object in this format: '%s'."
                , input, objectMap));
        if (!componentInstructions.isEmpty() || !typesMap.isEmpty()) {
            request.append("\nAdditional instructions about some of the JSON fields to be filled: ");
            for (Map.Entry<String, String> entry : typesMap.entrySet()) {
                request.append("\n").append(entry.getKey()).append(": Format this JSON field as ").append(entry.getValue()).append(".");
            }
            for (Map.Entry<Component, String> entry : componentInstructions.entrySet()) {
                if (entry.getKey().getId().isPresent())
                    request.append("\n").append(entry.getKey().getId().get()).append(": ").append(entry.getValue()).append(".");
            }
            if (!contextInstructions.isEmpty()) {
                request.append("\nAdditional instructions about the context and desired JSON output response:  ");
                for (String contextInstruction : contextInstructions) {
                    request.append(" ").append(contextInstruction).append(".");
                }
            }
        }

        log.debug("AI request {}", request);

        ChatResponse call = client.call(new Prompt(request.toString()));
        return call.getResult().getOutput().getContent();
    }
}
