package nl.ocs.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

/**
 * Base class for entities that have a tree hierarchy
 * 
 * @author bas.rutten
 * 
 * @param <ID>
 *            type of the primary key
 * @param <P>
 *            type of the entity
 */
@SuppressWarnings("rawtypes")
@MappedSuperclass
public abstract class AbstractTreeEntity<ID, P extends AbstractTreeEntity> extends
		AbstractEntity<ID> {

	private static final long serialVersionUID = 2561513983753651230L;

	@ManyToOne
	@JoinColumn(name = "parent")
	private P parent;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<P> children = new ArrayList<P>();

	/**
	 * @return the parent
	 */
	public P getParent() {
		return parent;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(P parent) {
		this.parent = parent;
	}

	/**
	 * @return the children
	 */
	public List<P> getChildren() {
		return children;
	}

	/**
	 * @param children
	 *            the children to set
	 */
	public void setChildren(List<P> children) {
		this.children = children;
	}

	@SuppressWarnings("unchecked")
	public P addChild(P child) {
		children.add(child);
		child.setParent(this);
		return child;
	}
}
