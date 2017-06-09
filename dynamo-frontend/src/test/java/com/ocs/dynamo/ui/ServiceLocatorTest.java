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
package com.ocs.dynamo.ui;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ocs.dynamo.dao.impl.TestEntityDao;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.test.BaseIntegrationTest;

import junitx.util.PrivateAccessor;

/**
 * Basic test class for the ServiceLocator
 * 
 * @author bas.rutten
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
