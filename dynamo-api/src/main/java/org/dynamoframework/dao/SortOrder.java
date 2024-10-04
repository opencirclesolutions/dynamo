package org.dynamoframework.dao;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.dynamoframework.exception.OCSRuntimeException;

import java.io.Serializable;

/**
 * Object representing a sort order and direction
 *
 * @author bas.rutten
 */
@Getter
@EqualsAndHashCode
@ToString
public class SortOrder implements Serializable {

	private static final long serialVersionUID = -4702369564151453555L;

	private static final Direction DEFAULT_DIRECTION = Direction.ASC;

	private final Direction direction;

	private final String property;

	public enum Direction {

		ASC, DESC;

		/**
		 * Translates the provided String into the corresponding Direction enum
		 *
		 * @param value the String representation of the Direction
		 * @return the Direction
		 */
		public static Direction fromString(String value) {
			try {
				return Direction.valueOf(value.toUpperCase());
			} catch (Exception e) {
				throw new OCSRuntimeException(String.format("Sort order [%s] is not recognized", value), e);
			}
		}
	}

	/**
	 * Constructor
	 *
	 * @param property  the property to sort on
	 * @param direction the desired sort direction
	 */
	public SortOrder(String property, Direction direction) {
		this.direction = direction == null ? DEFAULT_DIRECTION : direction;
		this.property = property;
	}

	/**
	 * Constructor
	 *
	 * @param property the property to sort on (in ascending direction)
	 */
	public SortOrder(String property) {
		this(property, DEFAULT_DIRECTION);
	}

	public boolean isAscending() {
		return this.direction.equals(Direction.ASC);
	}

	public SortOrder withDirection(Direction direction) {
		return new SortOrder(this.property, direction);
	}

}
