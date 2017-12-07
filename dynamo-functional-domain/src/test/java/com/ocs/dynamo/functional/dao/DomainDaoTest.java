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
package com.ocs.dynamo.functional.dao;

import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ocs.dynamo.functional.domain.Country;
import com.ocs.dynamo.functional.domain.Currency;
import com.ocs.dynamo.functional.domain.domain.Domain;
import com.ocs.dynamo.functional.domain.Region;
import com.ocs.dynamo.test.BaseIntegrationTest;

/**
 * @author Patrick Deenen (patrick@opencircle.solutions)
 *
 */
public class DomainDaoTest extends BaseIntegrationTest {

    @Inject
    private DomainDao domainDao;

    @Inject
    private DomainDao regionDao;

    private Region europa;

    private Region asia;

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
        List<Domain> all = domainDao.findAll();
        Assert.assertEquals(12, all.size());
    }

    @Test
    public void testFindAllByType() {
        List<? extends Domain> all = domainDao.findAllByType(Currency.class);
        Assert.assertEquals(3, all.size());
    }

    @Test
    public void testFindChildren() {
        Domain deu = regionDao.findByUniqueProperty("code", "EU", false);
        Assert.assertTrue(deu instanceof Region);
        Region eu = (Region) deu;
        Assert.assertEquals(4, eu.getChildren().size());
        List<Country> countries = domainDao.findChildren(eu);
        Assert.assertEquals(4, countries.size());
        countries = regionDao.findChildren(eu);
        Assert.assertEquals(4, countries.size());
    }
}
