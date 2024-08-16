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
package org.dynamoframework.rest.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@Jacksonized
public class EntityModelResponse {

    /**
     * The attribute models
     */
    @NotNull
    @NotEmpty
    private List<AttributeModelResponse> attributeModels;

    @NotNull
    @NotEmpty
    private List<AttributeGroupResponse> attributeGroups;

    /**
     * Attribute names ordered for edit or detail screen
     */
    @NotNull
    private List<String> attributeNamesOrdered;

    /**
     * Attribute names ordered for grid
     */
    @NotNull
    private List<String> attributeNamesOrderedForGrid;

    /**
     * Attribute names ordered for search form
     */
    @NotNull
    private List<String> attributeNamesOrderedForSearch;

    /**
     * Descriptions of the entity (in all supported locales)
     */
    @NotNull
    private Map<String, String> descriptions;

    /**
     * Display names of the entity (in all supported locales)
     */
    @NotNull
    private Map<String, String> displayNames;

    /**
     * Display names (plural form) of the entity (in all supported locales)
     */
    @NotNull
    private Map<String, String> displayNamesPlural;

    /**
     * Display property
     */
    private String displayProperty;

    /**
     * The unique reference of the model
     */
    private String reference;

    /**
     * The name of the property to sort on
     */
    @NotNull
    private String sortProperty;

    /**
     * Whether to sort in ascending direction
     */
    @NotNull
    private boolean sortAscending;

    @NotNull
    private boolean deleteAllowed;

    @NotNull
    private boolean listAllowed;

    @NotNull
    private boolean createAllowed;

    @NotNull
    private boolean updateAllowed;

    @NotNull
    private boolean exportAllowed;

    private List<EntityModelActionResponse> actions;

    private List<String> readRoles;

    private List<String> writeRoles;

    private List<String> deleteRoles;

}
