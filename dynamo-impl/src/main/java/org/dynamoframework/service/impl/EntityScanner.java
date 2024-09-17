package org.dynamoframework.service.impl;

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

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dynamoframework.configuration.DynamoConfigurationProperties;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.exception.OCSException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class EntityScanner {

    private static Set<String> basePackages = new HashSet<>();

    private static final Map<String, Class<?>> entityClasses = new ConcurrentHashMap<>();

    @Autowired
    private DynamoConfigurationProperties configurationProperties;

    @PostConstruct
    public void postConstruct() {
        if (configurationProperties.getDefaults().isEntityClassPathScan()) {

            ClassPathScanningCandidateComponentProvider entityScanProvider = new ClassPathScanningCandidateComponentProvider(false);
            entityScanProvider.addIncludeFilter(new AnnotationTypeFilter(EntityScan.class));
            basePackages = new HashSet<>();
            Set<BeanDefinition> beanDefs = entityScanProvider.findCandidateComponents("");
            for (BeanDefinition bd : beanDefs) {
                if (bd instanceof AnnotatedBeanDefinition) {
                    Map<String, Object> annotAttributeMap = ((AnnotatedBeanDefinition) bd).getMetadata().getAnnotationAttributes(EntityScan.class.getCanonicalName());
                    Collections.addAll(basePackages, ((String[]) annotAttributeMap.get("basePackages")));
                }
            }
        }
    }


    /**
     * Translates the name of an entity to the fully qualified class name. The <code>@EntityScan</code> base packages are searched.
     *
     * @param entityName the name of the entity; the class must be a concrete subclass of <code>AbstractEntity</code>
     * @return the class
     */
    public Class<?> findClass(String entityName) {

        if (configurationProperties.getDefaults().isEntityClassPathScan()) {

            if (entityClasses.containsKey(entityName)) {
                log.debug("Getting class for {} from cache", entityName);
                return entityClasses.get(entityName);
            }

            synchronized (EntityScanner.class) {
                ClassPathScanningCandidateComponentProvider entityProvider = new ClassPathScanningCandidateComponentProvider(false);
                entityProvider.addIncludeFilter(new AssignableTypeFilter(AbstractEntity.class));
                for (String packageName : basePackages) {
                    Set<BeanDefinition> beanDefs = entityProvider.findCandidateComponents(packageName);
                    for (BeanDefinition beanDef : beanDefs) {
                        if (entityName.equals(Objects.requireNonNull(beanDef.getBeanClassName()).substring(beanDef.getBeanClassName().lastIndexOf(".") + 1)) && !beanDef.isAbstract()) {
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
        } else {
            return findClassByName(entityName);
        }
    }

    @SneakyThrows
    private Class<?> findClassByName(String entityName) {
        String entityPackages = configurationProperties.getDefaults().getEntityPackages();
        if (entityPackages.isEmpty()) {
            throw new OCSException("No entity packages configured. Please configure the 'dynamoframework.defaults.entityPackages' property");
        }

        Class<?> result = null;
        String[] packageNames = entityPackages.split(",");

        for (String packageName : packageNames) {
            try {
                result = Class.forName("%s.%s".formatted(packageName, entityName));
            } catch (ClassNotFoundException | NoClassDefFoundError ex) {
                // ignore
            }
        }
        return result;
    }
}
