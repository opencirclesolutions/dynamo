package com.ocs.dynamo.importer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to indicate how a field should be imported from an Excel file
 * 
 * @author bas.rutten
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface XlsField {

	// the index of the field in the row in the Excel sheet
	int index();

	// indicates whether the field is required
	boolean required() default false;

	// gives the default value to be used when the cell is empty
	String defaultValue() default "";

	// indicates whether the field is a percentage
	boolean percentage() default false;

	// indicates that the value cannot be negative
	boolean cannotBeNegative() default false;
}
