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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang.StringUtils;

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.filter.PredicateUtil;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.server.SerializablePredicate;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRPropertiesMap;
import net.sf.jasperreports.engine.JasperReport;

/**
 * Utility methods for JasperReports with Dynamo.
 *
 * @author Patrick Deenen (patrick@opencircle.solutions)
 */
public final class JRUtils {

	public static final String CONTAINER_PROPERTY_NAME = "com.ocs.dynamo.containerPropertyName";

	public static final String FILTER_PROPERTY_NAME = "com.ocs.dynamo.filterPropertyName";

	public static final String FILTER_TYPE = "com.ocs.dynamo.filterType";

	public static final String PROPERTY_NESTED_NAME = "com.ocs.dynamo.propertyNestedName";

	private JRUtils() {
		// default constructor
	}

	/**
	 * 
	 * @param container
	 * @param jasperReport
	 */
	public static <T> void addContainerPropertiesFromReport(DataProvider<T, SerializablePredicate<T>> provider,
			JasperReport jasperReport) {
//		if (provider == null || jasperReport == null) {
//			return;
//		}
//		Collection<?> ids = container.getContainerPropertyIds();
//		for (JRField f : jasperReport.getFields()) {
//			if (f.hasProperties() && f.getPropertiesMap().containsProperty(CONTAINER_PROPERTY_NAME)) {
//				String cpn = f.getPropertiesMap().getProperty(CONTAINER_PROPERTY_NAME);
//				if (!ids.contains(cpn)) {
//					if (container instanceof AbstractBeanContainer<?, ?>) {
//						((AbstractBeanContainer<?, ?>) container).addNestedContainerProperty(cpn);
//					} else {
//						container.addContainerProperty(cpn, f.getValueClass(), null);
//					}
//				}
//			}
//		}
	}

	/**
	 * Create report parameters based on a filter value to defined report parameter
	 * when the parameter has the name of the filterproperty defined as a property.
	 *
	 * @param jasperReport The report
	 * @param filter       The (composite) filter
	 * @return a parameter map with the parameter values defined
	 */
	@SuppressWarnings("unchecked")
	public static <T> Map<String, Object> createParametersFromFilter(JasperReport jasperReport,
			SerializablePredicate<T> filter) {
		Map<String, Object> fillParameters = new HashMap<>();
		if (jasperReport != null && filter != null) {
			for (JRParameter p : jasperReport.getParameters()) {
				JRPropertiesMap pm = p.getPropertiesMap();
				final String parameterName = p.getName();
				if (pm.containsProperty(FILTER_PROPERTY_NAME)) {
					String name = pm.getProperty(FILTER_PROPERTY_NAME);
					Class<? extends SerializablePredicate<T>> ptype = null;
					try {
						ptype = (Class<? extends SerializablePredicate<T>>) Class.forName(pm.getProperty(FILTER_TYPE));
					} catch (ClassNotFoundException | NullPointerException e) {
						// Do nothing
					}
					if (StringUtils.isNotBlank(name)) {
						Object result = PredicateUtil.extractPredicateValue(filter, name, ptype);

						if (result != null) {
							final String nestedPropertyName = pm.getProperty(PROPERTY_NESTED_NAME);
							if (StringUtils.isNotBlank(nestedPropertyName)) {
								if (result instanceof Collection) {
									result = getPropertyNestedValue(fillParameters, parameterName, nestedPropertyName,
											(Collection<?>) result);
								} else {
									result = getNestedProperty(nestedPropertyName, result);
								}
							}
							fillParameters.put(parameterName, result);
						}
					}
				}
			}
		}
		return fillParameters;
	}

	/**
	 * Find a parameter which has a parameter property with a specific value.
	 *
	 * @param jasperReport  the report
	 * @param propertyName  the property name to search for
	 * @param propertyValue the property value to search for
	 * @return the parameter or null
	 */
	public static JRParameter findParameterWithPropertyValue(JasperReport jasperReport, String propertyName,
			Object propertyValue) {
		JRParameter result = null;
		if (jasperReport != null && propertyName != null && !"".equals(propertyName) && propertyValue != null) {
			for (JRParameter p : jasperReport.getParameters()) {
				String value = p.getPropertiesMap().getProperty(propertyName);
				if (propertyValue.equals(value)) {
					result = p;
					break;
				}
			}
		}
		return result;
	}

	private static Object getNestedProperty(String prop, Object bean) {
		try {
			return BeanUtilsBean.getInstance().getPropertyUtils().getNestedProperty(bean, prop);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new OCSRuntimeException("Failed to get bean property", e);
		}
	}

	@SuppressWarnings("unchecked")
	private static Collection<?> getPropertyNestedValue(Map<String, Object> fillParameters, String parameterName,
			String nestedPropertyName, Collection<?> result) {
		Collection<Object> resultCollection = (Collection<Object>) fillParameters.remove(parameterName);

		if (resultCollection == null) {
			resultCollection = new ArrayList<>();
		}
		for (Object o : result) {
			resultCollection.add(getNestedProperty(nestedPropertyName, o));
		}
		return resultCollection;
	}

}
