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

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.auth.PermissionChecker;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;

/**
 * Service for creating a menu based on property files. Use the
 * "menu.properties" file to configure this menu.
 * 
 * @author bas.rutten
 */
public class MenuService {

    /**
     * The last visited menu item
     */
    private String lastVisited;

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
     * Permission checker used for determining if users are allowed to access a page
     */
    @Autowired(required = false)
    private PermissionChecker checker;

    private Map<String, MenuItem> destinationMap = new HashMap<>();

    @Autowired
    private MessageService messageService;

    /**
     * Constructs a menu item and its children
     * 
     * @param bar    the main menu bar
     * @param parent the parent component (either a menu bar or menu item) to add
     *               the menu to
     * @param key    the message key
     * @return the constructed menu item
     */
    private MenuItem constructMenu(MenuBar bar, Object parent, String key) {
        String caption = messageService.getMessageNoDefault(key + "." + DISPLAY_NAME, VaadinUtils.getLocale());

        MenuItem menuItem = null;
        // When no caption exists, return no instance of menuItem (null)
        if (!StringUtils.isEmpty(caption)) {
            // look up the messages
            String destination = messageService.getMessageNoDefault(key + "." + DESTINATION, VaadinUtils.getLocale());
            String tabIndex = messageService.getMessageNoDefault(key + "." + TAB_INDEX, VaadinUtils.getLocale());
            String mode = messageService.getMessageNoDefault(key + "." + MODE, VaadinUtils.getLocale());
            String description = messageService.getMessageNoDefault(key + "." + DESCRIPTION, VaadinUtils.getLocale());

            // create navigation command
            NavigateCommand command = null;
            if (!StringUtils.isEmpty(destination)) {
                command = new NavigateCommand(this, bar, destination, tabIndex, mode);
            }

            if (parent instanceof MenuBar) {
                // main menu bar
                menuItem = ((MenuBar) parent).addItem(caption, command);
            } else {
                // sub menu
                menuItem = ((SubMenu) parent).addItem(caption, command);
            }

            // set description
            if (description != null) {
                VaadinUtils.setTooltip(menuItem, description);
            }

            parent = menuItem.getSubMenu();

            if (!StringUtils.isEmpty(destination)) {
                destinationMap.put(destination + "#" + mode, menuItem);
            }

            // add the child items
            int index = 1;
            String childKey = messageService.getMessageNoDefault(key + "." + index + "." + DISPLAY_NAME, VaadinUtils.getLocale());

            while (childKey != null) {
                constructMenu(bar, parent, key + "." + index);
                index++;
                childKey = messageService.getMessageNoDefault(key + "." + index + "." + DISPLAY_NAME, VaadinUtils.getLocale());
            }

            // hide menu item if user does not have permissions
            if (checker != null && !checker.isAccessAllowed(destination)) {
                menuItem.setVisible(false);
            }
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

    private boolean hasChildWithDestination(MenuItem item, String destination) {
        if (item.getSubMenu() != null && !item.getSubMenu().getItems().isEmpty()) {
            boolean found = false;
            for (MenuItem child : item.getSubMenu().getItems()) {
                boolean t = hasChildWithDestination(child, destination);
                found |= t;
            }
            return found;
        } else {
            // look in the destination map for any match
            Set<Entry<String, MenuItem>> possibleDestinations = destinationMap.entrySet().stream()
                    .filter(e -> e.getKey().startsWith(destination)).collect(Collectors.toSet());
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
    private void hideRecursively(MenuBar bar) {
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
//            if (item.getElement().getParent() != null) {
//                item.getElement().getParent().getClassList().remove(DynamoConstants.CSS_LAST_VISITED);
//            }
            if (hasChildWithDestination(item, destination)) {
                item.getElement().getClassList().add(DynamoConstants.CSS_LAST_VISITED);
//                if (item.getElement().getParent() != null) {
//                    item.getElement().getParent().getClassList().add(DynamoConstants.CSS_LAST_VISITED);
//                }
            }
        }
    }

    public String getLastVisited() {
        return lastVisited;
    }

    /**
     * Sets the visibility of a certain item item based on its destination (and
     * regardless of screen mode)
     * 
     * @param menu        the menu
     * @param destination the logical name of the destination
     * @param visible     whether to set the item to visible
     */
    public void setVisible(MenuBar menu, String destination, boolean visible) {
        setVisible(menu, destination, null, visible);
    }

    /**
     * Sets the visibility of a certain item identified by its destination and mode
     * 
     * @param menu        the menu bar
     * @param destination the destination
     * @param mode        the screen mode
     * 
     * @param visible     the desired visibility
     */
    public void setVisible(MenuBar menu, String destination, String mode, boolean visible) {

        List<MenuItem> items = menu.getItems();
        for (MenuItem item : items) {
            setVisible(item, destination, mode, visible);
        }
        hideRecursively(menu);
    }

    /**
     * Sets the visibility of a certain menu item if its destination matches the
     * specified destination
     * 
     * @param menu        the main menu bar
     * @param destination the destination
     * @param mode        the screen mode
     * @param visible     the desired visibility of the item
     */
    private void setVisible(MenuItem item, String destination, String mode, boolean visible) {
        MenuItem mi = destinationMap.get(destination + "#" + mode);
        if (mi != null) {
            mi.setVisible(visible);
        }
    }
}
