package com.ocs.dynamo.filter;

import org.apache.commons.lang.ObjectUtils;

import com.vaadin.server.SerializablePredicate;

public class CompareFilter<T> implements SerializablePredicate<T> {

	private static final long serialVersionUID = -1140861553052524418L;

	private String property;

	private Object value;

	public CompareFilter(String property, Object value) {
		this.property = property;
		this.value = value;
	}

	@Override
	public boolean test(T t) {
		Object v = com.ocs.dynamo.utils.ClassUtils.getFieldValue(t, property);
		boolean res = ObjectUtils.equals(value, v);
		return res;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
