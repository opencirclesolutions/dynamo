package com.ocs.dynamo.domain.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that can be placed on an entity - allows you to override metadata defaults
 * 
 * @author bas.rutten
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Model {

    // the display name
    String displayName() default "";

    // the display name (plural form)
    String displayNamePlural() default "";

    // the description
    String description() default "";

    // the property to use when displaying this item in a select component (like a combobox)
    String displayProperty() default "";

    // the default sort order for this entity
    String sortOrder() default "";
}
