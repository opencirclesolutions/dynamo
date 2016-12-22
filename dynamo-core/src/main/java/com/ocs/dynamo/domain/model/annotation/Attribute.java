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
import com.ocs.dynamo.domain.model.VisibilityType;

/**
 * An interface that can be used to specify the properties of an attribute - this will override any
 * defaults
 * 
 * @author bas.rutten
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Attribute {

	/** the allowed extensions in case a file upload is used to edit the attribute */
	String[] allowedExtensions() default {};

	/** whether a complex attribute is directly editable */
	boolean complexEditable() default false;

	/** is this a currency field? */
	boolean currency() default false;

	/** date type */
	AttributeDateType dateType() default AttributeDateType.INHERIT;

	/** the default value (represented as a string - use "." as the decimal separator) */
	String defaultValue() default "";

	/** textual description */
	String description() default "";

	/** the display format (useful in case of dates) */
	String displayFormat() default "";

	/** the display name */
	String displayName() default "";

	/**
	 * Can be used to mark an attribute as embedded
	 */
	boolean embedded() default false;

	/** the representation to use instead of "false" */
	String falseRepresentation() default "";

	/** the name of the property in which to store the file name (after an upload) */
	String fileNameProperty() default "";

	/**
	 * Names of other attributes that appear on the same line as this attribute inside an eedit form
	 * 
	 * @return
	 */
	String[] groupTogetherWith() default {};

	/** whether the component should be represented as an image */
	boolean image() default false;

	/** whether this field is the main attribute */
	boolean main() default false;

	/** the maximum element length (-1 indicates no value) */
	int maxLength() default -1;

	/** the type of the members in a detail collection - needed in very specific cases */
	Class<?> memberType() default Object.class;

	/** the minimum element length (-1 indicates no value) */
	int minLength() default -1;

	/** whether this attribute allows search for multiple values (only for MASTER attributes) */
	boolean multipleSearch() default false;

	/** is the numeric field a percentage */
	boolean percentage() default false;

	/** decimal precision */
	int precision() default -1;

	/** prompt value to use in input fields */
	String prompt() default "";

	/** */
	String quickAddPropertyName() default "";

	/** whether the field is readonly (i.e. does not appear in edit forms) */
	boolean readOnly() default false;

	/** replacement search path */
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

	/** determines which selection component to use in search mode */
	AttributeSelectMode searchSelectMode() default AttributeSelectMode.INHERIT;

	/** determines which selection component to use in edit mode */
	AttributeSelectMode selectMode() default AttributeSelectMode.INHERIT;

	/** whether the field shows up in a table */
	VisibilityType showInTable() default VisibilityType.INHERIT;

	/** whether a table can be sorted on this field */
	boolean sortable() default true;

	/** whether to display a text attribute as a text field or a text area */
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
}
