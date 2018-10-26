package com.ocs.dynamo.filter;

import com.ocs.dynamo.utils.ClassUtils;

import java.util.Collection;

public class ContainsPredicate<T> extends PropertyPredicate<T> {

	public ContainsPredicate(final String property, final Object value) {
		super(property, value);
	}

	@Override
	public boolean test(final T t) {
		if (t == null){
			return false;
		}
		Object v = ClassUtils.getFieldValue(t, getProperty());
		if (!(v instanceof Collection)){
			throw new IllegalArgumentException("Property: " + getProperty() + " of class: " + t.getClass().toString() + " is not a Collection");
		}
		return ((Collection) v).contains(getValue());
	}
}
