package nl.ocs.ui.auth;

import java.util.List;

import nl.ocs.service.UserDetailsService;
import nl.ocs.test.BaseMockitoTest;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

public class PermissionCheckerTest extends BaseMockitoTest {

	public PermissionChecker checker = new PermissionChecker("nl.ocs");

	@Mock
	private UserDetailsService userDetailsService;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		wireTestSubject(checker);
	}

	@Test
	public void testFindViews() {
		checker.postConstruct();

		// there are two views, but one of them does not have any roles and is
		// therefore skipped
		List<String> viewNames = checker.getViewNames();
		Assert.assertEquals(2, viewNames.size());

		// there are two view
		Assert.assertTrue(viewNames.contains("TestView"));
		Assert.assertTrue(viewNames.contains("Destination 1.1"));
	}

	/**
	 * Test that the "edit only" setting is correctly set
	 */
	@Test
	public void testEditOnly() {
		Assert.assertTrue(checker.isEditOnly("TestView"));
		Assert.assertTrue(checker.isEditOnly("Destination 1.1"));
	}
}
