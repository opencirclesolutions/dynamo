package com.ocs.dynamo.ui.container.pivot;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Container;
import com.vaadin.data.Item;

/**
 * @author Patrick Deenen
 */
public class PivotIdList extends AbstractList<Integer> {

	private Map<Integer, Integer> cachedIds = new HashMap<Integer, Integer>();

	private PivotContainer container;

	/**
	 * Constructs a Id list by delegating to a given container.
	 * 
	 * @param container
	 *            Mandatory reference to pivot container
	 */
	public PivotIdList(PivotContainer container) {
		if (container == null) {
			throw new AssertionError("Container is mandatory");
		}
		this.container = container;
	}

	@Override
	public Integer get(int index) {
		if (index < 0 || index >= container.size()) {
			throw new IndexOutOfBoundsException();
		}
		// Check is index is requested before
		if (cachedIds.containsKey(index)) {
			return cachedIds.get(index);
		}

		// Search the record with the highest index
		int startIndex = 0;
		int ci = index - 1;
		while (ci >= 0) {
			if (cachedIds.containsKey(ci)) {
				startIndex = cachedIds.get(ci);
				break;
			}
			ci--;
		}
		if (ci < 0) {
			ci = 0;
		}
		// Search the right record
		Container.Indexed sc = (Container.Indexed) container.getSourceContainer();
		Object rowValue = null;
		Object newRowValue = null;
		for (int i = startIndex; i < sc.size(); i++) {
			Item column = sc.getItem(sc.getIdByIndex(i));
			if (column != null) {
				newRowValue = column.getItemProperty(container.getRowPropertyId()).getValue();
				if (rowValue == null) {
					// Initialize the row value
					rowValue = newRowValue;
				} else if (!newRowValue.equals(rowValue)) {
					// When row value changes increase current index
					ci++;
					rowValue = newRowValue;
				}
			}
			if (index == ci) {
				cachedIds.put(index, i);
				return i;
			}
		}
		return null;
	}

	@Override
	public int indexOf(Object id) {
		if (id == null) {
			throw new AssertionError("Id is mandatory");
		}
		// Search in cache
		for (Integer index : cachedIds.keySet()) {
			if (id.equals(cachedIds.get(index))) {
				return index;
			}
		}
		return -1;
	}

	@Override
	public int size() {
		return this.container.size();
	}

	@Override
	public List<Integer> subList(int fromIndex, int toIndex) {
		List<Integer> sl = new ArrayList<>();
		for (int i = fromIndex; i < toIndex; i++) {
			sl.add(get(i));
		}
		return sl;
	}

}
