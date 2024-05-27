package com.ocs.dynamo.ui.composite.autofill;

import com.vaadin.flow.component.Component;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@ConditionalOnProperty(value = "ocs.ollama.enabled", havingValue = "true")
@RequiredArgsConstructor
public class OllamaService implements AIService {

    private OllamaChatClient client;

    @Value("${ocs.ollama.model}")
    private String model;

    @Value("${ocs.ollama.url}")
    private String url;

    @PostConstruct
    public void init() {
        var ollamaApi = new OllamaApi(url);
        client = new OllamaChatClient(ollamaApi)
                .withDefaultOptions(OllamaOptions.create()
                        .withModel(model)
                        .withTemperature(0.9f));
    }

    @Override
    public boolean supports(AIServiceType type) {
        return AIServiceType.OLLAMA.equals(type);
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
        appendInstructions(request, typesMap, componentInstructions, contextInstructions);

        log.debug("AI request {}", request);

        ChatResponse call = client.call(new Prompt(request.toString()));
        return call.getResult().getOutput().getContent();
    }
}
