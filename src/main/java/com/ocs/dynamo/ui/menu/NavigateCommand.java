package com.ocs.dynamo.ui.menu;

import com.ocs.dynamo.constants.OCSConstants;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;

/**
 * Command for navigating to a certain view
 * 
 * @author bas.rutten
 * 
 */
public class NavigateCommand implements Command {

	private static final long serialVersionUID = 5192333331107840255L;

	private final Navigator navigator;

	private final String destination;

	private final String selectedTab;

	private final String mode;

	/**
	 * Constructor
	 * 
	 * @param navigator
	 *            the Vaadin navigator
	 * @param destination
	 *            the destination to navigate to
	 * @param selectedTab
	 *            the index of the tab to select
	 * @param mode
	 *            an optional screen mode
	 */
	public NavigateCommand(Navigator navigator, String destination, String selectedTab, String mode) {
		this.navigator = navigator;
		this.destination = destination;
		this.selectedTab = selectedTab;
		this.mode = mode;
	}

	@Override
	public void menuSelected(MenuItem selectedItem) {
		if (selectedTab != null) {
			VaadinSession.getCurrent().setAttribute(OCSConstants.SELECTED_TAB,
					Integer.valueOf(selectedTab));
		} else {
			// clear the selected tab index
			VaadinSession.getCurrent().setAttribute(OCSConstants.SELECTED_TAB, null);
		}
		
		VaadinSession.getCurrent().setAttribute(OCSConstants.SCREEN_MODE, mode);
		
		navigator.navigateTo(destination);
	}

	public String getDestination() {
		return destination;
	}

	public String getSelectedTab() {
		return selectedTab;
	}

	public String getMode() {
		return mode;
	}

}
