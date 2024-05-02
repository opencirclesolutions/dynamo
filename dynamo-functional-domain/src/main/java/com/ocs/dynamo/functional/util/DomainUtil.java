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
	 * Creates a new entity if it does not exist. Otherwise, returns the existing entity
	 * 
	 * @param service       the service used to retrieve the item
	 * @param clazz         the domain class
	 * @param value         the value of the "name" attribute
	 * @param caseSensitive whether to check for case-sensitive values
	 * @return the existing or newly created entity
	 */
	public static <T extends Domain> T createIfNotExists(BaseService<?, T> service, Class<T> clazz, String value,
			boolean caseSensitive) {
		T entity = service.findByUniqueProperty(Domain.ATTRIBUTE_NAME, value, caseSensitive);
		if (entity == null) {
			entity = ClassUtils.instantiateClass(clazz);
			entity.setName(value);
			entity = service.save(entity);
		}
		return entity;
	}

	/**
	 * Returns all domain entities that match the specified type
	 * 
	 * @param clazz   the type
	 * @param domains the set of all domain values
	 * @return the set of matching entities
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Domain> Set<T> filterDomains(Class<T> clazz, Collection<Domain> domains) {
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
	 * Updates a certain category of domain items, removing all current values and
	 * replacing them by the new ones
	 * 
	 * @param clazz     the domain type
	 * @param domains   all current domain values
	 * @param newValues the set of new values
	 */
	public static <T extends Domain> void updateDomains(Class<T> clazz, Collection<Domain> domains,
			Collection<T> newValues) {
		domains.removeIf(domain -> domain != null && domain.getClass().isAssignableFrom(clazz));
		if (newValues != null) {
			newValues.stream().filter(Objects::nonNull).forEach(domains::add);
		}
	}

	/**
	 * Returns a string containing the descriptions of the supplied domain objects
	 * (truncated after a number of items)
	 * 
	 * @param domains the domains
	 * @return the description string
	 */
	public static <T extends Domain> String getDomainDescriptions(MessageService messageService, Collection<T> domains,
			Locale locale) {
		String result = domains.stream().map(Domain::getName).sorted().limit(MAX_DESCRIPTION_ITEMS)
				.collect(Collectors.joining(", "));
		if (domains.size() > MAX_DESCRIPTION_ITEMS) {
			result += messageService.getMessage("ocs.and.others", locale, domains.size() - MAX_DESCRIPTION_ITEMS);
		}
		return result;
	}
}
