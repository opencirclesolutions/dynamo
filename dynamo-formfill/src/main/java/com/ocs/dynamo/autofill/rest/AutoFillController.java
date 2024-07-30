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
package com.ocs.dynamo.autofill.rest;

import com.ocs.dynamo.autofill.FormFillService;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.rest.BaseController;
import com.ocs.dynamo.autofill.rest.model.AutoFillOptions;
import com.ocs.dynamo.autofill.rest.model.AutoFillRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * A controller that offers endpoints for the form-fill functionality
 */
@RestController
@RequestMapping(value = "/autofill")
@Slf4j
@CrossOrigin
public class AutoFillController extends BaseController {

    @Value("${ocs.ai.default.service:CHAT_GPT}")
    private String defaultValue;

    @Autowired
    private FormFillService formFillService;

    /**
     * Makes a request to automatically fill a form
     * @param entityName the name of the entity for which to fill a form
     * @param request request containing the input and desired AI service
     * @param reference optional reference to further specify the entity model
     * @return an object containing the fields that could be automatically filled
     */
    @PostMapping(value = "/{entityName}", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Object autoFill(@PathVariable("entityName") String entityName, @RequestBody
           @Valid AutoFillRequest request, @RequestParam(required = false) String reference) {

        Class<Object> clazz = findClass(entityName);
        EntityModel<?> entityModel = findEntityModel(reference, clazz);

        return formFillService.autoFillForm(entityModel, request);
    }

    /**
     * @return the list of available AI services
     */
    @GetMapping(value = "/options", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.ALL_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<AutoFillOptions> getOptions() {
        List<AutoFillOptions> list = formFillService.findSupportedServices()
                .stream().map(type -> AutoFillOptions.builder()
                        .type(type).description(type.toString()).build())
                .toList();

        list.stream().filter(option -> option.getType().name().equalsIgnoreCase(defaultValue))
                .forEach(option -> option.setDefaultValue(true));

        return list;
    }
}
