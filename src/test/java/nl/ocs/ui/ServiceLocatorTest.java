package nl.ocs.ui;

import junitx.util.PrivateAccessor;
import nl.ocs.dao.TestEntityDao;
import nl.ocs.domain.TestEntity;
import nl.ocs.test.BaseIntegrationTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Basic test class for the ServiceLocator
 * 
 * @author bas.rutten
 * 
 */
public class ServiceLocatorTest extends BaseIntegrationTest {

	@Before
	public void setup() throws NoSuchFieldException {
		PrivateAccessor.setField(ServiceLocator.class, "ctx", this.applicationContext);
	}

	@Test
	public void testGetEntityModelFactory() {
		Assert.assertNotNull(ServiceLocator.getEntityModelFactory());
	}

	@Test
	public void testGetService() {
		Assert.assertNotNull(ServiceLocator.getService(TestEntityDao.class));
	}

	@Test
	public void testGetServiceForEntity() {
		Assert.assertNotNull(ServiceLocator.getServiceForEntity(TestEntity.class));
	}

	@Test
	public void testGetMessageService() {
		Assert.assertNotNull(ServiceLocator.getMessageService());
	}

}
