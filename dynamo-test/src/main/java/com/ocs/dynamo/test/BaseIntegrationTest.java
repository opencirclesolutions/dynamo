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
package com.ocs.dynamo.test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.ocs.dynamo.constants.DynamoConstants;

@ContextConfiguration(locations = "classpath:META-INF/testApplicationContext.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class BaseIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	private static final Logger LOG = Logger.getLogger(BaseIntegrationTest.class);

	@Autowired
	private PlatformTransactionManager transactionManager;

	@PersistenceContext
	protected EntityManager entityManager;

	@BeforeClass
	public static void beforeClass() {
		// make sure the test service locator is loaded
		System.setProperty(DynamoConstants.SP_SERVICE_LOCATOR_CLASS_NAME, "com.ocs.dynamo.ui.SpringTestServiceLocator");
	}

	protected Logger getLog() {
		return LOG;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void wait(int miliSeconds) {
		try {
			Thread.sleep(miliSeconds);
		} catch (InterruptedException ex) {
			Assert.fail("Waiting period was interrupted");
		}
	}

	protected TransactionStatus startTransaction() {
		return transactionManager
				.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
	}

	protected void commitTransaction(TransactionStatus status) {
		transactionManager.commit(status);
	}

}
