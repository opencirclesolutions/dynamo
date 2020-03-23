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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.ocs.dynamo.BackendIntegrationTest;
import com.ocs.dynamo.functional.domain.Country;
import com.ocs.dynamo.functional.domain.Currency;
import com.ocs.dynamo.functional.domain.Domain;
import com.ocs.dynamo.functional.domain.Region;

/**
 * @author Patrick Deenen (patrick@opencircle.solutions)
 *
 */
@SpringBootTest()
public class DomainDaoTest extends BackendIntegrationTest {

    @Inject
    private DomainDao domainDao;

    private Region europa;

    private Region asia;

    @BeforeEach
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
        assertEquals(12, all.size());
    }

    @Test
    public void testFindAllByType() {
        List<? extends Domain> all = domainDao.findAllByType(Currency.class);
        assertEquals(3, all.size());
    }

    @Test
    public void testFindChildren() {
        domainDao.findAll();
        Domain deu = domainDao.findByTypeAndUniqueProperty(Region.class, "code", "EU", false);
        assertTrue(deu instanceof Region);
        Region eu = (Region) deu;
        assertEquals(4, eu.getChildren().size());
        List<Country> countries = domainDao.findChildren(eu);
        assertEquals(4, countries.size());
        countries = domainDao.findChildren(eu);
        assertEquals(4, countries.size());
    }
}
