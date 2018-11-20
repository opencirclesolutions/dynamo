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

import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;

public class PairField<L, R> extends CustomField<Pair<L, R>> {

	private static final long serialVersionUID = -8008364520978553938L;

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(PairField.class);

	private AbstractField<L> left;

	private AbstractField<R> right;

	private Component middle;

	public PairField(AbstractField<L> left) {
		this(left, null);
	}

	public PairField(AbstractField<L> left, AbstractField<R> right) {
		this(left, right, null);
	}

	public PairField(AbstractField<L> left, AbstractField<R> right, Component middle) {
		this.left = left;
		this.right = right;
		this.middle = middle;
	}

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

	@Override
	public Pair<L, R> getValue() {
		return Pair.of(left.getValue(), right.getValue());
	}

	@Override
	protected void doSetValue(Pair<L, R> value) {
		if (value == null || value.getLeft() == null) {
			left.clear();
		} else {
			left.setValue(value.getLeft());
		}

		if (value == null || value.getRight() == null) {
			right.clear();
		} else {
			right.setValue(value.getRight());
		}
	}

}
