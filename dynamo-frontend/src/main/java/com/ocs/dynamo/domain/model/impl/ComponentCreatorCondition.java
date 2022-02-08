///*
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// */
//package com.ocs.dynamo.domain.model.impl;
//
//import java.util.List;
//import java.util.Map;
//
//import org.springframework.context.annotation.Condition;
//import org.springframework.context.annotation.ConditionContext;
//import org.springframework.core.type.AnnotatedTypeMetadata;
//import org.springframework.util.MultiValueMap;
//
//import com.ocs.dynamo.domain.model.AttributeType;
//import com.ocs.dynamo.utils.ClassUtils;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//public class ComponentCreatorCondition implements Condition {
//
//	@Override
//	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
//		MultiValueMap<String, Object> allAnnotationAttributes = metadata
//				.getAllAnnotationAttributes("com.ocs.dynamo.domain.model.impl.CreatesComponentType");
//		if (allAnnotationAttributes == null || allAnnotationAttributes.isEmpty()) {
//			return true;
//		}
//
//		List<Object> type = allAnnotationAttributes.get("attributeType");
//		AttributeType toMatch = (AttributeType) type.get(0);
//
//		Map<String, ComponentCreator> beansOfType = context.getBeanFactory().getBeansOfType(ComponentCreator.class);
//		CreatesComponentType anotation = beansOfType.values().stream()
//				.map(bean -> ClassUtils.getAnnotationOnClass(bean.getClass(), CreatesComponentType.class))
//				.filter(annot -> annot != null).filter(annot -> annot.attributeType().equals(toMatch)).findFirst()
//				.orElse(null);
//
//		if (anotation == null) {
//			return true;
//		}
//
//		log.info("Using overridden custom creator for attribute type {}", toMatch);
//		return false;
//
//	}
//
//}
