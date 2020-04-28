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
package com.ocs.dynamo.ui.composite.layout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.ui.CanAssignEntity;
import com.ocs.dynamo.ui.Reloadable;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * A layout for nesting multiple other components. Each nested component must
 * implement the Reloadable interface and must be registered using the
 * addNestedComponent method.
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the ID of the entity
 * @param <T>  the type of the entity
 */
public abstract class CompositionLayout<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseCustomComponent implements Reloadable, CanAssignEntity<ID, T> {

	private static final long serialVersionUID = 3696293073812817902L;

	private List<Component> nestedComponents = new ArrayList<>();

	private T entity;

	private VerticalLayout main;

	public CompositionLayout(T entity) {
		this.entity = entity;
		addClassName(DynamoConstants.CSS_COMPOSITION_LAYOUT);
		setMargin(false);
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		build();
	}

	@Override
	public void build() {
		if (main == null) {
			main = new DefaultVerticalLayout(false, true);
			doBuildLayout(main);
			add(main);
		}
	}

	/**
	 * Place the logic for constructing the nested components in this method. Do not
	 * forget to call "addNestedComponent" for each component that you want to
	 * update automatically
	 * 
	 * @param main the layout
	 */
	protected abstract void doBuildLayout(VerticalLayout main);

	@Override
	public void assignEntity(T entity) {
		this.entity = entity;
	}

	/**
	 * Register a component with this layout, so that it will automatically be
	 * reloaded once the CompositionLayout is reloaded
	 * 
	 * @param c
	 */
	protected void addNestedComponent(Component c) {
		nestedComponents.add(c);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void reload() {
		for (Component c : nestedComponents) {
			if (c instanceof CanAssignEntity) {
				((CanAssignEntity<ID, T>) c).assignEntity(entity);
			}
			if (c instanceof Reloadable) {
				((Reloadable) c).reload();
			}
		}
	}

	public T getEntity() {
		return entity;
	}

	public void setEntity(T entity) {
		this.entity = entity;
	}

}
