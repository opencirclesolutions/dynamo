///*
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// */
//package com.ocs.dynamo.jasperreports;
//
//import java.util.Arrays;
//import java.util.Collection;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import com.ocs.dynamo.ui.provider.BaseDataProvider;
//import com.ocs.dynamo.ui.provider.IdBasedDataProvider;
//
//import net.sf.jasperreports.engine.JRException;
//import net.sf.jasperreports.engine.design.JRDesignField;
//
///**
// * @author patrick.deenen@opencircle.solutions
// *
// */
//public class JRIndexedContainerDataSourceTest {
//
//	private BaseDataProvider<Integer, Person> provider;
//
//	/**
//	 * @throws java.lang.Exception
//	 */
//	@Before
//	public void setUp() throws Exception {
//
//		Person piet = new Person(1, "Piet");
//		Customer acme = new Customer("Acme", piet);
//		piet.setCustomer(acme);
//		Collection<Person> persons = Arrays.asList(piet, new Person(2, "Kees", acme), new Person(3, "Jan", acme));
//		provider = new IdBasedDataProvider()<>();
//		
//		
//		
//	}
//
//	/**
//	 * Test method for
//	 * {@link com.ocs.dynamo.jasperreports.JRContainerDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)}
//	 * .
//	 * 
//	 * @throws JRException
//	 */
//	@Test
//	public void testGetFieldValue() throws JRException {
//		JRDesignField name = new JRDesignField();
//		name.setName("name");
//		JRIndexedContainerDataSource<Integer, Person> ds = new JRIndexedContainerDataSource(container);
//		ds.moveFirst();
//		Assert.assertEquals("Piet", ds.getFieldValue(name));
//
//		JRDesignField customerName = new JRDesignField();
//		customerName.setName("customer_customerName");
//		Assert.assertEquals("Acme", ds.getFieldValue(customerName));
//
//		Assert.assertEquals(0, ds.getRecordIndex());
//		Assert.assertTrue(ds.next());
//		Assert.assertEquals("Kees", ds.getFieldValue(name));
//
//		ds.moveFirst();
//		Assert.assertEquals("Piet", ds.getFieldValue(name));
//		Assert.assertEquals(0, ds.getRecordIndex());
//	}
//
//}
