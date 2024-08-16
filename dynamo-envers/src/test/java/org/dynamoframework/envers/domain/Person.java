package org.dynamoframework.envers.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import org.hibernate.envers.Audited;

import com.ocs.dynamo.domain.AbstractEntity;

@Audited
@Entity
public class Person extends AbstractEntity<Integer> {

	private static final long serialVersionUID = -5939362772351629511L;

	@Id
	@GeneratedValue
	private Integer id;

	private String name;

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
