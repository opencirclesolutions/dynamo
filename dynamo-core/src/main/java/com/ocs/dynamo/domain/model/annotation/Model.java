package com.ocs.dynamo.domain.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that can be placed on an entity - allows you to override meta
 * data defaults
 * 
 * @author bas.rutten
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Model {

	// the display name
	public String displayName() default "";

	// the display name (plural form)
	public String displayNamePlural() default "";

	// the description
	public String description() default "";

	// the property to use when displaying this item in a select component (like
	// a combobox)
	public String displayProperty() default "";

	// the default sort order for this entity
	public String sortOrder() default "";

}
