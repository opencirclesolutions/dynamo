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

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;
import org.dynamoframework.domain.model.AttributeDateType;
import org.dynamoframework.domain.model.EditableType;
import org.dynamoframework.domain.model.annotation.SearchMode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AttributeModelResponse {

	/**
	 * Allowed extensions in case of a file upload
	 */
	private Set<String> allowedExtensions;

	/**
	 * The currency code
	 */
	private String currencyCode;

	/**
	 * The date type (e.g. date, timestamp, time)
	 */
	private AttributeDateType dateType;

	/**
	 * The default value
	 */
	private Object defaultValue;

	/**
	 * The fine-grained type of the attribute
	 */
	@NotNull
	private AttributeModelType attributeModelDataType;

	/**
	 * The descriptions of the attribute in all the supported locales
	 */
	@NotNull
	private Map<String, String> descriptions;

	/**
	 * The display names of the attribute in all the supported locales
	 */
	@NotNull
	private Map<String, String> displayNames;

	/**
	 * The display formats of the attribute in all the supported locales
	 */
	@NotNull
	private Map<String, String> displayFormats;

	/**
	 * The editable type (read only, create only, or editable)
	 */
	@NotNull
	private EditableType editableType;

	/**
	 * List of attributes that this attribute is grouped together with
	 */
	private List<String> groupTogetherWith;

	/**
	 * The name of or path to the attribute
	 */
	@NotNull
	private String name;

	/**
	 * Whether this is attribute is needed for correct operation of
	 * other attributes
	 */
	@NotNull
	private boolean neededInData;

	/**
	 * Whether to display the value as a percentage
	 */
	private Boolean percentage;

	/**
	 * The decimal precision
	 */
	private Integer precision;

	/**
	 * Whether the attribute is required
	 */
	@NotNull
	private Boolean required;

	/**
	 * Whether the attribute is required when carrying out a search
	 */
	private Boolean requiredForSearching;

	/**
	 * Whether searching on this (timestamp) attribute searches by
	 * date only (as opposed to using a date range)
	 */
	private Boolean searchDateOnly;

	/**
	 * Whether searching on this (integral or date) attribute
	 * searches for an exact match
	 */
	private Boolean searchForExactValue;

	/**
	 * The search mode
	 */
	@NotNull
	private SearchMode searchMode;

	/**
	 * The attribute select mode to use (when searching)
	 */
	private AttributeSelectModeExternal searchSelectMode;

	/**
	 * The attribute select mode to use (when editing)
	 */
	private AttributeSelectModeExternal selectMode;

	/**
	 * Whether the attribute is sortable
	 */
	@NotNull
	private Boolean sortable;

	/**
	 * The text field mode to use
	 */
	private AttributeTextFieldModeExternal textFieldMode;

	/**
	 * Whether the attribute is visible (in an edit screen)
	 */
	@NotNull
	private Boolean visibleInForm;

	/**
	 * Whether the attribute is visible (in a grid)
	 */
	@NotNull
	private Boolean visibleInGrid;

	/**
	 * The name of the property to use for displaying a description of a linked item
	 */
	private String displayPropertyName;

	/**
	 * Mapping from locales to enum values
	 */
	private Map<String, Map<String, String>> enumDescriptions;

	/**
	 * The name of the entity to use for lookups
	 */
	private String lookupEntityName;

	/**
	 * Optional reference to use for lookup (in lookup or auto-select components)
	 */
	private String lookupEntityReference;

	/**
	 * Whether multiple search for singular entities is allowed
	 * (currently only for MASTER attributes)
	 */
	private boolean multipleSearch;

	/**
	 * Maximum length (for string)
	 */
	private Integer maxLength;

	/**
	 * Minimum length (for string)
	 */
	private Integer minLength;

	/**
	 * Minimum value (numeric)
	 */
	private BigDecimal minValue;

	/**
	 * Maximum value (numeric)
	 */
	private BigDecimal maxValue;

	/**
	 * The string representations of the "true" in the supported locales
	 */
	@NotNull
	private Map<String, String> trueRepresentations;

	/**
	 * The string representations of the "false" in the supported locales
	 */
	@NotNull
	private Map<String, String> falseRepresentations;

	/**
	 * Whether the attribute represents an e-mail address
	 */
	private Boolean email;

	/**
	 * Whether to include number spinner buttons
	 */
	private Boolean includeNumberSpinnerButton;

	/**
	 * The step size for number spinner buttons
	 */
	private BigDecimal numberFieldStep;

	/**
	 * Whether an upload represents an image
	 */
	private Boolean image;

	/**
	 * Whether downloading is allowed
	 */
	private Boolean downloadAllowed;

	/**
	 * The name of the attribute in which to store the file name after a file upload
	 */
	private String fileNameAttribute;

	/**
	 * The prompts/placeholders to display inside edit components
	 */
	@NotNull
	private Map<String, String> placeholders;

	/**
	 * The boolean field mode (check box or toggle)
	 */
	private AttributeBooleanFieldModeExternal booleanFieldMode;

	/**
	 * Whether the field represents a URL
	 */
	private boolean url;

	/**
	 * Whether inter-app navigation is enabled
	 */
	private boolean navigable;

	/**
	 * Maximum length (for string fields) inside grid
	 */
	private Integer maxLengthInGrid;

	/**
	 * The type of the elements inside an element collection
	 */
	private AttributeModelType elementCollectionType;

	/**
	 * The element collection mode
	 */
	private ElementCollectionModeExternal elementCollectionMode;

	/**
	 * The minimum collection size
	 */
	private Integer minCollectionSize;

	/**
	 * The aximum collection size
	 */
	private Integer maxCollectionSize;

	/**
	 * The internal navigation link
	 */
	private String navigationLink;

	/**
	 * Whether quick add functionality is allowed
	 */
	private boolean quickAddAllowed;

	/**
	 * The enumeration field mode to use
	 */
	private AttributeEnumFieldModeExternal enumFieldMode;

	/**
	 * Default search value
	 */
	private Object defaultSearchValue;

	/**
	 * Default search value (lower bound)
	 */
	private Object defaultSearchValueFrom;

	/**
	 * Default search value (upper bound)
	 */
	private Object defaultSearchValueTo;

	/**
	 * Cascading between attributes
	 */
	private List<CascadeModel> cascades;

	/**
	 * Whether to ignore this attribute when constructing search filters
	 */
	private boolean ignoreInSearchFilter;

	/**
	 * Whether to trim excess spaces
	 */
	private boolean trimSpaces;

	/**
	 * Query type to use inside lookup fields
	 */
	private QueryTypeExternal lookupQueryType;

	/**
	 * Whether to display a paginator in details mode
	 */
	private boolean showDetailsPaginator;

}
