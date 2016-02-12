package nl.ocs.ui.component;

import com.vaadin.ui.VerticalLayout;

/**
 * Default vertical layout - provides convenience constructor for setting the
 * margin and the spacing
 * 
 * @author bas.rutten
 * 
 */
public class DefaultVerticalLayout extends VerticalLayout {

	private static final long serialVersionUID = 979501638798053429L;

	public DefaultVerticalLayout(boolean margin, boolean spacing) {
		setMargin(margin);
		setSpacing(spacing);
	}

	public DefaultVerticalLayout() {
		this(true, true);
	}
}
