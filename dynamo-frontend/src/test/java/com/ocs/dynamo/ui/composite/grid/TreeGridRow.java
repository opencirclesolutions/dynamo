package com.ocs.dynamo.ui.composite.grid;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TreeGridRow {

	private String name;

	private Integer value;

	private Integer value2;

	private Integer valueSum;

	private Long longValue;

	private BigDecimal bdValue;

}
