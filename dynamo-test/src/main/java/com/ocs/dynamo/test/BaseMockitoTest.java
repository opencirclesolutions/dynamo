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

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import com.ocs.dynamo.constants.DynamoConstants;

/**
 * Base class for testing Spring beans. Automatically injects all dependencies
 * annotated with "@Mock" into the bean
 * 
 * @author bas.rutten
 */
@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public abstract class BaseMockitoTest {

	/**
	 * The Spring application context
	 */
	private GenericApplicationContext applicationContext;

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(DynamoConstants.SP_SERVICE_LOCATOR_CLASS_NAME, "com.ocs.dynamo.ui.SpringTestServiceLocator");
	}

	/**
	 * Adds a bean to the application context context under the bean's class
	 * name
	 * 
	 * @param bean
	 *            the bean
	 */
	protected void addBeanToContext(Object bean) {
		applicationContext.getAutowireCapableBeanFactory().autowireBean(bean);
		applicationContext.getBeanFactory().registerSingleton(bean.getClass().getName(), bean);
	}

	/**
	 * Adds a bean to the application context under the provided name
	 * 
	 * @param qualifier
	 *            the name
	 * @param bean
	 *            the bean to add
	 */
	public void addBeanToContext(String qualifier, Object bean) {
		applicationContext.getAutowireCapableBeanFactory().autowireBean(bean);
		applicationContext.getBeanFactory().registerSingleton(qualifier, bean);
	}

	/**
	 * 
	 * @param str
	 * @return
	 */
	protected String formatNumber(String str) {
		DecimalFormat df = (DecimalFormat) DecimalFormat.getNumberInstance();
		char ds = df.getDecimalFormatSymbols().getDecimalSeparator();
		return str.replace(',', ds);
	}

	/**
	 * Retrieves the map that contains the system properties
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, Object> getSystemProperties() {
		return (Map<String, Object>) applicationContext.getBean("systemProperties");
	}

	protected void initialize(Object subject) {
		applicationContext.getAutowireCapableBeanFactory().initializeBean(subject, subject.getClass().getSimpleName());
	}

	/**
	 * Registers a property map under a certain name
	 * 
	 * @param name
	 *            the name
	 * @param properties
	 *            the property map to register
	 */
	public void registerProperties(String name, Map<String, Object> properties) {
		applicationContext.getBeanFactory().registerSingleton(name, properties);
	}

	public void registerProperties(String name, Properties properties) {
		applicationContext.getBeanFactory().registerSingleton(name, properties);
	}

	@Before
	public void setUp() {
		setupApplicationContext();
	}

	protected void setupApplicationContext() {
		this.applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.refresh();
		applicationContext.start();
		MockUtil.registerMocks(applicationContext.getBeanFactory(), this);
	}

	/**
	 * Wires the test subject (the bean to test) by injecting all appropriate
	 * services and other fields into it
	 * 
	 * @param subject
	 *            the test subject to wire
	 * @return
	 */
	protected <T> T wireTestSubject(T subject) {
		applicationContext.getAutowireCapableBeanFactory().autowireBean(subject);
		initialize(subject);
		return subject;
	}
}
