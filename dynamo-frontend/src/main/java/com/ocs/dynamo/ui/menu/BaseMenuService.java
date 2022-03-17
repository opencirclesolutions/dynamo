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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.auth.PermissionChecker;
import com.vaadin.flow.component.Component;

import lombok.Getter;

/**
 * 
 * Base class for declarative menus
 * 
 * @author BasRutten
 * 
 *
 * @param <T> the type of the component that can be clicked to navigate to a
 *            view
 * @param <U> the type of the root component
 */
public abstract class BaseMenuService<T extends Component, U> {

	/**
	 * Optional tool tip that appears for the menu item
	 */
	public static final String DESCRIPTION = "description";

	/**
	 * The destination (name of the view) to navigate to
	 */
	public static final String DESTINATION = "destination";

	/**
	 * The display name
	 */
	public static final String DISPLAY_NAME = "displayName";

	/**
	 * The screen mode. Optional, is stored on the BaseUI
	 */
	public static final String MODE = "mode";

	/**
	 * The tab index of the tab to display. Optional, is stored on the BaseUI and
	 * can be retrieved from there
	 */
	public static final String TAB_INDEX = "tabIndex";

	/**
	 * The last visited menu item
	 */
	protected String lastVisited;

	/**
	 * Permission checker used for determining if users are allowed to access a page
	 */
	@Autowired(required = false)
	@Getter
	protected PermissionChecker checker;

	/**
	 * Mapping from view name (destination) to component
	 */
	private Map<String, T> destinationMap = new HashMap<>();

	@Autowired
	@Getter
	private MessageService messageService;

	/**
	 * Adds a destination to the map
	 * 
	 * @param destination the destination view
	 * @param mode        the screen mode
	 * @param component   the component that must be clicked to navigate to the view
	 */
	protected void addDestination(String destination, String mode, T component) {
		destinationMap.put(destination + "#" + (mode != null ? mode : "nomode"), component);
	}

	/**
	 * Creates a command for navigating to a certain view
	 * 
	 * @param root        the root component
	 * @param destination the destination string
	 * @param tabIndex    the tab index to select
	 * @param mode        the screen mode to select
	 * @return
	 */
	protected NavigateCommand<T, U> createNavigationCommand(U root, String destination, String tabIndex, String mode) {
		NavigateCommand<T, U> command = null;
		if (!StringUtils.isEmpty(destination)) {
			command = new NavigateCommand<>(this, root, destination, tabIndex, mode);
		}
		return command;
	}

	protected Set<Entry<String, T>> findDestinations(String destination) {
		return destinationMap.entrySet().stream().filter(e -> e.getKey().startsWith(destination))
				.collect(Collectors.toSet());
	}

	protected T getDestination(String destination, String mode) {
		return destinationMap.get(destination + "#" + (mode != null ? mode : "nomode"));
	}

	/**
	 * Returns the children of the root element of the menu
	 * 
	 * @param root the root element
	 * @return
	 */
	public abstract List<? extends Component> getRootChildren(U root);

	/**
	 * Hides a menu item if the user does not have permissions to see the associated
	 * screen
	 * 
	 * @param menuItem    the menu item
	 * @param destination the destination
	 */
	protected void hideIfNoPermission(Component menuItem, String destination) {
		// hide menu item if user does not have permissions
		if (checker != null && !checker.isAccessAllowed(destination)) {
			menuItem.setVisible(false);
		}
	}

	/**
	 * Recursively hides all menu items for which all children are hidden
	 * 
	 * @param root the root of the menu
	 */
	protected abstract void hideRecursively(U root);

	/**
	 * 
	 * @param comp
	 * @param visible
	 */
	protected void onSetVisible(Component comp, boolean visible) {
		// override in subclasses
	}

	/**
	 * Sets the last visited destination
	 * 
	 * @param parent      the root of the menu
	 * @param destination the destination
	 */
	public abstract void setLastVisited(U root, String destination);

	/**
	 * Sets the visibility of a certain menu item
	 * 
	 * @param item        the item
	 * @param destination the destination
	 * @param mode        the screen mode
	 * @param visible     the desired visibility
	 */
	private void setVisible(Component item, String destination, String mode, boolean visible) {
		T mi = getDestination(destination, mode);
		if (mi != null) {
			mi.setVisible(visible);
			onSetVisible(mi, visible);
		}
	}

	/**
	 * Sets the visibility of a certain item item based on its destination (and
	 * regardless of screen mode)
	 * 
	 * @param root        the root of the menu
	 * @param destination the logical name of the destination
	 * @param visible     whether to set the item to visible
	 */
	public void setVisible(U root, String destination, boolean visible) {
		setVisible(root, destination, null, visible);
	}

	/**
	 * Sets the visibility of a menu item for a certain destination (and mode)
	 * 
	 * @param root        the root of the menu
	 * @param destination the destination
	 * @param mode        the screen mode
	 * @param visible     the desired visibility
	 */
	public void setVisible(U root, String destination, String mode, boolean visible) {
		List<? extends Component> children = getRootChildren(root);
		for (Component child : children) {
			setVisible(child, destination, mode, visible);
		}
		hideRecursively(root);
	}

}
