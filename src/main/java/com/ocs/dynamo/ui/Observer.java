package com.ocs.dynamo.ui;

import com.ocs.dynamo.domain.AbstractEntity;

/**
 * An Observer according to the well-known pattern
 */
public interface Observer<T extends AbstractEntity<?>> {

	/**
	 * Call this to notify the observer of a change to the observed entity
	 * 
	 * @param entity
	 */
	public void notify(T entity);
}
