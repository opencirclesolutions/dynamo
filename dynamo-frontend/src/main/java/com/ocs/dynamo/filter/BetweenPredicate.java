package com.ocs.dynamo.filter;

import com.ocs.dynamo.utils.ClassUtils;

public class BetweenPredicate<T> extends PropertyPredicate<T> {

	private static final long serialVersionUID = -5077087872701525001L;

	private Comparable<?> toValue;

	public BetweenPredicate(String property, Comparable<?> fromValue, Comparable<?> toValue) {
		super(property, fromValue);
		this.toValue = toValue;
	}

	@Override
	public boolean test(T t) {
		Object v = ClassUtils.getFieldValue(t, getProperty());
		if (v == null) {
			return false;
		}
		return ((Comparable)v).compareTo(getFromValue()) >= 0 && ((Comparable)v).compareTo(toValue) < 0;
	}

	public Comparable<?> getFromValue(){
		return (Comparable<?>) getValue();
	}

	public Comparable<?> getToValue() {
		return toValue;
	}
}
