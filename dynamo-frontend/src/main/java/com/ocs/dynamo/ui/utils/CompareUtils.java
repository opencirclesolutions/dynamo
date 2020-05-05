/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.ocs.dynamo.ui.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Sets;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.utils.ClassUtils;
import com.ocs.dynamo.utils.EntityModelUtils;

public final class CompareUtils {

	private CompareUtils() {
		// hidden constructor
	}

	/**
	 * Compares two entities based on the entity model and reports a list of the
	 * differences
	 * 
	 * @param oldEntity          the old entity
	 * @param newEntity          the new entity
	 * @param model              the entity model
	 * @param entityModelFactory
	 * @param messageService     the message service
	 * @param ignore             the names of the fields to ignore
	 */
	public static List<String> compare(Object oldEntity, Object newEntity, EntityModel<?> model,
			EntityModelFactory entityModelFactory, MessageService messageService, String... ignore) {
		List<String> results = new ArrayList<>();

		Set<String> toIgnore = new HashSet<>();
		if (ignore != null) {
			toIgnore = Sets.newHashSet(ignore);
		}
		toIgnore.addAll(EntityModelUtils.ALWAYS_IGNORE);

		String noValue = messageService.getMessage("ocs.no.value", VaadinUtils.getLocale());

		for (AttributeModel am : model.getAttributeModels()) {
			if ((AttributeType.BASIC.equals(am.getAttributeType())
					|| AttributeType.MASTER.equals(am.getAttributeType())) && !toIgnore.contains(am.getName())) {

				Object oldValue = ClassUtils.getFieldValue(oldEntity, am.getName());
				Object newValue = ClassUtils.getFieldValue(newEntity, am.getName());

				if (!Objects.equals(oldValue, newValue)) {
					String oldValueStr = FormatUtils.formatPropertyValue(entityModelFactory, am, oldValue, ", ");
					String newValueStr = FormatUtils.formatPropertyValue(entityModelFactory, am, newValue, ", ");
					results.add(messageService.getMessage("ocs.value.changed", VaadinUtils.getLocale(),
							am.getDisplayName(VaadinUtils.getLocale()), oldValue == null ? noValue : oldValueStr,
							newValue == null ? noValue : newValueStr));
				}
			} else if (AttributeType.DETAIL.equals(am.getAttributeType())) {
				Collection<?> ocol = (Collection<?>) ClassUtils.getFieldValue(oldEntity, am.getName());
				Collection<?> ncol = (Collection<?>) ClassUtils.getFieldValue(newEntity, am.getName());

				for (Object o : ncol) {
					if (!ocol.contains(o)) {
						results.add(messageService.getMessage("ocs.value.added", VaadinUtils.getLocale(),
								getDescription(o, am.getNestedEntityModel()),
								am.getDisplayName(VaadinUtils.getLocale())));
					}
				}

				for (Object o : ocol) {
					if (!ncol.contains(o)) {
						results.add(messageService.getMessage("ocs.value.removed", VaadinUtils.getLocale(),
								getDescription(o, am.getNestedEntityModel()),
								am.getDisplayName(VaadinUtils.getLocale())));
					}
				}

				for (Object o : ocol) {
					for (Object o2 : ncol) {
						if (o.equals(o2)) {
							List<String> nested = compare(o, o2, am.getNestedEntityModel(), entityModelFactory,
									messageService, ignore);
							results.addAll(nested);
						}
					}
				}

			}
		}
		return results;
	}

	/**
	 * Gets the description for a certain entity. This uses the display property if
	 * one is set and the toString value otherwise
	 * 
	 * @param entity the entity
	 * @param model  the entity model
	 * @return
	 */
	private static String getDescription(Object entity, EntityModel<?> model) {
		if (entity instanceof AbstractEntity && model.getDisplayProperty() != null) {
			String property = model.getDisplayProperty();
			return ClassUtils.getFieldValueAsString(entity, property);
		}
		return entity.toString();
	}
}
