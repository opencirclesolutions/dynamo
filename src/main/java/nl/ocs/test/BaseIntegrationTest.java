package nl.ocs.test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = "classpath:META-INF/testApplicationContext.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class BaseIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	private static final Logger LOG = Logger.getLogger(BaseIntegrationTest.class);

	@PersistenceContext
	protected EntityManager entityManager;

	protected Logger getLog() {
		return LOG;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/**
	 * Commits the current transaction - this means that for the remainder of
	 * the unit test, the tests that follow this test will be able to see the
	 * data that is committed in this test
	 */
	public void commitTransaction() {
		getEntityManager().getTransaction().commit();
	}

	public void beginTransaction() {
		getEntityManager().getTransaction().begin();
	}

	public void commitAndStartTransaction() {
		commitTransaction();
		beginTransaction();
	}

	public void rollBackTransaction() {
		getEntityManager().getTransaction().rollback();
	}

	public void wait(int miliSeconds) {
		try {
			Thread.sleep(miliSeconds);
		} catch (InterruptedException ex) {
			Assert.fail("Waiting period was interrupted");
		}
	}

}
