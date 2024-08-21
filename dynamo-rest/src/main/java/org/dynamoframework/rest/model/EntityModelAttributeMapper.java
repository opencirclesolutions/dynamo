package org.dynamoframework.rest.model;

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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.dynamoframework.domain.model.*;
import org.dynamoframework.domain.model.annotation.SearchMode;
import org.dynamoframework.exception.OCSValidationException;
import org.dynamoframework.service.MessageService;
import org.dynamoframework.utils.NumberUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class EntityModelAttributeMapper {

    private final MessageService messageService;

    public AttributeModelResponse mapAttributeModel(AttributeModel model, Set<Locale> locales) {

        AttributeModelResponse.AttributeModelResponseBuilder builder = AttributeModelResponse.builder();

        AttributeModelType type = mapType(model);
        builder.attributeModelDataType(type);

        builder.visibleInGrid(model.isVisibleInGrid());
        builder.visibleInForm(model.isVisibleInForm());
        builder.neededInData(model.isNeededInData());
        builder.name(model.getName());
        builder.editableType(model.getEditableType());
        builder.required(model.isRequired());
        builder.dateType(model.getDateType());
        builder.defaultValue(model.getDefaultValue());
        builder.defaultSearchValue(model.getDefaultSearchValue());
        builder.defaultSearchValueFrom(model.getDefaultSearchValueFrom());
        builder.defaultSearchValueTo(model.getDefaultSearchValueTo());
        builder.displayFormats(extractFromAttributeModel(model, locales, AttributeModel::getDisplayFormat));
        builder.sortable(model.isSortable());
        builder.multipleSearch(model.isMultipleSearch());
        builder.placeholders(extractFromAttributeModel(model, locales, AttributeModel::getPrompt));
        builder.booleanFieldMode(mapBooleanFieldMode(model.getBooleanFieldMode(), model));
        builder.cascades(mapCascades(model));
        builder.ignoreInSearchFilter(model.isIgnoreInSearchFilter());
        builder.groupTogetherWith(model.getGroupTogetherWith());

        if (type == AttributeModelType.STRING) {
            builder.email(model.isEmail());
            builder.url(model.isUrl());
            builder.maxLengthInGrid(model.getMaxLengthInGrid());
            builder.textFieldMode(mapTextFieldMode(model.getTextFieldMode()));
            builder.trimSpaces(model.isTrimSpaces());
        }

        if (type == AttributeModelType.STRING || model.getAttributeType() == AttributeType.ELEMENT_COLLECTION) {
            builder.minLength(model.getMinLength());
            builder.maxLength(model.getMaxLength());
        }

        if (model.isNumerical() || model.getAttributeType() == AttributeType.ELEMENT_COLLECTION) {
            builder.minValue(model.getMinValue());
            builder.maxValue(model.getMaxValue());
            builder.includeNumberSpinnerButton(model.getNumberFieldMode() == NumberFieldMode.NUMBERFIELD);
            builder.numberFieldStep(model.getNumberFieldStep() == null ?
                    BigDecimal.ONE : BigDecimal.valueOf(model.getNumberFieldStep()));
        }

        if (model.getAttributeType() == AttributeType.ELEMENT_COLLECTION) {
            builder.elementCollectionType(mapMemberType(model));
            builder.elementCollectionMode(mapElementCollectionMode(model));
            builder.minCollectionSize(model.getMinCollectionSize());
            builder.maxCollectionSize(model.getMaxCollectionSize());
        }

        builder.descriptions(extractFromAttributeModel(model, locales, (am, loc) -> am.getDescription(loc)));
        builder.displayNames(extractFromAttributeModel(model, locales, (am, loc) -> am.getDisplayName(loc)));

        if (model.getAttributeType() == AttributeType.LOB) {
            builder.allowedExtensions(model.getAllowedExtensions());
        }

        if (model.getAttributeType() == AttributeType.MASTER ||
                model.getAttributeType() == AttributeType.DETAIL) {
            boolean single = model.getAttributeType() == AttributeType.MASTER;
            builder.selectMode(mapAttributeSelectMode(model.getSelectMode(), single));
            builder.searchSelectMode(mapAttributeSelectMode(model.getSearchSelectMode(), single));
            builder.navigable(model.isNavigable());
            builder.navigationLink(model.getNavigationLink());
            builder.lookupQueryType(mapQueryType(model.getLookupQueryType()));
        }

        if (model.getAttributeType() == AttributeType.MASTER ||
                model.getAttributeType() == AttributeType.DETAIL) {
            if (model.getNestedEntityModel() != null) {
                builder.displayPropertyName(model.getNestedEntityModel().getDisplayProperty());
                builder.lookupEntityName(model.getNestedEntityModel().getEntityClass().getSimpleName());
                builder.lookupEntityReference(model.getLookupEntityReference());
                builder.quickAddAllowed(model.isQuickAddAllowed());
            } else {
                log.warn("Expected a nested entity model for a MASTER or DETAIL attribute: {}",
                        model.getName());
                throw new OCSValidationException("Expected a nested entity model for a MASTER or DETAIL attribute");
            }
        }

        if (model.getAttributeType() == AttributeType.DETAIL) {
            builder.minCollectionSize(model.getMinCollectionSize());
            builder.maxCollectionSize(model.getMaxCollectionSize());
        }

        if (model.isNumerical()) {
            builder.percentage(model.isPercentage());
            builder.currencyCode(model.getCurrencyCode());
            builder.precision(model.getPrecision());
        }

        if (model.getSearchMode() != SearchMode.NONE) {
            builder.searchMode(model.getSearchMode());
            builder.searchDateOnly(model.isSearchDateOnly());
            builder.searchForExactValue(model.isSearchForExactValue());
            builder.requiredForSearching(model.isRequiredForSearching());
        }

        if (type == AttributeModelType.ENUM) {
            builder.enumDescriptions(mapEnumDescriptions(model, locales));
            builder.enumFieldMode(mapEnumFieldMode(model.getEnumFieldMode()));
        }

        if (type == AttributeModelType.BOOL) {
            builder.trueRepresentations(mapTrueRepresentations(model, locales));
            builder.falseRepresentations(mapFalseRepresentations(model, locales));
        }

        if (type == AttributeModelType.LOB) {
            builder.image(model.isImage());
            builder.downloadAllowed(model.isDownloadAllowed());
            builder.fileNameAttribute(model.getFileNameProperty());
        }

        return builder.build();
    }

    private QueryTypeExternal mapQueryType(QueryType queryType) {
        return switch (queryType) {
            case PAGING -> QueryTypeExternal.PAGING;
            default -> QueryTypeExternal.ID_BASED;
        };
    }

    private AttributeModelType mapType(AttributeModel model) {
        if (model.getType().equals(String.class)) {
            return AttributeModelType.STRING;
        } else if (model.isIntegral()) {
            return AttributeModelType.INTEGRAL;
        } else if (model.isNumerical()) {
            return AttributeModelType.DECIMAL;
        } else if (model.getType().equals(Boolean.class) || model.getType().equals(boolean.class)) {
            return AttributeModelType.BOOL;
        } else if (model.getType().isEnum()) {
            return AttributeModelType.ENUM;
        } else if (model.getAttributeType() == AttributeType.MASTER) {
            return AttributeModelType.MASTER;
        } else if (model.getAttributeType() == AttributeType.DETAIL) {
            return model.isNestedDetails() ? AttributeModelType.ONE_TO_MANY :
                    AttributeModelType.MANY_TO_MANY;
        } else if (model.getAttributeType() == AttributeType.LOB) {
            return AttributeModelType.LOB;
        } else if (model.getDateType() == AttributeDateType.INSTANT) {
            return AttributeModelType.INSTANT;
        } else if (model.getDateType() == AttributeDateType.LOCAL_DATE_TIME) {
            return AttributeModelType.LOCAL_DATE_TIME;
        } else if (model.getDateType() == AttributeDateType.TIME) {
            return AttributeModelType.TIME;
        } else if (model.getDateType() == AttributeDateType.DATE) {
            return AttributeModelType.DATE;
        } else if (model.getAttributeType() == AttributeType.ELEMENT_COLLECTION) {
            return AttributeModelType.ELEMENT_COLLECTION;
        }
        return null;
    }

    private List<CascadeModel> mapCascades(AttributeModel model) {
        return model.getCascadeAttributes().stream()
                .map(attribute -> CascadeModel.builder()
                        .cascadeMode(model.getCascadeMode(attribute))
                        .filterPath(model.getCascadeFilterPath(attribute))
                        .cascadeTo(attribute)
                        .build()).toList();
    }

    private AttributeModelType mapMemberType(AttributeModel am) {
        if (am.getMemberType().equals(String.class)) {
            return AttributeModelType.STRING;
        } else if (NumberUtils.isInteger(am.getMemberType()) ||
                NumberUtils.isLong(am.getMemberType())) {
            return AttributeModelType.INTEGRAL;
        }
        return null;
    }

    private ElementCollectionModeExternal mapElementCollectionMode(AttributeModel am) {
        return switch (am.getElementCollectionMode()) {
            case INHERIT, CHIPS -> ElementCollectionModeExternal.CHIPS;
            case DIALOG -> ElementCollectionModeExternal.DIALOG;
        };
    }

    private AttributeSelectModeExternal mapAttributeSelectMode(AttributeSelectMode mode, boolean single) {
        return switch (mode) {
            case COMBO -> AttributeSelectModeExternal.COMBO;
            case INHERIT -> single ? AttributeSelectModeExternal.COMBO : AttributeSelectModeExternal.MULTI_SELECT;
            case MULTI_SELECT -> AttributeSelectModeExternal.MULTI_SELECT;
            case LOOKUP -> AttributeSelectModeExternal.LOOKUP;
            case AUTO_COMPLETE -> AttributeSelectModeExternal.AUTO_COMPLETE;
        };
    }

    private AttributeTextFieldModeExternal mapTextFieldMode(AttributeTextFieldMode mode) {
        return switch (mode) {
            case INHERIT, TEXTFIELD -> AttributeTextFieldModeExternal.TEXTFIELD;
            case PASSWORD -> AttributeTextFieldModeExternal.PASSWORD;
            case TEXTAREA -> AttributeTextFieldModeExternal.TEXTAREA;
        };
    }

    private AttributeEnumFieldModeExternal mapEnumFieldMode(AttributeEnumFieldMode mode) {
        return switch (mode) {
            case INHERIT, DROPDOWN -> AttributeEnumFieldModeExternal.DROPDOWN;
            case RADIO -> AttributeEnumFieldModeExternal.RADIO;
        };
    }

    private AttributeBooleanFieldModeExternal mapBooleanFieldMode(AttributeBooleanFieldMode mode,
                                                                  AttributeModel am) {
        if (!am.isBoolean()) {
            return null;
        }

        return switch (mode) {
            case INHERIT, CHECKBOX -> AttributeBooleanFieldModeExternal.CHECKBOX;
            case SWITCH -> AttributeBooleanFieldModeExternal.SWITCH;
            case TOGGLE -> AttributeBooleanFieldModeExternal.TOGGLE;
        };
    }

    @SuppressWarnings("rawtypes")
    private Map<String, Map<String, String>> mapEnumDescriptions(AttributeModel model, Set<Locale> locales) {
        return locales.stream().map(locale -> {
            Class<? extends Enum> enumClass = model.getType().asSubclass(Enum.class);
            Map<String, String> strings = mapEnumValues(enumClass, locale);
            return Pair.of(locale, strings);
        }).collect(Collectors.toMap(pair -> pair.getLeft().toString(), pair -> pair.getRight()));
    }

    /**
     * Extract a locale-based value from the attribute model
     *
     * @param model    the attribute model
     * @param locales  the set of locales
     * @param function the mapping function
     * @return the result of the mapping
     */
    private Map<String, String> extractFromAttributeModel(AttributeModel model, Set<Locale> locales,
                                                          BiFunction<AttributeModel, Locale, String> function) {
        Map<String, String> result = new HashMap<>();
        for (Locale locale : locales) {
            String description = function.apply(model, locale);
            result.put(locale.toString(), description);
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    private <E extends Enum> Map<String, String> mapEnumValues(Class<E> enumClass, Locale locale) {
        return Arrays.stream(enumClass.getEnumConstants()).map(
                        enumValue -> Pair.of(enumValue.name(), messageService.getEnumMessage(enumClass, enumValue, locale)))
                .collect(Collectors.toMap(pair -> pair.getLeft(), pair -> pair.getRight()));
    }

    private Map<String, String> mapTrueRepresentations(AttributeModel model, Set<Locale> locales) {
        return locales.stream().map(locale ->
                        Pair.of(locale, model.getTrueRepresentation(locale)))
                .collect(toMap(pair -> pair.getLeft().toString(), pair -> pair.getRight()));
    }

    private Map<String, String> mapFalseRepresentations(AttributeModel model, Set<Locale> locales) {
        return locales.stream().map(locale ->
                        Pair.of(locale, model.getFalseRepresentation(locale)))
                .collect(toMap(pair -> pair.getLeft().toString(), pair -> pair.getRight()));
    }
}
