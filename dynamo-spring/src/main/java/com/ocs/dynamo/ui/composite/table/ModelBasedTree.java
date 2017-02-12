/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.ui.composite.table;

import java.io.Serializable;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.AbstractTreeEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Tree;

/**
 * A simple model based tree for displaying a recursive collection of data.
 * 
 * @author bas.rutten
 * @param <ID>
 *            the type of the primary key of the entities to display
 * @param <T>
 *            the type of the entities to display
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

	/**
	 * Callback method to determine the parent of an entity - override in subclasses
	 * 
	 * @param child
	 *            the entity
	 * @return
	 */
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
