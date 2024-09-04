package org.dynamoframework.export.rest;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dynamoframework.dao.FetchJoinInformation;
import org.dynamoframework.dao.SortOrders;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.domain.model.EntityModel;
import org.dynamoframework.domain.model.EntityModelFactory;
import org.dynamoframework.exception.OCSValidationException;
import org.dynamoframework.exception.OcsNotFoundException;
import org.dynamoframework.export.CustomXlsStyleGenerator;
import org.dynamoframework.export.ExportService;
import org.dynamoframework.export.type.ExportMode;
import org.dynamoframework.filter.And;
import org.dynamoframework.rest.crud.SearchService;
import org.dynamoframework.rest.crud.search.SearchModel;
import org.dynamoframework.utils.ClassUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@RestController
@RequestMapping(value = "#{@'dynamoframework-org.dynamoframework.configuration.DynamoConfigurationProperties'.defaults.endpoints.export}")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(exposedHeaders = {"Content-Type", "Content-Disposition"})
@Tag(name = "Export", description = "Dynamo export controller")
public class ExportController {

    private final EntityModelFactory entityModelFactory;

    private final ExportService exportService;

    private final SearchService searchService;

    private final CustomGeneratorService customGeneratorService;

    /**
     * Exports data to Excel
     *
     * @param entityName  the name of the entity to export
     * @param reference   optional entity model reference
     * @param searchModel the search model used to restrict the entries
     * @param locale      the locale
     * @param exportMode  the export mode
     * @param <ID>
     * @param <T>
     * @return the binary representation of the export
     */
    @PostMapping(path = "/excel/{entityName}")
    @SuppressWarnings("unchecked")
    @Operation(summary = "Exports data to Excel")
    public <ID extends Serializable, T extends AbstractEntity<ID>> ResponseEntity<Resource> exportExcel(
            @PathVariable("entityName") @Parameter(description = "The name of the entity to export") String entityName,
            @RequestParam(required = false) @Parameter(description = "The entity model reference") String reference,
            @RequestBody @Valid @Parameter(description = "The search model used to restrict the entries") SearchModel searchModel,
            @RequestParam(required = false, defaultValue = "en") @Parameter(description = "The locale to use") String locale,
            @RequestParam @Parameter(description = "The export mode") ExportMode exportMode) {

        EntityModel<T> entityModel = findEntityModel(entityName, reference);
        if (!entityModel.isExportAllowed()) {
            throw new OCSValidationException("Exporting this entity is not allowed");
        }

        And filter = searchService.createFilter(searchModel, entityModel);
        SortOrders sortOrders = searchService.createSortOrders(searchModel, entityModel);

        Locale loc = new Locale.Builder().setLanguage(locale).build();

        CustomXlsStyleGenerator<ID, T> customGenerator = (CustomXlsStyleGenerator<ID, T>) customGeneratorService.getCustomGenerator(entityModel.getEntityClass(), reference);
        byte[] bytes = exportService.exportExcel(entityModel, exportMode,
                filter, sortOrders.getOrders(), customGenerator == null ? null : () -> customGenerator, new Locale.Builder().setLanguage(locale).build(),
                entityModel.getFetchJoins().toArray(new FetchJoinInformation[0]));

        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(bytes));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=%s".formatted(
                        generateFileName(entityModel, loc, reference, "xlsx")
                ))
                .contentLength(bytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping(path = "/csv/{entityName}")
    @Operation(summary = "Exports data to csv")
    public <ID extends Serializable, T extends AbstractEntity<ID>> ResponseEntity<Resource> exportCsv(
            @PathVariable("entityName") @Parameter(description = "The name of the entity to export") String entityName,
            @RequestParam(required = false) @Parameter(description = "The entity model reference") String reference,
            @RequestBody @Valid @Parameter(description = "The search model used to restrict the entries") SearchModel searchModel,
            @RequestParam(required = false, defaultValue = "en") @Parameter(description = "The locale to use") String locale,
            @RequestParam @Parameter(description = "The export mode") ExportMode exportMode) {

        EntityModel<T> entityModel = findEntityModel(entityName, reference);
        if (!entityModel.isExportAllowed()) {
            throw new OCSValidationException("Exporting this entity is not allowed");
        }

        And filter = searchService.createFilter(searchModel, entityModel);
        SortOrders sortOrders = searchService.createSortOrders(searchModel, entityModel);

        Locale loc = new Locale.Builder().setLanguage(locale).build();
        byte[] bytes = exportService.exportCsv(entityModel, exportMode,
                filter, sortOrders.getOrders(), loc,
                entityModel.getFetchJoins().toArray(new FetchJoinInformation[0]));

        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(bytes));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=%s".formatted(
                        generateFileName(entityModel, loc, reference, "csv")
                ))
                .contentLength(bytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private <ID extends Serializable, T extends AbstractEntity<ID>> String generateFileName(EntityModel<T> entityModel,
                                                                                            Locale locale, String reference, String extension) {
        StringBuilder builder = new StringBuilder(entityModel.getDisplayNamePlural(locale)).append("_");
        if (!StringUtils.isEmpty(reference)) {
            builder.append(reference).append("_");
        }
        builder.append(Instant.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")));
        builder.append(".").append(extension);

        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private <ID, T extends AbstractEntity<ID>> EntityModel<T> findEntityModel(String entityName, String reference) {
        Class<T> clazz = (Class<T>) ClassUtils.findClass(entityName);
        if (clazz == null) {
            throw new OcsNotFoundException("Entity for %s could not be found".formatted(entityName));
        }

        EntityModel<T> model;
        if (!StringUtils.isEmpty((reference))) {
            model = entityModelFactory.getModel(reference, clazz);
        } else {
            model = entityModelFactory.getModel(clazz);
        }

        if (model == null) {
            throw new OcsNotFoundException("Entity Model for %s could not be found".formatted(entityName));
        }

        return model;
    }


}
