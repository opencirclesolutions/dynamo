package com.ocs.dynamo.dao;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import com.ocs.dynamo.exception.OCSRuntimeException;

/**
 * Object representing a sort order and direction
 * 
 * @author bas.rutten
 * 
 */
public class SortOrder implements Serializable {

	private static final long serialVersionUID = -4702369564151453555L;

	private static final Direction DEFAULT_DIRECTION = Direction.ASC;

	private final Direction direction;

	private final String property;

	public static enum Direction {

		ASC, DESC;

		/**
		 * Translates the provided string into the corresponding enum
		 * 
		 * @param value
		 * @return
		 */
		public static Direction fromString(String value) {
			try {
				return Direction.valueOf(value.toUpperCase());
			} catch (Exception e) {
				throw new OCSRuntimeException(String.format("Sort order %s is not recognized",
						value), e);
			}
		}
	}

	/**
	 * Constructor
	 * 
	 * @param direction
	 *            the desired sort direction
	 * @param property
	 *            the property to sort on
	 */
	public SortOrder(Direction direction, String property) {
		this.direction = direction == null ? DEFAULT_DIRECTION : direction;
		this.property = property;
	}

	public SortOrder(String property) {
		this(DEFAULT_DIRECTION, property);
	}

	public Direction getDirection() {
		return direction;
	}

	public String getProperty() {
		return property;
	}

	public boolean isAscending() {
		return this.direction.equals(Direction.ASC);
	}

	public SortOrder with(Direction order) {
		return new SortOrder(order, this.property);
	}

	@Override
	public int hashCode() {
		int result = 13;
		result = 43 * result + ObjectUtils.hashCode(direction);
		result = 53 * result + ObjectUtils.hashCode(property);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SortOrder)) {
			return false;
		}

		SortOrder that = (SortOrder) obj;
		return ObjectUtils.equals(this.direction, that.direction)
				&& ObjectUtils.equals(this.property, that.property);
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
