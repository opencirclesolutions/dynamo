package com.ocs.dynamo.filter;

import com.ocs.dynamo.utils.ClassUtils;

public class SimpleStringPredicate<T> extends PropertyPredicate<T> {

	private static final long serialVersionUID = -5077087872701525001L;

	private boolean caseSensitive;

	private boolean onlyMatchPrefix;

	public SimpleStringPredicate(String property, String value, boolean onlyMatchPrefix, boolean caseSensitive) {
		super(property, value);
		this.onlyMatchPrefix = onlyMatchPrefix;
		this.caseSensitive = caseSensitive;
	}

	@Override
	public boolean test(T t) {
		Object v = ClassUtils.getFieldValue(t, getProperty());
		if (v == null) {
			return false;
		} else if (!v.getClass().isAssignableFrom(String.class)) {
			return false;
		}
		String value = caseSensitive ? v.toString() : v.toString().toLowerCase();
		String match = caseSensitive ? getValue().toString() : getValue().toString().toLowerCase();
		if (onlyMatchPrefix){
			return value.startsWith(match);
		} else {
			return value.contains(match);
		}
	}

	public boolean isOnlyMatchPrefix() {
		return onlyMatchPrefix;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

}
