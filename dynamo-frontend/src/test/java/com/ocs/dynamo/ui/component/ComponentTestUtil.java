package com.ocs.dynamo.ui.component;

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ComponentTestUtil {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void query(DataProvider<?, ?> provider) {
		provider.size(new Query(0, 10, null, null, null));
		provider.fetch(new Query(0, 10, null, null, null));

	}
}
