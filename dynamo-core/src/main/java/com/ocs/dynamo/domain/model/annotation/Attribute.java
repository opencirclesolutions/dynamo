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

    // the allowed extensions in case a file upload is used to edit the
    // attribute
    String allowedExtensions() default "";

    // whether a complex attribute is directly editable
    boolean complexEditable() default false;

    // is this a currency field?
    boolean currency() default false;

    // date type
    AttributeDateType dateType() default AttributeDateType.INHERIT;

    // the default value (represented as a string - use "." as the decimal separator)
    String defaultValue() default "";

    // textual description
    String description() default "";

    // should the field get focus in a detail table
    boolean detailFocus() default false;

    // the display format (useful in case of dates)
    String displayFormat() default "";

    // the display name
    String displayName() default "";

    // is this an embedded object
    boolean embedded() default false;

    // the representation to use instead of "false"
    String falseRepresentation() default "";

    // whether the component should be represented as an image
    boolean image() default false;

    // whether this field is the main attribute
    boolean main() default false;

    // is the numeric field a percentage
    boolean percentage() default false;

    // decimal precision
    int precision() default -1;

    // prompt value to use in input fields
    String prompt() default "";

    // whether the field is readonly (i.e. does not appear in edit forms)
    boolean readOnly() default false;

    // whether the field is searchable
    boolean searchable() default false;

    // case sensitive search
    boolean searchCaseSensitive() default false;

    // search prefix only
    boolean searchPrefixOnly() default false;

    // selection mode (use either a combo box, a list box, or a lookup field)
    AttributeSelectMode selectMode() default AttributeSelectMode.INHERIT;

    // whether the field shows up in a table
    VisibilityType showInTable() default VisibilityType.INHERIT;

    // whether a table can be sorted on this field
    boolean sortable() default true;

    // whether to display a text attribute as a text field or a text area
    AttributeTextFieldMode textFieldMode() default AttributeTextFieldMode.INHERIT;

    // the representation to use instead of "true"
    String trueRepresentation() default "";

    // whether the field is visible
    VisibilityType visible() default VisibilityType.INHERIT;

    // whether the field represents a date that must be shown in week notation
    boolean week() default false;
}
