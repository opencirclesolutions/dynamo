package com.ocs.dynamo.ui.container.hierarchical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.util.StringUtils;

import com.ocs.dynamo.constants.OCSConstants;
import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.ModelBasedFieldFactory;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.ServiceLocator;
import com.ocs.dynamo.ui.container.QueryType;
import com.ocs.dynamo.ui.container.ServiceContainer;
import com.vaadin.data.Container;
import com.vaadin.data.fieldgroup.FieldGroupFieldFactory;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.TableFieldFactory;

/**
 * Assumes that the following properties are set in message bundle:
 * <ul>
 * <li><rootentity model>.hierarchicalVisualPropertyIds the ordered common
 * properties which will be shared among all levels. When a level doesn't have
 * that property it will be left blank when displayed.
 * <li><(sub) entity model reference>.itemPropertyIdChild the property id that
 * refers to the (collection) of children. (optional)
 * <li><(sub) entity model reference>.itemPropertyIdParent the property id that
 * refers to the parent.
 * </ul>
 * The actual search will only search on the container in the lowest level by
 * default.
 * 
 * @author Patrick Deenen (patrick.deenen@opencirclesolutions.nl)
 */
@SuppressWarnings("serial")
public class ModelBasedHierarchicalContainer<T> extends HierarchicalContainer {

	public static final String VISUAL_PROPERTY_IDS_MSG_KEY = "hierarchicalVisualPropertyIds";

	public class ModelBasedHierarchicalDefinition extends HierarchicalDefinition {

		private EntityModel<?> entityModel;

		private ModelBasedFieldFactory<AbstractEntity<?>> fieldFactory;

		public ModelBasedHierarchicalDefinition(EntityModel<?> entityModel, Indexed container,
		        int level, Object itemPropertyIdParent, List<?> propertyIds) {
			super(container, level, entityModel.getIdAttributeModel().getName(),
			        itemPropertyIdParent, propertyIds);
			this.entityModel = entityModel;
		}

		public EntityModel<?> getEntityModel() {
			return entityModel;
		}

		public void setEntityModel(EntityModel<?> entityModel) {
			this.entityModel = entityModel;
		}

		@SuppressWarnings("unchecked")
		public TableFieldFactory getFieldFactory(MessageService messageService) {
			if (fieldFactory == null) {
				fieldFactory = (ModelBasedFieldFactory<AbstractEntity<?>>) ModelBasedFieldFactory
				        .getInstance(entityModel, messageService);
			}
			return fieldFactory;
		}
	}

	public class HierarchicalFieldFactory implements FieldGroupFieldFactory, TableFieldFactory {

		private ModelBasedHierarchicalContainer<T> container;

		private MessageService messageService;

		/**
		 * Create field factory delegator whichs uses the field factories in the
		 * definitions
		 * 
		 * @param container
		 */
		public HierarchicalFieldFactory(ModelBasedHierarchicalContainer<T> container,
		        MessageService messageService) {
			this.container = container;
			this.messageService = messageService;
		}

		@Override
		public Field<?> createField(Container container, Object itemId, Object propertyId,
		        Component uiContext) {
			ModelBasedHierarchicalDefinition def = null;
			if (itemId instanceof HierarchicalId) {
				def = this.container.getHierarchicalDefinitionByItemId(itemId);
			} else {
				def = this.container.getHierarchicalDefinition(0);
			}
			if (def != null) {
				Object p = this.container.unmapProperty(def, propertyId);
				if (p != null) {
					return def.getFieldFactory(messageService).createField(def.getContainer(),
					        this.container.unwrapItemId(itemId), p, uiContext);
				}
			}
			return null;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public <F extends Field> F createField(Class<?> dataType, Class<F> fieldType) {
			FieldGroupFieldFactory f = (FieldGroupFieldFactory) container
			        .getHierarchicalDefinition(0).getFieldFactory(messageService);
			return f.createField(dataType, fieldType);
		}

		protected ModelBasedHierarchicalContainer<T> getContainer() {
			return container;
		}
	}

	/**
	 * Default constructor
	 */
	public ModelBasedHierarchicalContainer() {
		// Do nothing
	}

	/**
	 * Construct a hierarchical container using given root entity model and
	 * services.
	 * 
	 * @param messageService
	 *            used for custom definitions, see class description.
	 * @param rootEntityModel
	 *            The top level entity model, the other sub models will be
	 *            defined dynamically.
	 * @param services
	 *            All services to be used for each level.
	 */
	public ModelBasedHierarchicalContainer(MessageService messageService,
	        EntityModel<T> rootEntityModel, List<BaseService<?, ?>> services,
	        HierarchicalFetchJoinInformation[] joins) {
		generateHierarchy(messageService, ServiceLocator.getEntityModelFactory(), rootEntityModel,
		        services, joins, QueryType.ID_BASED);
	}

	public ModelBasedHierarchicalContainer(MessageService messageService,
	        EntityModelFactory entityModelFactory, EntityModel<T> rootEntityModel,
	        List<BaseService<?, ?>> services, HierarchicalFetchJoinInformation[] joins,
	        QueryType queryType) {
		generateHierarchy(messageService, entityModelFactory, rootEntityModel, services, joins,
		        queryType);
	}

	/**
	 * Construct this hierarchical container using given root entity model and
	 * services.
	 * 
	 * @param messageService
	 *            used for custom definitions, see class description.
	 * @param rootEntityModel
	 *            The top level entity model, the other sub models will be
	 *            defined dynamically.
	 * @param services
	 *            All services to be used for each level.
	 * @param joins
	 *            Join information for each level.
	 */
	public HierarchicalContainer generateHierarchy(MessageService messageService,
	        EntityModelFactory entityModelFactory, EntityModel<T> rootEntityModel,
	        List<BaseService<?, ?>> services, HierarchicalFetchJoinInformation[] joins,
	        QueryType queryType) {
		if (rootEntityModel != null) {
			// generate definitions
			EntityModel<?> pm = null;
			EntityModel<?> cm = rootEntityModel;
			for (int level = 0; level < services.size(); level++) {
				// Create container for hierarchy level
				Indexed container = createLevelContainer(level, services.get(level), cm, joins,
				        queryType);

				// Initialize common properties
				List<String> propertyIds = new ArrayList<>();
				String msg = messageService.getEntityMessage(cm.getReference(),
				        VISUAL_PROPERTY_IDS_MSG_KEY);
				if (!StringUtils.isEmpty(msg)) {
					// Use properties from message bundle
					String[] tokens = msg.split(",");
					if (level == 0) {
						setContainerPropertyIds(Arrays.asList(tokens));
					}
					for (String prop : tokens) {
						if ("null".equalsIgnoreCase(prop) || "".equals(prop)
						        || !container.getContainerPropertyIds().contains(prop)) {
							propertyIds.add(null);
						} else {
							propertyIds.add(prop);
						}
					}
				} else {
					// Create the common properties from the root
					for (AttributeModel am : cm.getAttributeModels()) {
						if (am.isVisibleInTable()) {
							propertyIds.add(am.getName());
						}
					}
				}

				// Create definition
				if (level > 0) {
					// Find parent, look in message bundle and otherwise an
					// educated guess
					AttributeModel parent = findRelatedAttributeModel(messageService, cm,
					        pm.getEntityClass(), AttributeType.MASTER, "itemPropertyIdParent");
					// Must have a parent
					addDefinition(new ModelBasedHierarchicalDefinition(cm, container, level,
					        parent.getName(), propertyIds));
				} else {
					// Root will not have a parent
					addDefinition(new ModelBasedHierarchicalDefinition(cm, container, level, null,
					        propertyIds));
				}

				if ((level + 1) < services.size()) {
					// Next child/level, look in message bundle and otherwise an
					// educated guess
					pm = cm;
					Class<?> pec = services.get(level + 1).getEntityClass();
					AttributeModel child = findRelatedAttributeModel(messageService, pm, pec,
					        AttributeType.DETAIL, "itemPropertyIdChild");
					if (child != null) {
						cm = child.getNestedEntityModel();
					} else {
						cm = entityModelFactory
						        .getModel(pm.getReference() + "_" + pec.getSimpleName(), pec);
					}
				}
			}
		}
		return this;
	}

	/**
	 * Create the container for a specific level
	 * 
	 * @return the created container
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Indexed createLevelContainer(int level, BaseService<?, ?> service,
	        EntityModel<?> entityModel, HierarchicalFetchJoinInformation[] joins,
	        QueryType queryType) {
		List<HierarchicalFetchJoinInformation> ljoins = new ArrayList<>();
		if (joins != null) {
			for (HierarchicalFetchJoinInformation join : joins) {
				if (join.getLevel() == level) {
					ljoins.add(join);
				}
			}
		}
		ServiceContainer container = new ServiceContainer(service, entityModel, false,
		        OCSConstants.EXTENDED_PAGE_SIZE, queryType,
		        ljoins.toArray(new FetchJoinInformation[0]));
		// increase cache size to prevent endless re-queries
		container.getQueryView().setMaxCacheSize(OCSConstants.CACHE_SIZE);

		return container;
	}

	protected AttributeModel findRelatedAttributeModel(MessageService messageService,
	        EntityModel<?> entityModel, Class<?> entityClassOther, AttributeType attributeType,
	        String messageKey) {
		AttributeModel related = null;
		String msg = messageService.getEntityMessage(entityModel.getReference(), messageKey);
		if (!StringUtils.isEmpty(msg) && entityModel.getAttributeModel(msg) != null) {
			related = entityModel.getAttributeModel(msg);
		} else {
			List<AttributeModel> ms = entityModel.getAttributeModelsForType(attributeType,
			        entityClassOther);
			if (!ms.isEmpty()) {
				related = ms.get(0);
			}
		}
		return related;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ModelBasedHierarchicalDefinition getHierarchicalDefinition(int level) {
		return (ModelBasedHierarchicalDefinition) getHierarchy().get(level);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ModelBasedHierarchicalDefinition getHierarchicalDefinitionByItemId(Object itemId) {
		return (ModelBasedHierarchicalDefinition) super.getHierarchicalDefinitionByItemId(itemId);
	}

	@Override
	public void addDefinition(HierarchicalDefinition definition) {
		if (!(definition instanceof ModelBasedHierarchicalContainer.ModelBasedHierarchicalDefinition)) {
			throw new UnsupportedOperationException(
			        "The use of a ModelBasedHierarchicalDefinition is mandatory for a ModelBasedHierarchicalContainer.");
		}
		super.addDefinition(definition);
	}

	@Override
	public void addDefinition(Indexed container, int level, Object itemPropertyId,
	        Object itemPropertyIdParent, Object... propertyIds) {
		throw new UnsupportedOperationException(
		        "The use of a ModelBasedHierarchicalDefinition is mandatory for a ModelBasedHierarchicalContainer.");
	}

	public void addDefinition(EntityModel<?> entityModel, Indexed container, int level,
	        Object itemPropertyIdParent, Object... propertyIds) {
		super.addDefinition(new ModelBasedHierarchicalDefinition(entityModel, container, level,
		        itemPropertyIdParent, Arrays.asList(propertyIds)));

	}
}
