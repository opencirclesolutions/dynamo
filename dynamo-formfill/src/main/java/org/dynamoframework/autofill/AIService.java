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

import java.util.List;
import java.util.Map;

/**
 * Interface for services that call an AI service/LLM
 */
public interface AIService {

    /**
     * @param type the AI service type
     * @return whether the service supports the specified service type
     */
    boolean supports(AIServiceType type);

    /**
     * Executes a request
     *
     * @param input                 the user input
     * @param objectMap             mapping of fields to include
     * @param typesMap              mapping of field name to data type
     * @param componentInstructions instructions per component
     * @param contextInstructions   general instructions
     * @return a string containing the resulting JSON object
     */
    String execute(String input, Map<String, Object> objectMap, Map<String, String> typesMap, Map<String, String> componentInstructions, List<String> contextInstructions);

    /**
     * Appends the additional instructions to an LLM request
     * @param request the LLM request (prompt)
     * @param typesMap type mapping
     * @param componentInstructions additional instructions per field
     * @param contextInstructions additional context
     */
    default void appendInstructions(StringBuilder request, Map<String, String> typesMap, Map<String, String> componentInstructions, List<String> contextInstructions) {
        if (!componentInstructions.isEmpty() || !typesMap.isEmpty()) {
            request.append("\nAdditional instructions about some of the JSON fields to be filled: ");
            for (Map.Entry<String, String> entry : typesMap.entrySet()) {
                request.append("\n").append(entry.getKey()).append(": Format this JSON field as ").append(entry.getValue()).append(".");
            }
            for (Map.Entry<String, String> entry : componentInstructions.entrySet()) {
                request.append("\n").append(entry.getKey()).append(": ").append(entry.getValue()).append(".");
            }
            if (!contextInstructions.isEmpty()) {
                request.append("\nAdditional instructions about the context and desired JSON output response:  ");
                for (String contextInstruction : contextInstructions) {
                    request.append(" ").append(contextInstruction).append(".");
                }
            }
        }
    }

    /**
     * Creates the basic LLM request (prompt)
     * @param input the input
     * @param objectMap mapping of the individual fields
     * @return the request
     */
    default StringBuilder createRequest(String input, Map<String, Object> objectMap) {
        return new StringBuilder(String.format(
                "Based on the user input: \n \"%s\", " +
                        "generate a JSON object according to these instructions: " +
                        "Never include duplicate keys, in case of duplicate keys just keep the first occurrence in the response. " +
                        "Leave out the JSON value if the user did not specify a value. " +
                        "Return only a valid JSON object in this format: '%s'."
                , input, objectMap));
    }
}
