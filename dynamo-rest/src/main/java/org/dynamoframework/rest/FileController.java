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
package org.dynamoframework.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.dynamoframework.constants.DynamoConstants;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.domain.model.AttributeModel;
import org.dynamoframework.domain.model.AttributeType;
import org.dynamoframework.domain.model.EntityModel;
import org.dynamoframework.domain.model.EntityModelFactory;
import org.dynamoframework.exception.OCSValidationException;
import org.dynamoframework.exception.OcsNotFoundException;
import org.dynamoframework.service.BaseService;
import org.dynamoframework.service.ServiceLocator;
import org.dynamoframework.service.ServiceLocatorFactory;
import org.dynamoframework.utils.ClassUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Generic controller for uploading and downloading files
 */
@RestController
@RequestMapping("/files")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "File", description = "Dynamo file controller")
public class FileController {

    private final ServiceLocator serviceLocator = ServiceLocatorFactory.getServiceLocator();

    private final EntityModelFactory entityModelFactory;

    /**
     * Uploads a file
     *
     * @param file          the file to upload
     * @param id            the ID of the entity on which to store the file
     * @param entityName    the name of the entity
     * @param attributeName the name of the attribute in which to store the file
     * @param <ID>          type parameter, type of the ID of the entity
     * @param <T>           type parameter, type of the entity
     * @throws IOException if an exception occurs
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SuppressWarnings("unchecked")
    @Transactional
    @Operation(summary = "Uploads a file")
    public <ID, T extends AbstractEntity<ID>> void upload(@RequestParam("file") MultipartFile file,
                                                          @RequestParam("entityId") String id,
                                                          @RequestParam("entityName") String entityName,
                                                          @RequestParam("attributeName") String attributeName) throws IOException {

        EntityModel<T> model = findEntityModel(entityName);
        AttributeModel attributeModel = findAttributeModel(model, attributeName);
        checkExtensions(attributeModel, file.getOriginalFilename());

        BaseService<ID, T> service = (BaseService<ID, T>) serviceLocator.getServiceForEntity(model.getEntityClass());
        ID entityId = convertId(model, id);

        T entity = findEntity(service, entityId);

        ClassUtils.setBytes(file.getBytes(), entity, attributeName);
        if (attributeModel.getFileNameProperty() != null) {
            ClassUtils.setFieldValue(entity, attributeModel.getFileNameProperty(),
                    file.getOriginalFilename());
        }

        service.save(entity);
    }

    @GetMapping(value = "/clear")
    @SuppressWarnings("unchecked")
    @Transactional
    @Operation(summary = "Clear a file")
    public <ID, T extends AbstractEntity<ID>> void clear(@RequestParam("entityId") String id,
                                                         @RequestParam("entityName") String entityName,
                                                         @RequestParam("attributeName") String attributeName) throws IOException {

        EntityModel<T> model = findEntityModel(entityName);
        AttributeModel attributeModel = findAttributeModel(model, attributeName);

        BaseService<ID, T> service = (BaseService<ID, T>) serviceLocator.getServiceForEntity(model.getEntityClass());
        ID entityId = convertId(model, id);
        T entity = findEntity(service, entityId);

        ClassUtils.setFieldValue(entity, attributeName, null);
        if (attributeModel.getFileNameProperty() != null) {
            ClassUtils.setFieldValue(entity, attributeModel.getFileNameProperty(), null);
        }
        service.save(entity);
    }

    @SuppressWarnings({"unchecked"})
    @GetMapping(path = "/download")
    @Transactional
    @Operation(summary = "Download a file")
    public <ID, T extends AbstractEntity<ID>> ResponseEntity<Resource> download(@RequestParam("entityId") String entityId,
                                                                                @RequestParam("entityName") String entityName,
                                                                                @RequestParam("attributeName") String attributeName) {
        EntityModel<T> model = findEntityModel(entityName);
        AttributeModel attributeModel = findAttributeModel(model, attributeName);

        BaseService<ID, T> service = (BaseService<ID, T>) serviceLocator.getServiceForEntity(model.getEntityClass());

        T entity = service.fetchById(convertId(model, entityId));
        if (entity == null) {
            throw new OcsNotFoundException("Entity with ID %s could not be found".formatted(entityId));
        }
        byte[] bytes = ClassUtils.getBytes(entity, attributeName);

        String fileName = "download.txt";
        if (attributeModel.getFileNameProperty() != null) {
            fileName = ClassUtils.getFieldValueAsString(entity, attributeModel.getFileNameProperty());
        }

        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(bytes));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=%s".formatted(fileName))
                .contentLength(bytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * Downloads a file as a Base64 string
     * @param entityId the entity ID
     * @param entityName the name of the entity
     * @param attributeName the name of
     * @return the base 64 representation of the string
     * @param <ID> the type of the primary key
     * @param <T> the type of the entity
     */
    @SuppressWarnings({"unchecked"})
    @GetMapping(path = "/downloadBase64", produces = MediaType.TEXT_PLAIN_VALUE)
    @Transactional
    @Operation(summary = "Downloads a file as a Base64 string")
    public <ID, T extends AbstractEntity<ID>> ResponseEntity<String> downloadBase64(@RequestParam("entityId") String entityId,
                                                                                    @RequestParam("entityName") String entityName,
                                                                                    @RequestParam("attributeName") String attributeName) {
        EntityModel<T> model = findEntityModel(entityName);
        BaseService<ID, T> service = (BaseService<ID, T>) serviceLocator.getServiceForEntity(model.getEntityClass());
        T entity = service.fetchById(convertId(model, entityId));
        if (entity == null) {
            throw new OcsNotFoundException("Entity with ID %s could not be found".formatted(entityId));
        }
        byte[] bytes = ClassUtils.getBytes(entity, attributeName);
        String base = Base64.getEncoder().encodeToString(bytes);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(base);
    }

    private <ID, T extends AbstractEntity<ID>> AttributeModel findAttributeModel(EntityModel<T> model,
                                                                                 String attributeName) {
        AttributeModel attributeModel = model.getAttributeModel(attributeName);
        if (attributeModel == null || attributeModel.getAttributeType() != AttributeType.LOB) {
            throw new OCSValidationException("Attribute %s is not suitable for file upload".formatted(attributeName));
        }
        return attributeModel;
    }

    private void checkExtensions(AttributeModel attributeModel, String fileName) {
        if (attributeModel.getAllowedExtensions() != null && !attributeModel.getAllowedExtensions().isEmpty() &&
                !StringUtils.isEmpty(fileName)) {
            String extension = FilenameUtils.getExtension(fileName);
            if (!attributeModel.getAllowedExtensions().contains(extension)) {
                throw new OCSValidationException("Extension '%s' is not allowed".formatted(extension));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <ID, T extends AbstractEntity<ID>> EntityModel<T> findEntityModel(String entityName) {
        Class<T> clazz = (Class<T>) ClassUtils.findClass(entityName);
        if (clazz == null) {
            throw new OcsNotFoundException("Entity for %s could not be found".formatted(entityName));
        }

        EntityModel<T> model = entityModelFactory.getModel(clazz);
        if (model == null) {
            throw new OcsNotFoundException("Entity Model for %s could not be found".formatted(entityName));
        }
        return model;
    }

    @SuppressWarnings("unchecked")
    private <ID, T extends AbstractEntity<ID>> ID convertId(EntityModel<T> model, String id) {
        AttributeModel idModel = model.getAttributeModel(DynamoConstants.ID);

        if (idModel.getType() == Integer.class) {
            return (ID) Integer.valueOf(id);
        } else if (idModel.getType() == Long.class) {
            return (ID) Long.valueOf(id);
        } else if (idModel.getType() == String.class) {
            return (ID) id;
        }
        return null;
    }

    private <ID, T extends AbstractEntity<ID>> T findEntity(BaseService<ID, T> service, ID entityId) {
        T entity = service.findById(entityId);
        if (entity == null) {
            throw new OcsNotFoundException("Entity with ID %s could not be found".formatted(entityId));
        }
        return entity;
    }
}
