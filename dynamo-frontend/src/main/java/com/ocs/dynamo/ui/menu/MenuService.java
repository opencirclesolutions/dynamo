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
package com.ocs.dynamo.ui.menu;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;

/**
 * Service for creating a menu based on property files. Use the
 * "menu.properties" file to configure this menu.
 * 
 * @author bas.rutten
 */
public class MenuService extends BaseMenuService<MenuItem, MenuBar> {

	/**
	 * Adds the child items for the specified key
	 * @param bar the menu bar (root)
	 * @param menuItem the menu item to add the children to
	 * @param key the key of the menu item
	 */
	private void addChildItems(MenuBar bar, HasMenuItems item, String key) {
		// add the child items
		int index = 1;
		String childKey = getMessageService().getMessageNoDefault(key + "." + index + "." + DISPLAY_NAME,
				VaadinUtils.getLocale());

		while (childKey != null) {
			constructMenu(bar, item, key + "." + index);
			index++;
			childKey = getMessageService().getMessageNoDefault(key + "." + index + "." + DISPLAY_NAME,
					VaadinUtils.getLocale());
		}
	}

	/**
	 * Adds a menu item
	 * 
	 * @param parent  the parent to which to add the item
	 * @param caption the caption of the item
	 * @param command the navigation command to add to the item
	 * @return
	 */
	private MenuItem addMenuItem(HasMenuItems parent, String caption, NavigateCommand<MenuItem, MenuBar> command) {
		MenuItem menuItem;
		if (parent instanceof MenuBar) {
			// main menu bar
			menuItem = ((MenuBar) parent).addItem(caption, command);
			return menuItem;
		} else {
			// sub menu
			menuItem = ((SubMenu) parent).addItem(caption, command);
		}
		return menuItem;
	}

	/**
	 * Constructs a menu item and its children
	 * 
	 * @param root   the root component
	 * @param parent the parent component (either a menu bar or menu item) to add
	 *               the menu to
	 * @param key    the message key
	 * @return the constructed menu item
	 */
	private MenuItem constructMenu(MenuBar root, HasMenuItems parent, String key) {
		String caption = getMessageService().getMessageNoDefault(key + "." + DISPLAY_NAME, VaadinUtils.getLocale());

		MenuItem menuItem = null;
		// When no caption exists, return no instance of menuItem (null)
		if (!StringUtils.isEmpty(caption)) {
			// look up the messages
			String destination = getMessageService().getMessageNoDefault(key + "." + DESTINATION,
					VaadinUtils.getLocale());
			String tabIndex = getMessageService().getMessageNoDefault(key + "." + TAB_INDEX, VaadinUtils.getLocale());
			String mode = getMessageService().getMessageNoDefault(key + "." + MODE, VaadinUtils.getLocale());
			String description = getMessageService().getMessageNoDefault(key + "." + DESCRIPTION,
					VaadinUtils.getLocale());

			// create navigation command
			NavigateCommand<MenuItem, MenuBar> command = createNavigationCommand(root, destination, tabIndex, mode);

			menuItem = addMenuItem(parent, caption, command);

			// set description
			if (description != null) {
				VaadinUtils.setTooltip(menuItem, description);
			}

			if (!StringUtils.isEmpty(destination)) {
				addDestination(destination, mode, menuItem);
			}

			parent = menuItem.getSubMenu();
			addChildItems(root, parent, key);

			// hide menu item if user does not have permissions
			hideIfNoPermission(menuItem, destination);
		}
		return menuItem;
	}

	/**
	 * Constructs a menu
	 * 
	 * @param rootName the root name (prefix) of the messages that are used to
	 *                 populate the menu
	 * @return
	 */
	public MenuBar constructMenu(String rootName) {
		MenuBar mainMenu = new MenuBar();

		// look up any messages of the form "rootName.i"
		int i = 1;
		while (true) {
			if (constructMenu(mainMenu, mainMenu, rootName + "." + i) == null) {
				break;
			}
			i++;
		}

		// hide any menu items for which the user has no access rights
		hideRecursively(mainMenu);

		return mainMenu;
	}

	public String getLastVisited() {
		return lastVisited;
	}

	/**
	 * Checks whether the provided item has a sub menu item with the provided
	 * destination
	 * 
	 * @param item
	 * @param destination
	 * @return
	 */
	private boolean hasChildWithDestination(MenuItem item, String destination) {
		if (item.getSubMenu() != null && !item.getSubMenu().getItems().isEmpty()) {
			return item.getSubMenu().getItems().stream().anyMatch(it -> hasChildWithDestination(it, destination));
		} else {
			// look in the destination map for any match
			Set<Entry<String, MenuItem>> possibleDestinations = findDestinations(destination);
			return possibleDestinations.stream().anyMatch(e -> e.getValue().equals(item));
		}
	}

	/**
	 * Hides a menu item if all its children are hidden
	 * 
	 * @param item the menu item
	 * @return
	 */
	private boolean hideIfAllChildrenHidden(MenuItem item) {
		if (!item.getSubMenu().getItems().isEmpty()) {
			// check if the item has any visible children
			boolean found = false;

			List<MenuItem> children = item.getSubMenu().getItems();
			for (MenuItem c : children) {
				boolean visible = hideIfAllChildrenHidden(c);
				found |= visible;
			}

			// .. if not, then hide the item
			if (!found) {
				item.setVisible(false);
			}
			return found;
		} else {
			return item.isVisible();
		}
	}

	/**
	 * Recursively process all menu items and hides the items that have no visible
	 * children
	 * 
	 * @param bar the menu bar
	 */
	protected void hideRecursively(MenuBar bar) {
		for (MenuItem item : bar.getItems()) {
			hideIfAllChildrenHidden(item);
		}
	}

	/**
	 * Marks the last visited menu item in the menu bar
	 * 
	 * @param menuBar     the menu bar
	 * @param destination the destination that was last visited
	 */
	public void setLastVisited(MenuBar menuBar, String destination) {
		this.lastVisited = destination;
		for (MenuItem item : menuBar.getItems()) {
			item.getElement().getClassList().remove(DynamoConstants.CSS_LAST_VISITED);
			if (hasChildWithDestination(item, destination)) {
				item.getElement().getClassList().add(DynamoConstants.CSS_LAST_VISITED);
			}
		}
	}

	@Override
	public List<? extends Component> getRootChildren(MenuBar root) {
		return root.getItems();
	}

}
