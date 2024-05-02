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
package com.ocs.dynamo.dao;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * A DTO representing the properties of a fetch join
 * 
 * @author bas.rutten
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FetchJoinInformation {

	@EqualsAndHashCode.Include
	private final String property;

	@EqualsAndHashCode.Include
	private final JoinType joinType;

	/**
	 * Constructor
	 * 
	 * @param property the property to use for the fetch
	 * @param joinType the desired join type (left, inner etc.)
	 */
	public FetchJoinInformation(String property, JoinType joinType) {
		this.property = property;
		this.joinType = joinType;
	}

	/**
	 * Constructor - defaults to left join
	 * 
	 * @param property the property to fetch
	 */
	public FetchJoinInformation(String property) {
		this(property, JoinType.LEFT);
	}

	/**
	 * Creates an array based on the specified vararg
	 * 
	 * @param joins the specified joins
	 * @return an array based on the joins
	 */
	public static FetchJoinInformation[] of(FetchJoinInformation... joins) {
		return joins;
	}

	/**
	 * Create a FetchJoinInformation based on the provided property
	 * @param property the property
	 * @return the created FetchJoinInformation object
	 */
	public static FetchJoinInformation of(String property) {
		return new FetchJoinInformation(property);
	}

	/**
	 * Create a FetchJoinInformation based on the provided property and join type
	 * @param property the property
	 * @param joinType the join type
	 * @return the created FetchJoinInformation object
	 */
	public static FetchJoinInformation of(String property, JoinType joinType) {
		return new FetchJoinInformation(property, joinType);
	}
}
