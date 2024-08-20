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
package org.dynamoframework.utils;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.Size;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.dynamoframework.configuration.DynamoProperties;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.exception.OCSRuntimeException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * @author Bas Rutten
 */
@Slf4j
@UtilityClass
public final class ClassUtils {

    private static final String GET = "get";

    private static final String IS = "is";

    private static final String SET = "set";

    /**
     * Checks if the specified property can be set for the specified object. This
     * method supports nested properties
     *
     * @param obj       the object
     * @param fieldName the name of the field
     * @return true if this is the case, false otherwise
     */
    public static boolean canSetProperty(Object obj, String fieldName) {
        try {
            int p = fieldName.indexOf('.');
            if (p >= 0) {
                String firstProperty = fieldName.substring(0, p);
                Object first = MethodUtils.invokeMethod(obj,
                        GET + StringUtils.capitalize(firstProperty));
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
     * Clears an attribute value
     *
     * @param obj           the object on which to clear the field
     * @param attributeName the name of the attribute
     * @param argType       the argument type
     */
    public static void clearAttributeValue(Object obj, String attributeName, Class<?> argType) {
        try {
            int p = attributeName.indexOf('.');
            if (p >= 0) {
                String firstProperty = attributeName.substring(0, p);
                Object first = MethodUtils.invokeMethod(obj, GET + StringUtils.capitalize(firstProperty));
                if (first != null) {
                    clearAttributeValue(first, attributeName.substring(p + 1), argType);
                }
            } else {
                Method method = obj.getClass().getMethod(SET + StringUtils.capitalize(attributeName), argType);
                method.invoke(obj, (Object) null);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new OCSRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Tries to retrieve an annotation, by first looking at the field name, and then
     * at the getter method
     *
     * @param clazz           the class
     * @param attributeName   the name of the field
     * @param annotationClass the annotation class to look for
     * @return the annotation
     */
    public static <T extends Annotation> T getAnnotation(Class<?> clazz, String attributeName, Class<T> annotationClass) {
        T entity = getAnnotationOnMethod(clazz, attributeName, annotationClass);
        if (entity == null) {
            entity = getAnnotationOnField(clazz, attributeName, annotationClass);
        }
        return entity;
    }

    /**
     * Get the value of a field in an annotation
     *
     * @param field           The field with annotations
     * @param annotationClass The class of the annotation
     * @param attributeName   The name of the attribute on the annotation type to find
     *                        the value
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
     * @param clazz           the class
     * @param fieldName       the field name
     * @param annotationClass the class of the annotation
     * @return the annotation
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
     * @param field           the field
     * @param annotationClass the annotation class
     * @return the annotation
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
     * @param clazz           the class
     * @param fieldName       the name of the field (will be prepended with "get")
     * @param annotationClass the class of the annotation to look for
     * @return the annotation
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
     * @param method          the method
     * @param annotationClass the class of the annotation
     * @return the annotation
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
     * @param obj       the object
     * @param fieldName the name of the field
     * @return the content, as a byte array
     */
    public static byte[] getBytes(Object obj, String fieldName) {
        return (byte[]) getFieldValue(obj, fieldName);
    }

    /**
     * Find constructor based on the types of the given arguments used to
     * instantiate the class with the found constructor
     *
     * @param clazz the class for which to find the constructor
     * @param args  the arguments
     * @return the constructor
     */
    public static <T> Constructor<T> getConstructor(Class<T> clazz, Object... args) {
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
            log.error(e.getMessage(), e);
        }
        return constructor;
    }

    /**
     * Returns a field with a certain name from a class
     *
     * @param clazz     the class
     * @param fieldName the name of the filed
     * @return the field
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        Field field = null;
        if (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
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
     * @param obj       the object from which to retrieve the field value
     * @param fieldName the name of the field
     * @return the field value
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        try {
            int p = fieldName.indexOf('.');
            if (p >= 0) {
                String firstProperty = fieldName.substring(0, p);
                Object first = MethodUtils.invokeMethod(obj, GET + StringUtils.capitalize(firstProperty));
                return first == null ? null : getFieldValue(first, fieldName.substring(p + 1));
            } else {
                if (hasMethod(obj, GET + StringUtils.capitalize(fieldName))) {
                    // first check for a getter
                    return MethodUtils.invokeMethod(obj, GET + StringUtils.capitalize(fieldName));
                } else {
                    // next, check for an "is" method in case of a boolean
                    return MethodUtils.invokeMethod(obj, IS + StringUtils.capitalize(fieldName));
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new OCSRuntimeException("Error getting of " + obj + ":" + e.getMessage(), e);
        }
    }

    /**
     * Returns a field value as a String
     *
     * @param obj       the object
     * @param fieldName the name of the field
     * @return the field value, as a string
     */
    public static String getFieldValueAsString(Object obj, String fieldName) {
        return getFieldValueAsString(obj, fieldName, null);
    }

    /**
     * Returns a field value as a String
     *
     * @param obj          the object
     * @param fieldName    the name of the field
     * @param defaultValue the default value that is used in case of a null value
     * @return the field value as a String
     */
    public static String getFieldValueAsString(Object obj, String fieldName, String defaultValue) {
        Object temp = getFieldValue(obj, fieldName);
        return temp == null ? defaultValue : temp.toString();
    }

    /**
     * Retrieves the getter method for a certain property
     *
     * @param clazz     the class
     * @param fieldName the name of the property
     * @return the method
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
     * @param clazz     the clazz on which the field is located
     * @param fieldName the name of the field
     * @return the maximum length
     */
    public static int getMaxLength(Class<?> clazz, String fieldName) {
        Size size = getAnnotation(clazz, fieldName, Size.class);
        if (size != null) {
            return size.max();
        }
        return -1;
    }

    /**
     * Return a Class<?> representing the generic parameter for the given indexes.
     * Indexes are zero based; for example given the type Map<Integer,
     * List<String>>, getGeneric(0) will access the Integer. Nested generics can be
     * accessed by specifying multiple indexes; for example getGeneric(1, 0) will
     * access the String from the nested List. For convenience, if no indexes are
     * specified the first generic is returned.
     *
     * @param type      the class
     * @param fieldName the name of the field
     * @param indexes   the set of indexes
     * @return the resolved type
     */
    public static <T> Class<?> getResolvedType(Class<T> type, String fieldName, int... indexes) {
        Field field = getField(type, fieldName);
        if (field != null) {
            ResolvableType rt = ResolvableType.forField(field);
            if (indexes != null && indexes.length > 0) {
                rt = rt.getGeneric(indexes);
            }
            return rt.resolve();
        }
        return null;
    }

    /**
     * Check if the object has a (public) method that has the specified name
     *
     * @param obj        the object to check
     * @param methodName the name of the method
     * @return true if this is the case, false otherwise
     */
    public static boolean hasMethod(Object obj, String methodName) {
        Method[] methods = obj.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName) && Modifier.isPublic(method.getModifiers())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Instantiate a class with the given arguments; assumed is that all arguments
     * are not null so the types can be determined and a matching constructor can be
     * found. When no constructor is found null is returned.
     *
     * @param clazz the class
     * @param args  the constructor arguments to pass
     * @return the instantiated class
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
            clearAttributeValue(obj, fieldName, byte[].class);
        }
    }

    /**
     * Sets the value of the provided field on the provided object
     *
     * @param obj       the object
     * @param fieldName the name of the field
     * @param value     the value to set
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) {
        try {
            int p = fieldName.indexOf('.');
            if (p >= 0) {
                String firstProperty = fieldName.substring(0, p);
                Object first = MethodUtils.invokeMethod(obj, GET + StringUtils.capitalize(firstProperty));
                if (first != null) {
                    if (first instanceof Collection<?> col) {
                        col.forEach(c -> setFieldValue(c, fieldName.substring(p + 1), value));
                    } else {
                        setFieldValue(first, fieldName.substring(p + 1), value);
                    }
                }
            } else {
                MethodUtils.invokeMethod(obj, SET + StringUtils.capitalize(fieldName), value);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error(e.getMessage(), e);
            throw new OCSRuntimeException(e.getMessage(), e);
        }
    }

    private static Set<String> basePackages = null;
    private static Map<String, Class> entityClasses = new HashMap<>();

    /**
     * Translates the name of an entity to the fully qualified class name. The <code>@EntityScan</code> base packages are searched.
     *
     * @param entityName the name of the entity; the class must be annotated with <code>@Entity</code>
     * @return the class
     */
    public static Class<?> findClass(String entityName) {
        if (basePackages == null) {
            synchronized (ClassUtils.class) {
                ClassPathScanningCandidateComponentProvider entityScanProvider = new ClassPathScanningCandidateComponentProvider(false);
                entityScanProvider.addIncludeFilter(new AnnotationTypeFilter(EntityScan.class));
                basePackages = new HashSet<>();
                Set<BeanDefinition> beanDefs = entityScanProvider.findCandidateComponents("");
                for (BeanDefinition beanDef : beanDefs) {
                    System.out.println(beanDef.getBeanClassName());
                }
                for (BeanDefinition bd : beanDefs) {
                    if (bd instanceof AnnotatedBeanDefinition) {
                        Map<String, Object> annotAttributeMap = ((AnnotatedBeanDefinition) bd).getMetadata().getAnnotationAttributes(EntityScan.class.getCanonicalName());
                        Arrays.stream(((String[]) annotAttributeMap.get("basePackages"))).forEach(s -> log.debug("Found basePackage {}", s));
                        Collections.addAll(basePackages, ((String[]) annotAttributeMap.get("basePackages")));
                    }
                }
            }
        }
        if (entityClasses.containsKey(entityName)) {
            log.debug("Getting class for {} from cache", entityName);
            return entityClasses.get(entityName);
        }
        synchronized (ClassUtils.class) {
            ClassPathScanningCandidateComponentProvider entityProvider = new ClassPathScanningCandidateComponentProvider(false);
            entityProvider.addIncludeFilter(new AssignableTypeFilter(AbstractEntity.class));
            for (String packageName : basePackages) {
                Set<BeanDefinition> beanDefs = entityProvider.findCandidateComponents(packageName);
                for (BeanDefinition beanDef : beanDefs) {
                    if (entityName.equals(beanDef.getBeanClassName().substring(beanDef.getBeanClassName().lastIndexOf(".") + 1)) && !beanDef.isAbstract()) {
                        try {
                            Class<?> clazz = Class.forName(beanDef.getBeanClassName());
                            entityClasses.put(entityName, clazz);
                            return clazz;
                        } catch (ClassNotFoundException e) {
                            log.warn("Class {} not found", beanDef.getBeanClassName());
                        }
                    }
                }
            }
        }
        return null;
    }

}
