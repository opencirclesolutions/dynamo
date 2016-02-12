package nl.ocs.importer.impl;

import java.math.BigDecimal;

import nl.ocs.importer.XlsField;
import nl.ocs.importer.dto.AbstractDTO;

public class PersonDTO extends AbstractDTO {

	private static final long serialVersionUID = 7099498644417977630L;

	public enum Gender {
		M, V
	}

	public PersonDTO() {
	}

	@XlsField(index = 0, defaultValue = "Unknown")
	private String name;

	@XlsField(index = 1, required = true)
	private Integer number;

	@XlsField(index = 2, defaultValue = "1.0", cannotBeNegative = true)
	private BigDecimal factor;

	@XlsField(index = 3)
	private String random;

	@XlsField(index = 4)
	private Gender gender;

	@XlsField(index = 5, percentage = true)
	private BigDecimal percentage;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public BigDecimal getFactor() {
		return factor;
	}

	public void setFactor(BigDecimal factor) {
		this.factor = factor;
	}

	public String getRandom() {
		return random;
	}

	public void setRandom(String random) {
		this.random = random;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public BigDecimal getPercentage() {
		return percentage;
	}

	public void setPercentage(BigDecimal percentage) {
		this.percentage = percentage;
	}

}
