package nl.ocs.domain.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that can be used to indicate that certain attribute should be
 * grouped together on the screen
 * 
 * @author bas.rutten
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AttributeGroup {

	public String displayName();

	public String[] attributeNames() default {};
}
