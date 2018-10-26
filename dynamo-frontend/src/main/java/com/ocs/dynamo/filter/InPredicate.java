package com.ocs.dynamo.filter;

import com.ocs.dynamo.utils.ClassUtils;

import java.util.Collection;

public class InPredicate<T> extends PropertyPredicate<T> {

	public InPredicate(final String property, final Collection values) {
		super(property, values);
	}

	@Override
	public Collection getValue() {
		return (Collection)super.getValue();
	}

	@Override
	public boolean test(final T t) {
		if (t == null){
			return false;
		}
		Object v = ClassUtils.getFieldValue(t, getProperty());
		if (v == null){
			return false;
		}
		Collection values = (Collection)getValue();
		return values.contains(v);
	}
}
