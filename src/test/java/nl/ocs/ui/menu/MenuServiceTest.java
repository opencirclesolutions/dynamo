package nl.ocs.ui.menu;

import javax.inject.Inject;

import nl.ocs.test.BaseIntegrationTest;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

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

		NavigateCommand command = (NavigateCommand) firstSub.getCommand();
		Assert.assertEquals("Destination 1.1", command.getDestination());
		Assert.assertEquals("3", command.getSelectedTab());
	}

	/**
	 * Test that an item is disabled if the user does not have the correct role
	 * (the "destination" in the menu has to match a view name in a view
	 * annotated with
	 * 
	 * @SpringView)
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
