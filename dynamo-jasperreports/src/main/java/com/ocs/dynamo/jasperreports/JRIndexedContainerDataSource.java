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
package com.ocs.dynamo.jasperreports;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import net.sf.jasperreports.engine.data.IndexedDataSource;

/**
 * JasperReports datasource implementation which uses a Vaadin container as source. Optimized for an indexed container.
 * 
 * Assumes that nested properties are named with underscores in the report, e.g. an property in the vaadin container
 * "customer.name" is referenced by "customer_name" in jasperreports.
 * 
 * @author Patrick Deenen (patrick@opencircle.solutions)
 *
 */
public class JRIndexedContainerDataSource implements JRRewindableDataSource, IndexedDataSource {
	private Container.Indexed container;
	private Object currentItemId;
	private Item currentItem;

	/**
	 * Construct the datasource using a Vaadin Indexed container
	 * 
	 * @param container
	 */
	public JRIndexedContainerDataSource(Container.Indexed container) {
		this.container = container;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports .engine.JRField)
	 */
	@Override
	public Object getFieldValue(JRField field) throws JRException {
		Object result = null;
		if (currentItem != null) {
			String fieldName = field.getName().replaceAll("_", ".");
			Property<?> p = currentItem.getItemProperty(fieldName);
			if (p == null) {
				fieldName = field.getPropertiesMap().getProperty(JRUtils.CONTAINER_PROPERTY_NAME);
				p = currentItem.getItemProperty(fieldName);
			}
			if (p != null) {
				result = p.getValue();
				if (result != null && !(result instanceof String) && field.getValueClass() == String.class) {
					result = result.toString();
				}
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jasperreports.engine.JRDataSource#next()
	 */
	@Override
	public boolean next() throws JRException {
		if (currentItem == null) {
			moveFirst();
			return currentItem != null;
		}
		currentItemId = container.nextItemId(currentItemId);
		currentItem = container.getItem(currentItemId);
		return currentItem != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jasperreports.engine.JRRewindableDataSource#moveFirst()
	 */
	@Override
	public void moveFirst() throws JRException {
		if (container.size() > 0) {
			currentItemId = container.firstItemId();
			currentItem = container.getItem(currentItemId);
		} else {
			currentItemId = null;
			currentItem = null;
		}
	}

	@Override
	public int getRecordIndex() {
		return container.indexOfId(currentItemId);
	}

	public Container.Indexed getContainer() {
		return container;
	}
}
