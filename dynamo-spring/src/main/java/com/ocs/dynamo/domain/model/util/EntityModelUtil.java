package com.ocs.dynamo.domain.model.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.google.common.collect.Sets;
import com.ocs.dynamo.constants.OCSConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.composite.table.TableUtils;
import com.ocs.dynamo.utils.ClassUtils;

/**
 * @author bas.rutten
 */
public class EntityModelUtil {

	private static final Set<String> ALWAYS_IGNORE = Sets.newHashSet("createdOn", "createdBy",
	        "changedBy", "changedOn");

	private EntityModelUtil() {
		// private constructor
	}

	/**
	 * Returns the value of the main attribute of an entity
	 * 
	 * @param entity
	 * @param model
	 * @return
	 */
	public static <T> String getMainAttributeValue(T entity, EntityModel<T> model) {
		AttributeModel main = model.getMainAttributeModel();
		if (main != null) {
			return ClassUtils.getFieldValueAsString(entity, main.getName());
		}
		return null;
	}

	/**
	 * Returns the value of the main attribute of an entity
	 * 
	 * @param entity
	 * @param model
	 * @return
	 */
	public static <T> String getDisplayPropertyValue(T entity, EntityModel<T> model) {
		String property = model.getDisplayProperty();
		return ClassUtils.getFieldValueAsString(entity, property);
	}

	/**
	 * Copies all simple attribute values from one entity to the other
	 * 
	 * @param source
	 *            the source entity
	 * @param target
	 *            the target entity
	 * @param model
	 */
	public static <T> void copySimpleAttributes(T source, T target, EntityModel<T> model,
	        String... ignore) {
		Set<String> toIgnore = new HashSet<>();
		if (ignore != null) {
			toIgnore = Sets.newHashSet(ignore);
		}
		toIgnore.addAll(ALWAYS_IGNORE);

		for (AttributeModel am : model.getAttributeModels()) {
			if ((AttributeType.BASIC.equals(am.getAttributeType())
			        || AttributeType.LOB.equals(am.getAttributeType()))
			        && !toIgnore.contains(am.getName())) {
				if (!OCSConstants.ID.equals(am.getName())) {
					Object value = ClassUtils.getFieldValue(source, am.getName());
					if (ClassUtils.canSetProperty(target, am.getName())) {
						if (value != null) {
							ClassUtils.setFieldValue(target, am.getName(), value);
						} else {
							ClassUtils.clearFieldValue(target, am.getName(), am.getType());
						}
					}

				}
			}
		}
	}

	/**
	 * Compares two entities based on the entity model and reports a list of the
	 * differences
	 * 
	 * @param oldEntity
	 *            the old entity
	 * @param newEntity
	 *            the new entity
	 * @param model
	 * @param ignore
	 */
	public static List<String> compare(Object oldEntity, Object newEntity, EntityModel<?> model,
	        EntityModelFactory entityModelFactory, MessageService messageService,
	        String... ignore) {
		List<String> results = new ArrayList<>();

		Set<String> toIgnore = new HashSet<>();
		if (ignore != null) {
			toIgnore = Sets.newHashSet(ignore);
		}
		toIgnore.addAll(ALWAYS_IGNORE);

		String noValue = messageService.getMessage("ocs.no.value");

		for (AttributeModel am : model.getAttributeModels()) {
			if ((AttributeType.BASIC.equals(am.getAttributeType())
			        || AttributeType.MASTER.equals(am.getAttributeType()))
			        && !toIgnore.contains(am.getName())) {

				Object oldValue = ClassUtils.getFieldValue(oldEntity, am.getName());
				Object newValue = ClassUtils.getFieldValue(newEntity, am.getName());

				if (!ObjectUtils.equals(oldValue, newValue)) {
					String oldValueStr = TableUtils.formatPropertyValue(entityModelFactory, model,
					        messageService, am.getName(), oldValue);
					String newValueStr = TableUtils.formatPropertyValue(entityModelFactory, model,
					        messageService, am.getName(), newValue);
					results.add(messageService.getMessage("ocs.value.changed", am.getDisplayName(),
					        oldValue == null ? noValue : oldValueStr,
					        newValue == null ? noValue : newValueStr));
				}
			} else if (AttributeType.DETAIL.equals(am.getAttributeType())) {
				Collection<?> ocol = (Collection<?>) ClassUtils.getFieldValue(oldEntity,
				        am.getName());
				Collection<?> ncol = (Collection<?>) ClassUtils.getFieldValue(newEntity,
				        am.getName());

				for (Object o : ncol) {
					if (!ocol.contains(o)) {
						results.add(messageService.getMessage("ocs.value.added",
						        getDescription(o, am.getNestedEntityModel()), am.getDisplayName()));
					}
				}

				for (Object o : ocol) {
					if (!ncol.contains(o)) {
						results.add(messageService.getMessage("ocs.value.removed",
						        getDescription(o, am.getNestedEntityModel()), am.getDisplayName()));
					}
				}

				for (Object o : ocol) {
					for (Object o2 : ncol) {
						if (o.equals(o2)) {
							List<String> nested = compare(o, o2, am.getNestedEntityModel(),
							        entityModelFactory, messageService, ignore);
							results.addAll(nested);
						}
					}
				}

			}
		}
		return results;
	}

	/**
	 * Gets the description for a certain entity
	 * 
	 * @param o
	 * @param model
	 * @return
	 */
	private static String getDescription(Object o, EntityModel<?> model) {
		if (o instanceof AbstractEntity && model.getDisplayProperty() != null) {
			String property = model.getDisplayProperty();
			return ClassUtils.getFieldValueAsString(o, property);
		}
		return o.toString();
	}
}
