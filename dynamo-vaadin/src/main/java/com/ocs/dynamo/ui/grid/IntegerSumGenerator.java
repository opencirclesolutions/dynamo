package com.ocs.dynamo.ui.grid;

import java.util.Collection;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.PropertyValueGenerator;

/**
 * A property value generator for summing up a collection of Integers
 * 
 * @author bas.rutten
 */
public class IntegerSumGenerator extends PropertyValueGenerator<Integer> {

	private static final long serialVersionUID = -3866833325930425694L;

	private String propertyNameFilter;

	/**
	 * Constructor
	 * 
	 * @param propertyNameFilter
	 *            the property name filter - only properties that contain an
	 *            underscore followed by the value of this field will be
	 *            considered
	 */
	public IntegerSumGenerator(String propertyNameFilter) {
		this.propertyNameFilter = propertyNameFilter;
	}

	@Override
	public Integer getValue(Item item, Object itemId, Object propertyId) {
		int sum = 0;
		Collection<?> cols = item.getItemPropertyIds();
		for (Object o : cols) {
			if (o.toString().contains("_" + propertyNameFilter)) {
				Property<?> prop = item.getItemProperty(o);
				if (prop.getValue() != null && prop.getValue() instanceof Integer) {
					Integer v = (Integer) prop.getValue();
					sum += v;
				}
			}
		}
		return sum;
	}

	@Override
	public Class<Integer> getType() {
		return Integer.class;
	}
}
