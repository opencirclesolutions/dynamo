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

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.vaadin.flow.component.Component;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIServiceOrchestrator {

    private final List<AIService> services;

    public String execute(AIServiceType type, String input, Map<String, Object> objectMap, Map<String, String> typesMap, Map<Component, String> componentInstructions, List<String> contextInstructions) {
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
