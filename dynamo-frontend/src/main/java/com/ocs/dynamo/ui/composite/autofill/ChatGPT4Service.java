package com.ocs.dynamo.ui.composite.autofill;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import com.vaadin.flow.ai.formfiller.services.LLMService;
import com.vaadin.flow.ai.formfiller.utils.KeysUtils;
import com.vaadin.flow.component.Component;

public class ChatGPT4Service extends OpenAiService implements LLMService {

    /**
     * ID of the model to use.
     */
    private String MODEL = "gpt-4-turbo";
    /**
     * The maximum number of tokens to generate in the completion.
     */
    private Integer MAX_TOKENS = 4096;

    /**
     * What sampling temperature to use, between 0 and 2.
     * Higher values like 0.8 will make the output more random,
     * while lower values like 0.2 will make it more focused and deterministic.
     */
    private Double TEMPERATURE = 0d;

    /**
     * If true the input prompt is included in the response
     */
    private Boolean ECHO = false;

    /**
     * Timeout for AI module response in seconds
     */
    private static Integer TIMEOUT = 60;

    public ChatGPT4Service() {
        super(KeysUtils.getOpenAiKey(), Duration.ofSeconds(TIMEOUT));
    }

    @Override
    public String getPromptTemplate(String input, Map<String, Object> objectMap, Map<String, String> typesMap, Map<Component, String> componentInstructions, List<String> contextInstructions) {
        String gptRequest = String.format(
                "Based on the user input: \n \"%s\", " +
                        "generate a JSON object according to these instructions: " +
                        "Never include duplicate keys, in case of duplicate keys just keep the first occurrence in the response. " +
                        "Fill out \"N/A\" in the JSON value if the user did not specify a value. " +
                        "Return the result as a JSON object in this format: '%s'."
                , input, objectMap);
        if (!componentInstructions.isEmpty() || !typesMap.isEmpty()) {
            gptRequest += "\nAdditional instructions about some of the JSON fields to be filled: ";
            for (Map.Entry<String, String> entry : typesMap.entrySet()) {
                gptRequest += "\n" + entry.getKey() + ": Format this JSON field as " + entry.getValue() + ".";
            }
            for (Map.Entry<Component, String> entry : componentInstructions.entrySet()) {
                if (entry.getKey().getId().isPresent())
                    gptRequest += "\n" + entry.getKey().getId().get() + ": " + entry.getValue() + ".";
            }
            if (!contextInstructions.isEmpty()) {
                gptRequest += "\nAdditional instructions about the context and desired JSON output response:  ";
                for (String contextInstruction : contextInstructions) {
                    gptRequest += " " + contextInstruction + ".";
                }
            }
        }
        return gptRequest;
    }

    @Override
    public String getGeneratedResponse(String prompt) {

        ArrayList<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user",prompt));
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .messages(messages)
                .model(MODEL).maxTokens(MAX_TOKENS).temperature(TEMPERATURE)
                .build();

        ChatCompletionResult completionResult = createChatCompletion(chatCompletionRequest);
        String aiResponse = completionResult.getChoices().get(0).getMessage().getContent();
        return aiResponse;
    }
}

