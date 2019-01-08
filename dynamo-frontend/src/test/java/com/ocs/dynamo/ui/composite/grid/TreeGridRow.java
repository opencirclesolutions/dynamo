package com.ocs.dynamo.ui.composite.grid;

import java.math.BigDecimal;

public class TreeGridRow {

	private String name;

	private Integer value;

	private Integer value2;

	private Integer valueSum;

	private Long longValue;

	private BigDecimal bdValue;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public Integer getValue2() {
		return value2;
	}

	public void setValue2(Integer value2) {
		this.value2 = value2;
	}

	public Integer getValueSum() {
		return valueSum;
	}

	public void setValueSum(Integer valueSum) {
		this.valueSum = valueSum;
	}

	public Long getLongValue() {
		return longValue;
	}

	public void setLongValue(Long longValue) {
		this.longValue = longValue;
	}

	public BigDecimal getBdValue() {
		return bdValue;
	}

	public void setBdValue(BigDecimal bdValue) {
		this.bdValue = bdValue;
	}

}
