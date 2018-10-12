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

import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.AttributeTextFieldMode;
import com.ocs.dynamo.domain.model.CheckboxMode;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.NumberSelectMode;
import com.ocs.dynamo.domain.model.VisibilityType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An interface that can be used to specify the properties of an attribute - this will override any
 * defaults
 *
 * @author bas.rutten
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Attribute {

    /** @return the allowed extensions for a file upload component */
    String[] allowedExtensions() default {};

    /** @return the properties to cascade to when the value of this property changes **/
    Cascade[] cascade() default {};

	/**
	 *
	 * @return the desired check box mode
	 */
	CheckboxMode checkboxMode() default CheckboxMode.CHECKBOX;

	/** @return whether a complex attribute is directly editable */
	boolean complexEditable() default false;

    /** @return is this a currency field? */
    boolean currency() default false;

    /** @return the date type (date, time, or timestamp) */
    AttributeDateType dateType() default AttributeDateType.INHERIT;

    /** @return the default value (represented as a string - use "." as the decimal separator) */
    String defaultValue() default "";

    /** @return the description (appears as a tooltip) */
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

	float expansionFactor() default 1.0f;

	/** @return the representation to use instead of "false" */
	String falseRepresentation() default "";

    /** @return the name of the property in which to store the file name (after an upload) */
    String fileNameProperty() default "";

    /**
     * @return the names of other attributes that appear on the same line as this attribute inside
     *         an edit form
     */
    String[] groupTogetherWith() default {};

    /** @return whether the component is meant for uploading/displaying images */
    boolean image() default false;

    /** @return whether the component is restricted to only the required locales */
    boolean localesRestricted() default false;

    /** @return whether this field is the main attribute */
    boolean main() default false;

    /** @return the maximum element length (-1 indicates no value) */
    int maxLength() default -1;

	/**
	 * 
	 * @return the maximum length of the text representation in a table
	 */
	int maxLengthInTable() default -1;

    /**
     * @return the maximum allowed value of an element in a collection table
     */
    long maxValue() default Long.MAX_VALUE;

    /** @return the type of the members in a detail collection - needed in very specific cases */
    Class<?> memberType() default Object.class;

    /** @return the minimum element length (-1 indicates no value) */
    int minLength() default -1;

    /** @return the minimum element length (-1 indicates no value) */
    long minValue() default Long.MIN_VALUE;

    /**
     * @return whether this attribute allows search for multiple values (only for MASTER attributes)
     */
    boolean multipleSearch() default false;

	/**
	 *
	 * @return whether this attribute is navigable when inside a table
	 */
	boolean navigable() default false;

	/**
	 *
	 * @return the number select mode (indicates which component to use for editing
	 *         numbers)
	 */
	NumberSelectMode numberSelectMode() default NumberSelectMode.TEXTFIELD;

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
	 * @return the replacement path to use for sorting when sorting on the property
	 *         itself is not directly possible
	 */
	String replacementSortPath() default "";

	/**
	 * @return the replacement search path to be used when the property does not
	 *         directly map to a JPA property
	 */
	String replacementSearchPath() default "";

	/**
	 * 
	 * @return whether the attribute is required
	 */
	boolean required() default false;

    /**
     * @return whether the attribute is required when performing a search
     */
    boolean requiredForSearching() default false;

    /** @return whether the field is searchable */
    boolean searchable() default false;

    /** @return whether searching is case-sensitive */
    boolean searchCaseSensitive() default false;

    /**
     * @return whether to search for an exact match rather than an interval (for integer or date
     *         fields)
     */
    boolean searchForExactValue() default false;

    /** @return whether to match on prefix only */
    boolean searchPrefixOnly() default false;

    /** @return the selection component to use in search mode */
    AttributeSelectMode searchSelectMode() default AttributeSelectMode.INHERIT;

    /** @return the selection component to use in edit mode */
    AttributeSelectMode selectMode() default AttributeSelectMode.INHERIT;

    /** @return whether the field shows up in a table */
    VisibilityType showInTable() default VisibilityType.INHERIT;

    /** @return whether a table can be sorted on this field */
    boolean sortable() default true;

	/**
	 * @return one or more references to styles; in Vaadin used to specify stylenames seperated by space
	 */
	String styles() default "";

	/**
	 * @return whether to display a text attribute as a text field or a text area
	 */
	AttributeTextFieldMode textFieldMode() default AttributeTextFieldMode.INHERIT;

    /** the representation to use instead of "true" */
    String trueRepresentation() default "";

    /** indicates whether this is a clickable URL */
    boolean url() default false;

    /** indicates whether to use thousands grouping for an integer or decimal field */
    boolean useThousandsGrouping() default true;

    /** whether the field is visible */
    VisibilityType visible() default VisibilityType.INHERIT;

    /** whether the field represents a date that must be shown in week notation */
    boolean week() default false;

    /** Whether direct navigation is allowed to this entity from another entity detailscreen */
    boolean directNavigation() default false;
}
