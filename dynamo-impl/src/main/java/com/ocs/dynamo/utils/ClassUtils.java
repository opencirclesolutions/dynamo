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
package com.ocs.dynamo.utils;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.log4j.Logger;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

import com.ocs.dynamo.exception.OCSRuntimeException;

/**
 * @author patrick.deenen Utility class for dealing with classes and annotations
 */
public final class ClassUtils {

	private static final String GET = "get";

	private static final String IS = "is";

	private static final Logger LOG = Logger.getLogger(ClassUtils.class);

	private static final String SET = "set";

	private ClassUtils() {
	}

	/**
	 * Checks if the specified property can be set for the specified object.
	 * This
	 * method supports nested properties
	 *
	 * @param obj
	 *            the object
	 * @param fieldName
	 *            the name of the field
	 * @return
	 */
	public static boolean canSetProperty(Object obj, String fieldName) {
		try {
			int p = fieldName.indexOf('.');
			if (p >= 0) {
				String firstProperty = fieldName.substring(0, p);
				Object first = MethodUtils.invokeMethod(obj, GET + StringUtils.capitalize(firstProperty),
						new Object[] {});
				if (first != null) {
					return canSetProperty(first, fieldName.substring(p + 1));
				}
				return false;
			} else {
				return hasMethod(obj, SET + StringUtils.capitalize(fieldName));
			}
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new OCSRuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * Clears a field
	 *
	 * @param obj
	 *            the object on which to clear the field
	 * @param fieldName
	 *            the name of the field
	 * @param argType
	 */
	public static void clearFieldValue(Object obj, String fieldName, Class<?> argType) {
		try {
			int p = fieldName.indexOf('.');
			if (p >= 0) {
				String firstProperty = fieldName.substring(0, p);
				Object first = MethodUtils.invokeMethod(obj, GET + StringUtils.capitalize(firstProperty),
						new Object[] {});
				if (first != null) {
					clearFieldValue(first, fieldName.substring(p + 1), argType);
				}
			} else {
				Method m = obj.getClass().getMethod(SET + StringUtils.capitalize(fieldName), argType);
				m.invoke(obj, new Object[] { null });
			}
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new OCSRuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * forClass which doesn't throw an exception and can handle empty class
	 * names
	 *
	 * @param clazz
	 *            The fully qualified class name
	 * @return the specified class and null when class is not found
	 */
	public static Class<?> forClass(final String clazz) {
		Class<?> result = null;
		try {
			if (StringUtils.isNotEmpty(clazz)) {
				result = Class.forName(clazz);
			}
		} catch (final ClassNotFoundException e) {
			LOG.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * Tries to retrieve an annotation, by first looking at the field name, and
	 * then
	 * at the getter method
	 *
	 * @param clazz
	 *            the class
	 * @param fieldName
	 *            the name of the field
	 * @param annotationClass
	 *            the annotation class to look for
	 * @return
	 */
	public static <T extends Annotation> T getAnnotation(Class<?> clazz, String fieldName, Class<T> annotationClass) {
		T t = getAnnotationOnMethod(clazz, fieldName, annotationClass);
		if (t == null) {
			t = getAnnotationOnField(clazz, fieldName, annotationClass);
		}
		return t;
	}

	/**
	 * Get the value of a field in an annotation
	 *
	 * @param field
	 *            The field with annotations
	 * @param annotionType
	 *            The name of the annotation type to find
	 * @param attributeName
	 *            The name of the attribute on the annotation type to find the
	 *            value
	 * @return the value of the field of the annotation or null when not found
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Annotation, R> R getAnnotationAttributeValue(Field field, Class<T> annotationClass,
			String attributeName) {
		R result = null;
		Annotation annotation = getAnnotationOnField(field, annotationClass);
		if (annotation != null) {
			result = (R) AnnotationUtils.getValue(annotation, attributeName);
		}
		return result;
	}

	/**
	 * Return an annotation on a certain field
	 *
	 * @param clazz
	 *            the class
	 * @param fieldName
	 *            the field name
	 * @param annotationClass
	 *            the class of the annotation
	 * @return
	 */
	public static <T extends Annotation> T getAnnotationOnField(Class<?> clazz, String fieldName,
			Class<T> annotationClass) {
		Field field = getField(clazz, fieldName);
		if (field != null) {
			return getAnnotationOnField(field, annotationClass);
		}
		return null;
	}

	/**
	 * Returns an annotation on a certain field
	 *
	 * @param field
	 *            the field
	 * @param annotationClass
	 *            the annotation class
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Annotation> T getAnnotationOnField(Field field, Class<T> annotationClass) {
		T result = null;
		if (field != null) {
			for (Annotation a : field.getDeclaredAnnotations()) {
				if (a.annotationType().equals(annotationClass)) {
					result = (T) a;
				}
			}
		}
		return result;
	}

	/**
	 * Looks for an annotation on a getter method
	 *
	 * @param clazz
	 *            the class
	 * @param fieldName
	 *            the name of the field (will be prepended with "get")
	 * @param annotationClass
	 *            the class of the annotation to look for
	 * @return
	 */
	public static <T extends Annotation> T getAnnotationOnMethod(Class<?> clazz, String fieldName,
			Class<T> annotationClass) {
		Method method = getGetterMethod(clazz, fieldName);
		if (method != null) {
			return getAnnotationOnMethod(method, annotationClass);
		}
		return null;
	}

	/**
	 * Looks for an annotation on a (getter) method
	 *
	 * @param method
	 *            the method
	 * @param annotationClass
	 *            the class of the annotation
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Annotation> T getAnnotationOnMethod(Method method, Class<T> annotationClass) {
		T result = null;
		if (method != null) {
			for (Annotation a : method.getDeclaredAnnotations()) {
				if (a.annotationType().equals(annotationClass)) {
					result = (T) a;
				}
			}
		}
		return result;
	}

	/**
	 * Retrieves the contents of a field as a byte array
	 *
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	public static byte[] getBytes(Object obj, String fieldName) {
		return (byte[]) getFieldValue(obj, fieldName);
	}

	/**
	 * Find constructor based on the types of the given arguments used to
	 * instantiate the class with the found constructor
	 *
	 * @param clazz
	 * @param args
	 * @return
	 */
	public static <T> Constructor<T> getConstructor(Class<T> clazz, Object... args) {
		Assert.notNull(clazz, "[Assertion failed] - clazz argument is required; it must not be null");
		Assert.noNullElements(args, "[Assertion failed] - the args must not contain any null elements");
		Constructor<T> constructor = null;
		List<Class<?>> types = new ArrayList<>();
		for (Object arg : args) {
			types.add(arg.getClass());
		}
		try {
			if (types.isEmpty()) {
				constructor = clazz.getConstructor();
			} else {
				constructor = clazz.getConstructor(types.toArray(new Class<?>[0]));
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return constructor;
	}

	/**
	 * Returns a field with a certain name from a class
	 *
	 * @param clazz
	 *            the class
	 * @param fieldName
	 *            the name of the filed
	 * @return
	 */
	public static Field getField(Class<?> clazz, String fieldName) {
		Field field = null;
		if (clazz != null) {
			try {
				field = clazz.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				LOG.debug(e.getMessage(), e);
				if (clazz.getSuperclass() != null) {
					return getField(clazz.getSuperclass(), fieldName);
				}
			}
		}
		return field;
	}

	/**
	 * Retrieves a field value
	 *
	 * @param obj
	 *            the object from which to retrieve the field value
	 * @param fieldName
	 *            the name of the field
	 * @return
	 */
	public static Object getFieldValue(Object obj, String fieldName) {
		try {
			int p = fieldName.indexOf('.');
			if (p >= 0) {
				String firstProperty = fieldName.substring(0, p);
				Object first = MethodUtils.invokeMethod(obj, GET + StringUtils.capitalize(firstProperty),
						new Object[] {});
				return first == null ? null : getFieldValue(first, fieldName.substring(p + 1));
			} else {
				if (hasMethod(obj, GET + StringUtils.capitalize(fieldName))) {
					// first check for a getter
					return MethodUtils.invokeMethod(obj, GET + StringUtils.capitalize(fieldName), new Object[] {});
				} else {
					// next, check for an "is" method in case of a boolean
					return MethodUtils.invokeMethod(obj, IS + StringUtils.capitalize(fieldName), new Object[] {});
				}
			}
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new OCSRuntimeException("Error getting of " + obj + ":" + e.getMessage(), e);
		}
	}

	public static String getFieldValueAsString(Object obj, String fieldName) {
		Object temp = getFieldValue(obj, fieldName);
		return temp == null ? null : temp.toString();
	}

	/**
	 * Retrieves the getter method for a certain property
	 *
	 * @param clazz
	 *            the class
	 * @param fieldName
	 *            the name of the property
	 * @return
	 */
	public static Method getGetterMethod(Class<?> clazz, String fieldName) {
		Method method = null;
		if (clazz != null) {
			try {
				// first, try to find a "get" method
				method = clazz.getDeclaredMethod(GET + StringUtils.capitalize(fieldName));
			} catch (NoSuchMethodException | SecurityException ex) {
				try {
					// next, try to find an "is" method
					method = clazz.getDeclaredMethod(IS + StringUtils.capitalize(fieldName));
				} catch (NoSuchMethodException | SecurityException ex2) {
					// if that fails, try the superclass
					if (clazz.getSuperclass() != null) {
						return getGetterMethod(clazz.getSuperclass(), fieldName);
					}
				}
			}
		}
		return method;
	}

	/**
	 * Returns the maximum allowed length of a field
	 *
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	public static int getMaxLength(Class<?> clazz, String fieldName) {
		Size size = getAnnotation(clazz, fieldName, Size.class);
		if (size != null) {
			return size.max();
		}
		return -1;
	}

	/**
	 * Returns the property descriptor for a nested property
	 *
	 * @param clazz
	 *            the class
	 * @param property
	 *            the name of the property
	 * @return
	 */
	public static PropertyDescriptor getPropertyDescriptorForNestedProperty(Class<?> clazz, String property) {
		PropertyDescriptor pd = null;
		if (clazz != null && !StringUtils.isEmpty(property)) {
			String[] props = property.split("\\.", 2);
			pd = org.springframework.beans.BeanUtils.getPropertyDescriptor(clazz, props[0]);
			if (props.length > 1) {
				pd = getPropertyDescriptorForNestedProperty(pd.getPropertyType(), props[1]);
			}
		}
		return pd;
	}

	/**
	 * Return a Class<?> representing the generic parameter for the given indexes.
	 * Indexes are zero based; for example given the type Map<Integer,
	 * List<String>>, getGeneric(0) will access the Integer. Nested generics can be
	 * accessed by specifying multiple indexes; for example getGeneric(1, 0) will
	 * access the String from the nested List. For convenience, if no indexes are
	 * specified the first generic is returned.
	 *
	 * @param type
	 * @param fieldName
	 * @param indexes
	 * @return
	 */
	public static <T> Class<?> getResolvedType(Class<T> type, String fieldName, int... indexes) {
		Field field = getField(type, fieldName);
		if (field != null) {
			ResolvableType rt = ResolvableType.forField(field);
			if (rt != null) {
				if (indexes != null && indexes.length > 0) {
					rt = rt.getGeneric(indexes);
				}
				if (rt != null) {
					return rt.resolve();
				}

			}
		}
		return null;
	}

	/**
	 * Return a Class<?> representing the generic parameter for the given indexes.
	 * Indexes are zero based; for example given the type Map<Integer,
	 * List<String>>, getGeneric(0) will access the Integer. Nested generics can be
	 * accessed by specifying multiple indexes; for example getGeneric(1, 0) will
	 * access the String from the nested List. For convenience, if no indexes are
	 * specified the first generic is returned.
	 *
	 * @param method
	 *            or method parameter or type
	 * @param indexes
	 * @return
	 */
	public static <T> Class<?> getResolvedType(Object object, int... indexes) {
		if (object != null) {
			ResolvableType rt = null;
			if (object instanceof Method) {
				rt = ResolvableType.forMethodReturnType((Method) object);
			} else if (object instanceof MethodParameter) {
				rt = ResolvableType.forMethodParameter((MethodParameter) object);
			} else if (object instanceof Type) {
				rt = ResolvableType.forType((Type) object);
			} else if (object instanceof Class) {
				rt = ResolvableType.forRawClass((Class<?>) object);
			}
			if (rt != null) {
				if (indexes != null && indexes.length > 0) {
					rt = rt.getGeneric(indexes);
				}
				if (rt != null) {
					return rt.resolve();
				}

			}
		}
		return null;
	}

	/**
	 * Check if the object has a (public) method that has the specified name
	 *
	 * @param obj
	 * @param methodName
	 * @return
	 */
	public static boolean hasMethod(Object obj, String methodName) {
		Method[] methods = obj.getClass().getMethods();
		for (Method method : methods) {
			if (method.getName().equals(methodName) && (Modifier.isPublic(method.getModifiers()))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Instantiate a class with the given arguments; assumed is that all
	 * arguments
	 * are not null so the types can be determined and a matching
	 * constructor can be
	 * found. When no constructor is found null is returned.
	 *
	 * @param clazz
	 * @param args
	 * @return
	 */
	public static <T> T instantiateClass(Class<T> clazz, Object... args) {
		Constructor<T> constructor = getConstructor(clazz, args);
		if (constructor != null) {
			return org.springframework.beans.BeanUtils.instantiateClass(constructor, args);
		}
		return null;
	}

	public static void setBytes(byte[] bytes, Object obj, String fieldName) {
		if (bytes != null) {
			setFieldValue(obj, fieldName, bytes);
		} else {
			clearFieldValue(obj, fieldName, byte[].class);
		}
	}

	/**
	 * @param obj
	 * @param fieldName
	 * @param value
	 */
	public static void setFieldValue(Object obj, String fieldName, Object value) {
		try {
			int p = fieldName.indexOf('.');
			if (p >= 0) {
				String firstProperty = fieldName.substring(0, p);
				Object first = MethodUtils.invokeMethod(obj, GET + StringUtils.capitalize(firstProperty),
						new Object[] {});
				if (first != null) {
					setFieldValue(first, fieldName.substring(p + 1), value);
				}
			} else {
				MethodUtils.invokeMethod(obj, SET + StringUtils.capitalize(fieldName), new Object[] { value });
			}
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			LOG.error(e.getMessage(), e);
			throw new OCSRuntimeException(e.getMessage(), e);
		}
	}

	public static <T, S extends T> void copyFields(T from, S to) {
		try {
			Class<?> aClass = from.getClass();
			final Field[] declaredFields = aClass.getDeclaredFields();
			copyFields(from, to, declaredFields);

			while ((aClass = aClass.getSuperclass()) != null) {
				copyFields(from, to, aClass.getFields());
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new OCSRuntimeException(e.getMessage(), e);
		}
	}

	private static <T, S extends T> void copyFields(T from, S to, Field[] fields) throws IllegalAccessException {
		for (Field field : fields) {
			final int fieldModifiers = field.getModifiers();
			if (!Modifier.isFinal(fieldModifiers) && !Modifier.isStatic(fieldModifiers)) {
				final Object value = FieldUtils.readField(field, from, true);
				FieldUtils.writeField(field, to, value, true);
			}
		}
	}
}
