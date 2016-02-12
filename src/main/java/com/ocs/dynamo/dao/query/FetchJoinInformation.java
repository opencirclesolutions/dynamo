package com.ocs.dynamo.dao.query;

import javax.persistence.criteria.JoinType;

import org.apache.commons.lang.ObjectUtils;

/**
 * A DTO representing the properties of a fetch join
 * 
 * @author bas.rutten
 * 
 */
public class FetchJoinInformation {

	private final String property;

	// FIXME Using the java persistence enum makes the UI dependable on JPA,
	// this is not recommended and should be redesigned
	private final JoinType joinType;

	/**
	 * Constructor
	 * 
	 * @param property
	 *            the property to use for the fetch
	 * @param joinType
	 *            the desired join type (left, inner etc)
	 */
	public FetchJoinInformation(String property, JoinType joinType) {
		this.property = property;
		this.joinType = joinType;
	}

	/**
	 * Constructor - defaults to left join
	 * 
	 * @param property
	 */
	public FetchJoinInformation(String property) {
		this(property, JoinType.LEFT);
	}

	public String getProperty() {
		return property;
	}

	public JoinType getJoinType() {
		return joinType;
	}

	@Override
	public int hashCode() {
		return ObjectUtils.hashCode(property) + ObjectUtils.hashCode(joinType);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FetchJoinInformation)) {
			return false;
		}
		FetchJoinInformation other = (FetchJoinInformation) obj;
		return ObjectUtils.equals(this.getProperty(), other.getProperty())
				&& ObjectUtils.equals(this.getJoinType(), other.getJoinType());
	}
}
