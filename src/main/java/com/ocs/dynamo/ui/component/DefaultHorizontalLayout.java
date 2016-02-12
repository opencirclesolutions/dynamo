package com.ocs.dynamo.ui.component;

import com.vaadin.ui.HorizontalLayout;

/**
 * Default horizontal layout
 * 
 * @author bas.rutten
 * 
 */
public class DefaultHorizontalLayout extends HorizontalLayout {

	private static final long serialVersionUID = 9070636803023696052L;

	public DefaultHorizontalLayout() {
		this(false, true);
	}

	public DefaultHorizontalLayout(boolean margin, boolean spacing) {
		setMargin(margin);
		setSpacing(spacing);
	}
}
