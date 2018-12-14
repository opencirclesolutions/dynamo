package com.ocs.dynamo.filter;

import com.ocs.dynamo.utils.ClassUtils;

/**
 * A predicate for checking whether a String value matches a predicate
 * 
 * @author Bas Rutten
 *
 * @param <T> the type of the entity
 */
public class LikePredicate<T> extends PropertyPredicate<T> {

	private static final long serialVersionUID = -5077087872701525001L;

	private boolean caseSensitive;

	public LikePredicate(String property, String value, boolean caseSensitive) {
		super(property, value);
		this.caseSensitive = caseSensitive;
	}

	@Override
	public boolean test(T t) {
		if (t == null) {
			return false;
		}

		Object v = ClassUtils.getFieldValue(t, getProperty());
		if (v == null || !v.getClass().isAssignableFrom(String.class)) {
			return false;
		}
		String pattern = getValue().toString().replace("%", ".*");
		if (isCaseSensitive()) {
			return ((String) v).matches(pattern);
		}
		return ((String) v).toUpperCase().matches(pattern.toUpperCase());
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

}
