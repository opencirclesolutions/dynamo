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
package org.dynamoframework.domain.model.annotation;

import org.dynamoframework.domain.model.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An interface that can be used to specify the properties of an attribute -
 * this will override any defaults
 *
 * @author bas.rutten
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Attribute {

    /**
     * @return the instructions to use when automatically filling the form using AI
     */
    String autoFillInstructions() default "";

    /**
     * @return the allowed extensions for a file upload component
     */
    String[] allowedExtensions() default {};

    /**
     * @return the attributes to cascade to when the value of this attribute changes
     */
    Cascade[] cascade() default {};

    /**
     * @return the currency code to use when formatting
     */
    String currencyCode() default "";

    /**
     * @return the custom settings that are defined for the attribute model
     */
    CustomSetting[] custom() default {};

    /**
     * @return the date/time type of the attribute
     */
    AttributeDateType dateType() default AttributeDateType.INHERIT;

    /**
     * @return the default value (represented as a string - use "." as the decimal
     * separator)
     */
    String defaultValue() default "";

    String defaultSearchValue() default "";

    String defaultSearchValueFrom() default "";

    String defaultSearchValueTo() default "";

    /**
     * @return the description of the attribute. This will show up as a tool tip
     */
    String description() default "";

    /**
     * @return the format to use when the attribute value is a date, time, or time
     * stamp
     */
    String displayFormat() default "";

    /**
     * @return the display name to use for the attribute
     */
    String displayName() default "";

    /**
     * @return the EditableType that describes under which conditions the field can
     * be edited
     */
    EditableType editable() default EditableType.EDITABLE;

    /**
     * @return whether the attribute is embedded
     */
    boolean embedded() default false;

    AttributeEnumFieldMode enumFieldMode() default AttributeEnumFieldMode.INHERIT;

    /**
     * @return the textual representation of the boolean "false"
     */
    String falseRepresentation() default "";

    /**
     * @return the name of the property in which to store the file name (after an
     * upload)
     */
    String fileNameProperty() default "";

    /**
     * @return the names of other attributes that appear on the same line as this
     * attribute inside an edit form
     */
    String[] groupTogetherWith() default {};

    /**
     * @return whether the attribute must be ignored when used in a search form
     */
    boolean ignoreInSearchFilter() default false;

    /**
     * @return whether the attribute is a BLOB that represents an image
     */
    boolean image() default false;

    /**
     * @return the maximum length for the attribute when it is a string used inside
     * a collection grid
     */
    int maxLength() default -1;

    /**
     * @return the maximum length of the text representation in a grid. If the
     * attribute value is longer it will be truncated
     */
    int maxLengthInGrid() default -1;

    /**
     * @return the maximum value for the attribute when used inside a collection
     * grid
     */
    double maxValue() default Double.MAX_VALUE;

    /**
     * @return the type of the members in a detail collection - needed in very
     * specific cases
     */
    Class<?> memberType() default Object.class;

    /**
     * @return the minimum length for the attribute when it is a string used inside
     * a collection grid
     */
    int minLength() default -1;

    /**
     * @return the minimum value (inclusive) for the attribute when used inside a
     * collection grid
     */
    double minValue() default Double.MIN_VALUE;

    /**
     * @return whether searching on multiple values is allowed for the attribute
     */
    boolean multipleSearch() default false;

    /**
     * @return whether the attribute is navigable when inside a grid
     */
    boolean navigable() default false;

    /**
     *
     * @return the navigation link for inter-app navigation
     */
    String navigationLink() default "";

    /**
     * @return whether the attribute is used by other attributes
     */
    boolean neededInData() default false;

    /**
     * @return whether one-to-many relation are nested inside the create/edit request
     */
    boolean nestedDetails() default true;

    /**
     * @return the number field mode
     */
    NumberFieldMode numberFieldMode() default NumberFieldMode.INHERIT;

    /**
     * @return the step size for a number field
     */
    int numberFieldStep() default 1;

    /**
     *
     * @return query type to use in case of a lookup field
     */
    QueryType lookupQueryType() default QueryType.ID_BASED;

    /**
     * @return whether the context of a password field is shown unobscured
     */
    boolean showPassword() default false;

    /**
     * @return whether the attribute value is treated as a percentage
     */
    boolean percentage() default false;

    /**
     * @return the decimal precision used when editing the attribute value
     */
    int precision() default -1;

    /**
     * @return the prompt/placeholder value to use for the attribute
     */
    String prompt() default "";

    /**
     * @return whether the "quick add" functionality is allowed
     */
    boolean quickAddAllowed() default false;

    /**
     * @return the replacement search path to be used when the property does not
     * directly map to a JPA property
     */
    String replacementSearchPath() default "";

    /**
     * @return the replacement path to use for sorting when sorting on the property
     * itself is not directly possible
     */
    String replacementSortPath() default "";

    /**
     * @return whether the attribute is required when performing a search
     */
    boolean requiredForSearching() default false;

    /**
     * @return whether the attribute appears within a search form
     */
    SearchMode searchable() default SearchMode.NONE;

    /**
     * @return whether string based search is case-sensitive
     */
    BooleanType searchCaseSensitive() default BooleanType.INHERIT;

    /**
     * @return whether to only search on the date for a LocalDateTime or
     * Instant field
     */
    boolean searchDateOnly() default false;

    /**
     * @return whether to search for exact values when searching on the attribute
     */
    boolean searchForExactValue() default false;

    /**
     * @return whether to match on prefix only when searching on the attribute
     */
    BooleanType searchPrefixOnly() default BooleanType.INHERIT;

    /**
     * @return the select mode that determines which component to use for the
     * attribute inside a search form
     */
    AttributeSelectMode searchSelectMode() default AttributeSelectMode.INHERIT;

    /**
     * @return the select mode that determines which component to use
     */
    AttributeSelectMode selectMode() default AttributeSelectMode.INHERIT;

    /**
     * @return whether the results inside a grid can be sorted on this attribute
     */
    boolean sortable() default true;

    /**
     * @return whether to display a text attribute as a text field or a text area
     */
    AttributeTextFieldMode textFieldMode() default AttributeTextFieldMode.INHERIT;

    /**
     * @return whether to trim additional spaces from text fields and text area
     * fields
     */
    TrimType trimSpaces() default TrimType.INHERIT;

    /**
     * @return the representation to use instead of "true"
     */
    String trueRepresentation() default "";

    /**
     * @return whether the attribute should be rendered as a clickable URL to an
     * outside location
     */
    boolean url() default false;

    /**
     * @return whether the attribute is visible inside a form
     */
    VisibilityType visibleInForm() default VisibilityType.INHERIT;

    /**
     * @return whether the attribute is visible inside a grid
     */
    VisibilityType visibleInGrid() default VisibilityType.INHERIT;

    /**
     * @return whether download of a binary attribute is allowed
     */
    boolean downloadAllowed() default false;

    /**
     * @return the desired boolean field mode
     */
    AttributeBooleanFieldMode booleanFieldMode() default AttributeBooleanFieldMode.INHERIT;

    /**
     * @return the reference of the entity model to use in lookup or auto-complete field
     */
    String lookupEntityReference() default "";

    /**
     * @return the element collection mode
     */
    ElementCollectionMode elementCollectionMode() default ElementCollectionMode.INHERIT;


}
