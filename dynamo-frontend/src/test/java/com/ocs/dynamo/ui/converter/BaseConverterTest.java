package com.ocs.dynamo.ui.converter;

import java.util.Locale;

import com.vaadin.data.ValueContext;

public abstract class BaseConverterTest {

	protected ValueContext createContext() {
		return new ValueContext(new Locale("nl"));
	}

	protected ValueContext createUsContext() {
		return new ValueContext(Locale.US);
	}
}
