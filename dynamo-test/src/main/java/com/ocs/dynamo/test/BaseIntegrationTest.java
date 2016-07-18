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

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@ContextConfiguration(locations = "classpath:META-INF/testApplicationContext.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class BaseIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

    private static final Logger LOG = Logger.getLogger(BaseIntegrationTest.class);

    @PersistenceContext
    private EntityManager entityManager;

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
     * Commits the current transaction - this means that for the remainder of the unit test, the
     * tests that follow this test will be able to see the data that is committed in this test
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
