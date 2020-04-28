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

import com.ocs.dynamo.constants.DynamoConstants;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * A collapsible panel for grouping attributes
 * 
 * @author Bas Rutten
 *
 */
public class CollapsiblePanel extends DefaultVerticalLayout {

	private static final long serialVersionUID = -7979238391035057707L;

	private Icon closedIcon = VaadinIcon.PLUS_CIRCLE.create();

	private Icon openIcon = VaadinIcon.MINUS_CIRCLE.create();

	/**
	 * The button that serves as the "header" for the panel
	 */
	private Button toggle = new Button(openIcon);

	private VerticalLayout contentWrapper = new DefaultVerticalLayout();

	public CollapsiblePanel() {

		toggle.setSizeFull();
		toggle.addClassName(DynamoConstants.CSS_COLLAPSIBLE_PANEL_BUTTON);

		contentWrapper.setPadding(true);
		contentWrapper.setVisible(true);
		add(toggle, contentWrapper);
		toggle.addClickListener(event -> setOpen(!isOpen()));
	}

	public CollapsiblePanel(String caption, Component content) {
		this();
		toggle.setText(caption);
		contentWrapper.add(content);
	}

	public boolean isOpen() {
		return toggle.getIcon() == openIcon;
	}

	public CollapsiblePanel setOpen(boolean open) {
		contentWrapper.setVisible(open);
		contentWrapper.getChildren().forEach(c -> c.setVisible(open));
		toggle.setIcon(open ? getOpenIcon() : getClosedIcon());
		return this;
	}

	public CollapsiblePanel setContent(Component content) {
		this.contentWrapper.removeAll();
		this.contentWrapper.add(content);
		return this;
	}

	public VerticalLayout getContentWrapper() {
		return contentWrapper;
	}

	public Icon getClosedIcon() {
		return closedIcon;
	}

	public void setClosedIcon(Icon closedIcon) {
		this.closedIcon = closedIcon;
	}

	public Icon getOpenIcon() {
		return openIcon;
	}

	public void setOpenIcon(Icon openIcon) {
		this.openIcon = openIcon;
	}

}
