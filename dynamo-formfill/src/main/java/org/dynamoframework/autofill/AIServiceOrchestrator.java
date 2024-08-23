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

import lombok.RequiredArgsConstructor;
import org.dynamoframework.exception.OCSRuntimeException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * AI Service orchestrator class, responsible for looking up the correct AI service
 */
@Service
@RequiredArgsConstructor
public class AIServiceOrchestrator {

    private final List<AIService> services;

    /**
     * Executes an AI request
     * @param type the requested service type
     * @param input the textual input (prompt)
     * @param objectMap mapping of property names
     * @param typesMap mapping of property names to type instructions
     * @param componentInstructions additional component instructions
     * @param contextInstructions additional context-wide instructions
     * @return the result of the AI request
     */
    public String execute(AIServiceType type, String input, Map<String, Object> objectMap, Map<String, String> typesMap, Map<String, String> componentInstructions, List<String> contextInstructions) {
        return services.stream().filter(service -> service.supports(type))
                .findFirst().map(service -> service.execute(input, objectMap, typesMap, componentInstructions,
                        contextInstructions)).orElseThrow(() -> new OCSRuntimeException("No suitable AI model has been configured"));
    }

    public List<AIServiceType> findSupportedServices() {
        return Arrays.stream(AIServiceType.values())
                .filter(type -> services.stream().anyMatch(service -> service.supports(type)))
                .toList();
    }
}
