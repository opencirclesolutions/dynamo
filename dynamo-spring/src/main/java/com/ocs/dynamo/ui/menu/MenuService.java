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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.auth.DefaultPermissionCheckerImpl;
import com.vaadin.navigator.Navigator;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;

/**
 * Service for creating a menu based on property files. Use the "menu.properties" file to configure
 * this menu.
 * 
 * @author bas.rutten
 */
public class MenuService {

    public static final String DESTINATION = "destination";

    public static final String DISPLAY_NAME = "displayName";

    public static final String MODE = "mode";

    public static final String ROLES = "roles";

    public static final String TAB_INDEX = "tabIndex";

    @Autowired(required = false)
    private DefaultPermissionCheckerImpl checker;

    @Autowired
    private MessageService messageService;

    /**
     * Constructs a menu item and its children
     * 
     * @param parent
     *            the parent component (either a menu bar or menu item) to add the menu to
     * @param key
     *            the message key
     * @param navigator
     *            the navigator component
     * @return the constructed menu item
     */
    private MenuItem constructMenu(Object parent, String key, Navigator navigator) {
        String caption = messageService.getMessageNoDefault(key + "." + DISPLAY_NAME);

        MenuItem menuItem = null;
        // When no caption exists, return no instance of menuItem (null)
        if (!StringUtils.isEmpty(caption)) {
            // look up the messages
            String destination = messageService.getMessageNoDefault(key + "." + DESTINATION);
            String tabIndex = messageService.getMessageNoDefault(key + "." + TAB_INDEX);
            String mode = messageService.getMessageNoDefault(key + "." + MODE);

            // create navigation command
            Command command = null;
            if (!StringUtils.isEmpty(destination)) {
                command = new NavigateCommand(navigator, destination, tabIndex, mode);
            }

            // create menu item
            if (parent instanceof MenuBar) {
                menuItem = ((MenuBar) parent).addItem(caption, command);
            } else {
                menuItem = ((MenuItem) parent).addItem(caption, command);
            }

            // add the child items
            int index = 1;
            String childKey = messageService.getMessageNoDefault(key + "." + index + "."
                    + DISPLAY_NAME);

            while (childKey != null) {
                constructMenu(menuItem, key + "." + index, navigator);
                index++;
                childKey = messageService.getMessageNoDefault(key + "." + index + "."
                        + DISPLAY_NAME);
            }

            if (checker != null && !checker.isAccessAllowed(destination)) {
                menuItem.setVisible(false);
            }

        }
        return menuItem;
    }

    /**
     * Constructs a menu
     * 
     * @param rootName
     *            the root name (prefix) of the messages that are used to populate the menu
     * @param navigator
     *            Vaadin navigator
     * @return
     */
    public MenuBar constructMenu(String rootName, Navigator navigator) {
        MenuBar mainMenu = new MenuBar();

        // look up any messages of the form "rootName.i"
        int i = 1;
        while (true) {
            if (constructMenu(mainMenu, rootName + "." + i, navigator) == null) {
                break;
            }
            i++;
        }

        // hide any menu items for which the user has no access rights
        hideRecursively(mainMenu);

        return mainMenu;
    }

    /**
     * Hides a menu item if all its children are hidden
     * 
     * @param item
     *            the menu item
     * @return
     */
    private boolean hideIfAllChildrenHidden(MenuItem item) {
        if (item.getChildren() != null && !item.getChildren().isEmpty()) {
            // check if the item has any visible children
            boolean found = false;
            for (MenuItem child : item.getChildren()) {
                boolean visible = hideIfAllChildrenHidden(child);
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
     * Recursively process all menu items and hides the items that have no visible children
     * 
     * @param bar
     */
    private void hideRecursively(MenuBar bar) {
        for (MenuItem item : bar.getItems()) {
            hideIfAllChildrenHidden(item);
        }
    }
}
