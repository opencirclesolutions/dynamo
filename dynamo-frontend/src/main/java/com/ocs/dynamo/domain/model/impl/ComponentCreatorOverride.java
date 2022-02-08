//package com.ocs.dynamo.domain.model.impl;
//
//import java.io.Serializable;
//import java.math.BigDecimal;
//
//import com.ocs.dynamo.domain.AbstractEntity;
//import com.ocs.dynamo.domain.model.AttributeModel;
//import com.ocs.dynamo.domain.model.AttributeType;
//import com.ocs.dynamo.domain.model.EntityModel;
//import com.ocs.dynamo.domain.model.FieldFactoryContext;
//import com.ocs.dynamo.exception.OCSRuntimeException;
//import com.ocs.dynamo.service.BaseService;
//import com.ocs.dynamo.service.ServiceLocator;
//import com.ocs.dynamo.service.ServiceLocatorFactory;
//import com.ocs.dynamo.ui.component.ElementCollectionGrid;
//import com.ocs.dynamo.ui.component.SimpleTokenFieldSelect;
//import com.ocs.dynamo.ui.composite.layout.FormOptions;
//import com.ocs.dynamo.utils.NumberUtils;
//import com.vaadin.flow.component.Component;
//import com.vaadin.flow.data.provider.DataProvider;
//import com.vaadin.flow.function.SerializablePredicate;
//
//@org.springframework.stereotype.Component
//@CreatesComponentType(attributeType = AttributeType.ELEMENT_COLLECTION)
//public class ComponentCreatorOverride implements ComponentCreator {
//
//	private ServiceLocator serviceLocator = ServiceLocatorFactory.getServiceLocator();
//
//	@Override
//	public boolean supports(AttributeType attributeType) {
//		return AttributeType.ELEMENT_COLLECTION.equals(attributeType);
//	}
//
//	@Override
//	public Component createComponent(AttributeModel am, FieldFactoryContext context, EntityModel<?> fieldEntityModel,
//			SerializablePredicate<?> fieldFilter, DataProvider<?, SerializablePredicate<?>> sharedProvider) {
//		Component field = null;
//		if (!context.isSearch()) {
//			// use a "collection grid" for an element collection
//			FormOptions fo = new FormOptions().setShowRemoveButton(true);
//
//			boolean allowed = String.class.equals(am.getMemberType()) || NumberUtils.isLong(am.getMemberType())
//					|| NumberUtils.isInteger(am.getMemberType()) || BigDecimal.class.equals(am.getMemberType());
//			if (allowed) {
//				field = new ElementCollectionGrid<>(am, fo);
//			} else {
//				// other types not supported for now
//				throw new OCSRuntimeException(
//						"Element collections of type " + am.getMemberType() + " are currently not supported");
//			}
//		} else {
//			// token search field
//			field = constructSimpleTokenField(fieldEntityModel != null ? fieldEntityModel : am.getEntityModel(), am,
//					am.getPath().substring(am.getPath().lastIndexOf('.') + 1), true, null);
//		}
//		return field;
//	}
//
//	/**
//	 * Constructs a token field for looking up simple values (Strings, ints) from a
//	 * property or element collection
//	 * 
//	 * @param entityModel       the entity model
//	 * @param am                the attribute model
//	 * @param distinctField     the database field from which to collect the
//	 *                          distinct values
//	 * @param elementCollection whether the lookup is from an element collection
//	 * @param fieldFilter       field filter used to limit the matching entities
//	 * @return
//	 */
//	@SuppressWarnings("unchecked")
//	private <ID extends Serializable, S extends AbstractEntity<ID>, O extends Comparable<O>> SimpleTokenFieldSelect<ID, S, O> constructSimpleTokenField(
//			EntityModel<?> entityModel, AttributeModel am, String distinctField, boolean elementCollection,
//			SerializablePredicate<?> fieldFilter) {
//		BaseService<ID, S> service = (BaseService<ID, S>) serviceLocator
//				.getServiceForEntity(entityModel.getEntityClass());
//		return new SimpleTokenFieldSelect<>(service, (EntityModel<S>) entityModel, am,
//				(SerializablePredicate<S>) fieldFilter, distinctField, (Class<O>) am.getNormalizedType(),
//				elementCollection);
//	}
//
//}
