package com.ocs.dynamo.importer.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.ocs.dynamo.utils.DateUtils;

public class XlsRowImportTemplateTest extends BaseMockitoTest {

	@Mock
	private MessageService messageService;

	private BaseXlsImporter importer = new BaseXlsImporter();

	@Override
	public void setUp() throws Exception {
		super.setUp();

		MockUtil.mockMessageService(messageService);
	}

	@Test
	public void test() throws IOException {
		byte[] bytes = readFile("importer_rows.xlsx");
		List<String> errors = new ArrayList<>();

		XlsRowImportTemplate<String, PersonDTO> template = new XlsRowImportTemplate<String, PersonDTO>(importer,
		        messageService, bytes, errors, PersonDTO.class, 0, 1, 1, 9, false) {

			@Override
			protected String extractKey(PersonDTO record) {
				return null;
			}
		};
		List<PersonDTO> result = template.execute();
		Assert.assertEquals(2, result.size());

		Assert.assertTrue(errors.isEmpty());

		PersonDTO person = result.get(0);
		Assert.assertEquals("Bas", person.getName());
		Assert.assertEquals(com.ocs.dynamo.importer.impl.PersonDTO.Gender.M, person.getGender());
		Assert.assertEquals(DateUtils.createDate("04042014"), person.getDate());
		Assert.assertEquals(2.4, person.getFactor().doubleValue(), 0.001);
		Assert.assertEquals(Boolean.TRUE, person.getAbool());
	}

	/**
	 * Test with an incomplete record
	 * 
	 * @throws IOException
	 */
	@Test
	public void test2() throws IOException {
		byte[] bytes = readFile("importer_rows2.xlsx");
		List<String> errors = new ArrayList<>();

		XlsRowImportTemplate<String, PersonDTO> template = new XlsRowImportTemplate<String, PersonDTO>(importer,
		        messageService, bytes, errors, PersonDTO.class, 0, 1, 1, 9, false) {

			@Override
			protected String extractKey(PersonDTO record) {
				return null;
			}
		};
		List<PersonDTO> result = template.execute();
		Assert.assertEquals(2, result.size());

		// incomplete record
		Assert.assertEquals(1, errors.size());

	}

	/**
	 * 
	 * @throws IOException
	 */
	@Test
	public void test3_Duplicates() throws IOException {
		byte[] bytes = readFile("importer_rows3.xlsx");
		List<String> errors = new ArrayList<>();

		XlsRowImportTemplate<String, PersonDTO> template = new XlsRowImportTemplate<String, PersonDTO>(importer,
		        messageService, bytes, errors, PersonDTO.class, 0, 1, 1, 9, true) {

			@Override
			protected String extractKey(PersonDTO record) {
				return record.getName();
			}
		};
		List<PersonDTO> result = template.execute();
		Assert.assertEquals(1, result.size());

		// duplicate record
		Assert.assertEquals(1, errors.size());
		Assert.assertEquals("ocs.duplicate.row", errors.get(0));

	}

	private byte[] readFile(String fileName) throws IOException {
		return FileUtils.readFileToByteArray(new File("src/test/resources/" + fileName));
	}
}
