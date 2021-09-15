package com.ocs.dynamo.ui.component;

import java.text.Normalizer;

import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.utils.EntityModelUtils;

public abstract class BaseIgnoreDiacriticsFilter<T> {

	private final boolean ignoreCase;

	private final boolean onlyMatchPrefix;

	private final EntityModel<T> entityModel;

	public BaseIgnoreDiacriticsFilter(EntityModel<T> entityModel, boolean ignoreCase, boolean onlyMatchPrefix) {
		this.ignoreCase = ignoreCase;
		this.onlyMatchPrefix = onlyMatchPrefix;
		this.entityModel = entityModel;
	}

	public boolean test(T item, String filterText) {
		// replace any diacritical characters
		if (item == null) {
			return false;
		}

		String temp = entityModel == null ? item.toString()
				: EntityModelUtils.getDisplayPropertyValue(item, entityModel);

		temp = ignoreCase ? temp.toLowerCase() : temp;
		filterText = ignoreCase ? filterText.toLowerCase() : filterText;

		temp = Normalizer.normalize(temp, Normalizer.Form.NFD);
		temp = temp.replaceAll("[^\\p{ASCII}]", "");

		return onlyMatchPrefix ? temp.startsWith(filterText) : temp.contains(filterText);
	}
	
	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public boolean isOnlyMatchPrefix() {
		return onlyMatchPrefix;
	}
}
