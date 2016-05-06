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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;

/**
 * Utility class for registering service and DAO related mock functionality
 * 
 * @author bas.rutten
 */
public final class MockUtil {

    private MockUtil() {
        // hidden constructor
    }

    /**
     * Capture a call of the "save" method on a DAO
     * 
     * @param dao
     *            the DAO
     * @param clazz
     *            the class of the entity being saved
     * @return
     */
    public static <ID, X extends AbstractEntity<ID>> X captureSave(BaseDao<ID, X> dao,
            Class<X> clazz) {
        ArgumentCaptor<X> captor = ArgumentCaptor.forClass(clazz);
        Mockito.verify(dao).save(captor.capture());
        return captor.getValue();
    }

    /**
     * Captures the call to the "save" method (that saves a list of entities) on a DAO
     * 
     * @param dao
     *            the DAO that is being called
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <ID, X extends AbstractEntity<ID>> List<X> captureSaveList(BaseDao<ID, X> dao) {
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(dao).save(captor.capture());
        return (List<X>) captor.getValue();
    }

    /**
     * Capture multipe calls to the "save" method on a DAO
     * 
     * @param dao
     *            the DAO
     * @param clazz
     *            the class of the entity being saved
     * @param times
     *            the desired number of method calls
     */
    public static <ID, X extends AbstractEntity<ID>> List<X> captureSaves(BaseDao<ID, X> dao,
            Class<X> clazz, int times) {
        ArgumentCaptor<X> captor = ArgumentCaptor.forClass(clazz);
        Mockito.verify(dao, Mockito.times(times)).save(captor.capture());
        return captor.getAllValues();
    }

    /**
     * Capture the call of the "save" method on a service
     * 
     * @param service
     *            the service
     * @param clazz
     *            the class of the entity being saved
     * @return
     */
    public static <ID, X extends AbstractEntity<ID>> X captureServiceSave(
            BaseService<ID, X> service, Class<X> clazz) {
        ArgumentCaptor<X> captor = ArgumentCaptor.forClass(clazz);
        Mockito.verify(service).save(captor.capture());
        return captor.getValue();
    }

    /**
     * Capture the call to the "save" service method that accepts a list of entities
     * 
     * @param service
     *            the service
     * @param clazz
     *            the class of the entities that are being saved
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <ID, X extends AbstractEntity<ID>> List<X> captureServiceSaveList(
            BaseService<ID, X> service) {
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(service).save(captor.capture());
        return (List<X>) captor.getValue();
    }

    /**
     * Capture multiple calls to the "save" method of a service
     * 
     * @param service
     *            the service
     * @param clazz
     *            the class of the object that is saved
     * @param times
     *            the number of times the method is supposed to be called
     * @return
     */
    public static <ID, X extends AbstractEntity<ID>> List<X> captureServiceSaves(
            BaseService<ID, X> service, Class<X> clazz, int times) {
        ArgumentCaptor<X> captor = ArgumentCaptor.forClass(clazz);
        Mockito.verify(service, Mockito.times(times)).save(captor.capture());
        return captor.getAllValues();
    }

    /**
     * Set up the message service to simply return the first argument passed to it - this allows for
     * easily checking if the message service is called with the correct parameter
     * 
     * @param messageService
     */
    @SuppressWarnings("unchecked")
    public static void mockMessageService(MessageService messageService) {
        // method without any arguments
        Mockito.when(messageService.getMessage(Matchers.anyString())).thenAnswer(
                new Answer<String>() {

                    @Override
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        return (String) invocation.getArguments()[0];
                    }

                });
        // method with varargs
        Mockito.when(messageService.getMessage(Matchers.anyString(), Matchers.anyVararg()))
                .thenAnswer(new Answer<String>() {

                    @Override
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        return (String) invocation.getArguments()[0];
                    }

                });

        // method with locale
        Mockito.when(messageService.getMessage(Matchers.anyString(), Matchers.any(Locale.class)))
                .thenAnswer(new Answer<String>() {

                    @Override
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        return (String) invocation.getArguments()[0];
                    }

                });

        // method for retrieving enum message
        Mockito.when(
                messageService.getEnumMessage(Matchers.any(Class.class), Matchers.any(Enum.class)))
                .thenAnswer(new Answer<String>() {

                    @Override
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        return invocation.getArguments()[1].toString();
                    }

                });
    }

    /**
     * Mocks a DAO save operation, making sure that the argument that is passed to the method is
     * returned from the method as well
     * 
     * @param dao
     *            the DAO that must be called
     * @param clazz
     *            the class of the entity
     */
    @SuppressWarnings("unchecked")
    public static <ID, X extends AbstractEntity<ID>> void mockSave(BaseDao<ID, X> dao,
            Class<X> clazz) {
        // mock the save behaviour - return the first argument being passed to the method
        Mockito.when(dao.save(Matchers.any(clazz))).thenAnswer(new Answer<X>() {

            @Override
            public X answer(InvocationOnMock invocation) throws Throwable {
                return (X) invocation.getArguments()[0];
            }

        });
    }

    /**
     * Mocks a service save operation, making sure that the argument that is passed to the method is
     * returned from the method as well
     * 
     * @param service
     * @param clazz
     */
    @SuppressWarnings("unchecked")
    public static <ID, U extends AbstractEntity<ID>> void mockServiceSave(
            BaseService<ID, U> service, Class<U> clazz) {
        // mock the save behaviour
        Mockito.when(service.save(Matchers.any(clazz))).thenAnswer(new Answer<U>() {

            @Override
            public U answer(InvocationOnMock invocation) throws Throwable {
                return (U) invocation.getArguments()[0];
            }

        });
    }

    /**
     * Registers all fields that are annotated with "@Mock" as beans in the Spring context
     * 
     * @param factory
     * @param subject
     */
    public static void registerMocks(ConfigurableListableBeanFactory factory, Object subject) {
        registerMocks(factory, subject, subject.getClass());
    }

    /**
     * Registers all fields that are annotated with "@Mock" as beans in the Spring context
     * 
     * @param factory
     * @param subject
     * @param clazz
     */
    public static void registerMocks(ConfigurableListableBeanFactory factory, Object subject,
            Class<?> clazz) {
        try {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.getAnnotation(Mock.class) != null) {
                    factory.registerSingleton(field.getName(), field.get(subject));
                }
            }
            if (clazz.getSuperclass() != null) {
                registerMocks(factory, subject, clazz.getSuperclass());
            }
        } catch (Exception e) {
            throw new OCSRuntimeException(e.getMessage(), e);
        }
    }

}
