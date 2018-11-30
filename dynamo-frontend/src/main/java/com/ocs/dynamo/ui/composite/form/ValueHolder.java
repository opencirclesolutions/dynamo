package com.ocs.dynamo.ui.composite.form;

/**
 * A holder for a simple value
 * 
 * @author Bas Rutten
 *
 * @param <T> the value
 */
public class ValueHolder<T> {

	T value;

	public ValueHolder(T t) {
		this.value = t;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

}
