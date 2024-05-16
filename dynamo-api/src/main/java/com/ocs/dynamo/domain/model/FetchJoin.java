package com.ocs.dynamo.domain.model;

import com.ocs.dynamo.dao.JoinType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface FetchJoin {

    String attribute();

    JoinType type() default JoinType.LEFT;
}
