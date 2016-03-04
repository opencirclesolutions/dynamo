package com.ocs.dynamo.importer.dto;

import java.io.Serializable;

/**
 * Abstract class for DTOs that are used during the import process
 * 
 * @author bas.rutten
 */
public class AbstractDTO implements Serializable {

	private static final long serialVersionUID = 4262695463304442546L;

	private int rowNum;

	public int getRowNum() {
		return rowNum;
	}

	public void setRowNum(int rowNum) {
		this.rowNum = rowNum;
	}

}
