package com.ocs.dynamo.functional.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * 
 * @author Bas Rutten
 *
 */
@Entity
public class EntityWithTranslations extends AbstractEntityTranslated<Integer, SomeTranslation> {

	private static final long serialVersionUID = 861184101226625001L;

	@Id
	@GeneratedValue
	private Integer id;

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	
}
