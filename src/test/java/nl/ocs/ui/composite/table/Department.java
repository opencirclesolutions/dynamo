package nl.ocs.ui.composite.table;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import nl.ocs.domain.AbstractEntity;

@Entity
public class Department extends AbstractEntity<Integer> {

	private static final long serialVersionUID = -2436328464507840946L;

	@Id
	@GeneratedValue
	private Integer id;

	private String name;

	private Set<Person> employees = new HashSet<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Person> getEmployees() {
		return employees;
	}

	public void setEmployees(Set<Person> employees) {
		this.employees = employees;
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

}
