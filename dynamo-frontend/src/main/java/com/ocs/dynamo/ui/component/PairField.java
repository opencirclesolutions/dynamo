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
package com.ocs.dynamo.ui.component;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;

public class PairField<L, R> extends CustomField<Pair<L, R>> {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(PairField.class);

	private Field<L> left;
	private Field<R> right;
	private Component middle;

	private boolean maskChanges = false;

	public PairField(Field<L> left) {
		this(left, null);
	}

	public PairField(Field<L> left, Field<R> right) {
		this(left, right, null);
	}

	public PairField(Field<L> left, Field<R> right, Component middle) {
		this.left = left;
		this.right = right;
		this.middle = middle;

		ValueChangeListener listener = e -> {
			synchronized (PairField.this) {
				if (!maskChanges) {
					maskChanges = true;
					setValue(Pair.of(left == null ? null : left.getValue(), right == null ? null : right.getValue()));
					maskChanges = false;
				}
			}
		};
		if (left != null) {
			left.addValueChangeListener(listener);
		}
		if (right != null) {
			right.addValueChangeListener(listener);
		}

		addValueChangeListener(e -> {
			synchronized (PairField.this) {
				if (!maskChanges) {
					maskChanges = true;
					if (left != null) {
						left.setValue(getValue().getLeft());
					}
					if (right != null) {
						right.setValue(getValue().getRight());
					}
					maskChanges = false;
				}
			}
		});
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8008364520978553938L;

	@Override
	protected Component initContent() {
		HorizontalLayout root = new HorizontalLayout();
		root.setHeight(null);
		root.setWidth(null);
		if (left != null) {
			root.addComponent(left);
		}
		if (middle != null) {
			root.addComponent(middle);
		}
		if (right != null) {
			root.addComponent(right);
		}

		return root;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Pair<L, R>> getType() {
		return (Class<Pair<L, R>>) (Class<?>) Pair.class;
	}
}
