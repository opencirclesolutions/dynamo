package com.ocs.dynamo.ui.composite.dialog;

import com.ocs.dynamo.ui.Buildable;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Base class for modal dialogs. This class has an empty button bar and no
 * content. Subclasses should implement the "doBuildButtonBar" method to add the
 * appropriate buttons and "doBuild" to add the appropriate content
 * 
 * @author bas.rutten
 * 
 */
public abstract class BaseModalDialog extends Window implements Buildable {

	private static final long serialVersionUID = -2265149201475495504L;

	/**
	 * Constructor
	 */
	public BaseModalDialog() {
	}

	@Override
	public void build() {
		constructLayout();
	}

	private void constructLayout() {
		this.setModal(true);
		this.setResizable(false);

		Panel panel = new Panel();
		panel.setCaptionAsHtml(true);
		panel.setCaption(getTitle());

		this.setContent(panel);

		VerticalLayout main = new DefaultVerticalLayout();
		main.setStyleName("ocsDialog");
		panel.setContent(main);

		doBuild(main);

		DefaultHorizontalLayout buttonBar = new DefaultHorizontalLayout();
		main.addComponent(buttonBar);

		doBuildButtonBar(buttonBar);
	}

	/**
	 * Constructs the actual contents of the window
	 * 
	 * @param parent
	 */
	protected abstract void doBuild(Layout parent);

	/**
	 * Constructs the button bar
	 * 
	 * @param buttonBar
	 */
	protected abstract void doBuildButtonBar(HorizontalLayout buttonBar);

	/**
	 * Returns the title
	 * 
	 * @return
	 */
	protected abstract String getTitle();

}
