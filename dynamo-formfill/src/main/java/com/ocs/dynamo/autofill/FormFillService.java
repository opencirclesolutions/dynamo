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
package com.ocs.dynamo.autofill;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocs.dynamo.autofill.mapper.FormFillMapper;
import com.ocs.dynamo.autofill.mapper.FormFillRecord;
import com.ocs.dynamo.autofill.rest.model.AutoFillRequest;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FormFillService {

    private final AIServiceOrchestrator orchestrator;

    private final List<FormFillMapper> formFillMappers;

    public List<AIServiceType> findSupportedServices() {
        return orchestrator.findSupportedServices();
    }

    /**
     * @param entityModel the entity model
     * @param request     the request
     * @return the result
     */
    public Map<String, Object> autoFillForm(EntityModel<?> entityModel, AutoFillRequest request) {

        List<AttributeModel> attributeModels = filterAttributeModels(entityModel);

        Map<String, Object> objectMap = new HashMap<>();
        Map<String, String> typeMap = new HashMap<>();
        Map<String, String> componentInstructions =
                attributeModels.stream().filter(am -> !StringUtils.isEmpty(am.getAutofillInstructions()))
                        .collect(Collectors.toMap(am -> am.getName(), am -> am.getAutofillInstructions()));

        List<String> contextInstructions = new ArrayList<>();

        if (!StringUtils.isEmpty(entityModel.getAutofillInstructions())) {
            contextInstructions.add(entityModel.getAutofillInstructions());
        }
        if (!StringUtils.isEmpty(request.getAdditionalInstructions())) {
            contextInstructions.add(request.getAdditionalInstructions());
        }

        attributeModels.stream()
                .map(attributeModel -> toFormFillInstructions(attributeModel))
                .filter(Objects::nonNull)
                .forEach(record -> {
                    typeMap.put(record.model().getName(), record.typeInfo());
                    objectMap.put(record.model().getName(), "");
                });

        // process nested attributes (one level deep)
        attributeModels.stream()
                .filter(attributeModel -> attributeModel.getAttributeType() == AttributeType.DETAIL
                        && attributeModel.isNestedDetails())
                .flatMap(attributeModel -> filterAttributeModels(attributeModel.getNestedEntityModel()).stream()
                        .map(nestedAm -> toFormFillInstructions(nestedAm)))
                .filter(Objects::nonNull)
                .forEach(record -> {
                    typeMap.put(record.model().getName(), record.typeInfo());
                    objectMap.put(record.model().getName(), "");
                });

        String response = orchestrator.execute(request.getType(),
                request.getInput(), objectMap, typeMap, componentInstructions, contextInstructions);
        log.info(response);

        Map<String, Object> tree = mapToTree(response);
        convertTree(tree, attributeModels);

        return tree;
    }

    /**
     * Filters the list of attribute models to only include those that appear inside the edit
     * form and can be edited
     *
     * @param model the entity model
     * @return the list of applicable entity models
     */
    private List<AttributeModel> filterAttributeModels(EntityModel<?> model) {
        return model.getAttributeModels().stream().filter(
                am -> am.isVisibleInForm() && (am.getEditableType() == EditableType.EDITABLE || am.getEditableType() == EditableType.CREATE_ONLY)
        ).toList();
    }

    /**
     * Maps an attribute model to the form fill instructions
     *
     * @param model the attribute model to map
     * @return the resulting form fill instructions
     */
    private FormFillRecord toFormFillInstructions(AttributeModel model) {
        return this.formFillMappers.stream().filter(mapper -> mapper.supports(model))
                .findFirst()
                .map(mapper -> mapper.map(model))
                .orElse(null);
    }

    /**
     * Converts the JSON returned by the AI service to an object structure understood by Dynamo
     * <p>
     * For the most part, no mapping is needed, but in some cases e.g. entity fields, we perform
     * a database lookup in order to translate the value returned by the LLM
     *
     * @param tree            the JSON tree structure
     * @param attributeModels the list of attribute models
     */
    private void convertTree(Map<String, Object> tree, List<AttributeModel> attributeModels) {
        attributeModels.forEach(model -> {

            Object value = tree.get(model.getName());
            if (value != null) {
                if (model.getAttributeType() == AttributeType.DETAIL && model.isNestedDetails()) {
                    List<AttributeModel> nestedModels = filterAttributeModels(model.getNestedEntityModel());
                    if (value instanceof List list) {
                        for (Object listEntry : list) {
                            if (listEntry instanceof Map) {
                                Map<String, Object> entry = (Map<String, Object>) listEntry;
                                convertTree(entry, nestedModels);
                            }
                        }
                    }
                } else {
                    Object mappedValue = mapBack(model, value);
                    tree.put(model.getName(), mappedValue);
                }
            }
        });
    }

    /**
     * Maps a value from the LLM request to
     * @param model the attribute model to base the conversion on
     * @param value the value to convert
     * @return the result of the conversion
     */
    private Object mapBack(AttributeModel model, Object value) {
        return this.formFillMappers.stream().filter(mapper -> mapper.supports(model))
                .findFirst()
                .map(mapper -> mapper.mapBack(model, value))
                .orElse(null);
    }

    /**
     * Transforms the response of the AI module (should be a valid JSON)
     * to a map with the hierarchy of the components and its values.
     *
     * @param response response prompt from the AI module
     * @return Map with components and values
     */
    private Map<String, Object> mapToTree(String response) {
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
     * Cleans up any faulty data in the JSON returned by the LLM service
     * @param response the result to clean up
     * @return
     */
    private String correctResponse(String response) {
        // strip away all nonsense in front of the JSON
        int p = response.indexOf('{');
        if (p > 0) {
            response = response.substring(p);
        }

        response = response.replaceAll(",\\R}", "}");
        response = response.replaceAll("```json", "");
        response = response.replaceAll("```", "");
        return response;
    }
}
