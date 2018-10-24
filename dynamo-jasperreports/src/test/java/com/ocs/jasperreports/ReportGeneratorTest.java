package com.ocs.jasperreports;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.mock.web.MockHttpSession;

import com.ocs.dynamo.jasperreports.Customer;
import com.ocs.dynamo.jasperreports.JRIndexedContainerDataSource;
import com.ocs.dynamo.jasperreports.JRUtils;
import com.ocs.dynamo.jasperreports.Person;
import com.vaadin.v7.data.util.BeanContainer;
import com.vaadin.v7.data.util.filter.And;
import com.vaadin.v7.data.util.filter.Compare;

import net.sf.jasperreports.engine.JasperReport;

public class ReportGeneratorTest {

	@Test
	@Ignore
	public void testExecuteReport() {
		BeanContainer<Integer, Person> container;

		Person piet = new Person(1, "Piet");
		piet.setId(1000);
		Customer acme = new Customer("Acme", "piet@acme.com", piet);
		piet.setCustomer(acme);
		Collection<Person> persons = Arrays.asList(piet, new Person(2, "Kees", acme), new Person(3, "Jan", acme));
		container = new BeanContainer<>(Person.class);
		container.setBeanIdProperty("socialId");
		container.addNestedContainerProperty("customer.customerName");
		container.addAll(persons);

		ReportGenerator reportGenerator = new ReportGenerator(null);

		final JasperReport jasperReport = reportGenerator.loadTemplate("test.jasper");
		assertNotNull(jasperReport);

		Person romeo = new Person(2, "Romeo");
		romeo.setId(2000);
		And filter = new And(new Compare.Equal("filterPropertyName", "someValue"), new Compare.Equal("person", piet),
				new Compare.LessOrEqual("person", romeo));
		Map<String, Object> fp = JRUtils.createParametersFromFilter(jasperReport, filter);
		assertNotNull(fp);
		assertTrue(fp.containsKey("someParameter"));
		assertEquals("someValue", fp.get("someParameter"));
		assertTrue(fp.containsKey("person_id"));
		assertEquals(1000, fp.get("person_id"));
		assertTrue(fp.containsKey("person_id_less_or_equal"));
		assertEquals(2000, fp.get("person_id_less_or_equal"));

		JRUtils.addContainerPropertiesFromReport(container, jasperReport);
		assertTrue(container.getContainerPropertyIds().contains("customer.email"));

		String result = reportGenerator.executeReportAsHtml(jasperReport, fp,
				new JRIndexedContainerDataSource(container), new MockHttpSession(), Locale.forLanguageTag("nl_NL"));

		assertNotNull(result);
		assertTrue(result.contains("piet@acme.com"));
		assertTrue(result.contains("someValue"));
	}

}
