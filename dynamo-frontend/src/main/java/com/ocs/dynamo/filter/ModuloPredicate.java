package com.ocs.dynamo.filter;

import com.ocs.dynamo.utils.ClassUtils;

public class ModuloPredicate<T> extends PropertyPredicate<T> {

	final String modExpression;

	final Integer modValue;

	public ModuloPredicate(final String property, final String modExpression, final Number result) {
		super(property, result);
		this.modExpression = modExpression;
		this.modValue = null;
	}

	public ModuloPredicate(final String property, final Integer modValue, final Number result) {
		super(property, result);
		this.modExpression = null;
		this.modValue = modValue;
	}

	@Override
	public Number getValue() {
		return (Number) super.getValue();
	}

	public String getModExpression() {
		return modExpression;
	}

	public Integer getModValue() {
		return modValue;
	}

	@Override
	public boolean test(final T t) {
		if (t == null) {
			return false;
		}
		Object value = ClassUtils.getFieldValue(t, getProperty());
		if (value == null) {
			return false;
		}
		if (!Number.class.isAssignableFrom(value.getClass())) {
			return false;
		}

		long temp = ((Number) value).longValue();

		long modVal = 0;
		if (getModExpression() != null) {
			modVal = ((Number) ClassUtils.getFieldValue(t, getModExpression())).longValue();
		} else {
			modVal = modValue.longValue();
		}

		if (modVal == 0L) {
			throw new IllegalArgumentException("Modulo operator cannot be used with '0' as its second argument");
		}

		return temp % modVal == getValue().longValue();
	}
}
