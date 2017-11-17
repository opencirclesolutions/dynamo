package com.ocs.dynamo.ui.composite.table;

import com.ocs.dynamo.domain.AbstractTreeEntity;
import com.ocs.dynamo.domain.model.annotation.Model;

@Model(displayProperty = "name")
public class TreeEntity extends AbstractTreeEntity<Integer, TreeEntity> {

	private static final long serialVersionUID = 4371478448824463157L;

	private Integer id;

	private String name;

	public TreeEntity(int id, String name, TreeEntity parent) {
		this.id = id;
		this.name = name;
		setParent(parent);
	}

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
