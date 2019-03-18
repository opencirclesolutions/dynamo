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

import static org.mockito.Mockito.spy;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.dao.FetchJoinInformation;
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

	private static final String UI_FIELD_NAME = "ui";

	/**
	 * Capture a call of the "save" method on a DAO
	 * 
	 * @param dao   the DAO
	 * @param clazz the class of the entity being saved
	 * @return
	 */
	public static <ID, X extends AbstractEntity<ID>> X captureSave(BaseDao<ID, X> dao, Class<X> clazz) {
		ArgumentCaptor<X> captor = ArgumentCaptor.forClass(clazz);
		Mockito.verify(dao).save(captor.capture());
		return captor.getValue();
	}

	/**
	 * Captures the call to the "save" method (that saves a list of entities) on a
	 * DAO
	 * 
	 * @param dao the DAO that is being called
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
	 * @param dao   the DAO
	 * @param times the desired number of method calls
	 */
	public static <ID, X extends AbstractEntity<ID>> List<X> captureSaves(BaseDao<ID, X> dao, Class<X> clazz,
			int times) {
		ArgumentCaptor<X> captor = ArgumentCaptor.forClass(clazz);
		Mockito.verify(dao, Mockito.times(times)).save(captor.capture());
		return captor.getAllValues();
	}

	/**
	 * Capture the call of the "save" method on a service
	 * 
	 * @param service the service
	 * @return
	 */
	public static <ID, X extends AbstractEntity<ID>> X captureServiceSave(BaseService<ID, X> service, Class<X> clazz) {
		ArgumentCaptor<X> captor = ArgumentCaptor.forClass(clazz);
		Mockito.verify(service).save(captor.capture());
		return captor.getValue();
	}

	/**
	 * Capture the call to the "save" service method that accepts a list of entities
	 * 
	 * @param service the service
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <ID, X extends AbstractEntity<ID>> List<X> captureServiceSaveList(BaseService<ID, X> service) {
		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
		Mockito.verify(service).save(captor.capture());
		return (List<X>) captor.getValue();
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
		Mockito.verify(service, Mockito.times(times)).save(captor.capture());
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
	 * Injects a mocked Vaadin UI into an object
	 * 
	 * @param field the object into which to inject the field
	 * @param ui    the user interface
	 */
	public static void injectUI(Object object, Object ui) {
		ReflectionTestUtils.setField(object, UI_FIELD_NAME, ui);
	}

	/**
	 * Mocks the "fetchById" method of a DAO by returning the provided entity
	 */
	public static <ID, X extends AbstractEntity<ID>> void mockFetchById(BaseDao<ID, X> dao, ID id, X entity) {
		Mockito.when(dao.fetchById(Mockito.eq(id), (FetchJoinInformation[]) Mockito.any())).thenReturn(entity);
	}

	/**
	 * Set up the message service to simply return the first argument passed to it -
	 * this allows for easily checking if the message service is called with the
	 * correct parameter
	 * 
	 * @param messageService
	 */
	public static void mockMessageService(MessageService messageService) {
		// method with varargs
		Mockito.lenient()
				.when(messageService.getMessage(Mockito.anyString(), Mockito.nullable(Locale.class), Mockito.any()))
				.thenAnswer(invocation -> (String) invocation.getArguments()[0]);

		Mockito.lenient().when(messageService.getMessage(Mockito.anyString(), Mockito.nullable(Locale.class)))
				.thenAnswer(invocation -> (String) invocation.getArguments()[0]);

		// method for retrieving enum message
		Mockito.lenient()
				.when(messageService.getEnumMessage(Mockito.any(), Mockito.any(Enum.class),
						Mockito.nullable(Locale.class)))
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
		Mockito.when(dao.save(Mockito.any(clazz))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	/**
	 * Mock the saving of a list on a DAO
	 * 
	 * @param dao the DAO
	 */
	@SuppressWarnings("unchecked")
	public static <ID, X extends AbstractEntity<ID>> void mockSaveList(BaseDao<ID, X> dao) {
		Mockito.when(dao.save(Mockito.any(List.class))).thenAnswer(invocation -> (List<X>) invocation.getArgument(0));
	}

	/**
	 * Mocks a service save operation, making sure that the argument that is passed
	 * to the method is returned from the method as well
	 * 
	 * @param service the service
	 */
	public static <ID, U extends AbstractEntity<ID>> void mockServiceSave(BaseService<ID, U> service, Class<U> clazz) {
		Mockito.when(service.save(Mockito.any(clazz))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	/**
	 * Mock the saving of a list on a DAO
	 * 
	 * @param dao the DAO
	 */
	@SuppressWarnings("unchecked")
	public static <ID, X extends AbstractEntity<ID>> void mockServiceSaveList(BaseService<ID, X> service) {
		Mockito.when(service.save(Mockito.any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	/**
	 * Registers all fields that are annotated with "@Mock" as beans in the Spring
	 * context
	 * 
	 * @param factory
	 * @param subject
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
