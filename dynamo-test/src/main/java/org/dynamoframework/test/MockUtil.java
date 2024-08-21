package org.dynamoframework.test;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.dynamoframework.dao.BaseDao;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.exception.OCSRuntimeException;
import org.dynamoframework.service.BaseService;
import org.dynamoframework.service.MessageService;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Utility class for registering service and DAO related mock functionality
 *
 * @author bas.rutten
 */
public final class MockUtil {

    /**
     * Capture a call of the "save" method on a DAO
     *
     * @param dao   the DAO
     * @param clazz the class of the entity being saved
     * @return the saved entity
     */
    public static <ID, X extends AbstractEntity<ID>> X captureSave(BaseDao<ID, X> dao, Class<X> clazz) {
        ArgumentCaptor<X> captor = ArgumentCaptor.forClass(clazz);
        verify(dao).save(captor.capture());
        return captor.getValue();
    }

    /**
     * Captures the call to the "save" method (that saves a list of entities) on a
     * DAO
     *
     * @param dao the DAO that is being called
     * @return the saved list
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <ID, X extends AbstractEntity<ID>> List<X> captureSaveList(BaseDao<ID, X> dao) {
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(dao).save(captor.capture());
        return captor.getValue();
    }

    /**
     * Capture multipe calls to the "save" method on a DAO
     *
     * @param dao   the DAO
     * @param times the desired number of method calls
     */
    public static <ID, X extends AbstractEntity<ID>> List<X> captureSaves(BaseDao<ID, X> dao, Class<X> clazz,
                                                                          int times) {
        ArgumentCaptor<X> captor = ArgumentCaptor.forClass(clazz);
        verify(dao, times(times)).save(captor.capture());
        return captor.getAllValues();
    }

    /**
     * Capture the call of the "save" method on a service
     *
     * @param service the service
     * @return the captured value
     */
    public static <ID, X extends AbstractEntity<ID>> X captureServiceSave(BaseService<ID, X> service, Class<X> clazz) {
        ArgumentCaptor<X> captor = ArgumentCaptor.forClass(clazz);
        verify(service).save(captor.capture());
        return captor.getValue();
    }

    /**
     * Capture the call to the "save" service method that accepts a list of entities
     *
     * @param service the service
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <ID, X extends AbstractEntity<ID>> List<X> captureServiceSaveList(BaseService<ID, X> service) {
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(service).save(captor.capture());
        return captor.getValue();
    }

    /**
     * Capture multiple calls to the "save" method of a service
     *
     * @param service the service
     * @param times   the number of times the method is supposed to be called
     * @return
     */
    public static <ID, X extends AbstractEntity<ID>> List<X> captureServiceSaves(BaseService<ID, X> service,
                                                                                 Class<X> clazz, int times) {
        ArgumentCaptor<X> captor = ArgumentCaptor.forClass(clazz);
        verify(service, times(times)).save(captor.capture());
        return captor.getAllValues();
    }

    /**
     * Util method to initialize the messageservice and inject it into the target
     * object when @Inject can not be used.
     *
     * @param target   Object with the field messageService of type MessageService
     * @param basename the base name of the message bundle to use
     */
    public static void injectMessageService(Object target, String basename) {
        ResourceBundleMessageSource rmb = new ResourceBundleMessageSource();
        rmb.setBasename(basename);
        MessageService ms = spy(MessageService.class);
        ReflectionTestUtils.setField(ms, "source", rmb);
        ReflectionTestUtils.setField(target, "messageService", ms);
    }

    /**
     * Mocks the "fetchById" method of a DAO by returning the provided entity
     */
    public static <ID, X extends AbstractEntity<ID>> void mockFetchById(BaseDao<ID, X> dao, ID id, X entity) {
        when(dao.fetchById(eq(id), any())).thenReturn(entity);
    }

    /**
     * Set up the message service to simply return the first argument passed to it -
     * this allows for easily checking if the message service is called with the
     * correct parameter
     *
     * @param messageService the message service
     */
    public static void mockMessageService(MessageService messageService) {
        // use somewhat wonky new varargs syntax
        lenient().when(messageService.getMessage(anyString(), nullable(Locale.class),
                        Mockito.any(Object[].class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        // method for retrieving enum message
        lenient().when(messageService.getEnumMessage(any(), any(Enum.class), nullable(Locale.class)))
                .thenAnswer(invocation -> invocation.getArguments()[1].toString());
    }

    /**
     * Mocks a DAO save operation, making sure that the argument that is passed to
     * the method is returned from the method as well
     *
     * @param dao   the DAO that must be called
     * @param clazz the class of the entity
     */
    public static <ID, X extends AbstractEntity<ID>> void mockSave(BaseDao<ID, X> dao, Class<X> clazz) {
        // mock the save behaviour - return the first argument being passed to the
        // method
        when(dao.save(any(clazz))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    /**
     * Mock the saving of a list on a DAO
     *
     * @param dao the DAO
     */
    @SuppressWarnings("unchecked")
    public static <ID, X extends AbstractEntity<ID>> void mockSaveList(BaseDao<ID, X> dao) {
        when(dao.save(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    /**
     * Mocks a service save operation, making sure that the argument that is passed
     * to the method is returned from the method as well
     *
     * @param service the service
     */
    public static <ID, U extends AbstractEntity<ID>> void mockServiceSave(BaseService<ID, U> service, Class<U> clazz) {
        when(service.save(any(clazz))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    /**
     * Mock the saving of a list on a service
     *
     * @param service the service
     */
    @SuppressWarnings("unchecked")
    public static <ID, X extends AbstractEntity<ID>> void mockServiceSaveList(BaseService<ID, X> service) {
        when(service.save(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    /**
     * Registers all fields that are annotated with "@Mock" as beans in the Spring
     * context
     *
     * @param factory the Spring bean factory
     * @param subject the subject from which to register the beans
     */
    public static void registerMocks(ConfigurableListableBeanFactory factory, Object subject) {
        registerMocks(factory, subject, subject.getClass());
    }

    /**
     * Registers all fields that are annotated with "@Mock" as beans in the Spring
     * context
     *
     * @param factory
     * @param subject
     * @param clazz
     */
    public static void registerMocks(ConfigurableListableBeanFactory factory, Object subject, Class<?> clazz) {
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

    private MockUtil() {
        // hidden constructor
    }

}
