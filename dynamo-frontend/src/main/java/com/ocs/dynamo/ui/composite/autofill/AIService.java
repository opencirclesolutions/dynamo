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

import com.vaadin.flow.component.Component;

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
     * @param input the user input
     * @param objectMap mapping of fields to include
     * @param typesMap mapping of field name to data type
     * @param componentInstructions instructions per component
     * @param contextInstructions general instructions
     * @return a string containing the resulting JSON object
     */
    String execute(String input, Map<String, Object> objectMap, Map<String, String> typesMap, Map<Component, String> componentInstructions, List<String> contextInstructions);

}
