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

import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Base class for testing Spring beans. Automatically injects all dependencies annotated with
 * "@Mock" into the bean
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
