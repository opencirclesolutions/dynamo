package com.ocs.dynamo.ui.composite.table;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.annotation.Model;

@Model(displayProperty = "name")
public class FakeTreeEntity extends AbstractEntity<Integer> {

	private static final long serialVersionUID = 4371478448824463157L;

	private Integer id;

	private String name;

	private FakeTreeEntity master;

	public FakeTreeEntity(int id, String name, FakeTreeEntity master) {
		this.id = id;
		this.name = name;
		this.master = master;
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

	public FakeTreeEntity getMaster() {
		return master;
	}

	public void setMaster(FakeTreeEntity master) {
		this.master = master;
	}

}
