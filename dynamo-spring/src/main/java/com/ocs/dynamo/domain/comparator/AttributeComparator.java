package com.ocs.dynamo.domain.comparator;

import java.util.Comparator;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.utils.ClassUtils;

/**
 * A comparator for comparing two entities based on a value of an attribute
 * (using reflection)
 * 
 * @author bas.rutten
 * 
 */
public class AttributeComparator implements Comparator<AbstractEntity<?>> {

	private String attribute;

	/**
	 * Constructor
	 * @param attribute
	 */
	public AttributeComparator(String attribute) {
		this.attribute = attribute;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int compare(AbstractEntity<?> o1, AbstractEntity<?> o2) {
		Object v1 = ClassUtils.getFieldValue(o1, attribute);
		Object v2 = ClassUtils.getFieldValue(o2, attribute);

		if (v1 == null) {
			return -1;
		} else if (v2 == null) {
			return 1;
		} else if (v1 instanceof String) {
			return ((String) v1).compareToIgnoreCase((String) v2);
		} else if (v1 instanceof Comparable) {
			return ((Comparable) v1).compareTo((Comparable) v2);
		}
		return 0;

	}
}
