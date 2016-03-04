package com.ocs.dynamo.filter.listener;

import java.io.Serializable;

import com.vaadin.data.Container.Filter;

/**
 * An event used to indicate that the value of a filter in a search form has
 * changed
 * 
 * @author bas.rutten
 */
public class FilterChangeEvent implements Serializable {

	private static final long serialVersionUID = 7833584773075924736L;

	private final String propertyId;

	private final Filter oldFilter;

	private final Filter newFilter;

	private final Object value;

	/**
	 * Constructor
	 * 
	 * @param propertyId
	 *            the name of the property
	 * @param oldFilter
	 *            the old filter
	 * @param newFilter
	 *            the new filter
	 * @param value
	 *            the new value
	 */
	public FilterChangeEvent(String propertyId, Filter oldFilter, Filter newFilter, Object value) {
		this.propertyId = propertyId;
		this.oldFilter = oldFilter;
		this.newFilter = newFilter;
		this.value = value;
	}

	public Filter getOldFilter() {
		return oldFilter;
	}

	public Filter getNewFilter() {
		return newFilter;
	}

	public String getPropertyId() {
		return propertyId;
	}

	public Object getValue() {
		return value;
	}
}
