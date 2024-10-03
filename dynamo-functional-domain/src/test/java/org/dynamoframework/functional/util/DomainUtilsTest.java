package org.dynamoframework.functional.util;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.dynamoframework.functional.domain.Country;
import org.dynamoframework.functional.domain.Currency;
import org.dynamoframework.functional.domain.Domain;
import org.dynamoframework.functional.domain.Region;
import org.dynamoframework.service.BaseService;
import org.dynamoframework.service.MessageService;
import org.dynamoframework.test.BaseMockitoTest;
import org.dynamoframework.test.MockUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class DomainUtilsTest extends BaseMockitoTest {

	@Mock
	private MessageService messageService;

	@Mock
	private BaseService<Integer, Country> service;

	private Set<Domain> domains;

	@BeforeEach
	public void setUp() {

		Currency c1 = new Currency("EUR", "Euro");
		Currency c2 = new Currency("USD", "Dollar");
		Country coun1 = new Country("NL", "Netherlands");

		domains = new HashSet<>();
		domains.add(c1);
		domains.add(c2);
		domains.add(coun1);
	}

	@Test
	public void testFilterDomains() {

		Set<Currency> cs = DomainUtil.filterDomains(Currency.class, null);
		assertEquals(0, cs.size());

		cs = DomainUtil.filterDomains(Currency.class, domains);
		assertEquals(2, cs.size());

		Set<Country> countries = DomainUtil.filterDomains(Country.class, domains);
		assertEquals(1, countries.size());

		Set<Region> regions = DomainUtil.filterDomains(Region.class, domains);
		assertEquals(0, regions.size());
	}

	@Test
	public void testUpdateDomains() {

		// replace currencies with new one
		DomainUtil.updateDomains(Currency.class, domains, Set.of(new Currency("GBP", "English Pound")));
		assertEquals(2, domains.size());

		// remove all currencies
		DomainUtil.updateDomains(Currency.class, domains, new HashSet<>());
		assertEquals(1, domains.size());

		// remove all currencies
		DomainUtil.updateDomains(Country.class, domains,
			Set.of(new Country("USA", "United States Of America")));
		assertEquals(1, domains.size());
	}

	@Test
	public void testGetDomainDescriptions() {

		String res = DomainUtil.getDomainDescriptions(messageService, DomainUtil.filterDomains(Currency.class, domains),
			new Locale("en"));
		assertEquals("Dollar, Euro", res);

		domains.add(new Currency("SEK", "Swedish Crown"));
		domains.add(new Currency("NEK", "Norwegian Crown"));
		domains.add(new Currency("DEK", "Danish Crown"));

		when(messageService.getMessage(eq("dynamoframework.and.others"), any(Locale.class), anyInt()))
			.thenAnswer(invocation -> " and " + invocation.getArguments()[2] + " others");

		res = DomainUtil.getDomainDescriptions(messageService, DomainUtil.filterDomains(Currency.class, domains),
			new Locale("nl"));

		assertEquals("Danish Crown, Dollar, Euro and 2 others", res);
	}

	@Test
	public void testCreateIfNotExists_Create() {
		MockUtil.mockServiceSave(service, Country.class);

		Country country = DomainUtil.createIfNotExists(service, Country.class, "SomeName", true);
		assertNotNull(country);

		// verify that a country is saved
		verify(service).save(any(Country.class));
	}

	@Test
	public void testCreateIfNotExists_DoNotCreate() {
		Country existing = new Country();
		existing.setName("SomeName");
		existing.setId(44);
		when(service.findByUniqueProperty(Domain.ATTRIBUTE_NAME, "SomeName", true)).thenReturn(existing);

		Country country = DomainUtil.createIfNotExists(service, Country.class, "SomeName", true);
		assertNotNull(country);
		assertEquals(existing.getId(), country.getId());

		// verify that a country is saved
		verify(service, times(0)).save(any(Country.class));
	}

}
