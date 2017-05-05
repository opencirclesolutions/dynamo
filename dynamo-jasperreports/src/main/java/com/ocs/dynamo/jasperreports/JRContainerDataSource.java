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

import java.util.Iterator;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * JasperReports datasource implementation which uses a Vaadin container as
 * source.
 * 
 * Assumes that nested properties are named with underscores in the report, e.g.
 * an property in the vaadin container "customer.name" is referenced by
 * "customer_name" in jasperreports.
 * 
 * @author Patrick Deenen (patrick@opencircle.solutions)
 *
 */
public class JRContainerDataSource implements JRDataSource {

	private Container container;

	private Object currentItemId;

	private Item currentItem;

	private Iterator<?> ids;

	/**
	 * Construct the data source using a Vaadin Indexed container
	 * 
	 * @param container
	 */
	public JRContainerDataSource(Container container) {
		this.container = container;
		ids = container.getItemIds().iterator();
		if (ids != null && ids.hasNext()) {
			currentItemId = ids.next();
			currentItem = container.getItem(currentItemId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports
	 * .engine.JRField)
	 */
	@Override
	public Object getFieldValue(JRField field) throws JRException {
		if (currentItemId != null) {
			String fieldName = field.getName().replaceAll("_", ".");
			Property<?> p = currentItem.getItemProperty(fieldName);
			return p == null ? null : p.getValue();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jasperreports.engine.JRDataSource#next()
	 */
	@Override
	public boolean next() throws JRException {
		if (ids != null && ids.hasNext()) {
			currentItemId = ids.next();
			currentItem = container.getItem(currentItemId);
			return true;
		}
		return false;
	}
}
