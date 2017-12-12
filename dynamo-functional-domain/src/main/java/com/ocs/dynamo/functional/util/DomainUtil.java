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
package com.ocs.dynamo.functional.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.ocs.dynamo.functional.domain.Domain;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.utils.ClassUtils;

/**
 * Utility methods for dealing with domains
 * 
 * @author bas.rutten
 *
 */
public final class DomainUtil {

	private static final int MAX_DESCRIPTION_ITEMS = 3;

	private DomainUtil() {
	}

	/**
	 * Creates a new item if it does not exist. Otherwise, returns the existing
	 * item
	 * 
	 * @param service
	 *            the service used to retrieve the item
	 * @param clazz
	 *            the domain class
	 * @param value
	 *            the value of the "name" attribute
	 * @param caseSensitive
	 *            whether to check for case-sensitive values
	 * @return
	 */
	public static <T extends Domain> T createIfNotExists(BaseService<?, T> service, Class<T> clazz, String value,
			boolean caseSensitive) {
		T t = service.findByUniqueProperty(Domain.ATTRIBUTE_NAME, value, caseSensitive);
		if (t == null) {
			t = ClassUtils.instantiateClass(clazz);
			t.setName(value);
			t = service.save(t);
		}
		return t;
	}

	/**
	 * Returns all domain value that match the specified type
	 * 
	 * @param clazz
	 *            the type
	 * @param domains
	 *            the set of all domain values
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Domain> Set<T> filterDomains(Class<T> clazz, Set<Domain> domains) {
		Set<T> result = new HashSet<>();
		if (domains != null) {
			for (Domain d : domains) {
				if (d != null && d.getClass().isAssignableFrom(clazz)) {
					result.add((T) d);
				}
			}
		}
		return result;
	}

	/**
	 * Updates a certain category of domain items, removing all current values
	 * and replacing them by the new ones
	 * 
	 * @param clazz
	 *            the domain type
	 * @param domains
	 *            all current domain values
	 * @param newValues
	 *            the set of new values
	 */
	public static <T extends Domain> void updateDomains(Class<T> clazz, Set<Domain> domains, Set<T> newValues) {
		domains.removeIf(domain -> domain != null && domain.getClass().isAssignableFrom(clazz));
		if (newValues != null) {
			newValues.stream().filter(Objects::nonNull).forEach(v -> domains.add(v));
		}
	}

	/**
	 * Returns a string containing the descriptions of the supplied domain
	 * objects (truncated after a number of items)
	 * 
	 * @param domains
	 *            the domains
	 * @return
	 */
	public static <T extends Domain> String getDomainDescriptions(MessageService messageService, Collection<T> domains,
			Locale locale) {
		String result = domains.stream().map(x -> x.getName()).sorted().limit(MAX_DESCRIPTION_ITEMS)
				.collect(Collectors.joining(", "));
		if (domains.size() > MAX_DESCRIPTION_ITEMS) {
			result += messageService.getMessage("ocs.and.others", locale, domains.size() - MAX_DESCRIPTION_ITEMS);
		}
		return result;
	}
}
