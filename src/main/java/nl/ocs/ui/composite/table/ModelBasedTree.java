package nl.ocs.ui.composite.table;

import java.io.Serializable;

import nl.ocs.domain.AbstractEntity;
import nl.ocs.domain.AbstractTreeEntity;
import nl.ocs.domain.model.EntityModel;
import nl.ocs.ui.utils.VaadinUtils;

import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Tree;

/**
 * A simple model based tree for displaying a recursive collection of data. This
 * currently only works with a BeanItemContainer
 * 
 * @author bas.rutten
 *
 * @param <ID>
 * @param <T>
 */
public class ModelBasedTree<ID extends Serializable, T extends AbstractEntity<ID>> extends Tree {

	private static final long serialVersionUID = 4646534281961148022L;

	private Container container;

	private EntityModel<T> entityModel;

	/**
	 * Constructor
	 * 
	 * @param container
	 * @param model
	 */
	@SuppressWarnings("unchecked")
	public ModelBasedTree(Container container, EntityModel<T> entityModel) {
		super("", container);
		this.container = container;
		this.entityModel = entityModel;

		// set the parents
		for (Object o : container.getItemIds()) {
			T t = VaadinUtils.getEntityFromContainer(this.container, o);
			if (t instanceof AbstractTreeEntity) {
				AbstractTreeEntity<ID, ?> te = (AbstractTreeEntity<ID, ?>) t;
				if (te.getParent() != null) {
					configureParent((T) te, (T) te.getParent());
				}
			} else {
				T parent = determineParent(t);
				configureParent(t, parent);
			}
		}

		this.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		this.setItemCaptionPropertyId(this.entityModel.getDisplayProperty());
	}

	protected T determineParent(T child) {
		return null;
	}

	private void configureParent(T child, T parent) {
		if (parent != null) {
			if (container instanceof BeanItemContainer) {
				setParent(child, parent);
			} else {
				setParent(child.getId(), parent.getId());
			}
		}
	}
}
