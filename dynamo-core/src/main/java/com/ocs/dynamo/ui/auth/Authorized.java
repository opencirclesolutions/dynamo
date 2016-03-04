package com.ocs.dynamo.ui.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for specifying which roles can access a view
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Authorized {

    /**
     * The roles that are given access to the view
     * 
     * @return
     */
    String[] roles();

    /**
     * Indicates that this screen is for editing purposes only
     * 
     * @return
     */
    boolean editOnly() default false;
}
