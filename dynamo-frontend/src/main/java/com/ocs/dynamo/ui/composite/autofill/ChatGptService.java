package com.ocs.dynamo.ui.composite.autofill;

import com.vaadin.flow.component.Component;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@ConditionalOnProperty(value = "ocs.openai.enabled", havingValue = "true")
public class ChatGptService implements AIService {

    private OpenAiChatClient client;

    @Value("${ocs.openai.api.key}")
    private String apiKey;

    @Value("${ocs.openai.model}")
    private String model;

    @Value("${ocs.openai.maxtokens}")
    private Integer maxTokens;

    @PostConstruct
    public void init() {
        var openAiApi = new OpenAiApi(apiKey);
        client = new OpenAiChatClient(openAiApi,
                OpenAiChatOptions.builder()
                        .withModel(model)
                        .withMaxTokens(maxTokens)
                        .build());
    }

    @Override
    public boolean supports(AIServiceType type) {
        return type.equals(AIServiceType.CHAT_GPT);
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
