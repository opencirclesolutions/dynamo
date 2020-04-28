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

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Default vertical layout - provides convenience constructor for setting the
 * margin and the spacing
 * 
 * @author bas.rutten
 */
public class DefaultVerticalLayout extends VerticalLayout {

	private static final long serialVersionUID = 979501638798053429L;

	public DefaultVerticalLayout(boolean padding, boolean spacing) {
		setMargin(false);
		setPadding(padding);
		setSpacing(spacing);
	}

	public DefaultVerticalLayout() {
		this(false, false);
	}
}
