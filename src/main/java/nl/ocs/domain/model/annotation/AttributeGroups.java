package nl.ocs.domain.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that can be used to place multiple "AttributeGroup" annotation
 * on an entity
 * 
 * @author bas.rutten
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AttributeGroups {

	public AttributeGroup[] attributeGroups() default {};

}
