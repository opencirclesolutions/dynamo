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

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * A service that builds a menu in which the items are displayed below each
 * other
 * 
 * @author BasRutten
 *
 */
public class VerticalMenuService extends BaseMenuService<Button, Accordion> {

	private Map<Component, AccordionPanel> toPanelMap = new HashMap<>();

	/**
	 * Adds child menu items to a parent item
	 * 
	 * @param parent      the parent menu item
	 * @param key         the message key
	 * @param destination the view to which to navigate
	 */
	private void addChildItems(Accordion parent, Accordion root, String key, String destination) {
		// add the child items
		int index = 1;
		String childKey = getMessageService().getMessageNoDefault(key + "." + index + "." + DISPLAY_NAME,
				VaadinUtils.getLocale());

		while (childKey != null) {
			constructMenu(parent, root, key + "." + index);
			index++;
			childKey = getMessageService().getMessageNoDefault(key + "." + index + "." + DISPLAY_NAME,
					VaadinUtils.getLocale());
		}

	}

	/**
	 * Adds a menu item
	 * 
	 * @param parent      the parent to which to add the item
	 * @param caption     the caption of the item
	 * @param hasChildren whether the menu item has any children
	 * @param mode        the screen mode
	 * @param destination the destination
	 * @param description the description of the menu item
	 * @param command     the navigation command to add to the item
	 * @return
	 */
	private Accordion addMenuItem(Accordion parent, String caption, boolean hasChildren, String mode,
			String destination, String description, NavigateCommand<Button, Accordion> command) {

		VerticalLayout layout = null;

		Accordion item = new Accordion();
		AccordionPanel panel = new AccordionPanel();
		if (command != null) {
			Button button = new Button(caption);
			button.addClassName("verticalMenuButton");
			button.addThemeVariants(ButtonVariant.LUMO_SMALL);
			button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
			button.addClickListener(event -> command.onComponentEvent(event));

			if (description != null) {
				VaadinUtils.setTooltip(button, description);
			}

			if (!StringUtils.isEmpty(destination)) {
				addDestination(destination, mode, button);
			}

			panel.setSummary(button);
			toPanelMap.put(button, panel);
		} else {
			Span span = new Span(caption);
			panel.setSummary(span);
			if (description != null) {
				VaadinUtils.setTooltip(span, description);
			}
			toPanelMap.put(span, panel);
		}

		if (hasChildren) {
			layout = new DefaultVerticalLayout(true, false);
			layout.add(item);
			panel.setContent(layout);
		} else {
			panel.addClassName("noChildren");
		}
		parent.add(panel);

		// hide menu item if user does not have permissions
		if (checker != null && !checker.isAccessAllowed(destination)) {
			panel.setVisible(false);
			if (layout != null) {
				layout.setVisible(false);
			}
		}

		return item;

	}

	/**
	 * Constructs a menu item and its children
	 * 
	 * @param bar    the main menu bar
	 * @param parent the parent component (either a menu bar or menu item) to add
	 *               the menu to
	 * @param key    the message key
	 * @return the constructed menu item
	 */
	protected Accordion constructMenu(Accordion parent, Accordion root, String key) {
		String caption = getMessageService().getMessageNoDefault(key + "." + DISPLAY_NAME, VaadinUtils.getLocale());

		Accordion menuItem = null;
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
			NavigateCommand<Button, Accordion> command = createNavigationCommand(root, destination, tabIndex, mode);
			boolean hasChildren = hasChildren(key);

			menuItem = addMenuItem(parent, caption, hasChildren, mode, destination, description, command);

			if (hasChildren) {
				addChildItems(menuItem, root, key, destination);
			}

		}
		return menuItem;
	}

	/**
	 * Constructs a vertical menu
	 * 
	 * @param rootName the root name (prefix) of the messages that are used to
	 *                 populate the menu
	 * @return
	 */
	public Accordion constructMenu(String rootName) {
		Accordion mainMenu = new Accordion();
		mainMenu.setClassName("dynamoVerticalMenuMain");

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

	private List<Component> getChildren(Component comp) {
		if (comp instanceof AccordionPanel) {
			return ((AccordionPanel) comp).getContent().collect(Collectors.toList());
		}

		return comp.getChildren().collect(Collectors.toList());
	}

	@Override
	public List<Component> getRootChildren(Accordion root) {
		return getChildren(root);
	}

	/**
	 * Checks whether the menu item with the specified key has any children
	 * 
	 * @param key the key of the menu item
	 * @return
	 */
	private boolean hasChildren(String key) {
		int index = 1;
		String childKey = getMessageService().getMessageNoDefault(key + "." + index + "." + DISPLAY_NAME,
				VaadinUtils.getLocale());
		while (childKey != null) {
			index++;
			childKey = getMessageService().getMessageNoDefault(key + "." + index + "." + DISPLAY_NAME,
					VaadinUtils.getLocale());
		}

		return index > 1;
	}

	/**
	 * Checks whether the provided item has a sub menu item with the provided
	 * destination
	 * 
	 * @param item        the menu item
	 * @param destination the destination
	 * @return
	 */
	private boolean hasChildWithDestination(Component item, String destination) {
		if (!(item instanceof Button)) {
			List<Component> children = getChildren(item);
			if (item instanceof AccordionPanel) {
				AccordionPanel panel = (AccordionPanel) item;
				Component child = (Component) panel.getSummary();
				return hasChildWithDestination(child, destination);
			} else {
				return children.stream().anyMatch(it -> hasChildWithDestination(it, destination));
			}
		} else {
			// look in the destination map for a match
			Set<Entry<String, Button>> possibleDestinations = findDestinations(destination);
			return possibleDestinations.stream().anyMatch(e -> e.getValue().equals(item));
		}
	}

	/**
	 * Hides a menu item if all its children are hidden
	 * 
	 * @param item the menu item
	 * @return
	 */
	private boolean hideIfAllChildrenHidden(Component item) {
		List<Component> children = getChildren(item);
		if (!children.isEmpty()) {
			boolean found = false;

			for (Component child : children) {
				boolean visible = hideIfAllChildrenHidden(child);
				found |= visible;
			}

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
	protected void hideRecursively(Accordion accordion) {
		for (Component item : getChildren(accordion)) {
			hideIfAllChildrenHidden(item);
		}
	}

	@Override
	protected void onSetVisible(Component comp, boolean visible) {
		AccordionPanel panel = toPanelMap.get(comp);
		if (panel != null) {
			panel.setVisible(visible);
		}
	}

	@Override
	public void setLastVisited(Accordion parent, String destination) {
		this.lastVisited = destination;
		List<Component> children = getChildren(parent);
		for (Component comp : children) {
			comp.getElement().getClassList().remove(DynamoConstants.CSS_LAST_VISITED);
		}

		for (Component item : children) {
			item.getElement().getClassList().remove(DynamoConstants.CSS_LAST_VISITED);
			if (hasChildWithDestination(item, destination)) {
				item.getElement().getClassList().add(DynamoConstants.CSS_LAST_VISITED);
			}
		}
	}
}
