package com.ocs.dynamo.test;

import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Base class for testing Spring beans. Automatically injects all dependencies
 * annotated with "@Mock" into the bean
 * 
 * @author bas.rutten
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class BaseMockitoTest {

	private GenericApplicationContext applicationContext;

	@Before
	public void setUp() throws Exception {
		setupApplicationContext();
	}

	protected <T> T wireTestSubject(T subject) {
		applicationContext.getAutowireCapableBeanFactory().autowireBean(subject);
		initialize(subject);
		return subject;
	}

	protected void initialize(Object subject) {
		applicationContext.getAutowireCapableBeanFactory().initializeBean(subject,
		        subject.getClass().getSimpleName());
	}

	protected void addBeanToContext(Object bean) {
		applicationContext.getAutowireCapableBeanFactory().autowireBean(bean);
		applicationContext.getBeanFactory().registerSingleton(bean.getClass().getName(), bean);
	}

	public void addBeanToContext(String qualifier, Object bean) {
		applicationContext.getAutowireCapableBeanFactory().autowireBean(bean);
		applicationContext.getBeanFactory().registerSingleton(qualifier, bean);
	}

	public void registerProperties(String name, Properties properties) {
		applicationContext.getBeanFactory().registerSingleton(name, properties);
	}

	public void registerProperties(String name, Map<String, Object> properties) {
		applicationContext.getBeanFactory().registerSingleton(name, properties);
	}

	protected void setupApplicationContext() {
		this.applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.refresh();
		applicationContext.start();
		MockitoSpringUtil.registerMocks(applicationContext.getBeanFactory(), this);
	}

}
