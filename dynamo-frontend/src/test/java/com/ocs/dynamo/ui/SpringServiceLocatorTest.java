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
import org.junit.Test;

import com.ocs.dynamo.dao.impl.TestEntityDao;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.service.ServiceLocator;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.test.BackendIntegrationTest;

/**
 * Basic test class for the ServiceLocator
 * 
 * @author bas.rutten
 */
public class SpringServiceLocatorTest extends BackendIntegrationTest {

	private ServiceLocator serviceLocator = ServiceLocatorFactory.getServiceLocator();

	@Test
	public void testGetEntityModelFactory() {
		Assert.assertNotNull(serviceLocator.getEntityModelFactory());
	}

	@Test
	public void testGetService() {
		Assert.assertNotNull(serviceLocator.getService(TestEntityDao.class));
	}

	@Test
	public void testGetServiceForEntity() {
		Assert.assertNotNull(serviceLocator.getServiceForEntity(TestEntity.class));
	}

	@Test
	public void testGetMessageService() {
		Assert.assertNotNull(serviceLocator.getMessageService());
	}

}
