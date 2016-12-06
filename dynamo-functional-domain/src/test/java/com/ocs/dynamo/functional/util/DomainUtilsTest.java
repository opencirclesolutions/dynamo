package com.ocs.dynamo.functional.util;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Sets;
import com.ocs.dynamo.functional.domain.Country;
import com.ocs.dynamo.functional.domain.Currency;
import com.ocs.dynamo.functional.domain.Domain;
import com.ocs.dynamo.functional.domain.Region;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;

public class DomainUtilsTest extends BaseMockitoTest {

	@Mock
	private MessageService messageService;

	private Set<Domain> domains;

	@Mock
	private BaseService<Integer, Country> service;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		Currency c1 = new Currency("EUR", "Euro");
		Currency c2 = new Currency("USD", "Dollar");
		Country coun1 = new Country("NL", "Netherlands");

		domains = new HashSet<Domain>();
		domains.add(c1);
		domains.add(c2);
		domains.add(coun1);
	}

	@Test
	public void testFilterDomains() {

		Set<Currency> cs = DomainUtil.filterDomains(Currency.class, null);
		Assert.assertEquals(0, cs.size());

		cs = DomainUtil.filterDomains(Currency.class, domains);
		Assert.assertEquals(2, cs.size());

		Set<Country> countries = DomainUtil.filterDomains(Country.class, domains);
		Assert.assertEquals(1, countries.size());

		Set<Region> regions = DomainUtil.filterDomains(Region.class, domains);
		Assert.assertEquals(0, regions.size());
	}

	@Test
	public void testUpdateDomains() {

		// replace currencies with new one
		DomainUtil.updateDomains(Currency.class, domains, Sets.newHashSet(new Currency("GBP", "English Pound")));
		Assert.assertEquals(2, domains.size());

		// remove all currencies
		DomainUtil.updateDomains(Currency.class, domains, new HashSet<Currency>());
		Assert.assertEquals(1, domains.size());

		// remove all currencies
		DomainUtil.updateDomains(Country.class, domains,
		        Sets.newHashSet(new Country("USA", "United States Of America")));
		Assert.assertEquals(1, domains.size());
	}

	@Test
	public void testGetDomainDescriptions() {

		String res = DomainUtil
		        .getDomainDescriptions(messageService, DomainUtil.filterDomains(Currency.class, domains));
		Assert.assertEquals("Dollar, Euro", res);

		domains.add(new Currency("SEK", "Swedish Crown"));
		domains.add(new Currency("NEK", "Norwegian Crown"));
		domains.add(new Currency("DEK", "Danish Crown"));

		Mockito.when(messageService.getMessage(Matchers.eq("ocs.and.others"), Matchers.anyInt())).thenAnswer(
		        new Answer<String>() {

			        @Override
			        public String answer(InvocationOnMock invocation) throws Throwable {
				        return " and " + invocation.getArguments()[1] + " others";
			        }
		        });

		res = DomainUtil.getDomainDescriptions(messageService, DomainUtil.filterDomains(Currency.class, domains));

		Assert.assertEquals("Danish Crown, Dollar, Euro and 2 others", res);
	}

	@Test
	public void testCreateIfNotExists_Create() {
		MockUtil.mockServiceSave(service, Country.class);

		Country country = DomainUtil.createIfNotExists(service, Country.class, "SomeName", true);
		Assert.assertNotNull(country);

		// verify that a country is saved
		Mockito.verify(service).save(Matchers.any(Country.class));
	}

	@Test
	public void testCreateIfNotExists_DoNotCreate() {
		Country existing = new Country();
		existing.setName("SomeName");
		existing.setId(44);
		Mockito.when(service.findByUniqueProperty(Domain.NAME, "SomeName", true)).thenReturn(existing);

		Country country = DomainUtil.createIfNotExists(service, Country.class, "SomeName", true);
		Assert.assertNotNull(country);
		Assert.assertEquals(existing.getId(), country.getId());

		// verify that a country is saved
		Mockito.verify(service, Mockito.times(0)).save(Matchers.any(Country.class));
	}

}
