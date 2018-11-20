package com.ocs.dynamo.ui.composite.form;

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
