package org.dynamoframework.importer.impl;

import org.apache.commons.io.FileUtils;
import org.dynamoframework.service.MessageService;
import org.dynamoframework.test.BaseMockitoTest;
import org.dynamoframework.test.MockUtil;
import org.dynamoframework.utils.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XlsRowImportTemplateTest extends BaseMockitoTest {

	@Mock
	private MessageService messageService;

	private final BaseXlsImporter importer = new BaseXlsImporter();

	@BeforeEach
	public void setUp() {
		MockUtil.mockMessageService(messageService);
	}

	@Test
	public void test() throws IOException {
		byte[] bytes = readFile("importer_rows.xlsx");
		List<String> errors = new ArrayList<>();

		XlsRowImportTemplate<String, PersonDTO> template = new XlsRowImportTemplate<>(importer,
				messageService, bytes, errors, PersonDTO.class, 0, 0, 1, 9, false) {

			@Override
			protected String extractKey(PersonDTO record) {
				return record.getName();
			}
		};
		List<PersonDTO> result = template.execute();
		assertEquals(2, result.size());

		assertTrue(errors.isEmpty());

		PersonDTO person = result.get(0);
		assertEquals("Bas", person.getName());
		assertEquals(PersonDTO.Gender.M, person.getGender());
		assertEquals(DateUtils.createLocalDate("04042014"), person.getDate());
		assertEquals(2.4, person.getFactor().doubleValue(), 0.001);
		assertEquals(Boolean.TRUE, person.getAbool());
	}

	@Test
	public void test2() throws IOException {
		byte[] bytes = readFile("importer_rows2.xlsx");
		List<String> errors = new ArrayList<>();

		XlsRowImportTemplate<String, PersonDTO> template = new XlsRowImportTemplate<>(importer,
				messageService, bytes, errors, PersonDTO.class, 0, 0, 1, 8, false) {

			@Override
			protected String extractKey(PersonDTO record) {
				return record.getName();
			}
		};
		List<PersonDTO> result = template.execute();
		assertEquals(2, result.size());

	}

	@Test
	public void test3_Duplicates() throws IOException {
		byte[] bytes = readFile("importer_rows3.xlsx");
		List<String> errors = new ArrayList<>();

		XlsRowImportTemplate<String, PersonDTO> template = new XlsRowImportTemplate<>(importer,
				messageService, bytes, errors, PersonDTO.class, 0, 0, 1, 9, true) {

			@Override
			protected String extractKey(PersonDTO record) {
				return record.getName();
			}
		};
		List<PersonDTO> result = template.execute();
		assertEquals(1, result.size());

		// duplicate record
		assertEquals(1, errors.size());
		assertEquals("dynamoframework.duplicate.row", errors.get(0));
	}

	@Test
	public void test4_Missing() throws IOException {
		byte[] bytes = readFile("importer_rows4.xlsx");
		List<String> errors = new ArrayList<>();

		XlsRowImportTemplate<String, PersonDTO> template = new XlsRowImportTemplate<>(importer,
				messageService, bytes, errors, PersonDTO.class, 0, 0, 1, 8, true) {

			@Override
			protected String extractKey(PersonDTO record) {
				return record.getName();
			}
		};
		List<PersonDTO> result = template.execute();
		assertEquals(1, result.size());

		// duplicate record
		assertEquals(1, errors.size());
		assertTrue(errors.get(0).contains("Required value for field 'number' is missing"));

	}

	private byte[] readFile(String fileName) throws IOException {
		return FileUtils.readFileToByteArray(new File("src/test/resources/" + fileName));
	}
}
