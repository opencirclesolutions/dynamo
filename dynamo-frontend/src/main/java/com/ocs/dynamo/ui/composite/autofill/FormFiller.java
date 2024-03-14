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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocs.dynamo.domain.model.EntityModel;
import com.vaadin.flow.ai.formfiller.services.ChatGPTChatCompletionService;
import com.vaadin.flow.ai.formfiller.services.LLMService;
import com.vaadin.flow.component.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A service that tries to fill a form based on natural language input using an AI service
 */
@Slf4j
public class FormFiller {

    /**
     * The target component to fill.
     */
    private final Component target;

    /**
     * Additional instructions for the AI module (i.e.: field format,
     * field context, etc...).
     * Use these instructions to provide additional information to the AI module about a specific field
     * when the response of the form filler is not accurate enough.
     * <p>
     * Use the target component as key and the instruction as value.
     */
    private final Map<Component, String> componentInstructions;

    /**
     * Additional instructions for the AI module (i.e.: target language, vocabulary explanation, etc.).
     * Use these instructions to provide additional information to the AI module about the context of the
     * input source in general.<br>
     * Be careful to add inconsistent instructions, as the AI module will try to find a response that
     * matches all the instructions.
     * <p>
     * Use the instruction as value.
     */
    private final List<String> contextInstructions;

    /**
     * The JSON representation of the target components to fill.
     * Includes hierarchy map and value types of the components
     */
    FormFillUtils.ComponentsMapping mapping;

    /**
     * The AI module service to use.
     */
    private final LLMService llmService;

    /**
     * Creates a FormFiller to fill the target component or
     * group of components (layout) and includes additional
     * instructions for the AI module (i.e.: field format,
     * field context, etc...)
     *
     * @param target                the target component or group of components (layout)
     *                              to fill
     * @param componentInstructions additional instructions for the AI module (i.e.: field format, field explanation, etc...).
     *                              Use these instructions to provide additional information to the AI module about a specific field
     *                              when the response of the form filler is not accurate enough.
     * @param contextInstructions   additional instructions for the AI module (i.e.: target language, vocabulary explanation, etc.).
     *                              Use these instructions to provide additional information to the AI module about the context of the
     *                              input source in general.
     * @param llmService            the AI module service to use. By default, this service would use OpenAI ChatGPT.
     */
    public FormFiller(Component target, Map<Component, String> componentInstructions, List<String> contextInstructions, LLMService llmService) {
        this.llmService = llmService;
        this.target = target;
        this.componentInstructions = componentInstructions;
        this.contextInstructions = contextInstructions;
    }

    /**
     * Creates a FormFiller with default llmService.
     * Check {@link #FormFiller(Component, Map, List, LLMService) FormFiller} for more information.
     */
    public FormFiller(Component target, Map<Component, String> componentInstructions, List<String> contextInstructions) {
        this(target, componentInstructions, contextInstructions, new ChatGPTChatCompletionService());
    }

    /**
     * Creates a FormFiller with default llmService and empty context instructions.
     * Check {@link #FormFiller(Component, Map, List, LLMService) FormFiller} for more information.
     */
    public FormFiller(Component target, Map<Component, String> componentInstructions) {
        this(target, componentInstructions, new ArrayList<>(), new ChatGPTChatCompletionService());
    }

    /**
     * Creates a FormFiller with default llmService and empty field instructions.
     * Check {@link #FormFiller(Component, Map, List, LLMService) FormFiller} for more information.
     */
    public FormFiller(Component target, List<String> contextInstructions) {
        this(target, new HashMap<>(), contextInstructions, new ChatGPTChatCompletionService());
    }

    /**
     * Creates a FormFiller with default llmService, empty field instructions and empty context instructions.
     * Check {@link #FormFiller(Component, Map, List, LLMService) FormFiller} for more information.
     */
    public FormFiller(Component target) {
        this(target, new HashMap<>(), new ArrayList<>(), new ChatGPTChatCompletionService());
    }

    /**
     * Creates a FormFiller with empty context instructions and empty context instructions with the given llmService.
     * Check {@link #FormFiller(Component, Map, List, LLMService) FormFiller} for more information.
     */
    public FormFiller(Component target, LLMService llmService) {
        this(target, new HashMap<>(), new ArrayList<>(), llmService);
    }

    /**
     * Fills automatically the target component analyzing the input source
     *
     * @param input Text input to send to the AI module
     * @return result of the query including request and response
     */
    public FormFillerResult fill(String input, EntityModel<?> entityModel) {

        mapping = FormFillUtils.createMapping(target, entityModel);
        String prompt = llmService.getPromptTemplate(input, mapping.componentsJSONMap(), mapping.componentsTypesJSONMap(), componentInstructions, contextInstructions);
        log.debug("Generated Prompt: {}", prompt);

        String aiResponse = llmService.getGeneratedResponse(prompt);
        FormFillUtils.fillComponents(mapping.componentInfoList(), promptJsonToMapHierarchyValues(aiResponse),
                entityModel);

        log.debug("AI response: " + aiResponse.trim());

        return new FormFillerResult(prompt, aiResponse);
    }

    /**
     * Transforms the response of the AI module (should be a valid JSON)
     * to a map with the hierarchy of the components and its values.
     *
     * @param response response prompt from the AI module
     * @return Map with components and values
     */
    private Map<String, Object> promptJsonToMapHierarchyValues(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        Map<String, Object> contentMap = new HashMap<>();
        try {
            response = correctResponse(response);
            log.info(response);
            contentMap = objectMapper.readValue(response.trim(), new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Error parsing AI response to JSON Object: {}", e.getMessage());
        }
        return contentMap;
    }

    /**
     * Make any corrections to the raw JSON response
     * <p>
     * Notably, this includes removing an extra trailing comma at the end of the response
     *
     * @param response the raw response
     * @return the corrected response
     */
    private String correctResponse(String response) {
        return response.replaceAll(",\\R}", "}");
    }

    public Map<Component, String> getComponentInstructions() {
        return componentInstructions;
    }

    public List<String> getContextInstructions() {
        return contextInstructions;
    }

    public FormFillUtils.ComponentsMapping getMapping() {
        return mapping;
    }
}
