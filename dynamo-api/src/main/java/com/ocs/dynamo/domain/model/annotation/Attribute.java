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
package com.ocs.dynamo.domain.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.AttributeTextFieldMode;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.VisibilityType;

/**
 * An interface that can be used to specify the properties of an attribute -
 * this will override any defaults
 *
 * @author bas.rutten
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Attribute {

    /**
     * 
     * @return the allowed extensions for a file upload component
     */
    String[] allowedExtensions() default {};

    /**
     * 
     * @return the attributes to cascade to when the value of this attribute changes
     */
    Cascade[] cascade() default {};

    /**
     * 
     * @return whether the attribute will show up inside an edit form
     */
    boolean complexEditable() default false;

    /**
     * 
     * @return whether a currency symbol will be prepended to the value
     */
    boolean currency() default false;
    
    /**
     * Custom settings
     */
    CustomSetting[] custom() default {};

    /**
     * 
     * @return the date/time type of the attribute
     */
    AttributeDateType dateType() default AttributeDateType.INHERIT;

    /**
     * @return the default value (represented as a string - use "." as the decimal
     *         separator)
     */
    String defaultValue() default "";

    /**
     * 
     * @return the description of the attribute. This will show up as a tool tip
     */
    String description() default "";

    /** @return the display format (useful in case of dates) */
    String displayFormat() default "";

    /** @return the display name */
    String displayName() default "";

    /** @return when the field can be edited */
    EditableType editable() default EditableType.EDITABLE;

    /**
     * @return whether the attribute is embedded
     */
    boolean embedded() default false;

    /** @return the representation to use instead of "false" */
    String falseRepresentation() default "";

    /**
     * @return the name of the property in which to store the file name (after an
     *         upload)
     */
    String fileNameProperty() default "";

    /**
     * @return the names of other attributes that appear on the same line as this
     *         attribute inside an edit form
     */
    String[] groupTogetherWith() default {};

    /**
     * The selection mode to use when displaying the component in an editable grid
     * 
     * @return
     */
    AttributeSelectMode gridSelectMode() default AttributeSelectMode.INHERIT;

    /**
     * Whether to ignore this attribute when constructing a search filter
     */
    boolean ignoreInSearchFilter() default false;

    /** @return whether the component is meant for uploading/displaying images */
    boolean image() default false;

    /** @return whether this field is the main attribute */
    boolean main() default false;

    /** @return the maximum element length (-1 indicates no value) */
    int maxLength() default -1;

    /**
     * 
     * @return the maximum length of the text representation in a grid
     */
    int maxLengthInGrid() default -1;

    /**
     * @return the maximum allowed value of an element in a element collection grid
     *         or for a slider component
     */
    long maxValue() default Long.MAX_VALUE;

    /**
     * @return the type of the members in a detail collection - needed in very
     *         specific cases
     */
    Class<?> memberType() default Object.class;

    /** @return the minimum element length (-1 indicates no value) */
    int minLength() default -1;

    /**
     * The minimum allowed value of an element inside a collection grid or for a
     * slider
     * 
     * @return
     */
    long minValue() default Long.MIN_VALUE;

    /**
     * @return whether this attribute allows search for multiple values (only for
     *         MASTER attributes)
     */
    boolean multipleSearch() default false;

    /**
     *
     * @return whether this attribute is navigable when inside a table
     */
    boolean navigable() default false;

    /** @return is the numeric field a percentage */
    boolean percentage() default false;

    /** @return the desired decimap precision */
    int precision() default -1;

    /** @return the prompt value to use in input fields */
    String prompt() default "";

    /**
     * @return the name of the property to set when using "quick add" functionality
     */
    String quickAddPropertyName() default "";

    /**
     * @return the replacement search path to be used when the property does not
     *         directly map to a JPA property
     */
    String replacementSearchPath() default "";

    /**
     * @return the replacement path to use for sorting when sorting on the property
     *         itself is not directly possible
     */
    String replacementSortPath() default "";

    /**
     * 
     * @return whether the attribute is required
     */
    boolean required() default false;

    /**
     * @return whether the attribute is required when performing a search
     */
    boolean requiredForSearching() default false;

    /**
     * 
     * @return whether the attribute is searchable
     */
    SearchMode searchable() default SearchMode.NONE;

    /**
     * 
     * @return whether string based search is case sensitive
     */
    boolean searchCaseSensitive() default false;

    /**
     * Whether to only search on the date for a LocalDateTime or ZonedDateTime field
     * 
     * @return
     */
    boolean searchDateOnly() default false;

    /**
     * @return whether to search for an exact match rather than an interval (for
     *         integer or date fields)
     */
    boolean searchForExactValue() default false;

    /** @return whether to match on prefix only */
    boolean searchPrefixOnly() default false;

    /** @return the selection component to use in search mode */
    AttributeSelectMode searchSelectMode() default AttributeSelectMode.INHERIT;

    /** @return the selection component to use in edit mode */
    AttributeSelectMode selectMode() default AttributeSelectMode.INHERIT;

    /** @return whether a table can be sorted on this field */
    boolean sortable() default true;

    /**
     * @return one or more references to styles; in Vaadin used to specify
     *         stylenames seperated by space
     */
    String styles() default "";

    /**
     * @return whether to display a text attribute as a text field or a text area
     */
    AttributeTextFieldMode textFieldMode() default AttributeTextFieldMode.INHERIT;

    /**
     * Indicates whether to use thousands grouping for an integer or decimal field
     */
    boolean thousandsGrouping() default true;

    /** the representation to use instead of "true" */
    String trueRepresentation() default "";

    /** indicates whether this is a clickable URL */
    boolean url() default false;

    /** whether the field is visible */
    VisibilityType visible() default VisibilityType.INHERIT;

    /** @return whether the field shows up in a grid */
    VisibilityType visibleInGrid() default VisibilityType.INHERIT;

    /** whether the field represents a date that must be shown in week notation */
    boolean week() default false;

}
