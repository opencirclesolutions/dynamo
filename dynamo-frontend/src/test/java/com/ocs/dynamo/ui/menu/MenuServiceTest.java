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

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.vaadin.navigator.Navigator;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;

public class MenuServiceTest extends BaseIntegrationTest {

	@Inject
	private MenuService menuService;

	private Navigator navigator = Mockito.mock(Navigator.class);

	@Test
	public void testEmpty() {
		MenuBar bar = menuService.constructMenu("ocs.not.here", navigator);
		Assert.assertTrue(bar.getItems().isEmpty());
	}

	/**
	 * Test the basic menu building
	 */
	@Test
	public void testMenuStructure() {
		MenuBar bar = menuService.constructMenu("ocs.menu", navigator);
		Assert.assertEquals(2, bar.getItems().size());

		MenuItem first = bar.getItems().get(0);
		Assert.assertEquals("Menu 1", first.getText());
		Assert.assertNull(first.getCommand());

		Assert.assertEquals(2, first.getChildren().size());
		MenuItem firstSub = first.getChildren().get(0);
		Assert.assertEquals("Menu 1.1", firstSub.getText());
		Assert.assertEquals("Description 1.1", firstSub.getDescription());

		NavigateCommand command = (NavigateCommand) firstSub.getCommand();
		Assert.assertEquals("Destination 1.1", command.getDestination());
		Assert.assertEquals("3", command.getSelectedTab());

		// check that the last visited item becomes highlighted
		menuService.setLastVisited(bar, "Destination 1.1");
		Assert.assertEquals(DynamoConstants.CSS_LAST_VISITED, first.getStyleName());
	}

	/**
	 * Test that an item is disabled if the user does not have the correct role (the
	 * "destination" in the menu has to match a view name in a view annotated
	 * with @SpringView)
	 */
	@Test
	public void testDisableItem() {
		MenuBar bar = menuService.constructMenu("ocs.menu", navigator);

		MenuItem first = bar.getItems().get(0);
		MenuItem firstSub = first.getChildren().get(0);
		Assert.assertFalse(firstSub.isVisible());

		// the other items are unprotected and therefore shown
		Assert.assertTrue(first.isVisible());
	}

	/**
	 * Test that a parent item is hidden if all its children are hidden as well
	 */
	@Test
	public void testDisableItemAndParent() {
		MenuBar bar = menuService.constructMenu("ocs.menu2", navigator);

		MenuItem first = bar.getItems().get(0);
		MenuItem firstSub = first.getChildren().get(0);
		Assert.assertFalse(firstSub.isVisible());
		Assert.assertFalse(first.isVisible());

	}
}
