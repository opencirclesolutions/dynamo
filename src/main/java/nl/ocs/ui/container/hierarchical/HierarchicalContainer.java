package nl.ocs.ui.container.hierarchical;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import nl.ocs.domain.AbstractEntity;
import nl.ocs.utils.SubList;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Hierarchical;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Container.ItemSetChangeNotifier;
import com.vaadin.data.Container.Ordered;
import com.vaadin.data.Container.Sortable;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * An hierarchicalContainer which delegates to other container(s). Please note:
 * <ul>
 * <li>Assumes that the query results of a container on a specific level
 * contains the results for all rows on that level. The FK of to the parent will
 * be used to divide the rows into 'chunks' of children.
 * <li>Assumes that detail containers are ordered related to their parent.
 * <li>Assumes that item values for the PK and FK (to parent) are comparable.
 * <li>One needs to specify properties on each level to map from lower levels to
 * the root level. All types should be the same on each level. Properties on
 * lower levels will be mapped to upper levels by copying properties to a
 * property name of the mapping root property (when the name is different).
 * <li>Only supports PK and FK which consists out of just one property.
 * <li>Only support search on the lowest level by default.
 * </ul>
 * 
 * @author Patrick Deenen (patrick.deenen@opencirclesolutions.nl)
 *
 */
@SuppressWarnings("serial")
public class HierarchicalContainer implements Hierarchical, Ordered, ItemSetChangeNotifier,
		ItemSetChangeListener, Sortable {

	public class HierarchicalDefinition {
		private Indexed container;
		private int level;
		private List<?> propertyIds;
		private Object itemPropertyId;
		private Object itemPropertyIdParent;
		private Object itemPropertyIdHasChildren;

		public HierarchicalDefinition(Indexed container, int level, Object itemPropertyId,
				Object itemPropertyIdParent, List<?> propertyIds) {
			if (container == null) {
				throw new AssertionError("container is mandatory");
			}
			if (level < 0) {
				throw new AssertionError("level should be positive");
			}
			if (propertyIds == null) {
				throw new AssertionError("propertyIds is mandatory");
			}
			if (propertyIds.isEmpty()) {
				throw new AssertionError("propertyIds should contain at least 1 id");
			}
			if (itemPropertyId == null) {
				throw new AssertionError("itemPropertyId is mandatory");
			}
			if (level > 0 && itemPropertyIdParent == null) {
				throw new AssertionError("itemPropertyIdParent is mandatory for children");
			}

			this.container = container;
			this.level = level;
			this.propertyIds = propertyIds;
			this.itemPropertyId = itemPropertyId;
			this.itemPropertyIdParent = itemPropertyIdParent;
		}

		/**
		 * @return the container
		 */
		public Indexed getContainer() {
			return container;
		}

		/**
		 * @return the level
		 */
		public int getLevel() {
			return level;
		}

		/**
		 * @return the propertyIds
		 */
		public List<?> getPropertyIds() {
			return propertyIds;
		}

		/**
		 * @return the itemPropertyId
		 */
		public Object getItemPropertyId() {
			return itemPropertyId;
		}

		/**
		 * @return the itemPropertyIdParent
		 */
		public Object getItemPropertyIdParent() {
			return itemPropertyIdParent;
		}

		/**
		 * @return the itemPropertyIdHasChildren
		 */
		public Object getItemPropertyIdHasChildren() {
			return itemPropertyIdHasChildren;
		}

		/**
		 * @param itemPropertyIdHasChildren
		 *            the itemPropertyIdHasChildren to set
		 */
		public void setItemPropertyIdHasChildren(Object itemPropertyIdHasChildren) {
			this.itemPropertyIdHasChildren = itemPropertyIdHasChildren;
		}
	}

	class Indexes {
		private int level;
		private Integer indexFirstChild = null;
		private Integer indexLastChild = null;

		public Indexes(int level, Integer indexFirstChild, Integer indexLastChild) {
			super();
			this.level = level;
			this.indexFirstChild = indexFirstChild;
			this.indexLastChild = indexLastChild;
		}

		/**
		 * @return the level
		 */
		public int getLevel() {
			return level;
		}

		/**
		 * @return the indexFirstChild
		 */
		public Integer getIndexFirstChild() {
			return indexFirstChild;
		}

		/**
		 * @return the indexLastChild
		 */
		public Integer getIndexLastChild() {
			return indexLastChild;
		}
	}

	class HierarchicalId implements Serializable {
		private int level;
		private Object itemId;
		private HierarchicalId parentId;

		public HierarchicalId(int level, Object itemId, HierarchicalId parentId) {
			super();
			this.level = level;
			this.itemId = itemId;
			this.parentId = parentId;
		}

		/**
		 * @return the level
		 */
		public int getLevel() {
			return level;
		}

		/**
		 * @return the itemId
		 */
		public Object getItemId() {
			return itemId;
		}

		public HierarchicalId getParentId() {
			return parentId;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((itemId == null) ? 0 : itemId.hashCode());
			result = prime * result + level;
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof HierarchicalId)) {
				return false;
			}
			HierarchicalId other = (HierarchicalId) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (itemId == null) {
				if (other.itemId != null) {
					return false;
				}
			} else if (!itemId.equals(other.itemId)) {
				return false;
			}
			if (level != other.level) {
				return false;
			}
			return true;
		}

		private HierarchicalContainer getOuterType() {
			return HierarchicalContainer.this;
		}

		@Override
		public String toString() {
			return itemId == null ? null : itemId.toString();
		}
	}

	class HierarchicalSubList extends SubList<Object> {

		class HierarchicalListIteratorImpl extends ListIteratorImpl {

			HierarchicalListIteratorImpl(ListIterator<Object> i) {
				super(i);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see nl.ocs.utils.SubList.ListIteratorImpl#next()
			 */
			@Override
			public Object next() {
				return new HierarchicalId(level, super.next(), parentId);
			}
		}

		int level;
		HierarchicalId parentId;

		public HierarchicalSubList(int level, HierarchicalId parentId, List<Object> list,
				int fromIndex, int toIndex) {
			super(list, fromIndex, toIndex);
			this.level = level;
			this.parentId = parentId;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see nl.ocs.utils.SubList#get(int)
		 */
		@Override
		public HierarchicalId get(int index) {
			return new HierarchicalId(level, super.get(index), parentId);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see nl.ocs.utils.SubList#listIterator(int)
		 */
		@Override
		public ListIterator<Object> listIterator(int index) {
			rangeCheckForAdd(index);
			return new HierarchicalListIteratorImpl(l.listIterator(index + offset));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see nl.ocs.utils.SubList#subList(int, int)
		 */
		@Override
		public List<Object> subList(int fromIndex, int toIndex) {
			return new HierarchicalSubList(level, parentId, l, fromIndex, toIndex);
		}

	}

	public class ItemSetChangeEvent extends EventObject implements Container.ItemSetChangeEvent {

		private ItemSetChangeEvent(HierarchicalContainer source) {
			super(source);
		}

		@Override
		public Container getContainer() {
			return (Container) getSource();
		}
	}

	/**
	 * The hierarchical definitions for each level.
	 */
	private Map<Integer, HierarchicalDefinition> hierarchy = new HashMap<>();
	/**
	 * The primary property ids
	 */
	private List<?> primairyPropertyIds;
	/**
	 * Cache of child chunk indexes for a given parent
	 */
	private Map<HierarchicalId, Indexes> childIndexes = new HashMap<>();
	/**
	 * List of registered ItemSetChangeListener.
	 */
	private List<ItemSetChangeListener> itemSetChangeListeners = new ArrayList<ItemSetChangeListener>();

	private boolean itemSetChangeEventInProgress = false;

	/**
	 * Default constructor
	 */
	public HierarchicalContainer(Object... propertyIds) {
		this.primairyPropertyIds = Arrays.asList(propertyIds);
	}

	/**
	 * Get all the definitions by level
	 * 
	 * @return all hierarchical definitions
	 */
	public Map<Integer, HierarchicalDefinition> getHierarchy() {
		return hierarchy;
	}

	public HierarchicalDefinition getHierarchicalDefinitionByItemId(Object itemId) {
		HierarchicalId hId = (HierarchicalId) itemId;
		return getHierarchy().get(hId.getLevel());
	}

	/**
	 * Add hierarchical definition
	 * 
	 * @param definition
	 */
	public void addDefinition(HierarchicalDefinition definition) {
		getHierarchy().put(definition.getLevel(), definition);
		if (definition.getContainer() instanceof ItemSetChangeNotifier) {
			((ItemSetChangeNotifier) definition.getContainer()).addItemSetChangeListener(this);
		}
	}

	/**
	 * Add hierarchical definition
	 * 
	 * @param container
	 * @param level
	 * @param itemPropertyId
	 * @param itemPropertyIdParent
	 * @param propertyIds
	 */
	public void addDefinition(Indexed container, int level, Object itemPropertyId,
			Object itemPropertyIdParent, Object... propertyIds) {
		getHierarchy().put(
				level,
				new HierarchicalDefinition(container, level, itemPropertyId, itemPropertyIdParent,
						Arrays.asList(propertyIds)));
		if (container instanceof ItemSetChangeNotifier) {
			((ItemSetChangeNotifier) container).addItemSetChangeListener(this);
		}
	}

	protected Object unwrapItemId(Object itemId) {
		if (itemId == null) {
			return null;
		}
		if (!(itemId instanceof HierarchicalId)) {
			return itemId;
		}
		return ((HierarchicalId) itemId).getItemId();
	}

	protected Item mapItemProperties(HierarchicalDefinition definition, Item item) {
		if (item == null) {
			return null;
		}
		int i = 0;
		Collection<?> pids = item.getItemPropertyIds();
		for (Object p : getContainerPropertyIds()) {
			if (p != null && !pids.contains(p)) {
				Property<?> prop = item.getItemProperty(definition.getPropertyIds().get(i));
				if (prop != null) {
					item.addItemProperty(p, prop);
				}
			}
		}
		return item;
	}

	protected Collection<Item> mapItemProperties(HierarchicalDefinition definition,
			Collection<Item> items) {
		if (items != null) {
			for (Item item : items) {
				mapItemProperties(definition, item);
			}
		}
		return items;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Container#getItem(java.lang.Object)
	 */
	@Override
	public Item getItem(Object itemId) {
		HierarchicalDefinition def = getHierarchicalDefinitionByItemId(itemId);
		return mapItemProperties(def, def.getContainer().getItem(unwrapItemId(itemId)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Container#getContainerPropertyIds()
	 */
	@Override
	public Collection<?> getContainerPropertyIds() {
		return primairyPropertyIds;
	}

	protected void setContainerPropertyIds(List<?> primairyPropertyIds) {
		this.primairyPropertyIds = primairyPropertyIds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Container#getItemIds()
	 */
	@Override
	public List<?> getItemIds() {
		return rootItemIds();
	}

	public Object unmapProperty(HierarchicalDefinition definition, Object propertyId) {
		int i = primairyPropertyIds.indexOf(propertyId);
		if (i >= 0 && i < definition.getPropertyIds().size()) {
			return definition.getPropertyIds().get(i);
		}
		if (definition.getContainer().getContainerPropertyIds().contains(propertyId)) {
			return propertyId;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Container#getContainerProperty(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public Property<?> getContainerProperty(Object itemId, Object propertyId) {
		HierarchicalId hId = (HierarchicalId) itemId;
		HierarchicalDefinition def = getHierarchy().get(hId.getLevel());
		Object pId = unmapProperty(def, propertyId);
		return def.getContainer().getContainerProperty(hId.getItemId(), pId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Container#getType(java.lang.Object)
	 */
	@Override
	public Class<?> getType(Object propertyId) {
		for (int level : getHierarchy().keySet()) {
			HierarchicalDefinition def = getHierarchy().get(level);
			Object pId = unmapProperty(def, propertyId);
			if (pId != null) {
				return def.getContainer().getType(pId);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Container#size()
	 */
	@Override
	public int size() {
		return getHierarchy().get(0).getContainer().size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Container#containsId(java.lang.Object)
	 */
	@Override
	public boolean containsId(Object itemId) {
		HierarchicalId hId = (HierarchicalId) itemId;
		HierarchicalDefinition def = getHierarchy().get(hId.getLevel());
		return def.getContainer().containsId(hId.getItemId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Container#addItem(java.lang.Object)
	 */
	@Override
	public Item addItem(Object itemId) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Container#addItem()
	 */
	@Override
	public Object addItem() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Container#addContainerProperty(java.lang.Object,
	 * java.lang.Class, java.lang.Object)
	 */
	@Override
	public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Container#removeContainerProperty(java.lang.Object)
	 */
	@Override
	public boolean removeContainerProperty(Object propertyId) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Container#removeAllItems()
	 */
	@Override
	public boolean removeAllItems() {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected int searchIndexByPropertyValue(Indexed container, Object propertyId,
			Object parentIdValue, int startIndex, boolean first) {
		// Binary search
		int ll = startIndex;
		int ul = container.size() - 1;
		int i = ll + (ul - ll) / 2;
		int maxsteps = 16;
		while (true) {
			Object currentpv = container.getItem(container.getIdByIndex(i))
					.getItemProperty(propertyId).getValue();
			if (AbstractEntity.class.isInstance(currentpv)) {
				currentpv = ((AbstractEntity) currentpv).getId();
			}
			int ci = ((Comparable) parentIdValue).compareTo(currentpv);
			if (ci == 0) {
				if ((first && i == ll) || (!first && i == ul)) {
					break;
				} else {
					int io = first ? i - 1 : i + 1;
					Object otherpv = container.getItem(container.getIdByIndex(io))
							.getItemProperty(propertyId).getValue();
					if (AbstractEntity.class.isInstance(otherpv)) {
						otherpv = ((AbstractEntity) otherpv).getId();
					}
					if (!currentpv.equals(otherpv)) {
						break;
					} else if (first) {
						// Search lower
						ul = i - 1;
					} else {
						// Search higher
						ll = i + 1;
					}
				}
			} else if (ci < 0) {
				// Search lower
				ul = i - 1;
			} else if (ci > 0) {
				// Search higher
				ll = i + 1;
			}
			i = ll + (ul - ll) / 2;
			if (ul < ll || --maxsteps < 0) {
				i = -1;
				break;
			}
		}
		return i;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Container.Hierarchical#getChildren(java.lang.Object)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<?> getChildren(Object itemId) {
		HierarchicalId hId = (HierarchicalId) itemId;
		if ((hId.getLevel() + 1) < getHierarchy().size()) {
			HierarchicalDefinition cdef = getHierarchy().get(hId.getLevel() + 1);
			Indexes childIndex = getChildIndexes(hId, cdef);
			if (childIndex.getIndexFirstChild() >= 0 && childIndex.getIndexLastChild() >= 0
					&& childIndex.getIndexFirstChild() <= childIndex.getIndexLastChild()) {
				// Return subset of container
				return new HierarchicalSubList(cdef.getLevel(), hId,
						(List) cdef.container.getItemIds(), childIndex.getIndexFirstChild(),
						childIndex.getIndexLastChild() + 1);
			}
		}
		// TODO what to return when there are NO children?
		return null;
	}

	protected Indexes getChildIndexes(HierarchicalId parentItemId,
			HierarchicalDefinition childDefinition) {
		Indexes childIndex = null;
		// Get child indexes from cache
		if (childIndexes.containsKey(parentItemId)) {
			childIndex = childIndexes.get(parentItemId);
		} else {
			// Search first child
			// TODO may be optimized by using startindex
			int fi = searchIndexByPropertyValue(childDefinition.getContainer(),
					childDefinition.getItemPropertyIdParent(), parentItemId.getItemId(), 0, true);
			// Search last child
			// TODO may be optimized by using startindex
			int li = searchIndexByPropertyValue(childDefinition.getContainer(),
					childDefinition.getItemPropertyIdParent(), parentItemId.getItemId(), 0, false);
			childIndex = new Indexes(parentItemId.getLevel(), fi, li);
			childIndexes.put(parentItemId, childIndex);
		}
		return childIndex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Container.Hierarchical#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object itemId) {
		HierarchicalId hId = (HierarchicalId) itemId;
		return hId.getParentId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Container.Hierarchical#rootItemIds()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<?> rootItemIds() {
		HierarchicalDefinition def = getHierarchy().get(0);
		return new HierarchicalSubList(def.getLevel(), null,
				(List<Object>) def.container.getItemIds(), 0, def.container.size());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Container.Hierarchical#setParent(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public boolean setParent(Object itemId, Object newParentId) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vaadin.data.Container.Hierarchical#areChildrenAllowed(java.lang.Object
	 * )
	 */
	@Override
	public boolean areChildrenAllowed(Object itemId) {
		HierarchicalId hId = (HierarchicalId) itemId;
		return hId.getLevel() < (getHierarchy().size() - 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vaadin.data.Container.Hierarchical#setChildrenAllowed(java.lang.Object
	 * , boolean)
	 */
	@Override
	public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Container.Hierarchical#isRoot(java.lang.Object)
	 */
	@Override
	public boolean isRoot(Object itemId) {
		HierarchicalId hId = (HierarchicalId) itemId;
		return hId.getLevel() == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Container.Hierarchical#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object itemId) {
		HierarchicalId hId = (HierarchicalId) itemId;
		if ((hId.getLevel() + 1) < getHierarchy().size()) {
			// Can have children
			HierarchicalDefinition def = getHierarchy().get(hId.getLevel());
			if (def.itemPropertyIdHasChildren != null) {
				// Use property
				Property<?> p = def.getContainer().getContainerProperty(hId.getItemId(),
						def.getItemPropertyIdHasChildren());
				return p != null && Boolean.TRUE.equals(p.getValue());
			} else {
				// Search child
				HierarchicalDefinition cdef = getHierarchy().get(hId.getLevel() + 1);
				Indexes childIndex = getChildIndexes(hId, cdef);
				return childIndex.getIndexFirstChild() >= 0;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Container.Hierarchical#removeItem(java.lang.Object)
	 */
	@Override
	public boolean removeItem(Object itemId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object nextItemId(Object itemId) {
		HierarchicalId hId = (HierarchicalId) itemId;
		HierarchicalDefinition def = getHierarchy().get(hId.getLevel());
		if (!def.container.isLastId(hId.getItemId())) {
			// Get next index
			int i = def.container.indexOfId(hId.getItemId()) + 1;
			// Find parent
			HierarchicalId pId = hId.getParentId();
			if (pId != null) {
				Indexes pi = childIndexes.get(pId);
				// Check if the next has the same parent
				if (pi.getIndexLastChild() < i) {
					// Not the same parent -> search next parent
					pId = new HierarchicalId(hId.getLevel(), def.getContainer().nextItemId(
							hId.getItemId()), pId);
				}
			}
			// Create item id
			return new HierarchicalId(hId.getLevel(), def.container.getIdByIndex(i), pId);
		} else {
			// Find next parent
			HierarchicalId pId = hId.getParentId();
			if (pId != null) {
				return nextItemId(pId);
			}
		}
		return null;
	}

	@Override
	public Object prevItemId(Object itemId) {
		HierarchicalId hId = (HierarchicalId) itemId;
		HierarchicalDefinition def = getHierarchy().get(hId.getLevel());
		if (!def.container.isFirstId(hId.getItemId())) {
			// Get previous index
			int i = def.container.indexOfId(hId.getItemId()) - 1;
			// Find parent
			HierarchicalId pId = hId.getParentId();
			if (pId != null) {
				Indexes pi = childIndexes.get(pId);
				// Check if the previous has the same parent
				if (i < pi.getIndexFirstChild()) {
					// Not the same parent -> search next parent
					pId = new HierarchicalId(hId.getLevel(), def.getContainer().nextItemId(
							hId.getItemId()), pId);
				}
			}
			return new HierarchicalId(hId.getLevel(), def.container.getIdByIndex(i), pId);
		} else {
			// Find previous parent
			HierarchicalId pId = hId.getParentId();
			if (pId != null) {
				return prevItemId(pId);
			}
		}
		return null;
	}

	@Override
	public Object firstItemId() {
		HierarchicalDefinition def = getHierarchy().get(0);
		if (def.getContainer().size() > 0) {
			return new HierarchicalId(0, def.container.firstItemId(), null);
		}
		return null;
	}

	@Override
	public Object lastItemId() {
		HierarchicalDefinition def = getHierarchy().get(0);
		if (def.getContainer().size() > 0) {
			return new HierarchicalId(0, def.container.lastItemId(), null);
		}
		return null;
	}

	@Override
	public boolean isFirstId(Object itemId) {
		if (itemId == null) {
			return false;
		}
		HierarchicalId hId = (HierarchicalId) itemId;
		HierarchicalDefinition def = getHierarchy().get(hId.getLevel());
		return def.container.isFirstId(hId.getItemId());
	}

	@Override
	public boolean isLastId(Object itemId) {
		if (itemId == null) {
			return false;
		}
		HierarchicalId hId = (HierarchicalId) itemId;
		HierarchicalDefinition def = getHierarchy().get(hId.getLevel());
		return def.container.isLastId(hId.getItemId());
	}

	@Override
	public Object addItemAfter(Object previousItemId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Item addItemAfter(Object previousItemId, Object newItemId) {
		throw new UnsupportedOperationException();
	}

	public HierarchicalDefinition getHierarchicalDefinition(int level) {
		return getHierarchy().get(level);
	}

	@Override
	public void addItemSetChangeListener(ItemSetChangeListener listener) {
		itemSetChangeListeners.add(listener);
	}

	@Override
	@Deprecated
	public void addListener(ItemSetChangeListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeItemSetChangeListener(ItemSetChangeListener listener) {
		itemSetChangeListeners.remove(listener);
	}

	@Override
	@Deprecated
	public void removeListener(ItemSetChangeListener listener) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Notifies that item set has been changed.
	 */
	private void notifyItemSetChanged() {
		ItemSetChangeEvent event = new ItemSetChangeEvent(this);
		for (ItemSetChangeListener listener : itemSetChangeListeners) {
			listener.containerItemSetChange(event);
		}
	}

	@Override
	public void containerItemSetChange(com.vaadin.data.Container.ItemSetChangeEvent event) {
		if (!itemSetChangeEventInProgress) {
			itemSetChangeEventInProgress = true;
			// Clear indexes
			childIndexes.clear();
			// Refresh other lazy containers
			for (HierarchicalDefinition def : getHierarchy().values()) {
				if (def.getContainer() != event.getContainer()) {
					if (def.getContainer() instanceof LazyQueryContainer) {
						((LazyQueryContainer) def.getContainer()).refresh();
					}
				}
			}
			// Notify dependent objects
			notifyItemSetChanged();
			itemSetChangeEventInProgress = false;
		}
	}

	@Override
	public void sort(Object[] propertyId, boolean[] ascending) {
		for (Integer level : getHierarchy().keySet()) {
			HierarchicalDefinition def = getHierarchicalDefinition(level);
			Indexed c = def.getContainer();
			if (c instanceof Sortable) {
				// Find sortable properties for this container
				List<Object> sids = new ArrayList<>();
				List<Integer> asci = new ArrayList<>();
				for (int i = 0; i < propertyId.length; i++) {
					Object pid = unmapProperty(def, propertyId[i]);
					if (((Sortable) c).getSortableContainerPropertyIds().contains(pid)) {
						sids.add(propertyId[i]);
						asci.add(i);
					}
				}
				if (!sids.isEmpty()) {
					// Copy boolean asc to array
					boolean[] asc = new boolean[sids.size()];
					for (int i = 0; i < sids.size(); i++) {
						asc[i] = ascending[asci.get(i)];
					}
					((Sortable) c).sort(sids.toArray(new Object[0]), asc);
				}
			}
		}
	}

	@Override
	public Collection<?> getSortableContainerPropertyIds() {
		Set<Object> sortablePids = new LinkedHashSet<>();
		for (Object pid : getContainerPropertyIds()) {
			for (Integer level : getHierarchy().keySet()) {
				HierarchicalDefinition def = getHierarchicalDefinition(level);
				Indexed c = def.getContainer();
				Object mpid = unmapProperty(def, pid);
				if (c instanceof Sortable && mpid != null
						&& ((Sortable) c).getSortableContainerPropertyIds().contains(mpid)) {
					sortablePids.add(pid);
				}
			}
		}
		return sortablePids;
	}
}
