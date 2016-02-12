package nl.ocs.ui.container.hierarchical;

import javax.persistence.criteria.JoinType;

import nl.ocs.dao.query.FetchJoinInformation;

public class HierarchicalFetchJoinInformation extends FetchJoinInformation {

	private int level;

	public HierarchicalFetchJoinInformation(int level, String property) {
		super(property);
		this.level = level;
	}

	public HierarchicalFetchJoinInformation(int level, String property, JoinType joinType) {
		super(property, joinType);
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

}