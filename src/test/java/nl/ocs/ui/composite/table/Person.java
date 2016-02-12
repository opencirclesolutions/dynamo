package nl.ocs.ui.composite.table;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import nl.ocs.domain.AbstractEntity;
import nl.ocs.domain.model.annotation.Attribute;

@Entity
public class Person extends AbstractEntity<Integer> {

	private static final long serialVersionUID = 5251680526340104915L;

	@Id
	@GeneratedValue
	private Integer id;

	private String name;

	private Integer age;

	private BigDecimal weight;

	@Attribute(percentage = true)
	private BigDecimal percentage;

	public Person(Integer id, String name, Integer age, BigDecimal weight, BigDecimal percentage) {
		this.id = id;
		this.name = name;
		this.age = age;
		this.weight = weight;
		this.percentage = percentage;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public BigDecimal getWeight() {
		return weight;
	}

	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public BigDecimal getPercentage() {
		return percentage;
	}

	public void setPercentage(BigDecimal percentage) {
		this.percentage = percentage;
	}

}
