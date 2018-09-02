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
package com.ocs.dynamo.functional.service;

import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.functional.domain.Country;
import com.ocs.dynamo.functional.domain.Currency;
import com.ocs.dynamo.functional.domain.Domain;
import com.ocs.dynamo.functional.domain.Region;
import com.ocs.dynamo.service.impl.DefaultServiceImpl;
import com.ocs.dynamo.test.BaseIntegrationTest;

/**
 * @author Patrick Deenen (patrick@opencircle.solutions)
 *
 */
public class DomainServiceTest extends BaseIntegrationTest {

	@Inject
	private DomainService domainService;

	@Inject
	private DomainService regionService;

	@Inject
	private DefaultServiceImpl<Integer, Currency> currencyService;

	Region europa;
	Region asia;

	@Before
	public void setup() {
		europa = new Region("EU", "Europa");
		asia = new Region("AS", "Asia");
		Country nl = new Country("NL", "The Netherlands");
		europa.addChild(nl);
		Country be = new Country("BE", "Belgium");
		europa.addChild(be);
		Country de = new Country("DE", "Germany");
		europa.addChild(de);
		Country fr = new Country("FR", "France");
		europa.addChild(fr);
		getEntityManager().persist(europa);

		Country jp = new Country("JP", "Japan");
		asia.addChild(jp);
		Country ch = new Country("CH", "China");
		asia.addChild(ch);
		Country th = new Country("TH", "Thailand");
		asia.addChild(th);
		getEntityManager().persist(asia);

		Currency euro = new Currency("EU", "Euro");
		getEntityManager().persist(euro);
		Currency usd = new Currency("USD", "United States Dollar");
		getEntityManager().persist(usd);
		Currency gpb = new Currency("GPB", "Pound Sterling");
		getEntityManager().persist(gpb);

	}

	@Test
	public void testAll() {
		List<Domain> all = domainService.findAll();
		Assert.assertEquals(12, all.size());
	}

	@Test
	public void testFindAllByType() {
		List<? extends Domain> all = domainService.findAllByType(Currency.class);
		Assert.assertEquals(3, all.size());
	}

	@Test
	public void testFindAllCurrencies() {
		List<Currency> all = currencyService.findAll((SortOrder) null);
		Assert.assertEquals(3, all.size());
	}

	@Test
	public void testFindChildren() {
		Domain deu = regionService.findByUniqueProperty("code", "EU", false);
		Assert.assertTrue(deu instanceof Region);
		Region eu = (Region) deu;
		Assert.assertEquals(4, eu.getChildren().size());
		List<Country> countries = domainService.findChildren(eu);
		Assert.assertEquals(4, countries.size());
		countries = regionService.findChildren(eu);
		Assert.assertEquals(4, countries.size());
	}
}
