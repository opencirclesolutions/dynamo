package com.ocs.dynamo.domain.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FetchJoins {

    FetchJoin[] joins() default {};

    FetchJoin[] detailJoins() default {};
}
