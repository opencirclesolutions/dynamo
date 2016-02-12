package com.ocs.dynamo.ui.container;

public class EnergyUsage {
	
	private String ean;

	private Integer week;
	
	private Integer usage;

	public EnergyUsage(String ean, Integer week, Integer usage) {
		this.ean = ean;
		this.week = week;
		this.usage = usage;
	}

	public String getEan() {
		return ean;
	}

	public void setEan(String ean) {
		this.ean = ean;
	}

	public Integer getWeek() {
		return week;
	}

	public void setWeek(Integer week) {
		this.week = week;
	}

	public Integer getUsage() {
		return usage;
	}

	public void setUsage(Integer usage) {
		this.usage = usage;
	}

	public String getId() {
		return ean + "_" + week;
	}

}