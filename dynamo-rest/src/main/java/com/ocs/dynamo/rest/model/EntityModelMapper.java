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
package com.ocs.dynamo.rest.model;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelAction;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.utils.SystemPropertyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class EntityModelMapper {

    private final MessageService messageService;

    private final EntityModelAttributeMapper mapper;

    public EntityModelResponse mapEntityModel(EntityModel<?> model) {
        Set<Locale> locales = discoverLocales();
        return EntityModelResponse.builder()
                .attributeModels(model.getAttributeModels().stream().map(am ->
                        mapper.mapAttributeModel(am, locales)).toList())
                .attributeNamesOrdered(extractAttributeNames(model.getAttributeModels()))
                .attributeNamesOrderedForGrid(extractAttributeNames(model.getAttributeModelsSortedForGrid()))
                .attributeNamesOrderedForSearch(extractAttributeNames(model.getAttributeModelsSortedForSearch()))
                .descriptions(extractDescriptions(model, locales))
                .displayNames(extractDisplayNames(model, locales))
                .displayNamesPlural(extractDisplayNamesPlural(model, locales))
                .displayProperty(model.getDisplayProperty())
                .reference(model.getReference())
                .sortProperty(extractSortOrder(model))
                .sortAscending(extractSortAscending(model))
                .listAllowed(model.isListAllowed())
                .deleteAllowed(model.isDeleteAllowed())
                .updateAllowed(model.isUpdateAllowed())
                .createAllowed(model.isCreateAllowed())
                .attributeGroups(mapAttributeModelGroups(model, locales))
                .exportAllowed(model.isExportAllowed())
                .actions(mapActions(model.getEntityModelActions(), locales))
                .readRoles(new ArrayList<>(model.getReadRoles()))
                .writeRoles(new ArrayList<>(model.getWriteRoles()))
                .deleteRoles(new ArrayList<>(model.getDeleteRoles()))
                .build();
    }

    /**
     * Maps attribute model groups to AttributeGroupResponse
     *
     * @param model   the entity model
     * @param locales the supported locales
     * @return the result of the mapping
     */
    private List<AttributeGroupResponse> mapAttributeModelGroups(EntityModel<?> model, Set<Locale> locales) {
        AtomicInteger index = new AtomicInteger(0);
        return model.getAttributeGroups().stream()
                .map(group -> mapAttributeGroup(model, group, index.incrementAndGet(), locales))
                .filter(group -> !group.getAttributes().isEmpty())
                .toList();
    }

    /**
     * Maps a single attribute group
     *
     * @param model     the entity model
     * @param groupName the name/ID of the group
     * @param index     the index of the group
     * @param locales   the supported locales
     * @return the result of the mapping
     */
    private AttributeGroupResponse mapAttributeGroup(EntityModel<?> model, String groupName, int index, Set<Locale> locales) {

        Map<String, String> descriptions = new HashMap<>();
        for (Locale locale : locales) {
            String description = messageService.getMessage(groupName, locale);
            descriptions.put(locale.toString(), description);
        }

        return AttributeGroupResponse.builder()
                .index(index)
                .groupName(groupName)
                .groupDescriptions(descriptions)
                .attributes(model.getAttributeModelsForGroup(groupName)
                        .stream().filter(am -> am.isVisibleInForm()).map(am -> am.getName()).toList())
                .build();
    }

    private boolean extractSortAscending(EntityModel<?> model) {
        Set<Map.Entry<AttributeModel, Boolean>> entries = model.getSortOrder().entrySet();
        if (entries.isEmpty()) {
            return true;
        }
        return entries.iterator().next().getValue();
    }

    private String extractSortOrder(EntityModel<?> model) {
        Set<Map.Entry<AttributeModel, Boolean>> entries = model.getSortOrder().entrySet();
        if (entries.isEmpty()) {
            return DynamoConstants.ID;
        }
        return entries.iterator().next().getKey().getName();
    }

    private Map<String, String> extractDescriptions(EntityModel<?> model, Set<Locale> locales) {
        return extractFromEntityModel(model, locales, (m, locale) -> m.getDescription(locale));
    }

    private Map<String, String> extractDisplayNames(EntityModel<?> model, Set<Locale> locales) {
        return extractFromEntityModel(model, locales, (m, locale) -> m.getDisplayName(locale));
    }

    private Map<String, String> extractDisplayNamesPlural(EntityModel<?> model, Set<Locale> locales) {
        return extractFromEntityModel(model, locales, (m, locale) -> m.getDisplayNamePlural(locale));
    }

    private Map<String, String> extractFromEntityModel(EntityModel<?> model, Set<Locale> locales,
                                                       BiFunction<EntityModel<?>, Locale, String> function) {
        Map<String, String> result = new HashMap<>();
        for (Locale locale : locales) {
            String description = function.apply(model, locale);
            result.put(locale.toString(), description);
        }
        return result;
    }

    private Map<String, String> extractActionDisplayNames(EntityModelAction action, Set<Locale> locales) {
        return extractFromAction(action, locales, (m, locale) -> m.getDisplayName(locale));
    }

    private Map<String, String> extractFromAction(EntityModelAction model, Set<Locale> locales,
                                                  BiFunction<EntityModelAction, Locale, String> function) {
        Map<String, String> result = new HashMap<>();
        for (Locale locale : locales) {
            String description = function.apply(model, locale);
            result.put(locale.toString(), description);
        }
        return result;
    }

    private List<String> extractAttributeNames(List<AttributeModel> attributeModels) {
        return attributeModels.stream().map(am -> am.getName()).toList();
    }

    /**
     * Scans the classpath for available locales (checks for entitymodel_<locale> files)
     *
     * @return the set of discovered locales
     */
    private Set<Locale> discoverLocales() {
        ClassLoader cl = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        Resource[] resources = new Resource[0];
        try {
            resources = resolver.getResources("classpath*:/META-INF/entitymodel*.properties");
        } catch (IOException e) {
            // log exception but otherwise skip
            log.error(e.getMessage(), e);
        }
        Set<Locale> collect = Arrays.stream(resources).map(res -> extractLocale(res.getFilename()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        collect.add(SystemPropertyUtils.getDefaultLocale());
        return collect;
    }

    private Locale extractLocale(String fileName) {
        String baseName = FilenameUtils.getBaseName(fileName);
        int p = baseName.indexOf("_");
        if (p >= 0) {
            String localeString = baseName.substring(p + 1);
            int q = localeString.indexOf("_");
            Locale.Builder builder = new Locale.Builder();

            if (q >= 0) {
                builder.setLanguage(localeString.substring(0, p));
                builder.setRegion(localeString.substring(p + 1));
            } else {
                builder.setLanguage(localeString);
            }
            return builder.build();
        }
        return null;
    }

    private List<EntityModelActionResponse> mapActions(List<EntityModelAction> actions, Set<Locale> locales) {
        return actions.stream().map(action -> mapAction(action, locales)).toList();
    }

    private EntityModelActionResponse mapAction(EntityModelAction action,  Set<Locale> locales) {
        return EntityModelActionResponse.builder()
                .id(action.getId())
                .displayNames(extractActionDisplayNames(action, locales))
                .type(action.getType())
                .icon(action.getIcon())
                .roles(new ArrayList<>(action.getRoles()))
                .build();
    }
}
