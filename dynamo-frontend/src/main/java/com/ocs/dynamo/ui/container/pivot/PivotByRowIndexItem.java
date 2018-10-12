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
package com.ocs.dynamo.ui.container.pivot;

import java.util.Collection;

import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;

/**
 * @author Patrick Deenen
 */
public class PivotByRowIndexItem implements Item {

	private static final long serialVersionUID = -8298765814386175038L;
	private Container.Indexed sourceContainer;
	private PivotByRowIndexContainer pivotContainer;
	private int row;

	/**
	 * Constructs the composite item
	 * 
	 * @param sourceContainer
	 * @param row
	 *            Source row of the first column
	 */
	public PivotByRowIndexItem(PivotByRowIndexContainer pivotContainer, Container.Indexed sourceContainer, int row) {
		if (pivotContainer == null) {
			throw new IllegalArgumentException("pivotContainer is mandatory");
		}
		if (sourceContainer == null) {
			throw new IllegalArgumentException("sourceContainer is mandatory");
		}
		this.pivotContainer = pivotContainer;
		this.sourceContainer = sourceContainer;
		this.row = row;
	}

	/**
	 * Get the property in the row. When the name is in the form "columnValue_columnPropertyId" then
	 * a property in the column designated by the columnValue is assumed. Otherwise the property is
	 * assumed to be equal over all columns and will be taken from the first item.
	 * 
	 * @see com.vaadin.data.Item#getItemProperty(java.lang.Object)
	 */
	@SuppressWarnings({ "rawtypes" })
	@Override
	public Property getItemProperty(Object id) {
		Property result = null;
		if (id != null) {
			String[] ids = id.toString().split("_");
			Integer i = 0;
			Object cId = null;
			// Test for column number
			try {
				i = new Integer(ids[0]);
			} catch (NumberFormatException e) {
				// When not a number assume columnPropertyId
				cId = ids[0];
			}
			// When both columnValue and columnPropertyId are given use second id as columnPropertyId
			if (ids.length > 1) {
				cId = ids[1];
			}
			if ((row + i) < sourceContainer.size()) {
				Object itemId = sourceContainer.getIdByIndex(row + i);
				if (cId == null) {
					// When columnValue is a number and columnPropertyId is omitted return
					Object r = VaadinUtils.getEntityFromItem(sourceContainer.getItem(itemId));
					if (r != null) {
						result = new ObjectProperty(r);
					}
				} else {
					result = sourceContainer.getContainerProperty(itemId, cId);
				}
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.Item#getItemPropertyIds()
	 */
	@Override
	public Collection<?> getItemPropertyIds() {
		return pivotContainer.getContainerPropertyIds();
	}

	@Override
	public boolean addItemProperty(Object id, Property property) throws UnsupportedOperationException {
		// FIXME Add support for adding properties
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
		// FIXME Add support for removing properties
		throw new UnsupportedOperationException();
	}
}
