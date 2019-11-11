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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ocs.dynamo.ui.FrontendIntegrationTest;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.menubar.MenuBar;

public class MenuServiceTest extends FrontendIntegrationTest {

    @Autowired
    private MenuService menuService;

    @Test
    public void testEmpty() {
        MenuBar bar = menuService.constructMenu("ocs.not.here");
        Assert.assertTrue(bar.getItems().isEmpty());
    }

    /**
     * Test the basic menu building
     */
    @Test
    public void testMenuStructure() {
        MenuBar bar = menuService.constructMenu("ocs.menu");
        Assert.assertEquals(2, bar.getItems().size());

        MenuItem first = bar.getItems().get(0);
        Assert.assertEquals("Menu 1", first.getText());

        Assert.assertEquals(2, first.getSubMenu().getItems().size());
        MenuItem firstSub = first.getSubMenu().getItems().get(0);
        Assert.assertEquals("Menu 1.1", firstSub.getText());

        MenuItem second = bar.getItems().get(1);
        Assert.assertEquals("Menu 2", second.getText());

    }

    /**
     * Test that an item is disabled if the user does not have the correct role (the
     * "destination" in the menu has to match a view name in a view annotated
     * with @SpringView)
     */
    @Test
    public void testDisableItem() {
        MenuBar bar = menuService.constructMenu("ocs.menu");

        MenuItem first = bar.getItems().get(0);
        MenuItem firstSub = (MenuItem) first.getSubMenu().getChildren().findFirst().orElse(null);
        Assert.assertFalse(firstSub.isVisible());

        // the other items are unprotected and therefore shown
        Assert.assertTrue(first.isVisible());
    }

    /**
     * Test that a parent item is hidden if all its children are hidden as well
     */
    @Test
    public void testDisableItemAndParent() {
        MenuBar bar = menuService.constructMenu("ocs.menu2");

        MenuItem first = bar.getItems().get(0);
        MenuItem firstSub = (MenuItem) first.getSubMenu().getChildren().findFirst().orElse(null);
        Assert.assertFalse(firstSub.isVisible());
        Assert.assertFalse(first.isVisible());
    }

    @Test
    public void testSetVisible() {
        MenuBar bar = menuService.constructMenu("ocs.menu");
        menuService.setVisible(bar, "Destination 1.1", false);

        MenuItem first = bar.getItems().get(0);
        MenuItem firstSub = (MenuItem) first.getSubMenu().getChildren().findFirst().orElse(null);
        Assert.assertTrue(first.isVisible());
        Assert.assertFalse(firstSub.isVisible());

        menuService.setVisible(bar, "Destination 1.1", true);
        Assert.assertTrue(firstSub.isVisible());

        // hide all children and verify that the parent is hidden as well
        menuService.setVisible(bar, "Destination 1.1", false);
        menuService.setVisible(bar, "Destination 1.2", false);
        menuService.setVisible(bar, "Destination 1.3", false);
        Assert.assertFalse(firstSub.isVisible());
    }

}
